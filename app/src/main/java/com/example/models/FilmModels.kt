package com.example.models

data class ProjectInput(
    val topicOrScript: String,
    val videoType: VideoType = VideoType.DRAMA,
    val language: String = "Hindi",
    val targetAudience: String = "General OTT & Cinema Viewers",
    val duration: String = "60s (Cinematic Scene)",
    val format: AspectRatioFormat = AspectRatioFormat.RATIO_16_9,
    val visualStyle: String = "Photorealistic 35mm Arri Alexa, Kodak Vision3 500T warm film grain",
    val charactersHint: String = "",
    val locationHint: String = "",
    val targetAiTool: AiVideoTool = AiVideoTool.KLING_2
)

data class ProjectBrief(
    val title: String,
    val logline: String,
    val corePremise: String,
    val genre: VideoType,
    val language: String,
    val targetAudience: String,
    val durationStr: String,
    val format: AspectRatioFormat,
    val visualStyle: String,
    val directorVisionStatement: String
)

data class EmotionalBeat(
    val timecodeSec: Int,
    val intensityPercent: Int, // 0 to 100
    val dominantEmotion: String,
    val description: String
)

data class ScriptAnalysis(
    val coreObjective: String,
    val actOneSetup: String,
    val actTwoConfrontation: String,
    val actThreeResolution: String,
    val subtextAnalysis: String,
    val emotionalGraph: List<EmotionalBeat>,
    val dramaticConflict: String,
    val keyTurningPoint: String,
    val comedicOrEmotionalPayoff: String,
    val silenceAndBreathingMoments: String,
    val scriptDoctorRecommendations: List<String>
)

data class CharacterProfile(
    val characterId: String, // e.g. "CHAR_01_KABIR"
    val name: String,
    val ageEstimate: String,
    val facialFeatures: String,
    val hairAndGrooming: String,
    val physiqueAndStature: String,
    val costumeWardrobe: String,
    val voiceTextureAndCadence: String,
    val internalMotivation: String,
    val deepFear: String,
    val emotionalBaseline: String,
    val actingPattern: String,
    val characterReferencePrompt: String // Ready prompt for image-consistency
)

data class EnvironmentProfile(
    val locationName: String,
    val description: String,
    val keyPropsAndTextures: List<String>,
    val timeOfDay: String,
    val weatherAndAtmosphere: String,
    val lightingAnchor: String,
    val spatialLayout: String
)

data class DirectorShot(
    val shotId: String,          // e.g. "SC01_SH01"
    val sceneId: String,        // e.g. "Scene 1"
    val startTime: String,      // e.g. "00:00"
    val endTime: String,        // e.g. "00:06"
    val durationSec: Int,       // e.g. 6
    val charactersInShot: List<String>,
    val shotSize: ShotSize,
    val cameraAngle: CameraAngle,
    val cameraMovement: CameraMovement,
    val lensSuggestion: String,
    val compositionAndFraming: String,
    val characterActionAndBlocking: String,
    val facialExpressionDetail: String, // Eye vector, brow, micro-tension
    val bodyLanguageDetail: String,    // Weight, shoulders, posture
    val dialogueLine: String,          // Spoken dialogue or "[SILENCE]"
    val voiceDirection: String,        // Texture, whisper, tremor, pauses
    val lightingSetup: String,         // Key light, fill ratio, Kelvin
    val backgroundAndProps: String,
    val colorGradeNote: String,
    val soundAndFoley: String,
    val cutPointAndNextMatch: String,
    val continuityNotes: String,
    val aiVideoPrompt: String,         // Precision copy-paste prompt
    val avoidNegativePrompt: String    // Tool-specific avoid list
)

data class ActingDirectionCue(
    val characterName: String,
    val lineOrMoment: String,
    val subtextWhatIsFelt: String,
    val eyeGazeAndBlinkCadence: String,
    val facialMusclesAndMouth: String,
    val postureAndHandMovement: String,
    val vocalPacingAndBreath: String,
    val reactionTimingBeat: String
)

data class LightingCameraPlan(
    val sceneTitle: String,
    val timeOfDay: String,
    val lightSources: String,
    val keyLightDetail: String,
    val fillRatio: String,
    val rimBacklight: String,
    val practicalsInFrame: String,
    val kelvinColorTemp: String,
    val shadowContrast: String,
    val skinTonePreservation: String,
    val continuityNotes: String
)

data class EditingSoundPlan(
    val editingPacingPhilosophy: String,
    val cutTransitions: List<String>,
    val dialogueSyncGuidelines: String,
    val roomToneAndAmbience: String,
    val foleySoundEffects: List<String>,
    val musicScoreCues: List<String>,
    val strategicSilenceMoments: List<String>
)

data class QualityControlReport(
    val actingScore: Int,
    val actingAuditList: List<Pair<String, Boolean>>,
    val cinematographyScore: Int,
    val cinematographyAuditList: List<Pair<String, Boolean>>,
    val continuityScore: Int,
    val continuityAuditList: List<Pair<String, Boolean>>,
    val storyScore: Int,
    val storyAuditList: List<Pair<String, Boolean>>,
    val directorVerdict: String,
    val preGenerationChecklist: List<String>
)

data class FullFilmProduction(
    val id: String,
    val createdAt: Long,
    val input: ProjectInput,
    val brief: ProjectBrief,
    val scriptAnalysis: ScriptAnalysis,
    val characters: List<CharacterProfile>,
    val environments: List<EnvironmentProfile>,
    val shotList: List<DirectorShot>,
    val actingDirectionSheet: List<ActingDirectionCue>,
    val lightingCameraPlan: List<LightingCameraPlan>,
    val editingSoundPlan: EditingSoundPlan,
    val qualityControl: QualityControlReport
)
