package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppSettings
import com.example.ui.components.ApiKeyConfigDialog
import com.example.ui.components.ErrorDiagnosticDialog
import com.example.ui.components.ShortsNavbar
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.PreviewScreen
import com.example.ui.screens.PythonColabScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.ShortsAITheme
import com.example.ui.viewmodel.ShortsViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ShortsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ShortsAITheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()
                val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
                val progressPercent by viewModel.progressPercent.collectAsStateWithLifecycle()
                val currentScript by viewModel.currentScript.collectAsStateWithLifecycle()
                val logs by viewModel.logs.collectAsStateWithLifecycle()
                val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        ShortsNavbar(
                            selectedTab = uiState.selectedTab,
                            onTabSelected = { viewModel.selectTab(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (uiState.selectedTab) {
                            0 -> StudioScreen(
                                topic = uiState.topic,
                                onTopicChange = { viewModel.updateTopic(it) },
                                durationSeconds = uiState.durationSeconds,
                                onDurationChange = { viewModel.updateDuration(it) },
                                voiceTone = uiState.voiceTone,
                                onVoiceToneChange = { viewModel.updateVoiceTone(it) },
                                captionStyle = uiState.captionStyle,
                                onCaptionStyleChange = { viewModel.updateCaptionStyle(it) },
                                language = uiState.language,
                                onLanguageChange = { viewModel.updateLanguage(it) },
                                settings = settingsState,
                                currentStep = currentStep,
                                progressPercent = progressPercent,
                                onOpenApiKeys = { viewModel.toggleApiKeyDialog(true) },
                                onOpenDiagnostics = { viewModel.toggleErrorDialog(true) },
                                onGenerateClick = { viewModel.startGeneration() },
                                onRunPythonClick = { viewModel.selectTab(2) }
                            )

                            1 -> PreviewScreen(
                                script = currentScript,
                                captionStyle = uiState.captionStyle,
                                onSpeakNarration = { viewModel.speakNarration(it) },
                                onStopSpeaking = { viewModel.stopSpeaking() },
                                onNavigateToStudio = { viewModel.selectTab(0) }
                            )

                            2 -> PythonColabScreen(
                                currentTopic = uiState.topic,
                                settings = settingsState,
                                onRunPythonScript = { code, onLog, onProgress, onComplete ->
                                    viewModel.runInAppPythonScript(code, onLog, onProgress, onComplete)
                                }
                            )

                            3 -> GalleryScreen(
                                projects = allProjects,
                                onDeleteProject = { viewModel.deleteProject(it) },
                                onNavigateToStudio = { viewModel.selectTab(0) }
                            )
                        }

                        // API Key Config Dialog
                        if (uiState.isApiKeyDialogOpen) {
                            ApiKeyConfigDialog(
                                currentSettings = settingsState,
                                onDismiss = { viewModel.toggleApiKeyDialog(false) },
                                onSave = { pexelsKey, geminiKey ->
                                    viewModel.saveApiKeys(pexelsKey, geminiKey)
                                }
                            )
                        }

                        // Error & Diagnostic Monitor Dialog
                        if (uiState.isErrorDialogOpen) {
                            ErrorDiagnosticDialog(
                                logs = logs,
                                onDismiss = { viewModel.toggleErrorDialog(false) }
                            )
                        }
                    }
                }
            }
        }
    }
}
