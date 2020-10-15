package com.chatterplot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val createDatasetButton = findViewById<Button>(R.id.createTableButton)
        createDatasetButton.setOnClickListener { view ->
            showCreateDialog(view)
        }
    }

    fun showCreateDialog(v: View) {
        val createDialog = layoutInflater.inflate(R.layout.create_table_view, null)
        val datasetName = createDialog.findViewById<EditText>(R.id.datasetName)
        val datasetIndependent = createDialog.findViewById<EditText>(R.id.datasetIndependent)
        val datasetDependent = createDialog.findViewById<EditText>(R.id.datasetDependent)

        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Create new dataset")
        alertDialog.setView(createDialog)
        alertDialog.setNegativeButton("Cancel", null)
        alertDialog.setPositiveButton("Create")  {dialog, which ->
            val tableName = datasetName.text.toString()
            val xAxis = datasetIndependent.text.toString()
            val yAxis = datasetDependent.text.toString()
            val schema = Schema(tableName)
            schema.addColumn(xAxis, "Int")
            schema.addColumn(yAxis, "Int")
            DatabaseHelper(this).createTable(schema)
        }
        alertDialog.create().show()
    }

    fun showAllDatabase(v:View?) {
        val intent = Intent(this, DatabaseActivity::class.java)
        startActivity(intent)
    }

    fun showGraph(v: View) {
        val intent = Intent(this, GraphActivity::class.java)
        startActivity(intent)
    }
}