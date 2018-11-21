package com.example.speechrecognition

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_first.*
import java.io.File

class FirstActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        val savePath = externalCacheDir.absolutePath + "/audio_record"
        val file = File(savePath)
        if (!file.exists()) {
            file.mkdir()
        }

        voiceInput.savePath = savePath
        voiceInput.onRecordSuccess = { file, duration ->
            val audio = File(file)
            Log.d("VoiceInput", "$duration, ${audio.length()}")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        voiceInput.requestPermissionsResult(this, requestCode, grantResults)
    }
}