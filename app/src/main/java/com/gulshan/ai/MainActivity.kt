package com.gulshan.ai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
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

        createInterface()
    }

    private fun createInterface() {

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
                respond("Pehle command likho.")
            } else {
                executeCommand(command)
            }
        }

        voiceButton.setOnClickListener {
            startVoiceInput()
        }
    }

    private fun executeCommand(command: String) {

        val original = command.trim()

        val cmd = original
            .lowercase(Locale.getDefault())
            .replace("\n", " ")
            .replace(".", "")
            .replace(",", "")
            .replace("!", "")
            .replace("?", "")
            .replace("-", " ")
            .replace("_", " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        statusText.text = "Command mili: $original"

        // HELLO
        if (
            cmd.contains("hello") ||
            cmd.contains("hi") ||
            cmd.contains("हेलो") ||
            cmd.contains("नमस्ते")
        ) {
            respond("Hello! Main Gulshan hoon.")
            return
        }

        // CHROME
        if (
            cmd.contains("chrome") ||
            cmd.contains("crom") ||
            cmd.contains("chrom") ||
            cmd.contains("krom") ||
            cmd.contains("क्रोम")
        ) {
            openChrome()
            return
        }

        // YOUTUBE
        if (
            cmd.contains("youtube") ||
            cmd.contains("you tube") ||
            cmd.contains("यूट्यूब")
        ) {
            openWebsite(
                "https://www.youtube.com",
                "YouTube khol rahi hoon."
            )
            return
        }

        // GOOGLE
        if (
            cmd.contains("google") ||
            cmd.contains("गूगल")
        ) {
            openWebsite(
                "https://www.google.com",
                "Google khol rahi hoon."
            )
            return
        }

        // CAMERA
        if (
            cmd.contains("camera") ||
            cmd.contains("कैमरा")
        ) {
            openCamera()
            return
        }

        // SETTINGS
        if (
            cmd.contains("settings") ||
            cmd.contains("setting") ||
            cmd.contains("सेटिंग") ||
            cmd.contains("सेटिंग्स")
        ) {
            openSettings()
            return
        }

        // TIME
        if (
            cmd.contains("time") ||
            cmd.contains("samay") ||
            cmd.contains("टाइम") ||
            cmd.contains("समय") ||
            cmd.contains("कितने बजे")
        ) {

            val time = SimpleDateFormat(
                "hh:mm a",
                Locale.getDefault()
            ).format(Date())

            respond("Abhi time hai $time.")
            return
        }

        // DATE
        if (
            cmd.contains("date") ||
            cmd.contains("today") ||
            cmd.contains("tarikh") ||
            cmd.contains("तारीख") ||
            cmd.contains("आज की तारीख")
        ) {

            val date = SimpleDateFormat(
                "dd MMMM yyyy",
                Locale("hi", "IN")
            ).format(Date())

            respond("Aaj ki tareekh hai $date.")
            return
        }

        // UNKNOWN
        respond(
            "Command mili: $original. " +
                    "Is command ki capability abhi available nahi hai."
        )
    }

    // ==================================================
    // CHROME
    // ==================================================

    private fun openChrome() {

        try {

            val chromeIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com")
            )

            // Directly Chrome ko target karo
            chromeIntent.setPackage("com.android.chrome")

            startActivity(chromeIntent)

            respond("Chrome khol rahi hoon.")

        } catch (e: Exception) {

            // Agar Chrome available nahi hai
            try {

                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com")
                )

                startActivity(browserIntent)

                respond(
                    "Chrome available nahi mila, " +
                            "available browser khol rahi hoon."
                )

            } catch (e2: Exception) {

                respond(
                    "Koi browser open nahi ho saka."
                )
            }
        }
    }

    // ==================================================
    // CAMERA
    // ==================================================

    private fun openCamera() {

        try {

            val cameraIntent = Intent(
                MediaStore.ACTION_IMAGE_CAPTURE
            )

            startActivity(cameraIntent)

            respond("Camera khol rahi hoon.")

        } catch (e: Exception) {

            respond("Camera open nahi ho saka.")
        }
    }

    // ==================================================
    // SETTINGS
    // ==================================================

    private fun openSettings() {

        try {

            val settingsIntent = Intent(
                Settings.ACTION_SETTINGS
            )

            startActivity(settingsIntent)

            respond("Settings khol rahi hoon.")

        } catch (e: Exception) {

            respond("Settings open nahi ho saki.")
        }
    }

    // ==================================================
    // WEBSITE
    // ==================================================

    private fun openWebsite(
        url: String,
        message: String
    ) {

        try {

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )

            startActivity(intent)

            respond(message)

        } catch (e: Exception) {

            respond("Website open nahi ho saki.")
        }
    }

    // ==================================================
    // VOICE COMMAND
    // ==================================================

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

            startActivityForResult(
                intent,
                100
            )

        } catch (e: Exception) {

            respond(
                "Voice input available nahi hai."
            )
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode == 100 &&
            resultCode == RESULT_OK
        ) {

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

    // ==================================================
    // GULSHAN RESPONSE
    // ==================================================

    private fun respond(message: String) {

        statusText.text = message

        resultText.text =
            "🤖 GULSHAN:\n$message"

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()

        speak(message)
    }

    // ==================================================
    // TEXT TO SPEECH
    // ==================================================

    private fun speak(message: String) {

        try {

            tts.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "GULSHAN_RESPONSE"
            )

        } catch (e: Exception) {
            // Ignore TTS error
        }
    }

    // ==================================================
    // DESTROY
    // ==================================================

    override fun onDestroy() {

        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }

        super.onDestroy()
    }
}
