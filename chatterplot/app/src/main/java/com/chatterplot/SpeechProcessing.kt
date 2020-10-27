package com.chatterplot

import android.content.Context
import android.util.Log
import android.widget.Toast


class SpeechProcessor(ctext: Context) {
     var context = ctext
     fun textProcessing(text: String) {
        if ((text.contains("make") || text.contains("create")) && text.contains("data")) {
            var name = " "
            for (word in text.split(" ")) {
                Log.i("SpeechRecognizer", "text response word: ".plus(word))
                if (word == "name") {
                    var idx = text.indexOf("name") + 5
                    name = text.substring(idx)
                    break
                } else if (word == "named") {
                    var idx = text.indexOf("named") + 6
                    name = text.substring(idx)
                    break
                }
            }
            Log.i("SpeechRecognizer","creating dataset named: ".plus(name))
            Toast.makeText(context, "Creating Dataset named: ".plus(name), Toast.LENGTH_SHORT).show()
            // Run create dataset function
        }
    }
}

