package com.chatterplot

import android.content.Context
import android.util.Log
import android.widget.Toast


class SpeechProcessor(ctext: Context) {
    var context = ctext
    var name = "Dataset"

    fun textProcessing(text: String):Boolean {
        if (this.createDataset(text)) {
            Log.i("SpeechRecognizer","created dataset")
            return true
        } else {
            Log.i("SpeechRecognizer","command not recognized")
            Toast.makeText(context, "Command not recognized", Toast.LENGTH_SHORT).show()
            return false
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
}

