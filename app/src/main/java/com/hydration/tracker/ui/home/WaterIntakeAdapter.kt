package com.hydration.tracker.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hydration.tracker.data.local.entities.WaterIntakeEntity
import com.hydration.tracker.databinding.ItemWaterIntakeBinding
import com.hydration.tracker.utils.formatAsWater
import com.hydration.tracker.utils.toTimeString

class WaterIntakeAdapter(
    private val onDeleteClick: (WaterIntakeEntity) -> Unit
) : ListAdapter<WaterIntakeEntity, WaterIntakeAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWaterIntakeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemWaterIntakeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(intake: WaterIntakeEntity) {
            binding.amountText.text = intake.amount.formatAsWater()
            binding.timeText.text = intake.timestamp.toTimeString()

            binding.deleteButton.setOnClickListener {
                onDeleteClick(intake)
            }

            // Reset any previous animations to prevent stuck items
            itemView.clearAnimation()
            itemView.alpha = 1f
            itemView.translationX = 0f
            itemView.translationY = 0f
            itemView.scaleX = 1f
            itemView.scaleY = 1f
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<WaterIntakeEntity>() {
        override fun areItemsTheSame(
            oldItem: WaterIntakeEntity,
            newItem: WaterIntakeEntity
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: WaterIntakeEntity,
            newItem: WaterIntakeEntity
        ) = oldItem == newItem
    }
}