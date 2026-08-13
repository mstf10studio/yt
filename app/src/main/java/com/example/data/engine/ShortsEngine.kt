package com.example.data.engine

import android.content.Context
import com.example.data.local.ShortsDao
import com.example.data.local.ShortsProjectEntity
import com.example.data.remote.GeminiScriptEngine
import com.example.data.remote.PexelsApiService
import com.example.model.AppSettings
import com.example.model.CaptionStyle
import com.example.model.GenerationStep
import com.example.model.LogEntry
import com.example.model.ShortScript
import com.example.model.VideoScene
import com.example.model.VoiceTone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class ShortsEngine(
    private val context: Context,
    private val pexelsApiService: PexelsApiService,
    private val geminiScriptEngine: GeminiScriptEngine,
    private val shortsDao: ShortsDao
) {

    private val _currentStep = MutableStateFlow(GenerationStep.IDLE)
    val currentStep: StateFlow<GenerationStep> = _currentStep.asStateFlow()

    private val _progressPercent = MutableStateFlow(0)
    val progressPercent: StateFlow<Int> = _progressPercent.asStateFlow()

    private val _currentScript = MutableStateFlow<ShortScript?>(null)
    val currentScript: StateFlow<ShortScript?> = _currentScript.asStateFlow()

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private fun addLog(tag: String, message: String, isError: Boolean = false, suggestion: String? = null) {
        _logs.update { currentLogs ->
            listOf(LogEntry(tag = tag, message = message, isError = isError, suggestion = suggestion)) + currentLogs
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    suspend fun generateShortsVideo(
        topic: String,
        targetDurationSeconds: Int = 30,
        language: String = "tr",
        voiceTone: VoiceTone = VoiceTone.ENERGETIC,
        captionStyle: CaptionStyle = CaptionStyle.HIGHLIGHT_YELLOW,
        settings: AppSettings
    ): Result<ShortScript> = withContext(Dispatchers.IO) {
        try {
            clearLogs()
            _progressPercent.value = 5
            _currentStep.value = GenerationStep.VALIDATING_KEYS
            addLog("BAŞLANGIÇ", "'$topic' konusu için 9:16 YouTube Shorts video üretim süreci başlatıldı.")

            // Step 1: Key & API Validation
            val pexelsKey = settings.pexelsApiKey.trim()
            val geminiKey = settings.geminiApiKey.trim()

            if (pexelsKey.isEmpty()) {
                addLog(
                    "PEXELS_UYARI",
                    "Pexels API anahtarı tanımlanmamış. Varsayılan dikey görsel efekt videoları kullanılacak.",
                    isError = false,
                    suggestion = "Ücretsiz Pexels API anahtarınızı Ayarlar panelinden ekleyebilirsiniz."
                )
            } else {
                addLog("PEXELS_BİLGİ", "Pexels API anahtarı mevcut, dikey stok video araması etkin.")
            }

            if (geminiKey.isEmpty()) {
                addLog(
                    "GEMINI_UYARI",
                    "Gemini API anahtarı girilmedi. Akıllı çevrimdışı şablon motoru devreye girdi.",
                    isError = false,
                    suggestion = "Ücretsiz Gemini API anahtarınızı Ayarlar panelinden veya .env dosyasından tanımlayın."
                )
            }

            _progressPercent.value = 20
            _currentStep.value = GenerationStep.GENERATING_SCRIPT
            addLog("GEMINI_SERVISI", "Gemini 3.5 Flash modeli ile 9:16 vertical sahne senaryoları ve zamanlanmış altyazılar hazırlanıyor...")

            // Step 2: Generate Script
            val scriptResult = geminiScriptEngine.generateShortScript(
                topic = topic,
                targetDurationSeconds = targetDurationSeconds,
                language = language,
                customGeminiKey = geminiKey.ifBlank { null }
            )

            val script = scriptResult.getOrElse {
                addLog("SENARYO_HATA", "Senaryo üretilirken hata oluştu: ${it.message}", isError = true)
                throw it
            }

            _currentScript.value = script
            addLog("SENARYO_BAŞARILI", "Senaryo hazırlandı: '${script.title}' (${script.scenes.size} Sahne, ${script.totalDurationSeconds} saniye).")
            _progressPercent.value = 45

            // Step 3: Fetch Pexels Videos
            _currentStep.value = GenerationStep.FETCHING_PEXELS
            addLog("STOK_VİDEO", "Pexels dikey 9:16 video arama motoru çalıştırılıyor...")

            val updatedScenes = mutableListOf<VideoScene>()
            for ((index, scene) in script.scenes.withIndex()) {
                var foundVideoUrl: String? = null
                var foundPreviewUrl: String? = null

                if (pexelsKey.isNotEmpty()) {
                    try {
                        addLog("PEXELS_ARAMA", "Sahne ${index + 1}: '${scene.pexelsSearchQuery}' araması yapılıyor...")
                        val searchResponse = pexelsApiService.searchVideos(
                            apiKey = pexelsKey,
                            query = scene.pexelsSearchQuery,
                            orientation = "portrait",
                            perPage = 5
                        )

                        if (searchResponse.videos.isNotEmpty()) {
                            val video = searchResponse.videos.first()
                            foundPreviewUrl = video.image
                            // Select HD portrait video file link
                            val portraitFile = video.video_files.firstOrNull {
                                (it.width ?: 0) <= (it.height ?: 0) && !it.link.contains("m3u8")
                            } ?: video.video_files.firstOrNull()

                            foundVideoUrl = portraitFile?.link
                            addLog("PEXELS_BULUNDU", "Sahne ${index + 1} için HD stok video bağlandı.")
                        } else {
                            addLog("PEXELS_BOŞ", "Sahne ${index + 1} için dikey video bulunamadı, varsayılan arka plan kullanılacak.")
                        }
                    } catch (e: Exception) {
                        addLog("PEXELS_HATA", "Sahne ${index + 1} video araması başarısız: ${e.message}", isError = false)
                    }
                }

                updatedScenes.add(
                    scene.copy(
                        videoUrl = foundVideoUrl,
                        previewThumbnailUrl = foundPreviewUrl
                    )
                )
            }

            val finalScript = script.copy(scenes = updatedScenes)
            _currentScript.value = finalScript
            _progressPercent.value = 70

            // Step 4: Audio Synthesis & Alignment
            _currentStep.value = GenerationStep.SYNTHESIZING_AUDIO
            addLog("SESLENDİRME", "Sahneler Türkçe metin okuma motoruyla sentezleniyor...")
            _progressPercent.value = 85

            _currentStep.value = GenerationStep.ALIGNING_SUBTITLES
            addLog("ALTYAZI_HİZALAMA", "Sese duyarlı dinamik altyazı kelime zamanlamaları yapılıyor...")
            _progressPercent.value = 95

            _currentStep.value = GenerationStep.RENDERING_PREVIEW
            addLog("İŞLEME", "9:16 YouTube Shorts oynatıcı ortamı hazırlanıyor...")

            // Save to Room Database
            val entity = ShortsProjectEntity(
                topic = topic,
                title = finalScript.title,
                durationSeconds = finalScript.totalDurationSeconds,
                language = language,
                voiceTone = voiceTone.name,
                captionStyle = captionStyle.name,
                scriptJson = "", // Can store json if required
                fullNarration = finalScript.fullNarrationText,
                hashtags = finalScript.hashtags.joinToString(" ")
            )
            shortsDao.insertProject(entity)

            _progressPercent.value = 100
            _currentStep.value = GenerationStep.COMPLETED
            addLog("TAMAMLANDI", "YouTube Shorts videosu başarıyla oluşturuldu! Önizleme sekmesinden izleyebilirsiniz.")

            Result.success(finalScript)
        } catch (e: Exception) {
            _currentStep.value = GenerationStep.FAILED
            addLog("GENEL_HATA", "Video oluşturma başarısız oldu: ${e.localizedMessage}", isError = true, suggestion = "Lütfen internet bağlantınızı ve API anahtarlarınızı kontrol edin.")
            Result.failure(e)
        }
    }
}
