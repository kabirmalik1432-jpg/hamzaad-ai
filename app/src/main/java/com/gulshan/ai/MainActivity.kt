package com.gulshan.ai

import android.Manifest
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
            requestPermissions(needed.toTypedArray(), permissionRequest)
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

        saveHistory(command)

        when {
            command == "hello" ||
            command == "hi" ||
            command.contains("namaste") ||
            command.contains("नमस्ते") -> {
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

            command.startsWith("website ") ||
            command.startsWith("site ") ||
            command.startsWith("वेबसाइट ") -> {
                var site = command
                    .replaceFirst("website ", "")
                    .replaceFirst("site ", "")
                    .replaceFirst("वेबसाइट ", "")
                    .trim()

                if (!site.startsWith("http")) {
                    site = "https://$site"
                }

                openWebsite(site)
                respond("Website khol raha hoon.")
            }

            command.contains("whatsapp") ||
            command.contains("व्हाट्सएप") -> {
                openInstalledApp("com.whatsapp", "WhatsApp")
            }

            command.contains("instagram") ||
            command.contains("इंस्टाग्राम") -> {
                openInstalledApp("com.instagram.android", "Instagram")
            }

            command.contains("facebook") ||
            command.contains("फेसबुक") -> {
                openInstalledApp("com.facebook.katana", "Facebook")
            }

            command.contains("maps") ||
            command.contains("map") ||
            command.contains("नक्शा") -> {
                openInstalledApp("com.google.android.apps.maps", "Google Maps")
            }

            command.contains("gmail") ||
            command.contains("जीमेल") -> {
                openInstalledApp("com.google.android.gm", "Gmail")
            }

            command.contains("calendar") ||
            command.contains("कैलेंडर") -> {
                openInstalledApp("com.google.android.calendar", "Google Calendar")
            }

            command.contains("play store") ||
            command.contains("playstore") ||
            command.contains("प्ले स्टोर") -> {
                openInstalledApp("com.android.vending", "Play Store")
            }

            command.contains("wifi") ||
            command.contains("wi-fi") ||
            command.contains("वाईफाई") -> {
                openWifiSettings()
            }

            command.contains("bluetooth") ||
            command.contains("ब्लूटूथ") -> {
                openBluetoothSettings()
            }

            command.contains("battery") ||
            command.contains("battery status") ||
            command.contains("बैटरी") -> {
                showBattery()
            }

            command.contains("status") ||
            command.contains("स्थिति") ||
            command.contains("gulshan status") -> {
                showStatus()
            }

            command.contains("history") ||
            command.contains("command history") ||
            command.contains("हिस्ट्री") ||
            command.contains("इतिहास") -> {
                showHistory()            }

            command.contains("history clear") ||
            command.contains("clear history") ||
            command.contains("हिस्ट्री साफ") -> {
                clearHistory()
            }

            command.contains("time") ||
            command.contains("samay") ||
            command.contains("समय") ||
            command.contains("टाइम") -> {
                val time = SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                ).format(Date())
                respond("Abhi time $time hai.")
            }

            command.contains("date") ||
            command.contains("tarikh") ||
            command.contains("तारीख") ||
            command.contains("डेट") -> {
                val date = SimpleDateFormat(
                    "dd MMMM yyyy",
                    Locale("hi", "IN")
                ).format(Date())
                respond("Aaj $date hai.")
            }

            command.contains("timer") ||
            command.contains("टाइमर") -> {
                startTimer(command)
            }

            command.contains("alarm") ||
            command.contains("alaram") ||
            command.contains("अलार्म") -> {
                startAlarm(command)
            }

            command.contains("flashlight on") ||
            command.contains("torch on") ||
            command.contains("torch chalu") ||
            command.contains("flash on") ||
            command.contains("टॉर्च चालू") -> {
                setFlashlight(true)
            }

            command.contains("flashlight off") ||
            command.contains("torch off") ||
            command.contains("torch band") ||
            command.contains("flash off") ||
            command.contains("टॉर्च बंद") -> {
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
            command.contains("बैकग्राउंड वॉइस चालू") -> {
                startBackgroundVoice()
            }

            command.contains("background voice off") ||
            command.contains("background voice band") ||
            command.contains("background voice stop") ||
            command.contains("बैकग्राउंड वॉइस बंद") -> {
                stopBackgroundVoice()
            }

            command.contains("update gulshan") ||
            command.contains("gulshan update") ||
            command == "update" -> {
                openUpdatePage()
            }

            command.contains("call allow") -> {
                setPermissionLimit("allow_call", true)
                respond("Call command allow kar diya.")
            }

            command.contains("call band") ||
            command.contains("call deny") -> {
                setPermissionLimit("allow_call", false)
                respond("Call command band kar diya.")
            }

            command.contains("sms allow") -> {
                setPermissionLimit("allow_sms", true)
                respond("SMS command allow kar diya.")
            }

            command.contains("sms band") ||
            command.contains("sms deny") -> {
                setPermissionLimit("allow_sms", false)
                respond("SMS command band kar diya.")
            }

            command.startsWith("yaad rakho ") ||
            command.startsWith("remember ") -> {
                learnCommand(command)
            }

            command.contains("app band") ||
            command.contains("app close") ||
            command == "exit" ||
            command == "band ho jao" -> {
                stopBackgroundVoice()
                respond("Gulshan band ho raha hai.")
                finishAndRemoveTask()
            }

            else -> {
                runLearnedCommand(command)
            }
        }
    }

    private fun openChrome() {
        try {
            val intent = packageManager.getLaunchIntentForPackage("com.android.chrome")
            if (intent != null) {
                startActivity(intent)
                respond("Chrome khol raha hoon.")
            } else {
                openWebsite("https://www.google.com")
                respond("Chrome nahi mila, browser khol raha hoon.")
            }
        } catch (e: Exception) {
            respond("Chrome nahi khul saka.")
        }
    }

    private fun openInstalledApp(packageName: String, name: String) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                startActivity(intent)
                respond("$name khol raha hoon.")
            } else {
                respond("$name phone mein installed nahi hai.")
            }
        } catch (e: Exception) {
            respond("$name nahi khul saka.")
        }
    }

    private fun openAppOrWebsite(
        packageName: String,
        website: String,
        name: String
    ) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                startActivity(intent)
                respond("$name khol raha hoon.")
            } else {
                openWebsite(website)
                respond("$name ka web version khol raha hoon.")
            }
        } catch (e: Exception) {
            openWebsite(website)
        }
    }

    private fun openCamera() {
        try {
            startActivity(Intent("android.media.action.IMAGE_CAPTURE"))
            respond("Camera khol raha hoon.")
        } catch (e: Exception) {
            respond("Camera nahi khul saka.")
        }
    }

    private fun openSettings() {
        try {
            startActivity(Intent(Settings.ACTION_SETTINGS))
            respond("Settings khol raha hoon.")
        } catch (e: Exception) {
            respond("Settings nahi khul saka.")
        }
    }

    private fun openWifiSettings() {
        try {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            respond("Wi-Fi settings khol raha hoon.")
        } catch (e: Exception) {
            respond("Wi-Fi settings nahi khul saki.")
        }
    }

    private fun openBluetoothSettings() {
        try {
            startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            respond("Bluetooth settings khol raha hoon.")
        } catch (e: Exception) {
            respond("Bluetooth settings nahi khul saki.")
        }
    }

    private fun openWebsite(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            respond("Website nahi khul saki.")
        }
    }

    private fun googleSearch(query: String) {
        val url = "https://www.google.com/search?q=" + Uri.encode(query)
        openWebsite(url)
        respond("Google par $query search kar raha hoon.")
    }

    private fun startTimer(command: String) {
        val match = Regex(
            "(\\d+)\\s*(second|seconds|sec|minute|minutes|min|hour|hours|ghanta|ghante)"
        ).find(command)

        if (match == null) {
            respond("Timer ke liye bolo: 10 minute timer.")
            return
        }

        val value = match.groupValues[1].toInt()
        val unit = match.groupValues[2]

        val seconds = when {
            unit.contains("hour") ||
            unit.contains("ghanta") ||
            unit.contains("ghante") ->
                value * 3600

            unit.contains("minute") ||
            unit.contains("min") ->
                value * 60

            else -> value
        }

        try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER)
            intent.putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            startActivity(intent)
            respond("$value $unit ka timer set kar raha hoon.")
        } catch (e: Exception) {
            respond("Timer set nahi ho saka.")
        }
    }

    private fun startAlarm(command: String) {
        val match = Regex(
            "(\\d{1,2})[:.]?(\\d{2})?\\s*(am|pm)?"
        ).find(command)

        if (match == null) {
            respond("Alarm ke liye bolo: alarm 7:30 am.")
            return
        }

        var hour = match.groupValues[1].toInt()
        val minute = match.groupValues[2].ifEmpty { "0" }.toInt()
        val ampm = match.groupValues[3]

        if (ampm == "pm" && hour < 12) hour += 12
        if (ampm == "am" && hour == 12) hour = 0

        if (hour !in 0..23 || minute !in 0..59) {
            respond("Alarm ka time sahi nahi hai.")
            return
        }

        try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM)
            intent.putExtra(AlarmClock.EXTRA_HOUR, hour)
            intent.putExtra(AlarmClock.EXTRA_MINUTES, minute)
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            startActivity(intent)

            val displayHour = if (hour % 12 == 0) 12 else hour % 12
            val suffix = if (hour >= 12) "PM" else "AM"

            respond(
                "Alarm $displayHour:${minute.toString().padStart(2, '0')} $suffix ke liye khol raha hoon."
            )
        } catch (e: Exception) {
            respond("Alarm set nahi ho saka.")
        }
    }

    private fun callNumber(number: String) {
        val allowed = getSharedPreferences("gulshan", MODE_PRIVATE)
            .getBoolean("allow_call", false)

        if (!allowed) {
            respond("Call command abhi band hai. Pehle bolo: call allow.")
            return
        }

        try {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
            respond("Dialer khol raha hoon.")
        } catch (e: Exception) {
            respond("Call screen nahi khul saki.")
        }
    }

    private fun sendSms(message: String) {
        val allowed = getSharedPreferences("gulshan", MODE_PRIVATE)
            .getBoolean("allow_sms", false)

        if (!allowed) {
            respond("SMS command abhi band hai. Pehle bolo: SMS allow.")
            return
        }

        try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
            intent.putExtra("sms_body", message)
            startActivity(intent)
            respond("SMS screen khol raha hoon.")
        } catch (e: Exception) {
            respond("SMS screen nahi khul saki.")
        }
    }

    private fun setFlashlight(enabled: Boolean) {
        try {
            val manager = getSystemService(CAMERA_SERVICE) as CameraManager
            val cameraId = manager.cameraIdList.firstOrNull()

            if (cameraId == null) {
                respond("Torch available nahi hai.")
                return
            }

            manager.setTorchMode(cameraId, enabled)
            respond(if (enabled) "Flashlight ON." else "Flashlight OFF.")
        } catch (e: Exception) {
            respond("Flashlight control nahi ho saka.")
        }
    }

    private fun startVoiceCommand() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Gulshan ko command bolo..."
            )
            startActivityForResult(intent, voiceRequest)
        } catch (e: Exception) {
            respond("Voice recognition available nahi hai.")
        }
    }

    @Deprecated("Use Activity Result APIs in a future version.")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == voiceRequest && resultCode == RESULT_OK) {
            val results = data?.getStringArrayListExtra(
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
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                permissionRequest
            )
            respond("Pehle microphone permission allow karo.")
            return
        }

        getSharedPreferences("gulshan", MODE_PRIVATE)
            .edit()
            .putBoolean("background_voice_enabled", true)
            .apply()

        val intent = Intent(this, VoiceService::class.java)

        try {
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            respond("Background voice ON. Ab bolo: Gulshan...")
        } catch (e: Exception) {
            getSharedPreferences("gulshan", MODE_PRIVATE)
                .edit()
                .putBoolean("background_voice_enabled", false)
                .apply()
            respond("Background voice start nahi ho saki.")
        }
    }

    private fun stopBackgroundVoice() {
        stopService(Intent(this, VoiceService::class.java))

        getSharedPreferences("gulshan", MODE_PRIVATE)
            .edit()
            .putBoolean("background_voice_enabled", false)
            .apply()

        respond("Background voice OFF.")
    }    private fun showStatus() {
        val prefs = getSharedPreferences("gulshan", MODE_PRIVATE)

        val backgroundOn =
            prefs.getBoolean("background_voice_enabled", false)

        val callAllowed =
            prefs.getBoolean("allow_call", false)

        val smsAllowed =
            prefs.getBoolean("allow_sms", false)

        val batteryManager =
            getSystemService(BATTERY_SERVICE) as BatteryManager

        val battery =
            batteryManager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CAPACITY
            )

        val status = """
            Gulshan Status

            Background Voice: ${if (backgroundOn) "ON" else "OFF"}
            Call Command: ${if (callAllowed) "ALLOW" else "OFF"}
            SMS Command: ${if (smsAllowed) "ALLOW" else "OFF"}
            Battery: ${if (battery >= 0) "$battery%" else "Unknown"}
        """.trimIndent()

        resultText.text = status

        Toast.makeText(
            this,
            "Gulshan status ready.",
            Toast.LENGTH_SHORT
        ).show()

        if (::tts.isInitialized) {
            tts.speak(
                "Background voice ${if (backgroundOn) "on" else "off"}. " +
                        "Battery ${if (battery >= 0) battery else "unknown"} percent.",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "GULSHAN_STATUS"
            )
        }
    }

    private fun showBattery() {
        val batteryManager =
            getSystemService(BATTERY_SERVICE) as BatteryManager

        val battery =
            batteryManager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CAPACITY
            )

        if (battery >= 0) {
            respond(
                "Phone ki battery $battery percent hai."
            )
        } else {
            respond(
                "Battery status nahi mil saka."
            )
        }
    }

    private fun saveHistory(command: String) {
        val prefs =
            getSharedPreferences(
                "gulshan_history",
                MODE_PRIVATE
            )

        val old =
            prefs.getStringSet(
                "items",
                emptySet()
            )?.toMutableList()
                ?: mutableListOf()

        old.add(
            "${SimpleDateFormat(
                "dd/MM HH:mm",
                Locale.getDefault()
            ).format(Date())} - $command"
        )

        val last =
            if (old.size > 30)
                old.takeLast(30)
            else
                old

        prefs.edit()
            .putStringSet(
                "items",
                last.toSet()
            )
            .apply()
    }

    private fun showHistory() {
        val prefs =
            getSharedPreferences(
                "gulshan_history",
                MODE_PRIVATE
            )

        val items =
            prefs.getStringSet(
                "items",
                emptySet()
            )?.toList()
                ?.sorted()
                ?: emptyList()

        if (items.isEmpty()) {
            resultText.text =
                "Command History\nAbhi koi command nahi hai."

            respond(
                "Abhi command history khaali hai."
            )
            return
        }

        val text =
            buildString {
                append("Command History\n\n")

                items.takeLast(20)
                    .forEachIndexed { index, item ->
                        append(
                            "${index + 1}. $item\n"
                        )
                    }
            }

        resultText.text = text

        Toast.makeText(
            this,
            "History dikhayi ja rahi hai.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun clearHistory() {
        getSharedPreferences(
            "gulshan_history",
            MODE_PRIVATE
        )
            .edit()
            .clear()
            .apply()

        respond(
            "Command history clear kar di."
        )
    }

    private fun openUpdatePage() {
        openWebsite(
            "https://github.com/kabirmalik1432-jpg/hamzaad-ai/releases"
        )

        respond(
            "Gulshan update page khol raha hoon."
        )
    }

    private fun setPermissionLimit(
        key: String,
        value: Boolean
    ) {
        getSharedPreferences(
            "gulshan",
            MODE_PRIVATE
        )
            .edit()
            .putBoolean(
                key,
                value
            )
            .apply()
    }

    private fun learnCommand(
        command: String
    ) {
        val text =
            command
                .replaceFirst(
                    "yaad rakho ",
                    ""
                )
                .replaceFirst(
                    "remember ",
                    ""
                )
                .trim()

        val parts =
            text.split("=")

        if (parts.size != 2) {
            respond(
                "Format: yaad rakho mera youtube = youtube kholo"
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
        val learned =
            getSharedPreferences(
                "gulshan_commands",
                MODE_PRIVATE
            )
                .getString(
                    command,
                    null
                )

        if (learned != null) {
            executeCommand(learned)
        } else {
            respond(
                "Ye command abhi available nahi hai. " +
                        "Bolo: yaad rakho phrase = action."
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
        if (status == TextToSpeech.SUCCESS) {

            val result =
                tts.setLanguage(
                    Locale("hi", "IN")
                )

            if (
                result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                tts.language =
                    Locale.getDefault()
            }

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
