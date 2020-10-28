package com.chatterplot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

        val parent = findViewById<ConstraintLayout>(R.id.insert_data)
        val setter = ConstraintSet()
        for(idx in 1 until columnNames.size-1) {
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
            insertFunction(ArrayList(columns.map{
                it.findViewById<TextInputLayout>(R.id.column_input).editText?.text.toString()
            }))
        }
    }

    private fun insertFunction(insertValues : ArrayList<String>) {
        DatabaseHelper(this).insertRow(datasetName, insertValues)
        val t = Toast.makeText(this, "Data point added!", Toast.LENGTH_LONG)
        t.show()
        val intent = Intent(this, MainActivity::class.java)
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