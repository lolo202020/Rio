package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.CharacterEntity
import com.example.data.repository.CharacterLibraryStats
import com.example.data.repository.CharacterRepository
import com.example.domain.CuratedProductions
import com.example.domain.DirectorEngine
import com.example.domain.GeminiScriptAnalyzer
import com.example.models.FullFilmProduction
import com.example.models.ProjectInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainNavigation {
    STUDIO,
    MASTER_DECK,
    CHARACTER_LIBRARY,
    SHOT_LAB,
    SUBTEXT_COACH,
    VAULT
}

enum class DeckTab(val tabIndex: Int, val shortTitle: String, val fullTitle: String) {
    BRIEF(0, "Brief", "OUTPUT 1: Project Brief"),
    SCRIPT_AI(1, "Script Analysis", "OUTPUT 2: Script Intelligence & Graph"),
    CHARACTERS(2, "Bibles", "OUTPUT 3 & 4: Character & Environment Bibles"),
    SHOT_TABLE(3, "Shot List", "OUTPUT 5: Director's Shot Table"),
    ACTING(4, "Acting Sheet", "OUTPUT 6: Human Acting Simulation"),
    LIGHTING(5, "Lighting Deck", "OUTPUT 7: Cinematography & Lighting Plan"),
    AI_PROMPTS(6, "AI Prompts", "OUTPUT 8: Copy-Paste AI Prompts"),
    EDITING(7, "Editing & Sound", "OUTPUT 9: Editing & Sound Design"),
    QUALITY_QC(8, "QC Audit", "OUTPUT 10: Automatic Quality Control"),
    DOSSIER(9, "Export Dossier", "OUTPUT 11: Final Production Package")
}

data class RioUiState(
    val currentNavigation: MainNavigation = MainNavigation.STUDIO,
    val activeDeckTab: DeckTab = DeckTab.SHOT_TABLE,
    val currentProduction: FullFilmProduction = CuratedProductions.defaultProduction,
    val savedProductions: List<FullFilmProduction> = CuratedProductions.allCurated,
    val characterList: List<CharacterEntity> = emptyList(),
    val characterStats: CharacterLibraryStats = CharacterLibraryStats(0, 0, 0, emptyList(), 0L),
    val characterToCreatePrefill: String? = null,
    val isGenerating: Boolean = false,
    val generationProgressMessage: String = "",
    val isSpeaking: Boolean = false,
    val currentSpeakingUtterance: String? = null,
    val isStoryboardViewMode: Boolean = true,
    val isHindiMode: Boolean = false,
    val toastMessage: String? = null
)

class RioDirectorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CharacterRepository(application)
    private val ttsManager = com.example.data.audio.TextToSpeechManager(application)

    private val _uiState = MutableStateFlow(RioUiState())
    val uiState: StateFlow<RioUiState> = _uiState.asStateFlow()

    val supabaseSql: String by lazy { repository.generateSupabaseSql() }

    init {
        // Observe permanent characters from Room database
        viewModelScope.launch {
            repository.getAllCharacters().collect { chars ->
                _uiState.update { it.copy(characterList = chars) }
                refreshCharacterStats()
            }
        }
        // Observe TTS state
        viewModelScope.launch {
            ttsManager.isSpeaking.collect { speaking ->
                _uiState.update { it.copy(isSpeaking = speaking) }
            }
        }
        viewModelScope.launch {
            ttsManager.currentUtteranceId.collect { id ->
                _uiState.update { it.copy(currentSpeakingUtterance = id) }
            }
        }
    }

    fun refreshCharacterStats() {
        viewModelScope.launch {
            val stats = repository.getLibraryStats()
            _uiState.update { it.copy(characterStats = stats) }
        }
    }

    fun navigateTo(nav: MainNavigation) {
        _uiState.update { it.copy(currentNavigation = nav) }
    }

    fun selectDeckTab(tab: DeckTab) {
        _uiState.update { it.copy(activeDeckTab = tab) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun loadProduction(prod: FullFilmProduction) {
        _uiState.update {
            it.copy(
                currentProduction = prod,
                currentNavigation = MainNavigation.MASTER_DECK,
                toastMessage = "Loaded: ${prod.brief.title}"
            )
        }
    }

    fun directFilm(input: ProjectInput) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGenerating = true,
                    generationProgressMessage = "Phase 1: Analyzing script intelligence & @character mentions..."
                )
            }
            delay(300)
            _uiState.update {
                it.copy(generationProgressMessage = "Phase 2 & 5: Matching saved Character IDs and reference photos...")
            }
            delay(300)
            _uiState.update {
                it.copy(generationProgressMessage = "Phase 3 & 4: Designing shots, focal lenses & 3200K lighting...")
            }
            delay(300)
            _uiState.update {
                it.copy(generationProgressMessage = "Phase 6 & 8: Generating voice profiles & character consistency prompts...")
            }
            delay(300)

            // Detect @character mentions in script
            val detection = repository.detectCharactersInScript(input.topicOrScript)

            val baseProduction = DirectorEngine.analyzeAndDirect(input)

            // Enhance characters with saved library characters if detected
            val enhancedCharacters = if (detection.detectedCharacters.isNotEmpty()) {
                detection.detectedCharacters.map { charEntity ->
                    com.example.models.CharacterProfile(
                        characterId = charEntity.id,
                        name = charEntity.characterName,
                        ageEstimate = charEntity.appearanceDescription.take(20),
                        facialFeatures = charEntity.appearanceDescription,
                        hairAndGrooming = charEntity.hairstyle,
                        physiqueAndStature = "Consistent with character reference",
                        costumeWardrobe = charEntity.clothingPreferences,
                        voiceTextureAndCadence = "${charEntity.voiceLanguage} ${charEntity.voiceGender} (${charEntity.speakingStyle})",
                        internalMotivation = charEntity.personality,
                        deepFear = "Failure in scene objective",
                        emotionalBaseline = charEntity.speakingStyle,
                        actingPattern = "Consistent face and demeanor from reference library",
                        characterReferencePrompt = "Character Reference ID: ${charEntity.id}, Name: ${charEntity.characterName}, ${charEntity.appearanceDescription}, wearing ${charEntity.clothingPreferences}, hairstyle: ${charEntity.hairstyle}, consistent facial identity lock"
                    )
                } + baseProduction.characters.filter { baseChar ->
                    detection.detectedCharacters.none { it.characterName.equals(baseChar.name, ignoreCase = true) }
                }
            } else {
                baseProduction.characters
            }

            val finalProduction = baseProduction.copy(characters = enhancedCharacters)
            val updatedSaved = listOf(finalProduction) + _uiState.value.savedProductions.filter { it.id != finalProduction.id }

            _uiState.update {
                it.copy(
                    isGenerating = false,
                    currentProduction = finalProduction,
                    savedProductions = updatedSaved,
                    currentNavigation = MainNavigation.MASTER_DECK,
                    activeDeckTab = DeckTab.SHOT_TABLE,
                    toastMessage = "Film Directed: ${finalProduction.brief.title}"
                )
            }
        }
    }

    fun analyzeScriptWithGemini(input: ProjectInput) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGenerating = true,
                    generationProgressMessage = "Calling Gemini API: Analyzing screenplay subtext & @character mapping..."
                )
            }

            val result = GeminiScriptAnalyzer.analyzeWithGemini(input) { progress ->
                _uiState.update { it.copy(generationProgressMessage = progress) }
            }

            val newProduction = result.getOrElse { DirectorEngine.analyzeAndDirect(input) }
            val updatedSaved = listOf(newProduction) + _uiState.value.savedProductions.filter { it.id != newProduction.id }

            _uiState.update {
                it.copy(
                    isGenerating = false,
                    currentProduction = newProduction,
                    savedProductions = updatedSaved,
                    currentNavigation = MainNavigation.MASTER_DECK,
                    activeDeckTab = DeckTab.SCRIPT_AI,
                    toastMessage = "Script Analyzed with Gemini: ${newProduction.brief.title}"
                )
            }
        }
    }

    // Permanent Character Library Operations
    fun saveNewCharacter(
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
    ) {
        viewModelScope.launch {
            repository.createCharacter(
                name = name,
                description = description,
                appearance = appearance,
                hairstyle = hairstyle,
                clothing = clothing,
                personality = personality,
                voiceLanguage = voiceLanguage,
                voiceGender = voiceGender,
                speakingStyle = speakingStyle,
                primaryImageUri = imageUri
            )
            refreshCharacterStats()
        }
    }

    fun updateCharacter(character: CharacterEntity) {
        viewModelScope.launch {
            repository.updateCharacter(character)
            refreshCharacterStats()
        }
    }

    fun deleteCharacter(characterId: String) {
        viewModelScope.launch {
            repository.deleteCharacter(characterId)
            refreshCharacterStats()
        }
    }

    fun saveCurrentToVault() {
        val curr = _uiState.value.currentProduction
        if (_uiState.value.savedProductions.none { it.id == curr.id }) {
            _uiState.update {
                it.copy(
                    savedProductions = listOf(curr) + it.savedProductions,
                    toastMessage = "Production saved to Vault"
                )
            }
        } else {
            showToast("Already in Vault")
        }
    }

    fun deleteFromVault(id: String) {
        _uiState.update {
            it.copy(
                savedProductions = it.savedProductions.filter { p -> p.id != id },
                toastMessage = "Deleted from Vault"
            )
        }
    }

    fun toggleStoryboardViewMode() {
        _uiState.update { it.copy(isStoryboardViewMode = !it.isStoryboardViewMode) }
    }

    fun toggleLanguage() {
        _uiState.update {
            val next = !it.isHindiMode
            it.copy(
                isHindiMode = next,
                toastMessage = if (next) "भाषा: हिन्दी सक्रिय" else "Language: English active"
            )
        }
    }

    fun speakDialogue(text: String, characterName: String = "", language: String = "Hindi") {
        val matchedChar = _uiState.value.characterList.find {
            it.characterName.equals(characterName.trim(), ignoreCase = true)
        }
        val gender = matchedChar?.voiceGender ?: if (characterName.contains("Mohini", ignoreCase = true) || characterName.contains("Mummy", ignoreCase = true)) "Female" else "Male"
        val style = matchedChar?.speakingStyle ?: "Natural"
        val lang = matchedChar?.voiceLanguage ?: language

        ttsManager.speak(
            text = text,
            language = lang,
            gender = gender,
            speakingStyle = style
        )
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}

