package com.example.speechrecognition

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast
import edu.cmu.pocketsphinx.*
import java.io.File

class MainActivity : AppCompatActivity() {
    private var recognizer: SpeechRecognizer? = null
    private var captions = HashMap<String, String>()
    private var btnGoto: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        captions[KWS_SEARCH] = SPEECH_TIPS

        btnGoto = findViewById<Button>(R.id.goto_activity).apply {
            setOnClickListener {
                val permission = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        PERMISSIONS_REQUEST_RECORD_AUDIO
                    )
                    return@setOnClickListener
                }

                prepare()
            }
        }
    }

    override fun onDestroy() {
        stopRecognizer()
        recognizer = null
        btnGoto = null
        captions.clear()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                prepare()
            } else {
                showToast("You have no RECORD_AUDIO permission!!")
            }
        }
    }

    private fun prepare() {
        try {
            val assetDir = Assets(this).syncAssets()
            setupRecognizer(assetDir)
            switchSearch()
        } catch (e: Exception) {
            Log.e("hxg", e.message)
        }
    }

    private fun showToast(msg: String){
        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
    }

    private fun goto(){
        startActivity(Intent(this, FirstActivity::class.java))
    }

    private fun setupRecognizer(assetDir: File) {
        recognizer = SpeechRecognizerSetup.defaultSetup()
            .setAcousticModel(File(assetDir, "en-us-ptm"))
            .setDictionary(File(assetDir, "cmudict-en-us.dict"))
            .setRawLogDir(assetDir)
            .recognizer
        recognizer?.addListener(object : RecognitionListener {
            override fun onResult(hypothesis: Hypothesis?) {
                hypothesis?.let { showToast(it.hypstr) }
            }

            override fun onPartialResult(hypothesis: Hypothesis?) {
                val result = hypothesis?.hypstr ?: return
                if (result == KEYPHRASE) {
                    Log.i("hxg", "'$result' is right, perfect!")
                    showToast(result)
                    stopRecognizer()
                    goto()
                }
            }

            override fun onTimeout() {
                switchSearch()
            }

            override fun onBeginningOfSpeech() {
                Log.d("hxg", "onBeginningOfSpeech")
            }

            override fun onEndOfSpeech() {
                val res = recognizer?.searchName ?: return
                if (res != KWS_SEARCH) {
                    switchSearch()
                }
            }

            override fun onError(e: Exception?) {
                Log.e("hxg", e?.message)
            }
        })

        // Create keyword-activation search.
        recognizer?.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE)
    }

    private fun switchSearch() {
        recognizer?.run {
            stop()
            startListening(KWS_SEARCH)
        }
    }

    private fun stopRecognizer(){
        recognizer?.run {
            cancel()
            shutdown()
        }
    }

    companion object {
        /* Named searches allow to quickly reconfigure the decoder */
        private const val KWS_SEARCH = "wakeup"
        private const val KEYPHRASE = "next one"
        private const val SPEECH_TIPS = "To start demonstration say 'next one'."
        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 2018
    }
}
