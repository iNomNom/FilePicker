// File: /mnt/data/filepicker_extracted/api/FilePicker.kt
package io.github.inomnom.filepicker.api

import android.app.Activity
import android.content.Context
import android.content.Intent
import io.github.inomnom.filepicker.internal.PickerResultRegistry
import io.github.inomnom.filepicker.internal.ui.FilePickerActivity
import io.github.inomnom.filepicker.internal.ui.PickerContract
import io.github.inomnom.filepicker.internal.ui.PickerContract.EXTRA_CONFIG
import io.github.inomnom.filepicker.internal.ui.PickerContract.EXTRA_LAUNCH_MODE
import io.github.inomnom.filepicker.internal.ui.PickerContract.EXTRA_REQUEST_ID
import java.util.UUID

/**
 * Provides simple entry points for launching file picking operations.
 * This object handles the underlying complexity of permissions, activity results,
 * and lifecycle management.
 */
object FilePicker {

    /**
     * Shows a file picker bottom sheet UI allowing the user to choose between
     * Camera, Gallery/Photos, or Files/Documents based on the provided configuration.
     */
    @JvmStatic
    fun showSheet(
        context: Context,
        config: PickerConfig = PickerConfig(),
        onResult: (PickerResult) -> Unit
    ) {
        launch(context, config, PickerContract.LaunchMode.SHEET, onResult)
    }

    /**
     * Launches the device camera directly to capture an image.
     */
    @JvmStatic
    fun launchCamera(
        context: Context,
        config: PickerConfig,
        onResult: (PickerResult) -> Unit
    ) {
        if (!config.allowsImages) {
            onResult(PickerResult.Error(IllegalArgumentException("PickerConfig must allow image types to launch the camera.")))
            return
        }
        launch(context, config, PickerContract.LaunchMode.CAMERA_ONLY, onResult)
    }

    /**
     * Launches the system's gallery or photo picker directly.
     */
    @JvmStatic
    fun launchGallery(
        context: Context,
        config: PickerConfig,
        onResult: (PickerResult) -> Unit

    ) {
        if (!config.allowsImagesOrVideos) {
            onResult(PickerResult.Error(IllegalArgumentException("PickerConfig must allow image or video types to launch the gallery.")))
            return
        }
        launch(context, config, PickerContract.LaunchMode.GALLERY_ONLY, onResult)
    }

    /**
     * Launches the system's file or document picker directly.
     */
    @JvmStatic
    fun launchFiles(
        context: Context,
        config: PickerConfig,
        onResult: (PickerResult) -> Unit
    ) {
        if (!config.allowsFiles) {
            onResult(PickerResult.Error(IllegalArgumentException("PickerConfig must have allowsFiles=true to launch the file browser.")))
            return
        }
        launch(context, config, PickerContract.LaunchMode.FILES_ONLY, onResult)
    }

    private fun launch(
        context: Context?,
        config: PickerConfig,
        mode: PickerContract.LaunchMode,
        onResult: (PickerResult) -> Unit
    ) {
        val appContext = context?.applicationContext
        if (appContext == null) {
            onResult(PickerResult.Error(IllegalStateException("Could not get context from FragmentManager.")))
            return
        }

        val requestId = UUID.randomUUID().toString()
        PickerResultRegistry.register(requestId, onResult)

        val intent = Intent(appContext, FilePickerActivity::class.java).apply {
            putExtra(EXTRA_CONFIG, config)
            putExtra(EXTRA_REQUEST_ID, requestId)
            putExtra(EXTRA_LAUNCH_MODE, mode)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        appContext.startActivity(intent)
    }

}