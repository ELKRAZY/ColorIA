package com.example.coloria.service

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AIColorService(private val apiKey: String) {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    suspend fun getColorName(hex: String): String = withContext(Dispatchers.IO) {
        val prompt = "What is the descriptive, artistic name of the color with hex code #$hex? Return ONLY the name, nothing else. Example: 'Midnight Blue' or 'Sunset Orange'."
        android.util.Log.d("AIColorService", "Sending prompt: $prompt")
        try {
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text?.trim()
            android.util.Log.d("AIColorService", "Received response: $responseText")
            return@withContext responseText ?: "Unknown Color"
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("AIColorService", "Error generating content", e)
            return@withContext "Error: ${e.message}"
        }
    }
}
