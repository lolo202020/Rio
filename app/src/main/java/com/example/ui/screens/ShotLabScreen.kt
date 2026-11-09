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
import androidx.compose.ui.graphics.Color
import com.example.models.*
import com.example.ui.components.copyTextToClipboard
import com.example.ui.theme.*

@Composable
fun ShotLabScreen(
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedSize by remember { mutableStateOf(ShotSize.CLOSE_UP) }
    var selectedAngle by remember { mutableStateOf(CameraAngle.EYE_LEVEL) }
    var selectedMovement by remember { mutableStateOf(CameraMovement.SLOW_PUSH_IN) }
    var selectedLens by remember { mutableStateOf("85mm Anamorphic Prime f/1.8") }
    var selectedLighting by remember { mutableStateOf("3200K Warm Tungsten Key with 6000K Twilight Rim") }
    var actorAction by remember { mutableStateOf("An Indian actor listening in silence, eyelids fluttering once in vulnerability, jaw clenching slightly before delivering dialogue") }
    var selectedTool by remember { mutableStateOf(AiVideoTool.KLING_2) }
    var format by remember { mutableStateOf(AspectRatioFormat.RATIO_2_39_1) }

    val lensOptions = listOf(
        "24mm Wide Environmental",
        "35mm Documentary Prime",
        "50mm Human Perspective",
        "85mm Anamorphic Prime f/1.8",
        "100mm Macro Prime (Textures)"
    )

    val lightingOptions = listOf(
        "3200K Warm Tungsten Key with 6000K Twilight Rim",
        "Dramatic High-Contrast Rembrandt (4:1 Ratio)",
        "Overhead Cold Fluorescent (Fincher Thriller)",
        "Golden Hour Sunlight with Lens Flare",
        "Neon Noir Dual-Color (Cyan & Amber)"
    )

    val promptResult = remember(selectedSize, selectedAngle, selectedMovement, selectedLens, selectedLighting, actorAction, selectedTool, format) {
        val basePrompt = "${selectedSize.label} at ${selectedAngle.label}. $actorAction. Lens: $selectedLens, beautiful cinematic shallow depth of field. Lighting: $selectedLighting. Camera movement: ${selectedMovement.label}. Aspect Ratio: ${format.ratioStr}, photorealistic skin pores, authentic micro-expressions, 35mm film grain, 8k digital cinema."
        when (selectedTool) {
            AiVideoTool.KLING_2 -> "Kling 2.0 Prompt: $basePrompt Camera brackets: [${selectedMovement.label}]. High fidelity human acting lock."
            AiVideoTool.SORA -> "OpenAI Sora Prompt: Ultra-realistic ${format.ratioStr} video sequence. $basePrompt Natural physical dynamics, photorealistic lighting simulation."
            AiVideoTool.VEO_2 -> "Google Veo 2 Prompt: Arri Alexa LF, ${format.ratioStr}. $basePrompt Master color science, pristine highlight roll-off."
            AiVideoTool.RUNWAY_GEN3 -> "Runway Gen-3 Alpha Prompt: Cinematic 35mm film, $basePrompt [Movement: ${selectedMovement.label}]."
            else -> basePrompt
        }
    }

    val negativeAvoid = "avoid: plastic skin, 3d render look, wax statue, smooth forehead, deformed hands, sudden camera jerk, flat lighting"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Card
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
                            imageVector = Icons.Default.Camera,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SHOT LAB — AI PROMPT CRAFTER",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CinemaGold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Build precision-crafted single shot prompts for Sora, Kling, Veo 2, and Runway by dialing in focal length, lighting Kelvin, and actor micro-movements.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaTextSecondary
                    )
                }
            }
        }

        // Shot Size Selector
        item {
            Column {
                Text(
                    text = "SHOT SIZE / FRAMING:",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ShotSize.entries) { size ->
                        val isSelected = size == selectedSize
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSize = size },
                            label = { Text("${size.code} (${size.label})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CinemaGold,
                                selectedLabelColor = CinemaBackground,
                                containerColor = CinemaSurface,
                                labelColor = CinemaTextSecondary
                            )
                        )
                    }
                }
            }
        }

        // Camera Angle & Movement Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "CAMERA ANGLE & MOVEMENT:",
                    style = MaterialTheme.typography.labelSmall,
                    color = AnamorphicCyan,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CameraAngle.entries) { angle ->
                        val isSelected = angle == selectedAngle
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedAngle = angle },
                            label = { Text(angle.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AnamorphicCyan,
                                selectedLabelColor = CinemaBackground,
                                containerColor = CinemaSurface,
                                labelColor = CinemaTextSecondary
                            )
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CameraMovement.entries) { mov ->
                        val isSelected = mov == selectedMovement
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMovement = mov },
                            label = { Text(mov.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LensIndigo,
                                selectedLabelColor = Color.White,
                                containerColor = CinemaSurface,
                                labelColor = CinemaTextSecondary
                            )
                        )
                    }
                }
            }
        }

        // Lens Selection
        item {
            Column {
                Text(
                    text = "LENS FOCAL LENGTH & OPTICS:",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaGoldLight,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(lensOptions) { lens ->
                        val isSelected = lens == selectedLens
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) CinemaGold.copy(alpha = 0.2f) else CinemaSurface,
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CinemaGold)) else CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.clickable { selectedLens = lens }
                        ) {
                            Text(
                                text = lens,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) CinemaGold else CinemaTextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Lighting Selection
        item {
            Column {
                Text(
                    text = "LIGHTING & COLOR TEMPERATURE:",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(lightingOptions) { light ->
                        val isSelected = light == selectedLighting
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) AnamorphicCyan.copy(alpha = 0.2f) else CinemaSurface,
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AnamorphicCyan)) else CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.clickable { selectedLighting = light }
                        ) {
                            Text(
                                text = light,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) AnamorphicCyan else CinemaTextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Actor Action & Micro-Movement Input
        item {
            Column {
                Text(
                    text = "ACTOR ACTION & MICRO-EXPRESSION:",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = actorAction,
                    onValueChange = { actorAction = it },
                    modifier = Modifier.fillMaxWidth(),
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

        // AI Tool Target
        item {
            Column {
                Text(
                    text = "TARGET GENERATION ENGINE:",
                    style = MaterialTheme.typography.labelSmall,
                    color = AnamorphicCyan,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AiVideoTool.entries) { tool ->
                        val isSelected = tool == selectedTool
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTool = tool },
                            label = { Text(tool.toolName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AnamorphicCyan,
                                selectedLabelColor = CinemaBackground,
                                containerColor = CinemaSurface,
                                labelColor = CinemaTextSecondary
                            )
                        )
                    }
                }
            }
        }

        // Synthesized Prompt Output Box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinemaSurface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CinemaGold)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GENERATED AI SHOT PROMPT",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CinemaGold
                        )
                        Button(
                            onClick = {
                                copyTextToClipboard(context, "Crafted Prompt", promptResult)
                                onShowToast("Prompt Copied to Clipboard!")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CinemaGold,
                                contentColor = CinemaBackground
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("copy_crafted_prompt_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Prompt", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = CinemaBackground,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = promptResult,
                            style = MaterialTheme.typography.bodySmall,
                            color = CinemaTextPrimary,
                            fontFamily = JetBrainsMono,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AVOID LIST (NEGATIVE PROMPT):",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaRed,
                            fontSize = 10.sp
                        )
                        TextButton(
                            onClick = {
                                copyTextToClipboard(context, "Negative Prompt", negativeAvoid)
                                onShowToast("Negative Prompt Copied!")
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Copy Negative", color = CinemaRed, fontSize = 11.sp)
                        }
                    }

                    Surface(
                        color = CinemaBackground,
                        shape = RoundedCornerShape(6.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CinemaRed.copy(alpha = 0.3f))),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = negativeAvoid,
                            style = MaterialTheme.typography.bodySmall,
                            color = CinemaTextMuted,
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
