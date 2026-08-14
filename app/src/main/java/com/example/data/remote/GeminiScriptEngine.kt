package com.example.data.remote

import com.example.BuildConfig
import com.example.model.ShortScript
import com.example.model.TimedSubtitle
import com.example.model.VideoScene
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiScriptEngine {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateShortScript(
        topic: String,
        targetDurationSeconds: Int = 30,
        language: String = "tr",
        customGeminiKey: String? = null
    ): Result<ShortScript> = withContext(Dispatchers.IO) {
        val apiKey = when {
            !customGeminiKey.isNullOrBlank() && customGeminiKey != "MY_GEMINI_API_KEY" -> customGeminiKey
            BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
            else -> null
        }

        if (apiKey.isNullOrEmpty()) {
            // HATA: API anahtarı olmadan işlem yapılamaz
            return@withContext Result.failure(
                Exception("❌ Gemini API anahtarı gereklidir. Ayarlar panelinden API anahtarınızı girin.")
            )
        }

        try {
            val systemPrompt = """
                You are a professional YouTube Shorts viral video scriptwriter and director.
                Create a high-retention 9:16 vertical video script for the given topic.
                Language: $language
                Target Duration: $targetDurationSeconds seconds (Max 60 seconds)
                
                Respond strictly in JSON format matching this schema:
                {
                  "title": "Viral Clickbait Short Title",
                  "hashtags": ["#Shorts", "#Trend", "#Bilgi"],
                  "fullNarrationText": "Full narration spoken text...",
                  "scenes": [
                    {
                      "index": 1,
                      "title": "Hook Scene",
                      "narrationText": "Did you know...",
                      "pexelsSearchQuery": "english search term for 9x16 vertical stock video clip",
                      "durationSeconds": 6
                    }
                  ]
                }
                
                Rules:
                1. pexelsSearchQuery MUST be in English for best Pexels search results (e.g. 'pyramid sunset 4k', 'space galaxy futuristic').
                2. Divide the narration into 3 to 6 logical scenes fitting the target duration of $targetDurationSeconds seconds.
                3. Do NOT include markdown code fences or extra text, return raw JSON string.
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "Topic: $topic\nLanguage: $language\nDuration: $targetDurationSeconds seconds"))
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemPrompt))
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("responseMimeType", "application/json")
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
                return@withContext Result.failure(
                    Exception("❌ Gemini API isteği başarısız. Durum: ${response.code}. Lütfen API anahtarınızı ve internet bağlantısını kontrol edin.")
                )
            }

            val responseJson = JSONObject(responseBody)
            val candidates = responseJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val textContent = firstCandidate
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")

            if (textContent.isNullOrEmpty()) {
                return@withContext Result.failure(
                    Exception("❌ Gemini API yanıtı boş veya geçersiz. Lütfen tekrar deneyin.")
                )
            }

            // Parse clean JSON text
            val cleanJson = textContent.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsedScript = parseJsonToScript(topic, targetDurationSeconds, language, cleanJson)
            Result.success(parsedScript)

        } catch (e: Exception) {
            // API hatası - fallback YOK
            Result.failure(Exception("❌ Senaryo oluşturma hatası: ${e.message}"))
        }
    }

    private fun parseJsonToScript(
        originalTopic: String,
        targetDurationSeconds: Int,
        language: String,
        jsonString: String
    ): ShortScript {
        val root = JSONObject(jsonString)
        val title = root.optString("title", "$originalTopic - YouTube Short")
        val fullNarration = root.optString("fullNarrationText", originalTopic)
        val hashtagsArray = root.optJSONArray("hashtags")
        val hashtagsList = mutableListOf<String>()
        if (hashtagsArray != null) {
            for (i in 0 until hashtagsArray.length()) {
                hashtagsList.add(hashtagsArray.getString(i))
            }
        } else {
            hashtagsList.addAll(listOf("#Shorts", "#ShortsAI", "#Trend"))
        }

        val scenesArray = root.optJSONArray("scenes")
        val scenesList = mutableListOf<VideoScene>()

        var cumulativeTimeMs = 0L

        if (scenesArray != null) {
            for (i in 0 until scenesArray.length()) {
                val sceneObj = scenesArray.getJSONObject(i)
                val sceneIndex = sceneObj.optInt("index", i + 1)
                val sceneTitle = sceneObj.optString("title", "Sahne $sceneIndex")
                val sceneNarration = sceneObj.optString("narrationText", "")
                val pexelsQuery = sceneObj.optString("pexelsSearchQuery", "cinematic nature portrait")
                val sceneDuration = sceneObj.optInt("durationSeconds", 5)

                // Build timed subtitles for this scene narration
                val words = sceneNarration.split("\\s+".toRegex()).filter { it.isNotBlank() }
                val perWordDurationMs = if (words.isNotEmpty()) (sceneDuration * 1000L) / words.size else 1000L
                val timedSubtitles = mutableListOf<TimedSubtitle>()

                var wordStartMs = cumulativeTimeMs
                for (wIdx in words.indices) {
                    val word = words[wIdx]
                    val wordEndMs = wordStartMs + perWordDurationMs
                    timedSubtitles.add(
                        TimedSubtitle(
                            id = i * 100 + wIdx,
                            text = word,
                            startMs = wordStartMs,
                            endMs = wordEndMs
                        )
                    )
                    wordStartMs = wordEndMs
                }

                cumulativeTimeMs += sceneDuration * 1000L

                scenesList.add(
                    VideoScene(
                        index = sceneIndex,
                        title = sceneTitle,
                        narrationText = sceneNarration,
                        pexelsSearchQuery = pexelsQuery,
                        durationSeconds = sceneDuration,
                        timedSubtitles = timedSubtitles
                    )
                )
            }
        }

        if (scenesList.isEmpty()) {
            return generateFallbackScript(originalTopic, targetDurationSeconds, language)
        }

        return ShortScript(
            topic = originalTopic,
            title = title,
            totalDurationSeconds = (cumulativeTimeMs / 1000L).toInt().coerceAtMost(60),
            language = language,
            scenes = scenesList,
            fullNarrationText = fullNarration,
            hashtags = hashtagsList
        )
    }

    private fun generateFallbackScript(
        topic: String,
        targetDurationSeconds: Int,
        language: String
    ): ShortScript {
        val isTr = language == "tr"
        val title = if (isTr) "Şaşırtıcı Gerçekler: $topic" else "Mindblowing Facts: $topic"
        val hashtags = listOf("#Shorts", "#ShortsAI", "#Trend", "#Bilgi", "#Viral")

        val scene1Text = if (isTr) "$topic hakkında muhtemelen daha önce hiç duymadığınız 3 gizemli gerçek!" else "Here are 3 mindblowing facts about $topic that you never knew!"
        val scene2Text = if (isTr) "Birincisi: Bilim insanlarına göre bu konu göründüğünden çok daha büyüleyici." else "First: Scientists say this concept is way more fascinating than it looks."
        val scene3Text = if (isTr) "İkincisi: Yüzyıllardır çözülemeyen birçok soru işaretini barındırıyor." else "Second: It has contained unresolved mysteries for centuries."
        val scene4Text = if (isTr) "Daha fazlası için kanala abone olmayı ve beğenmeyi unutmayın!" else "Don't forget to like and subscribe for more amazing shorts!"

        val scenes = listOf(
            createScene(1, if (isTr) "Giriş & Dikkat Çekici Kanca" else "Hook Scene", scene1Text, "epic cinematic 4k portrait", 6, 0L),
            createScene(2, if (isTr) "Gizemli Gerçek 1" else "Fact 1", scene2Text, "mysterious space universe vertical", 8, 6000L),
            createScene(3, if (isTr) "Gizemli Gerçek 2" else "Fact 2", scene3Text, "future technology abstract neon 9x16", 8, 14000L),
            createScene(4, if (isTr) "Kapanış & Abone Ol" else "Call to Action", scene4Text, "happy energetic urban vertical", 6, 22000L)
        )

        val fullNarration = scenes.joinToString(" ") { it.narrationText }

        return ShortScript(
            topic = topic,
            title = title,
            totalDurationSeconds = 28,
            language = language,
            scenes = scenes,
            fullNarrationText = fullNarration,
            hashtags = hashtags
        )
    }

    private fun createScene(
        index: Int,
        title: String,
        narration: String,
        pexelsQuery: String,
        durationSeconds: Int,
        startOffsetMs: Long
    ): VideoScene {
        val words = narration.split(" ")
        val perWordMs = if (words.isNotEmpty()) (durationSeconds * 1000L) / words.size else 500L
        val timedSubs = words.mapIndexed { wIdx, word ->
            TimedSubtitle(
                id = index * 100 + wIdx,
                text = word,
                startMs = startOffsetMs + (wIdx * perWordMs),
                endMs = startOffsetMs + ((wIdx + 1) * perWordMs)
            )
        }

        return VideoScene(
            index = index,
            title = title,
            narrationText = narration,
            pexelsSearchQuery = pexelsQuery,
            durationSeconds = durationSeconds,
            timedSubtitles = timedSubs
        )
    }

}
