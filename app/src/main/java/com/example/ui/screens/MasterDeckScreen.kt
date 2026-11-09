package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.DirectorEngine
import com.example.models.*
import com.example.ui.DeckTab
import androidx.compose.ui.graphics.Color
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun MasterDeckScreen(
    production: FullFilmProduction,
    activeTab: DeckTab,
    onSelectTab: (DeckTab) -> Unit,
    onShowToast: (String) -> Unit,
    isSpeaking: Boolean = false,
    isStoryboardViewMode: Boolean = true,
    onToggleStoryboardMode: () -> Unit = {},
    onPlayVoice: (text: String, character: String) -> Unit = { _, _ -> },
    onStopVoice: () -> Unit = {},
    isHindiMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSceneId by remember { mutableStateOf<String?>(null) }
    var promptToCalculate by remember { mutableStateOf<String?>(null) }
    var showPdfExportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaBackground)
    ) {
        // Scrollable Deck Tab Bar
        ScrollableTabRow(
            selectedTabIndex = activeTab.tabIndex,
            containerColor = CinemaSurface,
            contentColor = CinemaGold,
            edgePadding = 12.dp,
            divider = { HorizontalDivider(color = CinemaSurfaceBorder, thickness = 1.dp) }
        ) {
            DeckTab.entries.forEach { tab ->
                val isSelected = tab == activeTab
                Tab(
                    selected = isSelected,
                    onClick = { onSelectTab(tab) },
                    text = {
                        Text(
                            text = tab.shortTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) CinemaGold else CinemaTextSecondary
                        )
                    },
                    modifier = Modifier.testTag("deck_tab_${tab.name.lowercase()}")
                )
            }
        }

        // Active Output Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Output Title Banner
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = activeTab.fullTitle.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${production.shotList.size} Shots Planned",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaTextMuted
                    )
                }
            }

            when (activeTab) {
                DeckTab.BRIEF -> {
                    item { ProjectBriefView(production.brief) }
                }

                DeckTab.SCRIPT_AI -> {
                    item {
                        EmotionalGraphCanvas(beats = production.scriptAnalysis.emotionalGraph)
                    }
                    item {
                        ScriptIntelligenceView(production.scriptAnalysis)
                    }
                }

                DeckTab.CHARACTERS -> {
                    items(production.characters) { charProfile ->
                        CharacterBibleCard(
                            character = charProfile,
                            onCopyPrompt = {
                                copyTextToClipboard(context, "Character Prompt", it)
                                onShowToast("Character Seed Prompt Copied!")
                            }
                        )
                    }
                    items(production.environments) { env ->
                        EnvironmentBibleCard(env)
                    }
                }

                DeckTab.SHOT_TABLE -> {
                    // Multi-Scene Timeline & Beat Sequencer
                    item {
                        MultiSceneTimelineBar(
                            shotList = production.shotList,
                            emotionalBeats = production.scriptAnalysis.emotionalGraph,
                            selectedSceneId = selectedSceneId,
                            onSelectScene = { selectedSceneId = it },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    item {
                        Surface(
                            color = CinemaSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isStoryboardViewMode) Icons.Default.ViewCarousel else Icons.Default.TableChart,
                                        contentDescription = null,
                                        tint = CinemaGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isStoryboardViewMode) {
                                            if (isHindiMode) "विजुअल स्टोरीबोर्ड" else "VISUAL STORYBOARDS"
                                        } else {
                                            if (isHindiMode) "शॉट टेबल ग्रिड" else "DIRECTOR SHOT TABLE"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CinemaGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilterChip(
                                        selected = isStoryboardViewMode,
                                        onClick = { if (!isStoryboardViewMode) onToggleStoryboardMode() },
                                        label = { Text(if (isHindiMode) "फ्रेम्स" else "Frames", fontSize = 11.sp) },
                                        leadingIcon = { Icon(Icons.Default.MovieCreation, null, modifier = Modifier.size(12.dp)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CinemaGold,
                                            selectedLabelColor = CinemaBackground
                                        )
                                    )
                                    FilterChip(
                                        selected = !isStoryboardViewMode,
                                        onClick = { if (isStoryboardViewMode) onToggleStoryboardMode() },
                                        label = { Text(if (isHindiMode) "टेबल" else "Table", fontSize = 11.sp) },
                                        leadingIcon = { Icon(Icons.Default.TableRows, null, modifier = Modifier.size(12.dp)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CinemaGold,
                                            selectedLabelColor = CinemaBackground
                                        )
                                    )
                                }
                            }
                        }
                    }

                    val displayedShots = if (selectedSceneId != null) {
                        production.shotList.filter { it.sceneId == selectedSceneId }
                    } else {
                        production.shotList
                    }

                    if (isStoryboardViewMode) {
                        items(displayedShots) { shot ->
                            VisualStoryboardCard(
                                shot = shot,
                                format = production.brief.format,
                                isSpeaking = isSpeaking,
                                onPlayVoice = onPlayVoice,
                                onStopVoice = onStopVoice,
                                onCopyPrompt = {
                                    copyTextToClipboard(context, "AI Video Prompt", it)
                                    onShowToast(if (isHindiMode) "AI वीडियो प्रॉम्प्ट कॉपी हुआ!" else "AI Video Prompt Copied!")
                                }
                            )
                        }
                    } else {
                        items(displayedShots) { shot ->
                            DirectorShotCard(
                                shot = shot,
                                onCopyPrompt = {
                                    copyTextToClipboard(context, "Shot Prompt", it)
                                    onShowToast(if (isHindiMode) "शॉट प्रॉम्प्ट कॉपी हुआ!" else "AI Video Prompt Copied!")
                                }
                            )
                        }
                    }
                }

                DeckTab.ACTING -> {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CinemaSurface),
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "PHASE TWO: HUMAN ACTING SIMULATION",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CinemaGold
                                    )
                                    if (isSpeaking) {
                                        FilledTonalButton(
                                            onClick = onStopVoice,
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = CinemaRed,
                                                contentColor = Color.White
                                            ),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.height(26.dp)
                                        ) {
                                            Icon(Icons.Default.Stop, null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Stop Voice", fontSize = 10.sp)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Actor guidance for micro-expressions, breathing cadence, gaze vectors, and vocal delivery. Tap 'Listen Voice' to hear character delivery.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CinemaTextSecondary
                                )
                            }
                        }
                    }
                    items(production.actingDirectionSheet) { cue ->
                        ActingDirectionCard(
                            cue = cue,
                            isSpeaking = isSpeaking,
                            onPlayVoice = onPlayVoice,
                            onStopVoice = onStopVoice
                        )
                    }
                }

                DeckTab.LIGHTING -> {
                    items(production.lightingCameraPlan) { plan ->
                        LightingPlanCard(plan)
                    }
                }

                DeckTab.AI_PROMPTS -> {
                    item {
                        Surface(
                            color = CinemaSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CopyAll,
                                        contentDescription = null,
                                        tint = CinemaGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (isHindiMode) "रेडी-टू-कॉपी AI प्रॉम्प्ट्स" else "READY-TO-COPY AI PROMPTS",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = CinemaTextPrimary
                                        )
                                        Text(
                                            text = "Tailored for ${production.input.targetAiTool.toolName} with lighting Kelvin, camera verbs, and negative avoidance strings.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CinemaTextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        promptToCalculate = production.shotList.firstOrNull()?.aiVideoPrompt ?: "Cinematic film take"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = CinemaBackground),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_open_param_calc")
                                ) {
                                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isHindiMode) "⚙️ AI टूल पैरामीटर कैलकुलेटर (Kling/Sora/Veo/Runway)" else "⚙️ AI Tool Parameter Calculator (Kling/Sora/Veo/Runway)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    items(production.shotList) { shot ->
                        CopyReadyPromptCard(
                            shot = shot,
                            onCopyPrompt = {
                                copyTextToClipboard(context, "AI Video Prompt", it)
                                onShowToast(if (isHindiMode) "शॉट प्रॉम्प्ट कॉपी हुआ!" else "Copied Shot Prompt to Clipboard!")
                            },
                            onCopyNegative = {
                                copyTextToClipboard(context, "Negative Prompt", it)
                                onShowToast(if (isHindiMode) "नेगेटिव प्रॉम्प्ट कॉपी हुआ!" else "Copied Negative Prompt!")
                            }
                        )
                    }
                }

                DeckTab.EDITING -> {
                    item {
                        EditingSoundView(production.editingSoundPlan)
                    }
                }

                DeckTab.QUALITY_QC -> {
                    item {
                        QualityControlView(production.qualityControl)
                    }
                }

                DeckTab.DOSSIER -> {
                    item {
                        ExportDossierView(
                            production = production,
                            isHindiMode = isHindiMode,
                            onCopyMarkdown = {
                                val md = DirectorEngine.exportToMarkdown(production)
                                copyTextToClipboard(context, "Production Markdown", md)
                                onShowToast(if (isHindiMode) "संपूर्ण मार्कडाउन डोजियर कॉपी हुआ!" else "Complete Markdown Dossier Copied!")
                            },
                            onCopyTsv = {
                                val tsv = DirectorEngine.exportToTsv(production)
                                copyTextToClipboard(context, "Excel Data", tsv)
                                onShowToast(if (isHindiMode) "एक्सेल टेबल TSV कॉपी हुआ!" else "Excel/Sheets Table TSV Copied!")
                            },
                            onShare = {
                                val md = DirectorEngine.exportToMarkdown(production)
                                shareText(context, production.brief.title, md)
                            },
                            onOpenPdfExport = { showPdfExportDialog = true }
                        )
                    }
                }
            }
        }

        if (promptToCalculate != null) {
            AiParameterCalculatorDialog(
                initialPrompt = promptToCalculate ?: "",
                initialTool = production.input.targetAiTool,
                onDismiss = { promptToCalculate = null },
                onCopyCompletePrompt = { finalizedPrompt: String ->
                    copyTextToClipboard(context, "Calculated Prompt", finalizedPrompt)
                    onShowToast(if (isHindiMode) "पैरामीटर सहित प्रॉम्प्ट कॉपी हुआ!" else "Tool prompt copied with flags!")
                    promptToCalculate = null
                }
            )
        }

        if (showPdfExportDialog) {
            PdfExportDialog(
                production = production,
                isHindiMode = isHindiMode,
                onDismiss = { showPdfExportDialog = false },
                onShowToast = onShowToast
            )
        }
    }
}

