package io.github.inomnom.filepicker.api

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

@Parcelize
data class PickedFile(
    val uri: Uri,
    val mimeType: String?,
    val name: String?,
    val size: Long?,
    val fileType: String?
) : Parcelable {

    companion object {
        private val TAG = PickedFile::class.java.simpleName

        /**
         * Extracts the uppercase file extension from a name, if present.
         */
        private fun getFileTypeFromName(fileName: String?): String? =
            fileName
                ?.substringAfterLast('.', "")
                ?.takeIf { it.isNotEmpty() }
                ?.uppercase()

        /**
         * Resolves metadata for the given URI and returns a [PickedFile] instance.
         *
         * This function accesses the content resolver on an IO dispatcher and
         * handles failures gracefully. Metadata fields may be null if resolution fails.
         *
         * @param context Application or activity context.
         * @param uri The URI to resolve.
         * @return A [PickedFile] with available metadata.
         */
        suspend fun fromUri(context: Context, uri: Uri): PickedFile = withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver
            var name: String? = null
            var size: Long? = null

            val mimeType = runCatching {
                contentResolver.getType(uri)
            }.onFailure {
                Log.w(TAG, "Failed to resolve MIME type for URI: $uri", it)
            }.getOrNull()

            runCatching {
                contentResolver.query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                        if (nameIndex != -1) {
                            name = cursor.getString(nameIndex)
                        }
                        if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                            size = cursor.getLong(sizeIndex)
                        }
                    }
                }
            }.onFailure {
                Log.e(TAG, "Failed to query OpenableColumns for URI: $uri", it)
            }

            if (name == null && uri.scheme == "file") {
                name = runCatching { uri.path?.let { File(it).name } }.getOrNull()
            }

            val fileType = getFileTypeFromName(name)
            PickedFile(uri, mimeType, name, size, fileType)
        }
    }

    /**
     * Opens an InputStream for the file's content URI using ContentResolver.
     * Remember to close the stream when finished, preferably using `use`.
     *
     * @param context Application or activity context.
     * @return InputStream? An InputStream for the URI, or null if opening failed.
     */
    fun openInputStream(context: Context): InputStream? = runCatching {
        context.contentResolver.openInputStream(uri)
    }.onFailure {
        Log.e(TAG, "Failed to open InputStream for URI: $uri", it)
    }.getOrNull()

    /**
     * Reads the entire content of the file into a ByteArray.
     * **Warning:** Avoid using this for large files (e.g., > 10-20MB) as it loads the entire
     * content into memory, potentially causing [OutOfMemoryError]. Consider using [openInputStream]
     * and processing the stream for large files.
     *
     * @param context Application or activity context.
     * @return ByteArray? The file content, or null if reading failed.
     */
    suspend fun readBytes(context: Context): ByteArray? = withContext(Dispatchers.IO) {
        runCatching {
            openInputStream(context)?.use { it.readBytes() }
                ?: throw IOException("Could not open input stream for URI: $uri")
        }.onFailure {
            Log.e(TAG, "Failed to read bytes from URI: $uri", it)
        }.getOrNull()
    }

    /**
     * Copies the content of the file URI to a temporary File in the app's cache directory.
     * Useful when an API requires a `java.io.File` object.
     * **Note:** The caller is responsible for deleting the created temporary file when no longer needed
     * to avoid filling up cache storage.
     *
     * @param context Application or activity context.
     * @return File? The temporary File object created in the cache directory, or null if the copy failed.
     */

    fun toCacheFile(context: Context): File? = runCatching {
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            ?: name?.substringAfterLast('.', "")
                .orEmpty()
                .takeIf { it.isNotEmpty() } ?: fileType?.lowercase() ?: ""

        val prefix =
            name?.substringBeforeLast(".")?.replace("[^a-zA-Z0-9.-]".toRegex(), "_")?.take(50)
                ?: "file_${System.currentTimeMillis()}"

        val suffix = if (extension.isNotEmpty()) ".$extension" else ""

        val tempFile = File.createTempFile("temp_", suffix, context.cacheDir)
        val renamedFile = File(tempFile.parent, "$prefix$suffix")
        val success = tempFile.renameTo(renamedFile)

        val outputFile = if (success) renamedFile else tempFile

        openInputStream(context)?.use { inputStream ->
            FileOutputStream(outputFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IOException("Could not open input stream for URI: $uri")

        outputFile
    }.onFailure {
        Log.e(TAG, "Failed to copy URI content to cache file: $uri", it)
    }.getOrNull()
}