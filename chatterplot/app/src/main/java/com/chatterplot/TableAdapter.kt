package com.chatterplot

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.setPadding


class TableAdapter {
    private lateinit var tableData: MutableMap<String, ArrayList<Any>> // data will be preloaded into this list from the db
    private var tableView: TableLayout? = null
    var context: Context? = null
    private lateinit var tableNameDB: String

    constructor(context: Context, tableNameDB: String) {
        this.context = context
        this.tableNameDB = tableNameDB
        tableView = (this.context as DisplayDataActivity).findViewById<TableLayout>(R.id.dataDisplayTable)

        tableData = DatabaseHelper(this.context!!).getTable(tableNameDB)
//        var db: SQLiteDatabase = SQLiteDatabase.openDatabase(pathDB, null, 0)
//
//        val pullDataQuery: String = ""  //TODO write the actual query dependent on schema
//        val selectionArgs: Array<String> = {}  // also depend on schema (fill in ? in pullDataQuery)
//
//        var dataCursor = db.rawQuery(pullDataQuery, selectionArgs)
//
//        // retrieve data from cursor and preload into MutableList for easier manipulation and
//        // quicker retrieval
//        dataCursor.moveToFirst()
//        tableData.add(dataCursor.getInt(0))  //get col 0
//        while (dataCursor.moveToNext()) {
//            tableData.add(dataCursor.getInt(0))
//        }
    }

    /*
    * Fetches the item at row <position>.
    *
    * currently, our datasets only hold single-column numerical data
    *
    * */
//    fun getItem(position: Int): Int? {
//        if (position >= tableData.size) {
//            return null
//        }
//        return tableData[position]
//    }


    /*
    * Loads table in context with id dataDisplayTable with data from the dataset determined by
    * columnNameDB
    *
    * */
    fun loadTable() {
        //TODO add title row with column names
        var titleRow: TableRow = createTableRow(tableData.keys.size - 2)
        var rowLayoutParams: TableRow.LayoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT)
        titleRow.layoutParams = rowLayoutParams

        var i = 0
        for ((colKey, _) in tableData) {
            var tv = titleRow.getChildAt(i) as TextView
            tv.text = colKey
            tv.setTypeface(null, Typeface.BOLD)
            ++i
        }


        for (row in 0 until tableData["ID"]!!.size) {
            var newRow: TableRow = createTableRow(tableData.keys.size - 2)  // exclude timestamp and ID
            var rowLayoutParams: TableRow.LayoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT)
            newRow.layoutParams = rowLayoutParams

            var colNum: Int = 0
            for ((colKey, colData) in tableData) {
                if (colKey != "ID" && colKey != "Timestamp") {
                    var rowNum: TextView = newRow.getChildAt(colNum) as TextView
                    rowNum.text = colData[row].toString()
                    ++colNum
                }
            }
            (tableView as ViewGroup).addView(newRow)

            //show row number in table
//            var rowNum:TextView = newRow.getChildAt(0) as TextView
//            rowNum.text = row.toString()
//
//            //data value
//            var item: TextView = newRow.getChildAt(1) as TextView
//            item.text = tableData[row].toString()

        }
    }

    private fun createTableRow(numColumns: Int): TableRow {
        var row = TableRow(context)
        row.setPadding(5)
        for (i in 1..numColumns) {
            var item = TextView(context)
            row.addView(item)
            item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
            (item.layoutParams as TableRow.LayoutParams).weight = 1f
            (item.layoutParams as TableRow.LayoutParams).height = TableRow.LayoutParams.WRAP_CONTENT
            (item.layoutParams as TableRow.LayoutParams).width = TableRow.LayoutParams.WRAP_CONTENT
        }
        return row
    }
}