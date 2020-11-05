package com.chatterplot

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.ConditionVariable
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_create_dataset.*
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class CreateDatasetActivity : AppCompatActivity() {
    private lateinit var columns: ArrayList<View>
    private lateinit var progressBar: ProgressBar
    private val READ_PERMISSION = 20
    private val IMPORT_REQUEST = 10
    private var inputName = "Imported"
//    private lateinit var inputColumnLabel: ArrayList<String>
    private lateinit var inputColumn: ArrayList<ArrayList<String>>
    // 0 = not importing, 1 = importing in process, 2 = importing done
    var isImporting = 0
    val importingProcess = ConditionVariable(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_dataset)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        columns = arrayListOf(findViewById<TextInputLayout>(R.id.dataset_column_1))
        progressBar = findViewById(R.id.import_progress_bar)
        val addButton = findViewById<Button>(R.id.add_column_button)
        addButton.setOnClickListener { v ->
            addColumn(v)
        }

        val createButton = findViewById<Button>(R.id.create_dataset)
        createButton.setOnClickListener { _ ->
            createDataset()
        }
    }

    private fun addView(inputField: View) {
        val inputs = findViewById<ConstraintLayout>(R.id.create_input_fields)
        val setter = ConstraintSet()
        inputs.addView(inputField)
        setter.clone(inputs)
        setter.connect(inputField.id, ConstraintSet.TOP, columns.last().id, ConstraintSet.BOTTOM, 0)
        setter.applyTo(inputs)
        columns.add(inputField)
    }

    private fun addColumn(view: View?) {
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

//        Snackbar.make(view, "Created a column", Snackbar.LENGTH_LONG)
//            .setAction("Undo") {
//                //Action
//            }
//            .show()
    }

    private fun constructInputField(isDeletable: Boolean=true): View {
        val inflater = LayoutInflater.from(this)
        val layout = inflater.inflate(R.layout.column_input_field, null, false)
        layout.id = View.generateViewId()
        val textInput = layout.findViewById<TextInputLayout>(R.id.column_input)
        textInput.hint = "Column ${columns.size+1}"
        val deleteBtn = layout.findViewById<ImageButton>(R.id.delete_button)
        if(!isDeletable) {
            deleteBtn.visibility = View.INVISIBLE
        }
        else {
            deleteBtn.tag = columns.size
            deleteBtn.setOnClickListener { v ->
                deleteInputField(v.tag as Int)
            }
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
            setter.connect(
                columns[tag + 1].id,
                ConstraintSet.TOP,
                columns[tag - 1].id,
                ConstraintSet.BOTTOM,
                0
            )
            setter.applyTo(parent)
        }
        columns.removeAt(tag)
        for(i in tag until columns.size) {
            val textInput = columns[i].findViewById<TextInputLayout>(R.id.column_input)
            textInput.hint = "Column ${i+1}"
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
        DatabaseHelper(this).createTable(schema)
//        else {
//            inputColumn = ArrayList<String>()
//            for(column in columns) {
//                val colName = column.findViewById<TextInputLayout>(R.id.column_input).editText?.text.toString()
//                if(colName == "") {
//                    Toast.makeText(this, "Names of columns must be specified", Toast.LENGTH_LONG).show()
//                    return
//                }
//                inputColumn.add(colName)
//            }
//            if(name != "Imported") {
//                inputName = name
//                if(isImporting == 2) {
//                    db.changeDatabaseName("Imported", name)
//                }
//            }
//            db.changeDatabaseColumn("Imported", colNames)
//        }
        val resultIntent = Intent()
        resultIntent.putExtra("NAME", name)
        setResult(Activity.RESULT_OK, resultIntent)
        when(isImporting) {
            0 -> {
                finish()
            }
            else -> {
                progressBar.visibility = View.GONE
                findViewById<Button>(R.id.create_dataset).visibility = View.GONE
                findViewById<CoordinatorLayout>(R.id.main_progress_bar).visibility = View.VISIBLE
                Thread(Runnable {
                    importingProcess.block()
                    val db = DatabaseHelper(this)
                    db.insertRows(name, inputColumn)
                    finish()
                }).start()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.import_dataset -> {
                if (ContextCompat.checkSelfPermission(
                        this@CreateDatasetActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@CreateDatasetActivity,
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ), READ_PERMISSION
                    )
                }

                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = MimeTypeMap.getSingleton().getMimeTypeFromExtension("csv")
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_TEXT, "Choose a file to import")
                }
                startActivityForResult(Intent.createChooser(intent, null), IMPORT_REQUEST)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.support_action_bar_create, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == READ_PERMISSION) {
            if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Read access denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == IMPORT_REQUEST) {
            if(resultCode == RESULT_OK && data != null) {
                val selectedFile = data.data
                isImporting = 1
                progressBar.visibility = View.VISIBLE
                add_column_button.visibility = View.GONE
                Thread(Runnable {
                    importingProcess.close()
                    parseCSV(selectedFile)
                    runOnUiThread { progressBar.visibility = View.GONE }
                    importingProcess.open()
                }).start()

            }
        }
    }

