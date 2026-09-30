package com.gulshan.ai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeech
    private lateinit var commandBox: EditText
    private lateinit var statusText: TextView
    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) { result ->
            if (result == TextToSpeech.SUCCESS) {
                tts.language = Locale("hi", "IN")
                tts.setSpeechRate(0.92f)
                tts.setPitch(1.08f)
            }
        }

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 60, 40, 40)

        val title = TextView(this)
        title.text = "GULSHAN AI"
        title.textSize = 32f

        statusText = TextView(this)
        statusText.text = "Namaste! Main Gulshan hoon."
        statusText.textSize = 20f
        statusText.setPadding(0, 30, 0, 20)

        commandBox = EditText(this)
        commandBox.hint = "Command likho..."
        commandBox.textSize = 18f
        commandBox.setSingleLine(true)

        val commandButton = Button(this)
        commandButton.text = "COMMAND CHALAO"

        val voiceButton = Button(this)
        voiceButton.text = "🎤 VOICE COMMAND"

        resultText = TextView(this)
        resultText.text = "Result yahan dikhega."
        resultText.textSize = 18f
        resultText.setPadding(0, 30, 0, 20)

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(commandBox)
        layout.addView(commandButton)
        layout.addView(voiceButton)
        layout.addView(resultText)

        setContentView(layout)

        commandButton.setOnClickListener {
            val command = commandBox.text.toString().trim()

            if (command.isEmpty()) {
                showResult("Pehle command likho.")
            } else {
                executeCommand(command)
            }
        }

        voiceButton.setOnClickListener {
            startVoiceInput()
        }
    }

    private fun startVoiceInput() {

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
            RecognizerIntent.EXTRA_PROMPT,
            "Gulshan ko command bolo"
        )

        try {
            startActivityForResult(intent, 100)
        } catch (e: Exception) {
            showResult("Voice input available nahi hai.")
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {

            val results =
                data?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )

            val command = results?.firstOrNull()

            if (!command.isNullOrEmpty()) {
                commandBox.setText(command)
                executeCommand(command)
            }
        }
    }

    private fun executeCommand(command: String) {

        val original = command.trim()

        val cmd = original
            .lowercase(Locale.getDefault())
            .replace(".", "")
            .replace(",", "")
            .replace("!", "")
            .replace("?", "")
            .trim()

        statusText.text = "✅ Command mili: $original"

        // -------------------------
        // GREETING
        // -------------------------

        if (
            cmd == "hello" ||
            cmd == "hi" ||
            cmd.contains("hello gulshan") ||
            cmd.contains("hi gulshan") ||
            cmd.contains("हेलो गुलशन") ||
            cmd.contains("नमस्ते गुलशन")
        ) {
            respond(
                "Hello! Main Gulshan hoon. Aapki command mili."
            )
            return
        }

        // -------------------------
        // TIME
        // -------------------------

        if (
            cmd.contains("time") ||
            cmd.contains("samay") ||
            cmd.contains("समय") ||
            cmd.contains("टाइम")
        ) {
            val time = java.text.SimpleDateFormat(
                "hh:mm a",
                Locale.getDefault()
            ).format(java.util.Date())

            respond("Abhi time hai $time.")
            return
        }

        // -------------------------
        // YOUTUBE
        // -------------------------

        if (
            cmd.contains("youtube") ||
            cmd.contains("यूट्यूब")
        ) {
            openWebsite("https://www.youtube.com")
            respond("YouTube khol rahi hoon.")
            return
        }

        // -------------------------
        // GOOGLE
        // -------------------------

        if (
            cmd.contains("google") ||
            cmd.contains("गूगल")
        ) {
            openWebsite("https://www.google.com")
            respond("Google khol rahi hoon.")
            return
        }

        // -------------------------
        // CHROME
        // -------------------------

        if (
            cmd.contains("chrome") ||
            cmd.contains("क्रोम")
        ) {
            openApp("com.android.chrome")
            return
        }

        // -------------------------
        // CAMERA
        // -------------------------

        if (
            cmd.contains("camera") ||
            cmd.contains("कैमरा")
        ) {
            try {
                val cameraIntent =
                    Intent("android.media.action.IMAGE_CAPTURE")

                startActivity(cameraIntent)

                respond("Camera khol rahi hoon.")

            } catch (e: Exception) {
                respond("Camera open nahi ho saka.")
            }

            return
        }

        // -------------------------
        // SETTINGS
        // -------------------------

        if (
            cmd.contains("setting") ||
            cmd.contains("settings") ||
            cmd.contains("सेटिंग")
        ) {
            try {
                startActivity(
                    Intent(Settings.ACTION_SETTINGS)
                )

                respond("Settings khol rahi hoon.")

            } catch (e: Exception) {
                respond("Settings open nahi ho saka.")
            }

            return
        }

        // -------------------------
        // APP UPDATE REQUEST
        // -------------------------

        if (
            cmd.contains("update") ||
            cmd.contains("अपडेट")
        ) {
            respond(
                "Update system ke liye remote update service connect karni hogi. " +
                "Main abhi bina permission ke khud APK install nahi kar sakti."
            )
            return
        }

        // -------------------------
        // UNKNOWN COMMAND
        // -------------------------

        respond(
            "Command mili: $original. " +
            "Is command ka action abhi available nahi hai."
        )
    }

    // -------------------------
    // RESPONSE
    // -------------------------

    private fun respond(message: String) {

        showResult(
            "🤖 GULSHAN:\n$message"
        )

        speak(message)

        Toast.makeText(
            this,
            "Command successfully mili",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showResult(message: String) {
        resultText.text = message
    }

    // -------------------------
    // SPEECH
    // -------------------------

    private fun speak(message: String) {

        if (::tts.isInitialized) {

            tts.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "gulshan_voice"
            )
        }
    }

    // -------------------------
    // OPEN WEBSITE
    // -------------------------

    private fun openWebsite(url: String) {

        try {

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )

            startActivity(intent)

        } catch (e: Exception) {

            showResult(
                "Website open nahi ho saki."
            )
        }
    }

    // -------------------------
    // OPEN APP
    // -------------------------

    private fun openApp(packageName: String) {

        try {

            val launchIntent =
                packageManager.getLaunchIntentForPackage(
                    packageName
                )

            if (launchIntent != null) {

                startActivity(launchIntent)

            } else {

                respond(
                    "Ye app phone mein installed nahi hai."
                )
            }

        } catch (e: Exception) {

            respond(
                "App open nahi ho saka."
            )
        }
    }

    override fun onDestroy() {

        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }

        super.onDestroy()
    }
}
