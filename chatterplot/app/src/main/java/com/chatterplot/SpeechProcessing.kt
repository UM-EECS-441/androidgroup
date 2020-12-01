package com.chatterplot

import android.content.Context
import android.util.Log
import android.widget.Toast


class SpeechProcessor(ctext: Context) {
    var context = ctext
    var name: String? = " "

    fun textProcessing(text: String, tableName: String?):Int {
        if (this.createDataset(text)) {
            Log.i("SpeechRecognizer","created dataset")
            return 1
        } else if (this.insertDataset(text, tableName)) {
            Log.i("SpeechRecognizer","inserted data to dataset: ".plus(name))
            return 2
        } else {
            Log.i("SpeechRecognizer","command not recognized")
            Toast.makeText(context, "Command not recognized", Toast.LENGTH_SHORT).show()
            return 0
        }
    }

    private fun insertDataset(text:String, tableName:String?):Boolean {
        if((text.contains("insert") || text.contains("add") ||
                    text.contains("enter"))) {
            val words= text.split(" ").toMutableList()
//            val numbers = ArrayList<Int>()
            words.removeAt(0)
            name = tableName
            if (tableName == null) {
                val idx = text.indexOf("into")
                if (idx == -1 || idx + 5 >= text.length) return false
                name = text.substring(idx + 5).capitalize()
                if (!DatabaseHelper(context).getAllDatabaseNames().contains(name!!)) {
                    return false
                }
                Log.i("SpeechRecognizer", "dataset name: ".plus(name))
            }
            val data = ArrayList<String>()
            for(word in words) {
                if (word == "into") break
                if (word != "and" && word != "+") {
                    if(word.toIntOrNull() == null) {
                        return false
                    }
                    else {
                        data.add(word)
                    }
                }
//                numbers.add(words[idx].toIntOrNull() ?: return false)
            }
            var columns = DatabaseHelper(context).getColumnNames(name!!)
            if (data.size != columns.size) {
                Toast.makeText(context, "Invalid data entry", Toast.LENGTH_SHORT).show()
                return false
            }
            DatabaseHelper(context).insertRow(name!!, data)
            return true
        }
        return false
    }

    private fun createDataset(text: String): Boolean {
        if ((text.contains("make") || text.contains("create") ||
                    text.contains("new") || text.contains("start")) &&
            text.contains("data")) {
            var nameStart = text.length
            var nameEnd = text.length
            var numColumns: Int = 0
            var columnNames = mutableListOf<String>()
            try {
                for ((idx, word) in text.split(" ").withIndex()) {
                    Log.i("SpeechRecognizer", "text response word: ".plus(word))
                    if (word == "called" || word == "named" || word == "titled") {
                        nameStart = text.indexOf(word) + word.length + 1
                        if(nameStart >= text.length) return false
                    } else if (word == "column" || word == "columns") {
                        nameEnd = text.indexOf("with") - 1
                        val textList = text.split(" ")
                        try {
                            numColumns = textList[idx - 1].toInt()
                        } catch (e:Exception) {
                            numColumns = this.textToInt(textList[idx - 1])
                            if (numColumns == -1 || numColumns > 5) {
                                Toast.makeText(context, "Invalid column number", Toast.LENGTH_SHORT).show()
                                throw Exception("Invalid column number")
                            }
                        }
                        var columnCount = 0
                        for (i in (idx + 2) until textList.size) {
                            if (columnCount >= numColumns) break
                            if (textList[i] == "and" || textList[i] == "+") continue
                            columnNames.add(textList[i])
                            columnCount += 1
                        }
                        if (columnCount == 0) {
                            columnNames = arrayListOf("A", "B", "C", "D", "E").take(numColumns).toMutableList()
                        }
                        Log.i("SpeechRecognizer", "column names: ".plus(columnNames.joinToString(" ")))
                        break
                    }
                }
                name = text.substring(nameStart, nameEnd).capitalize()
                if(name == " ") return false

                // Run create dataset function

                if (columnNames.isEmpty()) {
                    DatabaseHelper(context).createDataset(name!!, mutableListOf<String>("Y"))
                } else {
                    DatabaseHelper(context).createDataset(name!!, columnNames)
                }
                Log.i("SpeechRecognizer","creating dataset named: ".plus(name))
                Toast.makeText(context, "Creating Dataset named: ".plus(name), Toast.LENGTH_SHORT).show()
                return true
            } catch(e:Exception) {
                Log.i("SpeechRecognizer","Error: ".plus(e.message))
                return false
            }
        }
        return false
    }

    private fun textToInt(text: String): Int {
        when (text.toLowerCase()) {
            "one" -> {
                return 1
            }
            "two" -> {
                return 2
            }
            "three" -> {
                return 3
            }
            "four" -> {
                return 4
            }
            "five" -> {
                return 5
            }
            "six" -> {
                return 6
            }
            "seven" -> {
                return 7
            }
            "eight" -> {
                return 8
            }
            "nine" -> {
                return 9
            }
            "zero" -> {
                return 0
            }
            else -> return -1
        }
    }
}

