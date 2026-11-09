package com.example.ui.components

import android.Manifest
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.database.CharacterEntity
import com.example.ui.theme.*

/**
 * A specialized Jetpack Compose component providing a dedicated screenplay and film script
 * text input area with character limit counter, quick clipboard paste, word/screen-time metrics,
 * screenplay formatting helpers, intelligent @Character autocomplete, and attached characters panel.
 */
@Composable
fun ScriptTextInputArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "FILM SCRIPT / SCREENPLAY INPUT",
    subtitle: String = "Paste screenplay, scene beats, dialogues, novel excerpt, or idea",
    placeholder: String = "Scene 1:\n@Raju enters the house.\n\n@Raju: Mohini, where is my food?\n\n@Mohini: First bring vegetables from the market.\n\n@Raju: Okay, I am going!",
    maxCharacters: Int = 4000,
    minHeight: Int = 160,
    savedCharacters: List<CharacterEntity> = emptyList(),
    onCreateCharacterRequested: ((String) -> Unit)? = null,
    onOpenCharacterLibrary: (() -> Unit)? = null,
    onPasteSuccess: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val currentLength = value.length
    val isNearLimit = currentLength >= (maxCharacters * 0.85)
    val isAtLimit = currentLength >= maxCharacters

    var isListeningSpeech by remember { mutableStateOf(false) }
    var isLightEditorMode by remember { mutableStateOf(false) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListeningSpeech = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenTextList = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenTextList?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                val newText = if (value.isBlank()) {
                    spokenText.take(maxCharacters)
                } else {
                    (value.trimEnd() + " " + spokenText).take(maxCharacters)
                }
                onValueChange(newText)
            }
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Hindi / English dialogue or scene...")
            }
            try {
                isListeningSpeech = true
                speechRecognizerLauncher.launch(intent)
            } catch (_: Exception) {
                isListeningSpeech = false
            }
        }
    }

    // Dynamic color for counter based on usage
    val counterColor by animateColorAsState(
        targetValue = when {
            isAtLimit -> CinemaRed
            isNearLimit -> CinemaWarning
            else -> CinemaTextSecondary
        },
        label = "counter_color"
    )

    // Calculate approximate word count and estimated scene screen time (~2 words/sec / 120 wpm)
    val wordCount = remember(value) {
        if (value.isBlank()) 0 else value.trim().split("\\s+".toRegex()).size
    }
    val estimatedSeconds = remember(wordCount) {
        if (wordCount == 0) 0 else ((wordCount / 2.0).coerceAtLeast(3.0)).toInt()
    }

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }

    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            val safeSelection = TextRange(
                textFieldValue.selection.start.coerceIn(0, value.length),
                textFieldValue.selection.end.coerceIn(0, value.length)
            )
            textFieldValue = textFieldValue.copy(text = value, selection = safeSelection)
        }
    }

    // Active @mention query detection based on cursor position
    val cursor = textFieldValue.selection.start
    val textBeforeCursor = textFieldValue.text.take(cursor)
    val atIndex = textBeforeCursor.lastIndexOf('@')

    val isAtActive = remember(textBeforeCursor, atIndex) {
        if (atIndex != -1) {
            val queryAfterAt = textBeforeCursor.substring(atIndex + 1)
            !queryAfterAt.contains(' ') && !queryAfterAt.contains('\n') &&
            (atIndex == 0 || textBeforeCursor[atIndex - 1].isWhitespace() || textBeforeCursor[atIndex - 1] == '\n')
        } else false
    }

    val atMentionQuery = if (isAtActive && atIndex != -1) {
        textBeforeCursor.substring(atIndex + 1)
    } else null

    val autocompleteSuggestions = remember(atMentionQuery, savedCharacters) {
        if (atMentionQuery != null) {
            if (atMentionQuery.isBlank()) {
                savedCharacters.take(6)
            } else {
                savedCharacters.filter {
                    it.characterName.contains(atMentionQuery, ignoreCase = true) ||
                    it.description.contains(atMentionQuery, ignoreCase = true) ||
                    it.appearanceDescription.contains(atMentionQuery, ignoreCase = true)
                }.take(6)
            }
        } else {
            emptyList()
        }
    }

    // Detect currently attached characters from script text (Section 3 & 4)
    val attachedCharacters = remember(value, savedCharacters) {
        val detected = mutableListOf<CharacterEntity>()
        savedCharacters.forEach { char ->
            if (value.contains("@${char.characterName}", ignoreCase = true) ||
                value.contains("${char.characterName}:", ignoreCase = true)
            ) {
                detected.add(char)
            }
        }
        detected
    }

    // Detect unknown @mention characters (Section 4)
    val unknownMentions = remember(value, savedCharacters) {
        val mentions = mutableSetOf<String>()
        val regex = "@([a-zA-Z0-9_]+)".toRegex()
        regex.findAll(value).forEach { match ->
            val name = match.groupValues[1]
            if (savedCharacters.none { it.characterName.equals(name, ignoreCase = true) }) {
                mentions.add(name)
            }
        }
        mentions.toList()
    }

    val screenplaySnippets = listOf(
        "EXT. " to "EXT. ",
        "INT. " to "INT. ",
        "(beat)" to " (beat) ",
        "[SILENCE]" to "\n[SILENCE - 3 SECONDS]\n",
        "(whispers)" to " (whispers) ",
        "CUT TO:" to "\nCUT TO:\n"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isAtLimit) CinemaRed.copy(alpha = 0.6f) else CinemaSurfaceBorder
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("script_text_input_area_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Label, Icon, Character Library shortcut, and Clipboard Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = CinemaGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaGold,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = AnamorphicCyan.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "@Character Enabled",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AnamorphicCyan,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = CinemaTextMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                // Action buttons: Theme Toggle, Library, Mic, Paste & Clear
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Script Editor Light/Dark Mode Toggle
                    FilledTonalButton(
                        onClick = { isLightEditorMode = !isLightEditorMode },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isLightEditorMode) Color(0xFFE8E3D8) else CinemaSurfaceElevated,
                            contentColor = if (isLightEditorMode) Color(0xFF2C2B29) else CinemaGold
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("btn_toggle_editor_theme")
                    ) {
                        Icon(
                            imageVector = if (isLightEditorMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "Toggle Editor Light/Dark Mode",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isLightEditorMode) "Paper" else "Dark",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    if (onOpenCharacterLibrary != null) {
                        FilledTonalButton(
                            onClick = onOpenCharacterLibrary,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = CinemaSurfaceElevated,
                                contentColor = AnamorphicCyan
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_open_char_lib")
                        ) {
                            Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Library", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Voice Mic Input button (Speech-to-Text)
                    FilledTonalButton(
                        onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak film script or dialogue in Hindi / English...")
                                }
                                try {
                                    isListeningSpeech = true
                                    speechRecognizerLauncher.launch(intent)
                                } catch (_: Exception) {
                                    isListeningSpeech = false
                                }
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isListeningSpeech) CinemaRed else CinemaSurfaceElevated,
                            contentColor = if (isListeningSpeech) Color.White else CinemaGold
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("btn_mic_voice_input")
                    ) {
                        Icon(
                            imageVector = if (isListeningSpeech) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice Dictate",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isListeningSpeech) "Rec..." else "Mic",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Paste from clipboard button
                    FilledTonalButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clipData = clipboard?.primaryClip
                            if (clipData != null && clipData.itemCount > 0) {
                                val pastedText = clipData.getItemAt(0)?.text?.toString() ?: ""
                                if (pastedText.isNotBlank()) {
                                    val newText = if (value.isBlank()) {
                                        pastedText.take(maxCharacters)
                                    } else {
                                        (value + "\n\n" + pastedText).take(maxCharacters)
                                    }
                                    onValueChange(newText)
                                    onPasteSuccess?.invoke(pastedText)
                                }
                            }
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = CinemaSurfaceElevated,
                            contentColor = CinemaGoldLight
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("paste_script_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentPaste,
                            contentDescription = "Paste Script",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Paste",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Clear button
                    if (value.isNotEmpty()) {
                        IconButton(
                            onClick = { onValueChange("") },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("clear_script_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Script",
                                tint = CinemaTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Quick Insertion Chips: Screenplay cues + 1-Tap Saved Character Insertions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Insert:",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaTextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(end = 6.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Trigger @ Character autocomplete at cursor position
                    item {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CinemaGold,
                            modifier = Modifier.clickable {
                                val cur = textFieldValue.selection.start.coerceIn(0, textFieldValue.text.length)
                                val textBefore = textFieldValue.text.substring(0, cur)
                                val textAfter = textFieldValue.text.substring(cur)
                                val prefix = if (textBefore.isNotEmpty() && !textBefore.endsWith(" ") && !textBefore.endsWith("\n")) " @" else "@"
                                val newText = (textBefore + prefix + textAfter).take(maxCharacters)
                                val newCursor = (textBefore + prefix).length.coerceAtMost(newText.length)
                                textFieldValue = TextFieldValue(text = newText, selection = TextRange(newCursor))
                                onValueChange(newText)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AlternateEmail,
                                    contentDescription = null,
                                    tint = CinemaBackground,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "@ Character",
                                    color = CinemaBackground,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Quick @Character Mention Chips
                    items(savedCharacters.take(4)) { char ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CinemaGold.copy(alpha = 0.15f),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(CinemaGold.copy(alpha = 0.5f))
                            ),
                            modifier = Modifier.clickable {
                                val cur = textFieldValue.selection.start.coerceIn(0, textFieldValue.text.length)
                                val textBefore = textFieldValue.text.substring(0, cur)
                                val textAfter = textFieldValue.text.substring(cur)
                                val tag = if (textBefore.isNotEmpty() && !textBefore.endsWith(" ") && !textBefore.endsWith("\n")) {
                                    " @${char.characterName} "
                                } else {
                                    "@${char.characterName} "
                                }
                                val newText = (textBefore + tag + textAfter).take(maxCharacters)
                                val newCursor = (textBefore + tag).length.coerceAtMost(newText.length)
                                textFieldValue = TextFieldValue(text = newText, selection = TextRange(newCursor))
                                onValueChange(newText)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "@${char.characterName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Bold,
                                    color = CinemaGold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Standard Screenplay Slugs
                    items(screenplaySnippets) { (tagLabel, tagContent) ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CinemaSurfaceElevated,
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.clickable {
                                val cur = textFieldValue.selection.start.coerceIn(0, textFieldValue.text.length)
                                val textBefore = textFieldValue.text.substring(0, cur)
                                val textAfter = textFieldValue.text.substring(cur)
                                val newText = (textBefore + tagContent + textAfter).take(maxCharacters)
                                val newCursor = (textBefore + tagContent).length.coerceAtMost(newText.length)
                                textFieldValue = TextFieldValue(text = newText, selection = TextRange(newCursor))
                                onValueChange(newText)
                            }
                        ) {
                            Text(
                                text = tagLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = JetBrainsMono,
                                color = AnamorphicCyan,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // SECTION 3: Intelligent Autocomplete Dropdown (When user types @...)
            AnimatedVisibility(visible = autocompleteSuggestions.isNotEmpty()) {
                Surface(
                    color = CinemaSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CinemaGold)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("autocomplete_dropdown")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AlternateEmail,
                                    contentDescription = null,
                                    tint = CinemaGold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (atMentionQuery.isNullOrBlank()) "SAVED CHARACTERS (TYPE TO SEARCH):" else "MATCHING CHARACTERS FOR '@$atMentionQuery':",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CinemaGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "Links Character ID & Photos",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaTextMuted,
                                fontSize = 9.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        autocompleteSuggestions.forEach { character ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        // Replace typed @query at exact cursor position
                                        if (atIndex != -1 && isAtActive) {
                                            val textBeforeAt = textFieldValue.text.substring(0, atIndex)
                                            val textAfterCursor = textFieldValue.text.substring(cursor)
                                            val replacement = "@${character.characterName} "
                                            val newText = (textBeforeAt + replacement + textAfterCursor).take(maxCharacters)
                                            val newCursorPos = (textBeforeAt + replacement).length.coerceAtMost(newText.length)

                                            textFieldValue = TextFieldValue(text = newText, selection = TextRange(newCursorPos))
                                            onValueChange(newText)
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Character Avatar Photo with fallback
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(CinemaGold.copy(alpha = 0.2f))
                                        .border(1.dp, CinemaGold.copy(alpha = 0.6f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (character.primaryImagePath.startsWith("http") || character.primaryImagePath.startsWith("file") || character.primaryImagePath.startsWith("/")) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(character.primaryImagePath)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = character.characterName,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = character.characterName.take(1),
                                            fontWeight = FontWeight.Bold,
                                            color = CinemaGold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "@${character.characterName}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = CinemaGoldLight,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = CinemaGold.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(3.dp)
                                        ) {
                                            Text(
                                                text = "ID: ${character.id.take(12)}",
                                                color = CinemaGold,
                                                fontSize = 9.sp,
                                                fontFamily = JetBrainsMono,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = character.appearanceDescription.take(50),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CinemaTextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${character.voiceLanguage} ${character.voiceGender} (${character.speakingStyle})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AnamorphicCyan,
                                        fontSize = 9.sp
                                    )
                                }
                                Surface(
                                    color = CinemaBackground,
                                    shape = RoundedCornerShape(4.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(AnamorphicCyan.copy(alpha = 0.5f))
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Link,
                                            contentDescription = null,
                                            tint = AnamorphicCyan,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Link",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AnamorphicCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Dynamic Script Editor Theme Colors (Paper Light Mode vs Cinema Dark Mode)
            val editorContainerColor = if (isLightEditorMode) Color(0xFFF7F5F0) else CinemaBackground
            val editorTextColor = if (isLightEditorMode) Color(0xFF1F1E1B) else CinemaTextPrimary
            val editorPlaceholderColor = if (isLightEditorMode) Color(0xFF88847C) else CinemaTextMuted
            val editorFocusedBorderColor = when {
                isAtLimit -> CinemaRed
                isLightEditorMode -> Color(0xFF8C7A52)
                else -> CinemaGold
            }
            val editorUnfocusedBorderColor = when {
                isAtLimit -> CinemaRed.copy(alpha = 0.5f)
                isLightEditorMode -> Color(0xFFD6CFC0)
                else -> CinemaSurfaceBorder
            }

            // Text Input Field (Screenplay Monospace Styling with Cursor Tracking & Theme Mode)
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { newTfv ->
                    if (newTfv.text.length <= maxCharacters) {
                        textFieldValue = newTfv
                        onValueChange(newTfv.text)
                    } else {
                        val truncated = newTfv.text.take(maxCharacters)
                        textFieldValue = newTfv.copy(
                            text = truncated,
                            selection = TextRange(newTfv.selection.start.coerceAtMost(maxCharacters))
                        )
                        onValueChange(truncated)
                    }
                },
                placeholder = {
                    Text(
                        text = placeholder,
                        color = editorPlaceholderColor,
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                },
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = editorTextColor
                ),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = editorContainerColor,
                    unfocusedContainerColor = editorContainerColor,
                    focusedBorderColor = editorFocusedBorderColor,
                    unfocusedBorderColor = editorUnfocusedBorderColor,
                    cursorColor = if (isLightEditorMode) Color(0xFFB8860B) else CinemaGold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight.dp, max = 280.dp)
                    .testTag("script_input_field")
            )

            // SECTION 4: Attached Characters Panel & Unknown Warning
            if (attachedCharacters.isNotEmpty() || unknownMentions.isNotEmpty()) {
                Surface(
                    color = CinemaSurfaceElevated,
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = CinemaSuccess,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ATTACHED CHARACTERS IN SCRIPT (${attachedCharacters.size}):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CinemaSuccess,
                                    fontSize = 10.sp
                                )
                            }
                            Text(
                                text = "Ref Photos & Voices Auto-Linked",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaTextMuted,
                                fontSize = 9.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Attached Characters Chips with Photos
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(attachedCharacters) { char ->
                                Surface(
                                    color = CinemaBackground,
                                    shape = RoundedCornerShape(6.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(CinemaGold.copy(alpha = 0.5f))
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(CinemaGold.copy(alpha = 0.3f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = char.characterName.take(1),
                                                fontWeight = FontWeight.Bold,
                                                color = CinemaGold,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = "@${char.characterName}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = CinemaGoldLight,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = "${char.voiceLanguage} ${char.voiceGender}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CinemaTextMuted,
                                                fontSize = 8.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Unknown Character Warning & Create Action (Section 4)
                        if (unknownMentions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            unknownMentions.forEach { unknownName ->
                                Surface(
                                    color = CinemaRed.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(CinemaRed.copy(alpha = 0.4f))
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = CinemaRed,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Character '@$unknownName' Not Found in Library",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CinemaRed,
                                                fontSize = 11.sp
                                            )
                                        }
                                        if (onCreateCharacterRequested != null) {
                                            Button(
                                                onClick = { onCreateCharacterRequested(unknownName) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = CinemaGold,
                                                    contentColor = CinemaBackground
                                                ),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("+ Create", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Bar: Word Count / Screen Time Metrics on left, Character Counter on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Word Count & Estimated Scene Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = CinemaSurfaceElevated,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "$wordCount words",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaTextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (wordCount > 0) {
                        Surface(
                            color = CinemaSurfaceElevated,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Est. ~$estimatedSeconds s scene",
                                style = MaterialTheme.typography.labelSmall,
                                color = LensIndigo,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Character Limit Counter with Dynamic Color
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("character_counter")
                ) {
                    if (isAtLimit) {
                        Text(
                            text = "MAX LIMIT REACHED  ",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }

                    Text(
                        text = "$currentLength",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = counterColor,
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp
                    )
                    Text(
                        text = " / $maxCharacters chars",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaTextMuted,
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp
                    )
                }
            }

            // Linear Progress Indicator for Character Limit Usage
            val progress = (currentLength.toFloat() / maxCharacters.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = counterColor,
                trackColor = CinemaSurfaceElevated
            )

            // Warning note if user reached max limit
            AnimatedVisibility(visible = isAtLimit) {
                Surface(
                    color = CinemaRed.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CinemaRed.copy(alpha = 0.4f))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = CinemaRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Character limit reached ($maxCharacters characters). Additional text will be truncated.",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaRed,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
