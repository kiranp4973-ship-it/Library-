package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ArchivalBook
import com.example.data.BookCategory
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BorderHairlineGold
import com.example.ui.theme.BorderHairlineSubtle
import com.example.ui.theme.GlassPanelElevated
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.IvoryWhite
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ParchmentMuted

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LeftArchiveFilterPanel(
    searchQuery: String,
    selectedCategories: Set<BookCategory>,
    selectedCountries: Set<String>,
    availableCountries: List<Pair<String, Int>>,
    matchingBooks: List<ArchivalBook>,
    selectedBookId: String?,
    hasActiveFilter: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onToggleCategory: (BookCategory) -> Unit,
    onToggleCountry: (String) -> Unit,
    onSelectBookFromList: (ArchivalBook) -> Unit,
    onClearAllFilters: () -> Unit,
    onClosePanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = GlassPanelElevated,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, BorderHairlineGold),
        modifier = modifier
            .fillMaxHeight()
            .testTag("left_filter_panel")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "ARCHIVE INDEX & FILTERS",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldBright
                    )
                    Text(
                        text = "${matchingBooks.size} matching works illuminated in 3D",
                        style = MaterialTheme.typography.labelSmall,
                        color = ParchmentMuted
                    )
                }
                IconButton(
                    onClick = onClosePanel,
                    modifier = Modifier
                        .size(36.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("close_left_panel_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Filters",
                        tint = ParchmentMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ParchmentMuted.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = GoldAccent
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Search",
                                tint = ParchmentMuted
                            )
                        }
                    }
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = IvoryWhite),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldBright,
                    unfocusedBorderColor = BorderHairlineSubtle,
                    focusedContainerColor = ObsidianBlack.copy(alpha = 0.75f),
                    unfocusedContainerColor = ObsidianBlack.copy(alpha = 0.55f),
                    cursorColor = GoldBright
                ),
                shape = RoundedCornerShape(3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("archive_search_input")
            )

            if (hasActiveFilter) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onClearAllFilters,
                        modifier = Modifier.testTag("reset_all_filters_button")
                    ) {
                        Text(
                            text = stringResource(R.string.clear_filters).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberPrimary
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Category Filters
                item {
                    Text(
                        text = "CATEGORIES",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldAccent
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BookCategory.entries.forEach { category ->
                            val isSelected = selectedCategories.contains(category)
                            val catColor = Color(category.badgeColorHex)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (isSelected) catColor.copy(alpha = 0.25f) else ObsidianBlack.copy(alpha = 0.65f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) catColor else BorderHairlineSubtle,
                                        shape = RoundedCornerShape(3.dp)
                                    )
                                    .clickable { onToggleCategory(category) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("category_filter_${category.name.lowercase()}")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(catColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = category.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) IvoryWhite else ParchmentMuted
                                    )
                                }
                            }
                        }
                    }
                }

                // Country Filters
                item {
                    HorizontalDivider(color = BorderHairlineSubtle)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "COUNTRIES & JURISDICTIONS",
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableCountries.forEach { (country, count) ->
                            val isSelected = selectedCountries.contains(country)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (isSelected) GoldAccent.copy(alpha = 0.25f) else ObsidianBlack.copy(alpha = 0.65f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) GoldBright else BorderHairlineSubtle,
                                        shape = RoundedCornerShape(3.dp)
                                    )
                                    .clickable { onToggleCountry(country) }
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                                    .testTag("country_filter_${country.lowercase().replace(" ", "_")}")
                            ) {
                                Text(
                                    text = "$country ($count)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) GoldBright else ParchmentMuted
                                )
                            }
                        }
                    }
                }

                // Matching Records Directory
                item {
                    HorizontalDivider(color = BorderHairlineSubtle)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "CATALOG DIRECTORY (${matchingBooks.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldAccent
                    )
                }

                if (matchingBooks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderHairlineSubtle, RoundedCornerShape(3.dp))
                                .background(ObsidianBlack.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "No matching records in current filter window.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = IvoryWhite
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Reset filters or broaden the publication timeline to illuminate archival volumes.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ParchmentMuted
                                )
                            }
                        }
                    }
                } else {
                    items(matchingBooks, key = { it.id }) { book ->
                        val isCurrent = (book.id == selectedBookId)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (isCurrent) GoldAccent.copy(alpha = 0.20f) else ObsidianBlack.copy(alpha = 0.60f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isCurrent) GoldBright else BorderHairlineSubtle,
                                    shape = RoundedCornerShape(3.dp)
                                )
                                .clickable { onSelectBookFromList(book) }
                                .padding(10.dp)
                                .testTag("catalog_item_${book.id}")
                        ) {
                            Column {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${book.country.uppercase()} · ${book.formattedYear}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldAccent
                                    )
                                    Text(
                                        text = book.category.displayName.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(book.category.badgeColorHex)
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = book.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = IvoryWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = book.author,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ParchmentMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
