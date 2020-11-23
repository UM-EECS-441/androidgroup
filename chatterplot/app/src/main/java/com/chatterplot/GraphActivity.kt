package com.chatterplot

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

class GraphActivity : AppCompatActivity() {
    private lateinit var tableName:String
    private lateinit var chartView: AAChartView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)
        tableName = intent.getStringExtra("DATASETNAME")
        chartView = findViewById<AAChartView>(R.id.aa_chart_view)
        graphDataset("line")
        val my_button = findViewById<Button>(R.id.chart_menu_button)
        my_button.setOnClickListener {
            showPopup(my_button)
        }
    }
    fun graphDataset(chartType: String) {

        val data = DatabaseHelper(this).getTable(tableName)
        val graphData = ArrayList<AASeriesElement>()
        val timestamp = data["Timestamp"] ?: ArrayList<Any>()
        var chartToDisp = AAChartType.Line
        when (chartType) {
            "line" -> chartToDisp = AAChartType.Line
            "pie" -> chartToDisp = AAChartType.Pie
            "bar" -> chartToDisp = AAChartType.Bar
            "column" -> chartToDisp = AAChartType.Column
        }
        val my_button = findViewById<Button>(R.id.chart_menu_button)
        my_button.text = chartType

        for((key,value) in data) {
            if(key != "ID" && key != "Timestamp") {
                val current = AASeriesElement().name(key).data(Array<Any>(value.size){it->
                    arrayOf(timestamp[it],(value[it] as String).toInt())
                })
                graphData.add(current)
            }
        }
        val chartModel = AAChartModel()
            .chartType(chartToDisp)
            .title(tableName)
            .subtitle("")
            .backgroundColor("#FFFFFF")
            .dataLabelsEnabled(true)
            .series(graphData.toTypedArray())
            .xAxisLabelsEnabled(false)
//            .xAxisLabelsEnabled(true)
        chartView.aa_drawChartWithChartModel(chartModel)
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.chart_type_menu)

        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.header1 -> {
                    graphDataset("bar")
                }
                R.id.header2 -> {
                    graphDataset("column")
                }
                R.id.header3 -> {
                    graphDataset("line")
                }
                R.id.header4 -> {
                    graphDataset("pie")
                }
            }

            true
        })

        popup.show()
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