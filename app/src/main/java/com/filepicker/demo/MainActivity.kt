package com.filepicker.demo

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.filepicker.demo.databinding.ActivityMainBinding
import io.github.inomnom.filepicker.api.FilePicker
import io.github.inomnom.filepicker.api.PickedFile
import io.github.inomnom.filepicker.api.PickerConfig
import io.github.inomnom.filepicker.api.PickerResult
import io.github.inomnom.filepicker.core.MimeType
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.pow

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val fileAdapter by lazy { FileAdapter() }
    private val TAG = "FilePickerDemo"

    private var shouldTestExtensions = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupRecyclerView()
        setupInsets()
        setupButtonClickListeners()
    }

    private fun setupRecyclerView() {
        binding.rvSelectedFiles.adapter = fileAdapter
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = insets.left,
                top = insets.top,
                right = insets.right,
                bottom = insets.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setupButtonClickListeners() {
        binding.btnPickSingleImage.setOnClickListener {
            shouldTestExtensions = false
            FilePicker.showSheet(
                context = this,
                config = PickerConfig.singleImage(),
                onResult = this::handlePickerResult
            )
        }
        binding.btnPickSingleImageCompressed.setOnClickListener {
            shouldTestExtensions = false
            FilePicker.showSheet(
                context = this,
                config = PickerConfig.singleImage(compress = true),
                onResult = this::handlePickerResult
            )
        }
        binding.btnPickMultipleImages.setOnClickListener {
            shouldTestExtensions = false
            FilePicker.showSheet(
                context = this,
                config = PickerConfig.multipleImages(maxCount = 3),
                onResult = this::handlePickerResult
            )
        }
        binding.btnPickSingleVideo.setOnClickListener {
            shouldTestExtensions = false
            FilePicker.showSheet(
                context = this,
                config = PickerConfig.singleVideo(),
                onResult = this::handlePickerResult
            )
        }
        binding.btnPickMultipleMedia.setOnClickListener {
            shouldTestExtensions = false
            FilePicker.showSheet(
                context = this,
                config = PickerConfig.multipleMedia(maxCount = 5, compress = true),
                onResult = this::handlePickerResult
            )
        }
        binding.btnPickSinglePdf.setOnClickListener {
            shouldTestExtensions = false
            FilePicker.showSheet(
                context = this,
                config = PickerConfig.singlePdf(),
                onResult = this::handlePickerResult
            )
        }
        binding.btnPickMultipleDocs.setOnClickListener {
            shouldTestExtensions = false
            FilePicker.showSheet(
                context = this,
                config = PickerConfig.multipleDocuments(
                    maxCount = 2,
                    specificTypes = setOf(
                        MimeType.APPLICATION_PDF,
                        MimeType.APPLICATION_MSWORD_DOCX
                    )
                ),
                onResult = this::handlePickerResult
            )
        }
        binding.btnPickAnySingleFile.setOnClickListener {
            shouldTestExtensions = false
            FilePicker.showSheet(
                context = this,
                config = PickerConfig.singleFile(),
                onResult = this::handlePickerResult
            )
        }
        binding.btnPickCustom.setOnClickListener {
            shouldTestExtensions = false
            val customConfig = PickerConfig.custom(
                allowedMimeTypes = setOf(MimeType.IMAGE_PNG, MimeType.APPLICATION_ZIP),
                allowMultipleSelection = true,
                maxSelectionCount = 4,
                allowCamera = true,
                compressCameraImage = true
            )
            FilePicker.showSheet(
                context = this,
                config = customConfig,
                onResult = this::handlePickerResult
            )
        }

        binding.btnPickAndTestHelpers.setOnClickListener {
            shouldTestExtensions = true
            FilePicker.showSheet(
                context = this,
                config = PickerConfig.singleFile(),
                onResult = this::handlePickerResult
            )
        }

        binding.btnPickFromCamera.setOnClickListener {
            shouldTestExtensions = false
            FilePicker.launchCamera(
                context = this,
                config = PickerConfig.singleImage(compress = true),
                onResult = this::handlePickerResult
            )
        }
        binding.btnPickFromGallery.setOnClickListener {
            shouldTestExtensions = false
            FilePicker.launchGallery(
                context = this,
                config = PickerConfig.multipleImages(maxCount = 5),
                onResult = this::handlePickerResult
            )
        }
        binding.btnPickFromFiles.setOnClickListener {
            shouldTestExtensions = false
            FilePicker.launchFiles(
                context = this,
                config = PickerConfig.multipleDocuments(maxCount = 2),
                onResult = this::handlePickerResult
            )
        }
    }


    private fun handlePickerResult(result: PickerResult) {
        Log.d(TAG, "Picker Result Received: $result")
        binding.tvExtensionResult.isGone = true
        when (result) {
            is PickerResult.Success -> handleSuccess(result.files)
            is PickerResult.Error -> handleError(result.error)
            is PickerResult.Cancelled -> handleCancellation()
        }
    }

    private fun handleSuccess(files: List<PickedFile>) {
        if (files.isEmpty()) {
            binding.tvResultHeader.text = getString(R.string.success_no_files_selected)
        } else {
            binding.tvResultHeader.text = resources.getQuantityString(
                R.plurals.success_files_picked,
                files.size,
                files.size
            )
        }
        binding.tvResultHeader.isVisible = true
        binding.rvSelectedFiles.isVisible = files.isNotEmpty()
        fileAdapter.submitList(files)
        if (files.isNotEmpty()) {
            binding.rvSelectedFiles.scrollToPosition(0)
        }

        if (shouldTestExtensions && files.isNotEmpty()) {
            testPickedFileHelpers(files.first())
        }
        shouldTestExtensions = false
    }

    private fun handleError(error: Throwable) {
        Log.e(TAG, "Picker error", error)
        binding.tvResultHeader.isVisible = true
        binding.rvSelectedFiles.isGone = true
        binding.tvExtensionResult.isGone = true
        val errorMessage = error.localizedMessage ?: error.javaClass.simpleName
        binding.tvResultHeader.text = getString(R.string.error, errorMessage)
        fileAdapter.submitList(emptyList())
    }

    private fun handleCancellation() {
        Log.i(TAG, "Picker operation cancelled.")
        binding.tvResultHeader.isVisible = true
        binding.rvSelectedFiles.isGone = true
        binding.tvExtensionResult.isGone = true
        binding.tvResultHeader.text = getString(R.string.picker_cancelled_by_user)
        fileAdapter.submitList(emptyList())
    }

    private fun testPickedFileHelpers(pickedFile: PickedFile) {
        binding.tvExtensionResult.text = "Testing helper functions for: ${pickedFile.name ?: "Unknown"}\n"
        binding.tvExtensionResult.isVisible = true

        lifecycleScope.launch {
            binding.tvExtensionResult.append("Testing openInputStream... ")
            val inputStream = pickedFile.openInputStream(this@MainActivity)
            if (inputStream != null) {
                binding.tvExtensionResult.append("SUCCESS\n")
                runCatching { inputStream.close() }.onFailure {
                    Log.w(
                        TAG,
                        "Error closing test stream",
                        it
                    )
                }
            } else {
                binding.tvExtensionResult.append("FAILED\n")
            }

            binding.tvExtensionResult.append("Testing readBytes... ")
            if ((pickedFile.size ?: Long.MAX_VALUE) < 20 * 1024 * 1024) { // Only test on reasonably small files (<20MB)
                val bytes = pickedFile.readBytes(this@MainActivity)
                if (bytes != null) {
                    binding.tvExtensionResult.append("SUCCESS (${bytes.size} bytes read)")
                } else {
                    binding.tvExtensionResult.append("FAILED")
                }
            } else {
                binding.tvExtensionResult.append(
                    "SKIPPED (File too large: ${
                        formatFileSize(
                            pickedFile.size ?: 0
                        )
                    })"
                )
            }

            binding.tvExtensionResult.append("\nTesting copyToCacheFile... ")
            val tempFile = pickedFile.toCacheFile(this@MainActivity)
            if (tempFile != null && tempFile.exists()) {
                binding.tvExtensionResult.append(
                    "SUCCESS (Created: ${tempFile.name}, Size: ${formatFileSize(tempFile.length())})"
                )
                // IMPORTANT: Delete the temp file after testing if you don't need it
                if (tempFile.delete()) {
                    Log.d(TAG, "Deleted temp cache file: ${tempFile.name}")
                } else {
                    Log.w(TAG, "Failed to delete temp cache file: ${tempFile.name}")
                }
            } else {
                binding.tvExtensionResult.append("FAILED")
            }
        }
    }

    private fun formatFileSize(sizeBytes: Long): String {
        if (sizeBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups =
            (kotlin.math.log10(sizeBytes.toDouble()) / kotlin.math.log10(1024.0)).toInt()
        val safeDigitGroup = digitGroups.coerceIn(0, units.size - 1)
        return String.format(
            Locale.US,
            "%.1f %s",
            sizeBytes / 1024.0.pow(safeDigitGroup.toDouble()),
            units[safeDigitGroup]
        )
    }
}