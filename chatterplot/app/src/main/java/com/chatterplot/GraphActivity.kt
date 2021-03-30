package com.chatterplot

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AADataLabels
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AATooltip
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
        supportActionBar?.title = tableName
        var chartType = "line"
        if (DatabaseHelper(this).isCategorical(tableName)) chartType = "column"
        graphDataset(chartType)
        val my_button = findViewById<Button>(R.id.chart_menu_button)
        my_button.setOnClickListener {
            showPopup(my_button)
        }

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
                R.id.table_dataset -> {
                    val intent = Intent(this, DisplayDataActivity::class.java)
                    intent.putExtra("DATASETNAME", tableName)
                    startActivity(intent)
                    true
                }
                else -> {
                    true
                }
            }

        }
    }

    private fun graphDataset(chartType: String) {
        val data = DatabaseHelper(this).isolateDataset(tableName)
        val columns = DatabaseHelper(this).getColumnNames(tableName)
        val isCategorical = DatabaseHelper(this).isCategorical(tableName)
        val graphData = ArrayList<AASeriesElement>()
        var chartToDisp = AAChartType.Line
        when (chartType) {
            "line" -> chartToDisp = AAChartType.Line
            "pie" -> chartToDisp = AAChartType.Pie
            "bar" -> chartToDisp = AAChartType.Bar
            "column" -> chartToDisp = AAChartType.Column
        }
        val my_button = findViewById<Button>(R.id.chart_menu_button)
        my_button.text = chartType

        if (!isCategorical) {
            for (c in 1 until columns.size + 1) {
                Log.d("Column Loop", "Looping to Column " + columns[c - 1])
                val colData = arrayListOf<Array<Any>>()
                for (row in data) {
                    if (row[c]!! != "-") {
                        Log.d("Graphing", "Adding Datapoint")
                        colData.add(arrayOf(row[0]!!.toLong(), row[c]!!.toInt()))
                    }
                }
                val currentLine = AASeriesElement().name(columns[c - 1]).data(colData.toTypedArray())
                graphData.add(currentLine)
            }
        } else if (chartType == "pie") {
            var total = 0
            for (c in 0 until columns.size) {
                total += data[0][c]!!.toInt()
            }
            val denominator = total
            val colData = arrayListOf<Array<Any>>()
            for (c in 0 until columns.size) {
                Log.d("Category Loop", "Looping to Category " + columns[c])

                if (data[0][c]!! != "-") {
                    Log.d("Graphing", "Adding Datapoint")
                    colData.add(arrayOf(columns[c], (data[0][c]!!.toInt().toFloat() / denominator)))
                }
            }
            val pieCat = AASeriesElement().name("Proportion").data(colData.toTypedArray())
            graphData.add(pieCat)

        } else {
            for (c in 0 until columns.size) {
                val colData = arrayListOf<Array<Any>>()
                Log.d("Category Loop", "Looping to Category " + columns[c])

                if (data[0][c]!! != "-") {
                    Log.d("Graphing", "Adding Datapoint")
                    colData.add(arrayOf("Values", data[0][c]!!.toInt()))
                }
                val barCat = AASeriesElement().name(columns[c]).data(colData.toTypedArray())
                graphData.add(barCat)
            }


        }


        val chartModel = AAChartModel()
            .chartType(chartToDisp)
            .title(tableName)
            .backgroundColor("#FFFFFF")
            .dataLabelsEnabled(true)
            .series(graphData.toTypedArray())
            .xAxisLabelsEnabled(false)
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
}