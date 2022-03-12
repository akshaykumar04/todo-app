package com.sstechcanada.todo.activities

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.sstechcanada.todo.BuildConfig
import com.sstechcanada.todo.R
import com.sstechcanada.todo.utils.PercentFormatter
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        textViewVersion.text = "Version: ${BuildConfig.VERSION_NAME}";

        initOnClicks()
        setupPieChart()
        loadPieChartData()
    }

    private fun initOnClicks() {
        fabBack.setOnClickListener { onBackPressed() }
    }

    private fun setupPieChart() {
        pieChart.isDrawHoleEnabled = true
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelTextSize(8F)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.centerText = getString(R.string.revenue_breakdown)
        pieChart.setCenterTextSize(12F)
        val l: Legend = pieChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.isEnabled = true
    }

    private fun loadPieChartData() {
        val entries: ArrayList<PieEntry> = ArrayList()
        entries.add(PieEntry(2.toFloat(), "Good Causes"))
        entries.add(PieEntry(2.toFloat(), "Advertising"))
        entries.add(PieEntry(3.toFloat(), "sstechcanada"))
        entries.add(PieEntry(3.toFloat(), "Jon (founder)"))
        val colors: ArrayList<Int> = ArrayList()
        for (color in ColorTemplate.MATERIAL_COLORS) {
            colors.add(color)
        }
        for (color in ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color)
        }
        val dataSet = PieDataSet(entries, "After taxes and transaction fees")
        dataSet.colors = colors
        val data = PieData(dataSet)
        data.setDrawValues(true)
        data.setValueFormatter(PercentFormatter(pieChart))
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.BLACK)
        ResourcesCompat.getFont(this, R.font.raleway_medium)?.let {
            pieChart.setCenterTextTypeface(it)
            pieChart.setCenterTextTypeface(it)
            data.setValueTypeface(it)
            pieChart.setNoDataTextTypeface(it)
            pieChart.legend.typeface = it
        }
        pieChart.data = data
        pieChart.invalidate()
        pieChart.animateY(1000, Easing.EaseInOutQuad)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        pieChart.description?.let {
            it.textSize = 15F
            it.isEnabled = true
            it.text = getString(R.string.pie_title)
            it.textColor = resources.getColor(R.color.textHeadings)
            it.textAlign = Paint.Align.CENTER
            it.setPosition((width/2).toFloat(), 32f)
            ResourcesCompat.getFont(this, R.font.raleway_semibold)?.let { tf ->
                it.typeface = tf
            }
        }

    }
}