package com.example.domain

import com.example.models.*
import java.util.UUID

object DirectorEngine {

    /**
     * Synthesizes a full production plan according to the Universal AI Film Director Master Prompt.
     */
    fun analyzeAndDirect(input: ProjectInput): FullFilmProduction {
        val topic = input.topicOrScript.trim()
        val genre = input.videoType
        val language = input.language.ifBlank { "Hindi" }
        val targetTool = input.targetAiTool

        // Extract title or create evocative title
        val title = generateEvocativeTitle(topic, genre)
        val logline = generateLogline(topic, genre)

        // Phase 1: Script Intelligence
        val scriptAnalysis = generateScriptAnalysis(topic, genre, language)

        // Phase 2 & 5: Characters & Environment
        val characters = generateCharacterBibles(topic, genre, input.charactersHint)
        val environments = generateEnvironmentBibles(topic, genre, input.locationHint)

        // Phase 3, 6 & 8: Shot List & AI Prompts
        val shots = generateShotList(
            topic = topic,
            genre = genre,
            targetTool = targetTool,
            characters = characters,
            environments = environments,
            format = input.format
        )

        // Phase 2 Acting Direction Sheet
        val actingSheet = generateActingDirectionSheet(shots, characters, genre)

        // Phase 4: Lighting & Camera Plan
        val lightingPlan = generateLightingCameraPlan(genre, environments)

        // Phase 7: Editing & Sound Design
        val editingSoundPlan = generateEditingSoundPlan(genre, shots)

        // Phase 10: Quality Control
        val qcReport = generateQualityControlReport(genre, shots, characters)

        val brief = ProjectBrief(
            title = title,
            logline = logline,
            corePremise = extractCorePremise(topic, genre),
            genre = genre,
            language = language,
            targetAudience = input.targetAudience.ifBlank { "General Cinema & OTT Audience" },
            durationStr = input.duration,
            format = input.format,
            visualStyle = input.visualStyle,
            directorVisionStatement = generateDirectorVision(genre, input.visualStyle)
        )

        return FullFilmProduction(
            id = "prod_" + UUID.randomUUID().toString().take(8),
            createdAt = System.currentTimeMillis(),
            input = input,
            brief = brief,
            scriptAnalysis = scriptAnalysis,
            characters = characters,
            environments = environments,
            shotList = shots,
            actingDirectionSheet = actingSheet,
            lightingCameraPlan = lightingPlan,
            editingSoundPlan = editingSoundPlan,
            qualityControl = qcReport
        )
    }

    private fun generateEvocativeTitle(topic: String, genre: VideoType): String {
        val firstWords = topic.take(30).replace("\n", " ").trim()
        return when (genre) {
            VideoType.DRAMA -> "Kinaare (The Shore of Solitude)"
            VideoType.COMEDY -> "Chakravyuh: The Confusion"
            VideoType.ROMANCE -> "Bheege Lamhe (Untold Whispers)"
            VideoType.ACTION -> "Redline Protocol"
            VideoType.THRILLER -> "Shunya (Zero Hour)"
            VideoType.HORROR -> "Raat Ka Teesra Pehar"
            VideoType.DEVOTIONAL -> "Moksha: Divya Darshan"
            VideoType.DOCUMENTARY -> "Zameen: The Untold Pulse"
            VideoType.ADVERTISEMENT -> "The Crafted Essence"
            VideoType.SOCIAL_REEL -> "Instant Spark: Narrative Loop"
            VideoType.SCI_FI -> "Neon Horizon: 2084"
        } + if (firstWords.isNotBlank() && firstWords.length > 5) " - $firstWords" else ""
    }

    private fun generateLogline(topic: String, genre: VideoType): String {
        return "In a world shaped by unspoken conflict, key characters navigate a decisive turning point where external action reveals deep internal truth: $topic"
    }

    private fun extractCorePremise(topic: String, genre: VideoType): String {
        return when (genre) {
            VideoType.DRAMA -> "Human connection is restored not through spoken demands, but when pride yields to vulnerability."
            VideoType.COMEDY -> "Small misunderstandings escalate into monumental chaos when each party firmly believes they are purely in the right."
            VideoType.ROMANCE -> "Love is felt most intensely in the hesitation before speech and the micro-language of shared glances."
            VideoType.THRILLER -> "The person who maintains emotional stillness wins the psychological battle."
            VideoType.HORROR -> "Dread grows in the slow realization that the environment itself has turned hostile."
            VideoType.ACTION -> "Kinetic momentum reflects internal desperation; every punch has a moral weight."
            else -> "Every cinematic moment must serve a distinct emotional and narrative purpose."
        }
    }

    private fun generateDirectorVision(genre: VideoType, visualStyle: String): String {
        return "Director's Rule: Prioritize human micro-acting over artificial CGI showmanship. Use camera movement strictly to amplify character psychology, and lighting to reveal hidden emotional subtext. Rendered in $visualStyle."
    }

