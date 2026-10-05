package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.DemoDataProvider
import com.example.ui.components.MediaPosterCard
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.CinemaBlack
import com.example.ui.theme.CinemaCardBorder
import com.example.ui.theme.CinemaDarkSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.EmbyViewModel

@Composable
fun LibraryDetailScreen(
    libraryId: String,
    libraryName: String,
    viewModel: EmbyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToItem: (String) -> Unit
) {
    BackHandler { onNavigateBack() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedSort by remember { mutableStateOf("SortName") }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val libraryItems by viewModel.libraryItems.collectAsState()
    val isLoading by viewModel.isLibraryLoading.collectAsState()

    LaunchedEffect(libraryId) {
        viewModel.loadLibrary(libraryId)
    }

    val filteredItems = remember(libraryItems, searchQuery) {
        if (searchQuery.isBlank()) {
            libraryItems
        } else {
            libraryItems.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.overview?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaBlack)
            .statusBarsPadding()
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Text(
                text = libraryName,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            // Sort Menu Button
            Box {
                IconButton(onClick = { sortMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Sort Options",
                        tint = AccentCyan
                    )
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false },
                    modifier = Modifier.background(CinemaDarkSurface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Name (A to Z)", color = TextPrimary) },
                        onClick = {
                            selectedSort = "SortName"
                            sortMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Release Year", color = TextPrimary) },
                        onClick = {
                            selectedSort = "ProductionYear"
                            sortMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Community Rating", color = TextPrimary) },
                        onClick = {
                            selectedSort = "CommunityRating"
                            sortMenuExpanded = false
                        }
                    )
                }
            }
        }

        // Search Bar in Library
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search in $libraryName...", color = TextMuted) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = AccentCyan)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("library_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentCyan,
                unfocusedBorderColor = CinemaCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = CinemaSurfaceVariant,
                unfocusedContainerColor = CinemaSurfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Count header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredItems.size} items",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Text(
                text = "Sorted by: $selectedSort",
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Grid of Media Posters
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentCyan)
            }
        } else if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = "No items",
                        tint = TextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "No items matching \"$searchQuery\"" else "Library is empty",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 130.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredItems) { item ->
                    MediaPosterCard(
                        item = item,
                        imageUrl = viewModel.getImageUrl(item, isBackdrop = false),
                        onClick = { onNavigateToItem(item.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
