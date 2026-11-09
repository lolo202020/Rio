package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.database.CharacterEntity
import com.example.data.repository.CharacterLibraryStats
import com.example.ui.components.copyTextToClipboard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterLibraryScreen(
    characters: List<CharacterEntity>,
    stats: CharacterLibraryStats,
    supabaseSql: String,
    onSaveCharacter: (
        name: String,
        description: String,
        appearance: String,
        hairstyle: String,
        clothing: String,
        personality: String,
        voiceLanguage: String,
        voiceGender: String,
        speakingStyle: String,
        imageUri: Uri?
    ) -> Unit,
    onUpdateCharacter: (CharacterEntity) -> Unit,
    onDeleteCharacter: (String) -> Unit,
    onShowToast: (String) -> Unit,
    onTestVoice: ((CharacterEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var characterToEdit by remember { mutableStateOf<CharacterEntity?>(null) }
    var characterToDelete by remember { mutableStateOf<CharacterEntity?>(null) }
    var showSupabaseSqlDialog by remember { mutableStateOf(false) }

    val filteredCharacters = remember(characters, searchQuery) {
        if (searchQuery.isBlank()) characters
        else characters.filter {
            it.characterName.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.appearanceDescription.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = CinemaBackground,
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = CinemaGold,
                contentColor = CinemaBackground,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("fab_create_character")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "New Character",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Stats Dashboard (Section 13)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CinemaSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CinemaGold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Groups,
                                        contentDescription = null,
                                        tint = CinemaGold,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "PERMANENT CHARACTER LIBRARY",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CinemaGold
                                    )
                                    Text(
                                        text = "Upload once, reuse across all scripts & video scenes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CinemaTextSecondary
                                    )
                                }
                            }

                            // Supabase Architecture Button
                            IconButton(
                                onClick = { showSupabaseSqlDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("btn_supabase_sql")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = "Supabase Schema",
                                    tint = AnamorphicCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Grid (Total Saved, Reference Photos, Scripts Linked, Storage)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatTile(
                                label = "Characters",
                                value = stats.totalCharacters.toString(),
                                icon = Icons.Default.Person,
                                color = CinemaGold,
                                modifier = Modifier.weight(1f)
                            )
                            StatTile(
                                label = "Ref Photos",
                                value = stats.totalReferenceImages.toString(),
                                icon = Icons.Default.PhotoLibrary,
                                color = AnamorphicCyan,
                                modifier = Modifier.weight(1f)
                            )
                            StatTile(
                                label = "Scripts Linked",
                                value = stats.totalScriptsLinked.toString(),
                                icon = Icons.Default.Description,
                                color = LensIndigo,
                                modifier = Modifier.weight(1f)
                            )
                            StatTile(
                                label = "Storage",
                                value = "${(stats.storageUsageBytes / 1024).coerceAtLeast(12)} KB",
                                icon = Icons.Default.CloudDone,
                                color = CinemaSuccess,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Search Bar & Filter Chips
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search characters by name, personality...", color = CinemaTextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = CinemaGold)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = CinemaTextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_characters_input"),
                    shape = RoundedCornerShape(10.dp),
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

            // Character Cards List
            if (filteredCharacters.isEmpty()) {
                item {
                    Surface(
                        color = CinemaSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonSearch,
                                contentDescription = null,
                                tint = CinemaTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Characters Found",
                                style = MaterialTheme.typography.titleMedium,
                                color = CinemaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Create a character with reference photos to use with @mention in scripts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CinemaTextSecondary
                            )
                        }
                    }
                }
            } else {
                items(filteredCharacters) { character ->
                    CharacterProfileCard(
                        character = character,
                        onEdit = { characterToEdit = character },
                        onDelete = { characterToDelete = character },
                        onCopyId = {
                            copyTextToClipboard(context, "Character ID", character.id)
                            onShowToast("Character ID Copied!")
                        },
                        onTestVoice = onTestVoice
                    )
                }
            }
        }
    }

    // Create Character Dialog
    if (showCreateDialog) {
        CharacterFormDialog(
            character = null,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, desc, app, hair, cloth, pers, lang, gen, style, uri ->
                onSaveCharacter(name, desc, app, hair, cloth, pers, lang, gen, style, uri)
                showCreateDialog = false
                onShowToast("Character '$name' saved permanently!")
            }
        )
    }

    // Edit Character Dialog
    characterToEdit?.let { charToEdit ->
        CharacterFormDialog(
            character = charToEdit,
            onDismiss = { characterToEdit = null },
            onConfirm = { name, desc, app, hair, cloth, pers, lang, gen, style, _ ->
                onUpdateCharacter(
                    charToEdit.copy(
                        characterName = name,
                        description = desc,
                        appearanceDescription = app,
                        hairstyle = hair,
                        clothingPreferences = cloth,
                        personality = pers,
                        voiceLanguage = lang,
                        voiceGender = gen,
                        speakingStyle = style
                    )
                )
                characterToEdit = null
                onShowToast("Character '${charToEdit.characterName}' updated!")
            }
        )
    }

    // Delete Confirmation Dialog (Rule 12: Explain impact on old scripts!)
    characterToDelete?.let { charToDelete ->
        AlertDialog(
            onDismissRequest = { characterToDelete = null },
            containerColor = CinemaSurface,
            title = {
                Text(
                    text = "Delete Character?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CinemaRed
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Are you sure you want to permanently delete '${charToDelete.characterName}'?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinemaTextPrimary
                    )
                    Surface(
                        color = CinemaRed.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(CinemaRed.copy(alpha = 0.4f))
                        )
                    ) {
                        Text(
                            text = "NOTICE: Existing scripts containing @${charToDelete.characterName} will preserve past video renders, but future AI scenes will treat this as an unlinked character.",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCharacter(charToDelete.id)
                        characterToDelete = null
                        onShowToast("Deleted '${charToDelete.characterName}'")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaRed, contentColor = Color.White)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { characterToDelete = null }) {
                    Text("Cancel", color = CinemaTextSecondary)
                }
            }
        )
    }

    // Supabase Schema / Architecture Dialog
    if (showSupabaseSqlDialog) {
        AlertDialog(
            onDismissRequest = { showSupabaseSqlDialog = false },
            containerColor = CinemaSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = AnamorphicCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Supabase Cloud SQL & RLS Architecture", color = AnamorphicCyan)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Production PostgreSQL migration schema including RLS policies and Storage bucket configurations (Sections 8 & 9):",
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = CinemaBackground,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(10.dp)) {
                            item {
                                Text(
                                    text = supabaseSql,
                                    fontFamily = JetBrainsMono,
                                    fontSize = 10.sp,
                                    color = CinemaTextPrimary,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        copyTextToClipboard(context, "Supabase SQL", supabaseSql)
                        onShowToast("Supabase SQL copied to clipboard!")
                        showSupabaseSqlDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AnamorphicCyan, contentColor = CinemaBackground)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy SQL Migration")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSupabaseSqlDialog = false }) {
                    Text("Close", color = CinemaTextSecondary)
                }
            }
        )
    }
}

