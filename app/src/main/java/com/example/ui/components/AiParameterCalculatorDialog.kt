package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.models.AiVideoTool
import com.example.models.AspectRatioFormat
import com.example.ui.theme.*

@Composable
fun AiParameterCalculatorDialog(
    initialPrompt: String,
    initialTool: AiVideoTool = AiVideoTool.KLING_2,
    onDismiss: () -> Unit,
    onCopyCompletePrompt: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedTool by remember { mutableStateOf(initialTool) }
    var selectedRatio by remember { mutableStateOf(AspectRatioFormat.RATIO_16_9) }
    var motionStrength by remember { mutableFloatStateOf(6f) }
    var cameraMotion by remember { mutableStateOf("Slow Push-In") }
    var lightingPreset by remember { mutableStateOf("3200K Warm Dusk") }
    var lockSeed by remember { mutableStateOf(true) }
    var seedValue by remember { mutableLongStateOf(78440031L) }

    val cameraOptions = listOf("Slow Push-In", "Pull-Back Reveal", "Pan Left", "Orbit 45°", "Static Lock", "Steadicam")
    val lightingOptions = listOf("3200K Warm Dusk", "Cinematic Golden Hour", "Neon Cyan & Amber", "High-Contrast Noir")

    // Calculate engine-tailored prompt with explicit flags
    val finalToolPrompt = remember(selectedTool, selectedRatio, motionStrength, cameraMotion, lightingPreset, lockSeed, seedValue, initialPrompt) {
        val base = initialPrompt.trim()
        when (selectedTool) {
            AiVideoTool.RUNWAY_GEN3 -> {
                val camTag = cameraMotion.lowercase().replace(" ", "_")
                "$base --ar ${selectedRatio.ratioStr} --motion ${motionStrength.toInt()} --camera $camTag" +
                        if (lockSeed) " --seed $seedValue" else ""
            }
            AiVideoTool.KLING_2 -> {
                val ratioParam = when (selectedRatio) {
                    AspectRatioFormat.RATIO_9_16 -> "9:16"
                    AspectRatioFormat.RATIO_16_9 -> "16:9"
                    AspectRatioFormat.RATIO_2_39_1 -> "21:9"
                    AspectRatioFormat.RATIO_1_1 -> "1:1"
                }
                "$base [Camera: $cameraMotion, Motion Intensity: ${motionStrength.toInt()}/10, Lighting: $lightingPreset, Aspect: $ratioParam]" +
                        if (lockSeed) " [Seed: $seedValue]" else ""
            }
            AiVideoTool.SORA -> {
                "Photorealistic 35mm cinematic film shot. $base. Realistic natural physics, continuous fluid motion, camera $cameraMotion, atmospheric $lightingPreset lighting, pristine temporal coherence, 4K resolution, aspect ratio ${selectedRatio.ratioStr}."
            }
            AiVideoTool.VEO_2 -> {
                "Cinematic master take shot on Arri Alexa Mini LF, 35mm anamorphic prime lens. $base. Camera motion: $cameraMotion with motion speed ${motionStrength.toInt()}. Color temperature: $lightingPreset. Frame ratio ${selectedRatio.ratioStr}."
            }
            else -> {
                "$base --ar ${selectedRatio.ratioStr} --motion ${motionStrength.toInt()} --camera $cameraMotion"
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CinemaSurface),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(CinemaGold)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI MODEL PARAMETER ENGINE",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CinemaGold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = CinemaTextMuted)
                    }
                }

                // AI Tool Picker
                Column {
                    Text(
                        text = "TARGET VIDEO GENERATOR:",
                        style = MaterialTheme.typography.labelSmall,
                        color = AnamorphicCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(AiVideoTool.entries) { tool ->
                            val isSelected = tool == selectedTool
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTool = tool },
                                label = { Text(tool.toolName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AnamorphicCyan,
                                    selectedLabelColor = CinemaBackground,
                                    containerColor = CinemaSurfaceElevated,
                                    labelColor = CinemaTextSecondary
                                )
                            )
                        }
                    }
                }

                // Aspect Ratio Selector
                Column {
                    Text(
                        text = "ASPECT RATIO:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaGoldLight,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AspectRatioFormat.entries.forEach { ratio ->
                            val isSelected = ratio == selectedRatio
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) CinemaGold else CinemaSurfaceElevated,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedRatio = ratio }
                            ) {
                                Text(
                                    text = ratio.ratioStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CinemaBackground else CinemaTextSecondary,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Motion Strength Slider (1 to 10)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "MOTION INTENSITY / SPEED:",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaTextSecondary
                        )
                        Text(
                            text = "${motionStrength.toInt()} / 10",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CinemaGold
                        )
                    }
                    Slider(
                        value = motionStrength,
                        onValueChange = { motionStrength = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = CinemaGold,
                            activeTrackColor = CinemaGold,
                            inactiveTrackColor = CinemaSurfaceElevated
                        )
                    )
                }

                // Camera Movement Quick Chips
                Column {
                    Text(
                        text = "CAMERA MOVEMENT:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(cameraOptions) { cam ->
                            val isSel = cam == cameraMotion
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSel) LensIndigo else CinemaSurfaceElevated,
                                modifier = Modifier.clickable { cameraMotion = cam }
                            ) {
                                Text(
                                    text = cam,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSel) Color.White else CinemaTextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // Seed Lock Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Seed Continuity Lock", style = MaterialTheme.typography.labelSmall, color = CinemaTextPrimary, fontWeight = FontWeight.Bold)
                        Text("Locks character face & physics", style = MaterialTheme.typography.labelSmall, color = CinemaTextMuted, fontSize = 9.sp)
                    }
                    Switch(
                        checked = lockSeed,
                        onCheckedChange = { lockSeed = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CinemaGold,
                            checkedTrackColor = CinemaGold.copy(alpha = 0.3f)
                        )
                    )
                }

                // Generated Output Prompt Box
                Surface(
                    color = CinemaBackground,
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "CALCULATED ${selectedTool.toolName.uppercase()} COMMAND:",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                            Text(
                                text = "${finalToolPrompt.length} chars",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaTextMuted,
                                fontSize = 9.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = finalToolPrompt,
                            style = MaterialTheme.typography.bodySmall,
                            color = CinemaTextSecondary,
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Copy Action Button
                Button(
                    onClick = {
                        onCopyCompletePrompt(finalToolPrompt)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaGold,
                        contentColor = CinemaBackground
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy Engine-Ready Prompt (${selectedTool.toolName})", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