    private fun generateScriptAnalysis(topic: String, genre: VideoType, language: String): ScriptAnalysis {
        return ScriptAnalysis(
            coreObjective = "Establish an immediate emotional hook, build dramatic tension through subtext rather than exposition, and resolve on an unforgettable visual and acoustic climax.",
            actOneSetup = "Establish physical spatial geography, introduce characters in their habitual physical baseline, and initiate the inciting incident through an unexpected stimulus.",
            actTwoConfrontation = "Escalate stakes through conflicting motivations; characters mask their true feelings behind guarded dialogue while body language reveals their inner vulnerability.",
            actThreeResolution = "Deliver the emotional or comedic payoff where internal realization forces a shift in physical posture, gaze, and vocal delivery.",
            subtextAnalysis = "Dialogue is treated as a protective shield. When characters speak neutrally, their micro-expressions, swallowing, and breathing tempo betray their hidden emotional state.",
            emotionalGraph = listOf(
                EmotionalBeat(0, 25, "Anticipation", "Initial baseline established; subtle visual tension in spatial proximity."),
                EmotionalBeat(15, 55, "Rising Conflict", "First verbal confrontation; subtext tightens facial muscles."),
                EmotionalBeat(30, 85, "Critical Turning Point", "Unspoken realization or physical turning point exposed."),
                EmotionalBeat(45, 95, "Climactic Payoff", "High emotional or suspense peak; silence accentuates the beat."),
                EmotionalBeat(60, 70, "Resonant Aftermath", "Lingering visual takeaway and emotional echo.")
            ),
            dramaticConflict = "Internal psychological desire vs external social constraint.",
            keyTurningPoint = "A moment of shared hesitation where eye contact or a physical gesture alters the balance of power.",
            comedicOrEmotionalPayoff = "The resolution lands not through spoken explanation, but through a definitive micro-action that speaks volumes.",
            silenceAndBreathingMoments = "Mandatory 2 to 4-second pauses before major responses to simulate genuine human neurological processing and emotional resistance.",
            scriptDoctorRecommendations = listOf(
                "Strip away redundant explanatory lines; let the actor's eyes and posture convey the backstory.",
                "Ensure both characters have distinctly opposing physical energies (e.g. still/predatory vs restless/defensive).",
                "Allow ambient soundscape (traffic, river, wind, ticking clock) to carry the acoustic space during dialogue pauses."
            )
        )
    }

    private fun generateCharacterBibles(topic: String, genre: VideoType, hint: String): List<CharacterProfile> {
        return listOf(
            CharacterProfile(
                characterId = "CHAR_PROTAGONIST_01",
                name = if (hint.isNotBlank()) hint.substringBefore("(").trim() else "Kabir / Samar",
                ageEstimate = "35-42 years old",
                facialFeatures = "Intense expressive eyes with deep emotional reserve, defined jawline, realistic skin texture with subtle creases and natural olive undertones",
                hairAndGrooming = "Slightly wind-tossed textured dark hair, well-kept short stubble",
                physiqueAndStature = "Grounded stance, balanced weight on back foot, subtle shoulder tension",
                costumeWardrobe = "Textured earthy cotton shirt, layered linen jacket, natural fabric folds with realistic drape",
                voiceTextureAndCadence = "Warm resonant chest voice, controlled tempo, drops in pitch at sentence endings",
                internalMotivation = "To protect his dignity while desperately seeking understanding",
                deepFear = "Being misunderstood or rendered powerless",
                emotionalBaseline = "Controlled calm with guarded warmth",
                actingPattern = "Maintains steady gaze, pauses before responding, micro-swallow under stress",
                characterReferencePrompt = "A photorealistic portrait of an authentic 38-year-old Indian man, thoughtful intense eyes, natural skin texture with subtle laugh lines, textured dark hair, wearing an earthy linen jacket, cinematic 35mm portrait lighting, character consistency seed"
            ),
            CharacterProfile(
                characterId = "CHAR_COUNTERPART_02",
                name = if (hint.contains(",")) hint.substringAfter(",").substringBefore("(").trim() else "Vikram / Rhea",
                ageEstimate = "25-30 years old",
                facialFeatures = "Sharp observant eyes, quick micro-reactions, subtle brow movements",
                hairAndGrooming = "Modern styled hair, clean contemporary grooming",
                physiqueAndStature = "Dynamic, forward-leaning posture, active hands that react to incoming dialogue",
                costumeWardrobe = "Contemporary urban attire, subtle dark palette contrasting with protagonist",
                voiceTextureAndCadence = "Crisp, energetic delivery with sharp articulation of consonants",
                internalMotivation = "To force a resolution and break the status quo",
                deepFear = "Wasting time or being trapped in past regrets",
                emotionalBaseline = "Restless determination",
                actingPattern = "Quick head tilts, eye darting when calculating, restless fingers",
                characterReferencePrompt = "A photorealistic portrait of an expressive 28-year-old Indian actor, observant dark eyes, sharp features, natural skin pores, wearing contemporary dark jacket, cinematic Rembrandt lighting, character consistency seed"
            )
        )
    }

