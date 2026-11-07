package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ArchivalBook
import com.example.engine3d.ProceduralCoverTextureGenerator
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.AmberRestricted
import com.example.ui.theme.BorderHairlineGold
import com.example.ui.theme.BorderHairlineSubtle
import com.example.ui.theme.CrimsonCensored
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.GlassPanelElevated
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.IvoryWhite
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ParchmentMuted

@Composable
fun RightBookDetailPanel(
    book: ArchivalBook?,
    selectedInstanceId: Int?,
    coverVariantOffset: Int,
    onFocusIn3D: () -> Unit,
    onOpenCoverStudio: () -> Unit,
    onMutateCoverVariant: () -> Unit,
    onPreviousBook: () -> Unit,
    onNextBook: () -> Unit,
    onClosePanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    Surface(
        color = GlassPanelElevated,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, BorderHairlineGold),
        modifier = modifier
            .fillMaxHeight()
            .testTag("right_book_detail_panel")
    ) {
        if (book == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Select any 3D book volume in the circular library to inspect its archival dossier.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentMuted
                )
            }
            return@Surface
        }

        val proceduralSpec = remember(book.id, book.title, book.author, book.category, coverVariantOffset) {
            ProceduralCoverTextureGenerator.deriveProceduralSpec(book, coverVariantOffset)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Dossier Header & Prev/Next Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ARCHIVAL DOSSIER · ${book.catalogCode}",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldAccent
                    )
                    Text(
                        text = if (selectedInstanceId != null) {
                            "3D VOLUME #${selectedInstanceId + 1000} · SEED ${proceduralSpec.hexSignature}"
                        } else {
                            "CENTRAL ROTUNDA · SEED ${proceduralSpec.hexSignature}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = ParchmentMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPreviousBook,
                        modifier = Modifier
                            .size(34.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("prev_book_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Book",
                            tint = GoldBright
                        )
                    }
                    IconButton(
                        onClick = onNextBook,
                        modifier = Modifier
                            .size(34.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("next_book_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Book",
                            tint = GoldBright
                        )
                    }
                    IconButton(
                        onClick = onClosePanel,
                        modifier = Modifier
                            .size(34.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("close_right_panel_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Dossier",
                            tint = ParchmentMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Demonstration Record vs User-Submitted Verified Record Notice
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (book.isDemonstrationRecord) GoldAccent.copy(alpha = 0.5f) else EmeraldVerified,
                            shape = RoundedCornerShape(3.dp)
                        )
                        .background(
                            if (book.isDemonstrationRecord) ObsidianBlack.copy(alpha = 0.65f) else EmeraldVerified.copy(alpha = 0.15f),
                            RoundedCornerShape(3.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = if (book.isDemonstrationRecord) GoldAccent else EmeraldVerified,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (book.isDemonstrationRecord) {
                                stringResource(R.string.demo_badge)
                            } else {
                                stringResource(R.string.verified_badge)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (book.isDemonstrationRecord) GoldBright else EmeraldVerified
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive 3D Procedural Cover Mesh + Core Metadata Row
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        InteractiveProceduralBook3DView(
                            book = book,
                            variantOffset = coverVariantOffset,
                            modifier = Modifier
                                .width(114.dp)
                                .height(156.dp)
                                .clickable { onOpenCoverStudio() }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .border(1.dp, BorderHairlineGold, RoundedCornerShape(2.dp))
                                .background(ObsidianBlack.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                                .clickable { onMutateCoverVariant() }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                .testTag("cycle_cover_edition_chip")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Cycle Procedural Cover Edition",
                                tint = GoldBright,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "EDITION #${coverVariantOffset + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldBright
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .border(1.dp, Color(book.category.badgeColorHex), RoundedCornerShape(2.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = book.category.displayName.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(book.category.badgeColorHex)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = IvoryWhite
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = book.author,
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldBright
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        MetadataKeyValueRow("COUNTRY", book.country)
                        MetadataKeyValueRow("YEAR", book.formattedYear)
                        MetadataKeyValueRow("LANGUAGE", book.language)
                        MetadataKeyValueRow("MOTIF", "${proceduralSpec.radialSymmetryOrder}-Fold Geometry")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons: Focus in 3D Rotunda + Inspect 3D Cover Texture Studio
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onFocusIn3D,
                        shape = RoundedCornerShape(3.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = ObsidianBlack
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("focus_in_3d_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CenterFocusStrong,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FOCUS IN 3D",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = ObsidianBlack,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenCoverStudio,
                        shape = RoundedCornerShape(3.dp),
                        border = BorderStroke(1.dp, BorderHairlineGold),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldBright),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_cover_studio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "3D COVER UV",
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldBright,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BorderHairlineSubtle)
                Spacer(modifier = Modifier.height(12.dp))

                // Restriction & Censorship Status Section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GppMaybe,
                        contentDescription = null,
                        tint = CrimsonCensored,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.censorship_status).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = AmberRestricted
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CrimsonCensored.copy(alpha = 0.45f), RoundedCornerShape(3.dp))
                        .background(CrimsonCensored.copy(alpha = 0.10f), RoundedCornerShape(3.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = book.restrictionSummary,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AmberPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = book.censorshipStatus,
                            style = MaterialTheme.typography.bodyMedium,
                            color = IvoryWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Historical Context Section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HistoryEdu,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.historical_background).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldAccent
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = book.historicalBackground,
                    style = MaterialTheme.typography.bodyLarge,
                    color = IvoryWhite
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BorderHairlineSubtle)
                Spacer(modifier = Modifier.height(12.dp))

                // Source & Reference Section
                Text(
                    text = stringResource(R.string.source_reference).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = book.sourceReference,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        runCatching { uriHandler.openUri(book.sourceUrl) }
                    },
                    shape = RoundedCornerShape(3.dp),
                    border = BorderStroke(1.dp, BorderHairlineGold),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GoldBright
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_source_reference_button")
                ) {
                    Text(
                        text = "VERIFY ARCHIVAL SOURCE REFERENCE",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldBright,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetadataKeyValueRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = ParchmentMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = IvoryWhite
        )
    }
}
