package com.chatterplot

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.material.textfield.TextInputLayout

class InsertDataActivity : AppCompatActivity() {
    lateinit var confirmButton: Button
    private var columns: ArrayList<View> = ArrayList<View>()
    private lateinit var datasetName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_data)

        datasetName = intent.getStringExtra("DATASETNAME")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = datasetName

        val titleText : TextView = findViewById<TextView>(R.id.InsertDatasetName)
        val preString : String = "Insert Into "
        titleText.text = preString + datasetName

        val columnNames = DatabaseHelper(this).getColumnNames(datasetName)
        Log.e("columns", columnNames.toString())

        val parent = findViewById<ConstraintLayout>(R.id.insert_data)
        val setter = ConstraintSet()
        for(idx in 0 until columnNames.size) {
            val name = columnNames[idx]
            val inflater = LayoutInflater.from(this)
            val input = inflater.inflate(R.layout.column_insert_field, null, false)
            input.findViewById<TextInputLayout>(R.id.column_input).hint = name
            input.id = View.generateViewId()
            parent.addView(input)
            setter.clone(parent)
            if(columns.size == 0) {
                setter.connect(input.id, ConstraintSet.TOP, titleText.id, ConstraintSet.BOTTOM)
            }
            else {
                setter.connect(input.id, ConstraintSet.TOP, columns.last().id, ConstraintSet.BOTTOM)
            }
            setter.applyTo(parent)
            columns.add(input)
        }


        confirmButton = findViewById(R.id.confirmInsertButton)

        confirmButton.setOnClickListener {
            val inputPairs = ArrayList<Pair<String, String>>()
            columns.forEach{
                var pair = Pair<String, String>(it.findViewById<TextInputLayout>(R.id.column_input).hint.toString(), it.findViewById<TextInputLayout>(R.id.column_input).editText?.text.toString())
                if (pair.second != "") inputPairs.add(pair)
            }
            insertFunction(inputPairs)
        }
    }

    private fun insertFunction(insertValues : ArrayList<Pair<String, String>>) {
        DatabaseHelper(this).insertData(datasetName, insertValues)
        val t = Toast.makeText(this, "Data added!", Toast.LENGTH_LONG)
        t.show()
        //createPreview(this, datasetName) BROKEN UNTIL GRAPHS ARE FIXED
        val resultIntent= Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home-> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}