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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    private lateinit var resultText: TextView
    private lateinit var commandInput: EditText

    private val voiceRequest = 1001
    private val permissionRequest = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        createInterface()
        requestPermissionsIfNeeded()
    }

    private fun createInterface() {

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(30, 40, 30, 30)

        val title = TextView(this)
        title.text = "GULSHAN AI"
        title.textSize = 28f

        val status = TextView(this)
        status.text = "Ready"
        status.textSize = 18f

        commandInput = EditText(this)
        commandInput.hint = "Command likho..."

        val commandButton = Button(this)
        commandButton.text = "COMMAND CHALAO"

        val voiceButton = Button(this)
        voiceButton.text = "🎤 VOICE COMMAND"

        val youtubeButton = Button(this)
        youtubeButton.text = "YouTube"

        val chromeButton = Button(this)
        chromeButton.text = "Chrome"

        val cameraButton = Button(this)
        cameraButton.text = "Camera"

        val settingsButton = Button(this)
        settingsButton.text = "Settings"

        resultText = TextView(this)
        resultText.text = "Gulshan ready hai."
        resultText.textSize = 18f

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

        voiceButton.setOnClickListener {
            startVoiceCommand()
        }

        youtubeButton.setOnClickListener {
            openYouTube()
        }

        chromeButton.setOnClickListener {
            openChrome()
        }

        cameraButton.setOnClickListener {
            openCamera()
        }

        settingsButton.setOnClickListener {
            openSettings()
        }
    }

    private fun executeCommand(command: String) {

        val cmd = command.lowercase(Locale.getDefault()).trim()

        showResult("Command: $command")

        when {

            cmd == "hello gulshan" ||
                    cmd == "hello" ||
                    cmd.contains("हेलो गुलशन") -> {

                speak("Hello. Main Gulshan hoon.")
            }

            cmd.contains("youtube") ||
                    cmd.contains("यूट्यूब") -> {

                speak("YouTube khol raha hoon.")
                openYouTube()
            }

            cmd.contains("chrome") ||
                    cmd.contains("क्रोम") -> {

                speak("Chrome khol raha hoon.")
                openChrome()
            }

            cmd.contains("camera") ||
                    cmd.contains("कैमरा") -> {

                speak("Camera khol raha hoon.")
                openCamera()
            }

            cmd.contains("settings") ||
                    cmd.contains("setting") ||
                    cmd.contains("सेटिंग") -> {

                speak("Settings khol raha hoon.")
                openSettings()
            }

            cmd.contains("google") ||
                    cmd.contains("गूगल") -> {

                speak("Google khol raha hoon.")
                openGoogle()
            }

            cmd.contains("time") ||
                    cmd.contains("समय") ||
                    cmd.contains("टाइम") -> {

                val time = SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                ).format(Date())

                speak("Abhi time hai $time")
            }

            cmd.contains("date") ||
                    cmd.contains("तारीख") -> {

                val date = SimpleDateFormat(
                    "dd MMMM yyyy",
                    Locale.getDefault()
                ).format(Date())

                speak("Aaj ki tareekh hai $date")
            }

            cmd.contains("battery") ||
                    cmd.contains("बैटरी") -> {

                speak("Battery information phone ki settings se check kar sakte hain.")
                openBatterySettings()
            }

            cmd.contains("stop voice") ||
                    cmd.contains("voice band") ||
                    cmd.contains("आवाज़ बंद") -> {

                tts?.stop()
                showResult("Voice stopped.")
            }

            cmd.contains("background") ||
                    cmd.contains("background voice") -> {

                startVoiceService()
            }

            cmd.contains("voice service band") ||
                    cmd.contains("background band") -> {

                stopVoiceService()
            }

            cmd.contains("clear") ||
                    cmd.contains("साफ") -> {

                commandInput.text.clear()
                resultText.text = "Gulshan ready hai."
            }

            else -> {

                speak("Mujhe ye command abhi samajh nahi aayi.")
                showResult("Command not recognized: $command")
            }
        }
    }

    private fun startVoiceCommand() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                permissionRequest
            )
            return
        }

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Command bolo..."
        )

        try {
            startActivityForResult(intent, voiceRequest)
        } catch (e: Exception) {
            showResult("Voice recognition available nahi hai.")
        }
    }

    @Deprecated("Deprecated in Android API")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == voiceRequest &&
            resultCode == RESULT_OK &&
            data != null
        ) {

            val results = data.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )

            if (!results.isNullOrEmpty()) {

                val command = results[0]

                commandInput.setText(command)

                executeCommand(command)
            }
        }
    }

    private fun openYouTube() {

        val intent = packageIntent(
            "com.google.android.youtube"
        )

        if (intent != null) {
            startActivity(intent)
        } else {
            openUrl("https://www.youtube.com")
        }
    }

    private fun openChrome() {

        val intent = packageIntent(
            "com.android.chrome"
        )

        if (intent != null) {
            startActivity(intent)
        } else {
            openUrl("https://www.google.com")
        }
    }

    private fun openCamera() {

        try {
            val intent = Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE
            )
            startActivity(intent)
        } catch (e: Exception) {
            showResult("Camera open nahi ho saka.")
        }
    }

    private fun openSettings() {

        try {
            startActivity(
                Intent(Settings.ACTION_SETTINGS)
            )
        } catch (e: Exception) {
            showResult("Settings open nahi ho saka.")
        }
    }

    private fun openGoogle() {
        openUrl("https://www.google.com")
    }

    private fun openBatterySettings() {

        try {
            startActivity(
                Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
            )
        } catch (e: Exception) {
            openSettings()
        }
    }

    private fun openUrl(url: String) {

        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )

            startActivity(intent)

        } catch (e: Exception) {
            showResult("Link open nahi ho saka.")
        }
    }

    private fun packageIntent(
        packageName: String
    ): Intent? {

        return try {
            packageManager.getLaunchIntentForPackage(
                packageName
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun startVoiceService() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                permissionRequest
            )
            return
        }

        try {

            val intent = Intent(
                this,
                VoiceService::class.java
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }

            showResult("Background voice service started.")
            speak("Background voice service start kar diya.")

        } catch (e: Exception) {

            showResult(
                "Background service start nahi hua."
            )
        }
    }

    private fun stopVoiceService() {

        try {

            val intent = Intent(
                this,
                VoiceService::class.java
            )

            stopService(intent)

            showResult("Background voice service stopped.")
            speak("Background voice service band kar diya.")

        } catch (e: Exception) {

            showResult(
                "Background service stop nahi hua."
            )
        }
    }

    private fun requestPermissionsIfNeeded() {

        val permissions = ArrayList<String>()

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(
                Manifest.permission.RECORD_AUDIO
            )
        }

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(
                Manifest.permission.CAMERA
            )
        }

        if (
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        if (permissions.isNotEmpty()) {

            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                permissionRequest
            )
        }
    }

    private fun showResult(message: String) {
        resultText.text = message
    }

    private fun speak(message: String) {

        val engine = tts ?: return

        if (engine.isSpeaking) {
            engine.stop()
        }

        engine.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "gulshan_response"
        )
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            tts?.language = Locale("hi", "IN")
        }
    }

    override fun onDestroy() {

        tts?.stop()
        tts?.shutdown()
        tts = null

        super.onDestroy()
    }
}
