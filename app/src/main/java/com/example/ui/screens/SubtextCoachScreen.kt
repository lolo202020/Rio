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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DetailRow
import com.example.ui.components.copyTextToClipboard
import com.example.ui.theme.*

data class SubtextAnalysisResult(
    val line: String,
    val subtext: String,
    val gazeAndBlink: String,
    val facialMicroTension: String,
    val voiceModulation: String,
    val breathAndPause: String,
    val reactionTiming: String,
    val aiPromptInjection: String
)

@Composable
fun SubtextCoachScreen(
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var dialogueInput by remember {
        mutableStateOf("Main theek hoon... tum jao.")
    }

    val sampleLines = listOf(
        "Main theek hoon... tum jao.",
        "Tumhe aane ki zaroorat nahi thi.",
        "Yeh sab meri galti thi.",
        "Chai thandi ho rahi hai... pee lo.",
        "Mujhe kisi ki madad nahi chahiye.",
        "Sach bolne ka koi faayda nahi ab."
    )

    val currentAnalysis = remember(dialogueInput) {
        val trimmed = dialogueInput.trim()
        when {
            trimmed.contains("theek", ignoreCase = true) -> SubtextAnalysisResult(
                line = trimmed,
                subtext = "I am on the verge of collapsing emotionally, but admitting it to you will destroy the last shred of my pride.",
                gazeAndBlink = "Break eye contact on 'theek'; drop gaze to floor for 3 seconds; blink slowly once; look back up with glassy eyes.",
                facialMicroTension = "Subtle swallow causing Adam's apple to rise; jaw clenches on right side; corners of mouth pulled tight to prevent trembling.",
                voiceModulation = "Low, raspy whisper; pitch dips sharply on 'jao'; breath runs out completely at sentence tail.",
                breathAndPause = "Sharp silent intake through nostrils before speaking; 2-second silence after 'hoon' before finishing the sentence.",
                reactionTiming = "Counterpart must hold silence for 3 full seconds. Do NOT let them speak immediately.",
                aiPromptInjection = "A photorealistic close-up of an Indian actor delivering the line in a restrained whisper, subtle jaw micro-tremor, eyes moist with withheld emotion, looking away then returning gaze, 35mm shallow focus"
            )
            trimmed.contains("chai", ignoreCase = true) -> SubtextAnalysisResult(
                line = trimmed,
                subtext = "I am extending an olive branch of forgiveness, masked as mundane hospitality.",
                gazeAndBlink = "Do not look at the person's face; keep eyes locked on the liquid surface to conceal crying.",
                facialMicroTension = "Softening of forehead brow lines; relaxed lips; fingers exhibit a gentle natural tremor holding the cup.",
                voiceModulation = "Warm, cracked baritone; delivered on an exhale with deep chest resonance.",
                breathAndPause = "1.5-second pause between 'hai' and 'pee lo'; audible breath release.",
                reactionTiming = "Counterpart takes 2 seconds to reach out and accept the cup; knuckles brush.",
                aiPromptInjection = "Close-up of weathered hands offering a hot glass of tea, subtle hand tremor, moist eyes in background bokeh looking downward, warm golden tungsten rim light"
            )
            trimmed.contains("zaroorat", ignoreCase = true) || trimmed.contains("madad", ignoreCase = true) -> SubtextAnalysisResult(
                line = trimmed,
                subtext = "I prayed every night that you would come, but I am terrified of depending on you again.",
                gazeAndBlink = "Stare unblinkingly for 4 seconds; then blink rapidly twice to clear rising tears.",
                facialMicroTension = "Upper lip tightens against teeth; nostrils flare slightly; posture stiffens defensively.",
                voiceModulation = "Sharp, clipped consonants; volume slightly too loud, overcompensating for inner panic.",
                breathAndPause = "Exhale sharply on 'zaroorat'; sudden sharp breath held in chest.",
                reactionTiming = "Counterpart steps forward 1 foot rather than replying with words.",
                aiPromptInjection = "Medium close-up of defensive actor, stiff neck muscles, delivering line with clipped consonants, eyes brimming with conflicted pride, dramatic high-contrast lighting"
            )
            else -> SubtextAnalysisResult(
                line = trimmed,
                subtext = "The surface words mask an unresolved trauma; the character is waiting to see if you will fight for them.",
                gazeAndBlink = "Fix eyes on counterpart's left pupil; blink once every 5 seconds with quiet intensity.",
                facialMicroTension = "Micro-twitch in left cheek muscle; slight brow furrow; controlled mouth envelope.",
                voiceModulation = "Monotone delivery masking internal storm; pitch drops at sentence end.",
                breathAndPause = "2-second pregnant pause before line initiation; deep nasal breathing.",
                reactionTiming = "Hold reaction for 2.5 seconds before any physical response.",
                aiPromptInjection = "Cinematic 85mm portrait close-up of expressive actor delivering line with subtle micro-expressions, authentic eye reflection catchlight, natural lip sync, 35mm film grain"
            )
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinemaSurface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ACTING COACH & DIALOGUE SUBTEXT DOCTOR",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CinemaGold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Paste any line of dialogue in Hindi, English, or any language. Rio reveals the hidden psychological subtext, eye trajectories, vocal texture, and reaction pauses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaTextSecondary
                    )
                }
            }
        }

        // Quick Dialogue Inspiration Chips
        item {
            Column {
                Text(
                    text = "SAMPLE DIALOGUES TO ANALYZE:",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaTextMuted,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sampleLines) { line ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CinemaSurfaceElevated,
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.clickable { dialogueInput = line }
                        ) {
                            Text(
                                text = line,
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaGoldLight,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Dialogue Input Field
        item {
            Column {
                Text(
                    text = "INPUT DIALOGUE LINE:",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = dialogueInput,
                    onValueChange = { dialogueInput = it },
                    placeholder = { Text("Enter dialogue to coach actor...", color = CinemaTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subtext_input_field"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CinemaSurface,
                        unfocusedContainerColor = CinemaSurface,
                        focusedTextColor = CinemaTextPrimary,
                        unfocusedTextColor = CinemaTextPrimary,
                        focusedBorderColor = CinemaGold,
                        unfocusedBorderColor = CinemaSurfaceBorder
                    )
                )
            }
        }

        // Subtext Breakdown Results
        item {
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
                    // Subtext Banner
                    Surface(
                        color = CinemaSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CinemaGold)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "HIDDEN PSYCHOLOGICAL SUBTEXT:",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaGold,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentAnalysis.subtext,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = CinemaTextPrimary,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    DetailRow(title = "Eye Gaze Vector & Blink Timing", content = currentAnalysis.gazeAndBlink, color = AnamorphicCyan)
                    DetailRow(title = "Facial Muscle & Micro-Tension", content = currentAnalysis.facialMicroTension, color = CinemaTextPrimary)
                    DetailRow(title = "Vocal Texture & Delivery Pace", content = currentAnalysis.voiceModulation, color = LensIndigo)
                    DetailRow(title = "Breathing Rhythm & Pauses", content = currentAnalysis.breathAndPause, color = CinemaWarning)
                    DetailRow(title = "Counterpart Reaction Timing", content = currentAnalysis.reactionTiming, color = CinemaRed)

                    // AI Prompt Injection String
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
                                    text = "AI VIDEO PROMPT INJECTION STRING",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CinemaGold,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(
                                    onClick = {
                                        copyTextToClipboard(context, "AI Acting Cue", currentAnalysis.aiPromptInjection)
                                        onShowToast("Acting Prompt Cue Copied!")
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Copy Cue", color = CinemaGold, fontSize = 11.sp)
                                }
                            }
                            Text(
                                text = currentAnalysis.aiPromptInjection,
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
    }
}
