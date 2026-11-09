package com.example.ui.theme

object AppStrings {
    fun appTitle(isHindi: Boolean) = if (isHindi) "रियो • AI फिल्म डायरेक्टर" else "RIO • DIRECTOR AI"
    fun appSubtitle(isHindi: Boolean) = if (isHindi) "यूनिवर्सल AI फिल्म डायरेक्टर मास्टर सिस्टम" else "Universal Film Master Prompt System"

    // Navigation Labels
    fun navDeck(isHindi: Boolean) = if (isHindi) "मास्टर डेक" else "Master Deck"
    fun navStudio(isHindi: Boolean) = if (isHindi) "स्टूडियो" else "Studio"
    fun navCharacters(isHindi: Boolean) = if (isHindi) "कैरेक्टर" else "Characters"
    fun navShotLab(isHindi: Boolean) = if (isHindi) "शॉट लैब" else "Shot Lab"
    fun navCoach(isHindi: Boolean) = if (isHindi) "एक्टिंग कोच" else "Coach"
    fun navVault(isHindi: Boolean) = if (isHindi) "वॉल्ट" else "Vault"

    // Actions
    fun directFilm(isHindi: Boolean) = if (isHindi) "मेरी फिल्म डायरेक्ट करो (10 चरण)" else "DIRECT MY FILM (EXECUTE 10 PHASES)"
    fun analyzeScript(isHindi: Boolean) = if (isHindi) "स्क्रिप्ट विश्लेषण (GEMINI API)" else "ANALYZE SCRIPT (GEMINI API)"
    fun copyPrompt(isHindi: Boolean) = if (isHindi) "AI वीडियो प्रॉम्प्ट कॉपी करें" else "Copy AI Video Prompt"
    fun framesView(isHindi: Boolean) = if (isHindi) "स्टोरीबोर्ड" else "Frames"
    fun tableView(isHindi: Boolean) = if (isHindi) "टेबल" else "Table"
    fun listenVoice(isHindi: Boolean) = if (isHindi) "आवाज़ सुनें" else "Listen Voice"
    fun stopVoice(isHindi: Boolean) = if (isHindi) "रोकें" else "Stop"
    fun testVoice(isHindi: Boolean) = if (isHindi) "टेस्ट वॉयस" else "Test Voice"
    fun timelineTitle(isHindi: Boolean) = if (isHindi) "सीन टाइमलाइन व बीट सीक्वेंसर" else "MULTI-SCENE TIMELINE & BEATS"
    fun toolCalculator(isHindi: Boolean) = if (isHindi) "AI टूल पैरामीटर कैलकुलेटर" else "AI Tool Parameter Calculator"
}
