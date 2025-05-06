package io.github.inomnom.filepicker.internal.ui

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.inomnom.filepicker.R
import io.github.inomnom.filepicker.api.PickedFile
import io.github.inomnom.filepicker.api.PickerConfig
import io.github.inomnom.filepicker.api.PickerResult
import io.github.inomnom.filepicker.internal.PickerResultRegistry
import io.github.inomnom.filepicker.internal.permissions.PermissionHandler
import io.github.inomnom.filepicker.internal.permissions.PermissionsUtil
import io.github.inomnom.filepicker.internal.ui.PickerContract.EXTRA_CONFIG
import io.github.inomnom.filepicker.internal.ui.PickerContract.EXTRA_LAUNCH_MODE
import io.github.inomnom.filepicker.internal.ui.PickerContract.EXTRA_REQUEST_ID
import io.github.inomnom.filepicker.internal.utils.FileUtils
import io.github.inomnom.filepicker.internal.utils.ImageUtils
import io.github.inomnom.filepicker.internal.utils.parcelable
import io.github.inomnom.filepicker.internal.utils.serializable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

internal object PickerContract {
    const val EXTRA_CONFIG = "io.github.inomnom.filepicker.CONFIG"
    const val EXTRA_REQUEST_ID = "io.github.inomnom.filepicker.REQUEST_ID"
    const val EXTRA_LAUNCH_MODE = "io.github.inomnom.filepicker.LAUNCH_MODE"

    enum class LaunchMode {
        SHEET, CAMERA_ONLY, GALLERY_ONLY, FILES_ONLY
    }
}

internal class FilePickerActivity : AppCompatActivity(), FilePickerListener {

    private val TAG = FilePickerActivity::class.java.simpleName

    private lateinit var config: PickerConfig
    private lateinit var requestId: String
    private lateinit var launchMode: PickerContract.LaunchMode

