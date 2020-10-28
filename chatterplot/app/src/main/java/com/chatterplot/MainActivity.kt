package com.chatterplot


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_dataset_list.*

class MainActivity : AppCompatActivity() {
    private val permission = 10
    private val RECOGNIZER_REQUEST_CODE = 20
    private val CREATE_REQUEST_CODE = 30
    private lateinit var recognizerIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        setContentView(R.layout.activity_dataset_list)

        val datasetList = getDatabases()

        recycler_view.adapter = DatasetRecyclerViewAdapter(datasetList)
        recycler_view.layoutManager = LinearLayoutManager(this)


        if(ContextCompat.checkSelfPermission(this@MainActivity,
            Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ), permission
            )
        }

        val recognizerButton = findViewById<FloatingActionButton>(R.id.recognizerButton)
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "US-en")
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

        recognizerButton.setOnClickListener { v ->
            startActivityForResult(recognizerIntent, RECOGNIZER_REQUEST_CODE)
        }

//        val createDatasetButton = findViewById<Button>(R.id.createTableButton)
//        createDatasetButton.setOnClickListener { view ->
//            showCreateDialog(view)
//        }

        val bottomAppBar = findViewById<BottomAppBar>(R.id.bottom_app_bar)
        bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.search_dataset -> {
                    Toast.makeText(this, "Search not implemented yet", Toast.LENGTH_LONG).show()
                    true
                }
                R.id.create_dataset -> {
                    val intent = Intent(this, CreateDatasetActivity::class.java)
                    startActivityForResult(intent, CREATE_REQUEST_CODE)
//                    showCreateDialog(null)
                    true
                }
                R.id.setting_dataset -> {
                    showAllDatabase(null)
                    true
                }
                else -> false
            }
        }
    }

    private fun getDatabases() : List<DatasetCard>{
        val datasetNameList = DatabaseHelper(this).getAllDatabaseNames()
        val cardList = ArrayList<DatasetCard>()
        for (i in 0 until datasetNameList.size) {
            val newCard = DatasetCard(R.drawable.graph_placeholder, datasetNameList[i])
            cardList += newCard
        }
        return cardList
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RECOGNIZER_REQUEST_CODE) {
            if(resultCode == RESULT_OK || null != data) {
                val res: ArrayList<String> = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val textView = findViewById<TextView>(R.id.textView)
                textView.text = res[0]
                Log.i("SpeechRecognizer", "returned text: ".plus(res[0]))
                val speechProcessor = SpeechProcessor(this)
                speechProcessor.textProcessing(res[0])
            }
        }
        else if(requestCode == CREATE_REQUEST_CODE) {
            if(resultCode == RESULT_OK || null != data) {
                val name: String = data!!.getStringExtra("NAME")
                Toast.makeText(this, "Created dataset $name", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == permission) {
            if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Audio access denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    fun showCreateDialog(v: View?) {
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
            try {
                DatabaseHelper(this).createTable(schema)
            } catch(e: Exception) {
                Log.e("DB Error", e.localizedMessage!!.toString())
            }

        }
        alertDialog.create().show()
    }

    fun showAllDatabase(v:View?) {
        val intent = Intent(this, DatabaseActivity::class.java)
        startActivity(intent)
    }

    fun showGraph(v:View?) {
        val intent = Intent(this, GraphActivity::class.java)
        startActivity(intent)
    }

    fun startEditor(v:View?) {
        val intent = Intent(this, DatasetListActivity::class.java)
        startActivity(intent)
    }
}