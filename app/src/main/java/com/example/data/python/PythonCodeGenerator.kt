package com.example.data.python

object PythonCodeGenerator {

    fun generatePythonScript(
        topic: String = "Antik Mısır Piramitlerinin Gizemi",
        pexelsApiKey: String = "",
        geminiApiKey: String = ""
    ): String {
        return """
# ==============================================================================
# YOUTUBE SHORTS AUTOMATED VIDEO GENERATOR (9:16 Vertical Shorts Engine)
# Works on Google Colab, Linux, macOS, and Windows (Free tier compatible)
# ==============================================================================
# Requirements:
# !pip install google-genai requests gTTS moviepy pillow pexels-api

import os
import sys
import json
import time
import requests
from gtts import gTTS
import moviepy.editor as mp
from moviepy.video.VideoClip import ColorClip
from moviepy.video.compositing.CompositeVideoClip import CompositeVideoClip
from moviepy.video.tools.subtitles import SubtitlesClip
from moviepy.config import change_settings

# --- CONFIGURATION & API KEYS ---
PEXELS_API_KEY = "${pexelsApiKey.ifBlank { "YOUR_PEXELS_API_KEY_HERE" }}"
GEMINI_API_KEY = "${geminiApiKey.ifBlank { "YOUR_GEMINI_API_KEY_HERE" }}"
TOPIC = "$topic"
OUTPUT_FILE = "youtube_short_9x16.mp4"

print("🚀 YouTube Shorts Video Oluşturucu Başlatılıyor...")
print(f"📌 Konu: {TOPIC}")

# --- STRICT ERROR CHECKING FUNCTION ---
def check_environment():
    print("🔍 [1/6] Sistem & API Anahtarları Denetleniyor...")
    if PEXELS_API_KEY == "YOUR_PEXELS_API_KEY_HERE" or not PEXELS_API_KEY:
        print("⚠️ UYARI: Pexels API anahtarı girilmedi. Varsayılan renkli dikey tuval videoları üretilecek.")
    if GEMINI_API_KEY == "YOUR_GEMINI_API_KEY_HERE" or not GEMINI_API_KEY:
        print("⚠️ UYARI: Gemini API anahtarı girilmedi. Dahili akıllı şablon senaryosu kullanılacak.")

check_environment()

# --- STEP 1: GEMINI SCRIPT & SCENE GENERATOR ---
def generate_script_with_gemini(topic):
    print("🤖 [2/6] Gemini AI ile 9:16 Senaryo Hazırlanıyor...")
    if GEMINI_API_KEY and GEMINI_API_KEY != "YOUR_GEMINI_API_KEY_HERE":
        url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key={GEMINI_API_KEY}"
        headers = {"Content-Type": "application/json"}
        prompt = f'''
        You are a YouTube Shorts video producer. Create a viral 30-second 9:16 vertical video script about '{topic}'.
        Return STRICT JSON format:
        {{
            "title": "Shorts Title",
            "scenes": [
                {{"index": 1, "narration": "Hook line text...", "search_term": "english stock query", "duration": 6}},
                {{"index": 2, "narration": "Fact 1 text...", "search_term": "english stock query", "duration": 8}},
                {{"index": 3, "narration": "Fact 2 text...", "search_term": "english stock query", "duration": 8}},
                {{"index": 4, "narration": "Call to action subscribe...", "search_term": "english stock query", "duration": 6}}
            ]
        }}
        Rule: search_term MUST be in English for Pexels search.
        '''
        payload = {
            "contents": [{"parts": [{"text": prompt}]}],
            "generationConfig": {"temperature": 0.7, "responseMimeType": "application/json"}
        }
        try:
            res = requests.post(url, json=payload, headers=headers, timeout=30)
            if res.status_code == 200:
                data = res.json()
                text = data["candidates"][0]["content"]["parts"][0]["text"]
                return json.loads(text.strip().removeprefix("```json").removeprefix("```").removesuffix("```"))
        except Exception as e:
            print(f"⚠️ Gemini API Çağrısında Uyarı: {e}. Çevrimdışı şablon kullanılıyor.")

    # Fallback Script
    return {
        "title": f"Bilinmeyen Gerçekler: {topic}",
        "scenes": [
            {"index": 1, "narration": f"{topic} hakkında bilmeniz gereken harika gerçekler!", "search_term": "epic nature portrait 4k", "duration": 6},
            {"index": 2, "narration": "Birincisi: Bilim insanları bu gizemi çözmek için yıllardır çalışıyor.", "search_term": "space galaxy vertical", "duration": 8},
            {"index": 3, "narration": "İkincisi: Son yapılan araştırmalar ezber bozan sonuçlar verdi.", "search_term": "futuristic technology vertical", "duration": 8},
            {"index": 4, "narration": "Daha fazla içerik için kanala abone olmayı unutmayın!", "search_term": "happy urban cinematic", "duration": 6}
        ]
    }

script_data = generate_script_with_gemini(TOPIC)
print(f"✅ Senaryo Hazır: {script_data.get('title')}")

# --- STEP 2: PEXELS 9:16 VERTICAL VIDEO DOWNLOADER ---
def download_pexels_video(query, scene_index):
    print(f"📹 [3/6] Sahne {scene_index} Stok Video İndiriliyor ('{query}')...")
    filename = f"scene_{scene_index}.mp4"
    if PEXELS_API_KEY and PEXELS_API_KEY != "YOUR_PEXELS_API_KEY_HERE":
        url = f"https://api.pexels.com/videos/search?query={query}&orientation=portrait&per_page=3"
        headers = {"Authorization": PEXELS_API_KEY}
        try:
            r = requests.get(url, headers=headers, timeout=20)
            if r.status_code == 200:
                videos = r.json().get("videos", [])
                if videos:
                    files = videos[0].get("video_files", [])
                    # Find portrait video file
                    file_url = None
                    for f in files:
                        if f.get("width", 0) <= f.get("height", 0):
                            file_url = f.get("link")
                            break
                    if not file_url and files:
                        file_url = files[0].get("link")

                    if file_url:
                        video_bytes = requests.get(file_url, timeout=30).content
                        with open(filename, "wb") as f:
                            f.write(video_bytes)
                        print(f"✅ Sahne {scene_index} stok videosu indirildi.")
                        return filename
        except Exception as e:
            print(f"⚠️ Pexels video indirme hatası: {e}")

    # Fallback procedural 9:16 clip if Pexels fails or no key
    print(f"🎨 Sahne {scene_index} için renkli 9:16 dikey tuval oluşturuluyor...")
    colors = [(30, 27, 75), (88, 28, 135), (159, 18, 57), (15, 23, 42)]
    bg_clip = ColorClip(size=(1080, 1920), color=colors[(scene_index-1) % len(colors)], duration=8)
    bg_clip.write_videofile(filename, fps=24, codec="libx264", logger=None)
    return filename

# --- STEP 3: TTS VOICE SYNTHESIS ---
def generate_voiceover(text, scene_index):
    print(f"🎙️ [4/6] Sahne {scene_index} Türkçe Seslendiriliyor...")
    audio_file = f"audio_{scene_index}.mp3"
    try:
        tts = gTTS(text=text, lang='tr', slow=False)
        tts.save(audio_file)
        return audio_file
    except Exception as e:
        print(f"⚠️ TTS Seslendirme hatası: {e}")
        return None

# --- STEP 4: VIDEO & AUDIO COMPOSITION ---
print("🎬 [5/6] Video Sahneleri, Ses ve Altyazılar Birleştiriliyor...")
clips = []
for scene in script_data["scenes"]:
    idx = scene["index"]
    vid_path = download_pexels_video(scene["search_term"], idx)
    aud_path = generate_voiceover(scene["narration"], idx)

    try:
        v_clip = mp.VideoFileClip(vid_path)
        # Crop to 9:16 vertical format (1080x1920) if needed
        (w, h) = v_clip.size
        target_aspect = 9.0 / 16.0
        current_aspect = w / float(h)

        if current_aspect > target_aspect:
            # Crop horizontally
            new_w = int(h * target_aspect)
            x1 = (w - new_w) // 2
            v_clip = v_clip.crop(x1=x1, width=new_w, height=h)
        v_clip = v_clip.resize((1080, 1920))

        if aud_path and os.path.exists(aud_path):
            a_clip = mp.AudioFileClip(aud_path)
            v_clip = v_clip.set_duration(a_clip.duration)
            v_clip = v_clip.set_audio(a_clip)
        else:
            v_clip = v_clip.set_duration(scene["duration"])

        clips.append(v_clip)
    except Exception as e:
        print(f"⚠️ Sahne {idx} işleme hatası: {e}")

if clips:
    final_video = mp.concatenate_videoclips(clips, method="compose")
    print(f"💾 [6/6] Final 9:16 Video İşleniyor ve Kaydediliyor -> {OUTPUT_FILE}")
    final_video.write_videofile(OUTPUT_FILE, fps=24, codec="libx264", audio_codec="aac")
    print("🎉 TEBRİKLER! YouTube Shorts videonuz hazır ve yüklenebilir!")
else:
    print("❌ HATA: İşlenecek geçerli video sahnesi bulunamadı.")
""".trimIndent()
    }

