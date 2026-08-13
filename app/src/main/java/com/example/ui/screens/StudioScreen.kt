package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import com.example.model.CaptionStyle
import com.example.model.GenerationStep
import com.example.model.VoiceTone
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.RedPrimary
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudioScreen(
    topic: String,
    onTopicChange: (String) -> Unit,
    durationSeconds: Int,
    onDurationChange: (Int) -> Unit,
    voiceTone: VoiceTone,
    onVoiceToneChange: (VoiceTone) -> Unit,
    captionStyle: CaptionStyle,
    onCaptionStyleChange: (CaptionStyle) -> Unit,
    language: String,
    onLanguageChange: (String) -> Unit,
    settings: AppSettings,
    currentStep: GenerationStep,
    progressPercent: Int,
    onOpenApiKeys: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onGenerateClick: () -> Unit,
    onRunPythonClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    val samplePrompts = listOf(
        "Antik Mısır Piramitlerinin Gizemi",
        "10 Saniyede Zihinsel Motivasyon",
        "Uzayın Derinliklerindeki Karadelikler",
        "Yapay Zeka 2030'da Dünyayı Nasıl Değiştirecek?",
        "İnsan Beyni Hakkında 3 Şaşırtıcı Bilgi"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Banner Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(RedPrimary.copy(alpha = 0.3f), PurpleAccent.copy(alpha = 0.15f))
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = RedPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "YouTube Shorts Studio AI",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = onOpenDiagnostics,
                            modifier = Modifier.testTag("btn_open_diagnostics")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = "Hata Denetimi",
                                tint = TextSecondary
                            )
                        }
                    }

                    Text(
                        text = "Bir konu girin, Gemini AI senaryoyu yazsın, Pexels 9:16 stok videoları indirsin ve sesli altyazılı Shorts üretsin!",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // API Keys Status Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clickable { onOpenApiKeys() },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = RedPrimary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = "API Bağlantıları (Ücretsiz Plan)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val geminiOk = settings.geminiApiKey.isNotBlank()
                            val pexelsOk = settings.pexelsApiKey.isNotBlank()

                            Text(
                                text = "Gemini AI: ${if (geminiOk) "Bağlı ✓" else "Varsayılan"}",
                                fontSize = 11.sp,
                                color = if (geminiOk) StatusGreen else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Pexels: ${if (pexelsOk) "Bağlı ✓" else "Varsayılan"}",
                                fontSize = 11.sp,
                                color = if (pexelsOk) StatusGreen else TextSecondary
                            )
                        }
                    }
                }

                Text(
                    text = "Düzenle >",
                    fontSize = 12.sp,
                    color = RedPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Topic Input Field
        Text(
            text = "Video Konusu veya Başlığı",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        OutlinedTextField(
            value = topic,
            onValueChange = onTopicChange,
            placeholder = { Text("Örn: Antik Mısır piramitlerinin gizemi...", color = TextSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 10.dp)
                .testTag("input_video_topic"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RedPrimary,
                unfocusedBorderColor = DarkSurfaceVariant,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        // Sample Topic Chips
        Text(
            text = "Örnek Konular:",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            samplePrompts.forEach { prompt ->
                Box(
                    modifier = Modifier
                        .background(DarkSurface, RoundedCornerShape(20.dp))
                        .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(20.dp))
                        .clickable { onTopicChange(prompt) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = prompt,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Video Settings Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Shorts Video Parametreleri (9:16 Dikey)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Duration Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = RedPrimary, modifier = Modifier.padding(end = 6.dp))
                    Text("Hedef Süre (Max 60s):", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    listOf(15, 30, 45, 60).forEach { sec ->
                        FilterChip(
                            selected = durationSeconds == sec,
                            onClick = { onDurationChange(sec) },
                            label = { Text("${sec}s") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RedPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Voice Tone Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = PurpleAccent, modifier = Modifier.padding(end = 6.dp))
                    Text("Ses Tonu & Konuşma Hızı:", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    VoiceTone.entries.forEach { tone ->
                        FilterChip(
                            selected = voiceTone == tone,
                            onClick = { onVoiceToneChange(tone) },
                            label = { Text(tone.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PurpleAccent,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Caption Style Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Subtitles, contentDescription = null, tint = StatusGreen, modifier = Modifier.padding(end = 6.dp))
                    Text("Altyazı Görsel Stili:", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    CaptionStyle.entries.forEach { style ->
                        FilterChip(
                            selected = captionStyle == style,
                            onClick = { onCaptionStyleChange(style) },
                            label = { Text(style.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Generate Button
        Button(
            onClick = onGenerateClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_generate_shorts"),
            enabled = topic.isNotBlank() && currentStep != GenerationStep.GENERATING_SCRIPT && currentStep != GenerationStep.FETCHING_PEXELS,
            colors = ButtonDefaults.buttonColors(
                containerColor = RedPrimary,
                disabledContainerColor = DarkSurfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (currentStep == GenerationStep.IDLE || currentStep == GenerationStep.COMPLETED)
                        "🚀 9:16 Shorts Videosu Oluştur"
                    else "Video Üretiliyor (%$progressPercent)...",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Secondary Action Button: Python 3.14.0 Live Engine Run
        Button(
            onClick = onRunPythonClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_studio_run_python_live"),
            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🐍 Python 3.14.0 ile Canlı Çalıştır",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        // Real-time Progress Section
        AnimatedVisibility(visible = currentStep != GenerationStep.IDLE) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentStep.stepName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = RedPrimary
                        )
                        Text(
                            text = "%$progressPercent",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = currentStep.description,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progressPercent / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = RedPrimary,
                        trackColor = DarkSurfaceVariant
                    )
                }
            }
        }
    }
}
