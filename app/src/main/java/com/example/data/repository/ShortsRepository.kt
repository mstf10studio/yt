package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.engine.ShortsEngine
import com.example.data.local.ShortsDao
import com.example.data.local.ShortsProjectEntity
import com.example.model.AppSettings
import com.example.model.CaptionStyle
import com.example.model.GenerationStep
import com.example.model.LogEntry
import com.example.model.ShortScript
import com.example.model.VoiceTone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class ShortsRepository(
    private val context: Context,
    private val shortsDao: ShortsDao,
    val shortsEngine: ShortsEngine
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("shorts_ai_prefs", Context.MODE_PRIVATE)

    val allProjects: Flow<List<ShortsProjectEntity>> = shortsDao.getAllProjects()

    val currentStep: StateFlow<GenerationStep> = shortsEngine.currentStep
    val progressPercent: StateFlow<Int> = shortsEngine.progressPercent
    val currentScript: StateFlow<ShortScript?> = shortsEngine.currentScript
    val logs: StateFlow<List<LogEntry>> = shortsEngine.logs

    fun getSettings(): AppSettings {
        val voiceToneStr = prefs.getString("voice_tone", VoiceTone.ENERGETIC.name)
        val captionStyleStr = prefs.getString("caption_style", CaptionStyle.HIGHLIGHT_YELLOW.name)

        val voiceTone = runCatching { VoiceTone.valueOf(voiceToneStr ?: "") }.getOrDefault(VoiceTone.ENERGETIC)
        val captionStyle = runCatching { CaptionStyle.valueOf(captionStyleStr ?: "") }.getOrDefault(CaptionStyle.HIGHLIGHT_YELLOW)

        return AppSettings(
            pexelsApiKey = prefs.getString("pexels_key", "") ?: "",
            geminiApiKey = prefs.getString("gemini_key", "") ?: "",
            defaultLanguage = prefs.getString("language", "tr") ?: "tr",
            defaultDuration = prefs.getInt("duration", 30),
            defaultVoiceTone = voiceTone,
            defaultCaptionStyle = captionStyle
        )
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit()
            .putString("pexels_key", settings.pexelsApiKey)
            .putString("gemini_key", settings.geminiApiKey)
            .putString("language", settings.defaultLanguage)
            .putInt("duration", settings.defaultDuration)
            .putString("voice_tone", settings.defaultVoiceTone.name)
            .putString("caption_style", settings.defaultCaptionStyle.name)
            .apply()
    }

    suspend fun generateShorts(
        topic: String,
        targetDurationSeconds: Int,
        language: String,
        voiceTone: VoiceTone,
        captionStyle: CaptionStyle
    ): Result<ShortScript> {
        val settings = getSettings()
        return shortsEngine.generateShortsVideo(
            topic = topic,
            targetDurationSeconds = targetDurationSeconds,
            language = language,
            voiceTone = voiceTone,
            captionStyle = captionStyle,
            settings = settings
        )
    }

    suspend fun deleteProject(id: Long) {
        shortsDao.deleteProjectById(id)
    }
}
