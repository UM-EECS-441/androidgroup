package com.chatterplot

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.*

class DatasetConfigActivity : AppCompatActivity() {

    private lateinit var tableName:String
    lateinit var timeResSpinner: Spinner
    lateinit var timeResAdapter: ArrayAdapter<String>
    lateinit var resolutionHint: TextView

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

        val timeFormats = arrayListOf("Month", "Week", "Day", "Hour", "Minute")
        timeResSpinner = findViewById(R.id.timeResConfigSpinner)
        timeResAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, timeFormats)
        timeResSpinner.adapter = timeResAdapter
        timeResSpinner.setSelection(DatabaseHelper(this).getTimeResolution(tableName))

        resolutionHint = findViewById(R.id.resolutionHint)

        updateHint(resolutionHint)

        timeResSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //updateHint(resolutionHint)
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateHint(resolutionHint)
            }
        }
    }

    private fun saveConfigs() {
        DatabaseHelper(this).setTimeResolution(tableName, timeResSpinner.selectedItemPosition)

        val resultIntent= Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private val resNames = arrayOf("month", "week", "day", "hour", "minute")

    private fun updateHint(hint: TextView) {
        var hintString = "Aggregate entries by "
        hintString += resNames[timeResSpinner.selectedItemPosition]
        hintString += ", beginning " + DatabaseHelper(this).formattedStartTime(tableName)
        hint.text = hintString
    }

}

