package com.chatterplot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import android.view.View
import java.io.ByteArrayOutputStream
import java.io.File

fun convertViewToBitmap(view: View): Bitmap {
    Log.e("width", view.measuredWidth.toString())
    Log.e("height", view.measuredHeight.toString())
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