//    private fun parseCSV(path: String?) {
//        if(path == null) return
//        val file = File(path)
//        Log.e("File", file.toString())
//        if(file.exists()) {
//            val inputStream = FileInputStream(file)
//            val reader = BufferedReader(InputStreamReader(inputStream))
//            var line = reader.readLine()
//            while(line != null) {
//                Log.e("Line", line)
//                line = reader.readLine()
//            }
//        }
//    }

    private fun parseCSV(uri: Uri?) {
        if(uri == null) return
//        val docId = DocumentsContract.getDocumentId(uri)
//        Log.e("docId", docId)
        val file = contentResolver.openInputStream(uri)!!
        Log.e("uri", uri.toString())
        val reader = BufferedReader(InputStreamReader(file))

        val tableName = "Imported"
        val _columns = reader.readLine().split(",")
        if(_columns.size >= 5) {
            Toast.makeText(this, "Dataset cannot have more than 5 columns", Toast.LENGTH_LONG).show()
        }
        val schema = Schema(tableName)
        for((idx,column) in _columns.withIndex()) {
            if(idx == 0) {
                runOnUiThread {
                    findViewById<TextInputLayout>(R.id.column_input).editText?.setText(column)
                }
            }
            else if(idx < columns.size) {
                runOnUiThread {
                    columns[idx].findViewById<TextInputLayout>(R.id.column_input).editText?.setText(column)
                }
            }
            else {
                runOnUiThread {
                    val input = constructInputField(false)
                    input.findViewById<TextInputLayout>(R.id.column_input).editText?.setText(column)
                    addView(input)
                }
            }
            schema.addColumn(column, "INT")
        }
        for(idx in _columns.size until columns.size) {
            runOnUiThread { deleteInputField(idx) }
        }
        //TODO: Couldn't rename column name in current version of sqlite
//        val db = DatabaseHelper(this)
//        db.createTable(schema)
//
//        var line = reader.readLine()
//        while(line != null) {
//            db.insertRow(tableName, ArrayList(line.split(",")))
//            line = reader.readLine()
//        }
        inputColumn = ArrayList()
        var line = reader.readLine()
        while(line != null) {
            inputColumn.add(ArrayList(line.split(",")))
            line = reader.readLine()
        }
        isImporting = 2

//        val resultIntent = Intent()
//        resultIntent.putExtra("NAME", "Imported")
//        setResult(Activity.RESULT_OK, resultIntent)
//        finish()


//        when(uri.authority) {
//            // External Storage
//            "com.android.externalstorage.documents" -> {
//                val file = contentResolver.openInputStream(uri)
//                val reader = BufferedReader(InputStreamReader(file))
//                var line = reader.readLine()
//                while(line != null) {
//                    Log.e("line", line.toString())
//                    line = reader.readLine()
//                }
                // TODO Implement
//                val selectionMime = MediaStore.Files.FileColumns.MIME_TYPE + "=?"
//                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("jpg")
//                contentResolver.query(
//                    MediaStore.Files.getContentUri("external"),
//                    null,
//                    selectionMime,
//                    arrayOf(mimeType),
//                    null
//                )?.use { cursor ->
//                    while (cursor.moveToNext()) {
//                        val temp = cursor.getString(cursor.getColumnIndexOrThrow("_data"))
//                        Log.e("Downloads", temp)
//                    }
//                }
////                val path = docId.split(":")
////                if("primary".equals(path[0], true)) {
////                    val down = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
////                    return "${getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}/${path[1]}"
////                }
//                return getDataColumn(uri, null, null)
//            }
            // Download
//            "com.android.providers.downloads.documents" -> {
                // TODO Implement
//                val contentUri = ContentUris.withAppendedId(
//                    Uri.parse("content://downloads/public_downloads"),
//                    docId.toLong()
//                )
//                return getDataColumn(contentUri, null, null)
//            }
            // Google Drive
//            "com.google.android.apps.docs.storage" -> {
//                val cursor = contentResolver?.query(uri, null, null, null, null, null)
//                if (cursor != null && cursor.moveToFirst()) {
//                    val displayName = cursor.getString(
//                        cursor.getColumnIndex("_data")
//                    )
//                    Log.e("display", displayName)
//                }
//                val file = contentResolver.openInputStream(uri)!!
//                val reader = BufferedReader(InputStreamReader(file))
//
//                val tableName = "Imported"
//                val columns = reader.readLine().split(",")
//                val schema = Schema(tableName)
//                for(column in columns) {
//                    schema.addColumn(column, "INT")
//                }
//                val db = DatabaseHelper(this)
//                db.createTable(schema)
//
//                var line = reader.readLine()
//                while(line != null) {
//                    Log.e("Line", line)
//                    db.insertRow(tableName, ArrayList(line.split(",")))
//                    line = reader.readLine()
//                }
//            }
//        }
    }

//    private fun getDataColumn(uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {
//        contentResolver?.query(uri, arrayOf("_data"), selection, selectionArgs, null)?.use {
//            if(it.moveToFirst()) {
//                return it.getString(it.getColumnIndexOrThrow("_data"))
//            }
//        }
//        return null
//        var cursor: Cursor? = null
//        var column = "_data"
//        val projection = arrayOf(column)
//        try {
//            if(uri == null) return null
//            cursor = context.contentResolver.query(uri, projection, selection , selectionArgs, null)
//            if(cursor != null && cursor.moveToFirst()) {
//                val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
//                return cursor.getString(index)
//            }
//        } finally {
//            cursor?.close()
//        }
//        return null
//    }
}