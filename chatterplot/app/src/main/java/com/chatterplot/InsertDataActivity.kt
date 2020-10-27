package com.chatterplot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class InsertDataActivity : AppCompatActivity() {


    lateinit var xVal : EditText
    lateinit var yVal : EditText
    lateinit var confirmButton: Button

    private lateinit var datasetName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_data)

        datasetName = intent.getStringExtra("DATASETNAME")

        val titleText : TextView = findViewById<TextView>(R.id.InsertDatasetName)
        val preString : String = "Insert Into "
        titleText.text = preString + datasetName

        xVal = findViewById(R.id.xInput)
        yVal = findViewById(R.id.yInput)
        confirmButton = findViewById(R.id.confirmInsertButton)




        confirmButton.setOnClickListener {
//            val insertValues = ArrayList<String>()
//            insertValues.add(xVal.text.toString())
//            insertValues.add(yVal.text.toString())
            insertFunction(arrayListOf(xVal.text.toString(), yVal.text.toString()))
        }
    }

    private fun insertFunction(insertValues : ArrayList<String>) {
        DatabaseHelper(this).insertRow(datasetName, insertValues)
    }
}