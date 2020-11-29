package com.chatterplot

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
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
    }

    fun refreshTable() {
        tableData = DatabaseHelper(this.context!!).getTable(tableNameDB)
        loadTable()
    }


    /*
    * Loads table in context with id dataDisplayTable with data from the dataset determined by
    * columnNameDB
    *
    * */
    fun loadTable() {
        tableView?.removeAllViews()
        var titleRow: TableRow = createTableRow(tableData.keys.size - 2, true)
        var rowLayoutParams: TableRow.LayoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT)
        titleRow.layoutParams = rowLayoutParams

        var i = 0
        for ((colKey, _) in tableData) {
            if (colKey != "ID" && colKey != "Timestamp") {
                var rowNum: TextView = titleRow.getChildAt(i) as TextView
                rowNum.text = colKey.toString()
                rowNum.setTypeface(null, Typeface.BOLD)
                ++i
            }
        }
        (tableView as ViewGroup).addView(titleRow)

        for (row in 0 until getNumRows()) {
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
                    rowNum.setTag(R.id.TAG_DB_ROW, row)  // 0: row, 1: column name
                    rowNum.setTag(R.id.TAG_DB_COL, colKey)
                    ++colNum
                }
            }
            (tableView as ViewGroup).addView(newRow)
        }
    }


    private fun createTableRow(numColumns: Int, firstRow: Boolean =false): TableRow {
        var row = TableRow(context)
        row.setPadding(5)
        for (i in 1..numColumns) {
            var item = TextView(context)
            item.isFocusable = true
            if (!firstRow) {
                item.setOnClickListener { v ->
                    var currTextView: TextView = v as TextView
                    currTextView.isCursorVisible = true
                    currTextView.isFocusableInTouchMode = true
                    currTextView.inputType = InputType.TYPE_CLASS_NUMBER
                    currTextView.requestFocus()  //to trigger the soft input
                }

                item.setOnEditorActionListener { v, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        val row = (v as View).getTag(R.id.TAG_DB_ROW)!! as Int + 1
                        val column = (v as View).getTag(R.id.TAG_DB_COL)!! as String
                        // update the value in the database to match the inputted
                        // key 0 is for row , key 1 is for column name
                        DatabaseHelper(this.context!!).editValue(
                            this.tableNameDB,
                            row,
                            column,
                            v.text.toString()
                        )
                        true
                    }
                    false
                }
            }

            row.addView(item)
            item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
            (item.layoutParams as TableRow.LayoutParams).weight = 1f
            (item.layoutParams as TableRow.LayoutParams).height = TableRow.LayoutParams.WRAP_CONTENT
            (item.layoutParams as TableRow.LayoutParams).width = TableRow.LayoutParams.WRAP_CONTENT
        }
        return row
    }


    fun getDataRow(row: Int): List<Any> {
        var data: ArrayList<Any> = arrayListOf()
        for ((colHeader, colData) in tableData) {
            if (colHeader != "ID" && colHeader != "Timestamp") {
                data.add(colData[row])
            }
        }
        return data.toList()
    }


    fun getNumRows(): Int {
        return tableData["ID"]!!.size
    }


    fun getColumnNames(): List<String> {
        var names: ArrayList<String> = arrayListOf()
        for ((colName, _) in tableData) {
            names.add(colName)
        }
        return names.toList()
    }
}