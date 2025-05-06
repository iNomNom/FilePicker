package io.github.inomnom.filepicker.internal.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

internal object ImageUtils {
    private val TAG = ImageUtils::class.simpleName
    fun compressImageFile(
        context: Context,
        originalFile: File,
        targetMaxWidth: Float = 1080f,
        targetMaxHeight: Float = 1920f,
        quality: Int = 100
    ): Result<File> = runCatching {
        val filePath = originalFile.absolutePath

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(filePath, options)

        val (actualWidth, actualHeight) = options.run { outWidth to outHeight }
        require(actualHeight > 0 && actualWidth > 0) { "Invalid image bounds for $filePath" }

        val (targetWidth, targetHeight) = calculateTargetDimensions(
            actualWidth, actualHeight, targetMaxWidth, targetMaxHeight
        )

        options.apply {
            inSampleSize = calculateInSampleSize(this, targetWidth, targetHeight)
            inJustDecodeBounds = false
        }

        val sourceBitmap = BitmapFactory.decodeFile(filePath, options)
            ?: error("Failed to decode sampled bitmap for $filePath")

        val scaledBitmap = sourceBitmap.scaleToTargetSize(targetWidth, targetHeight)
            .also { if (it != sourceBitmap) sourceBitmap.recycle() }

        val rotatedBitmap = scaledBitmap.rotateBitmapByExifData(filePath)
            .also { if (it != scaledBitmap) scaledBitmap.recycle() }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val compressedFile = File(
            context.cacheDir,
            "COMP_${timeStamp}_${originalFile.nameWithoutExtension}.jpg"
        )

        FileOutputStream(compressedFile).use { out ->
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }

        rotatedBitmap.recycle()
        compressedFile
    }

    private fun calculateTargetDimensions(
        actualWidth: Int,
        actualHeight: Int,
        targetMaxWidth: Float,
        targetMaxHeight: Float
    ): Pair<Int, Int> {
        if (actualHeight <= targetMaxHeight && actualWidth <= targetMaxWidth) {
            return actualWidth to actualHeight
        }

        val imgRatio = actualWidth.toFloat() / actualHeight
        val maxRatio = targetMaxWidth / targetMaxHeight

        return when {
            imgRatio < maxRatio -> {
                val ratio = targetMaxHeight / actualHeight
                (ratio * actualWidth).roundToInt() to targetMaxHeight.roundToInt()
            }

            imgRatio > maxRatio -> {
                val ratio = targetMaxWidth / actualWidth
                targetMaxWidth.roundToInt() to (ratio * actualHeight).roundToInt()
            }

            else -> targetMaxWidth.roundToInt() to targetMaxHeight.roundToInt()
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height, width) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        val totalPixels = width.toFloat() * height
        val totalReqPixelsCap = reqWidth.toFloat() * reqHeight * 2

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize *= 2
        }

        return inSampleSize
    }

    private fun Bitmap.scaleToTargetSize(targetWidth: Int, targetHeight: Int): Bitmap {
        val finalTargetWidth = targetWidth.coerceAtLeast(1)
        val finalTargetHeight = targetHeight.coerceAtLeast(1)

        return runCatching {
            scale(finalTargetWidth, finalTargetHeight, true)
        }.getOrElse {
            Log.w(TAG, "Bitmap.scale failed ($it), using original bitmap")
            this
        }
    }

    private fun Bitmap.rotateBitmapByExifData(filePath: String): Bitmap {
        return runCatching {
            val exif = ExifInterface(filePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            when (orientation) {
                ExifInterface.ORIENTATION_NORMAL -> this
                else -> {
                    val matrix = Matrix().apply {
                        when (orientation) {
                            ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
                            ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                            ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
                        }
                    }

                    Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
                }
            }
        }.getOrElse {
            Log.e(TAG, "Failed to read EXIF or rotate bitmap for $filePath", it)
            this
        }
    }
}