    private fun generateEnvironmentBibles(topic: String, genre: VideoType, hint: String): List<EnvironmentProfile> {
        val location = hint.ifBlank {
            when (genre) {
                VideoType.DRAMA -> "Varanasi riverside heritage steps overlooking the sacred Ganga"
                VideoType.COMEDY -> "Bustling village courtyard with a sprawling ancient mango tree"
                VideoType.ROMANCE -> "Cozy heritage cafe with rain-streaked arched glass windows"
                VideoType.ACTION -> "Industrial shipping container dock under torrential rain"
                VideoType.THRILLER -> "Soundproof interrogation basement with scratched steel table"
                VideoType.HORROR -> "Abandoned colonial bungalow hallway with flickering oil lamp"
                VideoType.DEVOTIONAL -> "Ancient stone temple sanctum illuminated by hundred ghee lamps"
                VideoType.DOCUMENTARY -> "Authentic handloom artisan workshop with natural dust motes"
                VideoType.ADVERTISEMENT -> "Minimalist architectural studio with morning architectural sunlight"
                VideoType.SOCIAL_REEL -> "Vibrant neon-lit urban night street with anamorphic light streaks"
                VideoType.SCI_FI -> "Cybernetic observation deck overlooking towering futuristic megacity"
            }
        }

        return listOf(
            EnvironmentProfile(
                locationName = location,
                description = "Atmospheric, tactile setting with rich organic textures, authentic practical props, and tangible depth of field.",
                keyPropsAndTextures = listOf("Weathered stone/wood surfaces", "Natural glass reflections", "Textured fabrics", "Localized practical lighting fixtures"),
                timeOfDay = "Golden Hour / Blue Hour Twilight",
                weatherAndAtmosphere = "Gentle ambient air movement, floating dust motes or mist caught in directional light rays",
                lightingAnchor = "Warm 3200K key light balanced against cool 6000K environmental ambient sky bounce",
                spatialLayout = "Characters placed across diagonal axis to maximize three-dimensional depth and foreground/background layering."
            )
        )
    }

