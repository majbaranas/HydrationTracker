package com.hydration.tracker.ui.tips

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hydration.tracker.databinding.ItemTipBinding
import com.hydration.tracker.utils.bounce

class TipsAdapter(
    private val tips: List<HydrationTip>,
    private val onShareClick: (HydrationTip) -> Unit
) : RecyclerView.Adapter<TipsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTipBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tips[position], position)
    }

    override fun getItemCount() = tips.size

    inner class ViewHolder(
        private val binding: ItemTipBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tip: HydrationTip, position: Int) {
            binding.tipIcon.setImageResource(tip.icon)
            binding.tipTitle.text = tip.title
            binding.tipDescription.text = tip.description

            binding.shareButton.setOnClickListener {
                it.bounce()
                onShareClick(tip)
            }

            // Card click animation
            binding.tipCard.setOnClickListener {
                it.bounce()
            }

            // Entrance animation
            itemView.alpha = 0f
            itemView.translationY = 100f
            itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay((position * 80L).coerceAtMost(500))
                .start()
        }
    }
}