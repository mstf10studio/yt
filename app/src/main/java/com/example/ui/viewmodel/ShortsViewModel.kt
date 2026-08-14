package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.engine.ShortsEngine
import com.example.data.engine.TextToSpeechEngine
import com.example.data.local.AppDatabase
import com.example.data.local.ShortsProjectEntity
import com.example.data.remote.GeminiScriptEngine
import com.example.data.remote.PexelsApiService
import com.example.data.repository.ShortsRepository
import com.example.model.AppSettings
import com.example.model.CaptionStyle
import com.example.model.GenerationStep
import com.example.model.LogEntry
import com.example.model.ShortScript
import com.example.model.VoiceTone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ShortsUiState(
    val topic: String = "",
    val durationSeconds: Int = 30,
    val language: String = "tr",
    val voiceTone: VoiceTone = VoiceTone.ENERGETIC,
    val captionStyle: CaptionStyle = CaptionStyle.HIGHLIGHT_YELLOW,
    val isApiKeyDialogOpen: Boolean = false,
    val isErrorDialogOpen: Boolean = false,
    val selectedTab: Int = 0 // 0: Studio, 1: Player Preview, 2: Python/Colab, 3: Gallery
)

class ShortsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val pexelsApiService = PexelsApiService.create()
    private val geminiScriptEngine = GeminiScriptEngine()
    private val shortsEngine = ShortsEngine(application, pexelsApiService, geminiScriptEngine, db.shortsDao())

    val repository = ShortsRepository(application, db.shortsDao(), shortsEngine)
    val ttsEngine = TextToSpeechEngine(application)

    private val _uiState = MutableStateFlow(ShortsUiState())
    val uiState: StateFlow<ShortsUiState> = _uiState.asStateFlow()

    private val _settingsState = MutableStateFlow(repository.getSettings())
    val settingsState: StateFlow<AppSettings> = _settingsState.asStateFlow()

    val allProjects: StateFlow<List<ShortsProjectEntity>> = repository.allProjects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val currentStep: StateFlow<GenerationStep> = repository.currentStep
    val progressPercent: StateFlow<Int> = repository.progressPercent
    val currentScript: StateFlow<ShortScript?> = repository.currentScript
    val logs: StateFlow<List<LogEntry>> = repository.logs

    fun updateTopic(newTopic: String) {
        _uiState.value = _uiState.value.copy(topic = newTopic)
    }

    fun updateDuration(seconds: Int) {
        _uiState.value = _uiState.value.copy(durationSeconds = seconds)
    }

    fun updateLanguage(lang: String) {
        _uiState.value = _uiState.value.copy(language = lang)
    }

    fun updateVoiceTone(tone: VoiceTone) {
        _uiState.value = _uiState.value.copy(voiceTone = tone)
    }

    fun updateCaptionStyle(style: CaptionStyle) {
        _uiState.value = _uiState.value.copy(captionStyle = style)
    }

    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex)
    }

    fun toggleApiKeyDialog(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isApiKeyDialogOpen = isOpen)
    }

    fun toggleErrorDialog(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isErrorDialogOpen = isOpen)
    }

    fun saveApiKeys(pexelsKey: String, geminiKey: String) {
        val current = repository.getSettings()
        repository.saveSettings(current.copy(pexelsApiKey = pexelsKey, geminiApiKey = geminiKey))
        _settingsState.value = repository.getSettings()
    }

    fun startGeneration() {
        val topic = _uiState.value.topic
        if (topic.isBlank()) {
            _uiState.value = _uiState.value.copy(isErrorDialogOpen = true)
            return
        }

        viewModelScope.launch {
            val result = repository.generateShorts(
                topic = topic,
                targetDurationSeconds = _uiState.value.durationSeconds,
                language = _uiState.value.language,
                voiceTone = _uiState.value.voiceTone,
                captionStyle = _uiState.value.captionStyle
            )

            if (result.isSuccess) {
                // Switch to preview tab
                _uiState.value = _uiState.value.copy(selectedTab = 1)
            } else {
                _uiState.value = _uiState.value.copy(isErrorDialogOpen = true)
            }
        }
    }

    fun speakNarration(text: String) {
        ttsEngine.speak(
            text = text,
            voiceTone = _uiState.value.voiceTone,
            language = _uiState.value.language
        )
    }

    fun stopSpeaking() {
        ttsEngine.stop()
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsEngine.shutdown()
    }
}
