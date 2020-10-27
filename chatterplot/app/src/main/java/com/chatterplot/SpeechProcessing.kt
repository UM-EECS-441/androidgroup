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

