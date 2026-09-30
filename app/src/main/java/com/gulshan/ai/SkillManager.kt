package com.gulshan.ai

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

        val cleanName = name.trim()
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
            val oldSkill = skills.optJSONObject(i)

            if (
                oldSkill != null &&
                oldSkill.optString("name")
                    .equals(cleanName, ignoreCase = true)
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
        return findSkill(name) != null
    }

    fun getSkill(name: String): JSONObject? {
        return findSkill(name)
    }

    fun removeSkill(name: String): Boolean {

        val skills = getSkillsArray()

        for (i in 0 until skills.length()) {
            val skill = skills.optJSONObject(i)

            if (
                skill != null &&
                skill.optString("name")
                    .equals(name.trim(), ignoreCase = true)
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

    fun clearAllSkills() {
        prefs.edit()
            .remove("skills")
            .apply()
    }

    private fun findSkill(name: String): JSONObject? {

        val cleanName = name.trim()

        if (cleanName.isEmpty()) {
            return null
        }

        val skills = getSkillsArray()

        for (i in 0 until skills.length()) {

            val skill = skills.optJSONObject(i)

            if (
                skill != null &&
                skill.optString("name")
                    .equals(cleanName, ignoreCase = true)
            ) {
                return skill
            }
        }

        return null
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

    private fun saveSkills(skills: JSONArray) {

        prefs.edit()
            .putString(
                "skills",
                skills.toString()
            )
            .apply()
    }
}
