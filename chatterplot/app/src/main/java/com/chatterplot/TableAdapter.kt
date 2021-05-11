package com.chatterplot

import android.content.ContentValues
import android.content.Context
import android.graphics.Typeface
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.setPadding
import java.util.*
import kotlin.collections.ArrayList


class TableAdapter {
    private var tableData: ArrayList<ArrayList<String?>> // data will be preloaded into this list from the db
    private var tableView: TableLayout? = null
    var context: Context? = null
    private var tableNameDB: String
    private var columns: ArrayList<String>

    constructor(context: Context, tableNameDB: String) {
        this.context = context
        this.tableNameDB = tableNameDB
        tableView = (this.context as DisplayDataActivity).findViewById(R.id.dataDisplayTable)
        tableData = DatabaseHelper(this.context!!).isolateDataset(tableNameDB)
        columns = DatabaseHelper(this.context!!).getColumnNames(tableNameDB)
    }

    fun refreshTable() {
        tableData = DatabaseHelper(this.context!!).isolateDataset(tableNameDB)
        loadTable()
    }


    fun loadTable() {
        tableView?.removeAllViews()

        var titleRow: TableRow = createTableRow(columns.size + 1, true)
        var rowLayoutParams: TableRow.LayoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT)
        titleRow.layoutParams = rowLayoutParams


        // Title Row //
        var i = 0
        val isCategorical = DatabaseHelper(this.context!!).isCategorical(tableNameDB)
        if (!isCategorical) {
            var rowText: TextView = titleRow.getChildAt(0) as TextView
            rowText.text = DatabaseHelper(this.context!!).getResolutionName(tableNameDB)
            rowText.setTypeface(null, Typeface.BOLD)
            i = 1
        }
        for (colName in columns) {
            var rowText: TextView = titleRow.getChildAt(i) as TextView
            ++i
            rowText.text = colName
            rowText.setTypeface(null, Typeface.BOLD)
        }

        (tableView as ViewGroup).addView(titleRow)
        // Title Row //


        for (row in 0 until getNumRows()) {
            var newRow: TableRow = createTableRow(tableData[0].size)
            var rowLayoutParams: TableRow.LayoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT)
            newRow.layoutParams = rowLayoutParams

            for ((colNum, cell) in tableData[row].withIndex()) {
                var rowText: TextView = newRow.getChildAt(colNum) as TextView
                rowText.text = cell
            }
            (tableView as ViewGroup).addView(newRow)
        }
        val placeholder: TableRow = createTableRow(columns.size + 1)
        placeholder.layoutParams = rowLayoutParams
        (tableView as ViewGroup).addView(placeholder)
    }


    private fun createTableRow(numColumns: Int, firstRow: Boolean=false): TableRow {
        var row = TableRow(context)
        row.setPadding(5)
        for (i in 1..numColumns) {
            var item = TextView(context)
            item.setPadding(50, 10, 100, 10)
            item.textSize = 20F
            item.isFocusable = false
            if (!firstRow) {
                item.textSize = 16F
//                item.setOnClickListener { v ->
//                    var currTextView: TextView = v as TextView
//                    currTextView.isCursorVisible = true
//                    currTextView.isFocusableInTouchMode = true
//                    currTextView.inputType = InputType.TYPE_CLASS_NUMBER
//                    currTextView.requestFocus()  //to trigger the soft input
//                }
//
//                item.setOnEditorActionListener { v, actionId, event ->
//                    if (actionId == EditorInfo.IME_ACTION_DONE) {
//                        val row = (v as View).getTag(R.id.TAG_DB_ROW)!! as Int + 1
//                        val column = (v as View).getTag(R.id.TAG_DB_COL)!! as String
//                        // update the value in the database to match the inputted
//                        // key 0 is for row , key 1 is for column name
//                        Log.d("welp row", row.toString())
//                        Log.d("welp col", column.toString())
//                        var cv: ContentValues = ContentValues()
//                        cv.put(column, v.text.toString())
//                        DatabaseHelper(this.context!!).updateRow(
//                            this.tableNameDB,
//                            cv,
//                            "ID=$row",
//                            null
////                            row,
////                            column,
////                            v.text.toString()
//                        )
//                        true
//                    }
//                    false
//                }
            }

            row.addView(item)
//            item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
            (item.layoutParams as TableRow.LayoutParams).weight = 1f
            (item.layoutParams as TableRow.LayoutParams).height = TableRow.LayoutParams.WRAP_CONTENT
            (item.layoutParams as TableRow.LayoutParams).width = TableRow.LayoutParams.WRAP_CONTENT
        }
        return row
    }

    fun getDataRow(row: Int): List<Any> {
        Log.e("getDataRow", "Defunct")
        return listOf(0)
    }

    fun getNumRows(): Int {
        return tableData.size!!
    }
}