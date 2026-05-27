package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 0,
    val xp: Int = 120,
    val level: Int = 1,
    val studyStreak: Int = 3,
    val selectedLanguage: String = "English", // English, Hindi, Hinglish, Spanish
    val selectedAtmosphere: String = "Cosmic Glow", // Cosmic Glow, Nebula Teal, Luminescent Light-Slate Space
    val soundLevel: Float = 0.5f,
    val auditoryEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val selectedModel: String = "gemini-3.5-flash" // gemini-3.5-flash or gemini-3.1-pro-preview
)

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val answer: String,
    val deckName: String = "General"
)

@Entity(tableName = "quiz_history")
data class QuizHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Int,
    val totalQuestions: Int,
    val xpEarned: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val topic: String
)

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String,
    val isCompleted: Boolean = true
)
