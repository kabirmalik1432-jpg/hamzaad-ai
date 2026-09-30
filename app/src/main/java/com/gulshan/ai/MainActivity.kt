package com.gulshan.ai

import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.content.Intent
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeech
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) {
            tts.language = Locale("hi", "IN")
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
        }

        val title = TextView(this).apply {
            text = "GULSHAN AI"
            textSize = 32f
        }

        statusText = TextView(this).apply {
            text = "Namaste! Main Gulshan hoon."
            textSize = 20f
            setPadding(0, 30, 0, 30)
        }

        val commandBox = EditText(this).apply {
            hint = "Command likho..."
            textSize = 18f
        }

        val commandButton = Button(this).apply {
            text = "COMMAND CHALAO"
        }

        val voiceButton = Button(this).apply {
            text = "🎤 VOICE COMMAND"
        }

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(commandBox)
        layout.addView(commandButton)
        layout.addView(voiceButton)

        setContentView(layout)

        commandButton.setOnClickListener {
            val command = commandBox.text.toString().trim()

            if (command.isNotEmpty()) {
                executeCommand(command)
            } else {
                statusText.text = "Pehle command bolo ya likho."
            }
        }

        voiceButton.setOnClickListener {
            startVoiceInput()
        }
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            "hi-IN"
        )

        try {
            startActivityForResult(intent, 100)
        } catch (e: Exception) {
            statusText.text = "Voice input available nahi hai."
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            val results = data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )

            val command = results?.firstOrNull()

            if (!command.isNullOrEmpty()) {
                executeCommand(command)
            }
        }
    }

    private fun executeCommand(command: String) {

    val cmd = command.trim().lowercase()

    statusText.text = "Command: $command"

    when {
        cmd == "hello gulshan" || cmd == "hello" -> {
            tts.speak(
                "Hello! Main Gulshan hoon. Command mili.",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "gulshan"
            )
        }

        cmd.contains("gulshan") -> {
            tts.speak(
                "Ji, command mili: $command",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "gulshan"
            )
        }

        else -> {
            tts.speak(
                "Command mili: $command. Abhi is command ka action set nahi hai.",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "gulshan"
            )
        }
    }
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
