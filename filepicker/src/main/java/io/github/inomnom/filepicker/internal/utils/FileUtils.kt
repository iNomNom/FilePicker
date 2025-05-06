package io.github.inomnom.filepicker.internal.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

internal object FileUtils {

    private val TAG = FileUtils::class.java.simpleName

    @Throws(IOException::class, IllegalArgumentException::class)
    fun createTempImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: throw IOException("Cannot get external files directory for pictures.")
        if (!storageDir.exists()) storageDir.mkdirs()
        val file = File.createTempFile("JPEG_ORIG_${timeStamp}_", ".jpg", storageDir)
        return file
    }

    @Throws(IllegalArgumentException::class)
    fun getFileUri(context: Context, file: File): Uri {
        val authority = "${context.applicationContext.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    fun cleanupFile(fileToClean: File?) {
        fileToClean ?: return
        runCatching {
            if (fileToClean.exists()) {
                if (!fileToClean.delete()) {
                    Log.w(TAG, "Failed to delete file: ${fileToClean.path}")
                } else {
                    Log.d(TAG, "Cleaned up file: ${fileToClean.path}")
                }
            }
        }.onFailure {
            Log.w(TAG, "Error during file cleanup: ${fileToClean.path}", it)
        }
    }
}