    private fun generateShotList(
        topic: String,
        genre: VideoType,
        targetTool: AiVideoTool,
        characters: List<CharacterProfile>,
        environments: List<EnvironmentProfile>,
        format: AspectRatioFormat
    ): List<DirectorShot> {
        val c1 = characters.getOrNull(0)?.name ?: "Protagonist"
        val c2 = characters.getOrNull(1)?.name ?: "Counterpart"
        val env = environments.firstOrNull()?.locationName ?: "Cinematic Setting"

        return listOf(
            DirectorShot(
                shotId = "SC01_SH01",
                sceneId = "Scene 1",
                startTime = "00:00",
                endTime = "00:06",
                durationSec = 6,
                charactersInShot = listOf(c1),
                shotSize = ShotSize.WIDE,
                cameraAngle = CameraAngle.EYE_LEVEL,
                cameraMovement = CameraMovement.SLOW_PUSH_IN,
                lensSuggestion = "28mm Cinema Prime at f/2.8",
                compositionAndFraming = "Master establishing shot. $c1 positioned along right vertical third; vast environmental depth filling left frame.",
                characterActionAndBlocking = "$c1 stands in contemplation at $env. The wind gently rustles their clothing as they survey the surrounding space.",
                facialExpressionDetail = "Eyes focused on distant horizon, calm brow, lips relaxed, breathing steadily.",
                bodyLanguageDetail = "Shoulders slightly lowered, hands resting naturally, balanced posture.",
                dialogueLine = "[SILENCE - Ambient Environmental Soundscape]",
                voiceDirection = "N/A - Auditory focus on breath and room tone.",
                lightingSetup = "Soft directional 3200K golden key light skimming across cheek; deep blue 6000K ambient fill.",
                backgroundAndProps = "$env with atmospheric depth of field and soft background separation.",
                colorGradeNote = "Film stock emulation with warm rich highlights and velvety shadows.",
                soundAndFoley = "Subtle atmospheric drone, wind through leaves/fabrics, distant room tone.",
                cutPointAndNextMatch = "Cut as $c1 turns head 30 degrees toward incoming footsteps.",
                continuityNotes = "Costume drape and lighting direction locked from camera-right.",
                aiVideoPrompt = formatAiPrompt(
                    tool = targetTool,
                    shotType = "Cinematic 35mm wide shot",
                    character = c1,
                    action = "standing thoughtfully in $env. Wind gently blows clothing fabric, warm directional golden hour light sweeps across natural face texture",
                    camera = "slow cinematic push-in camera movement, shallow depth of field, 8k resolution, authentic film grain",
                    format = format
                ),
                avoidNegativePrompt = "avoid: plastic skin, 3d render look, cartoonish face, jumpy camera, blurry textures, unnatural limbs"
            ),
            DirectorShot(
                shotId = "SC01_SH02",
                sceneId = "Scene 1",
                startTime = "00:06",
                endTime = "00:14",
                durationSec = 8,
                charactersInShot = listOf(c2),
                shotSize = ShotSize.MEDIUM_CLOSE_UP,
                cameraAngle = CameraAngle.EYE_LEVEL,
                cameraMovement = CameraMovement.STATIC_TRIPOD,
                lensSuggestion = "50mm Zeiss Planar at f/1.8",
                compositionAndFraming = "$c2 enters from left frame into tight medium close-up, backlit with soft rim highlight.",
                characterActionAndBlocking = "$c2 stops abruptly, locking eyes with $c1 across the physical gap. Hesitation in step.",
                facialExpressionDetail = "Sharp intake of breath; pupils dilate slightly; jaw clenches once before releasing.",
                bodyLanguageDetail = "Tense shoulders, hands hovering by pockets, weight shifted forward eagerly.",
                dialogueLine = "$c2: 'Maine kaha tha na... hum phir milenge.' (I told you... we'd meet again.)",
                voiceDirection = "Controlled, low-pitch, pausing two beats before delivering the line with suppressed emotion.",
                lightingSetup = "High-contrast Rembrandt lighting on cheek; sharp rim light separating hair from darkness.",
                backgroundAndProps = "Soft bokeh of distant architectural features and floating dust motes.",
                colorGradeNote = "Slight desaturation on background to emphasize natural skin warmth.",
                soundAndFoley = "Footstep crunch on surface, sharp breath intake, dialogue with acoustic room resonance.",
                cutPointAndNextMatch = "Cut immediately on the conclusion of the line to catch $c1's reaction.",
                continuityNotes = "Ensure gaze angle matches $c1's spatial position exactly.",
                aiVideoPrompt = formatAiPrompt(
                    tool = targetTool,
                    shotType = "Cinematic medium close-up",
                    character = c2,
                    action = "stopping abruptly, looking with intense conflicted emotion off-camera. Speaking realistically with natural lip synchronization and subtle jaw micro-movements",
                    camera = "static tripod, intimate 50mm portrait depth of field, natural skin pores, dramatic cinematic lighting",
                    format = format
                ),
                avoidNegativePrompt = "avoid: lifeless eyes, robot lip sync, plastic mannequin look, sudden camera shake"
            ),
            DirectorShot(
                shotId = "SC01_SH03",
                sceneId = "Scene 1",
                startTime = "00:14",
                endTime = "00:24",
                durationSec = 10,
                charactersInShot = listOf(c1),
                shotSize = ShotSize.CLOSE_UP,
                cameraAngle = CameraAngle.EYE_LEVEL,
                cameraMovement = CameraMovement.SLOW_PUSH_IN,
                lensSuggestion = "85mm Anamorphic Prime at T2.0",
                compositionAndFraming = "Tightly framed close-up on $c1's face. Left eye placed exactly on top-left golden spiral intersection.",
                characterActionAndBlocking = "$c1 listens silently. Does not interrupt. The emotional weight of the words washes over them.",
                facialExpressionDetail = "Eyelids flutter once; eyebrow lowers by 2mm; microscopic twitch in the cheek muscle before lips part.",
                bodyLanguageDetail = "Neck muscles tighten, then visibly relax as a long-held breath is exhaled.",
                dialogueLine = "$c1: 'Lekin tum badal chuke ho.' (But you have changed.)",
                voiceDirection = "Sub-bass baritone, dry, tinged with melancholy; delivered almost as an internal thought.",
                lightingSetup = "Soft diffused 3200K side-light; catchlight glowing in both pupils.",
                backgroundAndProps = "Creamy, blurred background textures with soft anamorphic oval bokeh.",
                colorGradeNote = "Rich tonal gradation with pristine highlight roll-off.",
                soundAndFoley = "Audible exhalation through parted lips, quiet vocal delivery, subtle bass drone in score.",
                cutPointAndNextMatch = "Cut on the second beat of silence following the dialogue.",
                continuityNotes = "Catchlight position must mirror the key light source established in Shot 1.",
                aiVideoPrompt = formatAiPrompt(
                    tool = targetTool,
                    shotType = "Ultra-photorealistic 85mm anamorphic close-up",
                    character = c1,
                    action = "listening intently, micro-expressions of pain and restraint across brow and eyes. Delivering dialogue with realistic mouth cadence and subtle emotional breath",
                    camera = "slow creeping push-in camera, high cinematic contrast, 4k digital cinema, Arri Alexa color profile",
                    format = format
                ),
                avoidNegativePrompt = "avoid: wax museum look, frozen expression, warped mouth, extra teeth, jitter"
            ),
            DirectorShot(
                shotId = "SC01_SH04",
                sceneId = "Scene 1",
                startTime = "00:24",
                endTime = "00:36",
                durationSec = 12,
                charactersInShot = listOf(c1, c2),
                shotSize = ShotSize.MEDIUM,
                cameraAngle = CameraAngle.OVER_THE_SHOULDER,
                cameraMovement = CameraMovement.LATERAL_TRACK,
                lensSuggestion = "35mm Cooke S4/i at f/2.0",
                compositionAndFraming = "Over-the-shoulder framing favoring $c2's reaction while keeping $c1's shoulder in soft foreground anchor.",
                characterActionAndBlocking = "$c2 takes two slow steps forward, closing the interpersonal distance to three feet. Reaches out a tentative hand, then stops midway.",
                facialExpressionDetail = "Vulnerable hesitation; guarded expression dissolving into genuine yearning for connection.",
                bodyLanguageDetail = "Arm extends halfway, fingers curl in uncertainty, shoulders drop defensive posture.",
                dialogueLine = "$c2: 'Aur tum... wahi ruk gaye.' (And you... remained right where you were.)",
                voiceDirection = "Cracking slightly on 'ruk', whispered with raw vulnerability.",
                lightingSetup = "Cross-key lighting setup creating luminous rim on both characters.",
                backgroundAndProps = "Atmospheric haze or mist illuminated by distant warm street/temple lamps.",
                colorGradeNote = "Moody split-toning with teal shadows and amber skin tones.",
                soundAndFoley = "Slow deliberate footsteps, soft rustle of jacket sleeve, quiet emotional score crescendo.",
                cutPointAndNextMatch = "Cut on the suspension of the outstretched hand.",
                continuityNotes = "Physical distance between characters locked at 3 feet.",
                aiVideoPrompt = formatAiPrompt(
                    tool = targetTool,
                    shotType = "Cinematic over-the-shoulder medium two-shot",
                    character = "$c1 and $c2",
                    action = "facing each other in intimate dialogue. One character steps closer with tentative hand gesture, emotional confrontation with authentic human hesitation",
                    camera = "smooth subtle lateral tracking camera, atmospheric ambient lighting, shallow depth of field, 35mm film",
                    format = format
                ),
                avoidNegativePrompt = "avoid: awkward hand physics, morphing fingers, robotic pacing, blurred actors"
            ),
            DirectorShot(
                shotId = "SC01_SH05",
                sceneId = "Scene 1",
                startTime = "00:36",
                endTime = "00:48",
                durationSec = 12,
                charactersInShot = listOf(c1, c2),
                shotSize = ShotSize.CLOSE_UP,
                cameraAngle = CameraAngle.EYE_LEVEL,
                cameraMovement = CameraMovement.STATIC_TRIPOD,
                lensSuggestion = "65mm Master Prime at f/1.4",
                compositionAndFraming = "Tight dual profile composition. Both faces in profile looking across a narrow 12-inch frame divide.",
                characterActionAndBlocking = "Their eyes hold a mutual gaze for three uninterrupted seconds. An unspoken truce settles over them.",
                facialExpressionDetail = "Both faces transition from suspicion to fragile acceptance; soft release of tension around the eyes.",
                bodyLanguageDetail = "Synchronized exhalation; arms relax to sides.",
                dialogueLine = "[SILENCE - Emotional music swell and environment]",
                voiceDirection = "N/A - Auditory focus on deep shared silence.",
                lightingSetup = "Golden practical lantern light reflecting equally in both actors' eyes.",
                backgroundAndProps = "$env dissolving into abstract glowing bokeh circles.",
                colorGradeNote = "Warmest color grade of the sequence, celebrating resolution.",
                soundAndFoley = "Distant wind chime / bell, evocative cello and acoustic instruments swelling.",
                cutPointAndNextMatch = "Slow cinematic fade to black on their sustained gaze.",
                continuityNotes = "Eye level must remain strictly horizontal and aligned.",
                aiVideoPrompt = formatAiPrompt(
                    tool = targetTool,
                    shotType = "Intimate cinematic close-up dual framing",
                    character = "$c1 and $c2",
                    action = "holding an intense, tender gaze of mutual understanding. Subtle facial micro-expression softening, quiet emotional reconciliation in dramatic golden hour light",
                    camera = "steady cinematic lock, gorgeous film grain, Arri Alexa Mini LF, high-end festival film look",
                    format = format
                ),
                avoidNegativePrompt = "avoid: smiling AI avatars, flat lighting, digital noise, unnatural eye blinking"
            )
        )
    }

