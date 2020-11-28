package com.chatterplot

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.google.android.material.bottomappbar.BottomAppBar
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class GraphActivity : AppCompatActivity() {
    private lateinit var tableName:String
    private lateinit var chartView: AAChartView
    private lateinit var preview: AAChartView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)
        tableName = intent.getStringExtra("DATASETNAME")
        chartView = findViewById<AAChartView>(R.id.aa_chart_view)
        preview = AAChartView(this)
        preview.layout(0, 0, 1000, 400)

        graphDataset()

        Handler().postDelayed({
            val bitmap = convertViewToBitmap(preview as View)
            saveBitmap(this, bitmap, "$tableName.png")
        }, 5000)

        val bottomAppBar = findViewById<BottomAppBar>(R.id.bottom_app_bar)
        bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.share_dataset -> {
                    val bitmap = convertViewToBitmap(chartView)
                    val bytestream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytestream)
                    val f = File(this.getExternalFilesDir(null), "temp.jpeg")
                    try {
                        f.createNewFile()
                        val fo = FileOutputStream(f)
                        fo.write(bytestream.toByteArray())
                    } catch (e: Exception) {
                        Log.e("Error", e!!.localizedMessage)
                    }
//                    saveBitmap(this, bitmap, "share.png")
//                    val f = File(this.filesDir, "share.png")
                    val uri = FileProvider.getUriForFile(this, this.applicationContext.packageName + ".provider", f)
//                    val uri = Uri.parse(f.toUri().toString())
                    Log.e("File uri", uri.toString())
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    startActivity(Intent.createChooser(intent, null))
                    true
                }
                else -> {
                    true
                }
            }

        }
    }
    private fun graphDataset() {
        val data = DatabaseHelper(this).getTable(tableName)
        val graphData = ArrayList<AASeriesElement>()
        val xAxisColumnName = DatabaseHelper(this).getXAxisColumn(tableName)
        val xValArray = data[xAxisColumnName] ?: ArrayList()

        for((key,value) in data) {

            if(key != "ID" && key != "Timestamp" && key != xAxisColumnName) {
                val current = AASeriesElement().name(key).data(Array<Any>(value.size){it->
                    arrayOf(xValArray[it],(value[it] as String).toInt())
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
            .xAxisLabelsEnabled(false)
//            .xAxisLabelsEnabled(true)
        chartView.aa_drawChartWithChartModel(chartModel)

        preview.aa_drawChartWithChartModel(chartModel
            .legendEnabled(false)
            .yAxisLabelsEnabled(false)
            .title("")
            .yAxisTitle("")
            .dataLabelsEnabled(false)
            .xAxisTickInterval(0)
            .yAxisGridLineWidth(0F)
            .xAxisVisible(false))
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

//class BarChartAxisFormatter(private val axisLabel : Array<String>) : ValueFormatter() {
//    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
//        return axisLabel[value.toInt()]
//    }
//}

//class BarChartSelectedListener : OnChartValueSelectedListener {
//    override fun onValueSelected(e: Entry?, h: Highlight?) {
//        e.
//    }
//}