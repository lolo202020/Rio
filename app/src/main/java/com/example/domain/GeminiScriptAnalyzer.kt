package com.example.domain

import android.util.Log
import com.example.models.*
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

object GeminiScriptAnalyzer {

    private const val TAG = "GeminiScriptAnalyzer"

    private fun logW(tag: String, msg: String) {
        try { Log.w(tag, msg) } catch (_: Throwable) { println("[$tag] WARN: $msg") }
    }

    private fun logE(tag: String, msg: String, tr: Throwable? = null) {
        try { Log.e(tag, msg, tr) } catch (_: Throwable) { println("[$tag] ERROR: $msg ${tr?.message}") }
    }

    private val generativeModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    suspend fun analyzeWithGemini(
        input: ProjectInput,
        onProgressUpdate: (String) -> Unit
    ): Result<FullFilmProduction> = withContext(Dispatchers.IO) {
        val baseProduction = DirectorEngine.analyzeAndDirect(input)

        try {
            onProgressUpdate("Calling Gemini API (gemini-3.8-flash): Evaluating psychological subtext & dramatic conflict...")

            val systemInstruction = """
                You are a World-Class AI Film Director, Screenwriter, Cinematographer (DOP), and Acting Coach.
                Analyze the following screenplay according to the Universal AI Film Director Master Prompt.
                Return a valid JSON object strictly adhering to this format:
                {
                  "title": "Evocative Film Title",
                  "logline": "Gripping 1-2 sentence logline highlighting internal stakes",
                  "corePremise": "Underlying thematic premise",
                  "subtext": "Psychological subtext (what characters speak vs what they secretly feel)",
                  "directorVision": "Director's cinematic vision statement on lighting, pacing and gaze"
                }
            """.trimIndent()

            val userPrompt = """
                Film Topic / Script: ${input.topicOrScript}
                Genre: ${input.videoType.label}
                Language: ${input.language}
                Aspect Ratio: ${input.format.ratioStr}
                Target AI Tool: ${input.targetAiTool.toolName}
            """.trimIndent()

            val combinedPrompt = "$systemInstruction\n\n$userPrompt\n\nProvide the analysis in raw JSON format."

            val response = withTimeout(60_000L) {
                generativeModel.generateContent(combinedPrompt)
            }

            val text = response.text
            if (text.isNullOrBlank()) {
                logW(TAG, "Gemini returned empty response. Using Director Engine.")
                return@withContext Result.success(baseProduction)
            }

            onProgressUpdate("Gemini 3.8 Flash response received! Formatting director dossier...")

            val cleanJsonStr = if (text.contains("```json")) {
                text.substringAfter("```json").substringBefore("```").trim()
            } else if (text.contains("```")) {
                text.substringAfter("```").substringBefore("```").trim()
            } else {
                text.trim()
            }

            val parsedObj = try {
                JSONObject(cleanJsonStr)
            } catch (e: Exception) {
                null
            }

            if (parsedObj != null) {
                val parsedTitle = parsedObj.optString("title").ifBlank { baseProduction.brief.title }
                val parsedLogline = parsedObj.optString("logline").ifBlank { baseProduction.brief.logline }
                val parsedSubtext = parsedObj.optString("subtext").ifBlank { baseProduction.scriptAnalysis.subtextAnalysis }
                val parsedPremise = parsedObj.optString("corePremise").ifBlank { baseProduction.brief.corePremise }
                val parsedVision = parsedObj.optString("directorVision").ifBlank {
                    "Directed via Gemini 3.8 Flash: Subtextual depth and micro-expressions prioritized."
                }

                val updatedBrief = baseProduction.brief.copy(
                    title = parsedTitle,
                    logline = parsedLogline,
                    corePremise = parsedPremise,
                    directorVisionStatement = parsedVision
                )

                val updatedScriptAnalysis = baseProduction.scriptAnalysis.copy(
                    subtextAnalysis = parsedSubtext
                )

                Result.success(
                    baseProduction.copy(
                        brief = updatedBrief,
                        scriptAnalysis = updatedScriptAnalysis
                    )
                )
            } else {
                val enrichedBrief = baseProduction.brief.copy(
                    directorVisionStatement = "Gemini Director Analysis: ${text.take(300)}..."
                )
                Result.success(baseProduction.copy(brief = enrichedBrief))
            }

        } catch (e: TimeoutCancellationException) {
            logE(TAG, "Gemini generation timed out", e)
            onProgressUpdate("Gemini request timed out. Synthesized with Universal Director Engine.")
            Result.success(baseProduction)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logE(TAG, "Gemini API inference error: ${e.message}", e)
            onProgressUpdate("Processing with Universal Director Engine...")
            Result.success(baseProduction)
        }
    }
}