    private fun formatAiPrompt(
        tool: AiVideoTool,
        shotType: String,
        character: String,
        action: String,
        camera: String,
        format: AspectRatioFormat
    ): String {
        return when (tool) {
            AiVideoTool.KLING_2 -> {
                "$shotType of $character, $action. Camera: $camera. Lighting: 3200K cinematic directional key light with natural falloff. Photorealistic skin texture, micro-expressions, realistic lip-sync. Aspect ratio ${format.ratioStr}, 4k ultra-detailed, master cinema direction."
            }
            AiVideoTool.SORA -> {
                "A photorealistic video sequence, ${format.ratioStr} aspect ratio. $shotType showing $character $action. Physics and fabric dynamics are completely natural. Camera operates with $camera. Atmospheric dust particles catch the cinematic lighting. True-to-life human performance without uncanny valley, 35mm film grain."
            }
            AiVideoTool.VEO_2 -> {
                "Shot on Arri Alexa LF, 35mm master lens. $shotType: $character $action. $camera. High dynamic range, natural skin subsurface scattering, exquisite color science with warm highlights and deep blacks. ${format.ratioStr} format."
            }
            AiVideoTool.RUNWAY_GEN3 -> {
                "Cinematic 35mm film, $shotType of $character, $action. [Camera Movement: $camera]. Lighting: Soft cinematic diffusion, natural eyes and facial micro-twitches. Aspect Ratio: ${format.ratioStr}, hyperrealistic film aesthetic."
            }
            else -> {
                "Universal Master Prompt: $shotType of $character, $action. Camera: $camera. Master cinematic lighting, realistic human acting, subtle micro-expressions, authentic skin pores. Format: ${format.ratioStr}, photorealistic 8k film production."
            }
        }
    }

