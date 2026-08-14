package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.python.PythonCodeGenerator
import com.example.model.AppSettings
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextSecondary

@Composable
fun PythonColabScreen(
    currentTopic: String,
    settings: AppSettings,
    onRunPythonScript: (
        pythonCode: String,
        onLog: (String) -> Unit,
        onProgress: (Float) -> Unit,
        onComplete: () -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var topicInput by remember(currentTopic) {
        mutableStateOf(currentTopic.ifBlank { "Antik Mısır Piramitlerinin Gizemi" })
    }

    var pythonCode by remember(topicInput, settings) {
        mutableStateOf(
            PythonCodeGenerator.generatePythonScript(
                topic = topicInput,
                pexelsApiKey = settings.pexelsApiKey,
                geminiApiKey = settings.geminiApiKey
            )
        )
    }

    var isExecuting by remember { mutableStateOf(false) }
    var executionProgress by remember { mutableStateOf(0f) }
    var copyNotice by remember { mutableStateOf<String?>(null) }

    val terminalLogs = remember { mutableStateListOf<String>() }

    fun triggerExecution() {
        isExecuting = true
        terminalLogs.clear()
        executionProgress = 0.05f
        onRunPythonScript(
            pythonCode,
            { logLine -> terminalLogs.add(logLine) },
            { progress -> executionProgress = progress },
            { isExecuting = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        // SCROLLABLE LOWER CONTENT
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Topic Input Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = topicInput,
                        onValueChange = { topicInput = it },
                        label = { Text("Betiğin Çalışacağı Konu", color = TextSecondary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_python_topic"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StatusGreen,
                            unfocusedBorderColor = DarkSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }
            }

            // Live Interactive Terminal Output Box
            AnimatedVisibility(visible = terminalLogs.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0F19)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🖥️ Python 3.14.0 stdout / Console",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = StatusGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isExecuting) "RUNNING" else "COMPLETED",
                                fontSize = 10.sp,
                                color = if (isExecuting) NeonYellow else StatusGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(DarkBackground, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Column {
                                terminalLogs.forEach { log ->
                                    Text(
                                        text = log,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = when {
                                            log.contains("❌") || log.contains("Exception") -> Color(0xFFFF6B6B)
                                            log.contains("🎉") || log.contains("✅") -> StatusGreen
                                            log.contains("📌") || log.contains("📝") -> PurpleAccent
                                            else -> Color.White
                                        },
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Action Toolbar & Export Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(pythonCode))
                        copyNotice = "Python Betiği (.py) Kopyalandı!"
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_copy_python_py"),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Python (.py)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = {
                        val notebookJson = PythonCodeGenerator.generateColabNotebookJson(
                            topic = topicInput,
                            pexelsApiKey = settings.pexelsApiKey,
                            geminiApiKey = settings.geminiApiKey
                        )
                        clipboardManager.setText(AnnotatedString(notebookJson))
                        copyNotice = "Jupyter Notebook (.ipynb) Kopyalandı!"
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_copy_ipynb_notebook"),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Notebook (.ipynb)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = {
                        pythonCode = PythonCodeGenerator.generatePythonScript(
                            topic = topicInput,
                            pexelsApiKey = settings.pexelsApiKey,
                            geminiApiKey = settings.geminiApiKey
                        )
                    },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("btn_reset_python_code"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Sıfırla", tint = PurpleAccent)
                }
            }

            copyNotice?.let { notice ->
                Text(
                    text = "✓ $notice",
                    fontSize = 12.sp,
                    color = StatusGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            // Python Code Viewer & Editor Area
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "youtube_shorts_generator.py",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = PurpleAccent,
                            fontWeight = FontWeight.Bold
                        )

                        // Code Editor Header Direct RUN Button
                        Button(
                            onClick = { triggerExecution() },
                            enabled = !isExecuting,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("btn_editor_header_run")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.height(16.dp).width(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("▶ Çalıştır", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = pythonCode,
                        onValueChange = { pythonCode = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .testTag("input_python_code_editor"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground,
                            focusedBorderColor = StatusGreen,
                            unfocusedBorderColor = DarkSurfaceVariant,
                            focusedTextColor = Color(0xFFE2E8F0),
                            unfocusedTextColor = Color(0xFFE2E8F0)
                        )
                    )
                }
            }
        }
    }
}
