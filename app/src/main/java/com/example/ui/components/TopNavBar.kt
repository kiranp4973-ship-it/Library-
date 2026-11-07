package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.NavigationSection
import com.example.data.RenderQualityPreset
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BorderHairlineGold
import com.example.ui.theme.BorderHairlineSubtle
import com.example.ui.theme.GlassPanelBg
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.IvoryWhite
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ParchmentMuted

@Composable
fun TopInstallationNavBar(
    activeSection: NavigationSection,
    hasActiveFilter: Boolean,
    activeFilterCount: Int,
    isLeftFilterOpen: Boolean,
    isRightDetailOpen: Boolean,
    isImmersiveFullscreen: Boolean,
    qualityPreset: RenderQualityPreset,
    currentFps: Int,
    onSelectSection: (NavigationSection) -> Unit,
    onToggleLeftFilter: () -> Unit,
    onToggleRightDetail: () -> Unit,
    onOpenAddRecord: () -> Unit,
    onCycleQuality: () -> Unit,
    onToggleImmersive: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = GlassPanelBg,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, BorderHairlineGold),
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_installation_navbar")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // Minimal Logo & Telemetry Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onSelectSection(NavigationSection.EXPLORE) }
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .border(1.dp, GoldAccent, RoundedCornerShape(3.dp))
                            .background(ObsidianBlack, RoundedCornerShape(3.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = GoldBright,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "FORBIDDEN LIBRARY",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = IvoryWhite
                        )
                        Text(
                            text = "${qualityPreset.instanceCount} 3D VOLUMES · $currentFps FPS",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent
                        )
                    }
                }

                // Right Action Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Filter & Search Drawer Toggle
                    IconButton(
                        onClick = onToggleLeftFilter,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("toggle_filter_panel_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (hasActiveFilter) {
                                    Badge(
                                        containerColor = AmberPrimary,
                                        contentColor = ObsidianBlack
                                    ) {
                                        Text("$activeFilterCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Search and Filters",
                                tint = if (isLeftFilterOpen || hasActiveFilter) GoldBright else ParchmentMuted
                            )
                        }
                    }

                    // Book Inspector Drawer Toggle
                    IconButton(
                        onClick = onToggleRightDetail,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("toggle_detail_panel_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Selected Book Dossier",
                            tint = if (isRightDetailOpen) GoldBright else ParchmentMuted
                        )
                    }

                    // Add Verified Record Button
                    IconButton(
                        onClick = onOpenAddRecord,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("add_verified_record_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_record),
                            tint = GoldAccent
                        )
                    }

                    // Adaptive Quality Mode Cycle Button
                    IconButton(
                        onClick = onCycleQuality,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("cycle_quality_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = stringResource(R.string.quality_mode),
                            tint = ParchmentMuted
                        )
                    }

                    // Fullscreen / Immersive HUD Toggle
                    IconButton(
                        onClick = onToggleImmersive,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("fullscreen_hud_button")
                    ) {
                        Icon(
                            imageVector = if (isImmersiveFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = stringResource(R.string.toggle_hud),
                            tint = if (isImmersiveFullscreen) GoldBright else ParchmentMuted
                        )
                    }
                }
            }

            // Navigation Mode Strip: Explore · Countries · Timeline · Categories · About
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
            ) {
                NavigationSection.entries.forEach { section ->
                    val selected = (section == activeSection)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (selected) GoldAccent.copy(alpha = 0.22f) else ObsidianBlack.copy(alpha = 0.45f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (selected) GoldBright else BorderHairlineSubtle,
                                shape = RoundedCornerShape(3.dp)
                            )
                            .clickable { onSelectSection(section) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("nav_tab_${section.name.lowercase()}")
                    ) {
                        Text(
                            text = section.label.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) GoldBright else ParchmentMuted
                        )
                    }
                }
            }
        }
    }
}
