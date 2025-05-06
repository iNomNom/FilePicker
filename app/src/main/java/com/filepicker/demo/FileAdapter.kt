package com.filepicker.demo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.filepicker.demo.databinding.ItemSelectedFileBinding
import io.github.inomnom.filepicker.api.PickedFile
import java.util.Locale
import kotlin.math.pow

class FileAdapter : ListAdapter<PickedFile, FileAdapter.FileViewHolder>(FileDiffCallback()) {

    inner class FileViewHolder(private val binding: ItemSelectedFileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(file: PickedFile, position: Int) {
            binding.tvFileNumber.text =
                itemView.context.getString(R.string.file_number_format, position + 1)
            binding.tvFileName.text =
                itemView.context.getString(R.string.file_name_format, file.name ?: "N/A")

            val sizeStr = file.size?.let { formatFileSize(it) } ?: "N/A"
            val typeStr = file.mimeType ?: "N/A"
            binding.tvFileDetails.text =
                itemView.context.getString(R.string.file_details_format, typeStr, sizeStr)
            binding.tvFileDetails.isVisible = true
        }

        private fun formatFileSize(sizeBytes: Long): String {
            if (sizeBytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups =
                (kotlin.math.log10(sizeBytes.toDouble()) / kotlin.math.log10(1024.0)).toInt()
            val safeDigitGroup = digitGroups.coerceIn(0, units.size - 1)
            return String.format(
                Locale.US, "%.1f %s",
                sizeBytes / 1024.0.pow(safeDigitGroup.toDouble()), units[safeDigitGroup]
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemSelectedFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = getItem(position)
        holder.bind(file, position)
    }

    private class FileDiffCallback : DiffUtil.ItemCallback<PickedFile>() {
        override fun areItemsTheSame(oldItem: PickedFile, newItem: PickedFile): Boolean {
            return oldItem.uri == newItem.uri
        }

        override fun areContentsTheSame(oldItem: PickedFile, newItem: PickedFile): Boolean {
            return oldItem == newItem
        }
    }
}