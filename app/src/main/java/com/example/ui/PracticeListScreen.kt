package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PracticeItem
import com.example.viewmodel.MemoryViewModel
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.BorderStroke
import com.example.data.PracticeLog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeListScreen(
    viewModel: MemoryViewModel,
    items: List<PracticeItem>,
    modifier: Modifier = Modifier
) {
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedDiff by viewModel.selectedDifficulty.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val hasAutosave by viewModel.hasAutosave.collectAsState()
    val logs by viewModel.allLogs.collectAsState()
    val unlockedChapters by viewModel.unlockedChapters.collectAsState()

    var showAddCustomDialog by remember { mutableStateOf(false) }
    var showAdvancedFilters by remember { mutableStateOf(false) }

    // Derive list based on filters
    val filteredItems = remember(items, selectedType, selectedDiff, selectedCategory) {
        items.filter { item ->
            val matchType = item.type == selectedType
            val matchDiff = selectedDiff == "ALL" || item.difficulty == selectedDiff
            val matchCat = selectedCategory == "ALL" || item.category == selectedCategory
            matchType && matchDiff && matchCat
        }.sortedWith(
            compareByDescending<PracticeItem> { it.isCustom }
                .thenBy {
                    when (it.difficulty.uppercase()) {
                        "HARD" -> 1
                        "MEDIUM" -> 2
                        "EASY" -> 3
                        else -> 4
                    }
                }
                .thenByDescending { it.id }
        )
    }

    Scaffold(
        modifier = modifier.testTag("practice_list_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCustomDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_custom_fab")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add custom phrase"
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val isWide = maxWidth >= 600.dp

            if (isWide) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Pane (Tracker, Autosave recovery & Active Featured Workout)
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 📝 Autosave recovery banner
                        if (hasAutosave) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.95f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = "Recovery session",
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Resume In-Progress Practice",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Text(
                                            text = "You have unsaved speech-to-text transcriptions from your last session.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { viewModel.resumeAutosavedSession() },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                                    contentColor = MaterialTheme.colorScheme.onTertiary
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(28.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                            ) {
                                                Text("Resume", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            TextButton(
                                                onClick = { viewModel.clearAutosavedSession() },
                                                modifier = Modifier.height(28.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                                colors = ButtonDefaults.textButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                                )
                                            ) {
                                                Text("Discard", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Weekly tracker progress metrics
                        WeeklyCelebrationChart(logs = logs)

                        // Featured active recall card
                        val featuredItem = remember(filteredItems) { filteredItems.firstOrNull() }
                        featuredItem?.let { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(24.dp),
                                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "ACTIVE RECALL WORKOUT",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    Text(
                                        text = item.content,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 22.sp
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            val displayDiff = when(item.difficulty.uppercase()) {
                                                "EASY" -> "Beginner"
                                                "MEDIUM" -> "Intermediate"
                                                "HARD" -> "Advanced"
                                                else -> item.difficulty
                                            }
                                            Text(
                                                text = item.category + " · " + displayDiff,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Button(
                                            onClick = { viewModel.startPractice(item) },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            ),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                            modifier = Modifier.testTag("featured_start_btn")
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Text("Begin Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Right Pane (Filter control pill & Scrollable List)
                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("PARAGRAPH", "SENTENCE", "WORD").forEach { type ->
                                    val isSelected = selectedType == type
                                    val label = when (type) {
                                        "WORD" -> "Words"
                                        "SENTENCE" -> "Sentences"
                                        else -> "Paragraphs"
                                    }
                                    val icon = when (type) {
                                        "WORD" -> Icons.Filled.Abc
                                        "SENTENCE" -> Icons.Filled.ShortText
                                        else -> Icons.Filled.Notes
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                            )
                                            .clickable { viewModel.setTypeFilter(type) }
                                            .padding(vertical = 8.dp)
                                            .testTag("tab_$type"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = label,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Available Exercises",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${filteredItems.size} items found",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }

                            val filtersActive = selectedDiff != "ALL" || selectedCategory != "ALL"
                            FilterChip(
                                selected = showAdvancedFilters,
                                onClick = { showAdvancedFilters = !showAdvancedFilters },
                                label = {
                                    Text(
                                        text = if (filtersActive) "Filters Active" else "Filters",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Filters",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }

                        AnimatedVisibility(
                            visible = showAdvancedFilters,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Difficulty Row Selector
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Difficulty:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )

                                    LazyRow(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        contentPadding = PaddingValues(horizontal = 2.dp)
                                    ) {
                                        val levels = listOf("EASY", "MEDIUM", "HARD")
                                        items(levels) { level ->
                                            val isSelected = selectedDiff == level
                                            val chipColor = when (level) {
                                                "EASY" -> Color(0xFF0D9488)
                                                "MEDIUM" -> Color(0xFF0EA5E9)
                                                "HARD" -> Color(0xFFF43F5E)
                                                else -> MaterialTheme.colorScheme.primary
                                            }
                                            val levelLabel = when(level) {
                                                "EASY" -> "Beginner"
                                                "MEDIUM" -> "Intermediate"
                                                "HARD" -> "Advanced"
                                                else -> "All Levels"
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(
                                                        if (isSelected) chipColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                                    )
                                                    .clickable { viewModel.setDifficultyFilter(level) }
                                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(12.dp),
                                                            tint = chipColor
                                                        )
                                                    }
                                                    Text(
                                                        text = levelLabel,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) chipColor else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Dynamic Category Filter
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Category:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )

                                    LazyRow(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        contentPadding = PaddingValues(horizontal = 2.dp)
                                    ) {
                                        val categories = listOf("ALL", "Wisdom", "Science", "Focus", "Visual", "General")
                                        items(categories) { category ->
                                            val isSelected = selectedCategory == category
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                                                    )
                                                    .clickable { viewModel.setCategoryFilter(category) }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = category,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(bottom = 80.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ChapterWisePracticeView(
                                    items = items,
                                    logs = logs,
                                    unlockedChapters = unlockedChapters,
                                    selectedDiff = selectedDiff,
                                    selectedType = selectedType,
                                    selectedCategory = selectedCategory,
                                    onStartPractice = { viewModel.startPractice(it) },
                                    onDeleteItem = { viewModel.deleteItem(it) }
                                )
                            }
                        }
                    }
                }
            } else {
                // Traditional compact single column layout with unified vertical scroll
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 📝 Autosave Session Recovery Banner
                    if (hasAutosave) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.95f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "Recovery session",
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Resume In-Progress Practice",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = "You have unsaved speech-to-text transcriptions from your last session.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.resumeAutosavedSession() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.tertiary,
                                                contentColor = MaterialTheme.colorScheme.onTertiary
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(28.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                        ) {
                                            Text("Resume", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        TextButton(
                                            onClick = { viewModel.clearAutosavedSession() },
                                            modifier = Modifier.height(28.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                            )
                                        ) {
                                            Text("Discard", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // D3/Recharts weekly sync tracker
                    WeeklyCelebrationChart(logs = logs)

                    // Exercise Type Selector (Vocabulary / Sentences / Complex Paragraphs) in a Premium Segmented Pill
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("PARAGRAPH", "SENTENCE", "WORD").forEach { type ->
                                val isSelected = selectedType == type
                                val label = when (type) {
                                    "WORD" -> "Words"
                                    "SENTENCE" -> "Sentences"
                                    else -> "Paragraphs"
                                }
                                val icon = when (type) {
                                    "WORD" -> Icons.Filled.Abc
                                    "SENTENCE" -> Icons.Filled.ShortText
                                    else -> Icons.Filled.Notes
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                        )
                                        .clickable { viewModel.setTypeFilter(type) }
                                        .padding(vertical = 8.dp)
                                        .testTag("tab_$type"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 🌟 HIGH-PRIORITY DAILY RECITE RECALL ACTIVE WORKOUT CARD
                    val featuredItem = remember(filteredItems) { filteredItems.firstOrNull() }
                    featuredItem?.let { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "ACTIVE RECALL WORKOUT",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Text(
                                    text = item.content,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 22.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        val displayDiff = when(item.difficulty.uppercase()) {
                                            "EASY" -> "Beginner"
                                            "MEDIUM" -> "Intermediate"
                                            "HARD" -> "Advanced"
                                            else -> item.difficulty
                                        }
                                        Text(
                                            text = item.category + " · " + displayDiff,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Button(
                                        onClick = { viewModel.startPractice(item) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                        modifier = Modifier.testTag("featured_start_btn")
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Text("Begin Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Results Counter and Advanced Filters Toggle Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Available Exercises",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${filteredItems.size} items found",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                        }

                        val filtersActive = selectedDiff != "ALL" || selectedCategory != "ALL"
                        FilterChip(
                            selected = showAdvancedFilters,
                            onClick = { showAdvancedFilters = !showAdvancedFilters },
                            label = {
                                Text(
                                    text = if (filtersActive) "Filters Active" else "Filters",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filters",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }

                    AnimatedVisibility(
                        visible = showAdvancedFilters,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Difficulty Row Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Difficulty:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )

                                LazyRow(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp)
                                ) {
                                    val levels = listOf("EASY", "MEDIUM", "HARD")
                                    items(levels) { level ->
                                        val isSelected = selectedDiff == level
                                        val chipColor = when (level) {
                                            "EASY" -> Color(0xFF0D9488)
                                            "MEDIUM" -> Color(0xFF0EA5E9)
                                            "HARD" -> Color(0xFFF43F5E)
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                        val levelLabel = when(level) {
                                            "EASY" -> "Beginner"
                                            "MEDIUM" -> "Intermediate"
                                            "HARD" -> "Advanced"
                                            else -> "All Levels"
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(
                                                    if (isSelected) chipColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                                )
                                                .clickable { viewModel.setDifficultyFilter(level) }
                                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(12.dp),
                                                        tint = chipColor
                                                    )
                                                }
                                                Text(
                                                    text = levelLabel,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) chipColor else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Dynamic Category Filter
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Category:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )

                                LazyRow(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp)
                                ) {
                                    val categories = listOf("ALL", "Wisdom", "Science", "Focus", "Visual", "General")
                                    items(categories) { category ->
                                        val isSelected = selectedCategory == category
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                                                )
                                                .clickable { viewModel.setCategoryFilter(category) }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = category,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    ChapterWisePracticeView(
                        items = items,
                        logs = logs,
                        unlockedChapters = unlockedChapters,
                        selectedDiff = selectedDiff,
                        selectedType = selectedType,
                        selectedCategory = selectedCategory,
                        onStartPractice = { viewModel.startPractice(it) },
                        onDeleteItem = { viewModel.deleteItem(it) }
                    )
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Add Custom Dialog
    if (showAddCustomDialog) {
        var customContent by remember { mutableStateOf("") }
        var customDifficulty by remember { mutableStateOf("EASY") }
        var customCategory by remember { mutableStateOf("General") }

        AlertDialog(
            onDismissRequest = { showAddCustomDialog = false },
            title = {
                Text(
                    text = "Add Practice Phrase",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = customContent,
                        onValueChange = { customContent = it },
                        label = { Text("Practice Context / Phrase") },
                        placeholder = { Text("Type or paste your customizable memory phrase here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_phrase_input"),
                        minLines = 4,
                        maxLines = 8,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Choose Difficulty Selector - Beautifully listed VERTICALLY to prevent wrapping!
                    Text(
                        text = "Set Difficulty Tier",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("EASY", "MEDIUM", "HARD").forEach { diff ->
                            val selected = customDifficulty == diff
                            val (diffLabel, diffColor, diffIcon) = when(diff) {
                                "EASY" -> Triple("Beginner (Easy)", Color(0xFF0D9488), Icons.Default.Face)
                                "MEDIUM" -> Triple("Intermediate (Medium)", Color(0xFF0EA5E9), Icons.Default.Star)
                                "HARD" -> Triple("Advanced (Hard)", Color(0xFFF43F5E), Icons.Default.FlashOn)
                                else -> Triple(diff, MaterialTheme.colorScheme.primary, Icons.Default.Star)
                            }
                            Surface(
                                onClick = { customDifficulty = diff },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) diffColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (selected) diffColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = diffIcon,
                                            contentDescription = null,
                                            tint = if (selected) diffColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = diffLabel,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) diffColor else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = diffColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Choose Category Selector - Beautifully styled vertically to provide matching polished layout
                    Text(
                        text = "Set Theme / Category",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("General", "Focus", "Wisdom").forEach { cat ->
                            val selected = customCategory == cat
                            val catIcon = when(cat) {
                                "Focus" -> Icons.Default.CenterFocusStrong
                                "Wisdom" -> Icons.Default.Lightbulb
                                else -> Icons.Default.Category
                            }
                            Surface(
                                onClick = { customCategory = cat },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = catIcon,
                                            contentDescription = null,
                                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = cat,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customContent.trim().isNotEmpty()) {
                            viewModel.addCustomItem(
                                content = customContent,
                                type = selectedType,
                                difficulty = customDifficulty,
                                category = customCategory
                            )
                        }
                        showAddCustomDialog = false
                    },
                    enabled = customContent.trim().isNotEmpty(),
                    modifier = Modifier.testTag("submit_custom_btn")
                ) {
                    Text("Add Phase", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun ChapterWisePracticeView(
    items: List<PracticeItem>,
    logs: List<com.example.data.PracticeLog>,
    unlockedChapters: Map<String, Int>,
    selectedDiff: String,
    selectedType: String = "PARAGRAPH",
    selectedCategory: String = "ALL",
    onStartPractice: (PracticeItem) -> Unit,
    onDeleteItem: (Int) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentDiff = if (selectedDiff == "ALL" || selectedDiff.isEmpty()) "EASY" else selectedDiff
    val maxUnlocked = unlockedChapters[currentDiff] ?: 1
    
    var expandedChapter by remember(currentDiff) { mutableStateOf<Int?>(null) }
    
    LaunchedEffect(currentDiff, maxUnlocked) {
        expandedChapter = maxUnlocked
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        for (ch in 1..10) {
            val isUnlocked = ch <= maxUnlocked
            val allItemsInCh = items.filter { it.difficulty == currentDiff && it.chapter == ch }
            
            if (allItemsInCh.isEmpty()) continue
            
            val itemsInCh = allItemsInCh.filter { 
                it.type == selectedType && 
                (selectedCategory == "ALL" || it.category == selectedCategory) 
            }
            
            val completedInCh = allItemsInCh.count { item ->
                logs.any { log -> log.itemId == item.id && log.accuracyScore >= 70 }
            }
            
            val isExpanded = expandedChapter == ch
            
            val chapterTitle = when (currentDiff.uppercase()) {
                "EASY" -> when (ch) {
                    1 -> "Neuronal Basics"
                    2 -> "Memory Foundations"
                    3 -> "Attention Focus"
                    4 -> "Mind Recall"
                    5 -> "Cognitive Sparks"
                    6 -> "Synaptic Path"
                    7 -> "Wisdom Vaults"
                    8 -> "Visual Journeys"
                    9 -> "Zen Mindsets"
                    else -> "Champion Ascent"
                }
                "MEDIUM" -> when (ch) {
                    1 -> "Association Linkage"
                    2 -> "Retrieval Practice"
                    3 -> "Hippocampus Forge"
                    4 -> "Neuroplasticity Flow"
                    5 -> "Memory Palace Gate"
                    6 -> "Working Memory Load"
                    7 -> "Deep Concentration"
                    8 -> "Sound Resonances"
                    9 -> "Mind-Body Symmetry"
                    else -> "Creative Imagining"
                }
                else -> when (ch) {
                    1 -> "Cognitive Reservoirs"
                    2 -> "Metacognitive Mastery"
                    3 -> "Synaptic Architecture"
                    4 -> "Cortex Decoders"
                    5 -> "Loci Mastery"
                    6 -> "Working Memory Zenith"
                    7 -> "Amygdala Regulation"
                    8 -> "Auditory Selectivity"
                    9 -> "Episodic Compilers"
                    else -> "Neuroplastic Genesis"
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .clickable {
                        if (isUnlocked) {
                            expandedChapter = if (isExpanded) null else ch
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                "🔒 Chapter $ch is locked! Complete preceding chapters to unlock.",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isExpanded) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    } else if (isUnlocked) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                    }
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    if (isExpanded) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    } else if (isUnlocked) {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
                    }
                )
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
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Chapter $ch",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                if (isUnlocked) {
                                    if (completedInCh == itemsInCh.size) {
                                        Surface(
                                            color = Color(0xFF0D9488).copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "COMPLETED",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF0D9488),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(2.dp))
                            
                            Text(
                                text = chapterTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            )
                        }

                        if (!isUnlocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (isUnlocked) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$completedInCh of ${itemsInCh.size} training milestones achieved",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "${((completedInCh / itemsInCh.size.toFloat()) * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { completedInCh / itemsInCh.size.toFloat() },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                color = if (completedInCh == itemsInCh.size) Color(0xFF0D9488) else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            )
                        }
                    } else {
                        Text(
                            text = "Achieve at least 1 milestone in Chapter ${ch-1} to unlock these neural networks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(4.dp))
                        if (itemsInCh.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No $selectedType syllabus tasks in $selectedCategory theme are located in this chapter.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Medium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                itemsInCh.forEach { item ->
                                val itemPassed = logs.any { it.itemId == item.id && it.accuracyScore >= 70 }
                                val bestLog = logs.filter { it.itemId == item.id }.maxByOrNull { it.accuracyScore }
                                
                                val typeIcon = when (item.type) {
                                    "WORD" -> Icons.Default.Abc
                                    "SENTENCE" -> Icons.Default.ShortText
                                    else -> Icons.Default.Notes
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (itemPassed) Color(0xFF0D9488).copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
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
                                                    imageVector = typeIcon,
                                                    contentDescription = item.type,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = item.type,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            if (itemPassed && bestLog != null) {
                                                Surface(
                                                    color = Color(0xFF0D9488).copy(alpha = 0.1f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(10.dp))
                                                        Text(
                                                            text = "Best: ${bestLog.accuracyScore}%",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = Color(0xFF0D9488)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = item.content,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (item.isCustom) {
                                                IconButton(
                                                    onClick = { onDeleteItem(item.id) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "Delete custom text",
                                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }

                                            Button(
                                                onClick = { onStartPractice(item) },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (itemPassed) Color(0xFF0D9488) else MaterialTheme.colorScheme.primary,
                                                    contentColor = Color.White
                                                ),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    Text(
                                                        text = if (itemPassed) "Retrain" else "Begin Now",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
fun ExerciseRow(
    item: PracticeItem,
    onStart: () -> Unit,
    onDelete: () -> Unit
) {
    val levelColor = when (item.difficulty) {
        "EASY" -> Color(0xFF0D9488)
        "MEDIUM" -> Color(0xFF0EA5E9)
        else -> Color(0xFFF43F5E)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStart() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Difficulty indicator banner
            Box(
                modifier = Modifier
                    .size(8.dp, 48.dp)
                    .clip(CircleShape)
                    .background(levelColor)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Badge
                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    // Difficulty Badge
                    Surface(
                        color = levelColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        val displayDiff = when(item.difficulty.uppercase()) {
                            "EASY" -> "Beginner"
                            "MEDIUM" -> "Intermediate"
                            "HARD" -> "Advanced"
                            else -> item.difficulty
                        }
                        Text(
                            text = displayDiff,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = levelColor,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.content,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Remove/Launch Action
            if (item.isCustom) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_custom_item_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete custom text",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Start",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

data class WeeklyProgressPoint(
    val dayLabel: String,         // "Mon", "Tue", etc.
    val hasPracticed: Boolean,    // whether they did at least one practice (Recall streak element)
    val averageScore: Int,        // average accuracy score (0 to 100) or 0 if no practice
    val isToday: Boolean
)

fun getWeeklyPerformanceData(logs: List<com.example.data.PracticeLog>): List<WeeklyProgressPoint> {
    val sdf = SimpleDateFormat("EEE", Locale.US)
    val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val calendar = Calendar.getInstance()
    val points = ArrayList<WeeklyProgressPoint>()
    
    // Generate for the last 7 days (from 6 days ago up to today)
    for (i in 6 downTo 0) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -i)
        
        val dayLabel = sdf.format(cal.time) // e.g. "Mon"
        val calDayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val calYear = cal.get(Calendar.YEAR)
        
        // Find all logs on this exact day
        val logsOnDay = logs.filter { log ->
            val logCal = Calendar.getInstance().apply { timeInMillis = log.timestamp }
            logCal.get(Calendar.YEAR) == calYear &&
            logCal.get(Calendar.DAY_OF_YEAR) == calDayOfYear
        }
        
        val hasPracticed = logsOnDay.isNotEmpty()
        val avgScore = if (hasPracticed) logsOnDay.map { it.accuracyScore }.average().toInt() else 0
        val isToday = (i == 0)
        
        points.add(WeeklyProgressPoint(dayLabel, hasPracticed, avgScore, isToday))
    }
    return points
}

@Composable
fun WeeklyCelebrationChart(
    logs: List<com.example.data.PracticeLog>,
    modifier: Modifier = Modifier
) {
    val progressPoints = remember(logs) { getWeeklyPerformanceData(logs) }
    val activeDaysCount = progressPoints.count { it.hasPracticed }
    var showTooltip by remember { mutableStateOf(false) }
    val improvedCategory = remember(logs) { getMostImprovedRecallArea(logs) }
    val improvementMsg = remember(logs) { getImprovementMessage(improvedCategory, logs) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Heading
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Weekly Activity Rhythm",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(
                            onClick = { showTooltip = !showTooltip },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Show improved area helper tooltip",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "Your practice status for the last 7 days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$activeDaysCount/7 Days",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // Weekday Circles showing streak tracking
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                progressPoints.forEach { pt ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val activeColor = MaterialTheme.colorScheme.primary
                        val inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        val textAlpha = if (pt.isToday) 1.0f else 0.5f
                        val textWeight = if (pt.isToday) FontWeight.Bold else FontWeight.Normal

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pt.hasPracticed) activeColor.copy(alpha = 0.15f) else Color.Transparent
                                )
                                .border(
                                    width = (if (pt.isToday && !pt.hasPracticed) 2.dp else 1.dp),
                                    color = if (pt.hasPracticed) activeColor else if (pt.isToday) activeColor.copy(alpha = 0.6f) else inactiveColor,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (pt.hasPracticed) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Active",
                                    tint = activeColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = pt.dayLabel.take(1),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }

                        Text(
                            text = pt.dayLabel,
                            fontSize = 11.sp,
                            fontWeight = textWeight,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha)
                        )
                    }
                }
            }

            // Beautiful D3/Recharts Area Curve Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(vertical = 4.dp)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val isRelease = event.changes.all { !it.pressed }
                                if (isRelease) {
                                    showTooltip = false
                                } else {
                                    showTooltip = true
                                }
                            }
                        }
                    }
                    .clickable { showTooltip = !showTooltip },
                contentAlignment = Alignment.Center
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val outlineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    val stepX = width / 6f

                    // Map score 0-100 to Y coordinates inside canvas heights
                    fun mapY(score: Int): Float {
                        val pct = score / 100f
                        return height - (pct * (height - 30.dp.toPx())) - 15.dp.toPx()
                    }

                    // Draw reference gridlines
                    val gridValues = listOf(25, 50, 75, 100)
                    gridValues.forEach { valPct ->
                        val gy = mapY(valPct)
                        drawLine(
                            color = outlineColor,
                            start = Offset(0f, gy),
                            end = Offset(width, gy),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Draw area and line path
                    val path = Path()
                    val fillPath = Path()

                    progressPoints.forEachIndexed { idx, pt ->
                        val cx = idx * stepX
                        val cy = mapY(pt.averageScore)

                        if (idx == 0) {
                            path.moveTo(cx, cy)
                            fillPath.moveTo(cx, height)
                            fillPath.lineTo(cx, cy)
                        } else {
                            // Support beautiful bezier tension matching D3 curveMonotoneX
                            val prevX = (idx - 1) * stepX
                            val prevY = mapY(progressPoints[idx - 1].averageScore)
                            val ctrlX1 = prevX + stepX / 2f
                            val ctrlY1 = prevY
                            val ctrlX2 = prevX + stepX / 2f
                            val ctrlY2 = cy
                            
                            path.cubicTo(ctrlX1, ctrlY1, ctrlX2, ctrlY2, cx, cy)
                            fillPath.cubicTo(ctrlX1, ctrlY1, ctrlX2, ctrlY2, cx, cy)
                        }

                        if (idx == progressPoints.size - 1) {
                            fillPath.lineTo(cx, height)
                            fillPath.close()
                        }
                    }

                    // Area background gradient
                    val fillGrad = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.3f),
                            primaryColor.copy(alpha = 0.0f)
                        )
                    )
                    drawPath(path = fillPath, brush = fillGrad)

                    // Line border
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )

                    // Draw high-contrast dots and score text label flags
                    progressPoints.forEachIndexed { idx, pt ->
                        val cx = idx * stepX
                        val cy = mapY(pt.averageScore)

                        if (pt.hasPracticed) {
                            // Back drop halo
                            drawCircle(
                                color = if (isDark) Color(0xFF1E1E1E) else Color.White,
                                radius = 6.dp.toPx(),
                                center = Offset(cx, cy)
                            )
                            // Outer color ring
                            drawCircle(
                                color = primaryColor,
                                radius = 4.dp.toPx(),
                                center = Offset(cx, cy)
                            )
                        }
                    }
                }

                // Small Helper Tooltip with elegant slide/scale/fade entry
                androidx.compose.animation.AnimatedVisibility(
                    visible = showTooltip,
                    enter = fadeIn() + scaleIn(initialScale = 0.95f),
                    exit = fadeOut() + scaleOut(targetScale = 0.95f),
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .align(Alignment.Center)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.96f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = "Improved Area",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Most Improved: $improvedCategory",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = improvementMsg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun PracticeLog.getCategory(): String {
    val text = originalText.lowercase()
    return when {
        text.contains("focus") || text.contains("attention") || text.contains("mindful") || text.contains("present moment") || text.contains("flexibility") -> "Focus"
        text.contains("science") || text.contains("memory") || text.contains("recall") || text.contains("cogni") || text.contains("neuro") || text.contains("hippocampus") || text.contains("synap") || text.contains("sleep") || text.contains("retrieval") || text.contains("neural") || text.contains("connection") || text.contains("retention") -> "Science"
        text.contains("wisdom") || text.contains("mnemonics") || text.contains("loci") || text.contains("books") || text.contains("mind") || text.contains("ancient") || text.contains("classical") -> "Wisdom"
        text.contains("visuo") || text.contains("imagination") || text.contains("visual") || text.contains("sketchpad") -> "Visual"
        else -> "General"
    }
}

fun getMostImprovedRecallArea(logs: List<com.example.data.PracticeLog>): String {
    if (logs.isEmpty()) return "Focus"
    val categoryLogs = logs.groupBy { it.getCategory() }
    var bestCategory = "Focus"
    var maxImprovement = -100f
    
    for ((category, list) in categoryLogs) {
        if (list.size >= 2) {
            val sorted = list.sortedBy { it.timestamp }
            val mid = sorted.size / 2
            val oldAvg = sorted.take(mid).map { it.accuracyScore }.average()
            val newAvg = sorted.drop(mid).map { it.accuracyScore }.average()
            val improv = (newAvg - oldAvg).toFloat()
            if (improv > maxImprovement) {
                maxImprovement = improv
                bestCategory = category
            }
        }
    }
    
    if (maxImprovement <= 0) {
        val avgScores = categoryLogs.mapValues { (_, list) -> list.map { it.accuracyScore }.average() }
        bestCategory = avgScores.maxByOrNull { it.value }?.key ?: "Focus"
    }
    return bestCategory
}

fun getImprovementMessage(category: String, logs: List<com.example.data.PracticeLog>): String {
    val catLogs = logs.filter { it.getCategory() == category }
    val avgScore = if (catLogs.isNotEmpty()) catLogs.map { it.accuracyScore }.average().toInt() else 85
    return when (category) {
        "Focus" -> "Your attention stamina in Focus exercises is shining! You averaged $avgScore% accuracy this week."
        "Science" -> "Synaptic connections are strengthening! Your recall speed and accuracy in Science reached $avgScore%."
        "Wisdom" -> "You have outstanding recall of Wisdom parameters, retaining key concepts with a score of $avgScore%!"
        "Visual" -> "Your spatial recall and visualization are soaring, achieving $avgScore% accuracy on complex designs!"
        else -> "Excellent progress across General exercises! Keep building your active recall streak ($avgScore% avg)."
    }
}
