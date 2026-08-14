# YouTube Shorts AI Video Generator - Sistem Mimarisi

Bu dokümantasyon, **Yapay Zeka destekli YouTube Shorts video üretim sistemi**nin nasıl çalıştığını detaylı olarak açıklamaktadır.

## 📋 İçindekiler
1. [Sistem Genel Bakış](#sistem-genel-bakış)
2. [Video Oluşturma Pipeline'ı](#video-oluşturma-pipelinı)
3. [Yazılım Mimarisi](#yazılım-mimarisi)
4. [Veri Modelleri](#veri-modelleri)
5. [API İntegrasyonları](#api-integrasyonları)
6. [Altyazı Senkronizasyonu](#altyazı-senkronizasyonu)
7. [Python Motoru](#python-motoru)

---

## 🎬 Sistem Genel Bakış

Bu Android uygulaması, **9:16 dikey format YouTube Shorts videolarını** otomatik olarak üretir. Kullanıcı sadece bir konu (topic) girerek tüm süreci başlatır ve sistem şunları otomatik yapar:

- ✅ **Gemini AI** ile senaryo ve sahne yazısı oluşturma
- ✅ **Pexels API** ile 9:16 stok videoları indirme
- ✅ **Text-to-Speech** ile Türkçe seslendirme
- ✅ **Altyazı senkronizasyonu** (kelime kelime)
- ✅ **Video işleme** ve dışa aktarma

---

## 🔄 Video Oluşturma Pipeline'ı

Sistem **8 adımlı bir pipeline** izler:

```
1️⃣ VALIDATING_KEYS (API Anahtar Doğrulama)
   ↓
2️⃣ GENERATING_SCRIPT (Gemini ile Senaryo)
   ↓
3️⃣ FETCHING_PEXELS (Stok Video İndirme)
   ↓
4️⃣ SYNTHESIZING_AUDIO (TTS - Seslendirme)
   ↓
5️⃣ ALIGNING_SUBTITLES (Altyazı Senkronizasyonu)
   ↓
6️⃣ RENDERING_PREVIEW (Video İşleme)
   ↓
7️⃣ COMPLETED (Tamamlandı)
   ↓
❌ FAILED (Hata)
```

### Adım Detayları

#### 1. **API Anahtar Doğrulama** (VALIDATING_KEYS)
```kotlin
- Pexels API anahtarı kontrolü
- Gemini API anahtarı kontrolü
- Düzeltme önerileri ve fallback mekanizmaları
```

#### 2. **Gemini ile Senaryo Oluşturma** (GENERATING_SCRIPT)
Gemini 3.5 Flash modeli şu görevleri yapar:

```json
{
  "title": "Viral Video Başlığı",
  "hashtags": ["#Shorts", "#Bilgi"],
  "fullNarrationText": "Tüm video seslendirmesi...",
  "scenes": [
    {
      "index": 1,
      "title": "Kanca Sahnesi (Hook)",
      "narrationText": "Bildin mi...",
      "pexelsSearchQuery": "pyramid sunset 4k",
      "durationSeconds": 6
    },
    {
      "index": 2,
      "title": "Bilgi Sahnesi",
      "narrationText": "Bilim insanları...",
      "pexelsSearchQuery": "space galaxy 4k",
      "durationSeconds": 8
    }
  ]
}
```

**Önemli:** `pexelsSearchQuery` İNGİLİZCE olmalıdır (Pexels arama motoru İngilizce sorgulara en iyi yanıt verir).

#### 3. **Pexels Stok Video İndirme** (FETCHING_PEXELS)
Her sahne için:
- Pexels API'ye İngilizce arama sorgusu gönderme
- **9:16 dikey (vertical) formatında video** filtreleme
- Video dosyası indirme ve lokal depolama

```kotlin
fun downloadPexelsVideo(query: String): Result<File> {
    // 1. API'ye GET isteği
    // 2. 9:16 ratio'lu videoları filtreleme
    // 3. En yüksek kalite dosyasını seçme
    // 4. İndirme ve depolama
}
```

#### 4. **Seslendirme (Text-to-Speech)** (SYNTHESIZING_AUDIO)
Android cihazının TTS motorunu kullanır:
- **Dil:** Türkçe (tr-TR)
- **Ses Tonu:** Kullanıcı seçimine bağlı
  - 🔊 Enerjik & Coşkulu (pitch: 1.2, speechRate: 1.15)
  - 📖 Hikaye Anlatıcı (pitch: 0.95, speechRate: 0.95)
  - 🎭 Dramatik & Derin (pitch: 0.8, speechRate: 0.9)
  - 😌 Sakin & Bilgilendirici (pitch: 1.0, speechRate: 1.0)

#### 5. **Altyazı Senkronizasyonu** (ALIGNING_SUBTITLES)
En kompleks adım - ses dosyasından altyazı oluşturur:

```kotlin
data class TimedSubtitle(
    val id: Int,
    val text: String,        // "Bildin"
    val startMs: Long,       // 1000ms
    val endMs: Long,         // 1500ms
    val isHighlighted: Boolean
)
```

Sistem:
1. Ses dosyasını analiz eder
2. Kelime sürelerini hesaplar
3. Altyazı stilleri uygulanır (Sarı Vurgu, Neon Mavi, vb.)

#### 6. **Video İşleme** (RENDERING_PREVIEW)
Preview ekranında gerçek zamanlı video görüntüleme:
- Stok video oynatma
- Altyazıları senkronize şekilde gösterme
- Ses oynatma

---

## 🏗️ Yazılım Mimarisi

Proje **katmanlı mimariye** (Layered Architecture) sahiptir:

```
📱 UI LAYER (Compose)
   ├── StudioScreen (Video oluşturma formu)
   ├── PreviewScreen (Oluşturulan videoyu izleme)
   ├── GalleryScreen (Kaydedilen projeleri listeleme)
   ├── PythonColabScreen (Python kodu çalıştırma)
   └── GitHubActionsScreen (GitHub Actions entegrasyonu)
   
📊 VIEWMODEL LAYER
   └── Ana state management ve business logic
   
🔧 REPOSITORY LAYER
   ├── ShortsRepository (Veri kaynağı koordinatörü)
   └── Tüm veri operasyonlarını koordine eder
   
⚙️ ENGINE LAYER (Business Logic)
   ├── ShortsEngine (Ana orchestrator)
   ├── GeminiScriptEngine (Senaryo AI)
   ├── TextToSpeechEngine (Seslendirme)
   ├── PexelsApiService (Stok video API)
   └── PythonCodeGenerator (Python kodu üretim)
   
💾 DATA LAYER
   ├── LOCAL: AppDatabase + ShortsDao (Room)
   ├── REMOTE: REST APIs (Gemini, Pexels)
   └── MODELS: Veri sınıfları
```

### ShortsEngine - Ana Orkestratör

`ShortsEngine.kt`, tüm adımları koordine eden merkezi bileşendir:

```kotlin
suspend fun generateShortsVideo(
    topic: String,
    targetDurationSeconds: Int = 30,
    language: String = "tr",
    voiceTone: VoiceTone = VoiceTone.ENERGETIC,
    captionStyle: CaptionStyle = CaptionStyle.HIGHLIGHT_YELLOW,
    settings: AppSettings
): Result<ShortScript>
```

**İş akışı:**
1. Adımı güncelle → `_currentStep.value = GenerationStep.GENERATING_SCRIPT`
2. İlerlemeyi güncelle → `_progressPercent.value = 20`
3. Log ekle → `addLog("TAG", "Mesaj")`
4. Servis çağrı yap (GeminiScriptEngine, PexelsApiService)
5. Başarı/Hata sonucunu döndür

---

## 📦 Veri Modelleri

### ShortScript (Ana Veri Yapısı)
```kotlin
data class ShortScript(
    val topic: String,                      // "Antik Mısır Piramitleri"
    val title: String,                      // "Bilinmeyen Gerçekler"
    val totalDurationSeconds: Int,          // 30
    val language: String,                   // "tr"
    val scenes: List<VideoScene>,           // Sahneler listesi
    val fullNarrationText: String,          // Tüm seslendirme metni
    val hashtags: List<String>              // ["#Shorts", "#Bilgi"]
)
```

### VideoScene (Sahne)
```kotlin
data class VideoScene(
    val index: Int,                         // 1, 2, 3...
    val title: String,                      // "Kanca Sahnesi"
    val narrationText: String,              // Sahne seslendirmesi
    val pexelsSearchQuery: String,          // Stok video arama terimi
    val durationSeconds: Int,               // Sahne süresi
    val videoUrl: String?,                  // İndirilen video dosyası
    val previewThumbnailUrl: String?,       // Thumbnail (resim)
    val timedSubtitles: List<TimedSubtitle>// Zamanlanmış altyazılar
)
```

### TimedSubtitle (Altyazı)
```kotlin
data class TimedSubtitle(
    val id: Int,
    val text: String,                       // "Bilinmeyen"
    val startMs: Long,                      // 1000ms (başlangıç)
    val endMs: Long,                        // 1800ms (bitiş)
    val isHighlighted: Boolean              // Vurgulu mu?
)
```

### CaptionStyle (Altyazı Görünümü)
```kotlin
enum class CaptionStyle(
    val displayName: String,
    val textColor: Color,
    val bgColor: Color
) {
    HIGHLIGHT_YELLOW("Sarı Vurgu", Color.Yellow, Color.Black),
    NEON_CYAN("Neon Mavi", Color.Cyan, Color.DarkBlue),
    MINIMAL_WHITE("Sade Beyaz", Color.White, Color.Transparent),
    BOX_MAGENTA("Pembe Kutulu", Color.White, Color.Magenta)
}
```

---

## 🌐 API İntegrasyonları

### 1. Gemini API (Google AI)

**Endpoint:** 
```
https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent
```

**Görev:** Senaryo ve sahne yazısı oluşturma

**Request:**
```json
{
  "contents": [{
    "parts": [{
      "text": "Topic: Antik Mısır Piramitleri\nLanguage: tr\nDuration: 30 seconds"
    }]
  }],
  "systemInstruction": {
    "parts": [{
      "text": "Professional YouTube Shorts scriptwriter..."
    }]
  },
  "generationConfig": {
    "temperature": 0.7,
    "responseMimeType": "application/json"
  }
}
```

**Response:** Yukarıdaki `ShortScript` JSON'i

**Fallback:** API başarısız olursa, dahili şablon kullanılır

### 2. Pexels API

**Endpoint:**
```
https://api.pexels.com/videos/search?query={query}&per_page=15&page=1
```

**Görev:** 9:16 dikey stok videoları bulma ve indirme

**Query Parametreleri:**
- `query`: İngilizce arama terimi (ör: "pyramid sunset 4k")
- `per_page`: Kaç video dönsün (15 varsayılan)
- `orientation`: "portrait" (dikey format)

**Response:**
```json
{
  "videos": [{
    "id": 12345,
    "video_files": [{
      "quality": "hd",
      "width": 1080,
      "height": 1920,
      "link": "https://..."
    }],
    "video_pictures": [{
      "picture": "https://..." // thumbnail
    }]
  }]
}
```

---

## 📝 Altyazı Senkronizasyonu

### Problem
Kullanıcı "Bildin mi?" kelimesini söylüyor, bu kelime kaç milisaniyede sona eriyor?

### Çözüm: Ses Analizi Pipeline'ı

```
1. Android TTS → WAV/MP3 ses dosyası
2. Ses dosyasını analiz et
3. Sessiz alanları tespit et
4. Kelime sürelerini hesapla
5. TimedSubtitle nesneleri oluştur
```

### Implementasyon (TextToSpeechEngine.kt)

```kotlin
class TextToSpeechEngine(private val context: Context) {
    
    private var tts: TextToSpeech? = null
    
    fun speak(
        text: String,
        voiceTone: VoiceTone = VoiceTone.ENERGETIC,
        language: String = "tr"
    ) {
        tts?.language = Locale("tr", "TR")
        tts?.setPitch(voiceTone.pitch)           // Ses perdesi
        tts?.setSpeechRate(voiceTone.speechRate) // Konuşma hızı
        
        val utteranceId = "shorts_tts_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }
}
```

### PreviewScreen - Altyazı Gösterimi

```kotlin
val words = currentScene.timedSubtitles

// Aktif olan kelimeyi vurgula
for (idx in startIdx..endIdx) {
    val sub = words[idx]
    val isWordHighlighted = idx == activeWordIndex
    
    Text(
        text = sub.text,
        fontSize = if (isWordHighlighted) 18.sp else 15.sp,
        fontWeight = if (isWordHighlighted) FontWeight.ExtraBold else FontWeight.Medium,
        color = if (isWordHighlighted) captionStyle.textColor else Color.White,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}
```

---

## 🐍 Python Motoru

Sistem, Google Colab'da çalışabilen **otomatik Python kodu üretir**. Bu, CloudFlare Workers, GitHub Actions vb. platformlarda video oluşturmayı sağlar.

### PythonCodeGenerator.kt

```kotlin
object PythonCodeGenerator {
    fun generatePythonScript(
        topic: String,
        pexelsApiKey: String,
        geminiApiKey: String
    ): String { /* ... */ }
}
```

### Üretilen Python Kodu Yapısı

#### 1. Kütüphane Kurulumu
```python
# !pip install google-genai requests gTTS moviepy pillow pexels-api
import os, json, requests
from gtts import gTTS
import moviepy.editor as mp
```

#### 2. Gemini Senaryo Oluşturma
```python
def generate_script_with_gemini(topic):
    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key={GEMINI_API_KEY}"
    payload = {
        "contents": [{...}],
        "generationConfig": {"responseMimeType": "application/json"}
    }
    res = requests.post(url, json=payload)
    return res.json()
```

#### 3. Pexels Video İndirme
```python
def download_pexels_video(query, scene_index):
    headers = {"Authorization": PEXELS_API_KEY}
    params = {"query": query, "per_page": 15, "orientation": "portrait"}
    res = requests.get("https://api.pexels.com/videos/search", 
                       params=params, headers=headers)
    # En kaliteli 9:16 videoyu indir
```

#### 4. TTS ve Altyazı Oluşturma
```python
def create_tts_with_subtitles(narration):
    tts = gTTS(text=narration, lang='tr', slow=False)
    tts.save("audio.mp3")
    
    # Web Speech API ile altyazı zamanlaması
    # (veya stabil-ts kütüphanesi)
```

#### 5. Video Bileştirme
```python
def compose_video(scenes, output_file="output.mp4"):
    clips = []
    for scene in scenes:
        # Arka plan videosu
        video_clip = mp.VideoFileClip(scene.video_url)
        
        # Ses
        audio_clip = mp.AudioFileClip(scene.audio_file)
        
        # Altyazılar
        subtitles_clip = create_subtitle_clip(scene.subtitles)
        
        # Bileştir
        final_clip = CompositeVideoClip([video_clip, subtitles_clip])
        clips.append(final_clip)
    
    # Sahne birleştir
    final = concatenate_videoclips(clips)
    final.write_videofile(output_file, fps=30)
```

### Çalıştırma Ortamları

- ✅ **Google Colab** (Ücretsiz GPU)
- ✅ **GitHub Actions** (CI/CD)
- ✅ **Linux/macOS/Windows** (Yerel)
- ✅ **Docker** (Konteyner)

---

## 🔗 Veri Akışı (Data Flow)

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. USER INPUT (Kullanıcı Girişi)                                │
│    Topic: "Antik Mısır Piramitleri"                            │
│    Duration: 30 seconds                                         │
│    Language: Türkçe                                             │
└──────────────────────┬──────────────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2. GEMINI API                                                   │
│    → Senaryo & Sahne Yazısı                                    │
│    → pexelsSearchQuery (İngilizce)                             │
│    → Narration Metni (Türkçe)                                  │
└──────────────────────┬──────────────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────────────┐
│ 3. PEXELS API                                                   │
│    → Dahil: 9:16 Dikey Video URL                               │
│    → Dahil: Thumbnail (Resim)                                  │
└──────────────────────┬──────────────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────────────┐
│ 4. TTS ENGINE (Seslendirme)                                     │
│    → Narration Metni → Ses Dosyası (MP3/WAV)                  │
│    → Ses Süresi Analizi                                        │
└──────────────────────┬──────────────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────────────┐
│ 5. SUBTITLE SYNC                                                │
│    → Ses Dosyası → TimedSubtitle Nesneleri                    │
│    → Her Kelime: [startMs, endMs]                              │
└──────────────────────┬──────────────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────────────┐
│ 6. PREVIEW SCREEN                                               │
│    → Video Oynatma                                              │
│    → Altyazı Senkronizasyonu                                    │
│    → Kanca-Bilgi-CTA Sunuş                                      │
└──────────────────────┬──────────────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────────────┐
│ 7. EXPORT                                                       │
│    → MP4 Video (9:16 format)                                    │
│    → ShortScript JSON Kütüphane Depolanır                      │
│    → GitHub/YouTube Hazırlanır                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Teknik Stack

| Katman | Teknoloji |
|--------|-----------|
| **UI Framework** | Jetpack Compose |
| **Language** | Kotlin |
| **Database** | Room (SQLite) |
| **HTTP Client** | OkHttp + Retrofit |
| **JSON Parsing** | Moshi |
| **AI/ML** | Gemini API (Google) |
| **Media** | Pexels API, Android MediaStore |
| **TTS** | Android TextToSpeech |
| **Async** | Coroutines (suspend functions) |
| **State Management** | Flow, StateFlow, MutableStateFlow |

---

## 📊 Örnek Durum (Scenario)

### Girdi
```
Konu: "Yapay Zeka Devrimi"
Süre: 30 saniye
Dil: Türkçe
Ses Tonu: Enerjik
Altyazı Stili: Sarı Vurgu
```

### İşlem Süreci (Sonuç)

**Gemini Çıktısı:**
```json
{
  "title": "AI 2024'te Hayatı Değiştiriyor!",
  "scenes": [
    {
      "narrationText": "Bildin mi? Yapay zeka artık işyerinin %50'sini dönüştürüyor!",
      "pexelsSearchQuery": "futuristic technology ai",
      "durationSeconds": 8
    },
    {
      "narrationText": "Bilim insanları 2025'in en büyük yıl olacağını söylüyor.",
      "pexelsSearchQuery": "artificial intelligence neural network",
      "durationSeconds": 8
    },
    {
      "narrationText": "Hazırlanıyor musunuz? Videoya yorum yapın!",
      "pexelsSearchQuery": "happy tech startup",
      "durationSeconds": 6
    }
  ]
}
```

**Pexels Çıktısı:**
```
Sahne 1: futuristic_tech.mp4 (1080x1920)
Sahne 2: neural_network.mp4 (1080x1920)
Sahne 3: startup_team.mp4 (1080x1920)
```

**TTS + Altyazı:**
```
0-800ms:    "Bildin mi?"
800-1600ms: "Yapay zeka"
1600-2400ms: "artık işyerinin"
...
7200-8000ms: "dönüştürüyor!"
```

**Final Video:**
- 9:16 dikey format
- 3 sahne = 22 saniye video
- Sarı renk altyazılar, kelime kelime vurgulu
- YouTube Shorts'a yüklemeye hazır ✅

---

## 🚀 Başlatılan Teknolojiler

1. **Google Generative AI (Gemini)** - Senaryo yazarlığı
2. **Pexels API** - Ücretsiz stok video kütüphanesi
3. **Android TextToSpeech** - Cihaz TTS motoru
4. **MoviePy** (Python) - Video bileştirme
5. **Google Colab** - Bulut video işleme

---

## 📞 Hata Yönetimi

Sistem **üç katmanlı hata işleme** sunar:

### 1. API Seviyesi
```kotlin
if (geminiKey.isEmpty()) {
    addLog("GEMINI_UYARI", "API anahtarı eksik. Çevrimdışı mod kullanılıyor.")
    return generateFallbackScript()
}
```

### 2. İşlem Seviyesi
```kotlin
val scriptResult = geminiScriptEngine.generateShortScript(...)
if (scriptResult.isFailure) {
    _currentStep.value = GenerationStep.FAILED
    addLog("GEMINI_HATA", scriptResult.exceptionOrNull()?.message ?: "Bilinmeyen hata")
}
```

### 3. UI Seviyesi
```kotlin
ErrorDiagnosticDialog(
    logs = logs,  // Tüm tanı logları
    onDismiss = { /* ... */ }
)
```

---

## 🎯 Özet

Bu sistem, **en son AI teknolojilerini** bir araya getirerek:
- ✅ Saniyeler içinde viral video senaryosu yazıyor
- ✅ Uygun stok videolar otomatik buluyor
- ✅ Türkçe seslendirme ve altyazı yapıyor
- ✅ Profesyonel 9:16 video işliyor
- ✅ YouTube Shorts'a yayın hazır video üretiyor

**Teknolojisi:** Gemini AI + Pexels + Android TTS + Python MoviePy

**Kullanım:** "Konu gir → Video al" 🎬
