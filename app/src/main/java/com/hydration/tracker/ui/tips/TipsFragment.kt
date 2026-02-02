package com.hydration.tracker.ui.tips

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hydration.tracker.R
import com.hydration.tracker.databinding.FragmentTipsBinding
import com.hydration.tracker.utils.slideInFromBottom
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TipsFragment : Fragment() {

    private var _binding: FragmentTipsBinding? = null
    private val binding get() = _binding!!

    private lateinit var tipsAdapter: TipsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTipsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        animateEntrance()
    }

    private fun setupUI() {
        tipsAdapter = TipsAdapter(
            tips = getHydrationTips(),
            onShareClick = { tip ->
                shareTip(tip)
            }
        )

        binding.tipsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tipsAdapter
        }
    }

    private fun getHydrationTips(): List<HydrationTip> {
        return listOf(
            HydrationTip(
                id = 1,
                title = getString(R.string.tip_1_title),
                description = getString(R.string.tip_1_description),
                icon = R.drawable.ic_morning
            ),
            HydrationTip(
                id = 2,
                title = getString(R.string.tip_2_title),
                description = getString(R.string.tip_2_description),
                icon = R.drawable.ic_meal
            ),
            HydrationTip(
                id = 3,
                title = getString(R.string.tip_3_title),
                description = getString(R.string.tip_3_description),
                icon = R.drawable.ic_exercise
            ),
            HydrationTip(
                id = 4,
                title = getString(R.string.tip_4_title),
                description = getString(R.string.tip_4_description),
                icon = R.drawable.ic_bottle
            ),
            HydrationTip(
                id = 5,
                title = getString(R.string.tip_5_title),
                description = getString(R.string.tip_5_description),
                icon = R.drawable.ic_reminder
            ),
            HydrationTip(
                id = 6,
                title = getString(R.string.tip_6_title),
                description = getString(R.string.tip_6_description),
                icon = R.drawable.ic_color
            )
        )
    }

    private fun shareTip(tip: HydrationTip) {
        val shareText = "${tip.title}\n\n${tip.description}\n\n${getString(R.string.share_suffix)}"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        startActivity(Intent.createChooser(intent, getString(R.string.share_tip)))
    }

    private fun animateEntrance() {
        binding.tipsRecyclerView.slideInFromBottom(300)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}