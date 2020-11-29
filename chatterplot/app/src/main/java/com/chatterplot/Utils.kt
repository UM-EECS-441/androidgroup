package com.chatterplot

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.Animation
import com.github.aachartmodel.aainfographics.aachartcreator.*
import java.io.ByteArrayOutputStream
import java.io.File

fun convertViewToBitmap(view: View): Bitmap {
    Log.d("width", view.measuredWidth.toString())
    Log.d("height", view.measuredHeight.toString())
    val img = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(img)
    val drawable = view.background
    if(drawable != null) {
        drawable.draw(canvas)
    }
    else {
        canvas.drawColor(Color.WHITE)
    }
    view.draw(canvas)
    return img
}

fun saveBitmap(context: Context, img: Bitmap, fileName: String) {
    val file = File(context.filesDir, fileName)
    val byteArray = ByteArrayOutputStream()
    img.compress(Bitmap.CompressFormat.PNG, 100, byteArray)
    file.writeBytes(byteArray.toByteArray())
}

fun deleteFile(context: Context, fileName: String) {
    val file = File(context.filesDir, fileName)
    if(file.isFile) {
        file.delete()
    }
}

fun createPreview(context: Context, datasetName: String) {
    val preview = AAChartView(context)
    preview.callBack = GraphCallback(context, datasetName, preview)
    val width = Resources.getSystem().displayMetrics.widthPixels
//    val height = Resources.getSystem().displayMetrics.heightPixels
    preview.layout(0, 0, width, 400)

    val data = DatabaseHelper(context).getTable(datasetName)
    val graphData = ArrayList<AASeriesElement>()
    val xAxisColumnName = DatabaseHelper(context).getXAxisColumn(datasetName)
    val xValArray = data[xAxisColumnName] ?: ArrayList()

    for((key, value) in data) {
        if(key != "ID" && key != "Timestamp" && key != xAxisColumnName) {
            val current = AASeriesElement().name(key).data(Array<Any>(value.size) { it ->
                arrayOf(xValArray[it], (value[it] as String).toInt())
            })
            graphData.add(current)
        }
    }
    val chartModel = AAChartModel()
        .chartType(AAChartType.Line)
        .title("")
        .subtitle("")
        .yAxisTitle("")
        .backgroundColor("#FFFFFF")
        .legendEnabled(false)
        .series(graphData.toTypedArray())
        .xAxisLabelsEnabled(false)
        .yAxisLabelsEnabled(false)
        .dataLabelsEnabled(false)
        .xAxisTickInterval(0)
        .yAxisGridLineWidth(0F)
        .xAxisVisible(false)
    preview.aa_drawChartWithChartModel(chartModel)

}

class GraphCallback(val context: Context, val fileName: String, val view: View) : AAChartView.AAChartViewCallBack {
    override fun chartViewDidFinishLoad(aaChartView: AAChartView) {
        Handler().postDelayed({
            val img = convertViewToBitmap(view)
            saveBitmap(context, img, "$fileName.png")
        }, 2000)
    }

    override fun chartViewMoveOverEventMessage(
        aaChartView: AAChartView,
        messageModel: AAMoveOverEventMessageModel
    ) {
        Log.e("When", "Trigger")
    }
}