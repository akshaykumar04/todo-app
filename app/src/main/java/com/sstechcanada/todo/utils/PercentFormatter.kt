package com.sstechcanada.todo.utils

import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat

class PercentFormatter() : ValueFormatter() {
    var mFormat: DecimalFormat = DecimalFormat("###,###,##")
    private var pieChart: PieChart? = null

    constructor(pieChart: PieChart?) : this() {
        this.pieChart = pieChart
    }

    override fun getFormattedValue(value: Float): String {
        return mFormat.format(value.toDouble()) + " %"
    }

    override fun getPieLabel(value: Float, pieEntry: PieEntry): String {
        return if (pieChart != null && pieChart!!.isUsePercentValuesEnabled) {
            // Converted to percent
            getFormattedValue(value)
        } else {
            // raw value, skip percent sign
            mFormat.format(value.toDouble())
        }
    }

}