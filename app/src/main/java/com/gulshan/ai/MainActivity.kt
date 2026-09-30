package com.gulshan.ai

import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeech
    private lateinit var statusText: TextView
    private lateinit var resultText: TextView
    private lateinit var commandBox: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Text To Speech
        tts = TextToSpeech(this) {
            if (it == TextToSpeech.SUCCESS) {
                tts.language = Locale("hi", "IN")
            }
        }

        // Main Layout
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
        }

        // Title
        val title = TextView(this).apply {
            text = "GULSHAN AI"
            textSize = 32f
        }

        // Greeting
        statusText = TextView(this).apply {
            text = "Namaste! Main Gulshan hoon."
            textSize = 20f
            setPadding(0, 30, 0, 20)
        }

        // Command Box
        commandBox = EditText(this).apply {
            hint = "Command likho..."
            textSize = 18f
            
        }setSingleLine(true)

        // Command Button
        val commandButton = Button(this).apply {
            text = "COMMAND CHALAO"
            textSize = 17f
        }

        // Voice Button
        val voiceButton = Button(this).apply {
            text = "🎤 VOICE COMMAND"
            textSize = 17f
        }

        // Result Box
        resultText = TextView(this).apply {
            text = "Result yahan dikhega."
            textSize = 19f
            setPadding(0, 35, 0, 20)
            setTextColor(Color.DKGRAY)
        }

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(commandBox)
        layout.addView(commandButton)
        layout.addView(voiceButton)
        layout.addView(resultText)

        setContentView(layout)

        // COMMAND CHALAO
        commandButton.setOnClickListener {

            val command = commandBox.text.toString().trim()

            if (command.isEmpty()) {

                statusText.text = "⚠️ Command nahi mili."
                resultText.text = "Pehle command likho."

                Toast.makeText(
                    this,
                    "Pehle command likho",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                executeCommand(command)
            }
        }

        // VOICE COMMAND
        voiceButton.setOnClickListener {
            startVoiceInput()
        }
    }

    // -------------------------
    // VOICE INPUT
    // -------------------------

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

            statusText.text = "❌ Voice input available nahi hai."

            resultText.text =
                "Phone mein voice recognition available nahi hai."

        }
    }

    // -------------------------
    // VOICE RESULT
    // -------------------------

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

            val command =
                results?.firstOrNull()

            if (!command.isNullOrEmpty()) {

                commandBox.setText(command)

                executeCommand(command)

            } else {

                statusText.text =
                    "❌ Voice command nahi mili."

                resultText.text =
                    "Dobara voice command bolo."
            }
        }
    }

    // -------------------------
    // COMMAND ENGINE
    // -------------------------

    private fun executeCommand(command: String) {

        val originalCommand = command.trim()

        val cmd = originalCommand
            .lowercase(Locale.getDefault())
            .replace(".", "")
            .replace(",", "")
            .replace("!", "")
            .replace("?", "")
            .trim()

        // COMMAND SCREEN PAR DIKHAO
        statusText.text =
            "✅ Command mili: $originalCommand"

        resultText.text =
            "Gulshan process kar raha hai..."

        // HELLO COMMANDS
        if (
            cmd == "hello" ||
            cmd == "hi" ||
            cmd == "hello gulshan" ||
            cmd == "hi gulshan" ||
            cmd.contains("hello gulshan") ||
            cmd.contains("hello main gulshan") ||
            cmd.contains("hello me gulshan") ||
            cmd.contains("हेलो गुलशन") ||
            cmd.contains("हेलो मैं गुलशन") ||
            cmd.contains("नमस्ते गुलशन")
        ) {

            val response =
                "Hello! Main Gulshan hoon. Command mili."

            resultText.text =
                "🤖 GULSHAN:\n$response"

            speak(response)

            Toast.makeText(
                this,
                "Command successfully mili",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        // GULSHAN KO BULANE WALI COMMAND
        if (
            cmd.contains("gulshan") ||
            cmd.contains("गुलशन")
        ) {

            val response =
                "Ji, main Gulshan hoon. Aapki command mili: $originalCommand"

            resultText.text =
                "🤖 GULSHAN:\n$response"

            speak(response)

            Toast.makeText(
                this,
                "Command mili",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        // UNKNOWN COMMAND
        val response =
            "Command mili: $originalCommand. Is command ka action abhi set nahi hai."

        resultText.text =
            "🤖 GULSHAN:\n$response"

        speak(response)

        Toast.makeText(
            this,
            "Command mili",
            Toast.LENGTH_SHORT
        ).show()
    }

    // -------------------------
    // SPEAK
    // -------------------------

    private fun speak(text: String) {

        if (::tts.isInitialized) {

            tts.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "gulshan_command"
            )
        }
    }

    // -------------------------
    // CLOSE
    // -------------------------

    override fun onDestroy() {

        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }

        super.onDestroy()
    }
}
