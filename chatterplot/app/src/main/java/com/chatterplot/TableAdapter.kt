package com.chatterplot

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
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
    var columnData = mutableListOf<Int>() // data will be preloaded into this list from the db
    var tableView: TableLayout? = null
    var context: Context? = null

    constructor(context: Context, pathDB: String, tableNameDB: String) {
        this.context = context
        tableView = (this.context as DisplayDataActivity).findViewById<TableLayout>(R.id.dataDisplayTable)
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
//        columnData.add(dataCursor.getInt(0))  //get col 0
//        while (dataCursor.moveToNext()) {
//            columnData.add(dataCursor.getInt(0))
//        }
    }

    /*
    * Fetches the item at row <position>.
    *
    * currently, our datasets only hold single-column numerical data
    *
    * */
    fun getItem(position: Int): Int? {
        if (position >= columnData.size) {
            return null
        }
        return columnData[position]
    }


    /*
    * Loads table in context with id dataDisplayTable with data from the dataset determined by
    * columnNameDB
    *
    * */
    fun loadTable() {
        //TESTING TABLEADAPTER
        for (i in 10 until 111) {
            columnData.add(i)
        }

        //END TESTING

        for (row in 0 until columnData.size) {
            var newRow: TableRow = createTableRow(3)
            var rowLayoutParams: TableRow.LayoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT)

            //show row number in table
            var rowNum:TextView = newRow.getChildAt(0) as TextView
            rowNum.text = row.toString()

            //data value
            var item: TextView = newRow.getChildAt(1) as TextView
            item.text = columnData[row].toString()
            (tableView as ViewGroup).addView(newRow)

            //for (item in 0 until (newRow as ViewGroup).childCount) {

            //}
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