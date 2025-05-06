package io.github.inomnom.filepicker.internal.launcher

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageAndVideo
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.SingleMimeType
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.VideoOnly
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.VisualMediaType
import io.github.inomnom.filepicker.api.PickerConfig
import io.github.inomnom.filepicker.core.MimeType
import io.github.inomnom.filepicker.internal.utils.FileUtils
import java.io.File

internal class IntentLauncher(
    private val context: Context,
    private val config: PickerConfig,
    private val takePictureLauncher: ActivityResultLauncher<Uri>,
    private val pickVisualMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    private val pickMultipleVisualMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    private val openDocumentLauncher: ActivityResultLauncher<Array<String>>,
    private val openMultipleDocumentsLauncher: ActivityResultLauncher<Array<String>>
) {

    companion object {
        private const val TAG = "IntentLauncher"
        private const val DEBOUNCE_DELAY = 1000L
    }

    private var lastLaunchTime: Long = 0

    private inline fun <T> withThrottling(crossinline block: () -> T): T? {
        val currentTime = System.currentTimeMillis()
        return if (currentTime - lastLaunchTime >= DEBOUNCE_DELAY) {
            lastLaunchTime = currentTime
            block()
        } else {
            Log.d(TAG, "Launch prevented: too soon since last launch")
            null
        }
    }

    fun launchCameraIntent(): File? = withThrottling {
        runCatching {
            val file = FileUtils.createTempImageFile(context)
            val uri = FileUtils.getFileUri(context, file)
            takePictureLauncher.launch(uri)
            file
        }.onFailure { error ->
            Log.e(TAG, "Failed to prepare/launch camera intent", error)
        }.getOrNull()
    }

    fun launchGalleryIntent() = withThrottling {
        val request = PickVisualMediaRequest(determineVisualMediaType())
        val launcher = if (config.allowMultipleSelection) {
            pickMultipleVisualMediaLauncher
        } else {
            pickVisualMediaLauncher
        }
        launcher.launch(request)
    }

    fun launchFileIntent() = withThrottling {
        val mimeTypes = config.allowedMimeTypes
            .ifEmpty { listOf(MimeType.ANY) }
            .toTypedArray()

        val launcher = if (config.allowMultipleSelection) {
            openMultipleDocumentsLauncher
        } else {
            openDocumentLauncher
        }
        launcher.launch(mimeTypes)
    }

    private fun determineVisualMediaType(): VisualMediaType {
        val specificImageTypes =
            config.allowedMimeTypes.filter { it.startsWith("image/") && it != MimeType.IMAGE_ANY }
        val specificVideoTypes =
            config.allowedMimeTypes.filter { it.startsWith("video/") && it != MimeType.VIDEO_ANY }

        return when {
            specificImageTypes.size == 1 && config.allowedMimeTypes.none { it.startsWith("video/") } ->
                SingleMimeType(specificImageTypes.first())

            specificVideoTypes.size == 1 && config.allowedMimeTypes.none { it.startsWith("image/") } ->
                SingleMimeType(specificVideoTypes.first())

            config.allowsOnlyImages -> ImageOnly
            config.allowsOnlyVideos -> VideoOnly

            else -> ImageAndVideo
        }
    }
}