@Composable
fun ProjectBriefView(brief: ProjectBrief) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = brief.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = CinemaGoldLight
            )

            // Logline Callout
            Surface(
                color = CinemaSurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "LOGLINE:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaGold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = brief.logline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinemaTextPrimary,
                        lineHeight = 20.sp
                    )
                }
            }

            DetailRow(title = "Core Premise", content = brief.corePremise, color = AnamorphicCyan)
            DetailRow(title = "Director Vision Statement", content = brief.directorVisionStatement, color = CinemaGold)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SpecPill(label = "Genre", value = brief.genre.label, icon = Icons.Default.TheaterComedy)
                SpecPill(label = "Format", value = brief.format.label, icon = Icons.Default.AspectRatio)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SpecPill(label = "Language", value = brief.language, icon = Icons.Default.Translate)
                SpecPill(label = "Audience", value = brief.targetAudience, icon = Icons.Default.Groups)
            }
        }
    }
}

@Composable
fun ScriptIntelligenceView(analysis: ScriptAnalysis) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailRow(title = "Core Objective", content = analysis.coreObjective, color = CinemaGold)

            // 3-Act Structure Cards
            Text(
                text = "THREE-ACT NARRATIVE STRUCTURE:",
                style = MaterialTheme.typography.labelSmall,
                color = CinemaGold,
                fontWeight = FontWeight.Bold
            )
            Surface(
                color = CinemaSurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Act I (Setup): ${analysis.actOneSetup}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaTextPrimary
                    )
                    Text(
                        text = "Act II (Confrontation): ${analysis.actTwoConfrontation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaTextPrimary
                    )
                    Text(
                        text = "Act III (Resolution): ${analysis.actThreeResolution}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaTextPrimary
                    )
                }
            }

            DetailRow(title = "Dialogue Subtext & Unspoken Truths", content = analysis.subtextAnalysis, color = AnamorphicCyan)
            DetailRow(title = "Key Turning Point & Payoff", content = analysis.keyTurningPoint, color = CinemaGold)
            DetailRow(title = "Silence, Gaze & Breathing Moments", content = analysis.silenceAndBreathingMoments, color = LensIndigo)

            // Script Doctor Recommendations
            if (analysis.scriptDoctorRecommendations.isNotEmpty()) {
                Text(
                    text = "SCRIPT DOCTOR RECOMMENDATIONS:",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaWarning,
                    fontWeight = FontWeight.Bold
                )
                analysis.scriptDoctorRecommendations.forEach { rec ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text("• ", color = CinemaGold, fontWeight = FontWeight.Bold)
                        Text(
                            text = rec,
                            style = MaterialTheme.typography.bodySmall,
                            color = CinemaTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CharacterBibleCard(
    character: CharacterProfile,
    onCopyPrompt: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = CinemaGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = character.characterId,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CinemaGold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = character.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CinemaTextPrimary
                    )
                }
                Text(
                    text = character.ageEstimate,
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaTextSecondary
                )
            }

            DetailRow(title = "Face, Eyes & Gaze", content = character.facialFeatures, color = CinemaGold)
            DetailRow(title = "Hair & Grooming", content = character.hairAndGrooming, color = CinemaTextSecondary)
            DetailRow(title = "Signature Wardrobe / Costume", content = character.costumeWardrobe, color = AnamorphicCyan)
            DetailRow(title = "Voice Texture & Cadence", content = character.voiceTextureAndCadence, color = LensIndigo)
            DetailRow(title = "Subtext & Internal Fear", content = "${character.internalMotivation} (Fear: ${character.deepFear})", color = CinemaRed)

            // Character Consistency Seed Prompt
            Surface(
                color = CinemaBackground,
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CHARACTER REFERENCE SEED PROMPT",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaGold,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { onCopyPrompt(character.characterReferencePrompt) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Copy Seed", color = CinemaGold, fontSize = 11.sp)
                        }
                    }
                    Text(
                        text = character.characterReferencePrompt,
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaTextSecondary,
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EnvironmentBibleCard(env: EnvironmentProfile) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = AnamorphicCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ENVIRONMENT BIBLE: ${env.locationName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AnamorphicCyan
                )
            }
            Text(
                text = env.description,
                style = MaterialTheme.typography.bodyMedium,
                color = CinemaTextPrimary
            )
            DetailRow(title = "Time of Day & Atmosphere", content = "${env.timeOfDay} — ${env.weatherAndAtmosphere}", color = CinemaGold)
            DetailRow(title = "Lighting Anchor & Practical Sources", content = env.lightingAnchor, color = CinemaGoldLight)
            DetailRow(title = "Key Props & Physical Textures", content = env.keyPropsAndTextures.joinToString(" • "), color = CinemaTextSecondary)
        }
    }
}

