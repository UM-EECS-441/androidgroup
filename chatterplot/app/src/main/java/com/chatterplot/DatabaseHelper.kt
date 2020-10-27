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
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class Schema(val name: String) {
    val columns = ArrayList<Pair<String, String>>()
    fun addColumn(name: String, type: String) {
        columns.add(Pair(name, type))
    }
}

class DatabaseHelper(val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        val sqlQuery = "CREATE TABLE DATASET (TableName TEXT NOT NULL PRIMARY KEY, Timestamp FLOAT)"
        db!!.execSQL(sqlQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS DATASET")
        onCreate(db)
    }

    private fun updateTimestamp(tableName: String) {
        val db = this.writableDatabase
        val sqlval = ContentValues()
        sqlval.put("Timestamp", System.currentTimeMillis() / 1000)
        db.update("DATASET", sqlval, "TableName='${tableName}'", null)
    }

    fun getColumnNames(tableName: String): Array<String> {
        val db = this.readableDatabase
        val cursor = db.query(tableName, null, null, null, null, null, null)
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
        for(i in 0 until values.size) {
            sqlval.put(colNames[i+1], values[i])
        }
        sqlval.put("Timestamp", System.currentTimeMillis()/1000)
        val db = this.writableDatabase
        db.insertOrThrow(tableName, null, sqlval)
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
        val cursor = db.query("DATASET", arrayOf("TableName"), null, null, null, null, null)
        val result = arrayListOf<String>()
        while(cursor.moveToNext()) {
            result.add(cursor.getString(0))
        }
        cursor.close()
        return result
    }

    fun getTable(tableName: String): MutableMap<String, ArrayList<Any>> {
        val db = this.writableDatabase
        val cursor = db.query(tableName, null, null, null, null, null, null)
        val colNames = cursor.columnNames
        val resultDict = mutableMapOf<String, ArrayList<Any>>()
        for(col in colNames) {
            resultDict[col] = ArrayList()
        }
        while(cursor.moveToNext()) {
            for (col in colNames) {
                if(col == "Timestamp") {
                    val timestamp = cursor.getFloat(cursor.getColumnIndex(col)).toLong()
//                    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
                    val cal = Instant.ofEpochSecond(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
                    resultDict[col]!!.add(cal)
                }
                else {
                    resultDict[col]!!.add(cursor.getString(cursor.getColumnIndex(col)))
                }
            }
        }
        cursor.close()
        return resultDict
    }

    fun createTable(schema: Schema) {
        val db = this.writableDatabase
        var sqlQuery = "CREATE TABLE [${schema.name}] (ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" + ","
        for (idx in 0 until schema.columns.size) {
            sqlQuery += "${schema.columns[idx].first} ${schema.columns[idx].second}, "
        }
        sqlQuery += "Timestamp FLOAT)"
        db.execSQL(sqlQuery)
        // Insert to the table that keeps track of all data sets
        db.execSQL("INSERT INTO DATASET (TableName, Timestamp) VALUES ('${schema.name}', ${System.currentTimeMillis() / 1000})")
    }

    fun deleteTable(tableName: String) {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM DATASET WHERE TableName='$tableName'")
        db.execSQL("DROP TABLE '$tableName'")
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
