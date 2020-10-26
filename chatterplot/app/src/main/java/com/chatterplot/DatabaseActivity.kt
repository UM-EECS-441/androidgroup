package com.chatterplot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast

class DatabaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_database)
        getListOfTables(findViewById<TextView>(R.id.dbList))
    }

    fun getListOfTables(text: TextView) {
//        val dbList = DatabaseHelper(this).getAllDatabase()
        val db = DatabaseHelper(this)
        db.insertRow("test5", arrayListOf("hello", "world"))
        val info = db.getTable("test5")
//        text.text = dbList.joinToString("\n")
        text.text = info.toString()
    }
}