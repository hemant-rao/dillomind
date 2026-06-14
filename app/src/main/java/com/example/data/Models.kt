package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "practice_items")
data class PracticeItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "WORD", "SENTENCE", "PARAGRAPH"
    val content: String,
    val difficulty: String, // "EASY", "MEDIUM", "HARD"
    val category: String, // "General", "Visual", "Science", "Focus", "Wisdom", "Story", "Poem", "Humor"
    val isCustom: Boolean = false,
    val chapter: Int = 1,
    // BCP-47-ish language code this item belongs to ("en", "hi", "es", "fr", "ur", ...).
    // Custom items inherit the user's active language so they show up alongside the rest.
    val language: String = "en"
)

@Entity(tableName = "practice_logs")
data class PracticeLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemId: Int,
    val itemType: String,
    val originalText: String,
    val recognizedText: String,
    val accuracyScore: Int, // 0 to 100
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val username: String = "MemoryChamp",
    val totalXp: Int = 0,
    val currentStreak: Int = 0,
    val lastPracticeDate: String = "", // YYYY-MM-DD
    val isDarkMode: Boolean = true,
    val practiceReminderEnabled: Boolean = true,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0
)

@Entity(tableName = "leaderboard_entries")
data class LeaderboardEntry(
    @PrimaryKey val id: Int,
    val username: String,
    val totalXp: Int,
    val rank: Int,
    val avatarColorHex: String,
    val isCurrentUser: Boolean = false,
    val challengeActive: Boolean = false,
    val challengeTargetScore: Int = 0
)
