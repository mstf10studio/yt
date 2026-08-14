package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CaptionStyle
import com.example.model.ShortScript
import com.example.model.VideoScene
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleDark
import com.example.ui.theme.RedPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun PreviewScreen(
    script: ShortScript?,
    captionStyle: CaptionStyle,
    onSpeakNarration: (String) -> Unit,
    onStopSpeaking: () -> Unit,
    onNavigateToStudio: () -> Unit
) {
    if (script == null || script.scenes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.height(64.dp).width(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Henüz oluşturulmuş bir Shorts videosu yok.",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Stüdyo sekmesinden bir konu girerek ilk 9:16 Shorts videonuzu hazırlayın.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
                )
                Button(
                    onClick = onNavigateToStudio,
                    colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
                ) {
                    Text("Stüdyo'ya Git")
                }
            }
        }
        return
    }

    var activeSceneIndex by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var activeWordIndex by remember { mutableStateOf(0) }
    val clipboardManager = LocalClipboardManager.current

    val currentScene = script.scenes.getOrElse(activeSceneIndex) { script.scenes.first() }

    // Auto-advance captions when playing
    LaunchedEffect(isPlaying, activeSceneIndex) {
        if (isPlaying) {
            onSpeakNarration(currentScene.narrationText)
            val subtitles = currentScene.timedSubtitles
            if (subtitles.isNotEmpty()) {
                val delayPerWordMs = (currentScene.durationSeconds * 1000L) / subtitles.size
                for (wIdx in subtitles.indices) {
                    activeWordIndex = wIdx
                    delay(delayPerWordMs)
                }
            } else {
                delay(currentScene.durationSeconds * 1000L)
            }
            // Advance to next scene
            if (activeSceneIndex < script.scenes.size - 1) {
                activeSceneIndex++
            } else {
                isPlaying = false
                activeWordIndex = 0
            }
        } else {
            onStopSpeaking()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = script.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 9:16 Aspect Ratio Vertical Video Frame
        Box(
            modifier = Modifier
                .width(280.dp)
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, RedPrimary.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .shadow(12.dp)
                .testTag("preview_player_frame"),
            contentAlignment = Alignment.Center
        ) {
            // Video Canvas / Background
            val bgColors = listOf(
                listOf(Color(0xFF0F172A), Color(0xFF1E1B4B)),
                listOf(Color(0xFF31101E), Color(0xFF581C87)),
                listOf(Color(0xFF064E3B), Color(0xFF0F172A)),
                listOf(Color(0xFF4C1D95), Color(0xFF881337))
            )
            val currentGradient = bgColors[activeSceneIndex % bgColors.size]

            if (!currentScene.previewThumbnailUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = currentScene.previewThumbnailUrl,
                    contentDescription = currentScene.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(currentGradient))
                )
            }

            // Dark Overlay for Subtitle Readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                        )
                    )
            )

            // Header Tag & Scene Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Sahne ${activeSceneIndex + 1}/${script.scenes.size}",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Central Play/Pause Toggle Overlay
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(RedPrimary.copy(alpha = 0.85f))
                    .clickable { isPlaying = !isPlaying }
                    .padding(14.dp)
                    .testTag("btn_toggle_play")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Oynat/Durdur",
                    tint = Color.White,
                    modifier = Modifier.height(28.dp).width(28.dp)
                )
            }

            // Animated Captions Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(0.95f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val words = currentScene.timedSubtitles
                if (words.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(captionStyle.bgColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .fillMaxWidth()
                    ) {
                        val startIdx = maxOf(0, activeWordIndex - 2)
                        val endIdx = minOf(words.size - 1, activeWordIndex + 2)
                        
                        val displayText = StringBuilder()
                        for (idx in startIdx..endIdx) {
                            if (displayText.isNotEmpty()) displayText.append(" ")
                            displayText.append(words[idx].text)
                        }
                        
                        Text(
                            text = displayText.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = captionStyle.textColor,
                            textAlign = TextAlign.Center,
                            softWrap = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Text(
                        text = currentScene.narrationText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = captionStyle.textColor,
                        textAlign = TextAlign.Center,
                        softWrap = true,
                        modifier = Modifier
                            .background(captionStyle.bgColor, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scene Switcher Chips
        Text(
            text = "Sahneler arasında geçiş yapın:",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(script.scenes) { scene ->
                val isSelected = scene.index - 1 == activeSceneIndex
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) RedPrimary else DarkSurface,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            activeSceneIndex = scene.index - 1
                            isPlaying = false
                            activeWordIndex = 0
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Sahne ${scene.index}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Script Inspector Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
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
                        text = "Aktif Sahne Metni & Arama Terimi",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(script.fullNarrationText))
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala", tint = RedPrimary)
                    }
                }

                Text(
                    text = "🗣️ Seslendirme Metni:",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    text = currentScene.narrationText,
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "📹 Pexels Arama Terimi:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = currentScene.pexelsSearchQuery,
                    fontSize = 13.sp,
                    color = PurpleAccent,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Etiketler: ${script.hashtags.joinToString(" ")}",
                    fontSize = 12.sp,
                    color = NeonYellow
                )
            }
        }
    }
}
