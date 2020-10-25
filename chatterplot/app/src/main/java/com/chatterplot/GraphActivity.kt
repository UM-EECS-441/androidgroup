package com.chatterplot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

class GraphActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)
//        createLineChart()
        createBarChart()
    }

    fun createLineChart() {
        val chart = LineChart(this)

        val frame = findViewById<RelativeLayout>(R.id.graph_frame)
        val params = frame.layoutParams
        params.width = RelativeLayout.LayoutParams.MATCH_PARENT
        params.height = RelativeLayout.LayoutParams.MATCH_PARENT
        chart.layoutParams = params
        frame.addView(chart)

        val mockData = listOf<Entry>(Entry(1F, 4F), Entry(2F, 4F), Entry(3F, 4F), Entry(4F, 5F))
        val dataSet = LineDataSet(mockData, "Label")
        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.invalidate()
    }

    fun createBarChart() {
        val chart = BarChart(this)
        val frame = findViewById<RelativeLayout>(R.id.graph_frame)
        val params = frame.layoutParams
        params.width = RelativeLayout.LayoutParams.MATCH_PARENT
        params.height = RelativeLayout.LayoutParams.MATCH_PARENT
        chart.layoutParams = params
        frame.addView(chart)

        val mockData = listOf(BarEntry(0F, 10F), BarEntry(1F, 12F), BarEntry(2F, 30F), BarEntry(3F, 23F))
        val mockLabel = arrayOf("First", "Second", "Third", "Fifth")
        val dataSet = BarDataSet(mockData, "BarDataSet")
        val barData = BarData(dataSet)
        barData.setValueTextSize(16F)
        chart.data = barData
        chart.description.isEnabled = false
        chart.xAxis.isEnabled = true
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.animateXY(500, 1000)
        chart.axisLeft.axisMinimum = 0F
        chart.xAxis.valueFormatter = BarChartAxisFormatter(mockLabel)
        chart.xAxis.granularity = 1F
        chart.xAxis.textSize = 18F
        chart.axisLeft.textSize = 16F
        chart.axisRight.isEnabled = false
        chart.isScaleYEnabled = false
        chart.isHighlightPerDragEnabled = false
        chart.invalidate()
    }
}

class BarChartAxisFormatter(private val axisLabel : Array<String>) : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return axisLabel[value.toInt()]
    }
}

//class BarChartSelectedListener : OnChartValueSelectedListener {
//    override fun onValueSelected(e: Entry?, h: Highlight?) {
//        e.
//    }
//}