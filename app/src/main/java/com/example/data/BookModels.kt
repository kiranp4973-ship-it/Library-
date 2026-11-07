package com.example.data

import androidx.compose.ui.graphics.Color

enum class BookCategory(val displayName: String, val badgeColorHex: Long) {
    HISTORY("History", 0xFFD4AF37),
    POLITICS("Politics", 0xFFE63946),
    LITERATURE("Literature", 0xFF7B4DFF),
    SCIENCE("Science", 0xFF4CC9F0),
    PHILOSOPHY("Philosophy", 0xFFFFB347),
    CULTURE("Culture", 0xFF2A9D8F)
}

enum class HistoricalEra(
    val id: String,
    val label: String,
    val yearRangeLabel: String,
    val startYear: Int,
    val endYear: Int
) {
    ALL("all", "All Eras", "250 BCE – Present", -500, 2100),
    ANTIQUITY_RENAISSANCE("antiquity", "Antiquity & Renaissance", "Before 1600", -500, 1599),
    ENLIGHTENMENT("enlightenment", "Age of Reason", "1600 – 1799", 1600, 1799),
    INDUSTRIAL("industrial", "19th Century", "1800 – 1899", 1800, 1899),
    EARLY_MODERN("early_modern", "Early 20th C.", "1900 – 1959", 1900, 1959),
    CONTEMPORARY("contemporary", "Contemporary", "1960 – Present", 1960, 2100)
}

enum class CoverPattern {
    ASTRONOMICAL_RINGS,
    GEOMETRIC_GRID,
    IMPERIAL_SEAL,
    MINIMAL_MONOGRAPH,
    CENSORED_REDACTION,
    CLASSICAL_FOLIO
}

data class BookCoverStyle(
    val primaryColorHex: Long,
    val accentFoilHex: Long,
    val spineColorHex: Long,
    val heightScale: Float,      // 0.80f .. 1.30f
    val thicknessScale: Float,   // 0.65f .. 1.45f
    val widthScale: Float,       // 0.85f .. 1.20f
    val pattern: CoverPattern
)

data class ArchivalBook(
    val id: String,
    val catalogCode: String,
    val title: String,
    val author: String,
    val country: String,
    val region: String,
    val publicationYear: Int,
    val language: String,
    val category: BookCategory,
    val historicalBackground: String,
    val censorshipStatus: String,
    val restrictionSummary: String,
    val sourceReference: String,
    val sourceUrl: String,
    val coverStyle: BookCoverStyle,
    val isDemonstrationRecord: Boolean = true
) {
    val formattedYear: String
        get() = if (publicationYear < 0) "${-publicationYear} BCE" else "$publicationYear CE"
}

enum class NavigationSection(val label: String) {
    EXPLORE("Explore"),
    COUNTRIES("Countries"),
    TIMELINE("Timeline"),
    CATEGORIES("Categories"),
    ABOUT("About")
}

enum class RenderQualityPreset(
    val label: String,
    val instanceCount: Int,
    val particleCount: Int,
    val description: String
) {
    HIGH("Ultra · 1,440 Volumes", 1440, 220, "5 concentric rings · 1,440 3D books · full volumetric dust"),
    BALANCED("Balanced · 900 Volumes", 900, 140, "5 concentric rings · 900 3D books · adaptive lighting"),
    PERFORMANCE("Mobile · 540 Volumes", 540, 75, "5 concentric rings · 540 3D books · high-FPS efficiency")
}
