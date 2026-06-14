package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class MemoryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = MemoryRepository(database.practiceDao(), application.applicationContext)

    // UI States
    val allItems: StateFlow<List<PracticeItem>> = repository.allPracticeItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ----- Multi-language content selection ----------------------------------------------------
    private val _availableLanguages = MutableStateFlow(
        listOf(LanguageOption("en", "English", "English", "en-US"))
    )
    val availableLanguages: StateFlow<List<LanguageOption>> = _availableLanguages.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("en")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    /**
     * Items scoped to the active language. The user's own custom phrases always travel with
     * them (they were authored in whatever language was active), so they are never filtered out.
     */
    val visibleItems: StateFlow<List<PracticeItem>> =
        combine(allItems, _selectedLanguage) { items, lang ->
            items.filter { it.language == lang || it.isCustom }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<PracticeLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val leaderboard: StateFlow<List<LeaderboardEntry>> = repository.leaderboard
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unlockedChapters: StateFlow<Map<String, Int>> = combine(allLogs, visibleItems) { logs, items ->
        val passingItemIds = logs.filter { it.accuracyScore >= 70 }.map { it.itemId }.toSet()
        val result = mutableMapOf("EASY" to 1, "MEDIUM" to 1, "HARD" to 1)
        
        listOf("EASY", "MEDIUM", "HARD").forEach { diff ->
            val itemsForDiff = items.filter { it.difficulty == diff }
            var maxUnlocked = 1
            for (ch in 1..10) {
                val itemsInCh = itemsForDiff.filter { it.chapter == ch }
                if (itemsInCh.isNotEmpty()) {
                    val completedInCh = itemsInCh.count { it.id in passingItemIds }
                    if (completedInCh >= 1) {
                        maxUnlocked = maxOf(maxUnlocked, ch + 1)
                    }
                }
            }
            result[diff] = minOf(maxUnlocked, 10)
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), mapOf("EASY" to 1, "MEDIUM" to 1, "HARD" to 1))

    // Filter properties
    private val _selectedDifficulty = MutableStateFlow("EASY")
    val selectedDifficulty: StateFlow<String> = _selectedDifficulty.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedType = MutableStateFlow("PARAGRAPH") // "WORD", "SENTENCE", "PARAGRAPH"
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    // Dynamic Accent and Typography theme selections (applied app-wide in real-time)
    private val _themeAccent = MutableStateFlow("TEAL")
    val themeAccent: StateFlow<String> = _themeAccent.asStateFlow()

    private val _themeTypography = MutableStateFlow("SANS")
    val themeTypography: StateFlow<String> = _themeTypography.asStateFlow()

    private val _fontScale = MutableStateFlow("BALANCED")
    val fontScale: StateFlow<String> = _fontScale.asStateFlow()

    private val _reminderTimes = MutableStateFlow<List<String>>(emptyList())
    val reminderTimes: StateFlow<List<String>> = _reminderTimes.asStateFlow()

    fun loadReminderTimes() {
        val prefs = getApplication<Application>().getSharedPreferences("xello_mind_reminders", android.content.Context.MODE_PRIVATE)
        val listStr = prefs.getString("reminder_list", "08:00 AM|08:00 PM") ?: "08:00 AM|08:00 PM"
        _reminderTimes.value = listStr.split("|").filter { it.isNotEmpty() }
    }

    fun addReminderTime(time: String) {
        val updated = _reminderTimes.value.toMutableList()
        if (!updated.contains(time)) {
            updated.add(time)
            saveReminderTimes(updated)
        }
    }

    fun removeReminderTime(time: String) {
        val updated = _reminderTimes.value.toMutableList()
        if (updated.remove(time)) {
            saveReminderTimes(updated)
        }
    }

    private fun saveReminderTimes(times: List<String>) {
        _reminderTimes.value = times
        val prefs = getApplication<Application>().getSharedPreferences("xello_mind_reminders", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("reminder_list", times.joinToString("|")).apply()
    }

    fun setThemeAccent(accent: String) {
        _themeAccent.value = accent
        val prefs = getApplication<Application>().getSharedPreferences("xello_mind_custom_theme", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("theme_accent", accent).apply()
    }

    fun setThemeTypography(typography: String) {
        _themeTypography.value = typography
        val prefs = getApplication<Application>().getSharedPreferences("xello_mind_custom_theme", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("theme_typography", typography).apply()
    }

    fun setFontScale(scale: String) {
        _fontScale.value = scale
        val prefs = getApplication<Application>().getSharedPreferences("xello_mind_custom_theme", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("font_scale", scale).apply()
    }

    // Current Navigation State
    private val _currentTab = MutableStateFlow("practice_list") // "dashboard", "practice_list", "active_practice", "leaderboard", "settings"
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Navigation History Stack
    private val tabHistory = java.util.Stack<String>()

    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    private fun updateGoBackState() {
        _canGoBack.value = !tabHistory.isEmpty()
    }

    // Active Practice Session State
    private val _activeItem = MutableStateFlow<PracticeItem?>(null)
    val activeItem: StateFlow<PracticeItem?> = _activeItem.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private val _practiceResult = MutableStateFlow<PracticeResult?>(null)
    val practiceResult: StateFlow<PracticeResult?> = _practiceResult.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    // Autosave tracking states
    private val _wasRestored = MutableStateFlow(false)
    val wasRestored: StateFlow<Boolean> = _wasRestored.asStateFlow()

    private val _hasAutosave = MutableStateFlow(false)
    val hasAutosave: StateFlow<Boolean> = _hasAutosave.asStateFlow()

    // Challenges Status
    private val _challenges = MutableStateFlow<List<SocialChallenge>>(emptyList())
    val challenges: StateFlow<List<SocialChallenge>> = _challenges.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndPrepopulate()
            loadAvailableLanguages()
            loadDailyChallenges()
            checkAutosavedSession()

            // Load saved theme customization settings
            val prefs = getApplication<Application>().getSharedPreferences("xello_mind_custom_theme", android.content.Context.MODE_PRIVATE)
            _themeAccent.value = prefs.getString("theme_accent", "TEAL") ?: "TEAL"
            _themeTypography.value = prefs.getString("theme_typography", "SANS") ?: "SANS"
            _fontScale.value = prefs.getString("font_scale", "BALANCED") ?: "BALANCED"

            // Load saved reminder times
            loadReminderTimes()
        }
    }

    fun checkAutosavedSession() {
        val prefs = getApplication<Application>().getSharedPreferences("xello_mind_autosave", android.content.Context.MODE_PRIVATE)
        val savedItemId = prefs.getInt("autosave_item_id", -1)
        val savedText = prefs.getString("autosave_text", "") ?: ""
        _hasAutosave.value = savedItemId != -1 && savedText.trim().isNotEmpty()
    }

    fun resumeAutosavedSession() {
        val prefs = getApplication<Application>().getSharedPreferences("xello_mind_autosave", android.content.Context.MODE_PRIVATE)
        val savedItemId = prefs.getInt("autosave_item_id", -1)
        val savedText = prefs.getString("autosave_text", "") ?: ""
        if (savedItemId != -1 && savedText.trim().isNotEmpty()) {
            val allItemsSnapshot = allItems.value
            val item = allItemsSnapshot.find { it.id == savedItemId }
            if (item != null) {
                val oldTab = _currentTab.value
                if (oldTab != "active_practice") {
                    if (tabHistory.isEmpty() || tabHistory.peek() != oldTab) {
                        tabHistory.push(oldTab)
                    }
                }
                _activeItem.value = item
                _spokenText.value = savedText
                _practiceResult.value = null
                _isRecording.value = false
                _wasRestored.value = true
                _currentTab.value = "active_practice"
                updateGoBackState()
            }
        }
    }

    fun clearAutosavedSession() {
        val prefs = getApplication<Application>().getSharedPreferences("xello_mind_autosave", android.content.Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        _hasAutosave.value = false
        _wasRestored.value = false
    }

    fun setTab(tab: String) {
        setTab(tab, addToHistory = true)
    }

    fun setTab(tab: String, addToHistory: Boolean) {
        val oldTab = _currentTab.value
        if (oldTab != tab) {
            if (addToHistory) {
                if (tabHistory.isEmpty() || tabHistory.peek() != oldTab) {
                    tabHistory.push(oldTab)
                }
            }
            _currentTab.value = tab
            updateGoBackState()
        }
        if (tab != "active_practice") {
            // reset active practice when leaving
            _activeItem.value = null
            _spokenText.value = ""
            _practiceResult.value = null
            _isRecording.value = false
            _wasRestored.value = false
            checkAutosavedSession()
        }
    }

    fun navigateBack(): Boolean {
        if (!tabHistory.isEmpty()) {
            val prevTab = tabHistory.pop()
            setTab(prevTab, addToHistory = false)
            updateGoBackState()
            return true
        }
        updateGoBackState()
        return false
    }

    fun setTypeFilter(type: String) {
        _selectedType.value = type
    }

    fun setDifficultyFilter(difficulty: String) {
        _selectedDifficulty.value = difficulty
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    private val langPrefs
        get() = getApplication<Application>().getSharedPreferences("xello_mind_language", android.content.Context.MODE_PRIVATE)

    private fun loadAvailableLanguages() {
        val langs = repository.getAvailableLanguages()
        _availableLanguages.value = langs

        // Restore the saved choice; otherwise match the device language if we ship it, else English.
        val saved = langPrefs.getString("selected_language", null)
        val deviceLang = Locale.getDefault().language // e.g. "hi", "es"
        val resolved = when {
            saved != null && langs.any { it.code == saved } -> saved
            langs.any { it.code == deviceLang } -> deviceLang
            else -> "en"
        }
        _selectedLanguage.value = resolved
    }

    fun setLanguage(code: String) {
        if (_selectedLanguage.value == code) return
        _selectedLanguage.value = code
        langPrefs.edit().putString("selected_language", code).apply()
        // Reset content filters so the user lands on a clean, fully-unlocked-aware view.
        _selectedCategory.value = "ALL"
    }

    /** BCP-47 tag for the active language, used to steer on-device speech recognition. */
    fun localeTagForLanguage(code: String): String =
        _availableLanguages.value.firstOrNull { it.code == code }?.localeTag ?: code

    fun startPractice(item: PracticeItem) {
        val oldTab = _currentTab.value
        if (oldTab != "active_practice") {
            if (tabHistory.isEmpty() || tabHistory.peek() != oldTab) {
                tabHistory.push(oldTab)
            }
        }
        _activeItem.value = item
        
        // Auto-resume check for this specific card
        val prefs = getApplication<Application>().getSharedPreferences("xello_mind_autosave", android.content.Context.MODE_PRIVATE)
        val savedItemId = prefs.getInt("autosave_item_id", -1)
        val savedText = prefs.getString("autosave_text", "") ?: ""
        if (savedItemId == item.id && savedText.trim().isNotEmpty()) {
            _spokenText.value = savedText
            _wasRestored.value = true
        } else {
            _spokenText.value = ""
            _wasRestored.value = false
        }
        
        _practiceResult.value = null
        _isRecording.value = false
        _currentTab.value = "active_practice"
        updateGoBackState()
        checkAutosavedSession()
    }

    fun addCustomItem(content: String, type: String, difficulty: String, category: String) {
        viewModelScope.launch {
            repository.insertCustomItem(content, type, difficulty, category, _selectedLanguage.value)
        }
    }

    fun deleteItem(id: Int) {
        viewModelScope.launch {
            repository.deleteItem(id)
        }
    }

    fun updateRecordingState(recording: Boolean) {
        _isRecording.value = recording
    }

    fun setSpokenText(text: String) {
        _spokenText.value = text
        
        // Write to autosave preferences instantly
        val item = _activeItem.value
        if (item != null) {
            val prefs = getApplication<Application>().getSharedPreferences("xello_mind_autosave", android.content.Context.MODE_PRIVATE)
            prefs.edit()
                .putInt("autosave_item_id", item.id)
                .putString("autosave_text", text)
                .apply()
            _hasAutosave.value = text.trim().isNotEmpty()
        }
    }

    // Levenshtein Text-matching Algorithm to compute memory accuracy score
    fun submitPractice(startTimeMs: Long) {
        val item = _activeItem.value ?: return
        val speakTxt = _spokenText.value.trim()
        val originalTxt = item.content.trim()

        if (speakTxt.isEmpty()) return

        val duration = System.currentTimeMillis() - startTimeMs
        val accuracy = calculateSimilarityPercentage(originalTxt, speakTxt)

        // Calculate speaking rate: words per minute (WPM)
        val wordCount = speakTxt.split(Regex("\\s+")).filter { it.isNotEmpty() }.size
        val durationSecs = maxOf(duration, 1000L) / 1000.0
        val wpmValue = ((wordCount / durationSecs) * 60).toInt()

        viewModelScope.launch {
            val (xpEarned, streakIncremented) = repository.savePracticeLog(
                itemId = item.id,
                itemType = item.type,
                originalText = originalTxt,
                recognizedText = speakTxt,
                accuracyScore = accuracy,
                durationMs = duration,
                difficulty = item.difficulty
            )

            // Dynamic challenge matching updates
            updateChallengesState(item, accuracy)

            _practiceResult.value = PracticeResult(
                accuracyScore = accuracy,
                xpEarned = xpEarned,
                streakIncremented = streakIncremented,
                originalText = originalTxt,
                recognizedText = speakTxt,
                wpm = wpmValue,
                durationMs = duration
            )
            clearAutosavedSession()
        }
    }

    fun updateUserSettings(username: String, isDarkMode: Boolean, reminders: Boolean) {
        viewModelScope.launch {
            repository.updateSettings(username, isDarkMode, reminders)
        }
    }

    // Levenshtein algorithm for similarity percentage (works 100% offline)
    private fun calculateSimilarityPercentage(s1: String, s2: String): Int {
        // Clean texts from common punctuations to focus on pure verbal recall
        val norm1 = s1.lowercase(Locale.getDefault())
            .replace(Regex("[.,/#!$%^&*;:{}=\\-_`~()?\"'\\s+]"), " ").replace(Regex("\\s+"), " ").trim()
        val norm2 = s2.lowercase(Locale.getDefault())
            .replace(Regex("[.,/#!$%^&*;:{}=\\-_`~()?\"'\\s+]"), " ").replace(Regex("\\s+"), " ").trim()

        if (norm1 == norm2) return 100
        if (norm1.isEmpty() || norm2.isEmpty()) return 0

        val words1 = norm1.split(" ")
        val words2 = norm2.split(" ")

        val dp = Array(words1.size + 1) { IntArray(words2.size + 1) }

        for (i in 0..words1.size) {
            dp[i][0] = i
        }
        for (j in 0..words2.size) {
            dp[0][j] = j
        }

        for (i in 1..words1.size) {
            for (j in 1..words2.size) {
                if (words1[i - 1] == words2[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1]
                } else {
                    dp[i][j] = 1 + minOf(
                        dp[i - 1][j],      // Deletion
                        dp[i][j - 1],      // Insertion
                        dp[i - 1][j - 1]   // Substitution
                    )
                }
            }
        }

        val maxLen = maxOf(words1.size, words2.size)
        val distance = dp[words1.size][words2.size]
        val similarity = (1.0 - (distance.toDouble() / maxLen.toDouble())) * 100
        return similarity.toInt().coerceIn(0, 100)
    }

    // Challenge Management Loader
    private fun loadDailyChallenges() {
        _challenges.value = listOf(
            SocialChallenge(
                id = 1,
                title = "Rapid Starter",
                description = "Secure an accuracy score of 95%+ in any Word practice exercise.",
                type = "WORD_95",
                target = 95,
                progress = 0,
                completed = false,
                xpReward = 80
            ),
            SocialChallenge(
                id = 2,
                title = "Sentence Champion",
                description = "Examine and master two Sentence exercises at Medium or Hard difficulty.",
                type = "SENTENCE_COUNT",
                target = 2,
                progress = 0,
                completed = false,
                xpReward = 120
            ),
            SocialChallenge(
                id = 3,
                title = "Executive Recall",
                description = "Tackle any scientific or wisdom Paragraph at Hard level with 80%+ accuracy.",
                type = "PARAGRAPH_HARD",
                target = 80,
                progress = 0,
                completed = false,
                xpReward = 200
            )
        )
    }

    private fun updateChallengesState(item: PracticeItem, score: Int) {
        val updated = _challenges.value.map { challenge ->
            if (challenge.completed) return@map challenge

            var completed = false
            var progress = challenge.progress

            when (challenge.type) {
                "WORD_95" -> {
                    if (item.type == "WORD" && score >= challenge.target) {
                        completed = true
                        progress = 1
                    }
                }
                "SENTENCE_COUNT" -> {
                    if (item.type == "SENTENCE" && (item.difficulty == "MEDIUM" || item.difficulty == "HARD") && score >= 75) {
                        progress = (progress + 1).coerceAtMost(challenge.target)
                        if (progress >= challenge.target) {
                            completed = true
                        }
                    }
                }
                "PARAGRAPH_HARD" -> {
                    if (item.type == "PARAGRAPH" && item.difficulty == "HARD" && score >= challenge.target) {
                        completed = true
                        progress = 1
                    }
                }
            }

            if (completed && !challenge.completed) {
                // Award completion bonus XP if challenge just finished
                viewModelScope.launch {
                    val profile = repository.getUserProfileOnce()
                    if (profile != null) {
                        repository.updateSettings(
                            username = profile.username,
                            isDarkMode = profile.isDarkMode,
                            reminders = profile.practiceReminderEnabled
                        )
                        // Trigger profile updating with added award
                        val bonusProfile = profile.copy(totalXp = profile.totalXp + challenge.xpReward)
                        database.practiceDao().insertOrUpdateProfile(bonusProfile)
                    }
                }
            }

            challenge.copy(progress = progress, completed = completed)
        }
        _challenges.value = updated
    }

    fun startNextExercise() {
        val current = _activeItem.value ?: return
        // Stay within the active language so "next" never jumps to a different language's deck.
        val items = visibleItems.value

        // Filter elements of identical difficulty level (e.g. EASY, MEDIUM, HARD)
        val sameDiffItems = items.filter { it.difficulty == current.difficulty }
        
        val currentIndex = sameDiffItems.indexOfFirst { it.id == current.id }
        if (currentIndex != -1 && currentIndex < sameDiffItems.lastIndex) {
            val nextItem = sameDiffItems[currentIndex + 1]
            startPractice(nextItem)
        } else {
            // Loop difficulty or advance to the next level
            val nextDiff = when (current.difficulty.uppercase()) {
                "EASY" -> "MEDIUM"
                "MEDIUM" -> "HARD"
                else -> "EASY"
            }
            val nextDiffItems = items.filter { it.difficulty == nextDiff }
            if (nextDiffItems.isNotEmpty()) {
                startPractice(nextDiffItems.first())
            }
        }
    }
}

// Support Structs
data class PracticeResult(
    val accuracyScore: Int,
    val xpEarned: Int,
    val streakIncremented: Boolean,
    val originalText: String,
    val recognizedText: String,
    val wpm: Int = 0,
    val durationMs: Long = 0L
)

data class SocialChallenge(
    val id: Int,
    val title: String,
    val description: String,
    val type: String,
    val target: Int,
    val progress: Int,
    val completed: Boolean,
    val xpReward: Int
)
