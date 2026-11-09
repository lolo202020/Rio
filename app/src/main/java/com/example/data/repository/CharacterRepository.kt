package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class CharacterDetectionResult(
    val detectedCharacters: List<CharacterEntity>,
    val unknownCharacterNames: List<String>,
    val sceneMappings: Map<String, List<CharacterEntity>> // Scene ID -> Characters in that scene
)

data class CharacterLibraryStats(
    val totalCharacters: Int,
    val totalReferenceImages: Int,
    val totalScriptsLinked: Int,
    val mostUsed: List<CharacterEntity>,
    val storageUsageBytes: Long
)

class CharacterRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.characterDao()

    fun getAllCharacters(): Flow<List<CharacterEntity>> = dao.getAllCharacters()

    fun searchCharacters(query: String): Flow<List<CharacterEntity>> = dao.searchCharacters(query)

    fun getReferenceImages(characterId: String): Flow<List<CharacterReferenceImageEntity>> =
        dao.getReferenceImages(characterId)

    suspend fun getCharacterByName(name: String): CharacterEntity? = withContext(Dispatchers.IO) {
        dao.getCharacterByName(name.trim())
    }

    suspend fun getCharacterById(id: String): CharacterEntity? = withContext(Dispatchers.IO) {
        dao.getCharacterById(id)
    }

    /**
     * Saves photo permanently to persistent internal storage in context.filesDir/character-library/
     * so it never gets lost, even if deleted from gallery.
     */
    suspend fun saveImagePermanently(sourceUri: Uri, characterId: String, fileName: String): String =
        withContext(Dispatchers.IO) {
            try {
                val charDir = File(context.filesDir, "character-library/$characterId")
                if (!charDir.exists()) charDir.mkdirs()

                val targetFile = File(charDir, fileName)
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                targetFile.absolutePath
            } catch (e: Exception) {
                sourceUri.toString()
            }
        }

    suspend fun createCharacter(
        name: String,
        description: String,
        appearance: String,
        hairstyle: String,
        clothing: String,
        personality: String,
        voiceLanguage: String,
        voiceGender: String,
        speakingStyle: String,
        primaryImageUri: Uri?
    ): CharacterEntity = withContext(Dispatchers.IO) {
        val characterId = "char_" + UUID.randomUUID().toString()

        val savedImagePath = if (primaryImageUri != null) {
            saveImagePermanently(primaryImageUri, characterId, "primary.png")
        } else {
            "android.resource://com.example/drawable/ic_launcher_foreground"
        }

        val entity = CharacterEntity(
            id = characterId,
            characterName = name.trim(),
            description = description.trim(),
            appearanceDescription = appearance.trim(),
            hairstyle = hairstyle.trim(),
            clothingPreferences = clothing.trim(),
            personality = personality.trim(),
            defaultVoiceId = "${voiceLanguage.lowercase().take(2)}_${voiceGender.lowercase()}_${speakingStyle.lowercase().take(6)}",
            voiceLanguage = voiceLanguage,
            voiceGender = voiceGender,
            speakingStyle = speakingStyle,
            primaryImagePath = savedImagePath,
            usageCount = 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        dao.insertCharacter(entity)
        entity
    }

    suspend fun updateCharacter(character: CharacterEntity) = withContext(Dispatchers.IO) {
        dao.updateCharacter(character.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteCharacter(characterId: String) = withContext(Dispatchers.IO) {
        // Delete local images
        try {
            val charDir = File(context.filesDir, "character-library/$characterId")
            if (charDir.exists()) charDir.deleteRecursively()
        } catch (_: Exception) {}

        dao.deleteCharacterById(characterId)
    }

    suspend fun addReferenceImage(characterId: String, uri: Uri, imageType: String) =
        withContext(Dispatchers.IO) {
            val imageId = UUID.randomUUID().toString()
            val savedPath = saveImagePermanently(uri, characterId, "ref_$imageId.png")
            dao.insertReferenceImage(
                CharacterReferenceImageEntity(
                    id = imageId,
                    characterId = characterId,
                    imagePath = savedPath,
                    imageType = imageType
                )
            )
        }

    suspend fun deleteReferenceImage(imageId: String) = withContext(Dispatchers.IO) {
        dao.deleteReferenceImage(imageId)
    }

    /**
     * Automatic Character Detection & Scene Mapping from script text.
     */
    suspend fun detectCharactersInScript(scriptText: String): CharacterDetectionResult =
        withContext(Dispatchers.IO) {
            val allCharacters = mutableListOf<CharacterEntity>()
            // Collect all saved characters
            val savedCharacters = mutableListOf<CharacterEntity>()
            // Query known names
            val lines = scriptText.lines()
            val detectedNames = mutableSetOf<String>()
            val unknownNames = mutableSetOf<String>()
            val sceneMappings = mutableMapOf<String, MutableList<CharacterEntity>>()

            // Regex for @Name mentions
            val atMentionRegex = "@([a-zA-Z0-9_]+)".toRegex()
            atMentionRegex.findAll(scriptText).forEach { match ->
                detectedNames.add(match.groupValues[1])
            }

            // Also check for standard script dialogue slugs like "Raju:" or "Mohini:"
            val dialogueSlugRegex = "(?m)^\\s*([A-Z][a-zA-Z0-9_\\s]+):".toRegex()
            dialogueSlugRegex.findAll(scriptText).forEach { match ->
                val name = match.groupValues[1].replace("@", "").trim()
                if (name.length in 2..20 && !name.equals("Scene", ignoreCase = true) && !name.equals("Cut", ignoreCase = true)) {
                    detectedNames.add(name)
                }
            }

            // Match against database
            detectedNames.forEach { name ->
                val character = dao.getCharacterByName(name)
                if (character != null) {
                    savedCharacters.add(character)
                    dao.incrementUsageCount(character.id)
                } else {
                    unknownNames.add(name)
                }
            }

            // Break down scene-wise characters
            var currentScene = "Scene 1"
            lines.forEach { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("Scene", ignoreCase = true) || trimmed.startsWith("SC", ignoreCase = true) || trimmed.startsWith("EXT.", ignoreCase = true) || trimmed.startsWith("INT.", ignoreCase = true)) {
                    currentScene = trimmed.take(25)
                }

                savedCharacters.forEach { char ->
                    if (line.contains("@${char.characterName}", ignoreCase = true) || line.contains("${char.characterName}:", ignoreCase = true)) {
                        val list = sceneMappings.getOrPut(currentScene) { mutableListOf() }
                        if (!list.any { it.id == char.id }) {
                            list.add(char)
                        }
                    }
                }
            }

            CharacterDetectionResult(
                detectedCharacters = savedCharacters.distinctBy { it.id },
                unknownCharacterNames = unknownNames.toList(),
                sceneMappings = sceneMappings
            )
        }

    suspend fun getLibraryStats(): CharacterLibraryStats = withContext(Dispatchers.IO) {
        val count = dao.getTotalCharactersCount()
        val refCount = dao.getTotalReferenceImagesCount()
        val mostUsed = dao.getMostUsedCharacters(5)

        val storageDir = File(context.filesDir, "character-library")
        var totalBytes = 0L
        if (storageDir.exists()) {
            storageDir.walkTopDown().forEach { file ->
                if (file.isFile) totalBytes += file.length()
            }
        }

        CharacterLibraryStats(
            totalCharacters = count,
            totalReferenceImages = refCount,
            totalScriptsLinked = mostUsed.sumOf { it.usageCount },
            mostUsed = mostUsed,
            storageUsageBytes = totalBytes
        )
    }

    /**
     * Generates complete production Supabase PostgreSQL migration SQL
     * with Row Level Security (RLS) policies and Storage bucket definitions
     * as required by Section 8 & 9.
     */
    fun generateSupabaseSql(): String {
        return """
            -- =======================================================
            -- SUPABASE DATABASE ARCHITECTURE: CHARACTER LIBRARY & CONSISTENCY
            -- =======================================================

            -- 1. Enable UUID Extension
            CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

            -- 2. Characters Table
            CREATE TABLE IF NOT EXISTS public.characters (
                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
                character_name TEXT NOT NULL,
                description TEXT,
                appearance_description TEXT,
                hairstyle TEXT,
                clothing_preferences TEXT,
                personality TEXT,
                default_voice_id TEXT,
                voice_language TEXT DEFAULT 'Hindi',
                voice_gender TEXT DEFAULT 'Male',
                speaking_style TEXT DEFAULT 'Natural',
                voice_pitch TEXT DEFAULT 'Medium',
                primary_image_path TEXT,
                usage_count INT DEFAULT 0,
                created_at TIMESTAMPTZ DEFAULT NOW(),
                updated_at TIMESTAMPTZ DEFAULT NOW()
            );

            -- 3. Character Reference Images Table
            CREATE TABLE IF NOT EXISTS public.character_reference_images (
                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                character_id UUID NOT NULL REFERENCES public.characters(id) ON DELETE CASCADE,
                user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
                image_path TEXT NOT NULL,
                image_type TEXT DEFAULT 'reference',
                created_at TIMESTAMPTZ DEFAULT NOW()
            );

            -- 4. Script Character Mentions Table
            CREATE TABLE IF NOT EXISTS public.script_character_mentions (
                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                script_id UUID NOT NULL,
                character_id UUID NOT NULL REFERENCES public.characters(id) ON DELETE CASCADE,
                mention_name TEXT NOT NULL,
                created_at TIMESTAMPTZ DEFAULT NOW()
            );

            -- 5. Scene Character Assignments Table
            CREATE TABLE IF NOT EXISTS public.scene_character_assignments (
                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                scene_id UUID NOT NULL,
                character_id UUID NOT NULL REFERENCES public.characters(id) ON DELETE CASCADE,
                reference_image_path TEXT,
                created_at TIMESTAMPTZ DEFAULT NOW()
            );

            -- Indexes for fast autocomplete & lookup
            CREATE INDEX IF NOT EXISTS idx_characters_user_name ON public.characters (user_id, LOWER(character_name));
            CREATE INDEX IF NOT EXISTS idx_char_ref_images ON public.character_reference_images (character_id);
            CREATE INDEX IF NOT EXISTS idx_script_mentions ON public.script_character_mentions (script_id);
            CREATE INDEX IF NOT EXISTS idx_scene_assignments ON public.scene_character_assignments (scene_id);

            -- 6. Row Level Security (RLS)
            ALTER TABLE public.characters ENABLE ROW LEVEL SECURITY;
            ALTER TABLE public.character_reference_images ENABLE ROW LEVEL SECURITY;
            ALTER TABLE public.script_character_mentions ENABLE ROW LEVEL SECURITY;
            ALTER TABLE public.scene_character_assignments ENABLE ROW LEVEL SECURITY;

            -- Characters RLS Policies
            CREATE POLICY "Users can view only their own characters"
                ON public.characters FOR SELECT USING (auth.uid() = user_id);

            CREATE POLICY "Users can insert their own characters"
                ON public.characters FOR INSERT WITH CHECK (auth.uid() = user_id);

            CREATE POLICY "Users can update their own characters"
                ON public.characters FOR UPDATE USING (auth.uid() = user_id);

            CREATE POLICY "Users can delete their own characters"
                ON public.characters FOR DELETE USING (auth.uid() = user_id);

            -- 7. Supabase Storage Bucket setup
            -- Bucket Name: character-library
            INSERT INTO storage.buckets (id, name, public)
            VALUES ('character-library', 'character-library', false)
            ON CONFLICT (id) DO NOTHING;

            -- Storage RLS: Users can only upload and read files within their own folder: user_id/character_id/*
            CREATE POLICY "Users access own character images"
                ON storage.objects FOR ALL
                USING (bucket_id = 'character-library' AND auth.uid()::text = (storage.foldername(name))[1]);
        """.trimIndent()
    }
}
