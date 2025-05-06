package io.github.inomnom.filepicker.api
import java.io.IOException
import android.content.Context // Keep if PickedFile.fromUri is here
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue // For Throwable

/**
 * Represents the outcome of a file picking operation.
 * This is a sealed interface, meaning all possible results are defined within this file.
 * It implements Parcelable to be easily passed between components (e.g., via Fragment results).
 */
sealed interface PickerResult : Parcelable {

    /**
     * Operation completed successfully, returning a list of selected files.
     *
     * @property files A list of [PickedFile] objects representing the user's selection.
     *                 The list might be empty if the user completed the selection process
     *                 without choosing any files (though typically [Cancelled] is used then).
     */
    @Parcelize
    data class Success(val files: List<PickedFile>) : PickerResult

    /**
     * Operation failed due to an error during the process.
     *
     * @property error The [Throwable] representing the cause of failure. Common types include:
     *                 - [SecurityException]: Necessary permissions were denied.
     *                 - [java.io.IOException]: An error occurred during file access or processing.
     *                 - [IllegalStateException]: An unexpected state occurred (e.g., context unavailable).
     *                 - [OutOfMemoryError]: Could not process large files (less common with URI handling).
     */
    @Parcelize
    data class Error(val error: @RawValue Throwable) : PickerResult // Use @RawValue for Throwable

    /**
     * Operation was cancelled by the user before completion (e.g., pressing back,
     * tapping outside the dialog, or pressing a cancel button).
     * This is represented as a data object, which is inherently singleton and Parcelable
     * when annotated with @Parcelize.
     */
    @Parcelize
    data object Cancelled : PickerResult
}