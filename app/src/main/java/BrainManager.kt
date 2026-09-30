package com.gulshan.ai

import android.content.Context
import org.json.JSONObject

class BrainManager(private val context: Context) {

    private val skillManager = SkillManager(context)

    fun understand(command: String): String {

        val text = normalize(command)

        if (text.isEmpty()) return "EMPTY_COMMAND"

        // पहले user की saved skill खोजो
        val skill = skillManager.findBestSkill(text)

        if (skill != null) {
            val action = skill.optString("action").trim()

            if (
                action.isNotEmpty() &&
                normalize(action) != text
            ) {
                return "ACTION:$action"
            }
        }

        return when {

            text.contains("hello") ||
            text.contains("hi") ||
            text.contains("namaste") ||
            text.contains("salam") ||
            text.contains("नमस्ते") -> {
                "GREETING_REQUEST"
            }

            text.contains("tum kya kar sakte") ||
            text.contains("kya kar sakte") ||
            text.contains("tumhare paas kya") ||
            text.contains("help") ||
            text.contains("madad") ||
            text.contains("capabilities") -> {
                "HELP_REQUEST"
            }

            text.contains("kaise ho") ||
            text.contains("kya haal") -> {
                "CHAT:Main bilkul ready hoon. Command bolo."
            }

            text.contains("tumhara naam") ||
            text.contains("apna naam") ||
            text.contains("who are you") -> {
                "CHAT:Main Gulshan AI hoon."
            }

            text.contains("thank") ||
            text.contains("thanks") ||
            text.contains("shukriya") -> {
                "CHAT:Khushi hui."
            }

            text.contains("youtube") -> {
                "YOUTUBE_REQUEST"
            }

            text.contains("chrome") ||
            text.contains("browser") -> {
                "CHROME_REQUEST"
            }

            text.contains("camera") ||
            text.contains("camra") -> {
                "CAMERA_REQUEST"
            }

            text.contains("setting") ||
            text.contains("settings") -> {
                "SETTINGS_REQUEST"
            }

            text.contains("battery") ||
            text.contains("battary") ||
            text.contains("charge") -> {
                "BATTERY_REQUEST"
            }

            text.contains("time") ||
            text.contains("kitne baje") -> {
                "TIME_REQUEST"
            }

            text.contains("date") ||
            text.contains("tarikh") ||
            text.contains("aaj ki date") -> {
                "DATE_REQUEST"
            }

            text.contains("background voice on") ||
            text.contains("background chalu") ||
            text.contains("background start") -> {
                "BACKGROUND_ON"
            }

            text.contains("background voice off") ||
            text.contains("background band") ||
            text.contains("background stop") -> {
                "BACKGROUND_OFF"
            }

            text.contains("image") ||
            text.contains("photo") ||
            text.contains("picture") ||
            text.contains("tasveer") ||
            text.contains("तस्वीर") -> {
                "IMAGE_REQUEST"
            }

            text.contains("video") ||
            text.contains("clip") -> {
                "VIDEO_REQUEST"
            }

            text.contains("skill") ||
            text.contains("seekho") ||
            text.contains("sikho") ||
            text.contains("yaad rakho") ||
            text.contains("remember") -> {
                "SKILL_REQUEST"
            }

            text.contains("add karo") ||
            text.contains("jod do") ||
            text.contains("seekh lo") ||
            text.contains("learn karo") ||
            text.contains("tumhare paas nahi hai") ||
            text.contains("feature nahi hai") ||
            text.contains("capability") -> {

                skillManager.saveCapabilityRequest(
                    text,
                    "User requested a new capability"
                )

                "CAPABILITY_REQUEST"
            }

            text.contains("google") ||
            text.contains("search") ||
            text.contains("सर्च") -> {
                "SEARCH_REQUEST"
            }

            else -> {
                "UNKNOWN_COMMAND"
            }
        }
    }

