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
// 0 - Unix Epoch Milliseconds (no conversion)
// 1 - MM/dd/YYYY
// 2 - MM/dd/YY HH:mm

class DatabaseHelper(val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        val sqlQuery = "CREATE TABLE DATASET (TableName TEXT NOT NULL PRIMARY KEY, Timestamp INTEGER, XAxisColumn TEXT, TimeFormat INTEGER)"
        db!!.execSQL(sqlQuery)
        Log.d("SQL", "Created DATASET table")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS DATASET")
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

//    TODO: Current sqlite version does not support renaming column
//    fun changeDatabaseColumn(tableName: String, newColumn: ArrayList<String>) {
//        val db = this.writableDatabase
//        val old = getColumnNames(tableName).joinToString(prefix="(", postfix=")") {
//            "[$it]"
//        }
//        val new = newColumn.joinToString(prefix="(", postfix=")") {
//            "[$it]"
//        }
//        val query = "ALTER TABLE [$tableName] RENAME COLUMN $old TO $new"
//        db.execSQL(query)
//    }

    fun getColumnNames(tableName: String): Array<String> {
        val db = this.readableDatabase
        val cursor = db.query("[$tableName]", null, null, null, null, null, null)
        val colNames = cursor.columnNames
        cursor.close()
        return colNames
    }

    fun insertRow(tableName:String, values:ArrayList<String>, byColumn: Boolean=false) {
        //TODO: implement by column insertion
        val sqlval = ContentValues()
        val colNames = getColumnNames(tableName)

        // ID and Timestamp does not need to be provided
        if(colNames.size - 2 != values.size) {
            throw Exception("Number of input does not match number of columns")
        }
//        sqlval.put("Timestamp", System.currentTimeMillis()/1000.0)
        val time = Instant.now().toEpochMilli()
//        Log.e("Time", time.toString())
        sqlval.put("Timestamp", time)

        for(i in 0 until values.size) {
            sqlval.put("[${colNames[i+2]}]", values[i])
        }
//        sqlval.put("Timestamp", System.currentTimeMillis()/1000.0)

        val db = this.writableDatabase
        db.insertOrThrow("[$tableName]", null, sqlval)
//        Log.e("sql", getTable(tableName).toString())
        updateTimestamp(tableName)
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

    fun editValue(tableName: String, row: Int, column: String, value: String) {
        val db = this.writableDatabase
        var newValues: ContentValues = ContentValues()
        newValues.put(column, value)
        newValues.put("Timestamp", Instant.now().toEpochMilli())
        db.update(tableName, newValues, "ID=$row", null)
        updateTimestamp(tableName)
    }

    fun deleteRow(tableName:String, where:String?, args:Array<String>?) {
        val db = this.writableDatabase
        db.delete(tableName, where, args)
    }

    fun updateRow(tableName:String, values:ContentValues, where:String?, args: Array<String>?) {
        this.writableDatabase.update(tableName, values, where, args)
    }

    fun getAllDatabaseNames(): ArrayList<String> {
        val db = this.writableDatabase
        val cursor = db.query("DATASET", arrayOf("TableName"), null, null, null, null, "Timestamp DESC")
        val result = arrayListOf<String>()
        while(cursor.moveToNext()) {
            result.add(cursor.getString(0))
        }
        cursor.close()
        return result
    }

    fun getXAxisColumn(datasetName: String): String {
        val db = this.writableDatabase
        val cursor = db.query("DATASET", arrayOf("XAxisColumn"), "TableName='$datasetName'", null, null, null, null, null)
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
        cv.put("XAxisColumn", newXAxis)
        db.update("DATASET", cv, "TableName='$datasetName'",null)
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
        if (timeCode == 1) {
            sdf = android.icu.text.SimpleDateFormat("MM/dd/YY")
        } else if (timeCode == 2) {
            sdf = android.icu.text.SimpleDateFormat("MM/dd/YY HH:mm")
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

    fun createDataset(tableName: String, columns: MutableList<String>) {
        val schema = Schema(tableName)
        for (column in columns) {
            schema.addColumn(column, "INT")
        }
        this.createTable(schema)
    }

    fun createTable(schema: Schema) {
        val db = this.writableDatabase
        var sqlQuery = "CREATE TABLE [${schema.name}] (ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" + ","
        sqlQuery += "Timestamp INTEGER"
        for (idx in 0 until schema.columns.size) {
            sqlQuery += ", [${schema.columns[idx].first}] ${schema.columns[idx].second}"
        }
        sqlQuery += ")"
        db.execSQL(sqlQuery)


        val time = Instant.now().toEpochMilli()
        var datasetXAxis = schema.xAxisColumnName
        // Insert to the table that keeps track of all data sets
        db.execSQL("INSERT INTO DATASET (TableName, Timestamp, XAxisColumn, TimeFormat) VALUES ('${schema.name}', ${time}, '${datasetXAxis}', 0)")
        Log.e("SQLInsert", "Inserted "+schema.name+" Into DATASET")
    }

    fun deleteTable(tableName: String) {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM DATASET WHERE TableName='$tableName'")
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
        private const val DATABASE_VERSION = 1
    }
}
