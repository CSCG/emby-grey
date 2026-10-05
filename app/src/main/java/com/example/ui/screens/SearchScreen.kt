package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MediaPosterCard
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.CinemaBlack
import com.example.ui.theme.CinemaCardBorder
import com.example.ui.theme.CinemaDarkSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.EmbyViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: EmbyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToItem: (String) -> Unit
) {
    BackHandler { onNavigateBack() }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedFilter by viewModel.selectedSearchFilter.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    val quickSuggestions = listOf("Alien", "Predator", "Action", "Sci-Fi", "Steel", "Bunny", "Fantasy", "Animation")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaBlack)
            .statusBarsPadding()
            .testTag("search_screen_root")
    ) {
        // Top Search Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag("search_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Query movies & shows by title...", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = AccentCyan)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .testTag("search_query_input"),
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
        }

        // Filter Chips Row (All, Movies, TV Shows)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedFilter == "All",
                onClick = { viewModel.setSearchFilter("All") },
                label = { Text("All Titles") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentCyan,
                    selectedLabelColor = CinemaBlack,
                    containerColor = CinemaSurfaceVariant,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == "All",
                    borderColor = CinemaCardBorder,
                    selectedBorderColor = AccentCyan
                ),
                modifier = Modifier.testTag("filter_chip_all")
            )

            FilterChip(
                selected = selectedFilter == "Movie",
                onClick = { viewModel.setSearchFilter("Movie") },
                label = { Text("Movies") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentCyan,
                    selectedLabelColor = CinemaBlack,
                    containerColor = CinemaSurfaceVariant,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == "Movie",
                    borderColor = CinemaCardBorder,
                    selectedBorderColor = AccentCyan
                ),
                modifier = Modifier.testTag("filter_chip_movies")
            )

            FilterChip(
                selected = selectedFilter == "Series",
                onClick = { viewModel.setSearchFilter("Series") },
                label = { Text("TV Shows") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentCyan,
                    selectedLabelColor = CinemaBlack,
                    containerColor = CinemaSurfaceVariant,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == "Series",
                    borderColor = CinemaCardBorder,
                    selectedBorderColor = AccentCyan
                ),
                modifier = Modifier.testTag("filter_chip_shows")
            )

            if (isSearching) {
                Spacer(modifier = Modifier.weight(1f))
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = AccentCyan,
                    strokeWidth = 2.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Body Content
        if (searchQuery.isBlank()) {
            // Initial Empty Search State with Quick Suggestions
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(CinemaSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = AccentCyan,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Search Emby Library",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Query movies and TV shows across your server by title or keyword.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Quick Searches:",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        quickSuggestions.forEach { tag ->
                            Surface(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.onSearchQueryChanged(tag) },
                                shape = RoundedCornerShape(8.dp),
                                color = CinemaSurfaceVariant
                            ) {
                                Text(
                                    text = tag,
                                    color = AccentCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else if (isSearching && searchResults.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AccentCyan)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Searching Emby server...",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = "No results",
                        tint = TextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No results found for \"$searchQuery\"",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Check the spelling or try searching with another keyword or filter.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Results Found Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filterLabel = when (selectedFilter) {
                    "Movie" -> "movies"
                    "Series" -> "shows"
                    else -> "items"
                }
                Text(
                    text = "${searchResults.size} $filterLabel found for \"$searchQuery\"",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AccentCyan.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "EMBY API",
                        color = AccentCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Results Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 130.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("search_results_grid")
            ) {
                items(searchResults) { item ->
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
