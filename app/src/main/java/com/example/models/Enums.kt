package com.example.models

enum class VideoType(val label: String, val hindiLabel: String, val iconName: String) {
    DRAMA("Drama", "ड्रामा / संजीदा", "theater_comedy"),
    COMEDY("Comedy", "कॉमेडी / हास्य", "sentiment_very_satisfied"),
    ROMANCE("Romance", "रोमांस / प्रेम", "favorite"),
    ACTION("Action", "एक्शन / मारधाड़", "flash_on"),
    THRILLER("Suspense Thriller", "थ्रिलर / रहस्य", "visibility"),
    HORROR("Horror", "हॉरर / डरावना", "nightlight_round"),
    DEVOTIONAL("Devotional / Mythological", "भक्ति / पौराणिक", "auto_awesome"),
    DOCUMENTARY("Documentary", "डॉक्यूमेंट्री / यथार्थवादी", "history_edu"),
    ADVERTISEMENT("Advertisement", "विज्ञापन / कमर्शियल", "campaign"),
    SOCIAL_REEL("Social Media Reel", "रील्स / शॉर्ट्स (9:16)", "smart_display"),
    SCI_FI("Sci-Fi / Futuristic", "साइंस फिक्शन", "rocket_launch")
}

enum class AiVideoTool(val toolName: String, val bestFor: String, val promptFormatNotes: String) {
    UNIVERSAL("Universal Master", "All AI Tools", "Balanced cinematic tags with physical & human action tokens"),
    KLING_2("Kling 1.5 / 2.0", "Human Acting & Micro-Expressions", "Subject consistency tags, camera movement brackets, lighting Kelvin"),
    SORA("OpenAI Sora", "Physics, Complex Motion & Realism", "Narrative temporal physics, intricate character actions, atmospheric depth"),
    VEO_2("Google Veo 2", "Cinematic 4K & Lens Precision", "Arri Alexa, 35mm master shot, lens focal length, cinematic depth"),
    RUNWAY_GEN3("Runway Gen-3 Alpha", "Dynamic Camera & Style Control", "Camera motion tokens, cinematic lighting, photorealism cues"),
    LUMA_DREAM("Luma Dream Machine", "Smooth Camera Sweeps & Lighting", "Fluid cinematic motion, key lighting highlights, organic movement"),
    HAILUO_MINIMAX("Hailuo / MiniMax", "High-Fidelity Asian/Indian Faces", "Facial micro-expression clarity, authentic skin tones and natural eyes")
}

enum class AspectRatioFormat(val label: String, val ratioStr: String, val description: String) {
    RATIO_9_16("9:16 Vertical", "9:16", "Instagram Reels, YouTube Shorts, TikTok"),
    RATIO_16_9("16:9 Widescreen", "16:9", "YouTube, OTT Cinema, Standard TV"),
    RATIO_2_39_1("2.39:1 Anamorphic", "2.39:1", "Theatrical Cinemascope, Film Festival"),
    RATIO_1_1("1:1 Square", "1:1", "Social Feeds, Square Ad Spots")
}

enum class ShotSize(val code: String, val label: String, val focalGuide: String) {
    EXTREME_WIDE("EWS", "Extreme Wide Shot", "16-24mm - Establishes massive environment & insignificance"),
    WIDE("WS", "Wide / Master Shot", "24-35mm - Full location, spatial relationships & geography"),
    FULL("FS", "Full Shot", "35-50mm - Head-to-toe actor body language & stance"),
    MEDIUM("MS", "Medium Shot", "50mm - Waist-up, natural human dialogue perspective"),
    MEDIUM_CLOSE_UP("MCU", "Medium Close-Up", "50-85mm - Chest-up, dialogue clarity + emotional cues"),
    CLOSE_UP("CU", "Close-Up", "85mm prime - Face only, micro-expressions & eye movements"),
    EXTREME_CLOSE_UP("ECU", "Extreme Close-Up", "105mm macro - Eyes, trembling lips, hands, critical prop")
}

enum class CameraAngle(val label: String, val psychologicalEffect: String) {
    EYE_LEVEL("Eye-Level", "Neutral, intimate, human connection"),
    LOW_ANGLE("Low Angle", "Power, dominance, intimidation, grand stature"),
    HIGH_ANGLE("High Angle", "Vulnerability, helplessness, observation from above"),
    OVER_THE_SHOULDER("Over-the-Shoulder (OTS)", "Dialogue immersion, interpersonal perspective"),
    POV("Point-of-View (POV)", "Viewer enters character's subjective visual experience"),
    DUTCH_CANTED("Dutch / Canted Angle", "Unease, psychological disorientation, tension"),
    BIRDS_EYE("Bird's Eye / God's Eye", "Detached spatial overview, fate, geometry")
}

enum class CameraMovement(val label: String, val narrativePurpose: String) {
    STATIC_TRIPOD("Static Tripod", "Restraint, heavy tension, observational stillness"),
    SLOW_PUSH_IN("Slow Push-In (Creep-In)", "Intensifying emotion, realization, rising internal conflict"),
    PULL_OUT("Pull-Out (Reveal)", "Loneliness, realization of scale, shock or ending beat"),
    LATERAL_TRACK("Lateral Tracking", "Following action, pacing alongside character's journey"),
    HANDHELD_JITTER("Handheld (Subtle)", "Documentary realism, anxiety, visceral immediacy"),
    STEADICAM("Smooth Steadicam", "Elegance, fluid exploration, dreamlike immersion"),
    WHIP_PAN("Whip Pan", "Sudden surprise, comedic punchline, energetic transition")
}