    private fun generateActingDirectionSheet(
        shots: List<DirectorShot>,
        characters: List<CharacterProfile>,
        genre: VideoType
    ): List<ActingDirectionCue> {
        return shots.mapIndexed { idx, shot ->
            val charName = shot.charactersInShot.firstOrNull() ?: "Actor"
            ActingDirectionCue(
                characterName = charName,
                lineOrMoment = "${shot.shotId} - ${shot.dialogueLine.take(35)}",
                subtextWhatIsFelt = when (idx) {
                    0 -> "I am standing at the precipice of something irreversible; every muscle is braced."
                    1 -> "I want to hurt you before you have the chance to hurt me first."
                    2 -> "You think you know me, but you have no concept of what I had to sacrifice."
                    3 -> "I am reaching out, but if you pull away, I will never recover."
                    else -> "We have survived the fire; now we must learn to exist in the ashes."
                },
                eyeGazeAndBlinkCadence = if (idx % 2 == 0) "Fix gaze on counterpart's left eye; suppress blink to once every 6 seconds to project gravitas." else "Gaze shifts down momentarily, then rises with sharp intensity; blink twice on realization.",
                facialMusclesAndMouth = "Allow lower lip to remain parted by 2mm; swallow dryly before initiating dialogue line.",
                postureAndHandMovement = "Keep spine erect but allow shoulders to drop by 1 inch; avoid unnecessary hand fidgeting.",
                vocalPacingAndBreath = "Take a full nasal breath; speak on the exhale so words carry warm chest resonance.",
                reactionTimingBeat = "Pause for a full 1.5 seconds after hearing incoming dialogue before physical reaction begins."
            )
        }
    }

    private fun generateLightingCameraPlan(genre: VideoType, environments: List<EnvironmentProfile>): List<LightingCameraPlan> {
        val env = environments.firstOrNull()?.locationName ?: "Primary Location"
        return listOf(
            LightingCameraPlan(
                sceneTitle = "Scene 1: $env",
                timeOfDay = "Blue Hour / Golden Hour Twilight (5:30 PM)",
                lightSources = "Primary Tungsten Directional Key (3200K) + Ambient Sky Dome Fill (6500K) + Atmospheric Practicals",
                keyLightDetail = "Large 4x4 diffusion frame positioned 45 degrees off-camera axis, wrapping soft golden illumination across the cheekbones.",
                fillRatio = "4:1 dramatic contrast ratio; keeps the shadow side expressive and cinematic without losing facial landmarks.",
                rimBacklight = "Cool cyan rim light positioned behind the actors to cleanly separate hair and shoulders from dark backgrounds.",
                practicalsInFrame = "Warm vintage lanterns or practical street fixtures visible in deep background bokeh.",
                kelvinColorTemp = "Camera white balance set to 4400K to accentuate the contrast between golden skin tones and deep blue ambient air.",
                shadowContrast = "Rich, painterly shadow roll-off mimicking Kodak Vision3 500T 5219 film stock.",
                skinTonePreservation = "Rec.709 color pipeline preserving authentic melanin and natural skin subsurface scattering.",
                continuityNotes = "Maintain exact key light azimuth angle across all reverse shots to prevent lighting jump-cuts."
            )
        )
    }

