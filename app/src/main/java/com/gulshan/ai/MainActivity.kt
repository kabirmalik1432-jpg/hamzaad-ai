package com.gulshan.ai

import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.content.Intent
import android.graphics.Color
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeech
    private lateinit var statusText: TextView
    private lateinit var resultText: TextView
    private lateinit var commandBox: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) { result ->
            if (result == TextToSpeech.SUCCESS) {
                tts.language = Locale("hi", "IN")
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
        commandButton.textSize = 17f

        val voiceButton = Button(this)
        voiceButton.text = "🎤 VOICE COMMAND"
        voiceButton.textSize = 17f

        resultText = TextView(this)
        resultText.text = "Result yahan dikhega."
        resultText.textSize = 19f
        resultText.setTextColor(Color.DKGRAY)
        resultText.setPadding(0, 35, 0, 20)

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

            } else {

                statusText.text =
                    "❌ Voice command nahi mili."

                resultText.text =
                    "Dobara voice command bolo."
            }
        }
    }

    private fun executeCommand(command: String) {

        val originalCommand = command.trim()

        val cmd = originalCommand
            .lowercase(Locale.getDefault())
            .replace(".", "")
            .replace(",", "")
            .replace("!", "")
            .replace("?", "")
            .trim()

        statusText.text =
            "✅ Command mili: $originalCommand"

        resultText.text =
            "Gulshan process kar raha hai..."

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

    override fun onDestroy() {

        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }

        super.onDestroy()
    }
}
