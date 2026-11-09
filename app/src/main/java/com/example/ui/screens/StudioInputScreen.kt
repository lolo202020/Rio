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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.data.database.CharacterEntity
import com.example.domain.CuratedProductions
import com.example.models.*
import com.example.ui.components.ScriptTextInputArea
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioInputScreen(
    onDirectFilm: (ProjectInput) -> Unit,
    onAnalyzeScript: (ProjectInput) -> Unit,
    onLoadCurated: (FullFilmProduction) -> Unit,
    isAnalyzing: Boolean = false,
    savedCharacters: List<CharacterEntity> = emptyList(),
    onOpenCharacterLibrary: () -> Unit = {},
    onCreateCharacterRequested: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var topicText by remember {
        mutableStateOf("A 62-year-old estranged father, Kabir, sits on Varanasi Assi Ghat at dusk. His son Aarav arrives after 4 years of silence. Cold river breeze, unspoken guilt, offering a steaming cutting chai glass.")
    }
    var selectedGenre by remember { mutableStateOf(VideoType.DRAMA) }
    var selectedTool by remember { mutableStateOf(AiVideoTool.KLING_2) }
    var selectedDuration by remember { mutableStateOf("60s (Cinematic Scene)") }
    var selectedFormat by remember { mutableStateOf(AspectRatioFormat.RATIO_2_39_1) }
    var language by remember { mutableStateOf("Hindi") }
    var charactersHint by remember { mutableStateOf("Kabir (62, weathered, shawl), Aarav (24, leather jacket, conflicted)") }
    var locationHint by remember { mutableStateOf("Varanasi Assi Ghat stone steps at twilight, River Ganga") }
    var visualStyle by remember { mutableStateOf("Photorealistic 35mm Arri Alexa, Kodak Vision3 500T warm dusk grain") }

    val durationOptions = listOf("15s (Reel)", "30s (Promo)", "60s (Cinematic Scene)", "2m (Sequence)", "5m (Short Film)")
    val languageOptions = listOf("Hindi", "Bhojpuri", "English", "Hinglish", "Bengali", "Marathi", "Tamil")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Hero Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinemaSurface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "UNIVERSAL AI FILM DIRECTOR STUDIO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CinemaGold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter any story, script, or idea in Hindi, Bhojpuri, English, or any language. Rio directs human acting, 35mm cinematography, subtext, and shot-by-shot AI prompts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Quick Preset Inspiration Chips
        item {
            Column {
                Text(
                    text = "QUICK DIRECTOR PRESETS (1-TAP LOAD):",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaTextMuted,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        PresetChip(
                            title = "Bhojpuri Comedy",
                            subtitle = "Aam Ke Ped Ka Kalesh",
                            onClick = {
                                topicText = "A heated yet hilarious village boundary dispute over a heavy-laden mango tree in Bihar. Two stubborn uncle neighbors with loud theatrical gestures, exaggerated eye rolls, and sharp comebacks."
                                selectedGenre = VideoType.COMEDY
                                language = "Bhojpuri / Hindi"
                                selectedFormat = AspectRatioFormat.RATIO_16_9
                                charactersHint = "Damodar Chacha (55, lungi, wooden stick), Ramsevak (52, gamchha, spectacles)"
                                locationHint = "Bihar village dusty courtyard under sprawling green mango tree in morning sunlight"
                            }
                        )
                    }
                    item {
                        PresetChip(
                            title = "Kolkata Romance",
                            subtitle = "College Street Monsoon",
                            onClick = {
                                topicText = "Inside a century-old College Street coffee house during torrential monsoon rain. Two former university sweethearts meet after 5 years. Condensation on arched window, nervous cup spinning, lingering glances."
                                selectedGenre = VideoType.ROMANCE
                                language = "Bengali / Hindi"
                                selectedFormat = AspectRatioFormat.RATIO_2_39_1
                                charactersHint = "Ananya (27, cotton sari, silver jhumkas), Shounak (28, wet formal shirt, book)"
                                locationHint = "Vintage Kolkata heritage cafe, rain streaked glass, ceiling fan"
                            }
                        )
                    }
                    item {
                        PresetChip(
                            title = "Highway Horror",
                            subtitle = "Midnight Highway 44",
                            onClick = {
                                topicText = "At 2 AM on a desolate desert highway, a lone truck driver inspects a mysterious wooden trunk left in the middle of the asphalt. Headlight beams cut through dust motes, heavy breath, growing dread."
                                selectedGenre = VideoType.HORROR
                                language = "Hindi"
                                selectedFormat = AspectRatioFormat.RATIO_2_39_1
                                charactersHint = "Gurdeep (42, turban, heavy breathing, flashlight)"
                                locationHint = "Dark two-lane asphalt highway, halogen high-beams, eerie desert silence"
                            }
                        )
                    }
                }
            }
        }

        // Script / Screenplay Input Area with Character Counter & Quick Actions
        item {
            ScriptTextInputArea(
                value = topicText,
                onValueChange = { topicText = it },
                label = "FILM SCRIPT / SCREENPLAY INPUT",
                subtitle = "Paste screenplay, scene beats, dialogues, novel excerpt, or idea",
                maxCharacters = 4000,
                minHeight = 150,
                savedCharacters = savedCharacters,
                onOpenCharacterLibrary = onOpenCharacterLibrary,
                onCreateCharacterRequested = onCreateCharacterRequested
            )
        }

        // 'Analyze Script' Button (in wide landscape / horizontal orientation)
        item {
            Button(
                onClick = {
                    val input = ProjectInput(
                        topicOrScript = topicText,
                        videoType = selectedGenre,
                        language = language,
                        targetAudience = "General Cinema & OTT Audience",
                        duration = selectedDuration,
                        format = selectedFormat,
                        visualStyle = visualStyle,
                        charactersHint = charactersHint,
                        locationHint = locationHint,
                        targetAiTool = selectedTool
                    )
                    onAnalyzeScript(input)
                },
                enabled = !isAnalyzing && topicText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("analyze_script_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaGold,
                    contentColor = CinemaBackground,
                    disabledContainerColor = CinemaSurfaceElevated,
                    disabledContentColor = CinemaTextMuted
                )
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        color = CinemaBackground,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ANALYZING SCRIPT WITH GEMINI API...",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ANALYZE SCRIPT (GEMINI API)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Genre Picker
        item {
            Column {
                Text(
                    text = "VIDEO TYPE / GENRE:",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(VideoType.entries) { genre ->
                        val isSelected = genre == selectedGenre
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedGenre = genre },
                            label = { Text(genre.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CinemaGold,
                                selectedLabelColor = CinemaBackground,
                                containerColor = CinemaSurface,
                                labelColor = CinemaTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = CinemaSurfaceBorder,
                                selectedBorderColor = CinemaGold
                            )
                        )
                    }
                }
            }
        }

        // Target AI Tool Selector
        item {
            Column {
                Text(
                    text = "TARGET AI VIDEO TOOL (OPTIMIZES PROMPTS):",
                    style = MaterialTheme.typography.labelSmall,
                    color = AnamorphicCyan,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AiVideoTool.entries) { tool ->
                        val isSelected = tool == selectedTool
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) AnamorphicCyan.copy(alpha = 0.2f) else CinemaSurface,
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AnamorphicCyan)) else CardDefaults.outlinedCardBorder(),
                            modifier = Modifier
                                .clickable { selectedTool = tool }
                                .padding(vertical = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    text = tool.toolName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) AnamorphicCyan else CinemaTextPrimary
                                )
                                Text(
                                    text = tool.bestFor,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CinemaTextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Format / Aspect Ratio
        item {
            Column {
                Text(
                    text = "ASPECT RATIO / FORMAT:",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AspectRatioFormat.entries.forEach { format ->
                        val isSelected = format == selectedFormat
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedFormat = format },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) CinemaGold.copy(alpha = 0.2f) else CinemaSurface,
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CinemaGold)) else CardDefaults.outlinedCardBorder()
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = format.ratioStr,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CinemaGold else CinemaTextPrimary
                                )
                                Text(
                                    text = format.label.substringAfter(" "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CinemaTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Duration & Language Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DURATION:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaTextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(durationOptions) { dur ->
                            val isSelected = dur == selectedDuration
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) CinemaGold else CinemaSurfaceElevated,
                                modifier = Modifier.clickable { selectedDuration = dur }
                            ) {
                                Text(
                                    text = dur.substringBefore(" "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) CinemaBackground else CinemaTextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LANGUAGE:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaTextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(languageOptions) { lang ->
                            val isSelected = lang == language
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) LensIndigo else CinemaSurfaceElevated,
                                modifier = Modifier.clickable { language = lang }
                            ) {
                                Text(
                                    text = lang,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White else CinemaTextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Character & Location Details (Optional Accordion/Fields)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinemaSurface),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CHARACTERS & LOCATION (OPTIONAL DETAILS):",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaGold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = charactersHint,
                        onValueChange = { charactersHint = it },
                        label = { Text("Characters (Names, Age, Costumes)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CinemaTextPrimary,
                            unfocusedTextColor = CinemaTextPrimary,
                            focusedBorderColor = CinemaGold,
                            unfocusedBorderColor = CinemaSurfaceBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = locationHint,
                        onValueChange = { locationHint = it },
                        label = { Text("Location & Atmosphere", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CinemaTextPrimary,
                            unfocusedTextColor = CinemaTextPrimary,
                            focusedBorderColor = CinemaGold,
                            unfocusedBorderColor = CinemaSurfaceBorder
                        )
                    )
                }
            }
        }

        // Master Direct Film Action Button
        item {
            Button(
                onClick = {
                    onDirectFilm(
                        ProjectInput(
                            topicOrScript = topicText,
                            videoType = selectedGenre,
                            language = language,
                            targetAudience = "General Cinema & OTT Audience",
                            duration = selectedDuration,
                            format = selectedFormat,
                            visualStyle = visualStyle,
                            charactersHint = charactersHint,
                            locationHint = locationHint,
                            targetAiTool = selectedTool
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("direct_film_master_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaGold,
                    contentColor = CinemaBackground
                )
            ) {
                Icon(
                    imageVector = Icons.Default.MovieFilter,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "DIRECT MY FILM (EXECUTE 10 PHASES)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PresetChip(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = CinemaSurface,
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CinemaGoldLight
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = CinemaTextSecondary,
                fontSize = 10.sp
            )
        }
    }
}