    private fun normalize(value: String): String {

        var text = value
            .trim()
            .lowercase()

        val replacements = mapOf(
            "गुलशन" to "gulshan",
            "गुलशन जी" to "gulshan",
            "हेलो" to "hello",
            "हैलो" to "hello",
            "नमस्ते" to "namaste",
            "यूट्यूब" to "youtube",
            "कैमरा" to "camera",
            "सेटिंग" to "settings",
            "सेटिंग्स" to "settings",
            "तस्वीर" to "image",
            "फोटो" to "photo",
            "वीडियो" to "video",
            "खोलो" to "kholo",
            "खोल दो" to "kholo",
            "चलाओ" to "chalao",
            "चला दो" to "chalao",
            "बनाओ" to "banao",
            "याद रखो" to "yaad rakho",
            "सीखो" to "seekho",
            "सर्च" to "search"
        )

        for ((old, new) in replacements) {
            text = text.replace(old, new)
        }

        return text
            .replace("।", " ")
            .replace(",", " ")
            .replace("?", " ")
            .replace("!", " ")
            .replace("  ", " ")
            .trim()
    }

    fun createSkill(
        name: String,
        description: String,
        action: String
    ): Boolean {
        return skillManager.addSkill(
            name,
            description,
            action
        )
    }

    fun hasSkill(name: String): Boolean {
        return skillManager.hasSkill(name)
    }

    fun getSkill(name: String): JSONObject? {
        return skillManager.getSkill(name)
    }

    fun getAllSkills(): List<JSONObject> {
        return skillManager.getAllSkills()
    }

