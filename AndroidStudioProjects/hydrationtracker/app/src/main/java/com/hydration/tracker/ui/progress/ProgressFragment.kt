package com.hydration.tracker.ui.progress

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.hydration.tracker.R
import com.hydration.tracker.databinding.FragmentProgressBinding
import com.hydration.tracker.utils.slideInFromBottom
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProgressViewModel by viewModels()

    private val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChart()
        setupObservers()
        animateEntrance()
    }

    private fun setupChart() {
        binding.weeklyChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setMaxVisibleValueCount(7)
            setPinchZoom(false)
            setScaleEnabled(false)

            // X Axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = Color.WHITE
                textSize = 12f
            }

            // Left Axis
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#22FFFFFF")
                textColor = Color.WHITE
                axisMinimum = 0f
            }

            // Right Axis
            axisRight.isEnabled = false

            // Legend
            legend.isEnabled = false

            // Animation
            animateY(1000, Easing.EaseInOutQuart)
        }
    }

    private fun setupObservers() {
        viewModel.weeklyData.observe(viewLifecycleOwner) { data ->
            updateChart(data)
            updateStatistics(data)
        }
    }

    private fun updateChart(weeklyData: Map<String, Int>) {
        val calendar = Calendar.getInstance()
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        // Generate last 7 days
        for (i in 6 downTo 0) {
            calendar.add(Calendar.DAY_OF_YEAR, if (i == 6) -6 else 1)
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(calendar.time)
            val dayLabel = dateFormat.format(calendar.time)

            val amount = weeklyData[dateStr] ?: 0
            entries.add(BarEntry((6 - i).toFloat(), amount.toFloat()))
            labels.add(dayLabel)
        }

        val dataSet = BarDataSet(entries, "Water Intake").apply {
            // Gradient colors
            colors = listOf(
                Color.parseColor("#A4D7E1"),
                Color.parseColor("#A496E2")
            )
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            setDrawValues(true)
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.7f
        }

        binding.weeklyChart.apply {
            this.data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            invalidate()

            // Animate bars growing
            animateY(800, Easing.EaseOutBounce)
        }
    }

    private fun updateStatistics(weeklyData: Map<String, Int>) {
        val values = weeklyData.values

        if (values.isEmpty()) {
            binding.averageText.text = "0ml"
            binding.totalText.text = "0ml"
            binding.bestDayText.text = "-"
            return
        }

        val total = values.sum()
        val average = total / 7
        val best = values.maxOrNull() ?: 0

        // Animate statistics
        animateStatistic(binding.averageText, average)
        animateStatistic(binding.totalText, total)
        binding.bestDayText.text = "${best}ml"

        // Update streak
        val streak = calculateStreak(weeklyData)
        binding.streakText.text = getString(R.string.day_streak, streak)
    }

    private fun animateStatistic(textView: android.widget.TextView, value: Int) {
        val animator = android.animation.ValueAnimator.ofInt(0, value)
        animator.duration = 800
        animator.addUpdateListener {
            textView.text = "${it.animatedValue}ml"
        }
        animator.start()
    }

    private fun calculateStreak(weeklyData: Map<String, Int>): Int {
        var streak = 0
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (i in 0 until 7) {
            val dateStr = dateFormat.format(calendar.time)
            val amount = weeklyData[dateStr] ?: 0

            if (amount >= 2000) { // Assuming 2000ml goal
                streak++
            } else {
                break
            }

            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return streak
    }

    private fun animateEntrance() {
        binding.chartCard.slideInFromBottom(300)

        binding.statisticsCard.postDelayed({
            binding.statisticsCard.slideInFromBottom(350)
        }, 100)

        binding.streakCard.postDelayed({
            binding.streakCard.slideInFromBottom(400)
        }, 200)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}