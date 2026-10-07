package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MediaPosterCard
import com.example.ui.theme.AppAccent
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AppElevatedSurface
import com.example.ui.theme.AppRadii
import com.example.ui.theme.AppSelectedSurface
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.AppSurface
import com.example.ui.theme.AppTextPrimary
import com.example.ui.theme.AppTextSecondary
import com.example.ui.theme.AppTextTertiary
import com.example.ui.theme.AppTypography
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

    val quickSuggestions = listOf("Animation", "Action", "Sci-Fi", "Comedy", "Drama", "Anime")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .testTag("search_screen_root")
    ) {
        // Top Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag("search_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AppTextPrimary
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search movies, shows, people...", color = AppTextTertiary, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = AppTextSecondary, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = AppTextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = AppSpacing.xs)
                    .testTag("search_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppAccent.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = AppTextPrimary,
                    unfocusedTextColor = AppTextPrimary,
                    focusedContainerColor = AppElevatedSurface,
                    unfocusedContainerColor = AppElevatedSurface
                ),
                shape = RoundedCornerShape(AppRadii.card)
            )
        }

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xxs),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            listOf("All", "Movie", "Series").forEach { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setSearchFilter(filter) },
                    label = {
                        Text(
                            text = if (filter == "Series") "Shows" else if (filter == "Movie") "Movies" else "All",
                            fontSize = 12.sp
                        )
                    },
                    shape = RoundedCornerShape(AppRadii.badge),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppSelectedSurface,
                        selectedLabelColor = AppTextPrimary,
                        containerColor = AppElevatedSurface,
                        labelColor = AppTextSecondary
                    ),
                    border = null
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.xxs))

        // Content Area
        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppAccent, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        } else if (searchQuery.isBlank()) {
            // Quick suggestions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.md)
            ) {
                Text(
                    text = "Suggestions",
                    style = AppTypography.cardTitle
                )
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    quickSuggestions.forEach { tag ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppRadii.badge))
                                .clickable { viewModel.updateSearchQuery(tag) },
                            shape = RoundedCornerShape(AppRadii.badge),
                            color = AppElevatedSurface
                        ) {
                            Text(
                                text = tag,
                                style = AppTypography.metadata,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        } else if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No results found",
                        style = AppTypography.cardTitle,
                        color = AppTextSecondary
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                    Text(
                        text = "Try searching for a different title or genre",
                        style = AppTypography.metadata
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 115.dp),
                contentPadding = PaddingValues(start = AppSpacing.md, end = AppSpacing.md, bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                modifier = Modifier.fillMaxSize()
            ) {
                items(searchResults, key = { it.id }) { item ->
                    MediaPosterCard(
                        item = item,
                        imageUrl = viewModel.getImageUrl(item, isBackdrop = false),
                        onClick = { onNavigateToItem(item.id) },
                        aspectRatio = 2f / 3f
                    )
                }
            }
        }
    }
}