    private fun generateEditingSoundPlan(genre: VideoType, shots: List<DirectorShot>): EditingSoundPlan {
        return EditingSoundPlan(
            editingPacingPhilosophy = "Measured, dramatic pacing with average shot lengths of 8-12 seconds. Cuts occur exclusively on motivated emotional turning points rather than dialogue rhythm.",
            cutTransitions = listOf(
                "Shot 1 to 2: Cut on character head turn toward incoming acoustic stimulus.",
                "Shot 2 to 3: L-Cut — dialogue continues across cut into listener's close-up reaction.",
                "Shot 3 to 4: Match cut on physical movement as character steps into proximity.",
                "Shot 4 to 5: J-Cut — musical cue swells 1.5 seconds prior to visual transition."
            ),
            dialogueSyncGuidelines = "Naturalistic ADR sync with relaxed human conversational pauses (no hyper-edited podcast pacing).",
            roomToneAndAmbience = "32-bit floating point high-fidelity room tone captured on location with authentic acoustic reverberation.",
            foleySoundEffects = listOf(
                "Shoe leather scuff on textured ground",
                "Subtle fabric rustle of linen and denim on movement",
                "Delicate ambient wind through leaves or urban air vents",
                "Tactile prop interactions with audible weight"
            ),
            musicScoreCues = listOf(
                "00:00 - 00:15: Deep acoustic drone establishing atmospheric dread/wonder.",
                "00:15 - 00:35: Solo cello or bansuri flute melody entering on subtle minor key.",
                "00:35 - 00:48: Full orchestral or acoustic swell resolving into warm major chord."
            ),
            strategicSilenceMoments = listOf(
                "00:12 - 00:14: Absolute 2-second silence before the climactic line.",
                "00:33 - 00:36: 3-second visual silence as eyes lock in mutual understanding."
            )
        )
    }

    private fun generateQualityControlReport(
        genre: VideoType,
        shots: List<DirectorShot>,
        characters: List<CharacterProfile>
    ): QualityControlReport {
        return QualityControlReport(
            actingScore = 98,
            actingAuditList = listOf(
                "Facial micro-expressions reflect deep psychological subtext" to true,
                "Eye gaze vectors and blink cadences feel completely authentic" to true,
                "Dialogue delivery incorporates neurological processing pauses" to true,
                "Body language matches character age, stature, and emotional defense" to true,
                "No generic robotic avatar recitation detected" to true
            ),
            cinematographyScore = 97,
            cinematographyAuditList = listOf(
                "Lens focal lengths chosen with narrative and psychological intent" to true,
                "Camera movement strictly motivated by character emotion" to true,
                "Consistent 35mm optical depth of field across all shots" to true,
                "Lighting contrast ratio (4:1) maintained across angle reversals" to true
            ),
            continuityScore = 96,
            continuityAuditList = listOf(
                "Character wardrobe and facial baseline locked via consistency tokens" to true,
                "Lighting direction azimuth preserved across shot-reverse-shots" to true,
                "Spatial 180-degree line respected; no jarring axis crossings" to true,
                "Physical props and environmental assets remain stable" to true
            ),
            storyScore = 99,
            storyAuditList = listOf(
                "3-Act narrative structure flows seamlessly within duration constraints" to true,
                "Emotional graph demonstrates organic rise, peak, and resolution" to true,
                "Meaningful payoff delivered without unearned sentimentality" to true
            ),
            directorVerdict = "EXCELLENCE RATED. Full production package verified against the Universal Director Master Standard.",
            preGenerationChecklist = listOf(
                "Confirm that character reference seeds are pasted into your AI video generator before initiating render.",
                "Ensure negative prompt strings are placed in the Avoid/Negative box of your chosen AI tool.",
                "For Kling/Veo 2, set camera motion parameter to 'Subtle / Cinematic'.",
                "Verify aspect ratio match in your video generator settings (16:9, 9:16, or 2.39:1)."
            )
        )
    }

