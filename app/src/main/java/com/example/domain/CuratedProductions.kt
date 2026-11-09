package com.example.domain

import com.example.models.*

object CuratedProductions {
    val defaultProduction = DirectorEngine.analyzeAndDirect(
        ProjectInput(
            topicOrScript = "A filmmaker entering their creative studio to direct a brand new masterpiece.",
            videoType = VideoType.DRAMA,
            language = "Hindi / English",
            targetAudience = "Global Audiences",
            duration = "60s (Cinematic Scene)",
            format = AspectRatioFormat.RATIO_16_9,
            visualStyle = "Cinematic 35mm Arri Alexa, warm natural lighting",
            targetAiTool = AiVideoTool.KLING_2
        )
    )

    val allCurated = listOf(defaultProduction)
}