@Composable
fun ActingDirectionCard(
    cue: ActingDirectionCue,
    isSpeaking: Boolean = false,
    onPlayVoice: (text: String, character: String) -> Unit = { _, _ -> },
    onStopVoice: () -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cue.characterName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CinemaGoldLight
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = {
                            if (isSpeaking) onStopVoice()
                            else onPlayVoice(cue.lineOrMoment, cue.characterName)
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isSpeaking) CinemaRed else CinemaGold,
                            contentColor = CinemaBackground
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSpeaking) "Stop" else "Voice",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(
                    color = CinemaSurfaceElevated,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = cue.lineOrMoment.take(30),
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaTextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Subtext Callout
            Surface(
                color = CinemaSurfaceElevated,
                shape = RoundedCornerShape(6.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = CinemaGold,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "UNSPOKEN SUBTEXT (WHAT IS FELT INSIDE):",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaGold,
                            fontSize = 10.sp
                        )
                        Text(
                            text = cue.subtextWhatIsFelt,
                            style = MaterialTheme.typography.bodySmall,
                            color = CinemaTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            DetailRow(title = "Eye Gaze & Blink Cadence", content = cue.eyeGazeAndBlinkCadence, color = AnamorphicCyan)
            DetailRow(title = "Facial Muscles & Lips", content = cue.facialMusclesAndMouth, color = CinemaTextPrimary)
            DetailRow(title = "Vocal Texture & Breathing Pauses", content = cue.vocalPacingAndBreath, color = LensIndigo)
            DetailRow(title = "Reaction Timing Beat", content = cue.reactionTimingBeat, color = CinemaWarning)
        }
    }
}

