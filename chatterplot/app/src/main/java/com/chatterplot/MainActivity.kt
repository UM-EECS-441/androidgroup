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

class MainActivity : AppCompatActivity(), RecognitionListener {
    private val permission = 10
    private val RECOGNIZER_REQUEST_CODE = 20
    private lateinit var returnedText: TextView
    private lateinit var recognizerButton: Button
//    private lateinit var progressBar: ProgressBar
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
//        progressBar = findViewById(R.id.progressBar)

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

//        setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
////                progressBar.visibility = View.VISIBLE
////                progressBar.isIndeterminate = true
//                startActivityForResult(recognizerIntent, RECOGNIZER_REQUEST_CODE)
////                speech.startListening(recognizerIntent)
////                Log.i("SpeechRecognizer","Asking Record Audio permission")
////                ActivityCompat.requestPermissions(this@MainActivity,
////                    arrayOf(Manifest.permission.RECORD_AUDIO),
////                    permission)
//            } else {
////                progressBar.isIndeterminate = false
////                progressBar.visibility = View.VISIBLE
////                speech.stopListening()
//                Log.i("SpeechRecognizer","stop listening")
//            }
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RECOGNIZER_REQUEST_CODE) {
            if(resultCode == RESULT_OK || null != data) {
                val res: ArrayList<String> = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val textView = findViewById<TextView>(R.id.textView)
                textView.text = res[0]
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
//        when (requestCode) {
//            permission -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager
//                    .PERMISSION_GRANTED) {
//                Log.i("SpeechRecognizer","Permission Granted, start listening")
//                speech.startListening(recognizerIntent)
//            } else {
//                Toast.makeText(this@MainActivity, "Permission Denied!",
//                    Toast.LENGTH_SHORT).show()
//            }
//        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onRmsChanged(rmsdB: Float) {
        TODO("Not yet implemented")
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun onPartialResults(partialResults: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onBeginningOfSpeech() {
        Log.i("SpeechRecognizer", "onBeginningOfSpeech")
//        progressBar.isIndeterminate = false
//        progressBar.max = 10
    }
    override fun onEndOfSpeech() {
//        progressBar.isIndeterminate = true
        Log.i("SpeechRecognizer", "onEndOfSpeech")
//        toggleButton.isChecked = false
    }

    override fun onError(error: Int) {
        val errorMessage: String = getErrorText(error)
        Log.d("SpeechRecognizer", "FAILED $errorMessage")
        returnedText.text = errorMessage
//        toggleButton.isChecked = false
    }

    private fun getErrorText(error: Int): String {
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
        return message
    }

    override fun onResults(results: Bundle?) {
        Log.i("SpeechRecognizer", "onResults")
        val matches = results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        var text = ""
        if (matches != null) {
            for (result in matches) text = result.trimIndent()
        }
        returnedText.text = text
    }
}