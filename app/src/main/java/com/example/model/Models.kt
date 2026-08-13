package com.example.model

import androidx.compose.ui.graphics.Color
import com.squareup.moshi.JsonClass

enum class CaptionStyle(val displayName: String, val textColor: Color, val bgColor: Color) {
    HIGHLIGHT_YELLOW("Sarı Vurgu", Color(0xFFFFEB3B), Color(0xCC000000)),
    NEON_CYAN("Neon Mavi", Color(0xFF00E5FF), Color(0xE60F172A)),
    MINIMAL_WHITE("Sade Beyaz", Color.White, Color(0x99000000)),
    BOX_MAGENTA("Pembe Kutulu", Color.White, Color(0xDDE11D48))
}

enum class VoiceTone(val displayName: String, val pitch: Float, val speechRate: Float) {
    ENERGETIC("Enerjik & Coşkulu", 1.2f, 1.15f),
    STORYTELLER("Hikaye Anlatıcı", 0.95f, 0.95f),
    DRAMATIC("Dramatik & Derin", 0.8f, 0.9f),
    CALM("Sakin & Bilgilendirici", 1.0f, 1.0f)
}

@JsonClass(generateAdapter = true)
data class TimedSubtitle(
    val id: Int,
    val text: String,
    val startMs: Long,
    val endMs: Long,
    val isHighlighted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class VideoScene(
    val index: Int,
    val title: String,
    val narrationText: String,
    val pexelsSearchQuery: String,
    val durationSeconds: Int,
    val videoUrl: String? = null,
    val previewThumbnailUrl: String? = null,
    val timedSubtitles: List<TimedSubtitle> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ShortScript(
    val topic: String,
    val title: String,
    val totalDurationSeconds: Int,
    val language: String,
    val scenes: List<VideoScene>,
    val fullNarrationText: String,
    val hashtags: List<String>
)

enum class GenerationStep(val stepName: String, val description: String) {
    IDLE("Hazır", "Konu girilmesi bekleniyor"),
    VALIDATING_KEYS("Anahtar Kontrolü", "API anahtarları ve servis bağlantıları doğrulanıyor"),
    GENERATING_SCRIPT("AI Senaryo Oluşturma", "Gemini 3.5 Flash ile 9:16 sahne senaryoları hazırlanıyor"),
    FETCHING_PEXELS("Pexels Stok Video", "Konuya uygun 9:16 dikey stok videolar indiriliyor"),
    SYNTHESIZING_AUDIO("Seslendirme & TTS", "Sahneler Türkçe sese çevriliyor"),
    ALIGNING_SUBTITLES("Altyazı Senkronizasyonu", "Ses ile kelime kelime altyazı zamanlaması yapılıyor"),
    RENDERING_PREVIEW("Video İşleme", "9:16 Shorts video önizlemesi hazırlanıyor"),
    COMPLETED("Tamamlandı", "YouTube Shorts videosu hazır!"),
    FAILED("Hata Oluştu", "Süreç sırasında bir sorun tespit edildi")
}

data class LogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val isError: Boolean = false,
    val suggestion: String? = null
)

data class AppSettings(
    val pexelsApiKey: String = "",
    val geminiApiKey: String = "",
    val defaultLanguage: String = "tr",
    val defaultDuration: Int = 30,
    val defaultVoiceTone: VoiceTone = VoiceTone.ENERGETIC,
    val defaultCaptionStyle: CaptionStyle = CaptionStyle.HIGHLIGHT_YELLOW
)

// Pexels API Models
@JsonClass(generateAdapter = true)
data class PexelsVideoFile(
    val id: Long,
    val quality: String?,
    val file_type: String?,
    val width: Int?,
    val height: Int?,
    val link: String
)

@JsonClass(generateAdapter = true)
data class PexelsVideoPictures(
    val id: Long,
    val picture: String,
    val nr: Int
)

@JsonClass(generateAdapter = true)
data class PexelsVideo(
    val id: Long,
    val width: Int,
    val height: Int,
    val url: String,
    val image: String,
    val duration: Int,
    val video_files: List<PexelsVideoFile>,
    val video_pictures: List<PexelsVideoPictures>
)

@JsonClass(generateAdapter = true)
data class PexelsSearchResponse(
    val page: Int,
    val per_page: Int,
    val total_results: Int,
    val url: String?,
    val videos: List<PexelsVideo>
)
