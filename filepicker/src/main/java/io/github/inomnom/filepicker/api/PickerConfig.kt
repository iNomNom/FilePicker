package io.github.inomnom.filepicker.api

import android.os.Parcelable
import android.util.Log
import io.github.inomnom.filepicker.core.MimeType
import kotlinx.parcelize.Parcelize

/**
 * Configuration for the File Picker library component.
 * Defines allowed file types, selection limits, and available sources.
 *
 * @property allowedMimeTypes Set of allowed MIME types (e.g., "image/jpeg").
 *                           Use constants from [MimeType].
 *                           Default: `setOf(MimeType.ANY)`.
 * @property allowMultipleSelection Whether multiple files can be selected. Default: `false`.
 * @property maxSelectionCount Maximum number of files for multiple selection.
 *                             `null` or <= 0 means no specific limit.
 *                             Default: `null`.
 * @property allowCamera Whether to show the Camera option. Only applies if image types are allowed.
 *                      Default: `true` if images allowed, `false` otherwise.
 * @property allowGallery Whether to show the Photos/Gallery option. Only applies if image/video types are allowed.
 *                        Default: `true` if image/video allowed, `false` otherwise.
 * @property allowsFiles Whether to show the Files/Documents option. Default: `true`.
 * @property compressCameraImage Whether to compress images taken with the camera. Default: `false`.
 */
