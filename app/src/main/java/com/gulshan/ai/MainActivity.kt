package com.gulshan.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var resultText: TextView
    private lateinit var commandInput: EditText

    private val voiceRequest = 1001
    private val permissionRequest = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        buildUI()

        requestBasicPermissions()

        val backgroundCommand =
            intent.getStringExtra("background_command")

        if (!backgroundCommand.isNullOrBlank()) {
            executeCommand(backgroundCommand)
        }
    }

    private fun buildUI() {

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(30, 30, 30, 30)

        val title = TextView(this)
        title.text = "GULSHAN AI"
        title.textSize = 30f

        val greeting = TextView(this)
        greeting.text = "Namaste! Main Gulshan hoon."
        greeting.textSize = 18f

        val commandInput = EditText(this)
        this.commandInput = commandInput
        commandInput.hint = "Command likho..."

        val executeButton = Button(this)
        executeButton.text = "COMMAND CHALAO"

        val voiceButton = Button(this)
        voiceButton.text = "🎤 VOICE COMMAND"

        val backgroundButton = Button(this)
        backgroundButton.text = "🎙️ BACKGROUND VOICE ON"

        val stopBackgroundButton = Button(this)
        stopBackgroundButton.text = "🔇 BACKGROUND VOICE OFF"

        resultText = TextView(this)
        resultText.text = "Ready..."
        resultText.textSize = 17f

        executeButton.setOnClickListener {
            executeCommand(commandInput.text.toString())
        }

        voiceButton.setOnClickListener {
            startVoiceCommand()
        }

        backgroundButton.setOnClickListener {
            startBackgroundVoice()
        }

        stopBackgroundButton.setOnClickListener {
            stopBackgroundVoice()
        }

        layout.addView(title)
        layout.addView(greeting)
        layout.addView(commandInput)
        layout.addView(executeButton)
        layout.addView(voiceButton)
        layout.addView(backgroundButton)
        layout.addView(stopBackgroundButton)
        layout.addView(resultText)

        setContentView(layout)
    }    private fun requestBasicPermissions() {

        val permissions = ArrayList<String>()

        if (Build.VERSION.SDK_INT >= 33) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        permissions.add(Manifest.permission.RECORD_AUDIO)
        permissions.add(Manifest.permission.CAMERA)

        val needPermissions = permissions.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }

        if (needPermissions.isNotEmpty()) {
            requestPermissions(
                needPermissions.toTypedArray(),
                permissionRequest
            )
        }
    }

    private fun normalize(command: String): String {

        return command
            .lowercase(Locale.getDefault())
            .replace(",", " ")
            .replace(".", " ")
            .replace("!", " ")
            .replace("?", " ")
            .replace("।", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun executeCommand(rawCommand: String) {

        var command = normalize(rawCommand)

        if (command.isEmpty()) {
            respond("Command batao.")
            return
        }

        if (command.contains("gulshan")) {
            command = command.replace("gulshan", "").trim()
        }

        when {

            command == "hello" ||
            command == "hi" ||
            command.contains("namaste") -> {

                respond("Namaste! Main Gulshan hoon.")
            }

            command.contains("youtube") -> {
                openWebsite("https://www.youtube.com")
                respond("YouTube khol raha hoon.")
            }

            command.contains("chrome") ||
            command.contains("crom") ||
            command.contains("chrom") ||
            command.contains("krom") ||
            command.contains("क्रोम") -> {

                openChrome()
            }

            command == "camera" ||
            command.contains("camera kholo") ||
            command.contains("camera open") -> {

                openCamera()
            }

            command.contains("settings") ||
            command.contains("setting kholo") -> {

                openSettings()
            }

            command == "google" ||
            command.contains("google kholo") -> {

                openWebsite("https://www.google.com")
                respond("Google khol raha hoon.")
            }

            command.startsWith("google search") ||
            command.startsWith("google par") ||
            command.startsWith("search") ||
            command.startsWith("सर्च") -> {

                var query = command
                    .replaceFirst("google search", "")
                    .replaceFirst("google par", "")
                    .replaceFirst("search", "")
                    .replaceFirst("सर्च", "")
                    .trim()

                if (query.isEmpty()) {
                    respond("Kya search karna hai?")
                } else {
                    googleSearch(query)
                }
            }

            command.contains("time") ||
            command.contains("samay") ||
            command.contains("समय") -> {

                val time = SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                ).format(Date())

                respond("Abhi time $time hai.")
            }

            command.contains("date") ||
            command.contains("tarikh") ||
            command.contains("तारीख") -> {

                val date = SimpleDateFormat(
                    "dd MMMM yyyy",
                    Locale("hi", "IN")
                ).format(Date())

                respond("Aaj $date hai.")
            }            command.contains("flashlight on") ||
            command.contains("torch on") ||
            command.contains("torch chalu") ||
            command.contains("flash on") -> {

                setFlashlight(true)
            }

            command.contains("flashlight off") ||
            command.contains("torch off") ||
            command.contains("torch band") ||
            command.contains("flash off") -> {

                setFlashlight(false)
            }

            command.startsWith("call ") ||
            command.startsWith("phone ") ||
            command.startsWith("कॉल ") -> {

                val number = command
                    .replaceFirst("call ", "")
                    .replaceFirst("phone ", "")
                    .replaceFirst("कॉल ", "")
                    .replace(" ", "")
                    .trim()

                if (number.isEmpty()) {
                    respond("Kis number par call karna hai?")
                } else {
                    callNumber(number)
                }
            }

            command.startsWith("sms ") ||
            command.startsWith("message ") ||
            command.startsWith("मैसेज ") -> {

                val message = command
                    .replaceFirst("sms ", "")
                    .replaceFirst("message ", "")
                    .replaceFirst("मैसेज ", "")
                    .trim()

                if (message.isEmpty()) {
                    respond("SMS ka message batao.")
                } else {
                    sendSms(message)
                }
            }

            command.contains("background voice on") ||
            command.contains("background voice chalu") ||
            command.contains("background voice start") ||
            command.contains("background listening on") -> {

                startBackgroundVoice()
            }

            command.contains("background voice off") ||
            command.contains("background voice band") ||
            command.contains("background voice stop") -> {

                stopBackgroundVoice()
            }

            command.contains("update gulshan") ||
            command.contains("gulshan update") ||
            command == "update" -> {

                openUpdatePage()
            }

            command.contains("app band") ||
            command.contains("app close") ||
            command.contains("exit") ||
            command.contains("बंद करो") -> {

                stopBackgroundVoice()
                respond("Gulshan band ho raha hai.")
                finishAndRemoveTask()
            }

            command.contains("call allow") -> {

                getSharedPreferences("gulshan", MODE_PRIVATE)
                    .edit()
                    .putBoolean("allow_call", true)
                    .apply()

                respond("Call command allow kar diya.")
            }

            command.contains("call band") ||
            command.contains("call deny") -> {

                getSharedPreferences("gulshan", MODE_PRIVATE)
                    .edit()
                    .putBoolean("allow_call", false)
                    .apply()

                respond("Call command band kar diya.")
            }

            command.contains("sms allow") -> {

                getSharedPreferences("gulshan", MODE_PRIVATE)
                    .edit()
                    .putBoolean("allow_sms", true)
                    .apply()

                respond("SMS command allow kar diya.")
            }

            command.contains("sms band") ||
            command.contains("sms deny") -> {

                getSharedPreferences("gulshan", MODE_PRIVATE)
                    .edit()
                    .putBoolean("allow_sms", false)
                    .apply()

                respond("SMS command band kar diya.")
            }            command.startsWith("yaad rakho ") ||
            command.startsWith("remember ") -> {

                learnCommand(command)
            }

            else -> {

                runLearnedCommand(command)
            }
        }
    }

    private fun openChrome() {

        try {

            val intent = packageManager
                .getLaunchIntentForPackage(
                    "com.android.chrome"
                )

            if (intent != null) {

                startActivity(intent)

                respond("Chrome khol raha hoon.")

            } else {

                openWebsite(
                    "https://www.google.com"
                )

                respond(
                    "Chrome available nahi mila, browser khol raha hoon."
                )
            }

        } catch (e: Exception) {

            respond("Chrome nahi khul saka.")
        }
    }

    private fun openCamera() {

        try {

            val intent = Intent(
                "android.media.action.IMAGE_CAPTURE"
            )

            startActivity(intent)

            respond("Camera khol raha hoon.")

        } catch (e: Exception) {

            respond("Camera nahi khul saka.")
        }
    }

    private fun openSettings() {

        try {

            startActivity(
                Intent(Settings.ACTION_SETTINGS)
            )

            respond("Settings khol raha hoon.")

        } catch (e: Exception) {

            respond("Settings nahi khul saka.")
        }
    }

    private fun openWebsite(url: String) {

        try {

            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )

        } catch (e: Exception) {

            respond("Website nahi khul saki.")
        }
    }

    private fun googleSearch(query: String) {

        val url =
            "https://www.google.com/search?q=" +
                    Uri.encode(query)

        openWebsite(url)

        respond(
            "Google par $query search kar raha hoon."
        )
    }

    private fun callNumber(number: String) {

        val allowed = getSharedPreferences(
            "gulshan",
            MODE_PRIVATE
        ).getBoolean(
            "allow_call",
            false
        )

        if (!allowed) {

            respond(
                "Call command abhi band hai. Pehle bolo: call allow."
            )

            return
        }

        try {

            val intent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:$number")
            )

            startActivity(intent)

            respond("Dialer khol raha hoon.")

        } catch (e: Exception) {

            respond(
                "Call screen nahi khul saki."
            )
        }
    }

    private fun sendSms(message: String) {

        val allowed = getSharedPreferences(
            "gulshan",
            MODE_PRIVATE
        ).getBoolean(
            "allow_sms",
            false
        )

        if (!allowed) {

            respond(
                "SMS command abhi band hai. Pehle bolo: SMS allow."
            )

            return
        }

        try {

            val intent = Intent(
                Intent.ACTION_SENDTO,
                Uri.parse("smsto:")
            )

            intent.putExtra(
                "sms_body",
                message
            )

            startActivity(intent)

            respond(
                "SMS screen khol raha hoon."
            )

        } catch (e: Exception) {

            respond(
                "SMS screen nahi khul saki."
            )
        }
    }    private fun setFlashlight(enabled: Boolean) {

        try {

            val cameraManager =
                getSystemService(
                    CAMERA_SERVICE
                ) as CameraManager

            val cameraId =
                cameraManager.cameraIdList.firstOrNull()

            if (cameraId == null) {

                respond(
                    "Torch available nahi hai."
                )

                return
            }

            cameraManager.setTorchMode(
                cameraId,
                enabled
            )

            if (enabled) {

                respond(
                    "Flashlight ON."
                )

            } else {

                respond(
                    "Flashlight OFF."
                )
            }

        } catch (e: Exception) {

            respond(
                "Flashlight control ke liye camera permission chahiye."
            )
        }
    }

    private fun startVoiceCommand() {

        try {

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
                "Gulshan ko command bolo..."
            )

            startActivityForResult(
                intent,
                voiceRequest
            )

        } catch (e: Exception) {

            respond(
                "Voice recognition available nahi hai."
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
            requestCode == voiceRequest &&
            resultCode == RESULT_OK
        ) {

            val results =
                data?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )

            val command =
                results?.firstOrNull()

            if (!command.isNullOrEmpty()) {

                commandInput.setText(
                    command
                )

                executeCommand(
                    command
                )
            }
        }
    }

    private fun startBackgroundVoice() {

        if (
            Build.VERSION.SDK_INT >= 23 &&
            checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ),
                permissionRequest
            )

            respond(
                "Pehle microphone permission allow karo."
            )

            return
        }

        val intent = Intent(
            this,
            VoiceService::class.java
        )

        if (Build.VERSION.SDK_INT >= 26) {

            startForegroundService(
                intent
            )

        } else {

            startService(
                intent
            )
        }

        respond(
            "Background voice ON. Ab bolo: Gulshan..."
        )
    }

    private fun stopBackgroundVoice() {

        stopService(
            Intent(
                this,
                VoiceService::class.java
            )
        )

        respond(
            "Background voice OFF."
        )
    }    private fun openUpdatePage() {

        openWebsite(
            "https://github.com/kabirmalik1432-jpg/hamzaad-ai/releases"
        )

        respond(
            "Gulshan update page khol raha hoon."
        )
    }

    private fun learnCommand(command: String) {

        val text = command
            .replaceFirst("yaad rakho ", "")
            .replaceFirst("remember ", "")
            .trim()

        val parts = text.split("=")

        if (parts.size != 2) {

            respond(
                "Format bolo: yaad rakho mera youtube = youtube kholo"
            )

            return
        }

        val phrase =
            normalize(parts[0])

        val action =
            normalize(parts[1])

        getSharedPreferences(
            "gulshan_commands",
            MODE_PRIVATE
        )
            .edit()
            .putString(
                phrase,
                action
            )
            .apply()

        respond(
            "Command yaad rakh li."
        )
    }

    private fun runLearnedCommand(
        command: String
    ) {

        val prefs =
            getSharedPreferences(
                "gulshan_commands",
                MODE_PRIVATE
            )

        val learned =
            prefs.getString(
                command,
                null
            )

        if (learned != null) {

            executeCommand(
                learned
            )

        } else {

            respond(
                "Ye command abhi available nahi hai."
            )
        }
    }

    private fun respond(
        message: String
    ) {

        resultText.text =
            "Gulshan: $message"

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()

        if (::tts.isInitialized) {

            tts.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "GULSHAN_RESPONSE"
            )
        }
    }

    override fun onInit(
        status: Int
    ) {

        if (
            status ==
            TextToSpeech.SUCCESS
        ) {

            tts.language =
                Locale(
                    "hi",
                    "IN"
                )

            tts.setSpeechRate(
                0.92f
            )

            tts.setPitch(
                1.08f
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
