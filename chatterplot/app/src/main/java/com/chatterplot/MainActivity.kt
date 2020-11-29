package com.chatterplot


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), RecognitionListener {
    private val permission = 10
    private val RECOGNIZER_REQUEST_CODE = 20
    private val CREATE_REQUEST_CODE = 30
    private lateinit var recognizerIntent: Intent
    private lateinit var continuousRecogIntent: Intent
    private lateinit var recycler_view: RecyclerView
    private lateinit var speechRecognizer: SpeechRecognizer
    private var getcommand = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val datasetList = getDatabases()

        recycler_view  = findViewById(R.id.recycler_view)
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
            getCommand()
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

        initContinuousRecog()
    }

    private fun getDatabases() : ArrayList<DatasetCard>{
        val datasetNameList = DatabaseHelper(this).getAllDatabaseNames()
        val cardList = ArrayList<DatasetCard>()
        for (i in 0 until datasetNameList.size) {
            val newCard = DatasetCard(R.drawable.graph_placeholder, datasetNameList[i])
            cardList.add(newCard)
        }
        return cardList
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RECOGNIZER_REQUEST_CODE) {
            if(resultCode == RESULT_OK || null != data) {
                val res: ArrayList<String> = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
//                val textView = findViewById<TextView>(R.id.textView)
//                textView.text = res[0]
                Log.i("SpeechRecognizer", "returned text: ".plus(res[0]))
                val speechProcessor = SpeechProcessor(this)
                if (speechProcessor.textProcessing(res[0], null) == 1) {
                    (recycler_view.adapter as DatasetRecyclerViewAdapter).addItem(speechProcessor.name!!)
                }
            }
        }
        else if(requestCode == CREATE_REQUEST_CODE) {
            if(resultCode == RESULT_OK || null != data) {
                val name: String = data!!.getStringExtra("NAME")
                (recycler_view.adapter as DatasetRecyclerViewAdapter).addItem(name)
                Toast.makeText(this, "Created dataset $name", Toast.LENGTH_LONG).show()
            }
        }
        getcommand = false
        speechRecognizer.startListening(continuousRecogIntent)
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

//    fun showCreateDialog(v: View?) {
//        val createDialog = layoutInflater.inflate(R.layout.create_table_view, null)
//        val datasetName = createDialog.findViewById<EditText>(R.id.datasetName)
//        val datasetIndependent = createDialog.findViewById<EditText>(R.id.datasetIndependent)
//        val datasetDependent = createDialog.findViewById<EditText>(R.id.datasetDependent)
//
//        val alertDialog = AlertDialog.Builder(this)
//        alertDialog.setTitle("Create new dataset")
//        alertDialog.setView(createDialog)
//        alertDialog.setNegativeButton("Cancel", null)
//        alertDialog.setPositiveButton("Create")  {dialog, which ->
//            val tableName = datasetName.text.toString()
//            val xAxis = datasetIndependent.text.toString()
//            val yAxis = datasetDependent.text.toString()
//            DatabaseHelper(this).createDataset(tableName, xAxis, yAxis)
//
//        }
//        alertDialog.create().show()
//    }

    fun showAllDatabase(v:View?) {
        val intent = Intent(this, DatabaseActivity::class.java)
        startActivity(intent)
    }

    fun showGraph(v:View?) {
        val intent = Intent(this, GraphActivity::class.java)
        startActivity(intent)
    }

    fun showDatasetList(v:View?) {
        val intent = Intent(this, DatasetListActivity::class.java)
        startActivity(intent)
    }

    fun initContinuousRecog() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(this)

        continuousRecogIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        continuousRecogIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "US-en")
        continuousRecogIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        continuousRecogIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

        speechRecognizer.startListening(continuousRecogIntent)
    }

    fun getCommand() {
        getcommand = true
        speechRecognizer.stopListening()
        startActivityForResult(recognizerIntent, RECOGNIZER_REQUEST_CODE)
    }

    override fun onReadyForSpeech(params: Bundle?) {
        Log.i("continuousRecog", "Ready to listen")
    }

    override fun onRmsChanged(rmsdB: Float) {
//        Log.i("continuousRecog", "Ready to listen")
    }

    override fun onBufferReceived(buffer: ByteArray?) {
//        Log.i("continuousRecog", "Ready to listen")
    }

    override fun onPartialResults(partialResults: Bundle?) {
//        Log.i("continuousRecog", "Ready to listen")
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
//        Log.i("continuousRecog", "Ready to listen")
    }

    override fun onBeginningOfSpeech() {
//        Log.i("continuousRecog", "Ready to listen")
    }

    override fun onEndOfSpeech() {
//        Log.i("continuousRecog", "Ready to listen")
    }

    override fun onError(error: Int) {
        var message = ""
        message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Didn't understand, please try again."
        }
        Log.i("continuousRecog", "Error: ".plus(message))
        if (getcommand) {
            speechRecognizer.stopListening()
        } else {
            speechRecognizer.startListening(continuousRecogIntent)
        }
    }

    override fun onResults(results: Bundle?) {
        val result = results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        var command = false
        for (word in result!!) {
            if (word == "listen") {
                command = true
            }
            Log.i("continuousRecog", "Result: ".plus(word))
        }
        if (command) {
            speechRecognizer.stopListening()
            startActivityForResult(recognizerIntent, RECOGNIZER_REQUEST_CODE)
        } else {
            speechRecognizer.startListening(continuousRecogIntent)
        }
    }
}