@Composable
fun StatTile(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CinemaSurfaceElevated,
        shape = RoundedCornerShape(8.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = CinemaTextSecondary, fontSize = 9.sp)
        }
    }
}

@Composable
fun CharacterProfileCard(
    character: CharacterEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopyId: () -> Unit,
    onTestVoice: ((CharacterEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val createdStr = remember(character.createdAt) { dateFormat.format(Date(character.createdAt)) }

    Card(
        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
            .fillMaxWidth()
            .testTag("character_card_${character.characterName}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Avatar Photo, Name, Mention Badge, and Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Character Photo / Avatar
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CinemaSurfaceElevated)
                        .border(1.dp, CinemaGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (character.primaryImagePath.startsWith("http") || character.primaryImagePath.startsWith("file") || character.primaryImagePath.startsWith("/")) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(character.primaryImagePath)
                                .crossfade(true)
                                .build(),
                            contentDescription = character.characterName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Initials or placeholder
                        Text(
                            text = character.characterName.take(2).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CinemaGold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Metadata
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = character.characterName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CinemaTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Autocomplete @mention pill
                        Surface(
                            color = CinemaGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "@${character.characterName}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                                color = CinemaGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Created $createdStr • Used in ${character.usageCount} scripts",
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaTextMuted,
                        fontSize = 11.sp
                    )
                }

                // Edit & Delete Icons
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CinemaGold, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = CinemaRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Appearance & Hairstyle
            Text(
                text = character.appearanceDescription,
                style = MaterialTheme.typography.bodySmall,
                color = CinemaTextPrimary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Spec Tags: Hair, Clothes, Voice Profile
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = CinemaSurfaceElevated,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Hair: ${character.hairstyle.take(18)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaTextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        fontSize = 10.sp
                    )
                }
                Surface(
                    color = CinemaSurfaceElevated,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Voice: ${character.voiceLanguage} ${character.voiceGender}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AnamorphicCyan,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        fontSize = 10.sp
                    )
                }
                Surface(
                    color = CinemaSurfaceElevated,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = character.speakingStyle.take(14),
                        style = MaterialTheme.typography.labelSmall,
                        color = LensIndigo,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        fontSize = 10.sp
                    )
                }

                if (onTestVoice != null) {
                    FilledTonalButton(
                        onClick = { onTestVoice(character) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AnamorphicCyan.copy(alpha = 0.2f),
                            contentColor = AnamorphicCyan
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Test Voice", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Character ID Bar (1-tap copy for script mapping)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(CinemaBackground)
                    .clickable(onClick = onCopyId)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "UUID: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaTextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = character.id,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = JetBrainsMono,
                        color = CinemaTextSecondary,
                        fontSize = 10.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy UUID",
                    tint = CinemaGold,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun CharacterFormDialog(
    character: CharacterEntity?,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        description: String,
        appearance: String,
        hairstyle: String,
        clothing: String,
        personality: String,
        voiceLanguage: String,
        voiceGender: String,
        speakingStyle: String,
        imageUri: Uri?
    ) -> Unit
) {
    val isEditing = character != null

    var name by remember { mutableStateOf(character?.characterName ?: "") }
    var description by remember { mutableStateOf(character?.description ?: "") }
    var appearance by remember { mutableStateOf(character?.appearanceDescription ?: "") }
    var hairstyle by remember { mutableStateOf(character?.hairstyle ?: "Short textured dark hair") }
    var clothing by remember { mutableStateOf(character?.clothingPreferences ?: "Casual cotton shirt and denim") }
    var personality by remember { mutableStateOf(character?.personality ?: "Funny, energetic, witty") }
    var voiceLanguage by remember { mutableStateOf(character?.voiceLanguage ?: "Hindi") }
    var voiceGender by remember { mutableStateOf(character?.voiceGender ?: "Male") }
    var speakingStyle by remember { mutableStateOf(character?.speakingStyle ?: "Funny & Natural") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Android Zero-Permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CinemaSurface,
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CinemaGold)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = if (isEditing) "Edit Character: ${character?.characterName}" else "Create Permanent Character",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CinemaGold
                    )
                    Text(
                        text = "Character photo & profile will be saved permanently and reusable via @mention",
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaTextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Photo Upload Box (Zero-permission photo picker)
                item {
                    Column {
                        Text(
                            text = "PRIMARY REFERENCE PHOTO:",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaGold,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CinemaBackground,
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(66.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CinemaSurfaceElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedImageUri != null) {
                                        AsyncImage(
                                            model = selectedImageUri,
                                            contentDescription = "Selected Photo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else if (character?.primaryImagePath != null && character.primaryImagePath.startsWith("http")) {
                                        AsyncImage(
                                            model = character.primaryImagePath,
                                            contentDescription = "Existing Photo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(28.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (selectedImageUri != null) "Photo Selected" else "Tap to Upload Reference Photo",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CinemaTextPrimary
                                    )
                                    Text(
                                        text = "Saved permanently to cloud / disk storage",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CinemaTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Character Name Field
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Character Name (e.g. Raju, Mohini)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_character_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CinemaTextPrimary,
                            unfocusedTextColor = CinemaTextPrimary,
                            focusedBorderColor = CinemaGold,
                            unfocusedBorderColor = CinemaSurfaceBorder
                        )
                    )
                }

                // Appearance & Face
                item {
                    OutlinedTextField(
                        value = appearance,
                        onValueChange = { appearance = it },
                        label = { Text("Face & Appearance (e.g. 24yo, warm brown skin, animated eyes)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CinemaTextPrimary,
                            unfocusedTextColor = CinemaTextPrimary,
                            focusedBorderColor = CinemaGold,
                            unfocusedBorderColor = CinemaSurfaceBorder
                        )
                    )
                }

                // Hairstyle & Clothing
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = hairstyle,
                            onValueChange = { hairstyle = it },
                            label = { Text("Hairstyle", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CinemaTextPrimary,
                                unfocusedTextColor = CinemaTextPrimary,
                                focusedBorderColor = CinemaGold,
                                unfocusedBorderColor = CinemaSurfaceBorder
                            )
                        )
                        OutlinedTextField(
                            value = clothing,
                            onValueChange = { clothing = it },
                            label = { Text("Signature Clothes", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CinemaTextPrimary,
                                unfocusedTextColor = CinemaTextPrimary,
                                focusedBorderColor = CinemaGold,
                                unfocusedBorderColor = CinemaSurfaceBorder
                            )
                        )
                    }
                }

                // Personality & Description
                item {
                    OutlinedTextField(
                        value = personality,
                        onValueChange = { personality = it },
                        label = { Text("Personality Traits (e.g. Sarcastic, emotional, dramatic)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CinemaTextPrimary,
                            unfocusedTextColor = CinemaTextPrimary,
                            focusedBorderColor = CinemaGold,
                            unfocusedBorderColor = CinemaSurfaceBorder
                        )
                    )
                }

                // Voice Profile (Language, Gender, Style)
                item {
                    Text(
                        text = "VOICE PROFILE:",
                        style = MaterialTheme.typography.labelSmall,
                        color = AnamorphicCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = voiceLanguage,
                            onValueChange = { voiceLanguage = it },
                            label = { Text("Language", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CinemaTextPrimary,
                                unfocusedTextColor = CinemaTextPrimary,
                                focusedBorderColor = AnamorphicCyan,
                                unfocusedBorderColor = CinemaSurfaceBorder
                            )
                        )
                        OutlinedTextField(
                            value = voiceGender,
                            onValueChange = { voiceGender = it },
                            label = { Text("Gender", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CinemaTextPrimary,
                                unfocusedTextColor = CinemaTextPrimary,
                                focusedBorderColor = AnamorphicCyan,
                                unfocusedBorderColor = CinemaSurfaceBorder
                            )
                        )
                        OutlinedTextField(
                            value = speakingStyle,
                            onValueChange = { speakingStyle = it },
                            label = { Text("Speaking Style", fontSize = 10.sp) },
                            modifier = Modifier.weight(1.5f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CinemaTextPrimary,
                                unfocusedTextColor = CinemaTextPrimary,
                                focusedBorderColor = AnamorphicCyan,
                                unfocusedBorderColor = CinemaSurfaceBorder
                            )
                        )
                    }
                }

                // Save & Cancel Buttons
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = CinemaTextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    onConfirm(
                                        name,
                                        description.ifBlank { "Character $name" },
                                        appearance.ifBlank { "Natural expressive Indian character" },
                                        hairstyle,
                                        clothing,
                                        personality,
                                        voiceLanguage,
                                        voiceGender,
                                        speakingStyle,
                                        selectedImageUri
                                    )
                                }
                            },
                            enabled = name.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = CinemaBackground),
                            modifier = Modifier.testTag("btn_save_character")
                        ) {
                            Text(if (isEditing) "Save Changes" else "Create Character", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
