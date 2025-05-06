package io.github.inomnom.filepicker.core

import io.github.inomnom.filepicker.api.PickerConfig

/**
 * Provides standard MIME type constants for use with [PickerConfig].
 *
 * This object offers both individual MIME types and convenience sets of related types
 * for common file categories (images, videos, documents, etc.).
 *
 * Usage examples:
 * - For any file: `MimeType.ANY`
 * - For all images: `MimeType.IMAGE_ANY`
 * - For specific image types: `MimeType.IMAGE_JPEG` or use `MimeType.IMAGE_TYPES` set
 * - For documents: Use `MimeType.DOCUMENT_TYPES` set
 */
object MimeType {
    /** Represents any file type */
    const val ANY = "*/*"

    // ---- Image Types ----
    /** Represents any image type */
    const val IMAGE_ANY = "image/*"
    /** JPEG image format (.jpg, .jpeg) */
    const val IMAGE_JPEG = "image/jpeg"
    /** PNG image format (.png) */
    const val IMAGE_PNG = "image/png"
    /** GIF image format (.gif) */
    const val IMAGE_GIF = "image/gif"
    /** WebP image format (.webp) */
    const val IMAGE_WEBP = "image/webp"
    /** Bitmap image format (.bmp) */
    const val IMAGE_BMP = "image/bmp"
    /** HEIC image format (.heic) */
    const val IMAGE_HEIC = "image/heic"

    // ---- Video Types ----
    /** Represents any video type */
    const val VIDEO_ANY = "video/*"
    /** MP4 video format (.mp4) */
    const val VIDEO_MP4 = "video/mp4"
    /** 3GPP video format (.3gp) - common for mobile devices */
    const val VIDEO_3GP = "video/3gpp"
    /** AVI video format (.avi) */
    const val VIDEO_AVI = "video/x-msvideo"
    /** QuickTime video format (.mov) */
    const val VIDEO_MOV = "video/quicktime"
    /** WebM video format (.webm) */
    const val VIDEO_WEBM = "video/webm"

    // ---- Audio Types ----
    /** Represents any audio type */
    const val AUDIO_ANY = "audio/*"
    /** MP3 audio format (.mp3) */
    const val AUDIO_MPEG = "audio/mpeg"
    /** Ogg Vorbis audio format (.ogg) */
    const val AUDIO_OGG = "audio/ogg"
    /** WAV audio format (.wav) */
    const val AUDIO_WAV = "audio/wav"
    /** AAC audio format (.aac) */
    const val AUDIO_AAC = "audio/aac"

    // ---- Document Types ----
    /** PDF document format (.pdf) */
    const val APPLICATION_PDF = "application/pdf"
    /** Microsoft Word document - legacy format (.doc) */
    const val APPLICATION_MSWORD = "application/msword"
    /** Microsoft Word document - modern format (.docx) */
    const val APPLICATION_MSWORD_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    /** Microsoft Excel spreadsheet - legacy format (.xls) */
    const val APPLICATION_MSEXCEL = "application/vnd.ms-excel"
    /** Microsoft Excel spreadsheet - modern format (.xlsx) */
    const val APPLICATION_MSEXCEL_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    /** Microsoft PowerPoint presentation - legacy format (.ppt) */
    const val APPLICATION_MSPOWERPOINT = "application/vnd.ms-powerpoint"
    /** Microsoft PowerPoint presentation - modern format (.pptx) */
    const val APPLICATION_MSPOWERPOINT_PPTX = "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    /** OpenDocument text (.odt) */
    const val APPLICATION_ODT = "application/vnd.oasis.opendocument.text"
    /** OpenDocument spreadsheet (.ods) */
    const val APPLICATION_ODS = "application/vnd.oasis.opendocument.spreadsheet"
    /** OpenDocument presentation (.odp) */
    const val APPLICATION_ODP = "application/vnd.oasis.opendocument.presentation"
    /** Rich Text Format (.rtf) */
    const val APPLICATION_RTF = "application/rtf"
    /** Plain text format (.txt) */
    const val TEXT_PLAIN = "text/plain"
    /** CSV (Comma-Separated Values) format (.csv) */
    const val TEXT_CSV = "text/csv"
    /** HTML format (.html, .htm) */
    const val TEXT_HTML = "text/html"
    /** XML format (.xml) */
    const val APPLICATION_XML = "application/xml"
    /** JSON format (.json) */
    const val APPLICATION_JSON = "application/json"

