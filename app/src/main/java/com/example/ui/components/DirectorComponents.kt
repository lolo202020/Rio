package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.*
import com.example.ui.theme.*

@Composable
fun RioHeader(
    currentTitle: String,
    genre: VideoType,
    aspectRatio: AspectRatioFormat,
    onShareDossier: () -> Unit,
    isHindiMode: Boolean = false,
    onToggleLanguage: () -> Unit = {},
    savedProductions: List<FullFilmProduction> = emptyList(),
    currentProductionId: String = "",
    onSelectProduction: (FullFilmProduction) -> Unit = {},
    onNewProjectRequested: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showProjectMenu by remember { mutableStateOf(false) }

    Surface(
        color = CinemaSurface,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = CinemaSurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Clapperboard Logo Badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(CinemaGold, CinemaAmber))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MovieCreation,
                            contentDescription = "Rio Logo",
                            tint = CinemaBackground,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "RIO",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = CinemaGold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Rec Indicator Dot
                            RecBlinkingDot()
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isHindiMode) "डायरेक्टर AI" else "DIRECTOR AI",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaTextMuted,
                                letterSpacing = 1.2.sp
                            )
                        }
                        Text(
                            text = if (isHindiMode) "यूनिवर्सल AI फिल्म डायरेक्टर मास्टर सिस्टम" else "Universal Film Master Prompt System",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Language Switcher Badge (हिन्दी / ENG)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isHindiMode) CinemaGold else CinemaSurfaceElevated,
                        border = borderStroke(CinemaGold.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clickable(onClick = onToggleLanguage)
                            .testTag("btn_language_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Toggle Language",
                                tint = if (isHindiMode) CinemaBackground else CinemaGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isHindiMode) "हिन्दी" else "ENG",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isHindiMode) CinemaBackground else CinemaGold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Share Dossier Button
                    IconButton(
                        onClick = onShareDossier,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("share_dossier_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Dossier",
                            tint = CinemaGoldLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Active Production Ribbon with Project Switcher
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CinemaSurfaceElevated)
                        .border(
                            width = 1.dp,
                            color = CinemaGold.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { showProjectMenu = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("btn_switch_project"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CinemaTextPrimary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Switch Project",
                            tint = CinemaGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))

                    // Switch Pill Button
                    Surface(
                        color = CinemaGold,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = CinemaBackground,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isHindiMode) "बदलें" else "SWITCH",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CinemaBackground,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = showProjectMenu,
                    onDismissRequest = { showProjectMenu = false },
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .background(CinemaSurface)
                        .border(width = 1.dp, color = CinemaGold.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (isHindiMode) "📂 प्रोजेक्ट चुनें (PROJECT SWITCHER)" else "📂 SWITCH PROJECT",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CinemaGold
                            )
                        },
                        onClick = {},
                        enabled = false
                    )

                    HorizontalDivider(color = CinemaSurfaceBorder, thickness = 1.dp)

                    if (savedProductions.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isHindiMode) "कोई सेव्ड प्रोजेक्ट नहीं है" else "No saved projects yet",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CinemaTextMuted
                                )
                            },
                            onClick = {}
                        )
                    } else {
                        savedProductions.forEach { prod ->
                            val isSelected = prod.id == currentProductionId
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = prod.brief.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) CinemaGold else CinemaTextPrimary,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${prod.brief.genre.label} • ${prod.shotList.size} shots",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CinemaTextSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                        if (isSelected) {
                                            Surface(
                                                color = CinemaGold.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = if (isHindiMode) "एक्टिव" else "ACTIVE",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CinemaGold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    showProjectMenu = false
                                    onSelectProduction(prod)
                                }
                            )
                        }
                    }

                    HorizontalDivider(color = CinemaSurfaceBorder, thickness = 1.dp)

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = AnamorphicCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isHindiMode) "✨ नया फिल्म प्रोजेक्ट बनाएँ (New Film)" else "✨ Create New Film Project",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AnamorphicCyan
                                )
                            }
                        },
                        onClick = {
                            showProjectMenu = false
                            onNewProjectRequested()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RecBlinkingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "rec_blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(CinemaRed.copy(alpha = alpha))
    )
}

