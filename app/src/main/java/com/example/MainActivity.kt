package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.DirectorEngine
import com.example.ui.*
import com.example.ui.components.RioHeader
import com.example.ui.components.shareText
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.util.configureAppCheck

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure Firebase App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            RioTheme {
                val viewModel: RioDirectorViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }
                val context = LocalContext.current

                // Show toast message in snackbar if present
                LaunchedEffect(uiState.toastMessage) {
                    uiState.toastMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearToast()
                    }
                }

                // Handle system back navigation
                BackHandler(enabled = uiState.currentNavigation != MainNavigation.MASTER_DECK) {
                    viewModel.navigateTo(MainNavigation.MASTER_DECK)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = CinemaBackground,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier
                                .navigationBarsPadding()
                                .padding(bottom = 60.dp)
                        ) { data ->
                            Snackbar(
                                snackbarData = data,
                                containerColor = CinemaGold,
                                contentColor = CinemaBackground,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    },
                    topBar = {
                        RioHeader(
                            currentTitle = uiState.currentProduction.brief.title,
                            genre = uiState.currentProduction.brief.genre,
                            aspectRatio = uiState.currentProduction.brief.format,
                            onShareDossier = {
                                val md = DirectorEngine.exportToMarkdown(uiState.currentProduction)
                                shareText(context, uiState.currentProduction.brief.title, md)
                            },
                            isHindiMode = uiState.isHindiMode,
                            onToggleLanguage = { viewModel.toggleLanguage() },
                            savedProductions = uiState.savedProductions,
                            currentProductionId = uiState.currentProduction.id,
                            onSelectProduction = { viewModel.loadProduction(it) },
                            onNewProjectRequested = { viewModel.navigateTo(MainNavigation.STUDIO) }
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = CinemaSurface,
                            contentColor = CinemaTextPrimary,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            NavigationBarItem(
                                selected = uiState.currentNavigation == MainNavigation.MASTER_DECK,
                                onClick = { viewModel.navigateTo(MainNavigation.MASTER_DECK) },
                                icon = {
                                    Icon(
                                        imageVector = if (uiState.currentNavigation == MainNavigation.MASTER_DECK) Icons.Filled.MovieFilter else Icons.Outlined.MovieFilter,
                                        contentDescription = "Master Deck"
                                    )
                                },
                                label = { Text(AppStrings.navDeck(uiState.isHindiMode), fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CinemaBackground,
                                    selectedTextColor = CinemaGold,
                                    indicatorColor = CinemaGold,
                                    unselectedIconColor = CinemaTextSecondary,
                                    unselectedTextColor = CinemaTextSecondary
                                ),
                                modifier = Modifier.testTag("nav_master_deck")
                            )

                            NavigationBarItem(
                                selected = uiState.currentNavigation == MainNavigation.STUDIO,
                                onClick = { viewModel.navigateTo(MainNavigation.STUDIO) },
                                icon = {
                                    Icon(
                                        imageVector = if (uiState.currentNavigation == MainNavigation.STUDIO) Icons.Filled.Create else Icons.Outlined.Create,
                                        contentDescription = "Studio"
                                    )
                                },
                                label = { Text(AppStrings.navStudio(uiState.isHindiMode), fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CinemaBackground,
                                    selectedTextColor = CinemaGold,
                                    indicatorColor = CinemaGold,
                                    unselectedIconColor = CinemaTextSecondary,
                                    unselectedTextColor = CinemaTextSecondary
                                ),
                                modifier = Modifier.testTag("nav_studio")
                            )

                            NavigationBarItem(
                                selected = uiState.currentNavigation == MainNavigation.CHARACTER_LIBRARY,
                                onClick = { viewModel.navigateTo(MainNavigation.CHARACTER_LIBRARY) },
                                icon = {
                                    Icon(
                                        imageVector = if (uiState.currentNavigation == MainNavigation.CHARACTER_LIBRARY) Icons.Filled.Face else Icons.Outlined.Face,
                                        contentDescription = "Characters"
                                    )
                                },
                                label = { Text(AppStrings.navCharacters(uiState.isHindiMode), fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CinemaBackground,
                                    selectedTextColor = CinemaGoldLight,
                                    indicatorColor = CinemaGoldLight,
                                    unselectedIconColor = CinemaTextSecondary,
                                    unselectedTextColor = CinemaTextSecondary
                                ),
                                modifier = Modifier.testTag("nav_character_library")
                            )

                            NavigationBarItem(
                                selected = uiState.currentNavigation == MainNavigation.SHOT_LAB,
                                onClick = { viewModel.navigateTo(MainNavigation.SHOT_LAB) },
                                icon = {
                                    Icon(
                                        imageVector = if (uiState.currentNavigation == MainNavigation.SHOT_LAB) Icons.Filled.CameraAlt else Icons.Outlined.CameraAlt,
                                        contentDescription = "Shot Lab"
                                    )
                                },
                                label = { Text(AppStrings.navShotLab(uiState.isHindiMode), fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CinemaBackground,
                                    selectedTextColor = AnamorphicCyan,
                                    indicatorColor = AnamorphicCyan,
                                    unselectedIconColor = CinemaTextSecondary,
                                    unselectedTextColor = CinemaTextSecondary
                                ),
                                modifier = Modifier.testTag("nav_shot_lab")
                            )

                            NavigationBarItem(
                                selected = uiState.currentNavigation == MainNavigation.SUBTEXT_COACH,
                                onClick = { viewModel.navigateTo(MainNavigation.SUBTEXT_COACH) },
                                icon = {
                                    Icon(
                                        imageVector = if (uiState.currentNavigation == MainNavigation.SUBTEXT_COACH) Icons.Filled.Psychology else Icons.Outlined.Psychology,
                                        contentDescription = "Acting Coach"
                                    )
                                },
                                label = { Text(AppStrings.navCoach(uiState.isHindiMode), fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CinemaBackground,
                                    selectedTextColor = LensIndigo,
                                    indicatorColor = LensIndigo,
                                    unselectedIconColor = CinemaTextSecondary,
                                    unselectedTextColor = CinemaTextSecondary
                                ),
                                modifier = Modifier.testTag("nav_acting_coach")
                            )

                            NavigationBarItem(
                                selected = uiState.currentNavigation == MainNavigation.VAULT,
                                onClick = { viewModel.navigateTo(MainNavigation.VAULT) },
                                icon = {
                                    Icon(
                                        imageVector = if (uiState.currentNavigation == MainNavigation.VAULT) Icons.Filled.FolderSpecial else Icons.Outlined.FolderSpecial,
                                        contentDescription = "Vault"
                                    )
                                },
                                label = { Text(AppStrings.navVault(uiState.isHindiMode), fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CinemaBackground,
                                    selectedTextColor = CinemaGoldLight,
                                    indicatorColor = CinemaGoldLight,
                                    unselectedIconColor = CinemaTextSecondary,
                                    unselectedTextColor = CinemaTextSecondary
                                ),
                                modifier = Modifier.testTag("nav_vault")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (uiState.currentNavigation) {
                            MainNavigation.MASTER_DECK -> {
                                MasterDeckScreen(
                                    production = uiState.currentProduction,
                                    activeTab = uiState.activeDeckTab,
                                    onSelectTab = { viewModel.selectDeckTab(it) },
                                    onShowToast = { viewModel.showToast(it) },
                                    isSpeaking = uiState.isSpeaking,
                                    isStoryboardViewMode = uiState.isStoryboardViewMode,
                                    onToggleStoryboardMode = { viewModel.toggleStoryboardViewMode() },
                                    onPlayVoice = { text, char -> viewModel.speakDialogue(text, char) },
                                    onStopVoice = { viewModel.stopSpeaking() },
                                    isHindiMode = uiState.isHindiMode
                                )
                            }

                            MainNavigation.STUDIO -> {
                                StudioInputScreen(
                                    onDirectFilm = { viewModel.directFilm(it) },
                                    onAnalyzeScript = { viewModel.analyzeScriptWithGemini(it) },
                                    onLoadCurated = { viewModel.loadProduction(it) },
                                    isAnalyzing = uiState.isGenerating,
                                    savedCharacters = uiState.characterList,
                                    onOpenCharacterLibrary = { viewModel.navigateTo(MainNavigation.CHARACTER_LIBRARY) },
                                    onCreateCharacterRequested = { charName ->
                                        viewModel.navigateTo(MainNavigation.CHARACTER_LIBRARY)
                                        viewModel.showToast("Create character for @$charName in Library")
                                    }
                                )
                            }

                            MainNavigation.CHARACTER_LIBRARY -> {
                                CharacterLibraryScreen(
                                    characters = uiState.characterList,
                                    stats = uiState.characterStats,
                                    supabaseSql = viewModel.supabaseSql,
                                    onSaveCharacter = { name, desc, app, hair, cloth, pers, lang, gen, style, uri ->
                                        viewModel.saveNewCharacter(name, desc, app, hair, cloth, pers, lang, gen, style, uri)
                                    },
                                    onUpdateCharacter = { viewModel.updateCharacter(it) },
                                    onDeleteCharacter = { viewModel.deleteCharacter(it) },
                                    onShowToast = { viewModel.showToast(it) },
                                    onTestVoice = { char ->
                                        val sampleText = when {
                                            char.characterName.contains("Raju", ignoreCase = true) -> "नमस्ते, मैं राजू हूँ। आज की शूटिंग के लिए बिल्कुल तैयार हूँ।"
                                            char.characterName.contains("Mohini", ignoreCase = true) -> "हाय, मैं मोहिनी हूँ। चलिए अगला शॉट शुरू करते हैं।"
                                            char.characterName.contains("Papa", ignoreCase = true) -> "अरे भाई, समय बहुत कीमती है, ध्यान से काम करो।"
                                            char.characterName.contains("Mummy", ignoreCase = true) -> "बेटा, थोड़ा पानी पी लो और मन लगाकर काम करो।"
                                            char.characterName.contains("Friend", ignoreCase = true) || char.characterName.contains("Bunty", ignoreCase = true) -> "भाई तू टेंशन मत ले, सब सेट हो जाएगा!"
                                            else -> "Hello, I am ${char.characterName}. Ready for the camera take."
                                        }
                                        viewModel.speakDialogue(sampleText, char.characterName, char.voiceLanguage)
                                    }
                                )
                            }

                            MainNavigation.SHOT_LAB -> {
                                ShotLabScreen(
                                    onShowToast = { viewModel.showToast(it) }
                                )
                            }

                            MainNavigation.SUBTEXT_COACH -> {
                                SubtextCoachScreen(
                                    onShowToast = { viewModel.showToast(it) }
                                )
                            }

                            MainNavigation.VAULT -> {
                                VaultScreen(
                                    savedProductions = uiState.savedProductions,
                                    currentProductionId = uiState.currentProduction.id,
                                    onSelectProduction = { viewModel.loadProduction(it) },
                                    onDeleteProduction = { viewModel.deleteFromVault(it) },
                                    onNavigateToStudio = { viewModel.navigateTo(MainNavigation.STUDIO) }
                                )
                            }
                        }

                        // Cinematic Director Processing Overlay
                        AnimatedVisibility(
                            visible = uiState.isGenerating,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(CinemaBackground.copy(alpha = 0.92f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CinemaSurface),
                                    shape = RoundedCornerShape(16.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CinemaGold)),
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(
                                            color = CinemaGold,
                                            strokeWidth = 3.dp,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "RIO FILM DIRECTOR ENGINE",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = CinemaGold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = uiState.generationProgressMessage,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = CinemaTextPrimary,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Analyzing acting micro-expressions, 35mm optics & continuity...",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CinemaTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
