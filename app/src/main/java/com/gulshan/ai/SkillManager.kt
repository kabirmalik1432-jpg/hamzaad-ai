package com.gulshan.ai

import android.content.Context

class SkillManager(private val context: Context) {

    private val skills = mutableMapOf<String, String>()

    init {
        loadDefaultSkills()
    }

    private fun loadDefaultSkills() {
        skills["hello"] = "Hello, main Gulshan AI hoon."
        skills["youtube"] = "YouTube khol raha hoon."
        skills["chrome"] = "Chrome khol raha hoon."
        skills["camera"] = "Camera khol raha hoon."
        skills["settings"] = "Settings khol raha hoon."
        skills["google"] = "Google khol raha hoon."
    }

    fun addSkill(name: String, response: String) {
        val key = name.trim().lowercase()

        if (key.isNotEmpty() && response.isNotEmpty()) {
            skills[key] = response
        }
    }

    fun hasSkill(name: String): Boolean {
        return skills.containsKey(
            name.trim().lowercase()
        )
    }

    fun getSkill(name: String): String? {
        return skills[name.trim().lowercase()]
    }

    fun removeSkill(name: String) {
        skills.remove(
            name.trim().lowercase()
        )
    }

    fun getAllSkills(): Map<String, String> {
        return skills.toMap()
    }

    fun clearSkills() {
        skills.clear()
        loadDefaultSkills()
    }
}
