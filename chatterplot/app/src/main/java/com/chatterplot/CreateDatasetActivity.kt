package com.chatterplot

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import kotlinx.android.synthetic.main.activity_create_dataset.*

class CreateDatasetActivity : AppCompatActivity() {
    private lateinit var columns: ArrayList<View>
    lateinit var graphedBy: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_dataset)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        columns = arrayListOf(findViewById<TextInputLayout>(R.id.dataset_column_1))

        graphedBy = findViewById(R.id.xAxisRadioGroup)

        val addButton = findViewById<Button>(R.id.add_column_button)
        addButton.setOnClickListener { v ->
            addColumn(v)
        }

        val createButton = findViewById<Button>(R.id.create_dataset)
        createButton.setOnClickListener { _ ->
            createDataset()
        }
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

        graphedBy.visibility = View.VISIBLE

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
        textInput.hint = "Column ${columns.size+1} Name"
        val deleteBtn = layout.findViewById<ImageButton>(R.id.delete_button)
        deleteBtn.tag = columns.size
        deleteBtn.setOnClickListener { v ->
            deleteInputField(v.tag as Int)
        }
        return layout
    }

    private fun deleteInputField(tag: Int) {
        if(columns.size == 1 || tag >= columns.size) return
        if(columns.size == 2) {
            graphedBy.visibility = View.INVISIBLE
            graphedBy.check(R.id.dateAsX)
        }
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
            textInput.hint = "Column ${i+1} Name"
            val deleteBtn = columns[i].findViewById<ImageButton>(R.id.delete_button)
            deleteBtn.tag = i
        }
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

        //Check if graphing by column is selected and set schema value accordingly.
        //Graphing by date is true by default, so it is not checked
        val selectedID = graphedBy.checkedRadioButtonId
        if (selectedID == R.id.col1AsX) schema.isGraphedByDate(false)

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