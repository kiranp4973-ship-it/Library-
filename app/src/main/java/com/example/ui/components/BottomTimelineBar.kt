package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ArchivalBook
import com.example.data.HistoricalEra
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BorderHairlineGold
import com.example.ui.theme.BorderHairlineSubtle
import com.example.ui.theme.GlassPanelBg
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.IvoryWhite
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ParchmentMuted
import kotlin.math.roundToInt

@Composable
fun BottomTimelineAndCameraBar(
    selectedEra: HistoricalEra,
    yearRangeStart: Int,
    yearRangeEnd: Int,
    showExpandedTimelineScrubber: Boolean,
    total3DVolumes: Int,
    matchingRecordsCount: Int,
    totalRecordsCount: Int,
    selectedBook: ArchivalBook?,
    autoRotateRings: Boolean,
    hasActiveFilter: Boolean,
    onSelectEra: (HistoricalEra) -> Unit,
    onUpdateYearRange: (Int, Int) -> Unit,
    onResetCamera: () -> Unit,
    onFocusSelectedBook: () -> Unit,
    onToggleAutoRotate: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onOpenSelectedBookDetail: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = GlassPanelBg,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, BorderHairlineGold),
        modifier = modifier
            .fillMaxWidth()
            .testTag("bottom_timeline_bar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Historical Era Pills Strip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = "TIMELINE:",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldAccent,
                    modifier = Modifier.padding(end = 4.dp)
                )

                HistoricalEra.entries.forEach { era ->
                    val isSelected = (era == selectedEra)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (isSelected) GoldAccent.copy(alpha = 0.25f) else ObsidianBlack.copy(alpha = 0.55f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) GoldBright else BorderHairlineSubtle,
                                shape = RoundedCornerShape(3.dp)
                            )
                            .clickable { onSelectEra(era) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("era_chip_${era.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = era.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) GoldBright else IvoryWhite
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = era.yearRangeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) AmberPrimary else ParchmentMuted
                            )
                        }
                    }
                }

                if (hasActiveFilter) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(AmberPrimary.copy(alpha = 0.20f))
                            .border(1.dp, AmberPrimary, RoundedCornerShape(3.dp))
                            .clickable { onClearFilters() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("bottom_clear_filters_chip")
                    ) {
                        Text(
                            text = "RESET FILTERS",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberPrimary
                        )
                    }
                }
            }

            // Expandable Continuous Year Range Scrubber when Timeline mode is active
            AnimatedVisibility(visible = showExpandedTimelineScrubber) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val startLabel = if (yearRangeStart < 0) "${-yearRangeStart} BCE" else "$yearRangeStart CE"
                        val endLabel = if (yearRangeEnd < 0) "${-yearRangeEnd} BCE" else "$yearRangeEnd CE"
                        Text(
                            text = "CHRONOLOGICAL WINDOW: $startLabel — $endLabel",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldBright
                        )
                        Text(
                            text = "$matchingRecordsCount MATCHING ARCHIVES",
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentMuted
                        )
                    }
                    RangeSlider(
                        value = yearRangeStart.toFloat()..yearRangeEnd.toFloat(),
                        onValueChange = { range ->
                            onUpdateYearRange(range.start.roundToInt(), range.endInclusive.roundToInt())
                        },
                        valueRange = -250f..2026f,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldBright,
                            activeTrackColor = GoldAccent,
                            inactiveTrackColor = ObsidianBlack
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .testTag("timeline_range_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom Status & Camera Controls Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Current Book Selection & Total Book Count Readout
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(3.dp))
                        .clickable { onOpenSelectedBookDetail() }
                        .padding(vertical = 2.dp)
                        .testTag("current_selection_bar_item")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ARCHIVAL RECORDS: $matchingRecordsCount/$totalRecordsCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "· $total3DVolumes 3D VOLUMES",
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentMuted
                        )
                    }
                    Text(
                        text = if (selectedBook != null) {
                            "SELECTED: ${selectedBook.title} (${selectedBook.author}, ${selectedBook.formattedYear})"
                        } else {
                            "TAP ANY 3D BOOK VOLUME TO INSPECT RECORD"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = IvoryWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Camera Controls: Zoom Out, Zoom In, Focus Selected, Orbit Play/Pause, Camera Reset
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onZoomOut,
                        modifier = Modifier
                            .size(36.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("camera_zoom_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomOut,
                            contentDescription = "Zoom Out",
                            tint = ParchmentMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onZoomIn,
                        modifier = Modifier
                            .size(36.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("camera_zoom_in_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Zoom In",
                            tint = ParchmentMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onFocusSelectedBook,
                        modifier = Modifier
                            .size(36.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("camera_focus_selected_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CenterFocusStrong,
                            contentDescription = stringResource(R.string.focus_camera),
                            tint = GoldBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleAutoRotate,
                        modifier = Modifier
                            .size(36.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("toggle_ring_rotation_button")
                    ) {
                        Icon(
                            imageVector = if (autoRotateRings) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (autoRotateRings) "Pause Ring Rotation" else "Resume Ring Rotation",
                            tint = if (autoRotateRings) GoldAccent else ParchmentMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onResetCamera,
                        modifier = Modifier
                            .size(36.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("camera_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = stringResource(R.string.reset_camera),
                            tint = IvoryWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
