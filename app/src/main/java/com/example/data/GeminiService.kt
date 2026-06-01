package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateMinecraftBuild(prompt: String): MinecraftBuild? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is missing or is the placeholder value.")
            return@withContext null
        }

        val systemPrompt = """
            You are an expert Minecraft Architect. Produce highly realistic, step-by-step building blueprints and guides.
            Return a JSON object matching this schema:
            {
               "title": "Beautiful English Countryside Cottage",
               "description": "An elegant, traditional brick house featuring oak columns, double storage roof, and clear bay windows.",
               "difficulty": "Easy" or "Medium" or "Hard",
               "estimatedTime": "30 Mins" or "2 Hours" etc.,
               "materials": "Oak Planks: 64, Glass Panes: 10, Cobblestone: 32, Brick: 40",
               "dimensions": "Width x Depth x Height (e.g. 6 x 6 x 4)",
               "instructions": "1. Build foundation... \n2. Raise logs... \n3. Add glass...",
               "tips": "Spruce plants placed near windows add excellent garden decoration.",
               "layers": [
                  [
                    ["C", "C", "C", "C", "C", "C"],
                    ["C", "P", "P", "P", "P", "C"],
                    ["C", "P", "P", "P", "P", "C"],
                    ["C", "P", "P", "P", "P", "C"],
                    ["C", "P", "P", "P", "P", "C"],
                    ["C", "C", "C", "C", "C", "C"]
                  ],
                  [
                    ["O", "P", "P", "P", "P", "O"],
                    ["P", ".", ".", ".", ".", "P"],
                    ["P", ".", ".", ".", ".", "P"],
                    ["P", ".", ".", ".", ".", "P"],
                    ["P", ".", ".", ".", ".", "P"],
                    ["O", "P", "P", "P", "P", "O"]
                  ]
               ]
            }
            Use ONLY these letters for cell grids in the 3D layers array:
            - "P": Planks (Wood beige block)
            - "C": Cobblestone (Rough grey stone block)
            - "S": Stone (Smooth clean grey block)
            - "O": Oak Logs (Brown log corner support)
            - "G": Glass (Light neon cyan pane)
            - "D": Dirt / Moss (Saddle brown block)
            - "W": Water (Bright neon blue block)
            - "L": Lava (Vibrant orange hazard block)
            - "R": Redstone Dust (Bright red dot block)
            - "T": Redstone Torch (Slender redstoning light source)
            - "B": Redstone Block (Luminous primary redstone power block)
            - "I": Iron Block / Anvil (Clean metal block)
            - "Y": Obsidian (Dark deep violet obsidian shield)
            - ".": Air / Empty space (Rendered as empty air cell)
            
            Keep grid sizes tight (typically 5x5x3, 6x6x4, up to 10x10x6 depending on building complexity so it is readable and builds fast).
            The 'layers' property must be a 3D grid layout represented as layer list Y (0=foundation to top), row list X, items list Z.
            Ensure the generated layers JSON matches the dimensions you provided (width rows, depth cells). 
            Generate absolutely valid, single-line clean JSON returned inside the candidate text without extra characters. Do NOT wrap inside markdown blocks like ```json ... ```! Provide raw JSON only.
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("parts", JSONArray().put(
                        JSONObject().apply {
                            put("text", "Generate Minecraft building guide for: $prompt")
                        }
                    ))
                }
            ))
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(
                    JSONObject().apply {
                        put("text", systemPrompt)
                    }
                ))
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            })
        }

        val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())
        val requestUrl = "$BASE_URL?key=$apiKey"

        val request = Request.Builder()
            .url(requestUrl)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed: code=${response.code} message=${response.message}")
                    return@withContext null
                }

                val responseBody = response.body?.string() ?: return@withContext null
                Log.d(TAG, "Gemini response: $responseBody")

                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    Log.e(TAG, "No candidates returned")
                    return@withContext null
                }

                val content = candidates.getJSONObject(0).optJSONObject("content") ?: return@withContext null
                val parts = content.optJSONArray("parts") ?: return@withContext null
                val text = parts.getJSONObject(0).optString("text")

                // Parse the inner JSON returned by Gemini
                val innerJson = JSONObject(text.trim())
                val title = innerJson.optString("title", "AI Minecraft Build")
                val description = innerJson.optString("description", "A custom design generated on demand.")
                val difficulty = innerJson.optString("difficulty", "Medium")
                val estimatedTime = innerJson.optString("estimatedTime", "30 Mins")
                val materials = innerJson.optString("materials", "Cobblestone: 64")
                val dimensions = innerJson.optString("dimensions", "6 x 6 x 4")
                val instructions = innerJson.optString("instructions", "Build brick by brick.")
                val tips = innerJson.optString("tips", "Enjoy your custom build!")

                // Clean and store layers JSON string
                val layersArr = innerJson.optJSONArray("layers")
                val layersJsonStr = layersArr?.toString() ?: "[]"

                return@withContext MinecraftBuild(
                    title = title,
                    description = description,
                    category = "Houses", // Default category or match dynamically
                    difficulty = difficulty,
                    estimatedTime = estimatedTime,
                    materials = materials,
                    dimensions = dimensions,
                    instructions = instructions,
                    tips = tips,
                    layersJson = layersJsonStr,
                    creatorName = "AI Builder",
                    creatorEmail = "ai@minecraft.net",
                    isFavorite = false,
                    isShared = false,
                    likesCount = 0,
                    userLiked = false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API: ${e.message}", e)
            return@withContext null
        }
    }
}
