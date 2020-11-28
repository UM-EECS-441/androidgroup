package com.chatterplot

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.sql.Timestamp
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStream
import java.io.OutputStreamWriter


class DisplayDataActivity : AppCompatActivity() {
    lateinit var adapter: TableAdapter
    private val INSERT_REQUEST_CODE = 10
    private val RECOGNIZER_REQUEST_CODE = 20
    private val EXPORT_LOC_REQUEST_CODE = 30
    private lateinit var tableName:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_data)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tableName = intent.getStringExtra("DATASETNAME") ?: ""
        //create tableAdapter
        adapter = TableAdapter(this, tableName)
        adapter.loadTable()
        //intent should include DB path/name and specified DB to open

        //set button to export dataset to CSV
//        var exportButton = findViewById<Button>(R.id.export_button)
//        exportButton.setOnClickListener {v ->
//
//        }

        val fab = findViewById<FloatingActionButton>(R.id.recognizerButton)
        fab.setOnClickListener { _ ->
            val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "US-en")
            recognizerIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            startActivityForResult(recognizerIntent, RECOGNIZER_REQUEST_CODE)
        }

        val bottomAppBar = findViewById<BottomAppBar>(R.id.bottom_app_bar)
        bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.graph_dataset -> {
                    val intent = Intent(this, GraphActivity::class.java)
                    intent.putExtra("DATASETNAME", tableName)
                    startActivity(intent)
                    true
                }
                R.id.insert_to_dataset -> {
                    val intent = Intent(this, InsertDataActivity::class.java)
                    intent.putExtra("DATASETNAME", tableName)
                    startActivityForResult(intent, INSERT_REQUEST_CODE)
                    true
                }
                R.id.setting_dataset -> {
                    Toast.makeText(this, "Setting not implemented yet", Toast.LENGTH_LONG).show()
                    true
                }
                R.id.export_data_button -> {
                    // create filename from dataset name and current date- should be unique
                    var timestamp: Timestamp = Timestamp(System.currentTimeMillis())
                    var title = tableName + "_" + timestamp.toString() + ".csv"

                    // replace any invalid characters in filename
                    title = title.replace(Regex("[^a-zA-Z0-9\\\\.\\\\-]"), "_")

                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "text/csv"
                        putExtra(Intent.EXTRA_TITLE, title)
                    }
                    startActivityForResult(intent, EXPORT_LOC_REQUEST_CODE)
                    true
                }

                else -> false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home-> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun makeCsvRow(data: List<Any>): String {
        var row: String = ""
        for (i in data.indices) {
            row += data[i].toString()
            if (i != data.size-1) {
                row += ","
            }
        }
        row += "\n"
        return row
    }

    private fun writeDocument(uri: Uri) {
        var os: OutputStream = this.contentResolver.openOutputStream(uri, "w")!!
        if (os != null) {
            // get writer for the file
            var osw: OutputStreamWriter = OutputStreamWriter(os)
            var writer: BufferedWriter = BufferedWriter(osw)

            // write col headers
            var headerVals = adapter.getColumnNames()
            var header = ""
            for (i in headerVals.indices) {

                // don't write ID column or Timestamp unless graphed by time
                if (headerVals[i] != "ID") {
                    if (headerVals[i] != "Timestamp" || DatabaseHelper(this).getXAxisColumnName(tableName) == "Timestamp") {
                        header += headerVals[i]
                        if (i != headerVals.size - 1) {
                            // add comma except for last element
                            header += ","
                        }
                    }
                }
            }
            writer.write(header)
            writer.write("\n")

            // write data
            for (i in 0 until adapter.getNumRows()) {
                val row: List<Any> = adapter.getDataRow(i)
                writer.write(makeCsvRow(row))
            }
            writer.close()
        }
        else {
            Log.d("herehere", "damn it") //lol
        }

        // edit document using documentURI
//        val writer = CsvWriter()
//        writer.open(File(uri.path!!)) {
//            // write headers
//            writeRow(adapter.getColumnNames())
//
//            // write data row by row
//            for (row in 0 until adapter.getNumRows()) {
//                writeRow(adapter.getDataRow(row))
//            }
//        }
//        Toast.makeText(this, "Saved as $uri", Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == INSERT_REQUEST_CODE && resultCode == RESULT_OK) {
            adapter.refreshTable()
            
        }
        else if(requestCode == RECOGNIZER_REQUEST_CODE) {
            if(resultCode == RESULT_OK || data != null) {
                val res: ArrayList<String> = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                Log.i("SpeechRecognizer", "returned text: ".plus(res[0]))
                val speechProcessor = SpeechProcessor(this)
                if(speechProcessor.insertDataset(res[0], tableName)) {
                    adapter.refreshTable()
                }
            }
        }
        else if (requestCode == EXPORT_LOC_REQUEST_CODE && resultCode == RESULT_OK
            && data != null) {
            data?.data?.also { uri ->
                Log.d("testing URI", uri.toString())
                writeDocument(uri)

            }
            //val documentFile: File = File(documentURI)

            // THE FILE IS BEING CREATED BUT IS NOT BEING FOUND WITH THE URI FOR SOME REASON

            // JUST TESTING
//            var testURI: String = "/com.android.providers.downloads.documents/"
//            File(testURI).walkTopDown().forEach { Log.d("testURI", it.path) }

            // DONE TESTING



        }
    }
}