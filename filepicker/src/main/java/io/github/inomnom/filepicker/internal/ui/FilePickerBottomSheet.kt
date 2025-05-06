package io.github.inomnom.filepicker.internal.ui

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.inomnom.filepicker.R
import io.github.inomnom.filepicker.api.PickerConfig
import io.github.inomnom.filepicker.core.MimeType
import io.github.inomnom.filepicker.databinding.FpLayoutPickerBottomSheetBinding
import io.github.inomnom.filepicker.internal.utils.parcelable

internal interface FilePickerListener {
    fun onCameraAction()
    fun onGalleryAction()
    fun onFilesAction()
    fun onPickerCancel()
}
internal class FilePickerBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FpLayoutPickerBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var config: PickerConfig
    private var listener: FilePickerListener? = null

    var actionTaken = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = arguments?.parcelable(ARG_CONFIG)
            ?: run {
                Log.e(TAG, "PickerConfig missing in arguments.")
                dismissAllowingStateLoss()
                PickerConfig()
            }

        listener = when {
            parentFragment is FilePickerListener -> parentFragment as FilePickerListener
            activity is FilePickerListener -> activity as FilePickerListener
            else -> {
                Log.e(TAG, "Host activity/fragment must implement FilePickerListener")
                null
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FpLayoutPickerBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.configureViews()
    }

    private fun FpLayoutPickerBottomSheetBinding.configureViews() {
        val analysis = MimeType.analyze(config)
        val canShowCamera = config.allowCamera && analysis.effectiveImageTypes.isNotEmpty()
        val canShowPhotos =
            config.allowGallery && (analysis.effectiveImageTypes.isNotEmpty() || analysis.effectiveVideoTypes.isNotEmpty())
        val canShowDocuments =
            config.allowsFiles && (analysis.effectiveDocumentTypes.isNotEmpty() || analysis.effectiveAudioTypes.isNotEmpty() || analysis.allowsAnyType)

        camera.isVisible = canShowCamera
        photos.isVisible = canShowPhotos
        documents.isVisible = canShowDocuments

        if (photos.isVisible) {
            photos.text = when (analysis.category) {
                MimeType.Category.IMAGES_ONLY -> getString(R.string.fp_photos_only)
                MimeType.Category.VIDEOS_ONLY -> getString(R.string.fp_videos)
                else -> getString(R.string.fp_photos_and_videos)
            }
        }

        if (documents.isVisible) {
            documents.text = when (analysis.category) {
                MimeType.Category.DOCUMENTS_ONLY -> getString(R.string.fp_documents_only)
                MimeType.Category.FILES_ONLY -> getString(R.string.fp_files_only)
                else -> getString(R.string.fp_files_and_documents)
            }
        }

        dividerCameraPhoto.isVisible = camera.isVisible && photos.isVisible
        dividerPhotoDocument.isVisible =
            (photos.isVisible && documents.isVisible) || (camera.isVisible && documents.isVisible && !photos.isVisible)

        camera.setOnClickListener {
            actionTaken = true
            listener?.onCameraAction()
        }
        photos.setOnClickListener {
            actionTaken = true
            listener?.onGalleryAction()
        }
        documents.setOnClickListener {
            actionTaken = true
            listener?.onFilesAction()
        }
        btnCancel.setOnClickListener {
            actionTaken = true
            listener?.onPickerCancel()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!actionTaken) {
            listener?.onPickerCancel()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        listener = null
    }

    companion object {
        private val TAG = FilePickerBottomSheet::class.java.simpleName
        private const val ARG_CONFIG = "picker_config"

        @JvmStatic
        internal fun newInstance(config: PickerConfig): FilePickerBottomSheet {
            return FilePickerBottomSheet().apply {
                arguments = bundleOf(ARG_CONFIG to config)
            }
        }
    }
}