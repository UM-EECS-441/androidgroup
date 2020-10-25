package com.chatterplot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

class DatabaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_database)
        getListOfTables(findViewById<TextView>(R.id.dbList))
    }

    fun getListOfTables(text: TextView) {
        val dbList = DatabaseHelper(this).getAllDatabase()
        text.text = dbList.joinToString("\n")
    }
}