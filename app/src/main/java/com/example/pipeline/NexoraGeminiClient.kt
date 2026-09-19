package com.example.pipeline

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AspectRatio
import com.example.data.model.CharacterEntity
import com.example.data.model.Language
import com.example.data.model.SceneEntity
import com.example.data.model.VideoStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object NexoraGeminiClient {
    private const val TAG = "NexoraGeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun getEffectiveApiKey(customKey: String?): String? {
        val trimmedCustom = customKey?.trim()
        if (!trimmedCustom.isNullOrEmpty()) {
            return trimmedCustom
        }
        val buildConfigKey = BuildConfig.GEMINI_API_KEY.trim()
        if (buildConfigKey.isNotEmpty() && buildConfigKey != "MY_GEMINI_API_KEY") {
            return buildConfigKey
        }
        return null
    }

    fun isConfigured(customKey: String?): Boolean {
        return getEffectiveApiKey(customKey) != null
    }

    suspend fun generateStoryAnalysis(
        apiKey: String,
        prompt: String,
        topic: String,
        durationMinutes: Int,
        style: String,
        language: String,
        targetScenesCount: Int
    ): StoryAnalysisResult = withContext(Dispatchers.IO) {
        val systemPrompt = """
            You are the master narrative AI for NEXORA Video AI.
            Analyze the user's story or prompt and return a valid JSON object matching the exact schema below.
            Target scenes count is approximately $targetScenesCount scenes for a $durationMinutes minute video.
            Style: $style
            Language: $language
            
            JSON schema:
            {
              "expandedTitle": "string",
              "storySummary": "string",
              "genre": "string",
              "mood": "string",
              "timePeriod": "string",
              "primaryLocation": "string",
              "keyObjects": ["string", "string"],
              "musicRequirement": "string",
              "characterPrototypes": [
                {
                  "name": "string",
                  "role": "string",
                  "age": "string",
                  "voiceType": "string",
                  "visualKeyword": "string"
                }
              ],
              "actBreakdown": ["Act 1: ...", "Act 2: ...", "Act 3: ..."]
            }
            Respond with ONLY the JSON object, without markdown quotes or backticks.
        """.trimIndent()

        val userMessage = "Story Topic: $topic\nUser Prompt / Script: $prompt"

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "$systemPrompt\n\n$userMessage"))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            })
        }

        val url = "$BASE_URL/gemini-3.5-flash:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw RuntimeException("Empty response from Gemini API")

        if (!response.isSuccessful) {
            Log.e(TAG, "Gemini API error: ${response.code} $responseBody")
            throw RuntimeException("Gemini API error HTTP ${response.code}: $responseBody")
        }

        val parsed = JSONObject(responseBody)
        val candidateText = parsed.optJSONArray("candidates")
            ?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text")
            ?: throw RuntimeException("No text in candidate response")

        val cleanJson = candidateText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val analysisJson = JSONObject(cleanJson)

        val charactersList = mutableListOf<CharacterPrototype>()
        val charArray = analysisJson.optJSONArray("characterPrototypes")
        if (charArray != null) {
            for (i in 0 until charArray.length()) {
                val c = charArray.getJSONObject(i)
                charactersList.add(
                    CharacterPrototype(
                        name = c.optString("name", "Protagonist"),
                        role = c.optString("role", "Lead"),
                        age = c.optString("age", "30"),
                        voiceType = c.optString("voiceType", "Confident"),
                        visualKeyword = c.optString("visualKeyword", "Cinematic attire")
                    )
                )
            }
        }

        val keyObjectsList = mutableListOf<String>()
        val keyObjArray = analysisJson.optJSONArray("keyObjects")
        if (keyObjArray != null) {
            for (i in 0 until keyObjArray.length()) {
                keyObjectsList.add(keyObjArray.getString(i))
            }
        }

        val actsList = mutableListOf<String>()
        val actsArray = analysisJson.optJSONArray("actBreakdown")
        if (actsArray != null) {
            for (i in 0 until actsArray.length()) {
                actsList.add(actsArray.getString(i))
            }
        }

        StoryAnalysisResult(
            expandedTitle = analysisJson.optString("expandedTitle", topic.ifBlank { "Cinematic AI Odyssey" }),
            storySummary = analysisJson.optString("storySummary", prompt),
            genre = analysisJson.optString("genre", "Cinematic Drama"),
            mood = analysisJson.optString("mood", "Atmospheric and intense"),
            timePeriod = analysisJson.optString("timePeriod", "Present Day"),
            primaryLocation = analysisJson.optString("primaryLocation", "Metropolis"),
            keyObjects = if (keyObjectsList.isEmpty()) listOf("Ancient Beacon") else keyObjectsList,
            musicRequirement = analysisJson.optString("musicRequirement", "Cinematic orchestral score"),
            targetScenesCount = targetScenesCount,
            calculatedSceneDuration = 9,
            characterPrototypes = if (charactersList.isEmpty()) listOf(
                CharacterPrototype("Alex Vance", "Protagonist", "32", "Deep tenor", "Leather traveler coat")
            ) else charactersList,
            actBreakdown = if (actsList.isEmpty()) listOf("Act 1: Beginning", "Act 2: Conflict", "Act 3: Resolution") else actsList
        )
    }

    suspend fun generateScriptScenes(
        apiKey: String,
        projectId: Long,
        analysis: StoryAnalysisResult,
        characters: List<CharacterEntity>,
        style: VideoStyle,
        language: Language,
        aspectRatio: AspectRatio
    ): List<SceneEntity> = withContext(Dispatchers.IO) {
        val totalScenes = analysis.targetScenesCount.coerceAtLeast(5)
        val prompt = """
            You are the cinematic director for NEXORA Video AI.
            Generate a screenplay scene breakdown for the story: "${analysis.expandedTitle}".
            Genre: ${analysis.genre}, Mood: ${analysis.mood}, Language: ${language.displayName}.
            Character: ${characters.firstOrNull()?.name ?: "Protagonist"}.
            Total scenes required: $totalScenes scenes.
            
            Return a JSON array of scene objects:
            [
              {
                "sceneIndex": 1,
                "durationSeconds": 9,
                "visualDescription": "Detailed visual description for video generation",
                "characterActions": "Actions happening in the scene",
                "cameraMovement": "ZOOM_IN",
                "environment": "Setting environment",
                "narrationText": "Narration voiceover in ${language.displayName}",
                "soundEffect": "Sound effect description",
                "backgroundMusicMood": "Music description"
              }
            ]
            Respond with ONLY the JSON array.
        """.trimIndent()

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            })
        }

        val url = "$BASE_URL/gemini-3.5-flash:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw RuntimeException("Empty response from Gemini API")
        if (!response.isSuccessful) {
            Log.e(TAG, "Gemini API script error: ${response.code} $responseBody")
            throw RuntimeException("Gemini API error HTTP ${response.code}: $responseBody")
        }

        val parsed = JSONObject(responseBody)
        val candidateText = parsed.optJSONArray("candidates")
            ?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text")
            ?: throw RuntimeException("No text in candidate response")

        val cleanJson = candidateText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val sceneArray = JSONArray(cleanJson)

        val resultScenes = mutableListOf<SceneEntity>()
        for (i in 0 until sceneArray.length()) {
            val item = sceneArray.getJSONObject(i)
            resultScenes.add(
                SceneEntity(
                    projectId = projectId,
                    sceneIndex = item.optInt("sceneIndex", i + 1),
                    durationSeconds = item.optInt("durationSeconds", 9),
                    visualDescription = item.optString("visualDescription", "Cinematic landscape vista with dramatic lighting"),
                    characterActions = item.optString("characterActions", "Character moves forward with determination"),
                    cameraMovement = item.optString("cameraMovement", "ZOOM_IN"),
                    environment = item.optString("environment", analysis.primaryLocation),
                    dialogueSpeaker = null,
                    dialogueText = null,
                    narrationText = item.optString("narrationText", "The journey continues through the unfolding narrative."),
                    soundEffect = item.optString("soundEffect", "Subtle wind and footsteps"),
                    backgroundMusicMood = item.optString("backgroundMusicMood", analysis.musicRequirement),
                    transition = "CROSSFADE",
                    visualSeed = projectId * 100L + (i + 1)
                )
            )
        }
        resultScenes
    }
}
