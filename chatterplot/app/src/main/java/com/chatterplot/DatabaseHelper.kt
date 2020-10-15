package com.chatterplot

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast

class Schema(val name: String) {
    val columns = ArrayList<Pair<String, String>>()
    fun addColumn(name: String, type: String) {
        columns.add(Pair(name, type))
    }
}

class DatabaseHelper(val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        val sqlQuery = "CREATE TABLE DATASET (TableName TEXT NOT NULL PRIMARY KEY)"
        db!!.execSQL(sqlQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS DATASET")
        onCreate(db)
    }

    fun getDB(): SQLiteDatabase {
        return this.writableDatabase
    }

    fun createTable(schema: Schema) {
        val db = this.writableDatabase



        try {
            db.execSQL("INSERT INTO DATASET (TableName) VALUES ('${schema.name}')")
            var sqlQuery = "CREATE TABLE ${schema.name} (" +
                    "ID INTEGER PRIMARY KEY,"
            for (idx in 0 until schema.columns.size) {
                sqlQuery += "${schema.columns[idx].first} ${schema.columns[idx].second}"
                if (idx != schema.columns.size - 1) {
                    sqlQuery += ","
                }
            }
            sqlQuery += ")"
            db.execSQL(sqlQuery)
        }
        catch(e: android.database.sqlite.SQLiteConstraintException) {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            Log.e("Error", e.message)
        }

    }

    fun deleteTable(tableName: String) {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM DATASET WHERE TableName='$tableName'")
    }

    fun getAllDatabase(): ArrayList<String> {
        val db: SQLiteDatabase = this.writableDatabase
//        val tables = db.query("SELECT * FROM DATASET", null)
        val result = arrayListOf<String>()
        val tables = db.query("DATASET", arrayOf("TableName"), null, null, null, null, null)
        while(tables.moveToNext()) {
            val name = tables.getString(0)
            val columns = db.query(name, null, null, null, null, null, null)
            var info = name
            for (item in columns.columnNames) {
                info += ", $item"
            }
            result.add(info)
        }
        return result
//        return db.rawQuery("SELECT TableName FROM DATASET", null)
//        return db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name!='android_metadata' order by name", null)
    }

    companion object {
        private const val DATABASE_NAME = "Chatterplot.db"
        private const val DATABASE_VERSION = 1
    }
}