@Composable
fun LightingPlanCard(plan: LightingCameraPlan) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WbIncandescent,
                    contentDescription = null,
                    tint = CinemaGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LIGHTING & COLOR PLAN: ${plan.sceneTitle}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CinemaGold
                )
            }

            DetailRow(title = "Time & Light Sources", content = "${plan.timeOfDay} — ${plan.lightSources}", color = CinemaTextSecondary)
            DetailRow(title = "Key Light Setup", content = plan.keyLightDetail, color = CinemaGold)
            DetailRow(title = "Fill Ratio & Shadows", content = "${plan.fillRatio} — ${plan.shadowContrast}", color = AnamorphicCyan)
            DetailRow(title = "Rim / Backlight & Hair Separation", content = plan.rimBacklight, color = LensIndigo)
            DetailRow(title = "Color Temperature (Kelvin)", content = plan.kelvinColorTemp, color = CinemaGoldLight)
            DetailRow(title = "Skin Tone Preservation", content = plan.skinTonePreservation, color = CinemaSuccess)
            DetailRow(title = "Lighting Continuity Note", content = plan.continuityNotes, color = CinemaWarning)
        }
    }
}

@Composable
fun EditingSoundView(plan: EditingSoundPlan) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailRow(title = "Editing Pacing Philosophy", content = plan.editingPacingPhilosophy, color = CinemaGold)

            Text(
                text = "CUT TRANSITIONS & MOTIVATED CUT POINTS:",
                style = MaterialTheme.typography.labelSmall,
                color = AnamorphicCyan,
                fontWeight = FontWeight.Bold
            )
            plan.cutTransitions.forEach { cut ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("✂ ", color = AnamorphicCyan)
                    Text(cut, style = MaterialTheme.typography.bodySmall, color = CinemaTextPrimary)
                }
            }

            DetailRow(title = "Dialogue ADR & Room Acoustics", content = plan.dialogueSyncGuidelines, color = CinemaTextSecondary)
            DetailRow(title = "Room Tone Ambience", content = plan.roomToneAndAmbience, color = CinemaGoldLight)

            Text(
                text = "FOLEY & SOUND EFFECTS (TACTILE REALISM):",
                style = MaterialTheme.typography.labelSmall,
                color = LensIndigo,
                fontWeight = FontWeight.Bold
            )
            plan.foleySoundEffects.forEach { sfx ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("• ", color = LensIndigo, fontWeight = FontWeight.Bold)
                    Text(sfx, style = MaterialTheme.typography.bodySmall, color = CinemaTextSecondary)
                }
            }

            Text(
                text = "STRATEGIC SILENCE BEATS:",
                style = MaterialTheme.typography.labelSmall,
                color = CinemaRed,
                fontWeight = FontWeight.Bold
            )
            plan.strategicSilenceMoments.forEach { sil ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("🔇 ", color = CinemaRed)
                    Text(sil, style = MaterialTheme.typography.bodySmall, color = CinemaTextPrimary)
                }
            }
        }
    }
}

