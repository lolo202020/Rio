package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        CharacterEntity::class,
        CharacterReferenceImageEntity::class,
        ScriptCharacterMentionEntity::class,
        SceneCharacterAssignmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun characterDao(): CharacterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rio_character_library.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate core characters requested by user
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    prepopulateDefaultCharacters(database.characterDao())
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun prepopulateDefaultCharacters(dao: CharacterDao) {
            val defaultCharacters = listOf(
                CharacterEntity(
                    id = "char_raju_default",
                    characterName = "Raju",
                    description = "Relatable, witty, and humorous young Indian protagonist. Often gets into funny daily dilemmas and misadventures.",
                    appearanceDescription = "22-year-old expressive Indian male, warm brown skin, animated big eyes, bright mischievous smile, slight stubble.",
                    hairstyle = "Short messy modern dark crop, slightly gelled at front.",
                    clothingPreferences = "Vibrant orange cotton polo t-shirt, blue denim jeans, white sneakers.",
                    personality = "Funny, warm-hearted, energetic, quick-witted, slightly dramatic in reactions.",
                    defaultVoiceId = "hi_male_funny_raju",
                    voiceLanguage = "Hindi",
                    voiceGender = "Male",
                    speakingStyle = "Funny, energetic with humorous comedic timing",
                    voicePitch = "Medium-High",
                    primaryImagePath = "android.resource://com.example/drawable/ic_launcher_foreground",
                    usageCount = 12
                ),
                CharacterEntity(
                    id = "char_mohini_default",
                    characterName = "Mohini",
                    description = "Sharp-minded, intelligent, and sarcastic female counterpart who always outsmarts Raju.",
                    appearanceDescription = "21-year-old sharp-featured Indian female, radiant glowing face, arched eyebrows, expressive observant brown eyes.",
                    hairstyle = "Long dark wavy hair, parted on the side with delicate loose strands.",
                    clothingPreferences = "Bright yellow embroidered kurti with white dupatta, silver oxidized jhumkas.",
                    personality = "Sensible, sharp-tongued, observant, affectionate but grounded.",
                    defaultVoiceId = "hi_female_natural_mohini",
                    voiceLanguage = "Hindi",
                    voiceGender = "Female",
                    speakingStyle = "Natural, witty, articulate with playful sarcasm",
                    voicePitch = "Medium",
                    primaryImagePath = "android.resource://com.example/drawable/ic_launcher_foreground",
                    usageCount = 9
                ),
                CharacterEntity(
                    id = "char_papaji_default",
                    characterName = "Papa Ji",
                    description = "Traditional, stern, yet deeply caring Indian father. Lover of morning tea and newspaper.",
                    appearanceDescription = "54-year-old dignified Indian father, wire-rimmed reading spectacles, grey-white peppered moustache, laughing crinkles around eyes.",
                    hairstyle = "Neat side-parted receding silver-black hair.",
                    clothingPreferences = "Crisp beige kurta with brown woollen Nehru jacket or cardigan.",
                    personality = "Authoritative, wise, impatient with modern gadgets, nostalgic.",
                    defaultVoiceId = "hi_male_mature_papaji",
                    voiceLanguage = "Hindi",
                    voiceGender = "Male",
                    speakingStyle = "Mature, booming baritone, stern yet fatherly",
                    voicePitch = "Deep",
                    primaryImagePath = "android.resource://com.example/drawable/ic_launcher_foreground",
                    usageCount = 6
                ),
                CharacterEntity(
                    id = "char_mummyji_default",
                    characterName = "Mummy Ji",
                    description = "Affectionate, superstitious, and culinary expert Indian mother who rules the household kitchen.",
                    appearanceDescription = "50-year-old warm-faced Indian mother, small red bindi on forehead, gentle caring eyes, warm smile.",
                    hairstyle = "Neat traditional low hair bun secured with floral hairpins.",
                    clothingPreferences = "Traditional printed cotton sari with red border, thin gold bangles.",
                    personality = "Nurturing, melodramatic when worried, quick with witty motherly advice.",
                    defaultVoiceId = "hi_female_warm_mummyji",
                    voiceLanguage = "Hindi",
                    voiceGender = "Female",
                    speakingStyle = "Warm, emotional, rapid motherly cadence",
                    voicePitch = "Medium",
                    primaryImagePath = "android.resource://com.example/drawable/ic_launcher_foreground",
                    usageCount = 5
                ),
                CharacterEntity(
                    id = "char_friend_bunty",
                    characterName = "Friend",
                    description = "Raju's loyal, goofy best friend who always gives terrible but hilarious advice.",
                    appearanceDescription = "22-year-old cheerful Indian male, round friendly face, wide infectious grin, always fidgeting.",
                    hairstyle = "Textured fade, often wears a backward baseball cap.",
                    clothingPreferences = "Oversized graphic streetwear t-shirt, cargo pants, smart band on wrist.",
                    personality = "Over-enthusiastic, superstitious, loyal wingman, loves street food.",
                    defaultVoiceId = "hi_male_energetic_bunty",
                    voiceLanguage = "Hindi",
                    voiceGender = "Male",
                    speakingStyle = "Fast-talking, slang-heavy, casual youth style",
                    voicePitch = "Medium",
                    primaryImagePath = "android.resource://com.example/drawable/ic_launcher_foreground",
                    usageCount = 4
                )
            )

            defaultCharacters.forEach { charEntity ->
                dao.insertCharacter(charEntity)
            }
        }
    }
}
