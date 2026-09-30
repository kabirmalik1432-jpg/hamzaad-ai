package com.gulshan.ai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import java.util.Locale

class VoiceService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var handler: Handler

    private val channelId = "gulshan_voice_channel"
    private var restarting = false
    private var lastCommandTime = 0L

    override fun onCreate() {
        super.onCreate()

        handler = Handler(Looper.getMainLooper())

        createNotificationChannel()

        startForeground(
            2001,
            createNotification()
        )

        startListening()
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= 26) {

            val channel = NotificationChannel(
                channelId,
                "Gulshan Background Voice",
                NotificationManager.IMPORTANCE_LOW
            )

            channel.description =
                "Gulshan AI background voice command"

            val manager =
                getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {

        return NotificationCompat.Builder(
            this,
            channelId
        )
            .setContentTitle("Gulshan AI")
            .setContentText(
                "Background voice command active"
            )
            .setSmallIcon(
                android.R.drawable.ic_btn_speak_now
            )
            .setOngoing(true)
            .build()
    }

    private fun startListening() {

        if (!::handler.isInitialized) return

        handler.post {

            if (!SpeechRecognizer.isRecognitionAvailable(this)) {
                return@post
            }

            try {

                speechRecognizer?.destroy()

                speechRecognizer =
                    SpeechRecognizer.createSpeechRecognizer(this)

                speechRecognizer?.setRecognitionListener(
                    object : RecognitionListener {

                        override fun onReadyForSpeech(
                            params: Bundle?
                        ) {
                        }

                        override fun onBeginningOfSpeech() {
                        }

                        override fun onRmsChanged(
                            rmsdB: Float
                        ) {
                        }

                        override fun onBufferReceived(
                            buffer: ByteArray?
                        ) {
                        }

                        override fun onEndOfSpeech() {
                        }

                        override fun onError(
                            error: Int
                        ) {
                            restartListening()
                        }

                        override fun onResults(
                            results: Bundle?
                        ) {

                            val list =
                                results?.getStringArrayList(
                                    SpeechRecognizer.RESULTS_RECOGNITION
                                )

                            val spoken =
                                list?.firstOrNull()

                            if (!spoken.isNullOrBlank()) {
                                handleVoiceCommand(spoken)
                            }

                            restartListening()
                        }

                        override fun onPartialResults(
                            partialResults: Bundle?
                        ) {
                        }

                        override fun onEvent(
                            eventType: Int,
                            params: Bundle?
                        ) {
                        }
                    }
                )

                val intent = Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                )

                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )

                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    "hi-IN"
                )

                intent.putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    false
                )

                speechRecognizer?.startListening(intent)

            } catch (e: Exception) {
                restartListening()
            }
        }
    }

    private fun restartListening() {

        if (restarting) return

        restarting = true

        handler.postDelayed(
            {
                restarting = false
                startListening()
            },
            1200
        )
    }

    private fun handleVoiceCommand(
        spokenText: String
    ) {

        val now = System.currentTimeMillis()

        if (now - lastCommandTime < 2500) {
            return
        }

        val normalized =
            spokenText
                .lowercase(Locale.getDefault())
                .replace("।", " ")
                .replace(",", " ")
                .replace(".", " ")
                .replace(Regex("\\s+"), " ")
                .trim()

        if (!normalized.contains("gulshan")) {
            return
        }

        val command =
            normalized
                .replace("gulshan", "")
                .trim()

        if (command.isEmpty()) {
            return
        }

        lastCommandTime = now

        val intent =
            Intent(
                this,
                MainActivity::class.java
            )

        intent.putExtra(
            "background_command",
            command
        )

        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        )

        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Android background-activity restrictions
            // may block some launches.
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        return START_STICKY
    }

    override fun onDestroy() {

        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null

        handler.removeCallbacksAndMessages(null)

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {
        return null
    }
}
