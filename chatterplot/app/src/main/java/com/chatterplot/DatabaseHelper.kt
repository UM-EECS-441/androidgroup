package com.chatterplot

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.view.View
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class Schema(val name: String) {
    val columns = ArrayList<Pair<String, String>>()
    var xAxisColumnName = "Timestamp"
    fun addColumn(name: String, type: String) {
        columns.add(Pair(name, type))
    }
    fun setXAxisColumn(checker: String) {
        xAxisColumnName = checker
    }
}

// TimeFormat Codes using SimpleDateFormat:
// 0 - MM/dd/YY HH:mm
// 1 - MM/dd/YYYY
// 2 - Unix Epoch Milliseconds (no conversion)

class DatabaseHelper(val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {

        val masterInit = "CREATE TABLE MASTER (TableName TEXT, ColumnName TEXT, Data INTEGER, Timestamp INTEGER)"
        db!!.execSQL(masterInit)
        val directoryInit = "CREATE TABLE DIRECTORY (TableName TEXT, isCategorical INTEGER, XAxis TEXT, TimeFormat INTEGER)"
        db!!.execSQL(directoryInit)

        Log.d("SQL", "Created MASTER and DIRECTORY table")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS MASTER")
        db!!.execSQL("DROP TABLE IF EXISTS DIRECTORY")
        onCreate(db)
    }

    private fun updateTimestamp(tableName: String) {
        val db = this.writableDatabase
        val sqlval = ContentValues()
//        sqlval.put("Timestamp", System.currentTimeMillis() / 1000.0)
        sqlval.put("Timestamp", Instant.now().toEpochMilli())
        db.update("DATASET", sqlval, "TableName='${tableName}'", null)
    }

    fun changeDatabaseName(oldName: String, newName: String) {
        val db = this.writableDatabase
        val args = ContentValues()
        args.put("TableName", newName)
        val query = "ALTER TABLE [$oldName] RENAME TO [$newName]"

        db.beginTransaction()
        db.update("DATASET", args, "TableName=[$oldName]", null)
        db.execSQL(query)
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    private fun <T> Cursor.toArrayList(block: (Cursor) -> T) : ArrayList<T> {
        return arrayListOf<T>().also { list ->
            if (moveToFirst()) {
                do {
                    list.add(block.invoke(this))
                } while (moveToNext())
            }
        }
    }

    fun getColumnNames(tableName: String): ArrayList<String> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT ColName FROM $tableName", null)
        val colNames = cursor.toArrayList{ cursor.getString(0) }
        cursor.close()
        return colNames
    }

    // insertData: adds data input to database
    //
    // IN: datasetname - String - The name of the target dataset
    //     dataList - ArrayList of <String,String> Pairs - (Column, Data) for each input
    //
    // OUT: logs successful insertions in debug
    fun insertData(datasetName: String, dataList: ArrayList<Pair<String, String>>) {
        val time = Instant.now().toEpochMilli()
        val sqlRow = ContentValues()
        val db = this.writableDatabase
        dataList.forEach {
            sqlRow.put("TableName", datasetName)
            sqlRow.put("ColumnName", it.first)
            sqlRow.put("Data", it.second)
            sqlRow.put("Timestamp", time)
            db.insertOrThrow("MASTER", null, sqlRow)
        }
        Log.d("SQLInsert", "Added ${dataList.size} data points to MASTER")
    }

    //Just to this compiles until I fix speech processing
    fun insertRow(string: String, al: ArrayList<String>) {
        Log.d("Uhh", "This is gone")
    }

    fun insertRows(tableName: String, values: ArrayList<ArrayList<String>>) {
        val colNames = getColumnNames(tableName)
        if(colNames.size - 2 != values[0].size) {
            throw Exception("Number of input does not match number of columns")
        }
        val time = Instant.now().toEpochMilli()
        val db = this.writableDatabase
        db.beginTransaction()
        for(row in values){
            val sqlval = ContentValues()
            for(i in 0 until row.size) {
                sqlval.put("[${colNames[i+2]}]", row[i])
            }
            sqlval.put("Timestamp", time)
            db.insertOrThrow("[$tableName]", null, sqlval)
        }
        db.setTransactionSuccessful()
        db.endTransaction()
    }

//    fun editValue(tableName: String, row: Int, column: String, value: String) {
//        val db = this.writableDatabase
//        var newValues: ContentValues = ContentValues()
//        newValues.put(column, value)
//        Log.d("welp content vals", newValues.toString())
//        //newValues.put("Timestamp", Instant.now().toEpochMilli())
//        db.update(tableName, newValues, "ID=$row", null)
//        updateTimestamp(tableName)
//    }

    fun deleteRow(tableName:String, where:String?, args:Array<String>?) {
        val db = this.writableDatabase
        db.delete(tableName, where, args)
    }

    fun updateRow(tableName:String, values:ContentValues, where:String?, args: Array<String>?) {
        this.writableDatabase.update("\'" + tableName + "\'", values, where, args)
        updateTimestamp(tableName)
    }

    fun getAllDatabaseNames(): ArrayList<String> {
        val db = this.writableDatabase
        val cursor = db.query("DIRECTORY", arrayOf("TableName"), null, null, null, null, null)
        val result = arrayListOf<String>()
        while(cursor.moveToNext()) {
            result.add(cursor.getString(0))
        }
        cursor.close()
        return result
    }

    fun getXAxisColumn(datasetName: String): String {
        val db = this.writableDatabase
        val cursor = db.query("DIRECTORY", arrayOf("xAxis"), "TableName='$datasetName'", null, null, null, null, null)
        var result = "Timestamp"
        while (cursor.moveToNext()) {
            result = cursor.getString(0)
        }
        cursor.close()
        return result
    }

    fun setXAxisColumn(datasetName: String, newXAxis: String) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("xAxis", newXAxis)
        db.update("DIRECTORY", cv, "TableName='$datasetName'",null)
    }


    fun setTimeFormat(datasetName: String, timeCode: Int) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("TimeFormat", timeCode)
        db.update("DATASET", cv, "TableName='$datasetName'",null)
    }

    fun getTimeFormat(datasetName: String): Int {
        val db = this.writableDatabase
        val cursor = db.query("DATASET", arrayOf("TimeFormat"), "TableName='$datasetName'", null, null, null, null, null)
        var result = 0
        while (cursor.moveToNext()) {
            result = cursor.getInt(0)
        }
        cursor.close()
        return result
    }

    fun formatTime(datasetName: String, time: Long): String {
        val timeCode = getTimeFormat(datasetName)
        val sdf: android.icu.text.SimpleDateFormat
        if (timeCode == 0) {
            sdf = android.icu.text.SimpleDateFormat("MM/dd HH:mm")
        } else if (timeCode == 1) {
            sdf = android.icu.text.SimpleDateFormat("MM/dd/YY")
        } else {
            return time.toString()
        }
        val netDate = Date(time)
        return sdf.format(netDate)
    }

    fun getTable(tableName: String): MutableMap<String, ArrayList<Any>> {
        val db = this.writableDatabase
        val cursor = db.query("[$tableName]", null, null, null, null, null, null)
        val colNames = cursor.columnNames
        val resultDict = mutableMapOf<String, ArrayList<Any>>()
        for(col in colNames) {
            resultDict[col] = ArrayList()
        }
        while(cursor.moveToNext()) {
            for (col in colNames) {
                if(col == "Timestamp") {
                    val timestamp = cursor.getLong(cursor.getColumnIndex(col))
//                    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
//                    val cal = Instant.ofEpochSecond(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
                    resultDict[col]!!.add(timestamp)
                }
                else {
                    resultDict[col]!!.add(cursor.getString(cursor.getColumnIndex(col)))
                }
            }
        }
        cursor.close()
        return resultDict
    }

    // createNewDataset: creates a new dataset in the database
    //
    // IN: tableName - String - The name of the new dataset
    //     columns - List of Strings - list of names of desired columns/categories
    //     categorical - Boolean - True if dataset contains categorical/tally data,
    //                             False for Time series or x,y plots
    //
    // OUT: logs successful creations in debug
    fun createNewDataset(tableName: String, columns: ArrayList<String>, categorical: Int) {
        val db = this.writableDatabase
        var sqlQuery = "CREATE TABLE $tableName (ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ColName TEXT NOT NULL)"
        db.execSQL(sqlQuery)
        var colQuery = "INSERT INTO $tableName (ColName) VALUES "
        for (col in columns) {
            colQuery += "(\"$col\"),"
        }
        colQuery = colQuery.dropLast(1)
        db.execSQL(colQuery)

        db.execSQL("INSERT INTO DIRECTORY (TableName, isCategorical, xAxis, TimeFormat) VALUES ('$tableName', $categorical, 'Timestamp', 0)")
        Log.d("SQL", "Created new dataset $tableName")
    }

    fun deleteTable(tableName: String) {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM MASTER WHERE TableName='$tableName'")
        db.execSQL("DELETE FROM DIRECTORY WHERE TableName='$tableName'")
        db.execSQL("DROP TABLE '$tableName'")

        // Delete the preview file
        File(context.filesDir, tableName).delete()
    }

    fun getAllDatabase(): ArrayList<String> {
        val db: SQLiteDatabase = this.writableDatabase
        val result = arrayListOf<String>()
        val tables = db.query("DATASET", arrayOf("TableName"), null, null, null, null, null)
        while(tables.moveToNext()) {
            val name = tables.getString(0)
            val info = getTable(name).toString()
//            val columns = db.query(name, null, null, null, null, null, null)
//            var info = name
//            for (item in columns.columnNames) {
//                info += ", $item"
//            }
            result.add(info)
//            columns.close()
        }
        tables.close()
        return result
    }

    companion object {
        private const val DATABASE_NAME = "Chatterplot.db"
        private const val DATABASE_VERSION = 2
    }
}
