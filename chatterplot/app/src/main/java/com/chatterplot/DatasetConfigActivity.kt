package com.chatterplot

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import java.util.ArrayList

class DatasetConfigActivity : AppCompatActivity() {

    private lateinit var tableName:String
    lateinit var xSpinner: Spinner
    
    lateinit var xAdapter: ArrayAdapter<String>
    lateinit var timeSpinner: Spinner
    lateinit var timeAdapter: ArrayAdapter<String>
    private lateinit var columnNames: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dataset_config)

        val saveButton = findViewById<Button>(R.id.configSaveButton)
        saveButton.setOnClickListener { _ ->
            saveConfigs()
        }

        tableName = intent.getStringExtra("DATASETNAME")
        val titleText : TextView = findViewById(R.id.configTitle)
        titleText.text = "Settings for \"$tableName\""

        xSpinner = findViewById(R.id.xAxisConfigSpinner)
        columnNames = DatabaseHelper(this).getColumnNames(tableName).drop(1) // don't fetch ID column
        xAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, columnNames)
        xSpinner.adapter = xAdapter

        val timeFormats = arrayListOf("Date & Time", "Date & Year", "Unix Time")
        timeSpinner = findViewById(R.id.timeFormatConfigSpinner)
        timeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, timeFormats)
        timeSpinner.adapter = timeAdapter

        // Set spinners to display currently selected settings
        //val spinPos = xColumnSpinnerPosition()
        xSpinner.setSelection(xColumnSpinnerPosition())

        timeSpinner.setSelection(DatabaseHelper(this).getTimeFormat(tableName))

        // Disable Spinner for single-column datasets, as they will always graph by time
        if (columnNames.size < 3) {
            disableSpinner(xSpinner)
        }



    }

    private fun saveConfigs() {

        DatabaseHelper(this).setXAxisColumn(tableName, xSpinner.selectedItem.toString())

        DatabaseHelper(this).setTimeFormat(tableName, timeSpinner.selectedItemPosition)

        val resultIntent= Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun xColumnSpinnerPosition(): Int {
        val currentXCol = DatabaseHelper(this).getXAxisColumn(tableName)
        return columnNames.indexOf(currentXCol)
    }

    private fun enableSpinner(spin: Spinner) {
        spin.isEnabled = true
        spin.alpha = 1f
    }

    private fun disableSpinner(spin: Spinner) {
        spin.isEnabled = false
        spin.alpha = 0.75f
        //spin.setSelection(0)
    }
}