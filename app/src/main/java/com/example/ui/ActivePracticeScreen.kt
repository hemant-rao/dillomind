package com.example.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PracticeItem
import com.example.viewmodel.MemoryViewModel
import com.example.viewmodel.PracticeResult
import java.util.*

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val speed: Float,
    val sway: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val rotationSpeed: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivePracticeScreen(
    viewModel: MemoryViewModel,
    item: PracticeItem,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current
    val spokenText by viewModel.spokenText.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val practiceResult by viewModel.practiceResult.collectAsState()
    val wasRestored by viewModel.wasRestored.collectAsState()

    var textHidden by remember(item.id) { mutableStateOf(false) }
    val startTimeMs = remember(item.id) { System.currentTimeMillis() }
    val scrollState = rememberScrollState()

    var confettiParticles by remember(item.id) { mutableStateOf(emptyList<ConfettiParticle>()) }

    LaunchedEffect(practiceResult) {
        val res = practiceResult
        if (res != null) {
            if (res.accuracyScore >= 70) {
                // Play success audio
                SoundSynth.playSuccess()

                // Initialize 55 confetti particles with random properties
                val random = java.util.Random()
                val colors = listOf(
                    Color(0xFF13B4A2), // Xello Teal
                    Color(0xFFFFD600), // Gold
                    Color(0xFFFF4081), // Pink red
                    Color(0xFF2979FF), // Bright Blue
                    Color(0xFF00E676)  // Green
                )

                confettiParticles = List(55) {
                    ConfettiParticle(
                        x = random.nextFloat(),
                        y = -50f - random.nextFloat() * 200f,
                        speed = 4f + random.nextFloat() * 7f,
                        sway = 0.5f + random.nextFloat() * 1.5f,
                        color = colors[random.nextInt(colors.size)],
                        size = 12f + random.nextFloat() * 16f,
                        rotation = random.nextFloat() * 360f,
                        rotationSpeed = (random.nextFloat() - 0.5f) * 8f
                    )
                }

                // Smooth 60fps frame updates loop
                val startMs = System.currentTimeMillis()
                while (System.currentTimeMillis() - startMs < 4200 && confettiParticles.isNotEmpty()) {
                    kotlinx.coroutines.delay(16)
                    confettiParticles = confettiParticles.map { p ->
                        p.copy(
                            y = p.y + p.speed,
                            x = (p.x + Math.sin(p.y.toDouble() / 40.0).toFloat() * 0.003f).coerceIn(0f, 1f),
                            rotation = p.rotation + p.rotationSpeed
                        )
                    }
                }
                confettiParticles = emptyList()
            } else {
                // Play error/needs-practice audio
                SoundSynth.playError()
            }
        } else {
            confettiParticles = emptyList()
        }
    }

    // Activity launcher for Android native Speech Recognizer
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.updateRecordingState(false)
        
        // Haptic feedback stop pattern
        try {
            hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            }
            vibrator?.let {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    it.vibrate(android.os.VibrationEffect.createOneShot(120, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(120)
                }
            }
        } catch (e: Exception) {}

        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val wordsList = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = wordsList?.firstOrNull() ?: ""
            if (recognizedText.isNotEmpty()) {
                viewModel.setSpokenText(recognizedText)
            } else {
                Toast.makeText(context, "Voice recognition empty. Try typing below!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Voice input canceled. You can write your response too!", Toast.LENGTH_SHORT).show()
        }
    }

    // Launch speech trigger intent
    val startSpeechToText = {
        // Haptic feedback start pattern (double pulse)
        try {
            hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            }
            vibrator?.let {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val timings = longArrayOf(0, 50, 50, 50)
                    val amplitudes = intArrayOf(0, android.os.VibrationEffect.DEFAULT_AMPLITUDE, 0, android.os.VibrationEffect.DEFAULT_AMPLITUDE)
                    it.vibrate(android.os.VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(longArrayOf(0, 50, 50, 50), -1)
                }
            }
        } catch (e: Exception) {}

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Read the text out loud now...")
        }
        try {
            viewModel.updateRecordingState(true)
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            viewModel.updateRecordingState(false)
            Toast.makeText(context, "Google voice typing not supported. Use manual input box below!", Toast.LENGTH_LONG).show()
        }
    }

    // Audio record permission launcher
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startSpeechToText()
        } else {
            Toast.makeText(context, "Microphone permission is required to use voice typing. Please type your answer instead!", Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("active_practice_screen")
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Practice Session",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Type details badge
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when (item.type) {
                            "WORD" -> "Words"
                            "SENTENCE" -> "Sentences"
                            "PARAGRAPH" -> "Paragraphs"
                            else -> item.type
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Check if restored from an autosave
            AnimatedVisibility(
                visible = wasRestored,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Restored",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Autosave Progress Loaded",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "We restored your last in-progress speech transcription screen.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Step 1 prompt: Read text
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "STEP 1: READ & MEMORIZE",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Examine the context lines carefully, read them aloud, and register them in your active working memory.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // The Practice Box card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target Cue",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelMedium
                        )

                        // Hide Text Toggle for Active memory testing
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { textHidden = !textHidden }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (textHidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (textHidden) "Show Text" else "Hide (Test Memory)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (textHidden) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Text Hidden for Recall Testing",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Recite the text from your short-term memory memory bank!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Text(
                                text = item.content,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
            }

            // Step 2 prompt: Voice Practice
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "STEP 2: RECORD RECALL",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Tap on the microphone to speak, or write down your memory reconstruction in the editor below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Big record mechanism (Tuned for maximum tactile accessibility and visual priority)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Record Button with Multi-Ring Ripple Simulation for rich visual feedback
                    Box(
                        modifier = Modifier
                            .size(136.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRecording) MaterialTheme.colorScheme.error.copy(alpha = 0.18f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            )
                            .clickable {
                                if (androidx.core.content.ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.RECORD_AUDIO
                                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                ) {
                                    startSpeechToText()
                                } else {
                                    recordAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                }
                            }
                            .testTag("record_mic_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner secondary outer-ring glow
                        Box(
                            modifier = Modifier
                                .size(112.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isRecording) MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Core tactile button
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isRecording) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.primary
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isRecording) Icons.Default.KeyboardVoice else Icons.Default.Mic,
                                    contentDescription = "Speech recognition start",
                                    tint = Color.White,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isRecording) "Listening..." else "Tap Mic to Speak",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium,
                        letterSpacing = 0.5.sp,
                        color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )

                    // Text representation / Manual Editor
                    OutlinedTextField(
                        value = spokenText,
                        onValueChange = { viewModel.setSpokenText(it) },
                        label = { Text("What You Spoke", fontWeight = FontWeight.Bold) },
                        placeholder = { Text("What you spoke appears here. You can edit this directly if needed...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vocal_editor_input"),
                        minLines = 2,
                        maxLines = 5,
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Scoring buttons
                    Button(
                        onClick = { viewModel.submitPractice(startTimeMs) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("submit_practice_btn"),
                        enabled = spokenText.trim().isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("Submit Recall Assessment", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // Overlay Results Dialog
        if (practiceResult != null) {
            val result = practiceResult!!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { /* Block clicks underlying background */ },
                contentAlignment = Alignment.Center
            ) {
                // Render custom lightweight celebratory confetti
                if (confettiParticles.isNotEmpty()) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        confettiParticles.forEach { p ->
                            val px = p.x * size.width
                            val py = p.y
                            if (py in 0f..size.height) {
                                rotate(p.rotation, pivot = Offset(px, py)) {
                                    drawRect(
                                        color = p.color,
                                        topLeft = Offset(px - p.size / 2, py - p.size / 2),
                                        size = Size(p.size, p.size)
                                    )
                                }
                            }
                        }
                    }
                }

                ResultReportCard(
                    result = result,
                    onDismiss = { viewModel.setTab("dashboard") },
                    onRetry = { viewModel.startPractice(item) },
                    onStartNext = { viewModel.startNextExercise() }
                )
            }
        }
    }
}

@Composable
fun ResultReportCard(
    result: PracticeResult,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onStartNext: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .testTag("result_report_box")
            .padding(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Practice Results",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )

            // Circular Meter
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                val accentColor = when (result.accuracyScore) {
                    in 90..100 -> Color(0xFF00E676)
                    in 75..89 -> Color(0xFF2196F3)
                    else -> Color(0xFFFF9100)
                }

                CircularProgressIndicator(
                    progress = { result.accuracyScore / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = accentColor,
                    strokeWidth = 8.dp,
                    trackColor = accentColor.copy(alpha = 0.15f)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${result.accuracyScore}%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor
                    )
                    Text(
                        text = "Accuracy",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Rewards Notification Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "+${result.xpEarned} Points",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Cognitive Points",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                if (result.streakIncremented) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "STREAK!",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFF9100)
                        )
                        Text(
                            text = "Daily Habit Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // 🎙️ Speaking Performance Metrics
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Speaking Performance",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        val durationSeconds = result.durationMs / 1000.0
                        Text(
                            text = "${"%.1f".format(durationSeconds)}s duration",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${result.wpm} WPM",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Speaking Rate",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        // Verbal cadence classification
                        val speedRating = when {
                            result.wpm == 0 -> "Not detected"
                            result.wpm < 110 -> "Measured & Steady"
                            result.wpm in 110..150 -> "Optimal & Conversational"
                            else -> "Fluent & Fast Cadence"
                        }
                        
                        val speedColor = when {
                            result.wpm == 0 -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            result.wpm < 110 -> Color(0xFF2196F3)
                            result.wpm in 110..150 -> Color(0xFF00E676)
                            else -> Color(0xFFFF9100)
                        }

                        Surface(
                            color = speedColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = speedRating,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = speedColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Word Comparison Logs
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Word Comparison",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                TextLayoutDetail(
                    title = "Expected phrase:",
                    content = result.originalText,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                TextLayoutDetail(
                    title = "Your spoken words:",
                    content = result.recognizedText,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Interactive bottom actions organized hierarchically
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onStartNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("result_next_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Advance to Next Exercise", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRetry,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("result_retry_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retry", maxLines = 1, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("result_dismiss_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Finish", maxLines = 1, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TextLayoutDetail(
    title: String,
    content: String,
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
            .padding(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
