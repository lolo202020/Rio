package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.DirectorShot
import com.example.models.EmotionalBeat
import com.example.ui.theme.*

data class SceneTimelineBlock(
    val sceneId: String,
    val title: String,
    val startTime: String,
    val endTime: String,
    val durationSec: Int,
    val shotCount: Int,
    val characters: List<String>,
    val dominantEmotion: String
)

@Composable
fun MultiSceneTimelineBar(
    shotList: List<DirectorShot>,
    emotionalBeats: List<EmotionalBeat> = emptyList(),
    selectedSceneId: String? = null,
    onSelectScene: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Group shots into chronological scene blocks
    val sceneBlocks = remember(shotList, emotionalBeats) {
        if (shotList.isEmpty()) emptyList()
        else {
            val grouped = shotList.groupBy { it.sceneId }
            grouped.map { (sceneId, shots) ->
                val start = shots.firstOrNull()?.startTime ?: "00:00"
                val end = shots.lastOrNull()?.endTime ?: "00:15"
                val duration = shots.sumOf { it.durationSec }
                val chars = shots.flatMap { it.charactersInShot }.distinct()
                val emotion = emotionalBeats.firstOrNull { it.description.contains(sceneId, ignoreCase = true) }?.dominantEmotion
                    ?: "Dramatic Tension"

                val sceneTitle = when {
                    sceneId.contains("1", ignoreCase = true) -> "Assi Ghat Confrontation"
                    sceneId.contains("2", ignoreCase = true) -> "Cutting Chai Reconciliation"
                    sceneId.contains("3", ignoreCase = true) -> "Twilight River Resolution"
                    else -> sceneId
                }

                SceneTimelineBlock(
                    sceneId = sceneId,
                    title = sceneTitle,
                    startTime = start,
                    endTime = end,
                    durationSec = duration,
                    shotCount = shots.size,
                    characters = chars,
                    dominantEmotion = emotion
                )
            }
        }
    }

    val totalDurationSec = remember(shotList) { shotList.sumOf { it.durationSec } }

    Card(
        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Bar
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
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MULTI-SCENE FILM TIMELINE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CinemaGold,
                        letterSpacing = 0.8.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Total Runtime: ${totalDurationSec}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaTextSecondary,
                        fontSize = 11.sp
                    )
                    if (selectedSceneId != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• Reset",
                            style = MaterialTheme.typography.labelSmall,
                            color = AnamorphicCyan,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onSelectScene(null) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scene Sequence Cards
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sceneBlocks) { block ->
                    val isSelected = block.sceneId == selectedSceneId
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) CinemaGold.copy(alpha = 0.2f) else CinemaSurfaceElevated,
                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CinemaGold)) else CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .widthIn(min = 140.dp, max = 180.dp)
                            .clickable {
                                if (isSelected) onSelectScene(null)
                                else onSelectScene(block.sceneId)
                            }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = block.sceneId.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CinemaGold else AnamorphicCyan,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${block.durationSec}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CinemaTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = block.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = CinemaTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Characters chips
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                block.characters.take(3).forEach { charName ->
                                    val shortName = charName.replace("@", "").take(5)
                                    Surface(
                                        color = CinemaBackground,
                                        shape = RoundedCornerShape(3.dp)
                                    ) {
                                        Text(
                                            text = "@$shortName",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CinemaGoldLight,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Emotional tone
                            Text(
                                text = "Mood: ${block.dominantEmotion}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LensIndigo,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