@Composable
fun QualityControlView(qc: QualityControlReport) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AUTOMATIC QUALITY CONTROL REPORT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CinemaGold
                )
                Surface(
                    color = CinemaSuccess.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "97% PASSED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CinemaSuccess,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // 4-Tier Score Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QcScoreCard(title = "Acting", score = qc.actingScore, modifier = Modifier.weight(1f))
                QcScoreCard(title = "Camera", score = qc.cinematographyScore, modifier = Modifier.weight(1f))
                QcScoreCard(title = "Continuity", score = qc.continuityScore, modifier = Modifier.weight(1f))
                QcScoreCard(title = "Story", score = qc.storyScore, modifier = Modifier.weight(1f))
            }

            // Director Verdict
            Surface(
                color = CinemaSurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "DIRECTOR'S FINAL VERDICT:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaGold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = qc.directorVerdict,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = CinemaTextPrimary
                    )
                }
            }

            // Pre-generation Checklist
            Text(
                text = "PRE-GENERATION VERIFICATION CHECKLIST:",
                style = MaterialTheme.typography.labelSmall,
                color = CinemaGoldLight,
                fontWeight = FontWeight.Bold
            )
            qc.preGenerationChecklist.forEach { item ->
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CinemaSuccess,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(item, style = MaterialTheme.typography.bodySmall, color = CinemaTextSecondary)
                }
            }
        }
    }
}