    private var cameraOriginalFile: File? = null
    private var resultDispatched = false

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickVisualMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var pickMultipleVisualMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var openDocumentLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var openMultipleDocumentsLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!intent.hasExtra(EXTRA_CONFIG) || !intent.hasExtra(EXTRA_REQUEST_ID) || !intent.hasExtra(
                EXTRA_LAUNCH_MODE
            )
        ) {
            dispatchResult(PickerResult.Error(IllegalStateException("Picker session invalid.")))
            return
        }

        config = intent.parcelable(EXTRA_CONFIG)!!
        requestId = intent.getStringExtra(EXTRA_REQUEST_ID)!!
        launchMode = intent.serializable(EXTRA_LAUNCH_MODE)!!

        registerActivityLaunchers()
        handleLaunchMode()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                dispatchResult(PickerResult.Cancelled)
            }
        })
    }

    private fun registerActivityLaunchers() {
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                handleCameraResult(success)
            }
        pickVisualMediaLauncher =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                handleSingleUriResult(uri)
            }

        val maxItems = calculateMaxItemsForPickerRegistration()
        pickMultipleVisualMediaLauncher =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxItems)) { uris ->
                handleMultipleUrisResult(uris)
            }
        openDocumentLauncher =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                handleSingleUriResult(uri)
            }
        openMultipleDocumentsLauncher =
            registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
                handleOpenMultipleDocumentsResult(uris)
            }
    }

    private fun handleLaunchMode() {
        when (launchMode) {
            PickerContract.LaunchMode.SHEET -> showBottomSheet()
            PickerContract.LaunchMode.CAMERA_ONLY -> launchCameraInternal()
            PickerContract.LaunchMode.GALLERY_ONLY -> launchGalleryInternal()
            PickerContract.LaunchMode.FILES_ONLY -> launchFilesInternal()
        }
    }

    private var bottomSheet: FilePickerBottomSheet? = null
    private fun showBottomSheet() {
        val existingSheet =
            supportFragmentManager.findFragmentByTag(FilePickerBottomSheet::class.java.simpleName)
        if (existingSheet != null) {
            return
        }

        bottomSheet = FilePickerBottomSheet.newInstance(config = config)
        bottomSheet?.show(supportFragmentManager, FilePickerBottomSheet::class.java.simpleName)
    }

    private fun launchCameraInternal() {
        checkPermissionsAndExecute(
            permissions = getCameraPermissions(),
            action = {
                runCatching {
                    val file = FileUtils.createTempImageFile(this)
                    cameraOriginalFile = file
                    val uri = FileUtils.getFileUri(this, file)
                    takePictureLauncher.launch(uri)
                }.onFailure { error ->
                    dispatchResult(PickerResult.Error(error))
                }
            }
        )
    }

    private fun launchGalleryInternal() {
        checkPermissionsAndExecute(
            permissions = getGalleryPermissions(),
            action = {
                runCatching {
                    val request = PickVisualMediaRequest(determineVisualMediaType())
                    val launcher = if (config.allowMultipleSelection) {
                        pickMultipleVisualMediaLauncher
                    } else {
                        pickVisualMediaLauncher
                    }
                    launcher.launch(request)
                }.onFailure { error ->
                    dispatchResult(PickerResult.Error(error))
                }
            }
        )
    }

    private fun launchFilesInternal() {
        checkPermissionsAndExecute(
            permissions = getFilePermissions(),
            action = {
                runCatching {
                    val mimeTypes = config.specificMimeTypes.ifEmpty { arrayOf("*/*") }
                    val launcher = if (config.allowMultipleSelection) {
                        openMultipleDocumentsLauncher
                    } else {
                        openDocumentLauncher
                    }
                    launcher.launch(mimeTypes)
                }.onFailure { error ->
                    dispatchResult(PickerResult.Error(error))
                }
            }
        )
    }
    private fun checkPermissionsAndExecute(permissions: Array<String>, action: () -> Unit) {
        if (permissions.isEmpty()) {
            action()
            return
        }

        PermissionsUtil.check(
            context = this,
            permissions = permissions,
            showSettingsDialog = true,
            handler = object : PermissionHandler {
                override fun onGranted() {
                    action()
                }

                override fun onDenied() {
                    dispatchResult(
                        PickerResult.Error(
                            SecurityException(
                                getString(
                                    R.string.fp_permission_denied_x,
                                    permissions.joinToString()
                                )
                            )
                        )
                    )
                }
            }
        )
    }

    private fun getCameraPermissions(): Array<String> = arrayOf(Manifest.permission.CAMERA)
    private fun getGalleryPermissions(): Array<String> = emptyArray()
    private fun getFilePermissions(): Array<String> = emptyArray()

    private fun handleCameraResult(success: Boolean) {
        val originalFile = cameraOriginalFile
        cameraOriginalFile = null

        if (success && originalFile != null) {
            processCameraImage(originalFile)
        } else {
            FileUtils.cleanupFile(originalFile)
            bottomSheet?.actionTaken = false
        }
    }

    private fun processCameraImage(originalFile: File) {
        lifecycleScope.launch {
            runCatching {
                val resultFile = if (config.compressCameraImage) {
                    withContext(Dispatchers.IO) {
                        ImageUtils.compressImageFile(this@FilePickerActivity, originalFile)
                            .getOrThrow()
                    }
                } else {
                    originalFile
                }

                val resultUri = withContext(Dispatchers.IO) {
                    FileUtils.getFileUri(this@FilePickerActivity, resultFile)
                }

                if (config.compressCameraImage && resultFile != originalFile) {
                    FileUtils.cleanupFile(originalFile)
                }
                processUris(listOf(resultUri))
            }.onFailure { error ->
                FileUtils.cleanupFile(originalFile)
                dispatchResult(PickerResult.Error(error))
            }
        }
    }

    private fun handleSingleUriResult(uri: Uri?) {
        if (uri != null) {
            processUris(listOf(uri))
        } else {
            handleSystemPickerCancellation()
        }
    }

    private fun handleMultipleUrisResult(uris: List<Uri>) {
        if (uris.isNotEmpty()) {
            processUris(uris)
        } else {
            handleSystemPickerCancellation()
        }
    }

    private fun handleOpenMultipleDocumentsResult(uris: List<Uri>) {
        if (uris.isNotEmpty()) {
            val limit = config.maxSelectionCount?.takeIf { it > 0 }
            val finalUris = if (limit != null && uris.size > limit) {
                Log.w(
                    TAG,
                    "ACTION_OPEN_MULTIPLE_DOCUMENTS returned ${uris.size} items, exceeding limit of $limit. Truncating."
                )
                uris.take(limit)
            } else {
                uris
            }
            processUris(finalUris)
        } else {
            handleSystemPickerCancellation()
        }
    }

    private fun handleSystemPickerCancellation() {
        if (launchMode == PickerContract.LaunchMode.SHEET) {
            bottomSheet?.actionTaken = false
        } else {
            dispatchResult(PickerResult.Cancelled)
        }
    }

    private fun processUris(uris: List<Uri>) {
        lifecycleScope.launch {
            runCatching {
                val files = withContext(Dispatchers.IO) {
                    uris.map { uri ->
                        PickedFile.fromUri(applicationContext, uri)
                    }
                }
                if (files.isNotEmpty()) {
                    dispatchResult(PickerResult.Success(files))
                } else if (uris.isNotEmpty()) {
                    dispatchResult(PickerResult.Error(Exception(getString(R.string.fp_failed_to_process_any_of_the_selected_files))))
                }
            }.onFailure { error ->
                dispatchResult(PickerResult.Error(error))
            }
        }
    }

    private fun dispatchResult(result: PickerResult) {
        synchronized(this) {
            if (resultDispatched) return
            resultDispatched = true
        }

        PickerResultRegistry.deliverResult(requestId, result)
        FileUtils.cleanupFile(cameraOriginalFile)
        cameraOriginalFile = null

        bottomSheet?.dismiss()
    }

    private fun calculateMaxItemsForPickerRegistration(): Int {
        if (!config.allowMultipleSelection) return 2
        val configuredLimit = config.maxSelectionCount?.takeIf { it > 0 }
        val systemLimit = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(
                    this
                )
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    MediaStore.getPickImagesMaxLimit()
                } else {
                    Int.MAX_VALUE
                }
            } else Int.MAX_VALUE
        }.getOrDefault(Int.MAX_VALUE)

        return when {
            configuredLimit == null -> systemLimit
            systemLimit == Int.MAX_VALUE -> configuredLimit
            else -> minOf(configuredLimit, systemLimit)
        }
    }

    private fun determineVisualMediaType(): ActivityResultContracts.PickVisualMedia.VisualMediaType {
        val specificImageTypes =
            config.allowedMimeTypes.filter { it.startsWith("image/") && it != "image/*" }
        val specificVideoTypes =
            config.allowedMimeTypes.filter { it.startsWith("video/") && it != "video/*" }

        return when {
            specificImageTypes.size == 1 && config.allowedMimeTypes.none { it.startsWith("video/") } ->
                ActivityResultContracts.PickVisualMedia.SingleMimeType(specificImageTypes.first())

            specificVideoTypes.size == 1 && config.allowedMimeTypes.none { it.startsWith("image/") } ->
                ActivityResultContracts.PickVisualMedia.SingleMimeType(specificVideoTypes.first())
            config.allowsOnlyImages -> ActivityResultContracts.PickVisualMedia.ImageOnly
            config.allowsOnlyVideos -> ActivityResultContracts.PickVisualMedia.VideoOnly
            else -> ActivityResultContracts.PickVisualMedia.ImageAndVideo
        }
    }

    override fun onCameraAction() = launchCameraInternal()

    override fun onGalleryAction() = launchGalleryInternal()

    override fun onFilesAction() = launchFilesInternal()

    override fun onPickerCancel() = dispatchResult(PickerResult.Cancelled)

}