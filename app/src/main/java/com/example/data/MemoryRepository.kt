package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MemoryRepository(private val dao: PracticeDao) {

    val allPracticeItems: Flow<List<PracticeItem>> = dao.getAllPracticeItems()
    val allLogs: Flow<List<PracticeLog>> = dao.getAllPracticeLogs()
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val leaderboard: Flow<List<LeaderboardEntry>> = dao.getLeaderboardEntries()

    fun getItemsByType(type: String): Flow<List<PracticeItem>> = dao.getPracticeItemsByType(type)
    fun getRecentLogs(limit: Int): Flow<List<PracticeLog>> = dao.getRecentPracticeLogs(limit)

    suspend fun insertCustomItem(content: String, type: String, difficulty: String, category: String) {
        val item = PracticeItem(
            type = type,
            content = content,
            difficulty = difficulty,
            category = category,
            isCustom = true
        )
        dao.insertPracticeItem(item)
    }

    suspend fun deleteItem(id: Int) = dao.deletePracticeItem(id)

    suspend fun getUserProfileOnce(): UserProfile? = dao.getUserProfileOnce()

    private fun generate365Exercises(): List<PracticeItem> {
        val items = mutableListOf<PracticeItem>()
        val categories = listOf("Science", "Focus", "Wisdom", "Visual", "General")
        
        // 1. WORDS (120 items)
        val wordPool = listOf(
            "Neuroscience", "Hippocampus", "Synapse", "Dendrite", "Myelin", "Neuron", "Neuroplasticity", "Cerebellum", "Cortex", "Amygdala",
            "Retention", "Retrieval", "Mnemonic", "Acronym", "Cognition", "Loci", "Association", "Chunking", "Consolidation", "Encoding",
            "Concentration", "Focus", "Perception", "Sensation", "Attention", "Vigilance", "Awareness", "Mindfulness", "Meditation", "Resilience",
            "Reflection", "Contemplation", "Observation", "Visualization", "Imagination", "Creativity", "Innovation", "Inspiration", "Intuition", "Insight",
            "Intellect", "Sagacity", "Philosophy", "Logic", "Reasoning", "Analysis", "Deduction", "Induction", "Hypothesis", "Thermodynamics",
            "Quantum", "Mechanism", "Evolution", "Biosphere", "Astronomy", "Galaxy", "Nebula", "Cosmos", "Telescope", "Microscope",
            "Syllable", "Metaphor", "Analogy", "Symphony", "Harmony", "Rhythm", "Melody", "Cadence", "Resonance", "Acoustics",
            "Vocal", "Articulation", "Elocution", "Oratory", "Eloquence", "Flourish", "Precision", "Clarity", "Brevity", "Gravity",
            "Serenity", "Tranquility", "Solitude", "Equanimity", "Patience", "Discipline", "Diligence", "Persistence", "Endurance", "Fortitude",
            "Adventure", "Discovery", "Exploration", "Curiosity", "Wonder", "Enigma", "Paradox", "Labyrinth", "Oasis", "Sanctuary",
            "Legacy", "Heritage", "Chronicle", "History", "Epoch", "Millennium", "Horizon", "Zenith", "Apex", "Summit",
            "Blueprint", "Architecture", "Foundation", "Structure", "Symmetry", "Pattern", "Geodesic", "Fractal", "Infinity", "Eternity"
        )
        for (i in 0 until 120) {
            val word = wordPool[i % wordPool.size]
            val diff = when {
                i < 40 -> "EASY"
                i < 80 -> "MEDIUM"
                else -> "HARD"
            }
            val difficultyOffset = when (diff) {
                "EASY" -> 0
                "MEDIUM" -> 40
                else -> 80
            }
            val category = categories[i % categories.size]
            val chapter = ((i - difficultyOffset) / 4) + 1
            items.add(PracticeItem(type = "WORD", content = word, difficulty = diff, category = category, isCustom = false, chapter = chapter))
        }

        // 2. SENTENCES (120 items)
        val sentencePool = listOf(
            "Focus on the present moment.",
            "Reading books sharpens your memory.",
            "Healthy sleep repairs brain cells.",
            "Practice makes a person perfect.",
            "A quiet mind is a strong mind.",
            "Write down your thoughts every day.",
            "Always keep learning something new.",
            "Drink plenty of water to stay alert.",
            "A warm smile builds instant trust.",
            "Consistency is the key to mastery.",
            "Laughter reduces unwanted stress hormones.",
            "Take a deep breath and relax.",
            "Engage your senses to remember better.",
            "A walk in nature clears the mind.",
            "Listen carefully before you speak copy.",
            "Read aloud to boost verbal pathways.",
            "Curiosity is the engine of wisdom.",
            "Visualizing characters helps retain details.",
            "Be patient with your mental progress.",
            "Your mind is highly adaptive.",
            "Puzzles challenge different brain networks.",
            "Good food supplies key brain fuel.",
            "Break tasks down into smaller steps.",
            "Stay organized for higher focus times.",
            "A positive outlook aids general learning.",
            "Meditation grows key cortical grey matter.",
            "Take simple notes during deep lectures.",
            "Speak clearly with a strong voice.",
            "Track your goals and daily habits.",
            "Resting is as vital as training.",
            
            "Active recall is the most effective way to retain new information.",
            "A healthy mind resides in a healthy body through daily exercise.",
            "Your brain is like a muscle; the more you train it, the stronger it grows.",
            "The art of memory is represented by the art of absolute attention.",
            "Neuroplasticity allows the mature brain to reorganize its synaptic connections.",
            "Consuming foods rich in omega-three fatty acids supports key cellular integrity.",
            "To remember a physical name, link it with a vivid visual caricature.",
            "We retain ninety percent of what we teach to another person.",
            "Deep breathing exercises enhance oxygen delivery to brain structures.",
            "Working memory acts as an active physical blackboard for incoming ideas.",
            "Mnemonics convert arbitrary names into logical visual associations.",
            "Sleep is the golden chain that ties our health and bodies together.",
            "The best way to predict the future is to active create and design it.",
            "Wisdom is not a byproduct of schooling but of lifelong training.",
            "Do not multitask; single-tasking yields double the mental performance.",
            "Familiarity is not the same as genuine internal conceptual comprehension.",
            "The prefrontal cortex regulates decision making and emotional balance.",
            "A structured study schedule prevents cognitive overload and memory fatigue.",
            "Challenging puzzles slow down the rate of normal cognitive decline.",
            "Your vocabulary is a direct window into your mental library.",
            "A clear purpose provides the emotional drive needed for retention.",
            "Reviewing material before bed triggers active neural consolidation.",
            "Optimizing your study environment removes unnecessary subconscious noise.",
            "The cognitive map in our brain is constructed by place cell neurons.",
            "Critical thinking requires questioning the obvious with elegant logic.",
            "True concentration is the total exclusion of all other thoughts.",
            "The human voice can convey deep layers of emotional resonance.",
            "Structured mnemonic journeys can store entire books in order.",
            "An enriched environment fosters the birth of new healthy neurons.",
            "Continuous feedback is essential for deliberate skill development.",
            
            "The hippocampus plays a fundamental role in consolidating short-term memory into long-term retention.",
            "Cognitive flexibility refers to our mental ability to switch between thinking about different concepts.",
            "Mnemonic techniques, such as the Method of Loci, have been utilized since ancient classical times.",
            "The cerebral cortex is highly organized into functional layers that process sensory information dynamically.",
            "Synaptic pruning is a natural biological process occurring to optimize active neural network efficiency.",
            "Metacognition involves actively monitors and regulating one's own internal cognitive strategies.",
            "Selective auditory attention allows us to focus on a single voice amid a noisy cocktail party.",
            "Episodic memory stores personally experienced events associated with specific times and places.",
            "Anatomical changes in brain structure can be measured following long-term mindfulness practice.",
            "The amygdala modulates memory strength based on emotional significance and survival salience.",
            "Distributed practice distributes learning over time rather than cramming into a single session.",
            "Dual-coding theory suggests holding information in both verbal and visual forms boosts retention.",
            "Executive functioning relies on complex networks linking the prefrontal cortex with subcortical targets.",
            "Anterograde amnesia prevents the formation of new semantic memories while leaving old ones intact.",
            "The default mode network is active when the brain is in a state of wakeful rest and daydreaming.",
            "Semantic encoding associates new incoming concepts with pre-existing framework structures in the brain.",
            "Long-term potentiation is the persistent strengthening of active synapses based on recent patterns.",
            "The brain filters hundreds of sensory stimulations per second before they enter conscious awareness.",
            "Phonological loops temporarily hold verbal data while visual sketchpads maintain spatial structures.",
            "Declarative memory is split into semantic facts and episodic personal historical records.",
            "Working memory capacity is a highly reliable predictor of success in complex cognitive tasks.",
            "Nootropics and natural adaptogens support optimal neurotransmitter production under high stress.",
            "Cognitive dissonance arises when new incoming facts conflict with deep long-held internal beliefs.",
            "Interleaving different topics during study cycles enhances long-term retrieval and problem solving.",
            "Prospective memory enables us to remember to perform intended actions in the distant future.",
            "Visual sensory memory has a high capacity but decays rapidly within milliseconds of stimulation.",
            "Mirror neurons fire both when performing an action and when observing someone else do it.",
            "Chunking complex alphanumeric strings reduces cognitive load on our limited storage registers.",
            "Aphasia is a language disorder resulting from damage to brain portions responsible for speech.",
            "The spatial mapping cells in our entorhinal cortex function like an internal coordinates grid."
        )
        for (i in 0 until 120) {
            val sentence = sentencePool[i % sentencePool.size]
            val diff = when {
                i < 40 -> "EASY"
                i < 80 -> "MEDIUM"
                else -> "HARD"
            }
            val difficultyOffset = when (diff) {
                "EASY" -> 0
                "MEDIUM" -> 40
                else -> 80
            }
            val category = categories[i % categories.size]
            val chapter = ((i - difficultyOffset) / 4) + 1
            items.add(PracticeItem(type = "SENTENCE", content = sentence, difficulty = diff, category = category, isCustom = false, chapter = chapter))
        }

        // 3. PARAGRAPHS (125 items)
        val paragraphPool = listOf(
            "To keep your memory sharp, simple lifestyle modifications make a huge difference. Regular physical exercise boosts oxygen flow to your brain. Getting eight hours of deep sleep allows your hippocampus to organize and consolidate everything you learned.",
            "Healthy eating fuels cognitive function. Consuming foods rich in omega-three fatty acids, like walnuts, flax seeds, and salmon, provides the building blocks for brain cells. Consuming fresh green leafy vegetables protects your nervous system from early fatigue.",
            "Taking regular short breaks during studies prevents mental fatigue. The Pomodoro technique advises working intensely for twenty-five minutes followed by a five-minute rest. This helps the prefrontal cortex recharge and maintain focus.",
            "Mnemonic systems allow you to attach meaningless facts to solid mental hooks. By linking new phone numbers or dates with visual stories, you change abstract symbols into memorable experiences. This relies on the brain's massive visual processing power.",
            "A healthy social life keeps brain networks active and keeps stress levels in check. Having deep conversations forces you to retrieve vocabulary terms rapidly and process other perspectives. People with strong social circles show slower cognitive decline as they age.",
            "Drinking water throughout the day is critical for intellectual sharp performance. Even mild dehydration can decrease attention span, disrupt working memory speed, and cause mild headaches. Keep a filled bottle next to your desk to stay fully hydrated.",
            "Your breathing style directly regulates your brain's level of alert focus. Shallow chest breaths cause low oxygenation and elevate cortisol. Slow, controlled belly breaths activate the vagus nerve, immediately slowing heart rate and sharpening concentration.",
            "Writing things by hand is vastly superior to typing on digital keypads. Handwriting engages intricate motor pathways, forcing your brain to synthesize the information as you compose each letter. This tactile feedback leaves a stronger physical trace in memory.",
            "Keeping a daily journal improves clarity of thought and emotional balance. Writing down your experiences helps you consolidate those memories, making them easier to recall in the future. It is a simple tool to observe your personal growth over time.",
            "Music has a profound effect on memory and learning. Listening to calm classical pieces or ambient tracks can help block distracting noises and create a peaceful mental training environment. It stimulates dopamine release, raising your potential for study.",
            "Science shows that practicing active retrieval is vastly superior to passive reading. When you look at information and immediately force yourself to recall it without looking, you strengthen neural pathways. With each effort, your brain hardens these connections.",
            "Focus is the entryway to memory. In our hyper-distracted modern era, multitasking acts as a direct inhibitor to learning. If you try to compile a report while reviewing emails and texting, your prefrontal cortex becomes overloaded, ensuring no deep memories are formed.",
            "The Method of Loci, also known as the Memory Palace, is one of the oldest and most successful recall techniques. It involves mapping visual representations of facts along a familiar physical path. When you need to retrieve them, you simply walk through that mental route.",
            "Stress is a silent destroyer of hippocampal brain function. When you are chronically stressed, your adrenal glands flood your blood with cortisol. High cortisol levels hinder synaptic plasticity, disrupt cell communication, and can even shrink physical storage centers.",
            "Developing a growth mindset is essential for peak cognitive training. If you believe your intelligence is fixed, you will avoid challenging tasks that lead to neural growth. Embracing difficult memory practice as a chance to grow build stronger connections over time.",
            "Quality sleep is divided into light, deep, and REM phases, each serving a unique cognitive purpose. Deep sleep clears metabolic waste from brain cells, while REM sleep is when the hippocampus replays and integrates newly acquired skills and complex memories.",
            "Dual coding theory explains that we process information through distinct verbal and non-verbal channels. Combining words with related drawings or diagrams creates dual traces in memory. If you forget the verbal explanation, the visual image can still trigger recall.",
            "Spaced repetition is a powerful technique that exploits the psychological spacing effect. By spacing out your review sessions over increasing intervals, you interrupt the forgetting curve. This forces the brain to actively reconstruct the memory, solidifying it.",
            "The prefrontal cortex is the seat of executive function, managing focus, planning, and impulse control. Eating antioxidants and keeping a steady sleep schedule prevents this delicate brain hub from fatiguing, keeping your mental sharp and disciplined.",
            "Our minds are naturally drawn to the unusual and emotional. Mnemonic systems turn dry data into funny, weird, or sensory-rich scenarios to exploit this bias. A talking apple or a glowing key is infinitely easier to recall than a list of plain, abstract figures.",
            "The process of neuroplasticity refers to the brain's remarkable ability to reorganize itself by forming new neural connections throughout life. This mechanism allows the neurons to adjust response to new situations, environmental changes, or cognitive training. By challenging ourselves with novel practices, we stimulate synaptic transmission.",
            "Psychologists define working memory as a cognitive system with a limited capacity that can hold information temporarily. It is critical for reasoning, decision-making, and behavior. Unlike traditional passive memory, working memory operates as an active mental workspace, processing and manipulating information on the fly, which degrades in efficiency under stress.",
            "The human hippocampal complex acts as an essential gateway for declarative memories. While long-term storage is ultimately consolidated in the neocortex, the hippocampus compiles and indexes these memories temporarily. Damage to this area disrupts our ability to form new episodic records while leaving procedural muscle memories unaffected.",
            "Long-term potentiation, or LTP, is a persistent strengthening of synapse connections based on recent patterns of activity. These are widely considered one of the primary cellular mechanisms that underly learning and memory. When presynaptic and postsynaptic neurons fire repeatedly together, the connection grows robustly.",
            "Cognitive reserve refers to the brain's resilience to neuropathological damage. Building a robust cognitive reserve through lifelong mental stimulation, challenging education, and language learning assists in maintaining function. It enables the brain to recruit alternative neural networks to bypass damaged pathways.",
            "The entorhinal cortex contains specialized neurons known as grid cells, which fire in a highly structured triangular coordinate framework. These cells serve as an internal positioning system, providing spatial context to incoming episodic experiences. They communicate directly with place cells inside the hippocampus to orient memory.",
            "The default mode network consists of interacting brain regions that show highly correlated activity when an individual is not focused on the external world. When you daydream, reflect on your past, or think about your future, this network lights up. Chronic stress can cause this network to interfere with goal-directed focus.",
            "Anterograde amnesia is a selective loss of the ability to create new memories after the event that caused the amnesia, leading to a partial or complete inability to recall the recent past. Despite this profound damage, patients can often still learn new motor actions, demonstrating the independence of procedural systems.",
            "Working memory operates under the executive supervision of the central executive, which delegates tasks to the phonological loop and the visuospatial sketchpad. This temporary holding loop is susceptible to cognitive overload. When sensory inputs overwhelm the workspace, background inputs are discarded to protect processing.",
            "The synaptic pruning hypothesis proposes that the brain actively eliminates redundant or weak connections during developmental stages to streamline active pathways. This process shape-shifts neural networks to adapt to frequent tasks. It represents a vital physical adaptation that keeps our thinking fluid and efficient."
        )
        for (i in 0 until 125) {
            val baseText = paragraphPool[i % paragraphPool.size]
            val diff = when {
                i < 41 -> "EASY"
                i < 82 -> "MEDIUM"
                else -> "HARD"
            }
            val difficultyOffset = when (diff) {
                "EASY" -> 0
                "MEDIUM" -> 41
                else -> 82
            }
            val category = categories[i % categories.size]
            val chapter = ((i - difficultyOffset) / 4) + 1
            
            val repetitionCount = i / paragraphPool.size
            val textWithHeader = if (repetitionCount > 0) {
                "[Focus Cycle $repetitionCount] $baseText"
            } else {
                baseText
            }
            items.add(PracticeItem(type = "PARAGRAPH", content = textWithHeader, difficulty = diff, category = category, isCustom = false, chapter = chapter))
        }
        return items
    }

    // Check database state and prepopulate if empty
    suspend fun checkAndPrepopulate() {
        withContext(Dispatchers.IO) {
            try {
                // Prepopulate items if database is empty or has outdated / incomplete syllabus count
                if (dao.getPracticeItemsCount() < 300) {
                    dao.deleteNonCustomItems()
                    val defaultItems = generate365Exercises()
                    dao.insertPracticeItems(defaultItems)
                }

                // Initial Profile if not exists
                val currentProfile = dao.getUserProfileOnce()
                if (currentProfile == null) {
                    dao.insertOrUpdateProfile(
                        UserProfile(
                            id = 1,
                            username = "Memory Champ",
                            totalXp = 150, // Starting default
                            currentStreak = 1,
                            lastPracticeDate = getTodayDateString(),
                            isDarkMode = true,
                            practiceReminderEnabled = true
                        )
                    )
                }

                // Initial Bots on Leaderboard to compete
                seedInitialLeaderboard()

            } catch (e: Exception) {
                Log.e("MemoryRepository", "Prepopulation failed", e)
            }
        }
    }

    private suspend fun seedInitialLeaderboard() {
        val bots = listOf(
            LeaderboardEntry(1, "Sophia_Synapse", 1250, 1, "#E91E63", challengeActive = true, challengeTargetScore = 85),
            LeaderboardEntry(2, "Aarav_Recall", 1020, 2, "#2196F3", challengeActive = false),
            LeaderboardEntry(3, "Vivaan_Focus", 940, 3, "#FFEB3B", challengeActive = true, challengeTargetScore = 90),
            LeaderboardEntry(4, "Memory Champ", 150, 4, "#4CAF50", isCurrentUser = true),
            LeaderboardEntry(5, "Mia_ZenMind", 410, 5, "#9C27B0", challengeActive = false),
            LeaderboardEntry(6, "Rohan_Brainy", 320, 6, "#FF9800", challengeActive = true, challengeTargetScore = 80)
        )
        dao.insertLeaderboardEntries(bots)
    }

    // Save practice log, update Streak, increase XP, and update Leaderboard!
    suspend fun savePracticeLog(
        itemId: Int,
        itemType: String,
        originalText: String,
        recognizedText: String,
        accuracyScore: Int,
        durationMs: Long,
        difficulty: String
    ): Pair<Int, Boolean> { // returns (EarnedXP, StreakIncremented)
        return withContext(Dispatchers.IO) {
            val log = PracticeLog(
                itemId = itemId,
                itemType = itemType,
                originalText = originalText,
                recognizedText = recognizedText,
                accuracyScore = accuracyScore,
                durationMs = durationMs
            )
            dao.insertPracticeLog(log)

            // Get profile and calculate Streak/XP rewards
            val profile = dao.getUserProfileOnce() ?: UserProfile()
            val todayStr = getTodayDateString()
            val lastPracStr = profile.lastPracticeDate

            val (newStreak, streakIncremented) = calculateNewStreak(lastPracStr, todayStr, profile.currentStreak)

            // Compute XP base and multiplier
            val difficultyMultiplier = when (difficulty.uppercase()) {
                "MEDIUM" -> 1.5
                "HARD" -> 2.0
                else -> 1.0
            }
            // Accuracy scales XP points
            val earnedXp = ((accuracyScore / 100.0) * 100 * difficultyMultiplier).toInt().coerceAtLeast(10)

            val updatedXp = profile.totalXp + earnedXp

            // Update user profile
            val updatedProfile = profile.copy(
                totalXp = updatedXp,
                currentStreak = newStreak,
                lastPracticeDate = todayStr
            )
            dao.insertOrUpdateProfile(updatedProfile)

            // Update Leaderboard Rank for Current User dynamically!
            updateLeaderboardRanks(updatedXp)

            Pair(earnedXp, streakIncremented)
        }
    }

    private fun calculateNewStreak(lastPractice: String, today: String, currentStreak: Int): Pair<Int, Boolean> {
        if (lastPractice.isEmpty()) {
            return Pair(1, true)
        }
        if (lastPractice == today) {
            return Pair(currentStreak, false) // Did it today already, keep current streak
        }

        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val lastDate = sdf.parse(lastPractice)
            val todayDate = sdf.parse(today)
            if (lastDate != null && todayDate != null) {
                val diffMs = todayDate.time - lastDate.time
                val diffDays = diffMs / (1000 * 60 * 60 * 24)
                if (diffDays == 1L) {
                    Pair(currentStreak + 1, true) // Consecutive day increase!
                } else if (diffDays > 1L) {
                    Pair(1, true) // Missed a day, resets to 1
                } else {
                    Pair(currentStreak, false) // Retroactive or error case safety
                }
            } else {
                Pair(1, true)
            }
        } catch (e: Exception) {
            Pair(1, true)
        }
    }

    private suspend fun updateLeaderboardRanks(currentUserNewXp: Int) {
        val staticCompetitors = listOf(
            LeaderboardEntry(1, "Sophia_Synapse", 1250, 1, "#E91E63", challengeActive = true, challengeTargetScore = 85),
            LeaderboardEntry(2, "Aarav_Recall", 1020, 2, "#2196F3", challengeActive = false),
            LeaderboardEntry(3, "Vivaan_Focus", 940, 3, "#FFEB3B", challengeActive = true, challengeTargetScore = 90),
            LeaderboardEntry(4, "Memory Champ", currentUserNewXp, 4, "#4CAF50", isCurrentUser = true),
            LeaderboardEntry(5, "Mia_ZenMind", 410, 5, "#9C27B0", challengeActive = false),
            LeaderboardEntry(6, "Rohan_Brainy", 320, 6, "#FF9800", challengeActive = true, challengeTargetScore = 80)
        )

        // Sort all entries descending by score/XP and assign ranks
        val sortedList = staticCompetitors.sortedByDescending { it.totalXp }
        val rankedList = sortedList.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
        dao.insertLeaderboardEntries(rankedList)
    }

    suspend fun updateSettings(username: String, isDarkMode: Boolean, reminders: Boolean) {
        withContext(Dispatchers.IO) {
            val profile = dao.getUserProfileOnce() ?: UserProfile()
            val updated = profile.copy(
                username = username,
                isDarkMode = isDarkMode,
                practiceReminderEnabled = reminders
            )
            dao.insertOrUpdateProfile(updated)

            // Also sync username on Leaderboard
            dao.getLeaderboardEntries().collect { list ->
                val updatedList = list.map {
                    if (it.isCurrentUser) it.copy(username = username) else it
                }
                dao.insertLeaderboardEntries(updatedList)
            }
        }
    }

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
}
