package com.promptvault.android.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.promptvault.android.ui.components.PromptCard
import com.promptvault.android.ui.components.PromptCardData
import com.promptvault.android.ui.gallery.GalleryViewModel
import com.promptvault.android.ui.gallery.SortOrder
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel = remember { GalleryViewModel() },
    onPromptClick: (Long) -> Unit = {},
    onFabClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    var expandedFilters by remember { mutableStateOf(false) }
    var expandedSort by remember { mutableStateOf(false) }
    var showDeleteSnackbar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gallery") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = { expandedSort = !expandedSort }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    IconButton(onClick = { expandedFilters = !expandedFilters }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // XENOCOGNITIVE: Cognitive Offloading - Quick-capture FAB for minimal friction
            FloatingActionButton(
                onClick = onFabClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Quick capture new prompt",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar with real-time filtering
            SearchBar(
                searchText = searchText,
                onSearchChange = { newText ->
                    searchText = newText
                    viewModel.searchPrompts(newText.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Filter chips (by tag, complexity, date range)
            if (expandedFilters) {
                FilterChipsSection(
                    selectedTags = uiState.selectedTags,
                    selectedComplexity = uiState.selectedComplexity,
                    onTagClick = { tag -> viewModel.filterByTag(tag) },
                    onComplexityClick = { complexity -> viewModel.filterByComplexity(complexity) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Sort menu
            if (expandedSort) {
                SortMenuSection(
                    currentSort = uiState.sortBy,
                    onSortSelected = { sort ->
                        viewModel.sortBy(sort)
                        expandedSort = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                return@Column
            }

            // Error state
            if (uiState.error != null) {
                ErrorMessage(
                    message = uiState.error ?: "Unknown error",
                    onDismiss = { viewModel.clearError() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

            // PP-T1: Pagination for scale - LazyVerticalGrid loads 100 prompts efficiently
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 320.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    uiState.prompts,
                    key = { it.id }
                ) { prompt ->
                    SwipeablPromptCard(
                        data = PromptCardData(
                            id = prompt.id,
                            title = prompt.title,
                            preview = prompt.preview,
                            isFavorite = prompt.isFavorite,
                            usageCount = prompt.usageCount,
                            createdAt = prompt.createdAt,
                            lastUsed = prompt.lastUsed,
                            tags = prompt.tags,
                            complexity = prompt.complexity
                        ),
                        onCardClick = { onPromptClick(it) },
                        onFavoriteToggle = { id, isFavorite ->
                            viewModel.toggleFavorite(id)
                        },
                        onDelete = { id ->
                            viewModel.deletePrompt(id)
                            showDeleteSnackbar = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Undo snackbar for deleted prompts
            if (uiState.undoAction != null && showDeleteSnackbar) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.undoLastAction() }) {
                            Text("Undo", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                ) {
                    Text("Prompt deleted")
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    searchText: TextFieldValue,
    onSearchChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = searchText,
        onValueChange = onSearchChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (searchText.text.isNotEmpty()) {
                IconButton(onClick = { onSearchChange(TextFieldValue("")) }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        placeholder = { Text("Search prompts...") },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun FilterChipsSection(
    selectedTags: Set<String>,
    selectedComplexity: String?,
    onTagClick: (String) -> Unit,
    onComplexityClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Complexity",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT").forEach { complexity ->
                FilterChip(
                    selected = selectedComplexity == complexity,
                    onClick = { onComplexityClick(complexity) },
                    label = { Text(complexity) }
                )
            }
        }
    }
}

@Composable
private fun SortMenuSection(
    currentSort: SortOrder,
    onSortSelected: (SortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        listOf(
            SortOrder.RECENT to "Most Recent",
            SortOrder.OLDEST to "Oldest First",
            SortOrder.MOST_USED to "Most Used",
            SortOrder.TITLE_ASC to "Title (A-Z)",
            SortOrder.TITLE_DESC to "Title (Z-A)"
        ).forEach { (sortOrder, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentSort == sortOrder,
                    onClick = { onSortSelected(sortOrder) }
                )
                Text(
                    label,
                    modifier = Modifier.padding(start = 12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun SwipeablPromptCard(
    data: PromptCardData,
    onCardClick: (Long) -> Unit,
    onFavoriteToggle: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var offset by remember { mutableStateOf(0f) }
    val dismissThreshold = 100f
    val backgroundColor by animateColorAsState(
        targetValue = if (offset < -dismissThreshold) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.surface
        }
    )

    Box(
        modifier = modifier
            .height(200.dp)
            .background(backgroundColor, shape = MaterialTheme.shapes.medium)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        offset = (offset + dragAmount).coerceIn(-200f, 0f)
                    },
                    onDragEnd = {
                        if (offset < -dismissThreshold) {
                            onDelete(data.id)
                        }
                        offset = 0f
                    }
                )
            }
    ) {
        PromptCard(
            data = data,
            onCardClick = onCardClick,
            onFavoriteToggle = onFavoriteToggle,
            modifier = Modifier
                .fillMaxSize()
                .offset(x = offset.dp)
        )
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                message,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss error",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
