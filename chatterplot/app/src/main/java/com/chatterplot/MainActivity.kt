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
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val permission = 10
    private val RECOGNIZER_REQUEST_CODE = 20
    private lateinit var returnedText: TextView
    private lateinit var recognizerButton: Button
    private lateinit var speech: SpeechRecognizer
    private lateinit var recognizerIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(ContextCompat.checkSelfPermission(this@MainActivity,
            Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ), permission
            )
        }

        returnedText = findViewById(R.id.textView)
        recognizerButton = findViewById<Button>(R.id.recognizerButton)

        speech = SpeechRecognizer.createSpeechRecognizer(this)
        Log.i("SpeechRecognizer","isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this))
        speech.setRecognitionListener(this)
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "US-en")
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

        recognizerButton.setOnClickListener { v ->
            startActivityForResult(recognizerIntent, RECOGNIZER_REQUEST_CODE)
        }

        val createDatasetButton = findViewById<Button>(R.id.createTableButton)
        createDatasetButton.setOnClickListener { view ->
            showCreateDialog(view)
        }
    }

    private fun textProcessing(text: String) {
        if ((text.contains("make") || text.contains("create")) && text.contains("data")) {
            var name = " "
            for (word in text.split(" ")) {
                Log.i("SpeechRecognizer", "text response word: ".plus(word))
                if (word == "name") {
                    var idx = text.indexOf("name") + 5
                    name = text.substring(idx)
                    break
                } else if (word == "named") {
                    var idx = text.indexOf("named") + 6
                    name = text.substring(idx)
                    break
                }
            }
            Log.i("SpeechRecognizer","creating dataset named: ".plus(name))
            Toast.makeText(this, "Creating Dataset named: ".plus(name), Toast.LENGTH_SHORT).show()
            // Run create dataset function
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RECOGNIZER_REQUEST_CODE) {
            if(resultCode == RESULT_OK || null != data) {
                val res: ArrayList<String> = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val textView = findViewById<TextView>(R.id.textView)
                textView.text = res[0]
                Log.i("SpeechRecognizer", "returned text: ".plus(res[0]))
                textProcessing(res[0])
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

    fun showGraph(v: View) {
        val intent = Intent(this, GraphActivity::class.java)
        startActivity(intent)
    }
}