package com.chatterplot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AADataLabels
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AASeries
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAXAxis
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
    private lateinit var tableName:String
    private lateinit var chartView: AAChartView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)
        tableName = intent.getStringExtra("DATASETNAME")
        chartView = findViewById<AAChartView>(R.id.aa_chart_view)
        graphDataset()
//        val data = arrayOf(arrayOf(1.0,4.0), arrayOf(4.0,2.1), arrayOf(6.4,32.1), arrayOf(3.1, 5.3))

//        createLineChart()
//        createBarChart()
    }
    fun graphDataset() {
        val data = DatabaseHelper(this).getTable(tableName)
        val graphData = ArrayList<AASeriesElement>()
        val timestamp = data["Timestamp"] ?: ArrayList<Any>()

        for((key,value) in data) {
            if(key != "ID" && key != "Timestamp") {
//                value.zip(timestamp!!)
//                val combined = (0 until value.size).map{arrayOf((value[it] as String).toInt(), timestamp[it])}
//                val combined = timestamp?.zip(value) {a, b->arrayOf((b as String).toInt(), a)}
                val current = AASeriesElement().name(key).data(Array<Any>(value.size){it->
                    arrayOf(timestamp[it],(value[it] as String).toInt())
                })
                graphData.add(current)
            }
        }
        val chartModel = AAChartModel()
            .chartType(AAChartType.Line)
            .title(tableName)
            .subtitle("")
            .backgroundColor("#FFFFFF")
            .dataLabelsEnabled(true)
            .series(graphData.toTypedArray())
//            .xAxisLabelsEnabled(true)
        chartView.aa_drawChartWithChartModel(chartModel)
    }

//    fun createLineChart() {
//        val chart = LineChart(this)
//
//        val frame = findViewById<RelativeLayout>(R.id.graph_frame)
//        val params = frame.layoutParams
//        params.width = RelativeLayout.LayoutParams.MATCH_PARENT
//        params.height = RelativeLayout.LayoutParams.MATCH_PARENT
//        chart.layoutParams = params
//        frame.addView(chart)
//
//        val mockData = listOf<Entry>(Entry(1F, 4F), Entry(2F, 4F), Entry(3F, 4F), Entry(4F, 5F))
//        val dataSet = LineDataSet(mockData, "Label")
//        val lineData = LineData(dataSet)
//        chart.data = lineData
//        chart.invalidate()
//    }

//    fun createBarChart() {
//        val chart = BarChart(this)
//        val frame = findViewById<RelativeLayout>(R.id.graph_frame)
//        val params = frame.layoutParams
//        params.width = RelativeLayout.LayoutParams.MATCH_PARENT
//        params.height = RelativeLayout.LayoutParams.MATCH_PARENT
//        chart.layoutParams = params
//        frame.addView(chart)
//
//        val mockData = listOf(BarEntry(0F, 10F), BarEntry(1F, 12F), BarEntry(2F, 30F), BarEntry(3F, 23F))
//        val mockLabel = arrayOf("First", "Second", "Third", "Fifth")
//        val dataSet = BarDataSet(mockData, "BarDataSet")
//        val barData = BarData(dataSet)
//        barData.setValueTextSize(16F)
//        chart.data = barData
//        chart.description.isEnabled = false
//        chart.xAxis.isEnabled = true
//        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
//        chart.xAxis.setDrawGridLines(false)
//        chart.animateXY(500, 1000)
//        chart.axisLeft.axisMinimum = 0F
//        chart.xAxis.valueFormatter = BarChartAxisFormatter(mockLabel)
//        chart.xAxis.granularity = 1F
//        chart.xAxis.textSize = 18F
//        chart.axisLeft.textSize = 16F
//        chart.axisRight.isEnabled = false
//        chart.isScaleYEnabled = false
//        chart.isHighlightPerDragEnabled = false
//        chart.invalidate()
//    }
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