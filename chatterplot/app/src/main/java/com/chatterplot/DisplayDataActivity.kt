package com.chatterplot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton


class DisplayDataActivity : AppCompatActivity() {
    lateinit var adapter: TableAdapter
    private val INSERT_REQUEST_CODE = 10
    private val RECOGNIZER_REQUEST_CODE = 20
    private lateinit var tableName:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_data)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tableName = intent.getStringExtra("DATASETNAME")
        //create tableAdapter
        adapter = TableAdapter(this, tableName)
        adapter.loadTable()
        //intent should include DB path/name and specified DB to open

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
    }
}