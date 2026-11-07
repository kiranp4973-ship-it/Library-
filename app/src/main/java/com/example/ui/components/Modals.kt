package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.BookCategory
import com.example.data.RenderQualityPreset
import com.example.ui.theme.BorderHairlineGold
import com.example.ui.theme.BorderHairlineSubtle
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.GlassPanelElevated
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.IvoryWhite
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ParchmentMuted

@Composable
fun AboutInstallationModal(
    qualityPreset: RenderQualityPreset,
    totalRecords: Int,
    onReplayIntro: () -> Unit,
    onCycleQuality: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = GlassPanelElevated,
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, BorderHairlineGold),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 580.dp)
                .padding(vertical = 24.dp)
                .testTag("about_installation_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "DIGITAL MUSEUM & ARCHIVE INSTALLATION",
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldAccent
                        )
                        Text(
                            text = "FORBIDDEN LIBRARY",
                            style = MaterialTheme.typography.headlineLarge,
                            color = IvoryWhite
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_about_modal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close About Modal",
                            tint = ParchmentMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Generated Hero Architectural Visual
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, BorderHairlineGold, RoundedCornerShape(4.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_archive_hero_1793999975205),
                        contentDescription = "3D Circular Library Rotunda Installation Concept",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ARCHITECTURAL STATEMENT",
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldBright
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Forbidden Library is an interactive 3D digital installation exploring the fragile history of human knowledge, censorship, scientific inquiry, and literary resilience across centuries. Five concentric counter-rotating rings house up to 1,440 3D extruded book volumes illuminated by a central warm amber core and deep cosmic violet rim light.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = IvoryWhite
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderHairlineSubtle)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "HISTORICAL VERIFICATION & INTEGRITY POLICY",
                    style = MaterialTheme.typography.labelLarge,
                    color = EmeraldVerified
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "All $totalRecords initial archival entries are grounded in documented historical treaties, decrees, and library archives (such as the Index Librorum Prohibitorum, the 213 BCE Qin Fenshu decree, the 1933 United States v. One Book Called 'Ulysses' decision, and UNESCO Memory of the World records) and are transparently badged as Demonstration Archive Records. Scholars and visitors can contribute additional verified entries using the '+' button in the top bar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentMuted
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderHairlineSubtle)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "ADAPTIVE 3D ENGINE CONFIGURATION",
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Active Preset: ${qualityPreset.label} (${qualityPreset.description})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = IvoryWhite
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onCycleQuality,
                        shape = RoundedCornerShape(3.dp),
                        border = BorderStroke(1.dp, BorderHairlineGold),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("modal_cycle_quality_button")
                    ) {
                        Text(
                            text = "SWITCH QUALITY",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldBright
                        )
                    }

                    Button(
                        onClick = onReplayIntro,
                        shape = RoundedCornerShape(3.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = ObsidianBlack
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("replay_intro_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "REPLAY INTRO",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = ObsidianBlack,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddVerifiedRecordModal(
    onAddRecord: (
        title: String,
        author: String,
        country: String,
        publicationYear: Int,
        language: String,
        category: BookCategory,
        historicalBackground: String,
        censorshipStatus: String,
        sourceReference: String,
        sourceUrl: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var yearText by remember { mutableStateOf("1945") }
    var language by remember { mutableStateOf("English") }
    var selectedCategory by remember { mutableStateOf(BookCategory.HISTORY) }
    var historicalBackground by remember { mutableStateOf("") }
    var censorshipStatus by remember { mutableStateOf("") }
    var sourceReference by remember { mutableStateOf("") }
    var sourceUrl by remember { mutableStateOf("https://www.unesco.org/en/memory-world") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = GlassPanelElevated,
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, BorderHairlineGold),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 560.dp)
                .padding(vertical = 20.dp)
                .testTag("add_verified_record_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = EmeraldVerified,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ADD VERIFIED ARCHIVAL RECORD",
                            style = MaterialTheme.typography.titleLarge,
                            color = IvoryWhite
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_record_modal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ParchmentMuted
                        )
                    }
                }

                Text(
                    text = "Contribute a historically documented work to the 3D circular library. Never submit unverified or fictional restriction claims.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                ArchiveFormTextField(
                    label = "Book Title *",
                    value = title,
                    onValueChange = { title = it },
                    testTag = "input_book_title"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ArchiveFormTextField(
                        label = "Author *",
                        value = author,
                        onValueChange = { author = it },
                        modifier = Modifier.weight(1f),
                        testTag = "input_book_author"
                    )
                    ArchiveFormTextField(
                        label = "Country *",
                        value = country,
                        onValueChange = { country = it },
                        modifier = Modifier.weight(1f),
                        testTag = "input_book_country"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ArchiveFormTextField(
                        label = "Year (negative for BCE)",
                        value = yearText,
                        onValueChange = { yearText = it },
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        testTag = "input_book_year"
                    )
                    ArchiveFormTextField(
                        label = "Language",
                        value = language,
                        onValueChange = { language = it },
                        modifier = Modifier.weight(1f),
                        testTag = "input_book_language"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "CATEGORY",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    BookCategory.entries.forEach { cat ->
                        val selected = (cat == selectedCategory)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (selected) Color(cat.badgeColorHex).copy(alpha = 0.25f) else ObsidianBlack
                                )
                                .border(
                                    1.dp,
                                    if (selected) Color(cat.badgeColorHex) else BorderHairlineSubtle,
                                    RoundedCornerShape(3.dp)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) IvoryWhite else ParchmentMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ArchiveFormTextField(
                    label = "Historical Background",
                    value = historicalBackground,
                    onValueChange = { historicalBackground = it },
                    singleLine = false,
                    testTag = "input_book_background"
                )

                Spacer(modifier = Modifier.height(8.dp))

                ArchiveFormTextField(
                    label = "Documented Censorship or Restriction Status",
                    value = censorshipStatus,
                    onValueChange = { censorshipStatus = it },
                    singleLine = false,
                    testTag = "input_book_censorship"
                )

                Spacer(modifier = Modifier.height(8.dp))

                ArchiveFormTextField(
                    label = "Primary Archival Source / Citation",
                    value = sourceReference,
                    onValueChange = { sourceReference = it },
                    testTag = "input_book_source"
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(3.dp),
                        border = BorderStroke(1.dp, BorderHairlineSubtle)
                    ) {
                        Text("CANCEL", style = MaterialTheme.typography.labelMedium, color = ParchmentMuted)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val parsedYear = yearText.toIntOrNull() ?: 1900
                            onAddRecord(
                                title,
                                author,
                                country.ifBlank { "International" },
                                parsedYear,
                                language.ifBlank { "English" },
                                selectedCategory,
                                historicalBackground,
                                censorshipStatus,
                                sourceReference,
                                sourceUrl
                            )
                        },
                        enabled = title.isNotBlank() && author.isNotBlank(),
                        shape = RoundedCornerShape(3.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = ObsidianBlack
                        ),
                        modifier = Modifier.testTag("submit_verified_record_button")
                    ) {
                        Text(
                            text = "INDEX INTO 3D ROTUNDA",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = ObsidianBlack,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchiveFormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = ParchmentMuted)
        },
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = IvoryWhite),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldBright,
            unfocusedBorderColor = BorderHairlineSubtle,
            focusedContainerColor = ObsidianBlack.copy(alpha = 0.7f),
            unfocusedContainerColor = ObsidianBlack.copy(alpha = 0.5f),
            cursorColor = GoldBright
        ),
        shape = RoundedCornerShape(3.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    )
}
