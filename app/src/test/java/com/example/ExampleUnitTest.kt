package com.example

import com.example.domain.CuratedProductions
import com.example.domain.DirectorEngine
import com.example.models.AiVideoTool
import com.example.models.AspectRatioFormat
import com.example.models.ProjectInput
import com.example.models.VideoType
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testDefaultProductionIntegrity() {
        val prod = CuratedProductions.defaultProduction
        assertNotNull(prod)
        assertNotNull(prod.brief.title)
        assertTrue(prod.shotList.isNotEmpty())
        assertTrue(prod.characters.isNotEmpty())
        assertTrue(prod.qualityControl.actingScore >= 80)
    }

    @Test
    fun testDirectorEngineDynamicGeneration() {
        val input = ProjectInput(
            topicOrScript = "A high-stakes chess match between two rival grandmasters in a Soviet library.",
            videoType = VideoType.THRILLER,
            language = "Russian / English",
            targetAudience = "Festival Viewers",
            duration = "60s (Cinematic Scene)",
            format = AspectRatioFormat.RATIO_2_39_1,
            visualStyle = "Cold 35mm monochrome with high contrast",
            charactersHint = "Boris (50, stern), Mikhail (22, nervous genius)",
            locationHint = "Vintage wood-paneled Moscow library",
            targetAiTool = AiVideoTool.SORA
        )

        val prod = DirectorEngine.analyzeAndDirect(input)

        assertNotNull(prod)
        assertEquals(VideoType.THRILLER, prod.brief.genre)
        assertEquals(AspectRatioFormat.RATIO_2_39_1, prod.brief.format)
        assertTrue(prod.shotList.isNotEmpty())
        assertTrue(prod.characters.isNotEmpty())
        assertTrue(prod.actingDirectionSheet.isNotEmpty())

        // Verify Sora prompt styling
        val firstShot = prod.shotList.first()
        assertTrue(firstShot.aiVideoPrompt.contains("Sora", ignoreCase = true) || firstShot.aiVideoPrompt.contains("photorealistic", ignoreCase = true))

        // Verify Markdown export
        val markdown = DirectorEngine.exportToMarkdown(prod)
        assertTrue(markdown.contains("RIO AI FILM DIRECTOR"))
        assertTrue(markdown.contains("OUTPUT 1 — PROJECT BRIEF"))
        assertTrue(markdown.contains("OUTPUT 5 — DETAILED SHOT LIST"))
        assertTrue(markdown.contains("OUTPUT 8 — AI VIDEO GENERATION PROMPTS"))

        // Verify TSV export
        val tsv = DirectorEngine.exportToTsv(prod)
        assertTrue(tsv.startsWith("Shot ID\tScene ID\tStart\tEnd"))
    }

    @Test
    fun testScriptCharacterLimitAndWordCount() {
        val maxLimit = 100
        val scriptText = "EXT. MUMBAI BEACH - NIGHT. The sound of waves crashing against the rocks."
        assertTrue(scriptText.length <= maxLimit)

        val longScript = "A".repeat(150)
        val truncated = if (longScript.length > maxLimit) longScript.take(maxLimit) else longScript
        assertEquals(100, truncated.length)

        val words = scriptText.trim().split("\\s+".toRegex()).size
        assertEquals(13, words)
    }

    @Test
    fun testGeminiScriptAnalyzerFallback() = kotlinx.coroutines.test.runTest {
        val input = ProjectInput(
            topicOrScript = "A tense standoff in a neon-lit alleyway in Old Delhi.",
            videoType = VideoType.ACTION
        )
        val result = com.example.domain.GeminiScriptAnalyzer.analyzeWithGemini(input) {}
        assertTrue(result.isSuccess)
        val production = result.getOrNull()
        assertNotNull(production)
        assertTrue(production!!.shotList.isNotEmpty())
    }

    @Test
    fun testAtMentionRegexExtraction() {
        val sampleScript = """
            Scene 1:
            @Raju enters the house hurriedly.
            @Raju: Mohini, where is my food?
            @Mohini: First bring vegetables from the market!
            @Raju: Okay, I am going!
            @Bunty watches from the window.
        """.trimIndent()

        val mentionRegex = Regex("""@([A-Za-z0-9_]+)""")
        val matches = mentionRegex.findAll(sampleScript).map { it.groupValues[1] }.distinct().toList()

        assertEquals(3, matches.size)
        assertTrue(matches.contains("Raju"))
        assertTrue(matches.contains("Mohini"))
        assertTrue(matches.contains("Bunty"))
    }

    @Test
    fun testCharacterConsistencyPromptFormatting() {
        val charName = "Raju"
        val charId = "char_raju_01"
        val appearance = "26-year-old young Indian man, athletic build, brown eyes"
        val clothing = "Blue casual polo t-shirt with dark jeans"
        val hairstyle = "Short cropped textured black hair with clean side fade"

        val consistencyPrompt = "Character Reference ID: $charId, Name: $charName, $appearance, wearing $clothing, hairstyle: $hairstyle, consistent facial identity lock"

        assertTrue(consistencyPrompt.contains("char_raju_01"))
        assertTrue(consistencyPrompt.contains("consistent facial identity lock"))
        assertTrue(consistencyPrompt.contains("Short cropped textured black hair"))
    }

    @Test
    fun testDialogueTtsParsing() {
        val dialogueLine = "@Raju: Mohini, did you bring the camera equipment?"
        val charName = dialogueLine.substringBefore(":").trim().replace("@", "")
        val speechText = dialogueLine.substringAfter(":").trim()

        assertEquals("Raju", charName)
        assertEquals("Mohini, did you bring the camera equipment?", speechText)
    }

    @Test
    fun testSpeechToTextScriptAppending() {
        val currentScript = "Scene 1:\n@Raju enters the room."
        val spokenText = "@Mohini turns around in surprise."
        val maxLimit = 1000

        val merged = (currentScript.trimEnd() + " " + spokenText).take(maxLimit)
        assertTrue(merged.startsWith("Scene 1:"))
        assertTrue(merged.endsWith("@Mohini turns around in surprise."))
        assertTrue(merged.length <= maxLimit)
    }

    @Test
    fun testCursorAwareAtMentionAutocompleteReplacement() {
        val script = "Scene 1: @Ra is walking towards the door."
        val cursor = script.indexOf("@Ra") + 3
        val textBeforeCursor = script.take(cursor)
        val atIndex = textBeforeCursor.lastIndexOf('@')
        assertTrue(atIndex != -1)

        val queryAfterAt = textBeforeCursor.substring(atIndex + 1)
        assertEquals("Ra", queryAfterAt)

        val chosenCharacter = "Raju"
        val textBeforeAt = script.substring(0, atIndex)
        val textAfterCursor = script.substring(cursor)
        val replacement = "@$chosenCharacter "
        val result = textBeforeAt + replacement + textAfterCursor

        assertEquals("Scene 1: @Raju  is walking towards the door.", result)
    }
}


