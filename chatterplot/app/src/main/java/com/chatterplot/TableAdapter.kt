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


class TableAdapter {
    private var tableData: MutableMap<String, ArrayList<Any>> // data will be preloaded into this list from the db
    private var tableView: TableLayout? = null
    var context: Context? = null
    private var tableNameDB: String
    private var xAxisColName: String

    constructor(context: Context, tableNameDB: String) {
        this.context = context
        this.tableNameDB = tableNameDB
        tableView = (this.context as DisplayDataActivity).findViewById(R.id.dataDisplayTable)
        tableData = DatabaseHelper(this.context!!).getTable(tableNameDB)
        Log.d("welp", tableData.keys.toString())
        xAxisColName = DatabaseHelper(this.context!!).getXAxisColumn(tableNameDB)
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

    fun refreshTable() {
        tableData = DatabaseHelper(this.context!!).getTable(tableNameDB)
        xAxisColName = DatabaseHelper(this.context!!).getXAxisColumn(tableNameDB)
        loadTable()
    }


    /*
    * Loads table in context with id dataDisplayTable with data from the dataset determined by
    * columnNameDB
    *
    * */
    fun loadTable() {
        tableView?.removeAllViews()

        val graphedByDate = (xAxisColName == "Timestamp")
        if (!graphedByDate) {
            Log.d("welp", "not by date")
            var titleRow: TableRow = createTableRow(tableData.keys.size - 2, true) //exclude ID and timestamp rows
            var rowLayoutParams: TableRow.LayoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT)
            titleRow.layoutParams = rowLayoutParams

            var i = 1
            for ((colKey, _) in tableData) {
                if (colKey != "ID" && colKey != "Timestamp") {
                    var rowText: TextView
                    if (colKey == xAxisColName) {
                        rowText = titleRow.getChildAt(0) as TextView
                    } else {
                        rowText = titleRow.getChildAt(i) as TextView
                        ++i
                    }
                    rowText.text = colKey
                    rowText.setTypeface(null, Typeface.BOLD)
                }
            }
            (tableView as ViewGroup).addView(titleRow)
            for (row in 0 until getNumRows()) {
                Log.d("welp num rows", getNumRows().toString())
                Log.d("welp this row", row.toString())
                var newRow: TableRow = createTableRow(tableData.keys.size - 2)  // exclude timestamp and ID
                var rowLayoutParams: TableRow.LayoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT)
                newRow.layoutParams = rowLayoutParams

                var colNum = 1
                for ((colKey, colData) in tableData) {
                    if (colKey != "ID" && colKey != "Timestamp") {
                        var rowText: TextView
                        if (colKey == xAxisColName) {
                            rowText = newRow.getChildAt(0) as TextView
                        } else {
                            rowText = newRow.getChildAt(colNum) as TextView
                            ++colNum
                        }
                        rowText.text = colData[row].toString()
                        rowText.setTag(R.id.TAG_DB_ROW, row)  // 0: row, 1: column name
                        rowText.setTag(R.id.TAG_DB_COL, colKey)
                    }
                }
                (tableView as ViewGroup).addView(newRow)
            }
        } else {

            var titleRow: TableRow = createTableRow(tableData.keys.size - 1, true)
            var rowLayoutParams: TableRow.LayoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT)
            titleRow.layoutParams = rowLayoutParams

            var i = 1
            for ((colKey, _) in tableData) {
                if (colKey != "ID" && colKey != "Timestamp") {
                    var rowNum: TextView = titleRow.getChildAt(i) as TextView
                    rowNum.text = colKey.toString()
                    rowNum.setTypeface(null, Typeface.BOLD)
                    ++i
                } else if (colKey == "Timestamp") { //Date directly set to first column
                    var rowNum: TextView = titleRow.getChildAt(0) as TextView
                    rowNum.text = "Time"
                    rowNum.setTypeface(null, Typeface.BOLD)
                }
            }
            (tableView as ViewGroup).addView(titleRow)

            for (row in 0 until getNumRows()) {
                var newRow: TableRow = createTableRow(tableData.keys.size - 1)  // exclude ID
                var rowLayoutParams: TableRow.LayoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT)
                newRow.layoutParams = rowLayoutParams

                var colNum = 1
                for ((colKey, colData) in tableData) {
                    if (colKey != "ID" && colKey != "Timestamp") {
                        var rowNum: TextView = newRow.getChildAt(colNum) as TextView
                        rowNum.text = colData[row].toString()
                        rowNum.setTag(R.id.TAG_DB_ROW, row)  // 0: row, 1: column name
                        rowNum.setTag(R.id.TAG_DB_COL, colKey)
                        ++colNum
                    } else if (colKey == "Timestamp") {
                        var rowNum: TextView = newRow.getChildAt(0) as TextView
                        rowNum.text = DatabaseHelper(this.context!!).formatTime(tableNameDB, colData[row] as Long)
                        rowNum.setTag(R.id.TAG_DB_ROW, row)  // 0: row, 1: column name
                        rowNum.setTag(R.id.TAG_DB_COL, colKey)
                    }
                }
                (tableView as ViewGroup).addView(newRow)
            }
        }
        val placeholder: TableRow = createTableRow(tableData.keys.size - 2)
        val rowLayoutParams: TableRow.LayoutParams = TableRow.LayoutParams(
        TableRow.LayoutParams.MATCH_PARENT,
        TableRow.LayoutParams.WRAP_CONTENT)
        placeholder.layoutParams = rowLayoutParams
        (tableView as ViewGroup).addView(placeholder)
    }


    private fun createTableRow(numColumns: Int, firstRow: Boolean =false): TableRow {
        Log.d("welp num cols createrow", numColumns.toString())
        var row = TableRow(context)
        row.setPadding(5)
        for (i in 1..numColumns) {
            var item = TextView(context)
            item.setPadding(50, 10, 100, 10)
            item.textSize = 25F
            item.isFocusable = true
            if (!firstRow) {
                item.textSize = 20F
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
                        Log.d("welp row", row.toString())
                        Log.d("welp col", column.toString())
                        var cv: ContentValues = ContentValues()
                        cv.put(column, v.text.toString())
                        DatabaseHelper(this.context!!).updateRow(
                            this.tableNameDB,
                            cv,
                            "ID=$row",
                            null
//                            row,
//                            column,
//                            v.text.toString()
                        )
                        true
                    }
                    false
                }
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
        var data: ArrayList<Any> = arrayListOf()
        for ((colHeader, colData) in tableData) {
            if (colHeader != "ID") {
                if (colHeader != "Timestamp" || DatabaseHelper(this.context!!).getXAxisColumn(tableNameDB) == "Timestamp") {
                    data.add(colData[row])
                }
            }
        }
        return data.toList()
    }


    fun getNumRows(): Int {
        Log.d("welp ID col", tableData["ID"].toString())
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