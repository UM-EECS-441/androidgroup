package com.chatterplot

import android.content.Context
import android.util.Log
import android.widget.Toast


class SpeechProcessor(ctext: Context) {
    var context = ctext

    fun textProcessing(text: String) {
        if (this.createDataset(text)) {
            Log.i("SpeechRecognizer","created dataset")
        } else {
            Log.i("SpeechRecognizer","command not recognized")
            Toast.makeText(context, "Command not recognized", Toast.LENGTH_SHORT).show()
        }
    }

    fun insertDataset(text:String, tableName:String):Boolean {
        if((text.contains("insert") || text.contains("add") ||
                    text.contains("enter"))) {
            val words= text.split(" ").toMutableList()
//            val numbers = ArrayList<Int>()
            words.removeAt(0)
            for(word in words) {
                word.toIntOrNull() ?: return false
//                numbers.add(words[idx].toIntOrNull() ?: return false)
            }
            DatabaseHelper(context).insertRow(tableName, ArrayList(words))
            return true
        }
        return false
    }

    private fun createDataset(text: String): Boolean {
        if ((text.contains("make") || text.contains("create") ||
                    text.contains("new") || text.contains("start")) &&
            text.contains("data")) {
            var name = " "
            for (word in text.split(" ")) {
                Log.i("SpeechRecognizer", "text response word: ".plus(word))
                if (word == "called" || word == "named" || word == "titled") {
                    var idx = text.indexOf(word) + word.length + 1
                    name = text.substring(idx)
                    break
                }
            }
            Log.i("SpeechRecognizer","creating dataset named: ".plus(name))
            Toast.makeText(context, "Creating Dataset named: ".plus(name), Toast.LENGTH_SHORT).show()
            // Run create dataset function
            DatabaseHelper(context).createDataset(name, "X", "Y")
            return true
        }
        return false
    }
}

