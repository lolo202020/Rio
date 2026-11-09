package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.AspectRatioFormat
import com.example.models.CameraMovement
import com.example.models.DirectorShot
import com.example.models.ShotSize
import com.example.ui.theme.*

/**
 * Visual Storyboard Card rendering an authentic cinematic keyframe simulator
 * with real aspect-ratio letterboxing, Rule-of-Thirds overlay grid,
 * camera motion vectors, lighting ambience gradient, and one-tap TTS dialogue playback.
 */
@Composable
fun VisualStoryboardCard(
    shot: DirectorShot,
    format: AspectRatioFormat = AspectRatioFormat.RATIO_2_39_1,
    isSpeaking: Boolean = false,
    onPlayVoice: (text: String, character: String) -> Unit = { _, _ -> },
    onStopVoice: () -> Unit = {},
    onCopyPrompt: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showGrid by remember { mutableStateOf(true) }

    Card(
        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CinemaSurfaceBorder)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("storyboard_frame_${shot.shotId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Bar: Shot ID, Lens, Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = CinemaGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, CinemaGold)
                    ) {
                        Text(
                            text = shot.shotId,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CinemaGoldLight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${shot.shotSize.label} • ${shot.lensSuggestion}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AnamorphicCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showGrid = !showGrid },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (showGrid) Icons.Default.GridOn else Icons.Default.GridOff,
                            contentDescription = "Toggle Grid",
                            tint = if (showGrid) CinemaGold else CinemaTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Surface(
                        color = CinemaSurfaceElevated,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${shot.durationSec}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaTextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cinematic Storyboard Frame Canvas
            val frameAspectRatio = when (format) {
                AspectRatioFormat.RATIO_2_39_1 -> 2.39f
                AspectRatioFormat.RATIO_16_9 -> 16f / 9f
                AspectRatioFormat.RATIO_9_16 -> 1.4f
                AspectRatioFormat.RATIO_1_1 -> 1.0f
            }

            // Determine atmosphere gradient based on shot lighting
            val gradientColors = remember(shot.lightingSetup) {
                when {
                    shot.lightingSetup.contains("dusk", ignoreCase = true) ||
                    shot.lightingSetup.contains("evening", ignoreCase = true) ||
                    shot.lightingSetup.contains("sunset", ignoreCase = true) -> {
                        listOf(Color(0xFF1A1A3A), Color(0xFF3E1E28), Color(0xFF8F4824), Color(0xFF1F1710))
                    }
                    shot.lightingSetup.contains("neon", ignoreCase = true) ||
                    shot.lightingSetup.contains("night", ignoreCase = true) ||
                    shot.lightingSetup.contains("cyber", ignoreCase = true) -> {
                        listOf(Color(0xFF07141E), Color(0xFF092938), Color(0xFF041926), Color(0xFF030D14))
                    }
                    shot.lightingSetup.contains("warm", ignoreCase = true) ||
                    shot.lightingSetup.contains("lamp", ignoreCase = true) ||
                    shot.lightingSetup.contains("candle", ignoreCase = true) -> {
                        listOf(Color(0xFF291B0C), Color(0xFF4A3114), Color(0xFF1E1308), Color(0xFF0F0A05))
                    }
                    else -> {
                        listOf(Color(0xFF16181E), Color(0xFF242730), Color(0xFF181B22), Color(0xFF0E1014))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(frameAspectRatio)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .border(1.dp, CinemaSurfaceBorder, RoundedCornerShape(8.dp))
            ) {
                // Background Atmospheric Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = gradientColors,
                                radius = 700f
                            )
                        )
                )

                // Canvas for Rule of Thirds and Composition Crosshairs
                if (showGrid) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 1.dp.toPx()
                        val gridColor = Color(0x35E5A93C)
                        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                        // Vertical 1/3 and 2/3 lines
                        drawLine(
                            color = gridColor,
                            start = Offset(size.width / 3f, 0f),
                            end = Offset(size.width / 3f, size.height),
                            strokeWidth = strokeWidth,
                            pathEffect = pathEffect
                        )
                        drawLine(
                            color = gridColor,
                            start = Offset(size.width * 2f / 3f, 0f),
                            end = Offset(size.width * 2f / 3f, size.height),
                            strokeWidth = strokeWidth,
                            pathEffect = pathEffect
                        )

                        // Horizontal 1/3 and 2/3 lines
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, size.height / 3f),
                            end = Offset(size.width, size.height / 3f),
                            strokeWidth = strokeWidth,
                            pathEffect = pathEffect
                        )
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, size.height * 2f / 3f),
                            end = Offset(size.width, size.height * 2f / 3f),
                            strokeWidth = strokeWidth,
                            pathEffect = pathEffect
                        )
                    }
                }

                // Top Left: Camera Movement Badge inside frame
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, AnamorphicCyan.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val movementIcon = when (shot.cameraMovement) {
                                CameraMovement.SLOW_PUSH_IN -> Icons.Default.ZoomIn
                                CameraMovement.PULL_OUT -> Icons.Default.ZoomOut
                                CameraMovement.LATERAL_TRACK -> Icons.Default.DirectionsWalk
                                CameraMovement.HANDHELD_JITTER -> Icons.Default.Videocam
                                CameraMovement.STEADICAM -> Icons.Default.MotionPhotosAuto
                                CameraMovement.WHIP_PAN -> Icons.Default.SwapHoriz
                                CameraMovement.STATIC_TRIPOD -> Icons.Default.Lock
                            }
                            Icon(
                                imageVector = movementIcon,
                                contentDescription = null,
                                tint = AnamorphicCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = shot.cameraMovement.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = AnamorphicCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Center Silhouette / Framing Graphic
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val silhouetteIcon = when (shot.shotSize) {
                            ShotSize.EXTREME_CLOSE_UP -> Icons.Default.Visibility
                            ShotSize.CLOSE_UP, ShotSize.MEDIUM_CLOSE_UP -> Icons.Default.Face
                            ShotSize.MEDIUM -> Icons.Default.Person
                            ShotSize.FULL -> Icons.Default.AccessibilityNew
                            ShotSize.WIDE, ShotSize.EXTREME_WIDE -> Icons.Default.Landscape
                        }
                        Icon(
                            imageVector = silhouetteIcon,
                            contentDescription = null,
                            tint = CinemaGold.copy(alpha = 0.7f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = shot.shotSize.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaGoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Bottom Overlay: Film Grain & Aspect Tag
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "35mm • ${shot.cameraAngle.label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontFamily = JetBrainsMono
                    )
                    Text(
                        text = format.ratioStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action / Blocking Description
            Text(
                text = shot.characterActionAndBlocking,
                style = MaterialTheme.typography.bodySmall,
                color = CinemaTextPrimary,
                lineHeight = 18.sp
            )

            // Dialogue Line & TTS Playback Action
            if (shot.dialogueLine.isNotBlank() && shot.dialogueLine != "[SILENCE]") {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = CinemaSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = CinemaGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "DIALOGUE:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CinemaGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = shot.dialogueLine,
                                style = MaterialTheme.typography.bodySmall,
                                color = CinemaGoldLight,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // TTS Voice Playback Button
                        FilledTonalButton(
                            onClick = {
                                if (isSpeaking) {
                                    onStopVoice()
                                } else {
                                    val charName = shot.dialogueLine.substringBefore(":").trim().replace("@", "")
                                    val dialogueText = shot.dialogueLine.substringAfter(":").trim().ifBlank { shot.dialogueLine }
                                    onPlayVoice(dialogueText, charName)
                                }
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSpeaking) CinemaRed else CinemaGold,
                                contentColor = CinemaBackground
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = if (isSpeaking) "Stop Voice" else "Listen Voice",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSpeaking) "Stop" else "Listen",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Copy AI Prompt Button
            Button(
                onClick = { onCopyPrompt(shot.aiVideoPrompt) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaSurfaceElevated,
                    contentColor = CinemaGoldLight
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Copy AI Video Prompt",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
