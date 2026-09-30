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
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VoiceService : Service(), TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var handler: Handler
    private lateinit var tts: TextToSpeech

    private val channelId = "gulshan_voice_channel"

    private var restarting = false
    private var lastCommandTime = 0L
    private var ttsReady = false

    override fun onCreate() {
        super.onCreate()

        handler = Handler(Looper.getMainLooper())

        tts = TextToSpeech(this, this)

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
                speak("Voice recognition available nahi hai.")
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

        var normalized =
            spokenText
                .lowercase(Locale.getDefault())
                .replace("।", " ")
                .replace(",", " ")
                .replace(".", " ")
                .replace("!", " ")
                .replace("?", " ")
                .replace(Regex("\\s+"), " ")
                .trim()

        // Hindi wake word को English form में normalize करें
        normalized = normalized
            .replace("गुलशन", "gulshan")
            .replace("गुलशन जी", "gulshan")
            .replace("हे गुलशन", "hello gulshan")
            .replace("हेलो गुलशन", "hello gulshan")
            .replace("हैलो गुलशन", "hello gulshan")
            .replace("हेलो गुलशन जी", "hello gulshan")
            .trim()

        if (!normalized.contains("gulshan")) {
            return
        }

        var command =
            normalized
                .replace("gulshan", "")
                .trim()

        // सिर्फ Gulshan / गुलशन
        if (command.isEmpty()) {
            lastCommandTime = now
            speak("Namaste! Main Gulshan hoon.")
            return
        }

        // hello gulshan
        if (
            command == "hello" ||
            command == "hi" ||
            command == "namaste"
        ) {
            lastCommandTime = now
            speak("Namaste! Main Gulshan hoon.")
            return
        }

        lastCommandTime = now

        // Background में सीधे handle होने वाली commands
        if (handleBackgroundCommand(command)) {
            return
        }

        // बाकी commands MainActivity को भेजें
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

            speak("Command chala raha hoon.")

            startActivity(intent)

        } catch (e: Exception) {

            speak(
                "Ye command background mein nahi chal saki."
            )
        }
    }

    private fun handleBackgroundCommand(
        command: String
    ): Boolean {

        when {

            command == "hello" ||
            command == "hi" ||
            command.contains("namaste") -> {

                speak("Namaste! Main Gulshan hoon.")
                return true
            }

            command.contains("battery") ||
            command.contains("battery status") ||
            command.contains("बैटरी") -> {

                val batteryManager =
                    getSystemService(BATTERY_SERVICE)
                            as android.os.BatteryManager

                val battery =
                    batteryManager.getIntProperty(
                        android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY
                    )

                if (battery >= 0) {
                    speak(
                        "Phone ki battery $battery percent hai."
                    )
                } else {
                    speak(
                        "Battery status nahi mil saka."
                    )
                }

                return true
            }

            command.contains("time") ||
            command.contains("samay") ||
            command.contains("समय") ||
            command.contains("टाइम") -> {

                val time =
                    SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                    ).format(Date())

                speak("Abhi time $time hai.")

                return true
            }

            command.contains("date") ||
            command.contains("tarikh") ||
            command.contains("तारीख") ||
            command.contains("डेट") -> {

                val date =
                    SimpleDateFormat(
                        "dd MMMM yyyy",
                        Locale("hi", "IN")
                    ).format(Date())

                speak("Aaj $date hai.")

                return true
            }

            command.contains("background voice off") ||
            command.contains("background voice band") ||
            command.contains("background voice stop") ||
            command.contains("बैकग्राउंड वॉइस बंद") -> {

                stopSelf()

                getSharedPreferences(
                    "gulshan",
                    MODE_PRIVATE
                )
                    .edit()
                    .putBoolean(
                        "background_voice_enabled",
                        false
                    )
                    .apply()

                speak("Background voice OFF.")

                return true
            }
        }

        return false
    }

    private fun speak(message: String) {

        if (!ttsReady) {
            return
        }

        tts.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "GULSHAN_BACKGROUND"
        )
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            val result =
                tts.setLanguage(
                    Locale("hi", "IN")
                )

            if (
                result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                tts.language =
                    Locale.getDefault()
            }

            tts.setSpeechRate(0.92f)
            tts.setPitch(1.08f)

            ttsReady = true
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

        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {
        return null
    }
}
