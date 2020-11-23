package com.chatterplot

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CreateDatasetActivity : AppCompatActivity() {
    private lateinit var columns: ArrayList<View>

    lateinit var spin: Spinner
    lateinit var adapter: ArrayAdapter<String>
    lateinit var refreshButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_dataset)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        columns = arrayListOf(findViewById<TextInputLayout>(R.id.dataset_column_1))

        spin = findViewById(R.id.xAxisSpinner)
        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayListOf("Timestamp"))
        refreshButton = findViewById<ImageButton>(R.id.spinnerRefreshButton)
        spin.adapter = adapter

        disableSpinner()

        refreshButton.setOnClickListener {
            refreshButton.animate().setDuration(250).rotationBy(360f).start()
            updateSpinner()
        }

        val addButton = findViewById<Button>(R.id.add_column_button)
        addButton.setOnClickListener { v ->
            addColumn(v)
        }

        val createButton = findViewById<Button>(R.id.create_dataset)
        createButton.setOnClickListener { _ ->
            createDataset()
        }
    }

    // Updates the X-Axis selection spinner to include all named columns as long as
    // there are more than 1 columns
    private fun updateSpinner() {
        adapter.clear()
        adapter.add("Timestamp")

        for (col in columns) {
            val colname = col.findViewById<TextInputLayout>(R.id.column_input).editText?.text.toString()
            if (colname != "") {
                adapter.add(colname)
            }
        }
        if (columns.size > 1) {
            enableSpinner()
        } else {
            disableSpinner()
        }
    }

    private fun enableSpinner() {
        spin.isEnabled = true
        spin.alpha = 1f
        refreshButton.isEnabled = true
    }

    private fun disableSpinner() {
        spin.isEnabled = false
        spin.alpha = 0.75f
        spin.setSelection(0)
        refreshButton.isEnabled = false
    }

    private fun addColumn(view : View?) {
        if(view == null) return
        if(columns.size >= 5) {
            Snackbar.make(view, "Cannot create more than 5 columns", Snackbar.LENGTH_LONG).show()
            return
        }
        val layout = constructInputField() ?: return

        val inputs = findViewById<ConstraintLayout>(R.id.create_input_fields)
        val setter = ConstraintSet()
        inputs.addView(layout)
        setter.clone(inputs)
        setter.connect(layout.id, ConstraintSet.TOP, columns.last().id, ConstraintSet.BOTTOM, 0)
        setter.applyTo(inputs)

        columns.add(layout)

        updateSpinner()
        Log.v("Spinner", "Updated")

//        Snackbar.make(view, "Created a column", Snackbar.LENGTH_LONG)
//            .setAction("Undo") {
//                //Action
//            }
//            .show()
    }

    private fun constructInputField(): View? {
        val inflater = LayoutInflater.from(this)
        val layout = inflater.inflate(R.layout.column_input_field, null, false)
        layout.id = View.generateViewId()
        val textInput = layout.findViewById<TextInputLayout>(R.id.column_input)
        textInput.hint = "Column ${columns.size+1}"
        val deleteBtn = layout.findViewById<ImageButton>(R.id.delete_button)
        deleteBtn.tag = columns.size
        deleteBtn.setOnClickListener { v ->
            deleteInputField(v.tag as Int)
        }
        return layout
    }

    private fun deleteInputField(tag: Int) {
        if(columns.size == 1 || tag >= columns.size) return
        val parent = findViewById<ConstraintLayout>(R.id.create_input_fields)
        parent.removeView(columns[tag])
        if(tag != columns.size - 1) {
            val setter = ConstraintSet()
            setter.clone(parent)
            setter.connect(columns[tag+1].id, ConstraintSet.TOP, columns[tag-1].id, ConstraintSet.BOTTOM, 0)
            setter.applyTo(parent)
        }
        columns.removeAt(tag)
        for(i in tag until columns.size) {
            val textInput = columns[i].findViewById<TextInputLayout>(R.id.column_input)
            textInput.hint = "Column ${i+1}"
            val deleteBtn = columns[i].findViewById<ImageButton>(R.id.delete_button)
            deleteBtn.tag = i
        }

        updateSpinner()
        Log.e("Spinner", "Updated")
    }

    private fun createDataset() {
        val name = findViewById<TextInputLayout>(R.id.dataset_name).editText?.text.toString()
        if(name == "") {
            Toast.makeText(this, "Name of the dataset must be specified", Toast.LENGTH_LONG).show()
            return
        }
        val schema = Schema(name)
        for(column in columns) {
            val colName = column.findViewById<TextInputLayout>(R.id.column_input).editText?.text.toString()
            if(colName == "") {
                Toast.makeText(this, "Names of columns must be specified", Toast.LENGTH_LONG).show()
                return
            }
            schema.addColumn(colName, "INT")
        }


        schema.setXAxisColumn(spin.selectedItem.toString())
        Log.v("Spin", "Selected "+spin.selectedItem.toString())

        DatabaseHelper(this).createTable(schema)
        val resultIntent = Intent()
        resultIntent.putExtra("NAME", name)
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