    fun getCapabilityRequests(): List<JSONObject> {
        return skillManager.getCapabilityRequests()
    }
}package com.gulshan.ai

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class SkillManager(private val context: Context) {

    private val prefs = context.getSharedPreferences(
        "gulshan_skills",
        Context.MODE_PRIVATE
    )

    fun addSkill(
        name: String,
        description: String,
        action: String = ""
    ): Boolean {

        val cleanName = normalize(name)
        val cleanDescription = description.trim()
        val cleanAction = action.trim()

        if (cleanName.isEmpty() || cleanDescription.isEmpty()) {
            return false
        }

        val skills = getSkillsArray()

        val skill = JSONObject()
        skill.put("name", cleanName)
        skill.put("description", cleanDescription)
        skill.put("action", cleanAction)

        var updated = false

        for (i in 0 until skills.length()) {

            val old = skills.optJSONObject(i)

            if (
                old != null &&
                normalize(old.optString("name")) == cleanName
            ) {
                skills.put(i, skill)
                updated = true
                break
            }
        }

        if (!updated) {
            skills.put(skill)
        }

        saveSkills(skills)

        return true
    }

    fun hasSkill(name: String): Boolean {
        return findBestSkill(name) != null
    }

    fun getSkill(name: String): JSONObject? {
        return findBestSkill(name)
    }

    fun findBestSkill(command: String): JSONObject? {

        val query = normalize(command)

        if (query.isEmpty()) return null

        val skills = getSkillsArray()

        // Exact match
        for (i in 0 until skills.length()) {

            val skill = skills.optJSONObject(i) ?: continue

            if (
                normalize(skill.optString("name")) == query
            ) {
                return skill
            }
        }

        // Partial / natural command match
        val queryWords = query
            .split(" ")
            .filter { it.length >= 3 }

        var bestSkill: JSONObject? = null
        var bestScore = 0

        for (i in 0 until skills.length()) {

            val skill = skills.optJSONObject(i) ?: continue

            val name = normalize(skill.optString("name"))

            val nameWords = name
                .split(" ")
                .filter { it.length >= 3 }

            var score = 0

            for (word in queryWords) {
                if (nameWords.contains(word)) {
                    score++
                }
            }

            if (name.contains(query) || query.contains(name)) {
                score += 2
            }

            if (score > bestScore) {
                bestScore = score
                bestSkill = skill
            }
        }

        return if (bestScore >= 2) {
            bestSkill
        } else {
            null
        }
    }

    fun removeSkill(name: String): Boolean {

        val skills = getSkillsArray()
        val target = normalize(name)

        for (i in 0 until skills.length()) {

            val skill = skills.optJSONObject(i)

            if (
                skill != null &&
                normalize(skill.optString("name")) == target
            ) {
                skills.remove(i)
                saveSkills(skills)
                return true
            }
        }

        return false
    }

    fun getAllSkills(): List<JSONObject> {

        val result = mutableListOf<JSONObject>()
        val skills = getSkillsArray()

        for (i in 0 until skills.length()) {

            val skill = skills.optJSONObject(i)

            if (skill != null) {
                result.add(skill)
            }
        }

        return result
    }

    fun saveCapabilityRequest(
        request: String,
        description: String
    ) {

        val requests = getCapabilityArray()

        val item = JSONObject()

        item.put(
            "request",
            request.trim()
        )

        item.put(
            "description",
            description.trim()
        )

        item.put(
            "time",
            System.currentTimeMillis()
        )

        requests.put(item)

        prefs.edit()
            .putString(
                "capability_requests",
                requests.toString()
            )
            .apply()
    }

    fun getCapabilityRequests(): List<JSONObject> {

        val result = mutableListOf<JSONObject>()
        val requests = getCapabilityArray()

        for (i in 0 until requests.length()) {

            val item = requests.optJSONObject(i)

            if (item != null) {
                result.add(item)
            }
        }

        return result
    }

    fun clearAllSkills() {

        prefs.edit()
            .remove("skills")
            .apply()
    }

    private fun getSkillsArray(): JSONArray {

        val saved = prefs.getString(
            "skills",
            "[]"
        )

        return try {
            JSONArray(saved)
        } catch (e: Exception) {
            JSONArray()
        }
    }

    private fun getCapabilityArray(): JSONArray {

        val saved = prefs.getString(
            "capability_requests",
            "[]"
        )

        return try {
            JSONArray(saved)
        } catch (e: Exception) {
            JSONArray()
        }
    }

    private fun saveSkills(skills: JSONArray) {

        prefs.edit()
            .putString(
                "skills",
                skills.toString()
            )
            .apply()
    }

    private fun normalize(value: String): String {

        var text = value
            .trim()
            .lowercase()

        val replacements = mapOf(
            "गुलशन" to "gulshan",
            "यूट्यूब" to "youtube",
            "खोलो" to "kholo",
            "खोल दो" to "kholo",
            "चलाओ" to "chalao",
            "चला दो" to "chalao",
            "बनाओ" to "banao",
            "याद रखो" to "yaad rakho",
            "सीखो" to "seekho"
        )

        for ((old, new) in replacements) {
            text = text.replace(old, new)
        }

        return text
            .replace(Regex("[,!?।]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}package com.gulshan.ai

import android.Manifest
import android.app.AlarmManager
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

class MainActivity :
    AppCompatActivity(),
    TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var resultText: TextView
    private lateinit var commandInput: EditText

    private lateinit var brainManager: BrainManager

    private val voiceRequest = 1001
    private val permissionRequest = 1002

    private var backgroundOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        brainManager = BrainManager(this)

        tts = TextToSpeech(
            this,
            this
        )

        buildUI()
        requestBasicPermissions()

        if (
            intent.getBooleanExtra(
                "background_command",
                false
            )
        ) {

            val command =
                intent.getStringExtra("command")

            if (!command.isNullOrBlank()) {
                commandInput.setText(command)
                executeCommand(command)
            }
        }
    }

    private fun buildUI() {

        val root = LinearLayout(this)

        root.orientation =
            LinearLayout.VERTICAL

        root.setPadding(
            30,
            30,
            30,
            30
        )

        val title = TextView(this)

        title.text =
            "GULSHAN AI V6"

        title.textSize = 28f

        root.addView(title)

        val greeting = TextView(this)

        greeting.text =
            "Namaste! Main Gulshan hoon."

        greeting.textSize = 18f

        root.addView(greeting)

        commandInput =
            EditText(this)

        commandInput.hint =
            "Command bolo..."

        root.addView(commandInput)

        val execute =
            Button(this)

        execute.text =
            "COMMAND CHALAO"

        execute.setOnClickListener {

            executeCommand(
                commandInput.text.toString()
            )
        }

        root.addView(execute)

        val voice =
            Button(this)

        voice.text =
            "🎤 VOICE COMMAND"

        voice.setOnClickListener {
            startVoiceCommand()
        }

        root.addView(voice)

        val backgroundStart =
            Button(this)

        backgroundStart.text =
            "BACKGROUND VOICE ON"

        backgroundStart.setOnClickListener {
            startBackgroundVoice()
        }

        root.addView(backgroundStart)

        val backgroundStop =
            Button(this)

        backgroundStop.text =
            "BACKGROUND VOICE OFF"

        backgroundStop.setOnClickListener {
            stopBackgroundVoice()
        }

        root.addView(backgroundStop)

        val brain =
            Button(this)

        brain.text =
            "🧠 BRAIN / SKILLS"

        brain.setOnClickListener {
            showBrainInfo()
        }

        root.addView(brain)

        val history =
            Button(this)

        history.text =
            "HISTORY"

        history.setOnClickListener {
            showHistory()
        }

        root.addView(history)

        val clearHistory =
            Button(this)

        clearHistory.text =
            "CLEAR HISTORY"

        clearHistory.setOnClickListener {
            clearHistory()
        }

        root.addView(clearHistory)

        val status =
            Button(this)

        status.text =
            "STATUS"

        status.setOnClickListener {
            showStatus()
        }

        root.addView(status)

        resultText =
            TextView(this)

        resultText.text =
            "Ready."

        resultText.textSize = 17f

        root.addView(resultText)

        setContentView(root)
    }

    private fun requestBasicPermissions() {

        val permissions =
            mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= 33) {

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

    private fun normalize(value: String): String {

        return value
            .trim()
            .lowercase()
            .replace("गुलशन", "gulshan")
            .replace("गुलशन जी", "gulshan")
            .replace("हेलो", "hello")
            .replace("हैलो", "hello")
            .replace("यूट्यूब", "youtube")
            .replace("कैमरा", "camera")
            .replace("खोलो", "kholo")
            .replace("खोल दो", "kholo")
            .replace("याद रखो", "yaad rakho")
            .replace("सीखो", "seekho")
            .replace(Regex("[,!?।]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun executeCommand(
        originalCommand: String
    ) {

        var command =
            normalize(originalCommand)

        if (command.startsWith("gulshan ")) {
            command =
                command.removePrefix("gulshan ")
        }

        if (command.isBlank()) {

            respond("Command batao.")

            return
        }

        saveHistory(command)

        val brainResult =
            brainManager.understand(command)

        when {

            brainResult.startsWith("ACTION:") -> {

                val action =
                    brainResult
                        .removePrefix("ACTION:")
                        .trim()

                if (
                    action.isNotBlank() &&
                    normalize(action) !=
                    normalize(command)
                ) {

                    executeCommand(action)

                } else {

                    respond(
                        "Saved command mil gaya."
                    )
                }

                return
            }

            brainResult == "GREETING_REQUEST" -> {

                respond(
                    "Namaste! Main Gulshan hoon."
                )

                return
            }

            brainResult == "HELP_REQUEST" -> {

                showHelp()

                return
            }

            brainResult.startsWith("CHAT:") -> {

                respond(
                    brainResult
                        .removePrefix("CHAT:")
                        .trim()
                )

                return
            }

            brainResult == "YOUTUBE_REQUEST" -> {

                openInstalledApp(
                    "com.google.android.youtube"
                )

                return
            }

            brainResult == "CHROME_REQUEST" -> {

                openChrome()

                return
            }

            brainResult == "CAMERA_REQUEST" -> {

                openCamera()

                return
            }

            brainResult == "SETTINGS_REQUEST" -> {

                openSettings()

                return
            }

            brainResult == "BATTERY_REQUEST" -> {

                showBattery()

                return
            }

            brainResult == "TIME_REQUEST" -> {

                val time =
                    SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                    ).format(Date())

                respond(
                    "Abhi $time baj rahe hain."
                )

                return
            }

            brainResult == "DATE_REQUEST" -> {

                val date =
                    SimpleDateFormat(
                        "dd MMMM yyyy",
                        Locale.getDefault()
                    ).format(Date())

                respond(
                    "Aaj $date hai."
                )

                return
            }

            brainResult == "BACKGROUND_ON" -> {

                startBackgroundVoice()

                return
            }

            brainResult == "BACKGROUND_OFF" -> {

                stopBackgroundVoice()

                return
            }

            brainResult == "CAPABILITY_REQUEST" -> {

                respond(
                    "Maine ye capability request save kar li hai."
                )

                return
            }

            brainResult == "IMAGE_REQUEST" -> {

                respond(
                    "Image request samajh gaya."
                )

                return
            }

            brainResult == "VIDEO_REQUEST" -> {

                respond(
                    "Video request samajh gaya."
                )

                return
            }

            brainResult == "SEARCH_REQUEST" -> {

                googleSearch(command)

                return
            }

            brainResult == "SKILL_REQUEST" -> {

                if (
                    command.startsWith("yaad rakho") ||
                    command.startsWith("remember")
                ) {

                    learnCommand(command)

                } else {

                    respond(
                        "Skill sikhane ke liye bolo: yaad rakho..."
                    )
                }

                return
            }
        }

        // पुराने commands भी काम करेंगे
        when {

            command.contains("youtube") -> {

                openInstalledApp(
                    "com.google.android.youtube"
                )
            }

            command.contains("chrome") ||
            command.contains("browser") -> {

                openChrome()
            }

            command.contains("camera") -> {

                openCamera()
            }

            command.contains("settings") -> {

                openSettings()
            }

            command.contains("google") -> {

                googleSearch(command)
            }

            command.contains("search") -> {

                googleSearch(command)
            }

            command.contains("wifi") -> {

                openWifiSettings()
            }

            command.contains("bluetooth") -> {

                openBluetoothSettings()
            }

            command.contains("battery") -> {

                showBattery()
            }

            command.contains("time") -> {

                val time =
                    SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                    ).format(Date())

                respond(
                    "Abhi $time baj rahe hain."
                )
            }

            command.contains("date") -> {

                val date =
                    SimpleDateFormat(
                        "dd MMMM yyyy",
                        Locale.getDefault()
                    ).format(Date())

                respond(
                    "Aaj $date hai."
                )
            }

            command.contains("flashlight on") ||
            command.contains("torch on") -> {

                setFlashlight(true)
            }

            command.contains("flashlight off") ||
            command.contains("torch off") -> {

                setFlashlight(false)
            }

            command.startsWith("timer") -> {

                startTimer(command)
            }

            command.startsWith("alarm") -> {

                startAlarm(command)
            }

            command.startsWith("website") ||
            command.startsWith("site") -> {

                val url =
                    command
                        .replaceFirst("website", "")
                        .replaceFirst("site", "")
                        .trim()

                openWebsite(url)
            }

            command.startsWith("yaad rakho") ||
            command.startsWith("remember") -> {

                learnCommand(command)
            }

            command.contains("history clear") ||
            command.contains("clear history") -> {

                clearHistory()
            }

            command.contains("background voice on") -> {

                startBackgroundVoice()
            }

            command.contains("background voice off") -> {

                stopBackgroundVoice()
            }

            command.contains("app close") ||
            command.contains("exit") ||
            command.contains("band ho jao") -> {

                finish()
            }

            else -> {

                runLearnedCommand(command)
            }
        }
    }

    private fun showHelp() {

        val skillCount =
            brainManager
                .getAllSkills()
                .size

        val text =
            """
            Main ye commands samajh sakta hoon:
            
            • YouTube / Chrome / Camera
            • Settings / WiFi / Bluetooth
            • Google Search
            • Battery / Time / Date
            • Timer / Alarm
            • Flashlight
            • Background Voice
            • Image / Video requests
            • User-defined Skills
            
            Saved Skills: $skillCount
            
            Nayi command sikhane ke liye:
            "Yaad rakho mera music = youtube kholo"
            """.trimIndent()

        resultText.text = text

        tts.speak(
            "Main apps, search, battery, time, background voice aur learned skills handle kar sakta hoon.",
            TextToSpeech.QUEUE_FLUSH,
            null,
            "help"
        )
    }

    private fun showBrainInfo() {

        val skills =
            brainManager.getAllSkills()

        val requests =
            brainManager.getCapabilityRequests()

        val builder =
            StringBuilder()

        builder.append(
            "GULSHAN BRAIN\n\n"
        )

        builder.append(
            "Saved Skills: ${skills.size}\n"
        )

        builder.append(
            "Capability Requests: ${requests.size}\n\n"
        )

        if (skills.isNotEmpty()) {

            builder.append(
                "Skills:\n"
            )

            for (skill in skills) {

                builder.append(
                    "• "
                )

                builder.append(
                    skill.optString("name")
                )

                builder.append(
                    " → "
                )

                builder.append(
                    skill.optString("action")
                )

                builder.append(
                    "\n"
                )
            }
        }

        resultText.text =
            builder.toString()

        tts.speak(
            "Brain ready hai. ${skills.size} saved skills hain.",
            TextToSpeech.QUEUE_FLUSH,
            null,
            "brain"
        )
    }

    private fun learnCommand(
        command: String
    ) {

        var text =
            command
                .removePrefix("yaad rakho")
                .removePrefix("remember")
                .trim()

        val parts =
            text.split(
                "=",
                limit = 2
            )

        if (parts.size != 2) {

            respond(
                "Format bolo: yaad rakho naam = action"
            )

            return
        }

        val phrase =
            normalize(parts[0])

        val action =
            normalize(parts[1])

        if (
            phrase.isBlank() ||
            action.isBlank()
        ) {

            respond(
                "Command aur action dono chahiye."
            )

            return
        }

        val saved =
            brainManager.createSkill(
                phrase,
                "User learned command",
                action
            )

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

        if (saved) {

            respond(
                "Command yaad rakh li."
            )

        } else {

            respond(
                "Command save nahi ho saki."
            )
        }
    }

    private fun runLearnedCommand(
        command: String
    ) {

        val saved =
            getSharedPreferences(
                "gulshan_commands",
                MODE_PRIVATE
            )

        val action =
            saved.getString(
                normalize(command),
                null
            )

        if (!action.isNullOrBlank()) {

            executeCommand(action)

        } else {

            respond(
                "Ye command abhi available nahi hai."
            )
        }
    }

    private fun openChrome() {

        try {

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com")
                )

            startActivity(intent)

        } catch (e: Exception) {

            respond(
                "Chrome open nahi ho saka."
            )
        }
    }

    private fun openInstalledApp(
        packageName: String
    ) {

        try {

            val intent =
                packageManager
                    .getLaunchIntentForPackage(
                        packageName
                    )

            if (intent != null) {

                startActivity(intent)

            } else {

                respond(
                    "App phone mein installed nahi hai."
                )
            }

        } catch (e: Exception) {

            respond(
                "App open nahi ho saka."
            )
        }
    }

    private fun openCamera() {

        try {

            val intent =
                Intent(
                    "android.media.action.IMAGE_CAPTURE"
                )

            startActivity(intent)

        } catch (e: Exception) {

            respond(
                "Camera open nahi ho saka."
            )
        }
    }

    private fun openSettings() {

        try {

            startActivity(
                Intent(
                    Settings.ACTION_SETTINGS
                )
            )

        } catch (e: Exception) {

            respond(
                "Settings open nahi ho saka."
            )
        }
    }

    private fun openWifiSettings() {

        startActivity(
            Intent(
                Settings.ACTION_WIFI_SETTINGS
            )
        )
    }

    private fun openBluetoothSettings() {

        startActivity(
            Intent(
                Settings.ACTION_BLUETOOTH_SETTINGS
            )
        )
    }

    private fun openWebsite(
        urlText: String
    ) {

        var url = urlText.trim()

        if (url.isEmpty()) {

            respond(
                "Website ka naam bolo."
            )

            return
        }

        if (
            !url.startsWith("http://") &&
            !url.startsWith("https://")
        ) {

            url =
                "https://$url"
        }

        try {

            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )

        } catch (e: Exception) {

            respond(
                "Website open nahi ho saki."
            )
        }
    }

    private fun googleSearch(
        query: String
    ) {

        var clean =
            query
                .replace("google", "")
                .replace("search", "")
                .replace("सर्च", "")
                .trim()

        if (clean.isEmpty()) {

            clean = "Google"
        }

        val url =
            "https://www.google.com/search?q=" +
                    Uri.encode(clean)

        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
        )
    }

    private fun startTimer(
        command: String
    ) {

        val intent =
            Intent(
                AlarmClock.ACTION_SET_TIMER
            )

        intent.putExtra(
            AlarmClock.EXTRA_MESSAGE,
            "Gulshan Timer"
        )

        intent.putExtra(
            AlarmClock.EXTRA_LENGTH,
            60
        )

        startActivity(intent)
    }

    private fun startAlarm(
        command: String
    ) {

        val intent =
            Intent(
                AlarmClock.ACTION_SET_ALARM
            )

        intent.putExtra(
            AlarmClock.EXTRA_HOUR,
            7
        )

        intent.putExtra(
            AlarmClock.EXTRA_MINUTES,
            0
        )

        intent.putExtra(
            AlarmClock.EXTRA_MESSAGE,
            "Gulshan Alarm"
        )

        startActivity(intent)
    }

    private fun setFlashlight(
        enabled: Boolean
    ) {

        try {

            val cameraManager =
                getSystemService(
                    CAMERA_SERVICE
                ) as CameraManager

            val cameraId =
                cameraManager.cameraIdList
                    .firstOrNull { id ->

                        val characteristics =
                            cameraManager.getCameraCharacteristics(id)

                        val flash =
                            characteristics.get(
                                android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE
                            )

                        flash == true
                    }

            if (cameraId != null) {

                cameraManager.setTorchMode(
                    cameraId,
                    enabled
                )

                respond(
                    if (enabled)
                        "Flashlight on."
                    else
                        "Flashlight off."
                )

            } else {

                respond(
                    "Flashlight available nahi hai."
                )
            }

        } catch (e: Exception) {

            respond(
                "Flashlight control nahi ho saka."
            )
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

            if (!command.isNullOrBlank()) {

                commandInput.setText(
                    command
                )

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

            if (Build.VERSION.SDK_INT >= 26) {

                startForegroundService(intent)

            } else {

                startService(intent)
            }

            backgroundOn = true

            respond(
                "Background voice on."
            )

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

            backgroundOn = false

            respond(
                "Background voice off."
            )

        } catch (e: Exception) {

            respond(
                "Background voice stop nahi ho saka."
            )
        }
    }

    private fun showStatus() {

        val manager =
            getSystemService(
                BATTERY_SERVICE
            ) as BatteryManager

        val battery =
            manager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CAPACITY
            )

        val voiceState =
            if (backgroundOn)
                "on"
            else
                "off"

        val batteryText =
            if (battery >= 0)
                battery.toString()
            else
                "unknown"

        val message =
            "Background voice $voiceState. Battery $batteryText percent."

        resultText.text =
            message

        tts.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "status"
        )
    }

    private fun showBattery() {

        val manager =
            getSystemService(
                BATTERY_SERVICE
            ) as BatteryManager

        val battery =
            manager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CAPACITY
            )

        val message =
            if (battery >= 0)
                "Battery $battery percent hai."
            else
                "Battery status available nahi hai."

        respond(message)
    }

    private fun saveHistory(
        command: String
    ) {

        val prefs =
            getSharedPreferences(
                "gulshan_history",
                MODE_PRIVATE
            )

        val old =
            prefs.getStringSet(
                "history",
                emptySet()
            )?.toMutableSet()
                ?: mutableSetOf()

        old.add(command)

        prefs.edit()
            .putStringSet(
                "history",
                old
            )
            .apply()
    }

    private fun showHistory() {

        val prefs =
            getSharedPreferences(
                "gulshan_history",
                MODE_PRIVATE
            )

        val history =
            prefs.getStringSet(
                "history",
                emptySet()
            )

        resultText.text =
            if (history.isNullOrEmpty()) {
                "History empty hai."
            } else {
                history.joinToString(
                    "\n"
                ) { "• $it" }
            }
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
            "History clear kar di."
        )
    }

    private fun respond(
        message: String
    ) {

        resultText.text =
            message

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
                "gulshan_response"
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

            val result =
                tts.setLanguage(
                    Locale(
                        "hi",
                        "IN"
                    )
                )

            if (
                result ==
                TextToSpeech.LANG_MISSING_DATA ||
                result ==
                TextToSpeech.LANG_NOT_SUPPORTED
            ) {

                tts.language =
                    Locale.ENGLISH
            }
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