/**
 * Visualizes the dramatic emotional tension graph over the timeline.
 */
@Composable
fun EmotionalGraphCanvas(
    beats: List<EmotionalBeat>,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = CinemaGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "EMOTIONAL TENSION GRAPH (Subtext & Conflict)",
                        style = MaterialTheme.typography.labelMedium,
                        color = CinemaGold,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "0% → 100% Tension",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaTextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Line Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CinemaSurfaceElevated)
                    .border(0.5.dp, CinemaSurfaceBorder, RoundedCornerShape(8.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    val w = size.width
                    val h = size.height

                    // Grid lines
                    drawLine(
                        color = CinemaSurfaceBorder,
                        start = Offset(0f, h * 0.25f),
                        end = Offset(w, h * 0.25f),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = CinemaSurfaceBorder,
                        start = Offset(0f, h * 0.5f),
                        end = Offset(w, h * 0.5f),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = CinemaSurfaceBorder,
                        start = Offset(0f, h * 0.75f),
                        end = Offset(w, h * 0.75f),
                        strokeWidth = 1f
                    )

                    if (beats.isNotEmpty()) {
                        val maxTime = beats.maxOfOrNull { it.timecodeSec }?.coerceAtLeast(60)?.toFloat() ?: 60f
                        val points = beats.map { beat ->
                            val x = (beat.timecodeSec / maxTime) * w
                            val y = h - (beat.intensityPercent / 100f * h)
                            Offset(x, y)
                        }

                        // Gradient Fill Path
                        val fillPath = Path().apply {
                            moveTo(points.first().x, h)
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(points.last().x, h)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(CinemaGold.copy(alpha = 0.35f), Color.Transparent)
                            )
                        )

                        // Tension Stroke Line
                        val linePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }
                        drawPath(
                            path = linePath,
                            color = CinemaGoldLight,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Data Points
                        points.forEachIndexed { i, pt ->
                            drawCircle(
                                color = CinemaRed,
                                radius = 4.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Beat Legend Items
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                beats.forEach { beat ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = CinemaSurfaceElevated,
                            shape = RoundedCornerShape(4.dp),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Text(
                                text = "${beat.timecodeSec}s",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${beat.dominantEmotion} (${beat.intensityPercent}%):",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = CinemaTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = beat.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = CinemaTextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive Director Shot Card with full camera, acting, and lighting breakdown.
 */
@Composable
fun DirectorShotCard(
    shot: DirectorShot,
    onCopyPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
            .fillMaxWidth()
            .testTag("shot_card_${shot.shotId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Shot ID, Duration, Framing Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = CinemaGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        border = borderStroke(CinemaGold)
                    ) {
                        Text(
                            text = shot.shotId,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CinemaGoldLight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${shot.startTime} - ${shot.endTime} (${shot.durationSec}s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaTextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Size Badge
                    Surface(
                        color = CinemaSurfaceElevated,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = shot.shotSize.code,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AnamorphicCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    // Movement Badge
                    Surface(
                        color = CinemaSurfaceElevated,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = shot.cameraMovement.label.take(12),
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaTextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action / Blocking Description
            Text(
                text = shot.characterActionAndBlocking,
                style = MaterialTheme.typography.bodyMedium,
                color = CinemaTextPrimary,
                lineHeight = 20.sp
            )

            // Dialogue Line if present
            if (shot.dialogueLine.isNotBlank() && shot.dialogueLine != "[SILENCE]") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CinemaSurfaceElevated)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = CinemaGoldLight,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = shot.dialogueLine,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = CinemaGoldLight
                        )
                        if (shot.voiceDirection.isNotBlank()) {
                            Text(
                                text = "Voice: ${shot.voiceDirection}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CinemaTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Spec Pills (Lens, Angle, Light)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpecPill(label = "Lens", value = shot.lensSuggestion, icon = Icons.Default.CameraAlt)
                SpecPill(label = "Angle", value = shot.cameraAngle.label, icon = Icons.Default.ViewInAr)
            }

            // Expandable details (Micro-expressions, Lighting, Continuity)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailRow(
                        title = "Acting / Micro-Expressions",
                        content = shot.facialExpressionDetail,
                        color = CinemaGold
                    )
                    DetailRow(
                        title = "Body Language & Stance",
                        content = shot.bodyLanguageDetail,
                        color = CinemaTextSecondary
                    )
                    DetailRow(
                        title = "Cinematic Lighting Setup",
                        content = shot.lightingSetup,
                        color = AnamorphicCyan
                    )
                    DetailRow(
                        title = "Sound & Foley",
                        content = shot.soundAndFoley,
                        color = LensIndigo
                    )
                    DetailRow(
                        title = "Continuity / Match on Action",
                        content = shot.continuityNotes,
                        color = CinemaSuccess
                    )

                    // Quick AI Prompt preview inside shot
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CinemaBackground)
                            .border(0.5.dp, CinemaSurfaceBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AI VIDEO GENERATION PROMPT",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaGold,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = { onCopyPrompt(shot.aiVideoPrompt) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Prompt",
                                    tint = CinemaGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", color = CinemaGold, fontSize = 11.sp)
                            }
                        }
                        Text(
                            text = shot.aiVideoPrompt,
                            style = MaterialTheme.typography.bodySmall,
                            color = CinemaTextSecondary,
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expand / Collapse Toggle Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Less Details" else "Full Director Notes",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaGold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = CinemaGold,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SpecPill(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        color = CinemaSurfaceElevated,
        shape = RoundedCornerShape(6.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CinemaTextMuted,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$label: $value",
                style = MaterialTheme.typography.labelSmall,
                color = CinemaTextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun DetailRow(
    title: String,
    content: String,
    color: Color
) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            color = CinemaTextPrimary,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}

/**
 * Dedicated Copy-Paste AI Video Prompt Card with 1-tap clipboard action.
 */
@Composable
fun CopyReadyPromptCard(
    shot: DirectorShot,
    onCopyPrompt: (String) -> Unit,
    onCopyNegative: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Shot ID and Technical Framing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = CinemaGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = shot.shotId,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CinemaGold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${shot.shotSize.code} (${shot.shotSize.label})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AnamorphicCyan
                    )
                }

                Surface(
                    color = CinemaSurfaceElevated,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = shot.cameraMovement.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaTextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // The AI Prompt Block
            Surface(
                color = CinemaBackground,
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = shot.aiVideoPrompt,
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaTextPrimary,
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Wide Landscape Copy Prompt Button (Full Width Horizontal)
            Button(
                onClick = { onCopyPrompt(shot.aiVideoPrompt) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaGold,
                    contentColor = CinemaBackground
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("copy_prompt_${shot.shotId}")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Prompt",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "COPY SHOT PROMPT (LANDSCAPE)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            if (shot.avoidNegativePrompt.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AVOID / NEGATIVE LIST:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaRed,
                        fontSize = 10.sp
                    )
                    TextButton(
                        onClick = { onCopyNegative(shot.avoidNegativePrompt) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Copy Negative", color = CinemaRed, fontSize = 11.sp)
                    }
                }
                Surface(
                    color = CinemaBackground,
                    shape = RoundedCornerShape(6.dp),
                    border = borderStroke(CinemaRed.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = shot.avoidNegativePrompt,
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

/**
 * Helper to copy string to Android clipboard.
 */
fun copyTextToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}

/**
 * Helper to share string via Android Share Sheet.
 */
fun shareText(context: Context, title: String, content: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TITLE, title)
        putExtra(Intent.EXTRA_TEXT, content)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Rio Director Dossier")
    context.startActivity(shareIntent)
}

private fun borderStroke(color: Color) = androidx.compose.foundation.BorderStroke(1.dp, color)
