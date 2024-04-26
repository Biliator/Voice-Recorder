package com.example.kotlinrecorder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Chronometer
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_record.*
import java.io.IOException

class RecordActivity : AppCompatActivity() {

    // Promene pro zobrazeni casu
    private var running: Boolean = false
    private var pauseOffset: Long = -1

    // Promene pro nahravani zvuku
    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var recordingStopped: Boolean = false

    private var povolenka = true

    private val TAG = "RecordActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        // Cesta k ulozene nahravce
        mediaRecorder = MediaRecorder()
        output = Environment.getExternalStorageDirectory().absolutePath + "/recording.mp3"

        // Vytvoreni chronometru
        chronometer2.format = "Time: %s"
     /*
        chronometer2.setOnChronometerTickListener {
                if (SystemClock.elapsedRealtime() - it.base >= 10000) {
                    it.base = SystemClock.elapsedRealtime()
                    Toast.makeText(this, "Bing!", Toast.LENGTH_SHORT).show()
            }
        }
        */

        // Po kliknuti na start se zepta o povoleni
        btnstart.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val permissions = arrayOf(
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
                ActivityCompat.requestPermissions(this, permissions, 0)
            } else {
                if (povolenka)
                    povoleni()
                startRecording()
            }
        }

        btnstop.setOnClickListener {
            stopRecording()
        }

        btnpause.setOnClickListener {
            pauseRecording()
        }
    }

    private fun startChronometr() {
        if (!running) {
            chronometer2.base = SystemClock.elapsedRealtime() - pauseOffset // Vrati milisekundy od zavedeni se casem utrcenym ve spanku
            chronometer2!!.start()
            Log.d(TAG, "Start Chronometr")
            running = true
        }
    }

    private fun pauseChronometr() {
        if (running) {
            chronometer2!!.stop()
            pauseOffset = SystemClock.elapsedRealtime() - chronometer2.base
            Log.d(TAG,"Pause Chronometr")
            running = false
        }
    }

    private fun StopChronometr() {
        chronometer2!!.stop()
        chronometer2.base = SystemClock.elapsedRealtime()
        Log.d(TAG, "Stop Chronometr")
        pauseOffset = 0
    }

    private fun startRecording() {
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            Log.d(TAG, "Start Recording")
            startChronometr()
            state = true
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun pauseRecording() {
        if (state) {
            if (!recordingStopped) {
                Toast.makeText(this, "Stopped!", Toast.LENGTH_SHORT).show()
                mediaRecorder?.pause()
                Log.d(TAG, "Pause Recording")
                pauseChronometr()
                recordingStopped = true
                btnpause.text = "Resume"
            } else {
                resumeRecording()
            }
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun resumeRecording() {
        Toast.makeText(this, "Resume!", Toast.LENGTH_SHORT).show()
        mediaRecorder?.resume()
        Log.d(TAG, "Resume Recording")
        startChronometr()
        btnpause.text = "Pause"
        recordingStopped = false
    }

    private fun stopRecording() {
        if (state) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            Log.d(TAG, "Stop Recording")
            StopChronometr()
            state = false
            Toast.makeText(this, "Stopped!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun povoleni() {
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(output)
        povolenka = false
        Log.d(TAG, "Ziskano povoleni")
    }
}
