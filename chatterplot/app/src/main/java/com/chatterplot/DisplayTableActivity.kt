package com.chatterplot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class DisplayTableActivity : AppCompatActivity() {

    private lateinit var tableName:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_table)

        tableName = intent.getStringExtra("DATASETNAME") ?: ""
        supportActionBar?.title = tableName

        val textBox = findViewById<TextView>(R.id.displayTableTemp)
        textBox.text = DatabaseHelper(this).isolateDataset(tableName).toString()
    }
}