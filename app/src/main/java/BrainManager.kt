package com.gulshan.ai

import android.content.Context
import org.json.JSONObject

class BrainManager(private val context: Context) {

    private val skillManager = SkillManager(context)

    fun understand(command: String): String {

        val text = command.trim().lowercase()

        if (text.isEmpty()) {
            return "Command batao."
        }

        val skill = skillManager.getSkill(text)

        if (skill != null) {
            val action = skill.optString("action")

            if (action.isNotEmpty()) {
                return action
            }
        }

        return understandBasic(text)
    }

    private fun understandBasic(text: String): String {

        return when {
            text.contains("image") ||
            text.contains("photo") ||
            text.contains("picture") ||
            text.contains("tasveer") -> {
                "IMAGE_REQUEST"
            }

            text.contains("video") ||
            text.contains("clip") -> {
                "VIDEO_REQUEST"
            }

            text.contains("youtube") -> {
                "YOUTUBE_REQUEST"
            }

            text.contains("google") ||
            text.contains("search") -> {
                "SEARCH_REQUEST"
            }

            text.contains("skill") ||
            text.contains("seekho") ||
            text.contains("yaad rakho") -> {
                "SKILL_REQUEST"
            }

            else -> {
                "UNKNOWN_COMMAND"
            }
        }
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
}
