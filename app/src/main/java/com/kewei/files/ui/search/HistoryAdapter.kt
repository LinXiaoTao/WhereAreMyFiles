package com.kewei.files.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.kewei.files.databinding.ItemHistoryBinding
import com.kewei.files.util.DateTimeUtil.toSimpleDateFormat
import com.kewei.files.util.DisplayUtil.dpToPx

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<HistoryModel>() {
        override fun areItemsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean {
            return oldItem.contentUri == newItem.contentUri
        }

        override fun areContentsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean {
            return oldItem == newItem
        }
    }
    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(data: List<HistoryModel>) {
        differ.submitList(data)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder =
        HistoryViewHolder(
            ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bindTo(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindTo(model: HistoryModel) {
            binding.imgCover.load(model.contentUri) {
                transformations(
                    RoundedCornersTransformation(
                        radius = 6.dpToPx()
                    )
                )
            }
            binding.textName.text = model.displayName
            binding.textMineType.text = model.mineType
            binding.textDateModified.text = model.dateModified.times(1000).toSimpleDateFormat()
        }
    }

}