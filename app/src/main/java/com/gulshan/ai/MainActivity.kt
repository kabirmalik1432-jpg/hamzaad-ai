package com.gulshan.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.BatteryManager
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

    private var tts: TextToSpeech? = null

    private lateinit var resultText: TextView
    private lateinit var commandInput: EditText

    private val voiceRequest = 1001
    private val permissionRequest = 1002

    private val prefsName = "gulshan_ai"
    private val historyKey = "command_history"
    private val learnedKey = "learned_commands"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        buildUI()
        requestBasicPermissions()
        handleBackgroundCommand(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleBackgroundCommand(intent)
    }

    private fun handleBackgroundCommand(intent: Intent?) {
        val command = intent?.getStringExtra("background_command")

        if (!command.isNullOrBlank()) {
            executeCommand(command)
            intent.removeExtra("background_command")
        }
    }

    private fun buildUI() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(30, 30, 30, 30)

        val title = TextView(this)
        title.text = "GULSHAN AI V4"
        title.textSize = 30f

        val greeting = TextView(this)
        greeting.text = "Namaste! Main Gulshan hoon."
        greeting.textSize = 18f

        commandInput = EditText(this)
        commandInput.hint = "Command likho..."

        val executeButton = Button(this)
        executeButton.text = "COMMAND CHALAO"

        val voiceButton = Button(this)
        voiceButton.text = "🎤 VOICE COMMAND"

        val backgroundButton = Button(this)
        backgroundButton.text = "🎙️ BACKGROUND VOICE ON"

        val stopBackgroundButton = Button(this)
        stopBackgroundButton.text = "🔇 BACKGROUND VOICE OFF"

        val historyButton = Button(this)
        historyButton.text = "📜 COMMAND HISTORY"

        val statusButton = Button(this)
        statusButton.text = "📊 GULSHAN STATUS"

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

        historyButton.setOnClickListener {
            showHistory()
        }

        statusButton.setOnClickListener {
            showStatus()
        }

        layout.addView(title)
        layout.addView(greeting)
        layout.addView(commandInput)
        layout.addView(executeButton)
        layout.addView(voiceButton)
        layout.addView(backgroundButton)
        layout.addView(stopBackgroundButton)
        layout.addView(historyButton)
        layout.addView(statusButton)
        layout.addView(resultText)

        setContentView(layout)
    }

    private fun requestBasicPermissions() {
        val permissions = ArrayList<String>()

        if (Build.VERSION.SDK_INT >= 33) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        permissions.add(Manifest.permission.RECORD_AUDIO)
        permissions.add(Manifest.permission.CAMERA)

        val needed = permissions.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isNotEmpty()) {
            requestPermissions(
                needed.toTypedArray(),
                permissionRequest
            )
        }
    }

    private fun normalize(command: String): String {
        return command
            .lowercase(Locale.getDefault())
            .replace("गुलशन जी", "gulshan")
            .replace("गुलशन", "gulshan")
            .replace("हेलो", "hello")
            .replace("हैलो", "hello")
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
            command = command
                .replace("gulshan", "")
                .trim()
        }

        if (command.isEmpty()) {
            respond("Namaste! Main Gulshan hoon.")
            return
        }

        saveHistory(command)

        when {

            command == "hello" ||
            command == "hi" ||
            command.contains("namaste") -> {
                respond("Namaste! Main Gulshan hoon.")
            }

            command.contains("youtube") -> {
                openAppOrWebsite(
                    "com.google.android.youtube",
                    "https://www.youtube.com",
                    "YouTube"
                )
            }

            command.contains("chrome") ||
            command.contains("crom") ||
            command.contains("chrom") ||
            command.contains("krom") -> {
                openChrome()
            }

            command == "camera" ||
            command.contains("camera kholo") ||
            command.contains("camera open") ||
            command.contains("कैमरा") -> {
                openCamera()
            }

            command.contains("settings") ||
            command.contains("setting kholo") ||
            command.contains("सेटिंग") -> {
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

                val query = command
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

            command.contains("battery") ||
            command.contains("बैटरी") -> {
                showBattery()
            }

            command.contains("time") ||
            command.contains("samay") ||
            command.contains("समय") -> {
                showTime()
            }

            command.contains("date") ||
            command.contains("tarikh") ||
            command.contains("तारीख") -> {
                showDate()
            }

            command.contains("history") ||
            command.contains("इतिहास") -> {
                showHistory()
            }

            command.contains("clear history") ||
            command.contains("history clear") -> {
                clearHistory()
            }

            command.startsWith("yaad rakho ") ||
            command.startsWith("remember ") ||
            command.startsWith("याद रखो ") -> {
                learnCommand(command)
            }

            command.startsWith("call ") ||
            command.startsWith("phone ") -> {
                val number = command
                    .replaceFirst("call ", "")
                    .replaceFirst("phone ", "")
                    .trim()

                if (number.isEmpty()) {
                    respond("Number batao.")
                } else {
                    callNumber(number)
                }
            }

            command.startsWith("sms ") ||
            command.startsWith("message ") -> {
                val message = command
                    .replaceFirst("sms ", "")
                    .replaceFirst("message ", "")
                    .trim()

                if (message.isEmpty()) {
                    respond("Message batao.")
                } else {
                    sendSms(message)
                }
            }

            command.contains("play store") ||
            command.contains("playstore") -> {
                openPlayStore()
            }

            command.contains("wifi") -> {
                openWifiSettings()
            }

            command.contains("bluetooth") -> {
                openBluetoothSettings()
            }

            command.contains("app info") -> {
                openAppInfo()
            }

            command.contains("permission") -> {
                openAppPermissions()
            }

            command.contains("status") -> {
                showStatus()
            }

            command.contains("background voice on") ||
            command.contains("background voice start") -> {
                startBackgroundVoice()
            }

            command.contains("background voice off") ||
            command.contains("background voice stop") -> {
                stopBackgroundVoice()
            }

            else -> {
                val learned = findLearnedCommand(command)

                if (learned != null) {
                    respond(learned)
                } else {
                    respond("Command samajh nahi aaya.")
                }
            }
        }
    }

    private fun startVoiceCommand() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

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
            "Command bolo..."
        )

        try {
            startActivityForResult(intent, voiceRequest)
        } catch (e: Exception) {
            respond("Voice recognition available nahi hai.")
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == voiceRequest && resultCode == RESULT_OK) {

            val results =
                data?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )

            val command = results?.firstOrNull()

            if (!command.isNullOrBlank()) {
                commandInput.setText(command)
                executeCommand(command)
            }
        }
    }

    private fun startBackgroundVoice() {
        try {
            val intent = Intent(this, VoiceService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }

            respond("Background voice ON.")
        } catch (e: Exception) {
            respond("Background voice start nahi ho saka.")
        }
    }

    private fun stopBackgroundVoice() {
        try {
            stopService(Intent(this, VoiceService::class.java))
            respond("Background voice OFF.")
        } catch (e: Exception) {
            respond("Background voice stop nahi ho saka.")
        }
    }

    private fun openChrome() {
        openAppOrWebsite(
            "com.android.chrome",
            "https://www.google.com",
            "Chrome"
        )
    }

    private fun openCamera() {
        try {
            startActivity(Intent("android.media.action.IMAGE_CAPTURE"))
        } catch (e: Exception) {
            respond("Camera open nahi ho saka.")
        }
    }

    private fun openSettings() {
        try {
            startActivity(
                Intent(Settings.ACTION_SETTINGS)
            )
        } catch (e: Exception) {
            respond("Settings open nahi hui.")
        }
    }

    private fun openWifiSettings() {
        try {
            startActivity(
                Intent(Settings.ACTION_WIFI_SETTINGS)
            )
        } catch (e: Exception) {
            respond("WiFi settings open nahi hui.")
        }
    }

    private fun openBluetoothSettings() {
        try {
            startActivity(
                Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            )
        } catch (e: Exception) {
            respond("Bluetooth settings open nahi hui.")
        }
    }

    private fun openAppInfo() {
        try {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            )

            intent.data = Uri.parse(
                "package:$packageName"
            )

            startActivity(intent)
        } catch (e: Exception) {
            respond("App info open nahi hua.")
        }
    }

    private fun openAppPermissions() {
        try {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            )

            intent.data = Uri.parse(
                "package:$packageName"
            )

            startActivity(intent)
        } catch (e: Exception) {
            respond("Permission settings open nahi hui.")
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
            respond("Website open nahi hui.")
        }
    }

    private fun openAppOrWebsite(
        packageName: String,
        url: String,
        name: String
    ) {
        try {
            val launchIntent =
                packageManager.getLaunchIntentForPackage(packageName)

            if (launchIntent != null) {
                startActivity(launchIntent)
                respond("$name khol raha hoon.")
            } else {
                openWebsite(url)
            }

        } catch (e: Exception) {
            openWebsite(url)
        }
    }

    private fun googleSearch(query: String) {
        val url =
            "https://www.google.com/search?q=" +
            Uri.encode(query)

        openWebsite(url)
    }

    private fun openPlayStore() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "market://details?id=$packageName"
                    )
                )
            )
        } catch (e: Exception) {
            openWebsite(
                "https://play.google.com/store"
            )
        }
    }

    private fun callNumber(number: String) {
        try {
            val intent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:${Uri.encode(number)}")
            )

            startActivity(intent)
            respond("Dialer khol raha hoon.")
        } catch (e: Exception) {
            respond("Dialer open nahi hua.")
        }
    }

    private fun sendSms(message: String) {
        try {
            val intent = Intent(
                Intent.ACTION_SENDTO
            )

            intent.data = Uri.parse("smsto:")

            intent.putExtra(
                "sms_body",
                message
            )

            startActivity(intent)
            respond("SMS screen khol raha hoon.")
        } catch (e: Exception) {
            respond("SMS screen open nahi hui.")
        }
    }

    private fun showBattery() {
        val manager =
            getSystemService(BATTERY_SERVICE)
                    as BatteryManager

        val level =
            manager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CAPACITY
            )

        respond("Battery $level percent hai.")
    }

    private fun showTime() {
        val time =
            SimpleDateFormat(
                "hh:mm a",
                Locale.getDefault()
            ).format(Date())

        respond("Abhi time $time hai.")
    }

    private fun showDate() {
        val date =
            SimpleDateFormat(
                "dd MMMM yyyy",
                Locale.getDefault()
            ).format(Date())

        respond("Aaj ki date $date hai.")
    }

    private fun saveHistory(command: String) {

        val prefs =
            getSharedPreferences(
                prefsName,
                MODE_PRIVATE
            )

        val history =
            prefs.getStringSet(
                historyKey,
                emptySet()
            )?.toMutableSet()
                ?: mutableSetOf()

        val stamp =
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(Date())

        history.add("$stamp|$command")

        prefs.edit()
            .putStringSet(
                historyKey,
                history
            )
            .apply()
    }

    private fun showHistory() {

        val prefs =
            getSharedPreferences(
                prefsName,
                MODE_PRIVATE
            )

        val history =
            prefs.getStringSet(
                historyKey,
                emptySet()
            )?.toList()
                ?.sortedByDescending {
                    it.substringBefore("|")
                }
                ?.take(20)
                ?: emptyList()

        if (history.isEmpty()) {
            respond("Command history empty hai.")
            return
        }

        val text =
            history.mapIndexed { index, item ->

                val command =
                    item.substringAfter(
                        "|",
                        item
                    )

                "${index + 1}. $command"

            }.joinToString("\n")

        resultText.text =
            "COMMAND HISTORY\n\n$text"

        speak("Command history screen par dikha di hai.")
    }

    private fun clearHistory() {

        getSharedPreferences(
            prefsName,
            MODE_PRIVATE
        )
            .edit()
            .remove(historyKey)
            .apply()

        respond("Command history clear kar di.")
    }

    private fun learnCommand(command: String) {

        val clean =
            command
                .replaceFirst("yaad rakho ", "")
                .replaceFirst("remember ", "")
                .replaceFirst("याद रखो ", "")
                .trim()

        if (clean.isEmpty()) {
            respond("Kya yaad rakhna hai?")
            return
        }

        val parts =
            clean.split(
                " bolo ",
                limit = 2
            )

        val key =
            parts.firstOrNull()
                ?.trim()
                .orEmpty()

        val value =
            parts.getOrNull(1)
                ?.trim()
                ?: clean

        if (key.isEmpty()) {
            respond("Command batao.")
            return
        }

        val prefs =
            getSharedPreferences(
                prefsName,
                MODE_PRIVATE
            )

        val learned =
            prefs.getStringSet(
                learnedKey,
                emptySet()
            )?.toMutableSet()
                ?: mutableSetOf()

        learned.removeAll {
            it.substringBefore("|")
                .equals(key, ignoreCase = true)
        }

        learned.add("$key|$value")

        prefs.edit()
            .putStringSet(
                learnedKey,
                learned
            )
            .apply()

        respond("Yaad rakh liya.")
    }

    private fun findLearnedCommand(
        command: String
    ): String? {

        val prefs =
            getSharedPreferences(
                prefsName,
                MODE_PRIVATE
            )

        val learned =
            prefs.getStringSet(
                learnedKey,
                emptySet()
            )
                ?: emptySet()

        for (item in learned) {

            val key =
                item.substringBefore("|")

            val value =
                item.substringAfter(
                    "|",
                    ""
                )

            if (
                command.equals(
                    key,
                    ignoreCase = true
                )
            ) {
                return value
            }
        }

        return null
    }

    private fun showStatus() {

        val batteryManager =
            getSystemService(
                BATTERY_SERVICE
            ) as BatteryManager

        val battery =
            batteryManager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CAPACITY
            )

        resultText.text =
            "GULSHAN STATUS\n\n" +
            "App: Running\n" +
            "Battery: $battery%\n" +
            "Voice: Available\n" +
            "Commands: Ready"

        speak("Gulshan status screen par dikha diya hai.")
    }

    private fun respond(message: String) {

        resultText.text = message

        speak(message)

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun speak(message: String) {

        val engine = tts ?: return

        if (!engine.isInitialized) {
            return
        }

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

            val engine = tts ?: return

            val hindiResult =
                engine.setLanguage(
                    Locale("hi", "IN")
                )

            if (
                hindiResult ==
                TextToSpeech.LANG_MISSING_DATA ||
                hindiResult ==
                TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                engine.language =
                    Locale.getDefault()
            }

            engine.setSpeechRate(0.92f)
            engine.setPitch(1.0f)
        }
    }

    override fun onDestroy() {

        tts?.let {
            if (it.isSpeaking) {
                it.stop()
            }
            it.shutdown()
        }

        tts = null

        super.onDestroy()
    }
}
