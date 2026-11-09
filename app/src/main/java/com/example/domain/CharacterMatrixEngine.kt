package com.example.domain

data class CharacterStateTag(
    val characterName: String,
    val mood: String? = null,
    val wardrobe: String? = null
)

object CharacterMatrixEngine {

    val availableMoods = listOf(
        "Angry" to "Furrowed brow, clutched jaw, fiery confrontational eyes",
        "Emotional" to "Tearful eyes, trembling lips, raw vulnerability, repressed sorrow",
        "Tense" to "Dilated pupils, shallow breathing, alert nervous posture",
        "Joyful" to "Radiant grin, crinkling warm eyes, relaxed joyful demeanor",
        "Conflicted" to "Hesitant downward gaze, conflicted brow, unspoken guilt",
        "Calm" to "Steady composed gaze, gentle peaceful breathing"
    )

    val availableWardrobes = listOf(
        "Jacket" to "Dark olive bomber jacket over cotton tee",
        "Kurta" to "Traditional cream handloom kurta with textured embroidery",
        "Casual" to "Everyday casual polo t-shirt with dark denim jeans",
        "Formal" to "Crisp cotton formal button-up shirt with rolled sleeves",
        "Shawl" to "Warm weathered woolen Kashmiri shawl wrapped around shoulders",
        "Saree" to "Elegant crimson cotton saree with subtle golden border"
    )

    /**
     * Parses tags like @Raju[Angry, Jacket] or @Mohini[Emotional] or @Kabir[Shawl]
     */
    fun parseStateTags(scriptText: String): List<CharacterStateTag> {
        val regex = Regex("""@([A-Za-z0-9_]+)(?:\[([^\]]+)\])?""")
        return regex.findAll(scriptText).map { match ->
            val name = match.groupValues[1]
            val params = match.groupValues.getOrNull(2)?.split(",")?.map { it.trim() } ?: emptyList()

            var mood: String? = null
            var wardrobe: String? = null

            for (p in params) {
                val matchedMood = availableMoods.find { it.first.equals(p, ignoreCase = true) }
                val matchedWardrobe = availableWardrobes.find { it.first.equals(p, ignoreCase = true) }
                if (matchedMood != null) mood = matchedMood.first
                else if (matchedWardrobe != null) wardrobe = matchedWardrobe.first
                else {
                    // Custom fallback
                    if (mood == null) mood = p
                    else wardrobe = p
                }
            }

            CharacterStateTag(characterName = name, mood = mood, wardrobe = wardrobe)
        }.distinct().toList()
    }

    /**
     * Injects wardrobe & expression modifier into the shot prompt
     */
    fun injectMatrixIntoPrompt(basePrompt: String, stateTags: List<CharacterStateTag>): String {
        if (stateTags.isEmpty()) return basePrompt
        var enriched = basePrompt
        for (tag in stateTags) {
            val moodDesc = availableMoods.find { it.first.equals(tag.mood, ignoreCase = true) }?.second ?: tag.mood
            val wardrobeDesc = availableWardrobes.find { it.first.equals(tag.wardrobe, ignoreCase = true) }?.second ?: tag.wardrobe

            val modifiers = listOfNotNull(
                moodDesc?.let { "facial expression: $it" },
                wardrobeDesc?.let { "wearing $it" }
            )
            if (modifiers.isNotEmpty()) {
                enriched += ", (${tag.characterName} state: ${modifiers.joinToString(", ")})"
            }
        }
        return enriched
    }
}
