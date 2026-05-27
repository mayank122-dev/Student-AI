package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.database.FlashcardEntity
import com.example.data.database.PomodoroSessionEntity
import com.example.data.database.QuizHistoryEntity
import com.example.data.database.SynapseDao
import com.example.data.database.UserSettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SynapticRepository(private val synapseDao: SynapseDao) {

    // Check if settings are null and return default
    val userSettings: Flow<UserSettingsEntity> = synapseDao.getUserSettings().map { entity ->
        if (entity == null) {
            val defaultSettings = UserSettingsEntity()
            try {
                synapseDao.insertUserSettings(defaultSettings)
            } catch (e: Exception) {
                Log.e("SynapticRepository", "Error saving default settings: ${e.message}")
            }
            defaultSettings
        } else {
            entity
        }
    }.flowOn(Dispatchers.IO)

    val allFlashcards: Flow<List<FlashcardEntity>> = synapseDao.getAllFlashcards().flowOn(Dispatchers.IO)
    val quizHistory: Flow<List<QuizHistoryEntity>> = synapseDao.getQuizHistory().flowOn(Dispatchers.IO)
    val pomodoroSessions: Flow<List<PomodoroSessionEntity>> = synapseDao.getPomodoroSessions().flowOn(Dispatchers.IO)

    suspend fun saveUserSettings(settings: UserSettingsEntity) = withContext(Dispatchers.IO) {
        synapseDao.insertUserSettings(settings)
    }

    suspend fun addXp(amount: Int) = withContext(Dispatchers.IO) {
        try {
            val currentSettings = userSettings.first()
            val newXp = currentSettings.xp + amount
            val newLevel = (newXp / 200) + 1 // 200 XP per level
            val updated = currentSettings.copy(xp = newXp, level = newLevel)
            synapseDao.insertUserSettings(updated)
        } catch (e: Exception) {
            Log.e("SynapticRepository", "Error adding XP: ${e.message}")
        }
    }

    suspend fun updateXpAndStreak(xpEarned: Int, streakUpdate: Int? = null) = withContext(Dispatchers.IO) {
        try {
            val currentSettings = userSettings.first()
            val newXp = currentSettings.xp + xpEarned
            val newLevel = (newXp / 200) + 1
            val updated = currentSettings.copy(
                xp = newXp,
                level = newLevel,
                studyStreak = streakUpdate ?: currentSettings.studyStreak
            )
            synapseDao.insertUserSettings(updated)
        } catch (e: Exception) {
            Log.e("SynapticRepository", "Error updating XP and streak: ${e.message}")
        }
    }

    suspend fun addFlashcard(flashcard: FlashcardEntity) = withContext(Dispatchers.IO) {
        synapseDao.insertFlashcard(flashcard)
    }

    suspend fun deleteFlashcard(id: Int) = withContext(Dispatchers.IO) {
        synapseDao.deleteFlashcardById(id)
    }

    suspend fun addQuizRecord(record: QuizHistoryEntity) = withContext(Dispatchers.IO) {
        synapseDao.insertQuizRecord(record)
    }

    suspend fun addPomodoroSession(session: PomodoroSessionEntity) = withContext(Dispatchers.IO) {
        synapseDao.insertPomodoroSession(session)
    }

    suspend fun askGemini(
        prompt: String,
        style: String,
        language: String,
        modelName: String
    ): String = withContext(Dispatchers.IO) {
        // Retrieve key from BuildConfig
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error: Gemini API Key is missing. Please configure GEMINI_API_KEY in the Secrets panel."
        }

        val systemInstructionText = when (style) {
            "Simple / Layman" -> "Explain the following scientific or academic topic to a 5-year-old child using extremely basic, easy words, making it colorful and clear."
            "Academic Spec" -> "Provide an academic, deeply technical, precise and highly authoritative explanation of the topic. Use scientific formulas, references, and rigorous definitions."
            "Meme Mode" -> {
                if (language == "Hinglish") {
                    "Bhai, read carefully! Explain the topic using hilarious Hinglish internet memes, Gen-Z slangs, sarcasm (bhot hard style), inside joke references, and dense emojis."
                } else if (language == "Hindi") {
                    "हंसमुख मीम शैली में समझाएं! इंटरनेट संस्कृति, व्यंग्य, चुटकुलों और लोकप्रिय हिंदी मीम्स का उपयोग करके विषय की व्याख्या करें।"
                } else {
                    "Explain the topic using hilarious internet memes, Gen-Z slangs, high sarcasm, gaming humor, funny metaphors, and heavy emojis."
                }
            }
            "Gamer Mode" -> "Explain the topic purely using gaming terminology (e.g. XP level ups, HP points, item crafting, dynamic boss encounters, speedrunning, and stats)."
            "Analogical" -> "Explain the topic which relies entirely on deep, creative, highly illustrative real-world analogies (e.g., comparing a cell membrane to an exclusive VIP nightclub bouncer)."
            else -> "Explain the topic beautifully."
        }

        val langInstruction = " Ensure the explanation matches the target output language precisely: $language." +
                " Do not output markdown code blocks unless writing code. Format nicely with paragraph spacing."

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText + langInstruction)))
        )

        try {
            val response = RetrofitClient.service.generateContent(modelName, apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No response content received from Synaptic Core."
        } catch (e: Exception) {
            Log.e("SynapticRepository", "Gemini API Error: ${e.message}", e)
            "Error contacting Synaptic Core: ${e.localizedMessage ?: "Unknown connection anomaly."}\nEnsure internet is enabled and your API Key is validated."
        }
    }
}
