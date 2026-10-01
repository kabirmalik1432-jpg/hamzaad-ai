package com.gulshan.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var ttsReady = false

    private lateinit var resultText: TextView
    private lateinit var commandInput: EditText

    // Modern Activity Result Launchers
    private val voiceLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                val command = results[0]
                commandInput.setText(command)
                executeCommand(command)
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val recordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (recordAudioGranted) {
            showResult("Permissions granted.")
        } else {
            showResult("Permission deny ho gayi.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        createInterface()
        requestPermissionsIfNeeded()
        handleBackgroundIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent?.let { handleBackgroundIntent(it) }
    }

    private fun handleBackgroundIntent(intent: Intent) {
        val command = intent.getStringExtra("background_command")
        if (!command.isNullOrBlank()) {
            commandInput.setText(command)
            executeCommand(command)
        }
    }

    private fun createInterface() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 40, 30, 30)
        }

        val title = TextView(this).apply {
            text = "GULSHAN AI"
            textSize = 28f
        }

        val status = TextView(this).apply {
            text = "Ready"
            textSize = 18f
        }

        commandInput = EditText(this).apply {
            hint = "Command likho..."
        }

        val commandButton = Button(this).apply { text = "COMMAND CHALAO" }
        val voiceButton = Button(this).apply { text = "🎤 VOICE COMMAND" }
        val youtubeButton = Button(this).apply { text = "YouTube" }
        val chromeButton = Button(this).apply { text = "Chrome" }
        val cameraButton = Button(this).apply { text = "Camera" }
        val settingsButton = Button(this).apply { text = "Settings" }

        resultText = TextView(this).apply {
            text = "Gulshan ready hai."
            textSize = 18f
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(commandInput)
        layout.addView(commandButton)
        layout.addView(voiceButton)
        layout.addView(youtubeButton)
        layout.addView(chromeButton)
        layout.addView(cameraButton)
        layout.addView(settingsButton)
        layout.addView(resultText)

        setContentView(layout)

        commandButton.setOnClickListener {
            val command = commandInput.text.toString().trim()
            if (command.isNotEmpty()) {
                executeCommand(command)
            } else {
                showResult("Command likho.")
                speak("Command likho.")
            }
        }

        voiceButton.setOnClickListener { startVoiceCommand() }
        youtubeButton.setOnClickListener { openYouTube() }
        chromeButton.setOnClickListener { openChrome() }
        cameraButton.setOnClickListener { openCamera() }
        settingsButton.setOnClickListener { openSettings() }
    }

    private fun executeCommand(command: String) {
        val cmd = command.lowercase(Locale.getDefault()).trim()
        showResult("Command: $command")

        when {
            cmd == "hello gulshan" || cmd == "hello" || cmd.contains("हेलो गुलशन") -> {
                speak("Hello. Main Gulshan hoon.")
            }
            cmd.contains("voice service band") || cmd.contains("background voice off") ||
            cmd.contains("background voice band") || cmd.contains("background voice stop") ||
            cmd.contains("background band") -> {
                stopVoiceService()
            }
            cmd.contains("background voice") || cmd == "background" -> {
                startVoiceService()
            }
            cmd.contains("youtube") || cmd.contains("यूट्यूब") -> {
                speak("YouTube khol raha hoon.")
                openYouTube()
            }
            cmd.contains("chrome") || cmd.contains("क्रोम") -> {
                speak("Chrome khol raha hoon.")
                openChrome()
            }
            cmd.contains("camera") || cmd.contains("कैमरा") -> {
                speak("Camera khol raha hoon.")
                openCamera()
            }
            cmd.contains("settings") || cmd.contains("setting") || cmd.contains("सेटिंग") -> {
                speak("Settings khol raha hoon.")
                openSettings()
            }
            cmd.contains("google") || cmd.contains("गूगल") -> {
                speak("Google khol raha hoon.")
                openGoogle()
            }
            cmd.contains("time") || cmd.contains("समय") || cmd.contains("टाइम") -> {
                val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                speak("Abhi time hai $time")
            }
            cmd.contains("date") || cmd.contains("तारीख") -> {
                val date = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
                speak("Aaj ki tareekh hai $date")
            }
            cmd.contains("battery") || cmd.contains("बैटरी") -> {
                speak("Battery information phone ki settings se check kar sakte hain.")
                openBatterySettings()
            }
            cmd.contains("stop voice") || cmd.contains("voice band") || cmd.contains("आवाज़ बंद") -> {
                stopSpeaking()
                showResult("Voice stopped.")
            }
            cmd.contains("clear") || cmd.contains("साफ") -> {
                commandInput.text.clear()
                resultText.text = "Gulshan ready hai."
            }
            else -> {
                showResult("Command not recognized: $command")
                speak("Mujhe ye command abhi samajh nahi aayi.")
            }
        }
    }

    private fun startVoiceCommand() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Command bolo...")
        }

        try {
            voiceLauncher.launch(intent)
        } catch (e: Exception) {
            showResult("Voice recognition available nahi hai.")
        }
    }

    private fun openYouTube() {
        val intent = packageIntent("com.google.android.youtube")
        if (intent != null) {
            startActivity(intent)
        } else {
            openUrl("https://www.youtube.com")
        }
    }

    private fun openChrome() {
        val intent = packageIntent("com.android.chrome")
        if (intent != null) {
            startActivity(intent)
        } else {
            openUrl("https://www.google.com")
        }
    }

    private fun openCamera() {
        try {
            val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            startActivity(intent)
        } catch (e: Exception) {
            showResult("Camera open nahi ho saka.")
        }
    }

    private fun openSettings() {
        try {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        } catch (e: Exception) {
            showResult("Settings open nahi ho saka.")
        }
    }

    private fun openGoogle() {
        openUrl("https://www.google.com")
    }

    private fun openBatterySettings() {
        try {
            startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
        } catch (e: Exception) {
            openSettings()
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            showResult("Link open nahi ho saka.")
        }
    }

    private fun packageIntent(packageName: String): Intent? {
        return try {
            packageManager.getLaunchIntentForPackage(packageName)
        } catch (e: Exception) {
            null
        }
    }

    private fun startVoiceService() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
            return
        }

        try {
            val intent = Intent(this, VoiceService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            showResult("Background voice service started.")
            speak("Background voice service start kar diya.")
        } catch (e: Exception) {
            showResult("Background service start nahi hua.")
        }
    }

    private fun stopVoiceService() {
        try {
            val intent = Intent(this, VoiceService::class.java)
            stopService(intent)
            showResult("Background voice service stopped.")
            speak("Background voice service band kar diya.")
        } catch (e: Exception) {
            showResult("Background service stop nahi hua.")
        }
    }

    private fun requestPermissionsIfNeeded() {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun showResult(message: String) {
        resultText.text = message
    }

    private fun speak(message: String) {
        if (!ttsReady) {
            showResult(message)
            return
        }

        if (tts.isSpeaking) {
            tts.stop()
        }

        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "gulshan_response")
    }

    private fun stopSpeaking() {
        if (tts.isSpeaking) {
            tts.stop()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("hi", "IN"))
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        } else {
            ttsReady = false
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        ttsReady = false
        super.onDestroy()
    }
}
