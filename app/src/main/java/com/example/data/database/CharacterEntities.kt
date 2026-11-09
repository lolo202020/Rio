package com.example.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey
    val id: String, // UUID
    @ColumnInfo(name = "user_id")
    val userId: String = "user_default_01",
    @ColumnInfo(name = "character_name")
    val characterName: String,
    val description: String,
    @ColumnInfo(name = "appearance_description")
    val appearanceDescription: String,
    val hairstyle: String = "Natural textured dark hair",
    @ColumnInfo(name = "clothing_preferences")
    val clothingPreferences: String = "Casual contemporary",
    val personality: String,
    @ColumnInfo(name = "default_voice_id")
    val defaultVoiceId: String = "hi_male_funny_01",
    @ColumnInfo(name = "voice_language")
    val voiceLanguage: String = "Hindi",
    @ColumnInfo(name = "voice_gender")
    val voiceGender: String = "Male",
    @ColumnInfo(name = "speaking_style")
    val speakingStyle: String = "Funny & Energetic",
    @ColumnInfo(name = "voice_pitch")
    val voicePitch: String = "Medium",
    @ColumnInfo(name = "primary_image_path")
    val primaryImagePath: String, // Persistent local file path or cloud URI
    @ColumnInfo(name = "usage_count")
    val usageCount: Int = 0,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "character_reference_images",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["character_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["character_id"])]
)
data class CharacterReferenceImageEntity(
    @PrimaryKey
    val id: String, // UUID
    @ColumnInfo(name = "character_id")
    val characterId: String,
    @ColumnInfo(name = "user_id")
    val userId: String = "user_default_01",
    @ColumnInfo(name = "image_path")
    val imagePath: String,
    @ColumnInfo(name = "image_type")
    val imageType: String = "reference", // "primary", "front_angle", "side_profile", "expression"
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "script_character_mentions",
    indices = [Index(value = ["character_id"]), Index(value = ["script_id"])]
)
data class ScriptCharacterMentionEntity(
    @PrimaryKey
    val id: String, // UUID
    @ColumnInfo(name = "script_id")
    val scriptId: String,
    @ColumnInfo(name = "character_id")
    val characterId: String,
    @ColumnInfo(name = "mention_name")
    val mentionName: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "scene_character_assignments",
    indices = [Index(value = ["character_id"]), Index(value = ["scene_id"])]
)
data class SceneCharacterAssignmentEntity(
    @PrimaryKey
    val id: String, // UUID
    @ColumnInfo(name = "scene_id")
    val sceneId: String,
    @ColumnInfo(name = "character_id")
    val characterId: String,
    @ColumnInfo(name = "reference_image_path")
    val referenceImagePath: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
