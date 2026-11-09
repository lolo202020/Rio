package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {

    @Query("SELECT * FROM characters ORDER BY updated_at DESC")
    fun getAllCharacters(): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters WHERE id = :id LIMIT 1")
    suspend fun getCharacterById(id: String): CharacterEntity?

    @Query("SELECT * FROM characters WHERE LOWER(character_name) = LOWER(:name) LIMIT 1")
    suspend fun getCharacterByName(name: String): CharacterEntity?

    @Query("SELECT * FROM characters WHERE character_name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY character_name ASC")
    fun searchCharacters(query: String): Flow<List<CharacterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity)

    @Update
    suspend fun updateCharacter(character: CharacterEntity)

    @Delete
    suspend fun deleteCharacter(character: CharacterEntity)

    @Query("DELETE FROM characters WHERE id = :id")
    suspend fun deleteCharacterById(id: String)

    @Query("UPDATE characters SET usage_count = usage_count + 1 WHERE id = :id")
    suspend fun incrementUsageCount(id: String)

    @Query("SELECT * FROM character_reference_images WHERE character_id = :characterId ORDER BY created_at ASC")
    fun getReferenceImages(characterId: String): Flow<List<CharacterReferenceImageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferenceImage(image: CharacterReferenceImageEntity)

    @Query("DELETE FROM character_reference_images WHERE id = :imageId")
    suspend fun deleteReferenceImage(imageId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScriptMention(mention: ScriptCharacterMentionEntity)

    @Query("SELECT * FROM script_character_mentions WHERE script_id = :scriptId")
    suspend fun getScriptMentions(scriptId: String): List<ScriptCharacterMentionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSceneAssignment(assignment: SceneCharacterAssignmentEntity)

    @Query("SELECT * FROM scene_character_assignments WHERE scene_id = :sceneId")
    suspend fun getSceneAssignments(sceneId: String): List<SceneCharacterAssignmentEntity>

    @Query("DELETE FROM scene_character_assignments WHERE scene_id = :sceneId")
    suspend fun clearSceneAssignments(sceneId: String)

    @Query("SELECT COUNT(*) FROM characters")
    suspend fun getTotalCharactersCount(): Int

    @Query("SELECT COUNT(*) FROM character_reference_images")
    suspend fun getTotalReferenceImagesCount(): Int

    @Query("SELECT * FROM characters ORDER BY usage_count DESC LIMIT :limit")
    suspend fun getMostUsedCharacters(limit: Int = 5): List<CharacterEntity>
}
