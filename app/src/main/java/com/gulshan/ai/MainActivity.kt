package com.gulshan.ai

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
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

        val historyButton = Button(this)
        historyButton.text = "📜 COMMAND HISTORY"

        val statusButton = Button(this)
        statusButton.text = "📊 GULSHAN STATUS"

        resultText = TextView(this)
        resultText.text = "Ready..."
        resultText.textSize = 17f

        layout.addView(title)
        layout.addView(greeting)
        layout.addView(commandInput)
        layout.addView(executeButton)
        layout.addView(voiceButton)
        layout.addView(backgroundButton)
        layout.addView(historyButton)
        layout.addView(statusButton)
        layout.addView(resultText)

        setContentView(layout)

        executeButton.setOnClickListener {
            val command = commandInput.text.toString().trim()

            if (command.isNotEmpty()) {
                executeCommand(command)
            } else {
                respond("Command likho.")
            }
        }

        voiceButton.setOnClickListener {
            startVoiceCommand()
        }

        backgroundButton.setOnClickListener {
            startBackgroundVoice()
        }

        historyButton.setOnClickListener {
            showHistory()
        }

        statusButton.setOnClickListener {
            showStatus()
        }
    }

    private fun executeCommand(rawCommand: String) {

        val command = normalize(rawCommand)

        if (command.isBlank()) {
            respond("Command nahi mili.")
            return
        }

        saveHistory(rawCommand)

        // HISTORY CLEAR MUST COME BEFORE HISTORY
        if (
            command == "history clear" ||
            command == "clear history" ||
            command.contains("history clear")
        ) {
            clearHistory()
            return
        }

        if (
            command == "history" ||
            command.contains("command history") ||
            command.contains("commands history")
        ) {
            showHistory()
            return
        }

        if (
            command == "status" ||
            command.contains("gulshan status") ||
            command.contains("system status")
        ) {
            showStatus()
            return
        }

        // Greeting
        if (
            command == "hello" ||
            command == "hi" ||
            command == "hey" ||
            command.contains("hello gulshan") ||
            command.contains("hi gulshan") ||
            command.contains("namaste")
        ) {
            respond("Namaste! Main Gulshan hoon. Command batao.")
            return
        }

        // YouTube
        if (
            command.contains("youtube") ||
            command.contains("you tube")
        ) {
            openWebsite("https://www.youtube.com")
            return
        }

        // Chrome
        if (command.contains("chrome")) {
            openInstalledApp("com.android.chrome")
            return
        }

        // Camera
        if (
            command == "camera" ||
            command.contains("camera kholo") ||
            command.contains("camera open")
        ) {
            openCamera()
            return
        }

        // Settings
        if (
            command == "settings" ||
            command.contains("settings kholo") ||
            command.contains("setting kholo")
        ) {
            openSettings()
            return
        }

        // Google
        if (
            command == "google" ||
            command.contains("google kholo")
        ) {
            openWebsite("https://www.google.com")
            return
        }

        // Search
        if (
            command.startsWith("google search") ||
            command.startsWith("search google") ||
            command.startsWith("search ")
        ) {
            val query = when {
                command.startsWith("google search") ->
                    command.removePrefix("google search").trim()

                command.startsWith("search google") ->
                    command.removePrefix("search google").trim()

                else ->
                    command.removePrefix("search").trim()
            }

            if (query.isNotBlank()) {
                googleSearch(query)
            } else {
                respond("Kya search karna hai?")
            }

            return
        }

        // Website
        if (
            command.startsWith("website ") ||
            command.startsWith("site ")
        ) {
            var site = command
                .removePrefix("website ")
                .removePrefix("site ")
                .trim()

            if (!site.startsWith("http://") &&
                !site.startsWith("https://")
            ) {
                site = "https://$site"
            }

            openWebsite(site)
            return
        }

        // WhatsApp
        if (
            command.contains("whatsapp") ||
            command.contains("what's app")
        ) {
            openInstalledApp("com.whatsapp")
            return
        }

        // Instagram
        if (command.contains("instagram")) {
            openInstalledApp("com.instagram.android")
            return
        }

        // Facebook
        if (command.contains("facebook")) {
            openInstalledApp("com.facebook.katana")
            return
        }

        // Maps
        if (
            command.contains("maps") ||
            command.contains("map kholo")
        ) {
            openInstalledApp("com.google.android.apps.maps")
            return
        }

        // Gmail
        if (command.contains("gmail")) {
            openInstalledApp("com.google.android.gm")
            return
        }

        // Calendar
        if (
            command.contains("calendar") ||
            command.contains("calender")
        ) {
            openInstalledApp("com.google.android.calendar")
            return
        }

        // Play Store
        if (
            command.contains("play store") ||
            command.contains("playstore")
        ) {
            openInstalledApp("com.android.vending")
            return
        }

        // Wi-Fi
        if (
            command.contains("wifi") ||
            command.contains("wi fi")
        ) {
            openWifiSettings()
            return
        }

        // Bluetooth
        if (
            command.contains("bluetooth") ||
            command.contains("blue tooth")
        ) {
            openBluetoothSettings()
            return
        }

        // Battery
        if (
            command == "battery" ||
            command.contains("battery kitni") ||
            command.contains("battery status")
        ) {
            showBattery()
            return
        }

        // Date
        if (
            command == "date" ||
            command.contains("aaj ki date") ||
            command.contains("today date")
        ) {
            val date = SimpleDateFormat(
                "dd MMMM yyyy",
                Locale("hi", "IN")
            ).format(Date())

            respond("Aaj ki date $date hai.")
            return
        }

        // Time
        if (
            command == "time" ||
            command.contains("time kya") ||
            command.contains("samay kya")
        ) {
            val time = SimpleDateFormat(
                "hh:mm a",
                Locale.getDefault()
            ).format(Date())

            respond("Abhi time $time hai.")
            return
        }

        // Timer
        if (
            command.contains("timer")
        ) {
            startTimer(command)
            return
        }

        // Alarm
        if (
            command.contains("alarm")
        ) {
            startAlarm(command)
            return
        }

        // Flashlight ON
        if (
            command.contains("flashlight on") ||
            command.contains("torch on") ||
            command.contains("flash on") ||
            command.contains("torch chalu")
        ) {
            setFlashlight(true)
            return
        }

        // Flashlight OFF
        if (
            command.contains("flashlight off") ||
            command.contains("torch off") ||
            command.contains("flash off") ||
            command.contains("torch band")
        ) {
            setFlashlight(false)
            return
        }

        // Call permission
        if (
            command.contains("call allow") ||
            command.contains("call permission on") ||
            command.contains("call enable")
        ) {
            setPermissionLimit("allow_call", true)
            respond("Call permission ON.")
            return
        }

        if (
            command.contains("call deny") ||
            command.contains("call permission off") ||
            command.contains("call disable")
        ) {
            setPermissionLimit("allow_call", false)
            respond("Call permission OFF.")
            return
        }

        // SMS permission
        if (
            command.contains("sms allow") ||
            command.contains("sms permission on") ||
            command.contains("sms enable")
        ) {
            setPermissionLimit("allow_sms", true)
            respond("SMS permission ON.")
            return
        }

        if (
            command.contains("sms deny") ||
            command.contains("sms permission off") ||
            command.contains("sms disable")
        ) {
            setPermissionLimit("allow_sms", false)
            respond("SMS permission OFF.")
            return
        }

        // Call
        if (
            command.startsWith("call ") ||
            command.startsWith("dial ")
        ) {
            if (!getPermissionLimit("allow_call")) {
                respond("Call permission OFF hai. Pehle call allow command do.")
                return
            }

            val number = command
                .removePrefix("call ")
                .removePrefix("dial ")
                .trim()

            if (number.isNotBlank()) {
                callNumber(number)
            } else {
                respond("Kis number par call karna hai?")
            }

            return
        }

        // SMS
        if (
            command.startsWith("sms ") ||
            command.startsWith("message ")
        ) {
            if (!getPermissionLimit("allow_sms")) {
                respond("SMS permission OFF hai. Pehle SMS allow command do.")
                return
            }

            val message = command
                .removePrefix("sms ")
                .removePrefix("message ")
                .trim()

            if (message.isNotBlank()) {
                sendSms(message)
            } else {
                respond("Message kya bhejna hai?")
            }

            return
        }

        // Background voice ON
        if (
            command.contains("background voice on") ||
            command.contains("background voice chalu") ||
            command.contains("voice background on")
        ) {
            startBackgroundVoice()
            return
        }

        // Background voice OFF
        if (
            command.contains("background voice off") ||
            command.contains("background voice band") ||
            command.contains("voice background off")
        ) {
            stopBackgroundVoice()
            return
        }

        // Update
        if (
            command.contains("update") ||
            command.contains("update page")
        ) {
            openUpdatePage()
            return
        }

        // Learn command
        if (
            command.startsWith("yaad rakho") ||
            command.startsWith("remember")
        ) {
            learnCommand(command)
            return
        }

        // Exit
        if (
            command == "exit" ||
            command == "close app" ||
            command == "app close" ||
            command.contains("gulshan band ho jao")
        ) {
            respond("Theek hai.")
            return
        }

        // Learned command
        if (runLearnedCommand(command)) {
            return
        }

        respond(
            "Ye command abhi mujhe nahi aati. " +
                    "Aise sikha sakte ho: " +
                    "yaad rakho command = action"
        )
    }

    private fun normalize(text: String): String {
        return text
            .trim()
            .lowercase(Locale.getDefault())
            .replace(Regex("\\s+"), " ")
    }

    private fun openInstalledApp(packageName: String) {

        try {
            val intent =
                packageManager.getLaunchIntentForPackage(packageName)

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                respond("App khol raha hoon.")
            } else {
                respond("Ye app phone mein installed nahi hai.")
            }

        } catch (e: Exception) {
            respond("App open nahi ho saka.")
        }
    }

    private fun openCamera() {

        try {
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            startActivity(intent)
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
            respond("Wi-Fi settings open nahi hui.")
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

    private fun openWebsite(url: String) {

        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )

            startActivity(intent)

        } catch (e: Exception) {
            respond("Website open nahi hui.")
        }
    }

    private fun googleSearch(query: String) {

        val url =
            "https://www.google.com/search?q=" +
                    Uri.encode(query)

        openWebsite(url)
    }

    private fun callNumber(number: String) {

        try {

            val cleanNumber =
                number.replace(
                    Regex("[^0-9+]"),
                    ""
                )

            val intent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:$cleanNumber")
            )

            startActivity(intent)

        } catch (e: Exception) {
            respond("Dialer open nahi hua.")
        }
    }

    private fun sendSms(message: String) {

        try {

            val intent = Intent(
                Intent.ACTION_SENDTO
            )

            intent.data =
                Uri.parse("smsto:")

            intent.putExtra(
                "sms_body",
                message
            )

            startActivity(intent)

        } catch (e: Exception) {
            respond("SMS screen open nahi hui.")
        }
    }

    private fun startTimer(command: String) {

        try {

            val regex =
                Regex("(\\d+)\\s*(second|seconds|sec|minute|minutes|min|hour|hours)")

            val match =
                regex.find(command)

            if (match == null) {
                respond(
                    "Timer ke liye bolo, jaise 5 minute timer."
                )
                return
            }

            val value =
                match.groupValues[1].toLong()

            val unit =
                match.groupValues[2]

            val seconds = when {

                unit.startsWith("hour") ->
                    value * 60L * 60L

                unit.startsWith("minute") ||
                        unit.startsWith("min") ->
                    value * 60L

                else ->
                    value
            }

            val intent =
                Intent(AlarmClock.ACTION_SET_TIMER)

            intent.putExtra(
                AlarmClock.EXTRA_LENGTH,
                seconds.toInt()
            )

            intent.putExtra(
                AlarmClock.EXTRA_SKIP_UI,
                false
            )

            startActivity(intent)

            respond("$value $unit ka timer set kar raha hoon.")

        } catch (e: Exception) {
            respond("Timer set nahi ho saka.")
        }
    }

    private fun startAlarm(command: String) {

        try {

            val regex =
                Regex("(\\d{1,2})(?::|\\s)(\\d{2})\\s*(am|pm)?")

            val match =
                regex.find(command)

            if (match == null) {
                respond(
                    "Alarm ke liye time bolo, jaise 7:30 AM."
                )
                return
            }

            var hour =
                match.groupValues[1].toInt()

            val minute =
                match.groupValues[2].toInt()

            val ampm =
                match.groupValues[3]

            if (ampm.equals("pm", true) &&
                hour < 12
            ) {
                hour += 12
            }

            if (ampm.equals("am", true) &&
                hour == 12
            ) {
                hour = 0
            }

            val intent =
                Intent(AlarmClock.ACTION_SET_ALARM)

            intent.putExtra(
                AlarmClock.EXTRA_HOUR,
                hour
            )

            intent.putExtra(
                AlarmClock.EXTRA_MINUTES,
                minute
            )

            intent.putExtra(
                AlarmClock.EXTRA_MESSAGE,
                "Gulshan AI Alarm"
            )

            startActivity(intent)

            respond(
                "Alarm $hour:${
                    minute.toString().padStart(2, '0')
                } ke liye set kar raha hoon."
            )

        } catch (e: Exception) {
            respond("Alarm set nahi ho saka.")
        }
    }

    private fun setFlashlight(on: Boolean) {

        try {

            val cameraManager =
                getSystemService(
                    Context.CAMERA_SERVICE
                ) as CameraManager

            val cameraId =
                cameraManager.cameraIdList.firstOrNull()

            if (cameraId == null) {
                respond("Flashlight available nahi hai.")
                return
            }

            cameraManager.setTorchMode(
                cameraId,
                on
            )

            if (on) {
                respond("Flashlight ON.")
            } else {
                respond("Flashlight OFF.")
            }

        } catch (e: Exception) {
            respond("Flashlight control nahi ho saka.")
        }
    }

    private fun startVoiceCommand() {

        try {

            val intent =
                Intent(
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
            respond("Voice recognition available nahi hai.")
        }
    }

    @Deprecated("Android legacy callback")
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

        if (requestCode == voiceRequest &&
            resultCode == RESULT_OK
        ) {

            val results =
                data?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )

            val command =
                results?.firstOrNull()

            if (!command.isNullOrBlank()) {
                commandInput.setText(command)
                executeCommand(command)
            }
        }
    }

    private fun startBackgroundVoice() {

        try {

            val intent =
                Intent(
                    this,
                    VoiceService::class.java
                )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }

            getSharedPreferences(
                prefsName,
                MODE_PRIVATE
            )
                .edit()
                .putBoolean(
                    "background_voice",
                    true
                )
                .apply()

            respond("Background voice ON.")

        } catch (e: Exception) {
            respond(
                "Background voice start nahi ho saka."
            )
        }
    }

    private fun stopBackgroundVoice() {

        try {

            val intent =
                Intent(
                    this,
                    VoiceService::class.java
                )

            stopService(intent)

            getSharedPreferences(
                prefsName,
                MODE_PRIVATE
            )
                .edit()
                .putBoolean(
                    "background_voice",
                    false
                )
                .apply()

            respond("Background voice OFF.")

        } catch (e: Exception) {
            respond(
                "Background voice stop nahi ho saka."
            )
        }
    }

    private fun showBattery() {

        try {

            val batteryManager =
                getSystemService(
                    Context.BATTERY_SERVICE
                ) as BatteryManager

            val level =
                batteryManager.getIntProperty(
                    BatteryManager.BATTERY_PROPERTY_CAPACITY
                )

            respond(
                "Battery $level percent hai."
            )

        } catch (e: Exception) {
            respond("Battery status nahi mil saka.")
        }
    }

    private fun showStatus() {

        val prefs =
            getSharedPreferences(
                prefsName,
                MODE_PRIVATE
            )

        val background =
            prefs.getBoolean(
                "background_voice",
                false
            )

        val callAllowed =
            prefs.getBoolean(
                "allow_call",
                false
            )

        val smsAllowed =
            prefs.getBoolean(
                "allow_sms",
                false
            )

        val batteryManager =
            getSystemService(
                Context.BATTERY_SERVICE
            ) as BatteryManager

        val battery =
            batteryManager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CAPACITY
            )

        resultText.text =
            """
            GULSHAN AI STATUS
            
            Background Voice: ${
                if (background) "ON" else "OFF"
            }
            
            Call Permission: ${
                if (callAllowed) "ON" else "OFF"
            }
            
            SMS Permission: ${
                if (smsAllowed) "ON" else "OFF"
            }
            
            Battery: $battery%
            """.trimIndent()

        speak(
            "Gulshan status dikha diya hai."
        )
    }

    private fun saveHistory(command: String) {

        val prefs =
            getSharedPreferences(
                prefsName,
                MODE_PRIVATE
            )

        val old =
            prefs.getStringSet(
                historyKey,
                emptySet()
            ) ?: emptySet()

        val newHistory =
            old.toMutableSet()

        newHistory.add(
            System.currentTimeMillis()
                .toString() +
                    "|" +
                    command
        )

        while (newHistory.size > 30) {
            newHistory.remove(
                newHistory.minOrNull()
            )
        }

        prefs.edit()
            .putStringSet(
                historyKey,
                newHistory
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
            resultText.text =
                "Command history empty hai."

            speak(
                "Command history empty hai."
            )
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
            }
                .joinToString("\n")

        resultText.text =
            "COMMAND HISTORY\n\n$text"

        speak(
            "Command history screen par dikha di hai."
        )
    }

    private fun clearHistory() {

        getSharedPreferences(
            prefsName,
            MODE_PRIVATE
        )
            .edit()
            .remove(historyKey)
            .apply()

        resultText.text =
            "Command history clear ho gayi."

        speak(
            "Command history clear kar di."
        )
    }

    private fun setPermissionLimit(
        key: String,
        value: Boolean
    ) {

        getSharedPreferences(
            prefsName,
            MODE_PRIVATE
        )
            .edit()
            .putBoolean(
                key,
                value
            )
            .apply()
    }

    private fun getPermissionLimit(
        key: String
    ): Boolean {

        return getSharedPreferences(
            prefsName,
            MODE_PRIVATE
        )
            .getBoolean(
                key,
                false
            )
    }

    private fun learnCommand(command: String) {

        var text =
            command.removePrefix(
                "yaad rakho"
            ).trim()

        if (text == command) {
            text =
                command.removePrefix(
                    "remember"
                ).trim()
        }

        val parts =
            text.split(
                "=",
                limit = 2
            )

        if (parts.size != 2) {
            respond(
                "Format: yaad rakho command = action"
            )
            return
        }

        val name =
            normalize(parts[0])

        val action =
            parts[1].trim()

        if (name.isBlank() ||
            action.isBlank()
        ) {
            respond(
                "Command aur action dono likho."
            )
            return
        }

        val prefs =
            getSharedPreferences(
                prefsName,
                MODE_PRIVATE
            )

        val old =
            prefs.getStringSet(
                learnedKey,
                emptySet()
            ) ?: emptySet()

        val updated =
            old.toMutableSet()

        updated.removeAll {
            it.startsWith("$name|||")
        }

        updated.add(
            "$name|||$action"
        )

        prefs.edit()
            .putStringSet(
                learnedKey,
                updated
            )
            .apply()

        respond(
            "Theek hai. Maine $name yaad rakh liya."
        )
    }

    private fun runLearnedCommand(
        command: String
    ): Boolean {

        val prefs =
            getSharedPreferences(
                prefsName,
                MODE_PRIVATE
            )

        val learned =
            prefs.getStringSet(
                learnedKey,
                emptySet()
            ) ?: emptySet()

        val item =
            learned.firstOrNull {
                val name =
                    it.substringBefore("|||")

                normalize(name) == command
            }

        if (item == null) {
            return false
        }

        val action =
            item.substringAfter(
                "|||",
                ""
            )

        if (action.isNotBlank()) {
            executeCommand(action)
            return true
        }

        return false
    }

    private fun openUpdatePage() {

        openWebsite(
            "https://github.com/kabirmalik1432-jpg/hamzaad-ai/releases"
        )
    }

    private fun requestBasicPermissions() {

        val permissions =
            mutableListOf<String>()

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            permissions.add(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        permissions.add(
            Manifest.permission.RECORD_AUDIO
        )

        permissions.add(
            Manifest.permission.CAMERA
        )

        val needed =
            permissions.filter {
                checkSelfPermission(it) !=
                        PackageManager.PERMISSION_GRANTED
            }

        if (needed.isNotEmpty()) {

            requestPermissions(
                needed.toTypedArray(),
                permissionRequest
            )
        }
    }

    private fun respond(message: String) {

        resultText.text =
            message

        speak(message)

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun speak(message: String) {

        if (!::tts.isInitialized) {
            return
        }

        if (tts.isSpeaking) {
            tts.stop()
        }

        tts.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "gulshan_response"
        )
    }

    override fun onInit(status: Int) {

        if (status ==
            TextToSpeech.SUCCESS
        ) {

            val hindiResult =
                tts.setLanguage(
                    Locale("hi", "IN")
                )

            if (
                hindiResult ==
                TextToSpeech.LANG_MISSING_DATA ||
                hindiResult ==
                TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                tts.language =
                    Locale.getDefault()
            }

            tts.setSpeechRate(0.92f)
            tts.setPitch(1.08f)
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