    // ---- Archive Types ----
    /** ZIP archive format (.zip) */
    const val APPLICATION_ZIP = "application/zip"
    /** RAR archive format (.rar) */
    const val APPLICATION_RAR = "application/vnd.rar"
    /** TAR archive format (.tar) */
    const val APPLICATION_TAR = "application/x-tar"
    /** 7-Zip archive format (.7z) */
    const val APPLICATION_7Z = "application/x-7z-compressed"

    // ---- Developer / Code File Types ----
    /** Java source file (.java) */
    const val TEXT_JAVA = "text/x-java-source"
    /** Python source file (.py) */
    const val TEXT_PYTHON = "text/x-python"
    /** Kotlin source file (.kt) */
    const val TEXT_KOTLIN = "text/x-kotlin"
    /** JavaScript file (.js) */
    const val TEXT_JS = "application/javascript"
    /** TypeScript file (.ts) */
    const val TEXT_TYPESCRIPT = "application/x-typescript"

    // ---- Grouped Types (Convenience Sets) ----

    /**
     * Common image formats (JPEG, PNG, GIF, WebP, BMP, HEIC)
     *
     * Use when you want to specifically allow common image formats
     * rather than all possible image types (which would use IMAGE_ANY).
     */
    @JvmStatic val IMAGE_TYPES = setOf(
        IMAGE_JPEG,
        IMAGE_PNG,
        IMAGE_GIF,
        IMAGE_WEBP,
        IMAGE_BMP,
        IMAGE_HEIC
    )

    /**
     * Common video formats (MP4, 3GP, AVI, MOV, WebM)
     *
     * Use when you want to specifically allow common video formats
     * rather than all possible video types (which would use VIDEO_ANY).
     */
    @JvmStatic val VIDEO_TYPES = setOf(
        VIDEO_MP4,
        VIDEO_3GP,
        VIDEO_AVI,
        VIDEO_MOV,
        VIDEO_WEBM
    )

    /**
     * Common audio formats (MP3, OGG, WAV, AAC)
     *
     * Use when you want to specifically allow common audio formats
     * rather than all possible audio types (which would use AUDIO_ANY).
     */
    @JvmStatic val AUDIO_TYPES = setOf(
        AUDIO_MPEG,
        AUDIO_OGG,
        AUDIO_WAV,
        AUDIO_AAC
    )

    /**
     * Common document formats including Microsoft Office, OpenDocument, PDF, text, HTML, and JSON/XML
     */
    @JvmStatic val DOCUMENT_TYPES = setOf(
        APPLICATION_PDF,
        APPLICATION_MSWORD,
        APPLICATION_MSWORD_DOCX,
        APPLICATION_MSEXCEL,
        APPLICATION_MSEXCEL_XLSX,
        APPLICATION_MSPOWERPOINT,
        APPLICATION_MSPOWERPOINT_PPTX,
        APPLICATION_ODT,
        APPLICATION_ODS,
        APPLICATION_ODP,
        TEXT_PLAIN,
        TEXT_CSV,
        TEXT_HTML,
        APPLICATION_RTF,
        APPLICATION_XML,
        APPLICATION_JSON
    )

    /**
     * Common archive formats (ZIP, RAR, TAR, 7Z)
     */
    @JvmStatic val ARCHIVE_TYPES = setOf(
        APPLICATION_ZIP,
        APPLICATION_RAR,
        APPLICATION_TAR,
        APPLICATION_7Z
    )

