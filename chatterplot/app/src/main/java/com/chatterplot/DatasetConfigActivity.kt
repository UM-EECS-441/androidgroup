package com.chatterplot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class DatasetConfigActivity : AppCompatActivity() {

    private lateinit var tableName:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dataset_config)

        tableName = intent.getStringExtra("DATASETNAME")
        val titleText : TextView = findViewById(R.id.configTitle)
        titleText.text = "Settings for \"$tableName\""
    }
}