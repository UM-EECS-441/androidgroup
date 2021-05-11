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

    //Create a new categorical dataset called spending with three columns, food, gas, and rent
    //
    //
    //Insert 32 pushups, 53 situps, and 45 squats into exercise

    private fun insertDataset(text:String, tableName:String?):Boolean {
        var words = text.split(" ").toMutableList()
        if(words[0] == "insert" || words[0] == "add" || words[0] == "enter") {

            //remove the primer word from the command
            words.removeAt(0)

            //remove any ands from the command so it has only column names and values followed by "into [dataset]"
            while (words.indexOf("and") != -1) {
                words.removeAt(words.indexOf("and"))
            }
            name = tableName

            if (tableName == null) {
                val idx = words.indexOf("into")
                if (idx == -1 || idx == words.size - 1) return false
                name = words[idx + 1].capitalize()
                if (!DatabaseHelper(context).getAllDatabaseNames().contains(name!!)) {
                    return false
                }
                Log.i("SpeechRecognizer", "dataset name: ".plus(name))
            }

            var columns = DatabaseHelper(context).getColumnNames(name!!)
            Log.e("columns", columns.toString())

            var data = ArrayList<Pair<String, String>>()
            for (w in words.indices step 2) {
                if (words[w] == "into") break
                val num = textToInt(words[w]) ?: return false
                val colName = words[w+1]
                if (columns.contains(colName.capitalize()) || columns.contains(colName)) {
                    data.add(Pair(colName.capitalize(), num.toString()))
                } else {
                    Log.e("VoiceInsert", "Invalid Column")
                    return false
                }
            }

            DatabaseHelper(context).insertData(name!!, data)
            return true
        }
        return false
    }

    private fun createDataset(text: String): Boolean {
        if ((text.contains("make") || text.contains("create") ||
                    text.contains("new") || text.contains("start")) &&
            (text.contains("data") || text.contains("dating site"))) {
            var nameStart = text.length
            var nameEnd = text.length
            var numColumns = 0
            var columnNames = arrayListOf<String>()
            var isCategorical = 0
            if (text.contains("categorical")) isCategorical = 1
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
                            numColumns = this.textToInt(textList[idx - 1])!!
                            if (numColumns == -1 || numColumns > 5) {
                                Toast.makeText(context, "Invalid column number", Toast.LENGTH_SHORT).show()
                                throw Exception("Invalid column number")
                            }
                        }
                        var columnCount = 0
                        for (i in (idx + 1) until textList.size) {
                            if (columnCount >= numColumns) break
                            if (textList[i] == "and" || textList[i] == "+" || textList[i] == "called" || textList[i] == "named") continue
                            columnNames.add(textList[i].capitalize())
                            columnCount += 1
                        }
                        if (columnCount == 0) {
                            columnNames = ArrayList(arrayListOf("A", "B", "C", "D", "E").take(numColumns))
                        }
                        Log.i("SpeechRecognizer", "column names: ".plus(columnNames.joinToString(" ")))
                        break
                    }
                }
                name = text.substring(nameStart, nameEnd).capitalize()
                if(name == " ") return false

                // Run create dataset function

                if (columnNames.isEmpty()) {
                    DatabaseHelper(context).createNewDataset(name!!, arrayListOf<String>("Y"), isCategorical)
                } else {
                    DatabaseHelper(context).createNewDataset(name!!, columnNames, isCategorical)
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

    private fun textToInt(text: String): Int? {
        when (text.toLowerCase()) {
            "one" -> {
                return 1
            }
            "won" -> {
                return 1
            }
            "two" -> {
                return 2
            }
            "too" -> {
                return 2
            }
            "to" -> {
                return 2
            }
            "three" -> {
                return 3
            }
            "four" -> {
                return 4
            }
            "for" -> {
                return 4
            }
            "fore" -> {
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
            "ate" -> {
                return 8
            }
            "nine" -> {
                return 9
            }
            "nein" -> {
                return 9
            }
            "zero" -> {
                return 0
            }
            "ten" -> {
                return 10
            }
            else -> return text.toIntOrNull()
        }
    }
}