@Composable
fun QcScoreCard(title: String, score: Int, modifier: Modifier = Modifier) {
    Surface(
        color = CinemaSurfaceElevated,
        shape = RoundedCornerShape(8.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "$score%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CinemaGold)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = CinemaTextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
fun ExportDossierView(
    production: FullFilmProduction,
    isHindiMode: Boolean,
    onCopyMarkdown: () -> Unit,
    onCopyTsv: () -> Unit,
    onShare: () -> Unit,
    onOpenPdfExport: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (isHindiMode) "फाइनल प्रोडक्शन पैकेज एक्सपोर्ट" else "FINAL PRODUCTION PACKAGE EXPORT",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CinemaGold
            )
            Text(
                text = if (isHindiMode)
                    "पूरी 11-फेज की निर्देशक रिपोर्ट को Markdown, Excel/Google Sheets TSV, या प्रिंटेबल PDF स्टोरीबोर्ड बुकलेट के रूप में एक्सपोर्ट करें।"
                else
                    "Export the entire 11-phase director dossier into formatted Markdown, Excel/Sheets TSV, or a professional printable PDF Storyboard Booklet.",
                style = MaterialTheme.typography.bodySmall,
                color = CinemaTextSecondary
            )

            // Primary Action Buttons
            Button(
                onClick = onCopyMarkdown,
                colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = CinemaBackground),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isHindiMode) "संपूर्ण मार्कडाउन डोजियर कॉपी करें" else "Copy Complete Markdown Dossier", fontWeight = FontWeight.Bold)
            }

            // PDF Export Booklet Button
            Button(
                onClick = onOpenPdfExport,
                colors = ButtonDefaults.buttonColors(containerColor = AnamorphicCyan, contentColor = CinemaBackground),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_export_pdf")
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isHindiMode) "📄 PDF स्टोरीबोर्ड बुकलेट एक्सपोर्ट करें" else "📄 Export PDF Storyboard Booklet", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onCopyTsv,
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AnamorphicCyan)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.TableChart, contentDescription = null, tint = AnamorphicCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isHindiMode) "एक्सेल / गूगल शीट्स टेबल डेटा कॉपी करें" else "Copy Excel / Google Sheets Table Data", color = AnamorphicCyan, fontWeight = FontWeight.Bold)
            }

            FilledTonalButton(
                onClick = onShare,
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = CinemaSurfaceElevated, contentColor = CinemaGoldLight),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isHindiMode) "व्हाट्सएप / ड्राइव पर डोजियर शेयर करें" else "Share Dossier to WhatsApp / Drive / Email")
            }
        }
    }
}
