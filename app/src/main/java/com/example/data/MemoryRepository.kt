package com.example.data

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MemoryRepository(
    private val dao: PracticeDao,
    private val appContext: Context
) {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val allPracticeItems: Flow<List<PracticeItem>> = dao.getAllPracticeItems()
    val allLogs: Flow<List<PracticeLog>> = dao.getAllPracticeLogs()
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val leaderboard: Flow<List<LeaderboardEntry>> = dao.getLeaderboardEntries()

    fun getItemsByType(type: String): Flow<List<PracticeItem>> = dao.getPracticeItemsByType(type)
    fun getRecentLogs(limit: Int): Flow<List<PracticeLog>> = dao.getRecentPracticeLogs(limit)

    suspend fun insertCustomItem(content: String, type: String, difficulty: String, category: String, language: String) {
        val item = PracticeItem(
            type = type,
            content = content,
            difficulty = difficulty,
            category = category,
            isCustom = true,
            language = language
        )
        dao.insertPracticeItem(item)
    }

    suspend fun deleteItem(id: Int) = dao.deletePracticeItem(id)

    suspend fun getUserProfileOnce(): UserProfile? = dao.getUserProfileOnce()

    // Curated, fully unique offline syllabus: 120 words + 120 sentences + 120 paragraphs.
    // Each type splits into 40 EASY / 40 MEDIUM / 40 HARD, and each difficulty maps cleanly
    // to 10 chapters of 4 exercises. No repeats and no filler — a genuine year of daily recall.
    private fun generateCoreExercises(): List<PracticeItem> {
        val items = mutableListOf<PracticeItem>()
        val categories = listOf("Science", "Focus", "Wisdom", "Visual", "General")

        // 1. WORDS (120 unique items)
        val wordPool = listOf(
            // EASY (40) — short, everyday words of mind and growth
            "Focus", "Memory", "Brain", "Recall", "Learn", "Think", "Calm", "Sleep", "Dream", "Idea",
            "Logic", "Sense", "Habit", "Goal", "Skill", "Study", "Notes", "Quiet", "Energy", "Effort",
            "Growth", "Vision", "Wonder", "Wisdom", "Insight", "Clarity", "Balance", "Rhythm", "Pattern", "Signal",
            "Senses", "Mindful", "Curious", "Patience", "Courage", "Kindness", "Gratitude", "Discipline", "Awareness", "Confidence",
            // MEDIUM (40) — richer cognitive vocabulary
            "Cognition", "Attention", "Retention", "Retrieval", "Reflection", "Perception", "Reasoning", "Intuition", "Imagination", "Creativity",
            "Motivation", "Concentration", "Observation", "Association", "Visualization", "Comprehension", "Resilience", "Persistence", "Endurance", "Adaptability",
            "Mnemonic", "Acronym", "Chunking", "Encoding", "Rehearsal", "Mindfulness", "Meditation", "Serenity", "Equanimity", "Contemplation",
            "Neuron", "Synapse", "Dendrite", "Cortex", "Cerebellum", "Hippocampus", "Neurotransmitter", "Plasticity", "Cognitive", "Analytical",
            // HARD (40) — advanced neuroscience and scholarship
            "Neuroplasticity", "Metacognition", "Consolidation", "Potentiation", "Synaptogenesis", "Neurogenesis", "Myelination", "Connectivity", "Differentiation", "Reconsolidation",
            "Proprioception", "Interoception", "Phonological", "Visuospatial", "Semantic", "Episodic", "Procedural", "Declarative", "Prospective", "Retrospective",
            "Entorhinal", "Prefrontal", "Amygdala", "Thalamus", "Hypothalamus", "Neocortex", "Acetylcholine", "Dopaminergic", "Glutamatergic", "Electrophysiology",
            "Psychophysics", "Chronobiology", "Interleaving", "Elaboration", "Recapitulation", "Heuristic", "Schema", "Cognizance", "Sagacity", "Erudition"
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
            // EASY (40) — short, simple, encouraging
            "Focus on one task at a time.",
            "A calm mind learns faster.",
            "Practice a little every single day.",
            "Good sleep strengthens your memory.",
            "Read slowly and picture each word.",
            "Write your ideas down by hand.",
            "Take a deep breath and relax.",
            "Curiosity makes learning feel easy.",
            "Drink water to stay sharp.",
            "Repeat new facts out loud.",
            "Small steps lead to big progress.",
            "Rest is a real part of learning.",
            "A quiet room helps you focus.",
            "Believe in your own growth.",
            "Review your notes before sleep.",
            "Patience builds a strong mind.",
            "Move your body to clear your head.",
            "Ask questions to learn more.",
            "Teach someone to remember better.",
            "Break large tasks into small ones.",
            "Your brain loves a good story.",
            "Smile and your stress will fade.",
            "Listen first, then speak clearly.",
            "A short walk refreshes the mind.",
            "Stay curious about the world.",
            "Notice the small details around you.",
            "One clear goal guides your day.",
            "Kind words build real trust.",
            "Healthy food fuels a sharp brain.",
            "Turn off the noise and concentrate.",
            "Celebrate every small win today.",
            "Sunlight lifts your mood and focus.",
            "Keep your desk clean and tidy.",
            "A grateful heart sleeps well.",
            "Try again, a little bit better.",
            "Slow breathing calms a busy mind.",
            "Learn one new word each day.",
            "Picture the idea in your mind.",
            "Effort matters more than talent.",
            "Begin now, and start small.",
            // MEDIUM (40) — practical learning science, moderate length
            "Active recall is the fastest way to remember new information.",
            "Your brain grows stronger each time you challenge it.",
            "Linking new ideas to old ones makes them easier to recall.",
            "A vivid mental image is far easier to remember than a plain fact.",
            "Reviewing material in short sessions beats one long cram.",
            "Deep breathing brings more oxygen to your working brain.",
            "We remember most of what we explain to another person.",
            "A clear purpose gives you the energy to keep learning.",
            "Single tasking produces far better results than multitasking.",
            "The mind remembers the unusual and forgets the ordinary.",
            "Spacing your study sessions slows down forgetting.",
            "Writing by hand leaves a stronger trace in memory.",
            "A short nap can refresh tired mental focus.",
            "Curiosity turns difficult study into an enjoyable game.",
            "Calm attention is the doorway to lasting memory.",
            "Strong habits remove the need for daily willpower.",
            "Reading aloud strengthens the pathways for clear speech.",
            "A walk in nature quietly restores your focus.",
            "Mistakes are simply signals pointing toward growth.",
            "Reflection at night helps the brain store the day.",
            "Puzzles gently exercise many different brain networks.",
            "Good posture supports steady breathing and clear thinking.",
            "Music without lyrics can sharpen your concentration.",
            "Confidence grows each time you finish a hard task.",
            "A balanced diet keeps your mind steady and alert.",
            "Boredom often hides a chance for deeper focus.",
            "Naming your feelings helps calm a restless mind.",
            "The first few minutes set the tone for any session.",
            "Repetition turns a new skill into second nature.",
            "Gratitude gently shifts the mind toward calm and clarity.",
            "The hippocampus helps turn experiences into lasting memories.",
            "Neurons that fire together gradually wire together.",
            "Mild stress sharpens focus, while heavy stress dulls it.",
            "The brain filters most signals before you ever notice them.",
            "Sleep quietly replays and stores the skills you practiced.",
            "Imagination and memory share many of the same pathways.",
            "Attention is a limited resource, so spend it wisely.",
            "Emotion makes a memory far more vivid and durable.",
            "Your environment shapes your focus more than you think.",
            "Steady effort compounds into remarkable long-term results.",
            // HARD (40) — dense, precise neuroscience and learning theory
            "The hippocampus consolidates fragile short-term traces into stable long-term memories during deep sleep.",
            "Cognitive flexibility is the mental ability to shift smoothly between different concepts and perspectives.",
            "The method of loci stores ordered information by placing vivid images along a familiar mental route.",
            "Synaptic pruning removes weak or unused connections so that frequently used pathways become more efficient.",
            "Metacognition is the practice of observing and adjusting your own thinking strategies as you learn.",
            "Selective attention lets you follow a single voice within a crowded and noisy room.",
            "Episodic memory preserves personal events bound to specific times, places, and emotions.",
            "Long-term potentiation strengthens a synapse when two neurons are repeatedly activated together.",
            "The amygdala tags experiences with emotional weight, making intense events especially memorable.",
            "Distributed practice spreads learning across many sessions instead of compressing it into one.",
            "Dual coding pairs words with images so that two independent cues can later trigger recall.",
            "The prefrontal cortex coordinates planning, focus, and the restraint of impulsive reactions.",
            "Anterograde amnesia blocks the formation of new memories while leaving older ones intact.",
            "The default mode network becomes active whenever the mind drifts into rest and reflection.",
            "Semantic encoding connects new ideas to the broad framework of knowledge you already hold.",
            "Working memory capacity reliably predicts performance on complex reasoning and problem solving.",
            "Interleaving different topics within a session improves long-term retention and transfer.",
            "Prospective memory allows you to remember to carry out an intended action at a future moment.",
            "Mirror neurons fire both when you perform an action and when you watch another perform it.",
            "Chunking groups scattered details into meaningful units that ease the load on memory.",
            "Grid cells in the entorhinal cortex form an internal coordinate system for spatial navigation.",
            "The phonological loop briefly rehearses verbal information to keep it available for use.",
            "Cognitive reserve, built through lifelong learning, helps the brain resist age-related decline.",
            "Reconsolidation reopens a recalled memory, allowing it to be updated before it stabilizes again.",
            "Neurogenesis continues in the adult hippocampus, supporting both new learning and mood regulation.",
            "The visuospatial sketchpad holds and manipulates images of shapes, locations, and movement.",
            "Deliberate practice targets specific weaknesses with focused effort and immediate feedback.",
            "Sleep spindles during light sleep appear to protect newly formed memories from interference.",
            "The forgetting curve shows that memory fades rapidly unless it is deliberately reviewed.",
            "Elaborative interrogation deepens learning by asking why a stated fact is actually true.",
            "Spaced repetition schedules each review just before a memory is likely to fade.",
            "Procedural memory stores well-practiced skills that operate with little conscious effort.",
            "The thalamus relays nearly all sensory signals on their way to the cortex.",
            "Acetylcholine supports attention and the encoding of new memories in the cortex.",
            "Chronic stress floods the brain with cortisol, which gradually impairs the hippocampus.",
            "Cognitive load theory warns that overloading attention prevents durable learning.",
            "The testing effect shows that retrieving information strengthens it more than rereading.",
            "Mental contrasting pairs a vivid goal with a clear view of the obstacles ahead.",
            "Bilingual experience appears to strengthen the brain networks that manage attention.",
            "Reflective journaling consolidates the day and reveals patterns in your own thinking."
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

        // 3. PARAGRAPHS (120 unique items)
        val paragraphPool = listOf(
            // EASY (40) — friendly, practical habits for a sharp mind
            "Your memory works best when your body is rested. A full night of sleep gives your brain time to sort and store what you learned. Aim for a steady bedtime so each day starts fresh.",
            "Focus is a skill you can train like a muscle. Start with short, distraction-free sessions and slowly make them longer. Over time, deep attention will feel natural.",
            "Drinking enough water keeps your mind clear and alert. Even mild thirst can slow your thinking and weaken focus. Keep a bottle nearby and sip throughout the day.",
            "A short walk can reset a tired brain. Gentle movement sends fresh oxygen to your mind and lifts your mood. After a walk, study often feels easier.",
            "Writing notes by hand helps ideas stick. As you shape each letter, your brain processes the meaning more deeply. Review those notes later to lock the learning in.",
            "Reading aloud uses both your eyes and your ears. This double path makes new words easier to remember. Try it whenever a sentence feels hard to recall.",
            "Curiosity makes learning fun and lasting. When you truly want to know something, your brain pays closer attention. Ask simple questions and chase the answers.",
            "Breaking a big goal into small steps keeps you moving. Each small win builds confidence and energy for the next. Progress feels lighter when the path is clear.",
            "A calm space helps your mind settle. Clear your desk, lower the noise, and keep only what you need. A tidy space invites tidy thinking.",
            "Teaching a friend is a powerful way to learn. Explaining an idea forces you to organize it clearly. If you can teach it simply, you truly understand it.",
            "Healthy food gives your brain steady fuel. Fruits, nuts, and vegetables support clear thinking and stable energy. What you eat shapes how well you focus.",
            "Deep, slow breathing calms a busy mind. A few quiet breaths lower stress and steady your heartbeat. Use this simple tool before any hard task.",
            "Small daily practice beats rare long sessions. A little effort each day keeps your skills warm and growing. Consistency is the quiet secret of mastery.",
            "Gratitude gently shifts your mood toward calm. Naming a few good things each night helps you sleep well. A peaceful mind remembers more.",
            "Mistakes are signs that you are learning. Each error shows you exactly where to improve next. Treat them as helpful guides, not failures.",
            "Morning sunlight helps set your body clock. Natural light lifts your mood and sharpens your focus. Step outside for a few minutes early in the day.",
            "One task at a time gives your best results. Switching between jobs scatters your attention and slows you down. Finish one thing before you start another.",
            "A good story makes facts easy to remember. Your brain holds images and meaning far better than plain lists. Turn dry details into a small story.",
            "Review just before sleep to help memory grow. As you rest, the brain quietly replays the day. A short evening review pays off by morning.",
            "Patience is part of every skill. New abilities take time to settle and feel natural. Keep going gently, and growth will come.",
            "A clear goal gives your day direction. When you know your target, choices become simpler. Write your goal where you can see it.",
            "Music without words can help you concentrate. Soft, steady sound blocks distractions and creates calm. Find a track that helps you settle in.",
            "Kindness builds trust and a steady heart. Gentle words ease tension in yourself and others. A calm heart supports a clear mind.",
            "Stretch and move after long sitting. Light movement loosens your body and wakes your brain. A quick stretch can restore your focus.",
            "Notice small details to sharpen your senses. Looking closely trains your mind to observe well. The more you notice, the more you remember.",
            "Set a simple routine to save your energy. Good habits run on their own and free your mind for harder work. Routines turn effort into ease.",
            "Rest your eyes during long study. Look at something far away for a few seconds now and then. Short breaks keep your focus from fading.",
            "Believe that your mind can grow. People who expect to improve try harder and learn more. Your effort shapes your ability.",
            "Keep a small notebook for new ideas. Writing a thought down clears space in your mind. You can return to it whenever you wish.",
            "Laughter eases stress and lightens the mind. A good laugh relaxes your body and renews your focus. Make room for joy in your day.",
            "Plan tomorrow before you sleep tonight. A short list calms the mind and prevents worry. You will wake up ready to begin.",
            "Practice in the same spot to build a habit. A regular place tells your brain it is time to focus. Soon, sitting down will switch you on.",
            "Speak slowly and clearly to be understood. Calm speech gives your listener time to follow. Clear words also help you think clearly.",
            "Limit bright screens before bed for better sleep. Late-night light confuses your body clock. Dim the lights and let your mind unwind.",
            "Cheer your small wins out loud. Noticing progress keeps your motivation strong. Every step forward deserves a quiet smile.",
            "Ask for help when a task feels stuck. A fresh view often reveals a simple path. Learning together is faster than struggling alone.",
            "Keep your goals realistic and kind. Gentle, steady targets are easier to reach. Success builds on success.",
            "A grateful pause can reset a hard day. Take one breath and name something good. Calm returns when you slow down.",
            "Try a new route or food now and then. Small novelty keeps your brain curious and awake. Variety quietly strengthens the mind.",
            "End each session by noting one thing you learned. This simple habit turns effort into lasting memory. Tiny reflections add up over time.",
            // MEDIUM (40) — applied learning science
            "Active recall means testing yourself instead of simply rereading. When you struggle to retrieve an answer, your brain strengthens the path to that memory. Each effort to remember makes the next recall easier.",
            "The spacing effect shows that learning sticks better when reviews are spread out. By revisiting material over days rather than hours, you interrupt the natural forgetting curve. The brain treats each return as important and stores it more firmly.",
            "Sleep is when much of learning is completed. During deep sleep the brain clears waste, and during dream sleep it replays new skills. A protected night of rest is one of the best study tools you have.",
            "Multitasking feels productive but quietly drains your focus. Each switch between tasks forces your brain to reload context and lose momentum. Choosing one task at a time protects both speed and accuracy.",
            "The memory palace links facts to places you already know well. By imagining each item along a familiar path, you turn abstract lists into a guided tour. Walking the route in your mind brings the items back in order.",
            "Stress in small doses can sharpen attention and drive. When stress becomes constant, however, cortisol begins to wear down memory. Learning to relax on purpose keeps your mind in its best range.",
            "Dual coding combines words with pictures to deepen memory. If the verbal cue fades, the visual image can still bring the idea back. Pairing a diagram with a definition gives you two ways to remember.",
            "A growth mindset treats ability as something you build, not something fixed. Believing you can improve makes you more willing to face hard practice. That willingness, over time, is what actually produces growth.",
            "Chunking groups small pieces of information into larger, meaningful units. A long string of numbers becomes easy when split into familiar groups. Your limited working memory handles a few chunks far better than many fragments.",
            "Reflection turns raw experience into durable learning. Spending a few minutes reviewing what worked helps the brain organize and store it. Without reflection, lessons fade as quickly as they arrive.",
            "Deep work depends on protecting long blocks of focus. Notifications and interruptions shatter concentration and reset your progress. Guarding even one uninterrupted hour can transform your output.",
            "The testing effect reveals that retrieval beats rereading. Every quiz, even a failed one, strengthens the underlying memory. Trying to recall is itself a powerful act of learning.",
            "Interleaving mixes related topics within a single study session. Although it feels harder than blocking one subject, it improves lasting retention. The brain learns to tell ideas apart and apply them flexibly.",
            "Exercise benefits the mind as much as the body. Movement increases blood flow and releases chemicals that support new connections. A regular walk or workout can lift both mood and memory.",
            "Attention is a limited spotlight, not a floodlight. Whatever you focus on grows clearer while everything else dims. Directing that spotlight on purpose is the heart of effective study.",
            "Emotion acts like a highlighter for memory. Events tied to strong feeling are recalled far more vividly than neutral ones. Adding meaning or curiosity to material makes it naturally easier to remember.",
            "A consistent routine conserves your mental energy. When habits handle the small decisions, your willpower is free for harder challenges. Structure quietly multiplies what you can achieve.",
            "Visualization rehearses success before it happens. Imagining each step in detail prepares the same brain regions that real action uses. Athletes and performers rely on this mental practice for good reason.",
            "Note-taking works best when you summarize in your own words. Copying text passively leaves little trace, but rephrasing forces understanding. The act of translating ideas is where real learning occurs.",
            "Boredom often appears at the edge of deeper focus. Instead of reaching for distraction, sit with the dull moment a little longer. Concentration frequently waits just past that first wave of restlessness.",
            "The brain craves novelty and pays attention to change. Studying in slightly different ways keeps the mind alert and engaged. Small variations can refresh material that has started to feel stale.",
            "Hydration has a direct effect on mental performance. Even mild dehydration can slow reaction time and weaken short-term memory. Keeping water within reach is a simple way to protect your focus.",
            "Self-explanation deepens understanding by filling in hidden steps. Asking yourself why each step follows the last reveals gaps in your knowledge. Closing those gaps builds a stronger, more flexible memory.",
            "Goals work best when they are specific and measurable. A vague wish gives the mind nothing to aim at, but a clear target guides action. Defining success in concrete terms makes progress visible.",
            "The first and last items in a list are easiest to recall. This pattern, known as the serial position effect, shapes how we remember sequences. Placing key material at the start or end can take advantage of it.",
            "Mindful breathing anchors a wandering mind in the present. Following the breath for a minute lowers stress and restores clarity. This brief practice can reset your focus before any demanding task.",
            "Feedback is the fuel of deliberate practice. Knowing quickly whether you were right lets you correct course while it still matters. Practice without feedback often repeats the same mistakes.",
            "Curiosity primes the brain to absorb information. When you genuinely want an answer, the reward system makes learning feel rewarding. Turning a topic into a question can switch on that drive.",
            "Overlearning means practicing a skill past the point of first success. This extra repetition makes the skill more automatic and resistant to stress. Under pressure, well-practiced skills hold steady.",
            "Environment shapes behavior more than willpower does. Keeping distractions out of sight makes focus the easy default. Designing your space well removes many battles before they start.",
            "A brief daytime nap can refresh learning capacity. Twenty minutes of rest clears mental fatigue without leaving you groggy. Used wisely, a short nap restores attention for the afternoon.",
            "Writing a worry down can quiet an anxious mind. Putting the thought on paper moves it out of your working memory. With the worry parked, attention returns to the task at hand.",
            "Comparing yourself to your past self fuels steady growth. Measuring progress against others often breeds discouragement instead. Your own earlier work is the fairest and most useful benchmark.",
            "Reading widely strengthens the network of ideas in your mind. New facts attach more easily when they meet related knowledge. A broad foundation makes future learning faster and richer.",
            "The brain consolidates skills best with rest between sessions. Pushing nonstop yields diminishing returns as fatigue sets in. Short breaks let the mind absorb what hard practice planted.",
            "Habits form through cue, routine, and reward. Recognizing the cue that triggers a behavior gives you power to reshape it. Replace the routine while keeping the reward, and change becomes easier.",
            "Confidence grows from a record of completed challenges. Each finished task becomes evidence that you can handle the next. Collecting small victories builds durable self-belief.",
            "Slow, deliberate reading improves comprehension and recall. Racing through a page leaves only a shallow impression. Pausing to picture and question the text plants it more firmly.",
            "A clear morning intention shapes the entire day. Naming your single most important task focuses your energy. When priorities are set early, distractions lose their pull.",
            "Gratitude practice gradually rewires attention toward the positive. Regularly noticing what is going well lowers stress and steadies mood. A calmer mind, in turn, learns and remembers with greater ease.",
            // HARD (40) — rigorous neuroscience and the science of learning
            "The hippocampus serves as a temporary index for new declarative memories before they are gradually consolidated into the neocortex. While it binds the scattered elements of an experience together, long-term storage is distributed across cortical networks. Damage to this region disrupts the formation of new episodic memories while sparing well-practiced procedural skills.",
            "Long-term potentiation is widely regarded as a core cellular mechanism of learning. When a presynaptic neuron repeatedly and reliably helps fire a postsynaptic neuron, the connection between them strengthens. This durable change in synaptic efficiency allows patterns of activity to leave a lasting physical trace.",
            "Neuroplasticity describes the brain's capacity to reorganize its connections in response to experience. New skills and repeated practice can reshape both the strength of synapses and the maps that represent the body. This adaptability persists throughout life, which is why deliberate training continues to change the adult brain.",
            "Working memory operates under a central executive that allocates attention between competing demands. The phonological loop rehearses verbal material while the visuospatial sketchpad maintains images and locations. Because this system has a strict capacity limit, overloading it causes information to be dropped before it can be used.",
            "The forgetting curve, first charted by Ebbinghaus, shows that memory decays rapidly without reinforcement. Spaced repetition counteracts this decay by scheduling each review near the point of likely forgetting. By repeatedly reconstructing the memory at widening intervals, the curve is flattened and retention deepens.",
            "Metacognition is the capacity to monitor and regulate one's own cognitive processes. Skilled learners constantly assess whether their current strategy is working and adjust it when it is not. This self-aware control over learning often separates expert performers from novices who rely on effort alone.",
            "The amygdala modulates how strongly an experience is encoded according to its emotional significance. By signaling the hippocampus during moments of arousal, it ensures that survival-relevant events are remembered vividly. This is why deeply meaningful moments are recalled with unusual clarity and detail.",
            "Sleep contributes to memory through distinct stages that serve complementary functions. Slow-wave sleep supports the consolidation of factual, declarative knowledge, while rapid eye movement sleep favors procedural and emotional integration. Depriving the brain of either stage measurably weakens the learning that preceded it.",
            "Synaptic pruning refines neural circuits by eliminating weak or redundant connections during development and beyond. This selective removal sharpens the pathways that are used frequently and discards those that are not. The result is a more efficient network tuned to the demands the brain actually encounters.",
            "Cognitive load theory distinguishes the intrinsic difficulty of material from the extraneous load imposed by poor presentation. Because working memory is sharply limited, unnecessary complexity crowds out the capacity needed for genuine understanding. Well-designed instruction reduces extraneous load so that effort can be spent on meaningful learning.",
            "The entorhinal cortex houses grid cells that fire in a regular triangular lattice as the body moves through space. Together with place cells in the hippocampus, they form an internal coordinate system for navigation and memory. This spatial scaffolding may also help organize non-spatial information into structured, retrievable maps.",
            "Reconsolidation reveals that recalling a memory can render it temporarily unstable and open to revision. When an old memory is reactivated, it must be restabilized, and during that window it can be strengthened or altered. This mechanism explains how memories subtly change each time they are revisited.",
            "The prefrontal cortex orchestrates executive functions such as planning, working memory, and inhibitory control. By suppressing irrelevant impulses, it allows goal-directed behavior to override automatic reactions. Its slow maturation through adolescence parallels the gradual development of self-regulation and judgment.",
            "Dual-coding theory proposes that information processed both verbally and visually is encoded through two independent channels. Because either channel can later cue retrieval, this redundancy makes the memory more robust. Combining a clear explanation with a meaningful image therefore yields stronger recall than words alone.",
            "The testing effect demonstrates that the act of retrieval is itself a potent learning event. Each successful recall strengthens the memory trace more than an equivalent period of passive review. Even unsuccessful attempts, when followed by feedback, prepare the mind to encode the correct answer.",
            "Neurogenesis continues in the adult dentate gyrus, where new neurons are integrated into existing hippocampal circuits. These young cells appear to support pattern separation, helping the brain distinguish similar experiences. Exercise and enriched environments increase this birth of neurons, while chronic stress suppresses it.",
            "Selective attention filters the flood of sensory input so that limited processing resources reach what matters. The cocktail party effect shows how we can track one voice among many while still detecting our own name elsewhere. This balance between focus and vigilance reflects a finely tuned attentional system.",
            "Interleaving practice forces the learner to repeatedly retrieve and discriminate between related skills. Although it feels less fluent than massed practice, it builds the flexibility to apply knowledge in varied contexts. The added difficulty is desirable because it strengthens durable, transferable learning.",
            "Acetylcholine plays a central role in attention and in the encoding of new cortical memories. By increasing the signal-to-noise ratio in sensory regions, it helps the brain prioritize relevant input. Disruption of cholinergic systems is closely linked to the memory deficits seen in certain disorders.",
            "The default mode network becomes active during rest, mind-wandering, and reflection on the self. Rather than being idle, this network supports autobiographical memory and the simulation of future events. Its balance with task-focused networks shapes whether attention turns inward or outward.",
            "Procedural memory underlies skills that become automatic through extensive practice, such as typing or playing an instrument. It depends heavily on the basal ganglia and cerebellum rather than the hippocampus. Because it operates largely outside awareness, these skills can survive even when declarative memory fails.",
            "Cognitive reserve describes the brain's resilience against injury and age-related change. Built through education, complex work, and lifelong mental engagement, it allows alternative networks to compensate for damage. Individuals with greater reserve often maintain function despite significant underlying pathology.",
            "Elaborative encoding strengthens memory by connecting new material to a rich web of existing knowledge. The more meaningful associations a fact acquires, the more retrieval routes lead back to it. This is why understanding something deeply makes it far easier to recall than rote memorization.",
            "Chronic activation of the stress response floods the brain with glucocorticoids that impair hippocampal function. Sustained cortisol exposure can shrink dendrites and interfere with the formation of new memories. Managing stress is therefore not only emotional self-care but a direct investment in cognition.",
            "The serial position effect produces superior recall for items at the beginning and end of a list. Primacy reflects extra rehearsal of early items, while recency reflects their lingering presence in working memory. The weaker middle region reveals the limits of both consolidation and short-term storage.",
            "Deliberate practice is distinguished by focused effort on specific weaknesses guided by immediate feedback. Rather than simply repeating what is comfortable, it targets the edge of current ability. This demanding, feedback-rich approach is what transforms ordinary practice into genuine expertise.",
            "Pattern separation and pattern completion are complementary operations performed by the hippocampus. Separation keeps similar experiences distinct, preventing interference between overlapping memories. Completion allows a partial cue to reconstruct an entire memory, which is the basis of associative recall.",
            "The brain encodes time as well as content, allowing memories to be placed in sequence. Specialized cells fire at particular moments within an experience, creating a temporal scaffold. This sense of when events occurred is essential to the structure of episodic memory.",
            "Myelination wraps axons in an insulating sheath that dramatically increases the speed of neural signaling. As skills are practiced, activity-dependent myelination fine-tunes the timing of communication between regions. This often-overlooked form of plasticity contributes to the smooth automaticity of well-learned abilities.",
            "Semantic memory stores general facts and concepts independent of the moment they were acquired. Over time, repeated episodic experiences are distilled into this stable, schematic knowledge. The gradual shift from episodic detail to semantic gist reflects the brain's drive toward efficient generalization.",
            "Desirable difficulties are conditions that slow learning in the moment but strengthen it in the long run. Spacing, interleaving, and retrieval all feel harder than passive review yet yield deeper retention. Confusing immediate fluency with durable learning is one of the most common study mistakes.",
            "The brain's reward system, driven largely by dopamine, signals the gap between expectation and outcome. Surprising rewards generate strong prediction errors that reinforce the preceding behavior. This mechanism links motivation tightly to learning, shaping which actions are repeated and remembered.",
            "Mental imagery activates many of the same visual and motor regions engaged during actual perception and movement. Because the brain partly treats imagined rehearsal like real experience, visualization can refine genuine skill. This overlap explains why detailed mental practice measurably improves performance.",
            "The phonological loop maintains verbal information through silent, internal rehearsal that refreshes a fading trace. Its limited duration is why long numbers slip away unless they are repeated or grouped. Muttering an unrelated word reliably disrupts this rehearsal and demonstrates how fragile the loop is.",
            "Habits are encoded as action sequences in the basal ganglia that run with minimal conscious oversight. Once a cue reliably triggers a routine, the behavior becomes efficient but also resistant to change. Reshaping a habit usually means redesigning its cue and reward rather than relying on willpower.",
            "Attention and memory are deeply intertwined, since unattended information is rarely encoded at all. What feels like forgetting is often a failure to have noticed in the first place. Directing focused attention during learning is therefore a prerequisite for later recall.",
            "The cerebellum, long associated with movement, also contributes to the timing and coordination of cognition. It helps fine-tune predictions and smooth the execution of both physical and mental sequences. Its role illustrates how regions once thought narrow turn out to support broad functions.",
            "Spaced retrieval combines two of the most powerful principles in the science of learning. By testing memory at expanding intervals, it harnesses both the spacing effect and the testing effect together. This pairing produces some of the most durable retention that research has documented.",
            "Emotional regulation depends on a dynamic balance between the prefrontal cortex and the limbic system. When regulation succeeds, the cortex dampens reactive signals from the amygdala and restores deliberate control. Strengthening this balance improves not only mood but the clarity of thought under pressure.",
            "The consolidation of memory unfolds across multiple timescales, from minutes to years. Rapid synaptic consolidation stabilizes a trace shortly after learning, while systems consolidation slowly reorganizes it across the cortex. This extended process is why a good night of sleep and later review both strengthen what was learned."
        )
        for (i in 0 until 120) {
            val paragraph = paragraphPool[i % paragraphPool.size]
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
            items.add(PracticeItem(type = "PARAGRAPH", content = paragraph, difficulty = diff, category = category, isCustom = false, chapter = chapter))
        }
        return items
    }

    // ----- Asset-bundled, multi-language content library -------------------------------------
    // The English core ships in Kotlin (proven & always available); every other language — plus
    // the fun Story/Poem/Humor categories — is loaded from assets/content/<code>.json. Adding a
    // language is therefore a pure content drop, no code change required.

    private val DEFAULT_ENGLISH = LanguageOption("en", "English", "English", "en-US")

    private fun readAsset(path: String): String? = try {
        appContext.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
    } catch (e: Exception) {
        Log.w("MemoryRepository", "Asset not found or unreadable: $path", e)
        null
    }

    /** Languages declared in assets/content/manifest.json. Always includes English as a baseline. */
    fun getAvailableLanguages(): List<LanguageOption> {
        val result = linkedMapOf(DEFAULT_ENGLISH.code to DEFAULT_ENGLISH)
        try {
            val json = readAsset("content/manifest.json")
            if (json != null) {
                val manifest = moshi.adapter(ContentManifest::class.java).fromJson(json)
                manifest?.languages?.forEach { meta ->
                    result[meta.code] = LanguageOption(
                        code = meta.code,
                        name = meta.name,
                        nativeName = meta.nativeName.ifBlank { meta.name },
                        localeTag = meta.localeTag?.ifBlank { null } ?: meta.code
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("MemoryRepository", "Failed to parse content manifest", e)
        }
        return result.values.toList()
    }

    /** Reads every per-language JSON file and flattens it into PracticeItems tagged by language. */
    private fun loadJsonContentItems(languages: List<LanguageOption>): List<PracticeItem> {
        val adapter = moshi.adapter(LanguageContent::class.java)
        val items = mutableListOf<PracticeItem>()
        languages.forEach { lang ->
            val json = readAsset("content/${lang.code}.json") ?: return@forEach
            try {
                val parsed = adapter.fromJson(json) ?: return@forEach
                parsed.entries.forEach { e ->
                    if (e.content.isNotBlank()) {
                        items.add(
                            PracticeItem(
                                type = e.type.uppercase(Locale.US),
                                content = e.content.trim(),
                                difficulty = e.difficulty.uppercase(Locale.US),
                                category = e.category,
                                isCustom = false,
                                chapter = e.chapter.coerceIn(1, 10),
                                language = lang.code
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MemoryRepository", "Failed to parse content/${lang.code}.json", e)
            }
        }
        return items
    }

    /** Full built-in library = English Kotlin core + everything declared in the asset JSON files. */
    private fun buildFullLibrary(): List<PracticeItem> {
        val core = generateCoreExercises() // English baseline, language = "en"
        val json = loadJsonContentItems(getAvailableLanguages())
        return core + json
    }

    // Check database state and prepopulate if empty
    suspend fun checkAndPrepopulate() {
        withContext(Dispatchers.IO) {
            try {
                // Re-seed the built-in syllabus whenever the on-device content set does not
                // match the current curated library. This refreshes existing installs to the
                // latest exercises WITHOUT touching the user's custom items, logs, XP or streak.
                val coreItems = buildFullLibrary()
                if (dao.getNonCustomItemsCount() != coreItems.size) {
                    dao.deleteNonCustomItems()
                    dao.insertPracticeItems(coreItems)
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
