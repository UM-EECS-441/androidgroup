package com.chatterplot

import android.content.Context
import android.renderscript.Element
import android.util.Log
import android.widget.Toast
import com.chatterplot.ml.Label
import com.chatterplot.ml.LabelEnd
import com.chatterplot.ml.Title
import com.chatterplot.ml.TitleEnd
import org.tensorflow.lite.DataType
import org.tensorflow.lite.TensorFlowLite
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder


class SpeechProcessor(ctext: Context) {
    var context = ctext
    var name = "Dataset"
    init {
        name_beg = Title.newInstance(context)
        name_end = TitleEnd.newInstance(context)
        label_beg = Label.newInstance(context)
        label_end = LabelEnd.newInstance(context)
    }

    fun textProcessing(text: String):Boolean {
        return if (this.parseCreate(text)) {
            Log.i("SpeechRecognizer","created dataset")
            true
        } else {
            Log.i("SpeechRecognizer","command not recognized")
            Toast.makeText(context, "Command not recognized", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun insertDataset(text:String, tableName:String):Boolean {
        if((text.contains("insert") || text.contains("add") ||
                    text.contains("enter"))) {
            val words= text.split(" ").toMutableList()
//            val numbers = ArrayList<Int>()
            words.removeAt(0)
            val data = ArrayList<String>()
            for(word in words) {
                if(word != "and" && word != "+") {
                    if(word.toIntOrNull() ==null) {
                        return false
                    }
                    else {
                        data.add(word)
                    }
                }
//                numbers.add(words[idx].toIntOrNull() ?: return false)
            }
            DatabaseHelper(context).insertRow(tableName, data)
            return true
        }
        return false
    }

    private fun convertToByteBuffer(sentence: String): ByteBuffer {
        val splitted = sentence.split(" ")
        val out = arrayListOf<Int>()
        for (word in splitted) {
            var group = 55
            for ((key, value) in categories) {
                if(word.toLowerCase() in value) {
                    group = key
                    break
                }
            }
            out.add(group)
        }
        while(out.size < 20) {
            out.add(-10)
        }

        val byteBuffer = ByteBuffer.allocateDirect(80)
        byteBuffer.order(ByteOrder.nativeOrder())
        for (elt in out) {
            byteBuffer.putFloat(elt.toFloat())
        }
        byteBuffer.rewind()
        return byteBuffer
    }

    private fun predict(buffer: ByteBuffer): Int {
        buffer.rewind()
        var max = 0f
        var maxIdx = -1
        for (idx in 0 until 20) {
            val current = buffer.float
            if (current > max) {
                max = current
                maxIdx = idx
            }
        }
        return maxIdx
    }

    private fun parseName(text: String, byteBuffer: ByteBuffer): String {
        byteBuffer.rewind()
        val input = TensorBuffer.createFixedSize(intArrayOf(1, 20), DataType.FLOAT32)
        input.loadBuffer(byteBuffer)
        val output_nb = name_beg.process(input).outputFeature0AsTensorBuffer.buffer
        byteBuffer.rewind()

        input.loadBuffer(byteBuffer)
        val output_ne = name_end.process(input).outputFeature0AsTensorBuffer.buffer

        val nameBegin = predict(output_nb)
        val nameEnd = predict(output_ne)
        val limit = text.split(" ").size
        if(nameBegin < 0 || nameEnd < 0 || nameBegin >= limit || nameEnd >= limit) {
            return ""
        }
        return text.split(" ").subList(nameBegin, nameEnd+1).joinToString(" ")
    }

    private fun parseLabel(text:String, byteBuffer: ByteBuffer): ArrayList<String> {
        byteBuffer.rewind()
        val input = TensorBuffer.createFixedSize(intArrayOf(1, 20), DataType.FLOAT32)
        input.loadBuffer(byteBuffer)
        val output_lb = label_beg.process(input).outputFeature0AsTensorBuffer.buffer

        byteBuffer.rewind()
        input.loadBuffer(byteBuffer)
        val output_le = label_end.process(input).outputFeature0AsTensorBuffer.buffer

        val begin = predict(output_lb)
        val end = predict(output_le)
        val limit = text.split(" ").size
        if(begin < 0 || end < 0 || begin >= limit || end >= limit) {
            return ArrayList()
        }
        val labels = ArrayList(text.split(" ").subList(begin, end+1))
        val res = ArrayList<String>()
        var prev = 0
        for((idx, elt) in labels.withIndex()) {
            if(elt == "and") {
                res.add(labels.subList(prev, idx).joinToString(" "))
                prev = idx+1
            }
        }
        res.add(labels.subList(prev, labels.size).joinToString(" "))
//        if(prev == 0) {
//            res.add(labels.joinToString(" "))
//        }

        return res
    }

    private fun parseCreate(text: String): Boolean {
        if (!text.contains("make") && !text.contains("create") && !text.contains("new") &&
            !text.contains("new") && !text.contains("start")) {
            return false
        }
        val byteBuffer = convertToByteBuffer(text)
        name = parseName(text, byteBuffer)
        val labels = parseLabel(text, byteBuffer)
        Log.e("labels", labels.toString())

        try {
            DatabaseHelper(context).createDataset(name, labels)
        } catch(e: Exception) {
            Log.e("Database Error", e.localizedMessage)
            return false
        }

        Toast.makeText(context, "Creating Dataset named $name", Toast.LENGTH_SHORT).show()
        Log.e("name", name)
        return true
    }

    private fun createDataset(text: String): Boolean {
        if ((text.contains("make") || text.contains("create") ||
                    text.contains("new") || text.contains("start")) &&
            text.contains("data")) {
            name = " "
            for (word in text.split(" ")) {
                Log.i("SpeechRecognizer", "text response word: ".plus(word))
                if (word == "called" || word == "named" || word == "titled" || word == "name") {
                    val idx = text.indexOf(word) + word.length + 1
                    if(idx >= text.length) return false
                    name = text.substring(idx).capitalize()
                    break
                }
            }
            if(name == " ") {
                return false
            }
            Log.i("SpeechRecognizer","creating dataset named: ".plus(name))
            Toast.makeText(context, "Creating Dataset named: ".plus(name), Toast.LENGTH_SHORT).show()
            // Run create dataset function
            try {
                DatabaseHelper(context).createDataset(name, arrayListOf("Y"))
            } catch(e:Exception) {
                return false
            }
            return true
        }
        return false
    }

    companion object {
        private val categories = hashMapOf(
            0 to arrayOf("create", "add", "insert", "make", "build", "start"),
            1 to arrayOf("a", "the"),
            2 to arrayOf("is", "are", "that"),
            3 to arrayOf("and"),
            5 to arrayOf("dataset", "graph", "table"),
            8 to arrayOf("into", "to"),
            13 to arrayOf("with"),
            21 to arrayOf("labeled", "label", "axis", "variable", "column"),
            34 to arrayOf("named", "called", "name", "call")
        )
        lateinit var name_beg: Title
        lateinit var name_end: TitleEnd
        lateinit var label_beg: Label
        lateinit var label_end: LabelEnd
    }
}