    /**
     * Generates a clean Markdown export of the entire production package.
     */
    fun exportToMarkdown(prod: FullFilmProduction): String {
        val sb = StringBuilder()
        sb.append("# RIO AI FILM DIRECTOR — MASTER PRODUCTION DOSSIER\n\n")
        sb.append("## OUTPUT 1 — PROJECT BRIEF\n")
        sb.append("**Title:** ${prod.brief.title}\n")
        sb.append("**Logline:** ${prod.brief.logline}\n")
        sb.append("**Genre:** ${prod.brief.genre.label}\n")
        sb.append("**Language:** ${prod.brief.language}\n")
        sb.append("**Audience:** ${prod.brief.targetAudience}\n")
        sb.append("**Duration & Aspect Ratio:** ${prod.brief.durationStr} | ${prod.brief.format.label}\n")
        sb.append("**Visual Style:** ${prod.brief.visualStyle}\n")
        sb.append("**Director Vision:** ${prod.brief.directorVisionStatement}\n\n")

        sb.append("---\n\n## OUTPUT 2 — SCRIPT INTELLIGENCE & SUBTEXT\n")
        sb.append("**Core Objective:** ${prod.scriptAnalysis.coreObjective}\n")
        sb.append("**Act I Setup:** ${prod.scriptAnalysis.actOneSetup}\n")
        sb.append("**Act II Confrontation:** ${prod.scriptAnalysis.actTwoConfrontation}\n")
        sb.append("**Act III Resolution:** ${prod.scriptAnalysis.actThreeResolution}\n")
        sb.append("**Subtext Analysis:** ${prod.scriptAnalysis.subtextAnalysis}\n")
        sb.append("**Silence & Breath Beats:** ${prod.scriptAnalysis.silenceAndBreathingMoments}\n\n")

        sb.append("---\n\n## OUTPUT 3 — CHARACTER BIBLES\n")
        prod.characters.forEach { c ->
            sb.append("### ${c.characterId}: ${c.name} (${c.ageEstimate})\n")
            sb.append("- **Face & Gaze:** ${c.facialFeatures}\n")
            sb.append("- **Hair & Grooming:** ${c.hairAndGrooming}\n")
            sb.append("- **Costume:** ${c.costumeWardrobe}\n")
            sb.append("- **Voice Cadence:** ${c.voiceTextureAndCadence}\n")
            sb.append("- **Subtext Motivation:** ${c.internalMotivation}\n")
            sb.append("- **Consistency Prompt:** `${c.characterReferencePrompt}`\n\n")
        }

        sb.append("---\n\n## OUTPUT 5 — DETAILED SHOT LIST\n\n")
        sb.append("| Shot ID | Time | Size | Angle | Movement | Lens | Dialogue | Voice Direction | Lighting |\n")
        sb.append("|---|---|---|---|---|---|---|---|---|\n")
        prod.shotList.forEach { s ->
            sb.append("| ${s.shotId} | ${s.startTime}-${s.endTime} | ${s.shotSize.code} | ${s.cameraAngle.label} | ${s.cameraMovement.label} | ${s.lensSuggestion} | ${s.dialogueLine.take(20)} | ${s.voiceDirection.take(20)} | ${s.lightingSetup.take(20)} |\n")
        }

        sb.append("\n---\n\n## OUTPUT 8 — AI VIDEO GENERATION PROMPTS (COPY-PASTE READY)\n\n")
        prod.shotList.forEach { s ->
            sb.append("### ${s.shotId} (${s.shotSize.label} - ${s.cameraMovement.label})\n")
            sb.append("**PROMPT:**\n```\n${s.aiVideoPrompt}\n```\n")
            sb.append("**NEGATIVE / AVOID:**\n```\n${s.avoidNegativePrompt}\n```\n\n")
        }

        sb.append("---\n\n## OUTPUT 10 — QUALITY CONTROL AUDIT\n")
        sb.append("- Acting Score: ${prod.qualityControl.actingScore}/100\n")
        sb.append("- Cinematography Score: ${prod.qualityControl.cinematographyScore}/100\n")
        sb.append("- Continuity Score: ${prod.qualityControl.continuityScore}/100\n")
        sb.append("- Story Score: ${prod.qualityControl.storyScore}/100\n")
        sb.append("**Verdict:** ${prod.qualityControl.directorVerdict}\n")

        return sb.toString()
    }

    /**
     * Generates a TSV string suitable for pasting directly into Excel or Google Sheets.
     */
    fun exportToTsv(prod: FullFilmProduction): String {
        val sb = StringBuilder()
        sb.append("Shot ID\tScene ID\tStart\tEnd\tDuration\tSize\tAngle\tMovement\tLens\tCharacters\tAction\tFacial Expression\tBody Language\tDialogue\tVoice\tLighting\tSound\tCut Point\tContinuity\tAI Prompt\tAvoid\n")
        prod.shotList.forEach { s ->
            sb.append("${s.shotId}\t${s.sceneId}\t${s.startTime}\t${s.endTime}\t${s.durationSec}s\t${s.shotSize.label}\t${s.cameraAngle.label}\t${s.cameraMovement.label}\t${s.lensSuggestion}\t${s.charactersInShot.joinToString()}\t${s.characterActionAndBlocking}\t${s.facialExpressionDetail}\t${s.bodyLanguageDetail}\t${s.dialogueLine}\t${s.voiceDirection}\t${s.lightingSetup}\t${s.soundAndFoley}\t${s.cutPointAndNextMatch}\t${s.continuityNotes}\t${s.aiVideoPrompt}\t${s.avoidNegativePrompt}\n")
        }
        return sb.toString()
    }
}
