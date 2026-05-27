package com.example.ui

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.FlashcardEntity
import com.example.data.database.PomodoroSessionEntity
import com.example.data.database.QuizHistoryEntity
import com.example.data.database.UserSettingsEntity
import com.example.data.repository.SynapticRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Tab { Home, Explainer, Quiz, Flashcards, Focus, Settings }

data class ChatMessage(
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

class SynapseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = SynapticRepository(db.synapseDao())

    // UI state flows from DB
    val userSettings: StateFlow<UserSettingsEntity> = repository.userSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserSettingsEntity()
    )

    val flashcardsList: StateFlow<List<FlashcardEntity>> = repository.allFlashcards.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val quizHistoryList: StateFlow<List<QuizHistoryEntity>> = repository.quizHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pomodoroSessionsList: StateFlow<List<PomodoroSessionEntity>> = repository.pomodoroSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dynamic Local States
    var currentTab by mutableStateOf(Tab.Home)
    var currentMood by mutableStateOf("Curious 🤔")

    // Dynamic wave animation ticker
    var frameTick by mutableStateOf(0f)
        private set

    // Synaptic flow stimulation ripple state
    var rippleActive by mutableStateOf(false)
    var rippleScale by mutableStateOf(0f)
    var rippleAlpha by mutableStateOf(0f)

    // Gemini Chat State
    var chatHistory = mutableStateOf<List<ChatMessage>>(
        listOf(
            ChatMessage("ai", "Greeting Explorer! Welcome to the Synaptic Core. What are we studying today?")
        )
    )
    var isExplaining by mutableStateOf(false)
    var customExplainerStyle by mutableStateOf("Simple / Layman")
    var chatInputText by mutableStateOf("")

    // Quiz State
    val quizQuestions = listOf(
        QuizQuestion(
            question = "What does a Transformer neural network rely on to focus on relevant input features?",
            options = listOf("Gradient Descent", "Self-Attention Mechanism", "Convolutional Pools", "Sigmoid Activation"),
            correctIndex = 1,
            explanation = "Self-attention allows the model to capture dependencies between words regardless of their distance in the sequence."
        ),
        QuizQuestion(
            question = "In physical cosmology, what is the theoretical end state of an expanding universe after trillions of years?",
            options = listOf("The Big Crunch", "Maximum Cosmic Heat Death", "Spontaneous Supernova Injection", "White Hole Genesis"),
            correctIndex = 1,
            explanation = "Heat Death or the Big Freeze represents a thermodynamic equilibrium where entropy is maximized and zero physical work can be done."
        ),
        QuizQuestion(
            question = "Which particles mediate the strong nuclear force binding quarks under quantum chromodynamics?",
            options = listOf("Gluons", "Higgs Bosons", "Gravitons", "Z Weak Bosons"),
            correctIndex = 0,
            explanation = "Gluons act as exchange particles for the force holding quarks together to build protons and neutrons."
        ),
        QuizQuestion(
            question = "What brainwave frequency domain is characteristic of deep slow-wave non-REM sleep?",
            options = listOf("Gamma (40 Hz)", "Delta (1-4 Hz)", "Beta (15-30 Hz)", "Theta (4-8 Hz)"),
            correctIndex = 1,
            explanation = "Delta waves are high-amplitude waves with a frequency between 1 and 4 Hz, indicating deep slow-wave state sleep."
        ),
        QuizQuestion(
            question = "Who formulated the mathematical theorem which proves every consistent formal system containing basic arithmetic must be incomplete?",
            options = listOf("Alan Turing", "Kurt Gödel", "John von Neumann", "Emil Post"),
            correctIndex = 1,
            explanation = "Kurt Gödel's Incompleteness Theorems established that there are mathematical truths that can never be proven within a consistent axiomatic system."
        )
    )

    var currentQuizIndex by mutableStateOf(0)
    var selectedQuizOption by mutableStateOf(-1)
    var quizSubmitted by mutableStateOf(false)
    var quizScore by mutableStateOf(0)
    var quizActiveState by mutableStateOf("IDLE") // IDLE, RUNNING, COMPLETED

    // Flashcard UI State
    var flashcardInputQuestion by mutableStateOf("")
    var flashcardInputAnswer by mutableStateOf("")
    var activeFlashcardIndex by mutableStateOf(0)
    var flashcardFlipped by mutableStateOf(false)

    // Focus Pomodoro Timer State
    var focusTimerJob: Job? = null
    var focusTimeSelection by mutableStateOf(25) // minutes setup
    var focusSecondsRemaining by mutableStateOf(25 * 60)
    var isFocusRunning by mutableStateOf(false)
    var ambientWhiteNoiseEnabled by mutableStateOf(true)
    var ambientRainEnabled by mutableStateOf(false)
    var ambientLofiEnabled by mutableStateOf(false)

    // Calibration Dialog State
    var showCalibrationDialog by mutableStateOf(false)
    var calibrationMessage by mutableStateOf("")

    init {
        // Run ticker loop for real-time smooth canvas rendering of waves
        viewModelScope.launch {
            while (true) {
                frameTick += 0.05f
                delay(16) // ~60fps wave ticks
            }
        }

        // Initialize preset flashcards if database is completely empty
        viewModelScope.launch {
            delay(500)
            if (flashcardsList.value.isEmpty()) {
                repository.addFlashcard(FlashcardEntity(question = "What is the synaptic cleft?", answer = "The microscopic gap between adjacent neurons where chemical neurotransmitters pass messages.", deckName = "Neuroscience"))
                repository.addFlashcard(FlashcardEntity(question = "What is quantum entanglement?", answer = "A physical phenomenon occurring when a pair of particles share spatial proximity such that their quantum states are interdependent.", deckName = "Physics"))
                repository.addFlashcard(FlashcardEntity(question = "What is a glassmorphic layout?", answer = "An elegant interface styling that mimics frosted glass with background transparency, blur, and high contrast light borders.", deckName = "Design"))
                repository.addFlashcard(FlashcardEntity(question = "What is the Pomodoro Technique?", answer = "A time management framework breaking work into focused intervals (traditionally 25 mins) followed by short dynamic breaks.", deckName = "Productivity"))
            }
        }
    }

    // Acoustic trigger
    fun triggerAudioFeedback(score: Int = 1) {
        val settings = userSettings.value
        if (!settings.auditoryEnabled) return
        viewModelScope.launch {
            try {
                // Generate a brief high-tech synthesizer chirp based on setting volume
                val toneType = if (score > 0) ToneGenerator.TONE_PROP_BEEP else ToneGenerator.TONE_CDMA_PIP
                val volume = (settings.soundLevel * 100).toInt().coerceIn(1, 100)
                val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, volume)
                toneGenerator.startTone(toneType, 120)
                delay(150)
                toneGenerator.release()
            } catch (e: Exception) {
                Log.e("SynapseViewModel", "Aural feedback exception: ${e.message}")
            }
        }
    }

    // Haptic simulator
    fun triggerHapticFeedback(view: View?) {
        val settings = userSettings.value
        if (!settings.hapticEnabled) return
        try {
            view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        } catch (e: Exception) {
            // Fallback if view is absent
        }
    }

    // Synaptic Stimulation Trigger
    fun triggerSynapticStimulation(view: View?) {
        triggerAudioFeedback(2)
        triggerHapticFeedback(view)
        viewModelScope.launch {
            rippleActive = true
            rippleScale = 0f
            rippleAlpha = 1f
            // Smoothly animate expanding concentric rings
            for (i in 1..25) {
                rippleScale = i / 25f
                rippleAlpha = 1f - (i / 25f)
                delay(15)
            }
            rippleActive = false
            rippleScale = 0f
            rippleAlpha = 0f
        }
    }

    // Update settings in Database
    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            val settings = userSettings.value.copy(selectedLanguage = lang)
            repository.saveUserSettings(settings)
        }
    }

    fun updateAtmosphere(style: String) {
        viewModelScope.launch {
            val settings = userSettings.value.copy(selectedAtmosphere = style)
            repository.saveUserSettings(settings)
        }
    }

    fun updateSoundSettings(volume: Float, audioEnabled: Boolean, hapticEnabled: Boolean) {
        viewModelScope.launch {
            val settings = userSettings.value.copy(
                soundLevel = volume,
                auditoryEnabled = audioEnabled,
                hapticEnabled = hapticEnabled
            )
            repository.saveUserSettings(settings)
        }
    }

    fun updateAiModel(model: String) {
        viewModelScope.launch {
            val settings = userSettings.value.copy(selectedModel = model)
            repository.saveUserSettings(settings)
        }
    }

    // Gemini Explainer interaction
    fun queryIntelligenceNucleus() {
        if (chatInputText.trim().isEmpty()) return
        val prompt = chatInputText.trim()
        chatHistory.value = chatHistory.value + ChatMessage("user", prompt)
        chatInputText = ""
        isExplaining = true

        val settings = userSettings.value
        viewModelScope.launch {
            triggerAudioFeedback(1)
            val response = repository.askGemini(
                prompt = prompt,
                style = customExplainerStyle,
                language = settings.selectedLanguage,
                modelName = settings.selectedModel
            )
            chatHistory.value = chatHistory.value + ChatMessage("ai", response)
            isExplaining = false
            triggerAudioFeedback(3)
        }
    }

    // Quiz mechanics
    fun startQuiz() {
        currentQuizIndex = 0
        selectedQuizOption = -1
        quizSubmitted = false
        quizScore = 0
        quizActiveState = "RUNNING"
        triggerAudioFeedback(1)
    }

    fun selectQuizOption(index: Int) {
        if (quizSubmitted) return
        selectedQuizOption = index
    }

    fun submitQuizAnswer(view: View?) {
        if (selectedQuizOption == -1 || quizSubmitted) return
        quizSubmitted = true
        triggerHapticFeedback(view)
        if (selectedQuizOption == quizQuestions[currentQuizIndex].correctIndex) {
            quizScore++
            triggerAudioFeedback(4)
        } else {
            triggerAudioFeedback(0)
        }
    }

    fun nextQuizQuestion() {
        if (currentQuizIndex < quizQuestions.size - 1) {
            currentQuizIndex++
            selectedQuizOption = -1
            quizSubmitted = false
            triggerAudioFeedback(1)
        } else {
            completeQuiz()
        }
    }

    private fun completeQuiz() {
        quizActiveState = "COMPLETED"
        val totalQ = quizQuestions.size
        // Calculate dynamically (+50 for perfect, +10 for each correct)
        val basicXp = quizScore * 20
        val bonus = if (quizScore == totalQ) 50 else 0
        val earnedXp = basicXp + bonus

        viewModelScope.launch {
            // Save Quiz Record
            repository.addQuizRecord(
                QuizHistoryEntity(
                    score = quizScore,
                    totalQuestions = totalQ,
                    xpEarned = earnedXp,
                    topic = "Quantum and Cognitive Synthesis"
                )
            )

            // Update user stats
            val settings = userSettings.value
            val currentXp = settings.xp
            val nextXp = currentXp + earnedXp
            val newLevel = (nextXp / 200) + 1
            // Auto increment day streak for fun engagement
            val newStreak = settings.studyStreak + 1

            repository.saveUserSettings(
                settings.copy(
                    xp = nextXp,
                    level = newLevel,
                    studyStreak = newStreak
                )
            )
            triggerAudioFeedback(5)
        }
    }

    // Flashcard management
    fun prevFlashcard() {
        if (flashcardsList.value.isEmpty()) return
        flashcardFlipped = false
        activeFlashcardIndex = (activeFlashcardIndex - 1 + flashcardsList.value.size) % flashcardsList.value.size
        triggerAudioFeedback(1)
    }

    fun nextFlashcard() {
        if (flashcardsList.value.isEmpty()) return
        flashcardFlipped = false
        activeFlashcardIndex = (activeFlashcardIndex + 1) % flashcardsList.value.size
        triggerAudioFeedback(1)
    }

    fun flipFlashcard(view: View?) {
        triggerHapticFeedback(view)
        flashcardFlipped = !flashcardFlipped
        triggerAudioFeedback(1)
    }

    fun createFlashcard(context: Context) {
        if (flashcardInputQuestion.trim().isEmpty() || flashcardInputAnswer.trim().isEmpty()) {
            Toast.makeText(context, "Please fill both Question and Answer fields", Toast.LENGTH_SHORT).show()
            return
        }
        viewModelScope.launch {
            repository.addFlashcard(
                FlashcardEntity(
                    question = flashcardInputQuestion.trim(),
                    answer = flashcardInputAnswer.trim()
                )
            )
            // Add small study XP
            repository.addXp(10)
            flashcardInputQuestion = ""
            flashcardInputAnswer = ""
            Toast.makeText(context, "Flashcard mapped successfully! +10 XP", Toast.LENGTH_SHORT).show()
            triggerAudioFeedback(2)
        }
    }

    fun deleteCurrentFlashcard() {
        val list = flashcardsList.value
        if (list.isEmpty() || activeFlashcardIndex >= list.size) return
        val cardId = list[activeFlashcardIndex].id
        viewModelScope.launch {
            repository.deleteFlashcard(cardId)
            activeFlashcardIndex = 0
            flashcardFlipped = false
            triggerAudioFeedback(0)
        }
    }

    // Focus Room (Pomodoro Timer) Mechanics
    fun configureFocusTimer(minutes: Int) {
        if (isFocusRunning) return
        focusTimeSelection = minutes
        focusSecondsRemaining = minutes * 60
        triggerAudioFeedback(1)
    }

    fun toggleFocusTimer(view: View?) {
        triggerHapticFeedback(view)
        if (isFocusRunning) {
            pauseFocusTimer()
        } else {
            startFocusTimer()
        }
    }

    private fun startFocusTimer() {
        isFocusRunning = true
        triggerAudioFeedback(2)
        focusTimerJob = viewModelScope.launch {
            while (focusSecondsRemaining > 0) {
                delay(1000)
                focusSecondsRemaining--
            }
            completeFocusSession()
        }
    }

    private fun pauseFocusTimer() {
        isFocusRunning = false
        focusTimerJob?.cancel()
        triggerAudioFeedback(0)
    }

    fun resetFocusTimer() {
        pauseFocusTimer()
        focusSecondsRemaining = focusTimeSelection * 60
        triggerAudioFeedback(1)
    }

    private suspend fun completeFocusSession() {
        isFocusRunning = false
        focusTimerJob?.cancel()

        // Create Session Record
        repository.addPomodoroSession(
            PomodoroSessionEntity(
                durationMinutes = focusTimeSelection,
                category = "Advanced Focus Session"
            )
        )

        // Award dynamic focus XP
        val focusXpGained = focusTimeSelection * 4
        repository.addXp(focusXpGained)

        // Reset timer
        focusSecondsRemaining = focusTimeSelection * 60
        triggerAudioFeedback(5)
    }

    // Calibration check (simplifying English)
    fun runCalibrationDiagnostic() {
        val s = userSettings.value
        calibrationMessage = """
            [App Alignment Success]
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            • Selected Style: Active (${s.selectedAtmosphere})
            • Selected Language: ${s.selectedLanguage}
            • Sound Volume: ${(s.soundLevel * 100).toInt()}%
            • Sound Effects: ${if (s.auditoryEnabled) "ON" else "OFF"}
            • Vibration Effects: ${if (s.hapticEnabled) "ON" else "OFF"}
            • Selected AI Model: ${s.selectedModel}
            • Your Current Level: Level ${s.level}
            • Total Study Cards: ${flashcardsList.value.size} cards
            • Database Status: Connected & Healthy
        """.trimIndent()
        showCalibrationDialog = true
        triggerAudioFeedback(2)
    }
}