    fun generateColabNotebookJson(
        topic: String = "Antik Mısır Piramitlerinin Gizemi",
        pexelsApiKey: String = "",
        geminiApiKey: String = ""
    ): String {
        val pythonScript = generatePythonScript(topic, pexelsApiKey, geminiApiKey)
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n\"\n        \"")

        return """
{
  "nbformat": 4,
  "nbformat_minor": 0,
  "metadata": {
    "colab": {
      "provenance": [],
      "authorship_tag": "ShortsAIStudio"
    },
    "language_info": {
      "name": "python"
    }
  },
  "cells": [
    {
      "cell_type": "markdown",
      "metadata": {},
      "source": [
        "# 🎬 YouTube Shorts Automatic Video Generator\\n",
        "**AI Studio Shorts Engine** - Automated 9:16 Shorts Generation via Gemini AI & Pexels"
      ]
    },
    {
      "cell_type": "code",
      "execution_count": null,
      "metadata": {},
      "outputs": [],
      "source": [
        "!pip install -q google-genai requests gTTS moviepy pillow pexels-api\\n",
        "!apt-get quiet update && !apt-get quiet install -y ffmpeg"
      ]
    },
    {
      "cell_type": "code",
      "execution_count": null,
      "metadata": {},
      "outputs": [],
      "source": [
        "$pythonScript"
      ]
    }
  ]
}
""".trimIndent()
    }
}