@Parcelize
data class PickerConfig(
    val allowedMimeTypes: Set<String> = setOf(MimeType.ANY),
    var allowMultipleSelection: Boolean = false,
    val maxSelectionCount: Int? = null,
    val allowCamera: Boolean = allowedMimeTypes.any { it.startsWith("image/") || it == MimeType.ANY },
    val allowGallery: Boolean = allowedMimeTypes.any {
        it.startsWith("image/") || it.startsWith("video/") || it == MimeType.ANY
    },
    val allowsFiles: Boolean = true,
    val compressCameraImage: Boolean = false
) : Parcelable {
    internal val allowsImages: Boolean
        get() = allowedMimeTypes.any { it.startsWith("image/") || it == MimeType.ANY }

    internal val allowsVideos: Boolean
        get() = allowedMimeTypes.any { it.startsWith("video/") || it == MimeType.ANY }

    internal val allowsOnlyImages: Boolean
        get() = allowedMimeTypes.isNotEmpty() &&
                allowedMimeTypes.all { it.startsWith("image/") }

    internal val allowsOnlyVideos: Boolean
        get() = allowedMimeTypes.isNotEmpty() &&
                allowedMimeTypes.all { it.startsWith("video/") }

    internal val allowsImagesOrVideos: Boolean
        get() = allowsImages || allowsVideos

    internal val specificMimeTypes: Array<String>
        get() = allowedMimeTypes.filter { it != MimeType.ANY }.toTypedArray()

    init {
        if (maxSelectionCount != null && maxSelectionCount <= 0) {
            Log.i(TAG, "maxSelectionCount <= 0 treated as no limit")
        }
        if (maxSelectionCount == 1 && allowMultipleSelection) {
            allowMultipleSelection = false
            Log.w(TAG, "maxSelectionCount=1 with allowMultipleSelection=true will behave as single selection")
        }
        if (maxSelectionCount != null && !allowMultipleSelection) {
            Log.i(TAG, "maxSelectionCount is set but allowMultipleSelection=false; limit will be ignored")
        }
    }

    companion object {
        private const val TAG = "PickerConfig"

        /** Creates configuration for single image selection */
        @JvmStatic
        fun singleImage(
            specificTypes: Set<String> = setOf(MimeType.IMAGE_ANY),
            compress: Boolean = false
        ) = PickerConfig(
            allowedMimeTypes = specificTypes,
            allowMultipleSelection = false,
            maxSelectionCount = 1,
            allowsFiles = false,
            compressCameraImage = compress
        )

        /** Creates configuration for multiple image selection */
        @JvmStatic
        fun multipleImages(
            maxCount: Int? = null,
            specificTypes: Set<String> = setOf(MimeType.IMAGE_ANY),
            compress: Boolean = false
        ) = PickerConfig(
            allowedMimeTypes = specificTypes,
            allowMultipleSelection = true,
            maxSelectionCount = maxCount,
            allowsFiles = false,
            compressCameraImage = compress
        )

        /** Creates configuration for single video selection */
        @JvmStatic
        fun singleVideo(specificTypes: Set<String> = setOf(MimeType.VIDEO_ANY)) = PickerConfig(
            allowedMimeTypes = specificTypes,
            allowMultipleSelection = false,
            maxSelectionCount = 1,
            allowCamera = false,
            allowsFiles = false,
            compressCameraImage = false
        )

        /** Creates configuration for multiple video selection */
        @JvmStatic
        fun multipleVideos(
            maxCount: Int? = null,
            specificTypes: Set<String> = setOf(MimeType.VIDEO_ANY)
        ) = PickerConfig(
            allowedMimeTypes = specificTypes,
            allowMultipleSelection = true,
            maxSelectionCount = maxCount,
            allowCamera = false,
            allowsFiles = false,
            compressCameraImage = false
        )

        /** Creates configuration for single image or video selection */
        @JvmStatic
        fun singleMedia(
            imageTypes: Set<String> = setOf(MimeType.IMAGE_ANY),
            videoTypes: Set<String> = setOf(MimeType.VIDEO_ANY),
            compress: Boolean = false
        ) = PickerConfig(
            allowedMimeTypes = imageTypes + videoTypes,
            allowMultipleSelection = false,
            maxSelectionCount = 1,
            allowsFiles = false,
            compressCameraImage = compress
        )

        /** Creates configuration for multiple image/video selection */
        @JvmStatic
        fun multipleMedia(
            maxCount: Int? = null,
            imageTypes: Set<String> = setOf(MimeType.IMAGE_ANY),
            videoTypes: Set<String> = setOf(MimeType.VIDEO_ANY),
            compress: Boolean = false
        ) = PickerConfig(
            allowedMimeTypes = imageTypes + videoTypes,
            allowMultipleSelection = true,
            maxSelectionCount = maxCount,
            allowsFiles = false,
            compressCameraImage = compress
        )

        /** Creates configuration for single PDF selection */
        @JvmStatic
        fun singlePdf() = PickerConfig(
            allowedMimeTypes = setOf(MimeType.APPLICATION_PDF),
            allowMultipleSelection = false,
            maxSelectionCount = 1,
            compressCameraImage = false
        )

        /** Creates configuration for multiple PDF selection */
        @JvmStatic
        fun multiplePdfs(maxCount: Int? = null) = PickerConfig(
            allowedMimeTypes = setOf(MimeType.APPLICATION_PDF),
            allowMultipleSelection = true,
            maxSelectionCount = maxCount,
            compressCameraImage = false
        )

        /** Creates configuration for single document selection */
        @JvmStatic
        fun singleDocument(specificTypes: Set<String> = MimeType.DOCUMENT_TYPES) = PickerConfig(
            allowedMimeTypes = specificTypes,
            allowMultipleSelection = false,
            maxSelectionCount = 1,
            compressCameraImage = false
        )

        /** Creates configuration for multiple document selection */
        @JvmStatic
        fun multipleDocuments(
            maxCount: Int? = null,
            specificTypes: Set<String> = MimeType.DOCUMENT_TYPES
        ) = PickerConfig(
            allowedMimeTypes = specificTypes,
            allowMultipleSelection = true,
            maxSelectionCount = maxCount,
            compressCameraImage = false
        )

        /** Creates configuration for single audio selection */
        @JvmStatic
        fun singleAudio(specificTypes: Set<String> = setOf(MimeType.AUDIO_ANY)) = PickerConfig(
            allowedMimeTypes = specificTypes,
            allowMultipleSelection = false,
            maxSelectionCount = 1,
            allowCamera = false,
            allowGallery = false,
            compressCameraImage = false
        )

        /** Creates configuration for multiple audio selection */
        @JvmStatic
        fun multipleAudio(
            maxCount: Int? = null,
            specificTypes: Set<String> = setOf(MimeType.AUDIO_ANY)
        ) = PickerConfig(
            allowedMimeTypes = specificTypes,
            allowMultipleSelection = true,
            maxSelectionCount = maxCount,
            allowCamera = false,
            allowGallery = false,
            compressCameraImage = false
        )

        /** Creates configuration for single archive selection */
        @JvmStatic
        fun singleArchive(specificTypes: Set<String> = MimeType.ARCHIVE_TYPES) = PickerConfig(
            allowedMimeTypes = specificTypes,
            allowMultipleSelection = false,
            maxSelectionCount = 1,
            allowCamera = false,
            allowGallery = false,
            compressCameraImage = false
        )

        /** Creates configuration for multiple archive selection */
        @JvmStatic
        fun multipleArchives(
            maxCount: Int? = null,
            specificTypes: Set<String> = MimeType.ARCHIVE_TYPES
        ) = PickerConfig(
            allowedMimeTypes = specificTypes,
            allowMultipleSelection = true,
            maxSelectionCount = maxCount,
            allowCamera = false,
            allowGallery = false,
            compressCameraImage = false
        )

        /** Creates configuration for single file selection of any type */
        @JvmStatic
        fun singleFile(allowedTypes: Set<String> = setOf(MimeType.ANY)) = PickerConfig(
            allowedMimeTypes = allowedTypes,
            allowMultipleSelection = false,
            maxSelectionCount = 1,
            compressCameraImage = false
        )

        /** Creates configuration for multiple file selection of any type */
        @JvmStatic
        fun multipleFiles(
            maxCount: Int? = null,
            allowedTypes: Set<String> = setOf(MimeType.ANY)
        ) = PickerConfig(
            allowedMimeTypes = allowedTypes,
            allowMultipleSelection = true,
            maxSelectionCount = maxCount,
            compressCameraImage = false
        )

        /**
         * Creates a custom picker configuration for advanced scenarios
         * with fine-grained control over all options.
         *
         * @param allowedMimeTypes Set of MIME types allowed for selection
         * @param allowMultipleSelection Whether multiple files can be selected
         * @param maxSelectionCount Optional maximum number of files to select
         * @param allowCamera Whether to show camera option (null uses default based on MIME types)
         * @param allowGallery Whether to show gallery option (null uses default based on MIME types)
         * @param allowFiles Whether to show files/documents option
         * @param compressCameraImage Whether to compress camera images
         * @return Configured PickerConfig
         */
        @JvmStatic
        fun custom(
            allowedMimeTypes: Set<String>,
            allowMultipleSelection: Boolean,
            maxSelectionCount: Int? = null,
            allowCamera: Boolean? = null,
            allowGallery: Boolean? = null,
            allowFiles: Boolean = true,
            compressCameraImage: Boolean = false
        ): PickerConfig {
            val finalAllowCamera = allowCamera
                ?: allowedMimeTypes.any { it.startsWith("image/") || it == MimeType.ANY }
            val finalAllowGallery = allowGallery
                ?: allowedMimeTypes.any {
                    it.startsWith("image/") || it.startsWith("video/") || it == MimeType.ANY
                }

            return PickerConfig(
                allowedMimeTypes,
                allowMultipleSelection,
                maxSelectionCount,
                finalAllowCamera,
                finalAllowGallery,
                allowFiles,
                compressCameraImage
            )
        }
    }
}