    /**
     * Common programming-related file types
     */
    @JvmStatic val CODE_TYPES = setOf(
        TEXT_JAVA,
        TEXT_PYTHON,
        TEXT_KOTLIN,
        TEXT_JS,
        TEXT_TYPESCRIPT,
        APPLICATION_XML,
        APPLICATION_JSON
    )


    internal enum class Category {
        IMAGES_ONLY,
        VIDEOS_ONLY,
        IMAGES_AND_VIDEOS,
        DOCUMENTS_ONLY,
        FILES_ONLY,
        FILES_AND_DOCUMENTS
    }

    internal data class Analysis(
        val effectiveImageTypes: Set<String>,
        val effectiveVideoTypes: Set<String>,
        val effectiveDocumentTypes: Set<String>,
        val effectiveAudioTypes: Set<String>,
        val allowsAnyType: Boolean,
        val category: Category
    )

    /**
     * Analyzes a PickerConfig to determine the effective MIME types and the overall category.
     *
     * @param config The PickerConfig to analyze.
     * @return A [MimeType.Analysis] object containing the results.
     */
    @JvmStatic
    internal fun analyze(config: PickerConfig): Analysis {
        val hasSpecificTypes = config.allowedMimeTypes.filterNot { it == ANY }.isNotEmpty()
        val allowsAnyType = config.allowedMimeTypes.contains(ANY)

        val effectiveImageTypes: Set<String> = if (hasSpecificTypes) {
            config.allowedMimeTypes.filter { it.startsWith("image/") }.toSet()
        } else if (config.allowsImages) {
            setOf(IMAGE_ANY)
        } else {
            emptySet()
        }

        val effectiveVideoTypes: Set<String> = if (hasSpecificTypes) {
            config.allowedMimeTypes.filter { it.startsWith("video/") }.toSet()
        } else if (config.allowsVideos) {
            setOf(VIDEO_ANY)
        } else {
            emptySet()
        }

        val effectiveAudioTypes: Set<String> = if (hasSpecificTypes) {
            config.allowedMimeTypes.filter { it.startsWith("audio/") }.toSet()
        } else if (config.allowsFiles) {
            if (allowsAnyType) setOf(AUDIO_ANY) else emptySet()
        }
        else {
            emptySet()
        }

        val effectiveDocumentTypes: Set<String> = if (hasSpecificTypes) {
            config.allowedMimeTypes.filterNot {
                it.startsWith("image/") || it.startsWith("video/") || it.startsWith("audio/") || it == ANY
            }.toSet()
        } else if (config.allowsFiles) {
            if (allowsAnyType) setOf(ANY) else emptySet()
        } else {
            emptySet()
        }

        val category = when {
            allowsAnyType -> Category.FILES_AND_DOCUMENTS
            effectiveImageTypes.isNotEmpty() && effectiveVideoTypes.isEmpty() -> Category.IMAGES_ONLY
            effectiveVideoTypes.isNotEmpty() && effectiveImageTypes.isEmpty() && effectiveAudioTypes.isEmpty() && effectiveDocumentTypes.isEmpty() -> Category.VIDEOS_ONLY
            effectiveImageTypes.isNotEmpty() && effectiveVideoTypes.isNotEmpty() -> Category.IMAGES_AND_VIDEOS
            effectiveDocumentTypes.isNotEmpty() && effectiveDocumentTypes.all { it in DOCUMENT_TYPES } -> Category.DOCUMENTS_ONLY
            effectiveDocumentTypes.isNotEmpty() -> Category.FILES_AND_DOCUMENTS
            else -> Category.FILES_ONLY
        }

        return Analysis(
            effectiveImageTypes = effectiveImageTypes,
            effectiveVideoTypes = effectiveVideoTypes,
            effectiveDocumentTypes = effectiveDocumentTypes,
            effectiveAudioTypes = effectiveAudioTypes,
            allowsAnyType = allowsAnyType,
            category = category
        )
    }
}
