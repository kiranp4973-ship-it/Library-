package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.ArchivalBook
import com.example.data.BookCategory
import com.example.data.BookCoverStyle
import com.example.data.CoverPattern
import com.example.data.ForbiddenLibraryRepository
import com.example.data.HistoricalEra
import com.example.data.NavigationSection
import com.example.data.RenderQualityPreset
import com.example.engine3d.Library3DEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ForbiddenLibraryUiState(
    val allBooks: List<ArchivalBook> = ForbiddenLibraryRepository.initialArchivalBooks,
    val searchQuery: String = "",
    val selectedCategories: Set<BookCategory> = emptySet(),
    val selectedCountries: Set<String> = emptySet(),
    val selectedEra: HistoricalEra = HistoricalEra.ALL,
    val yearRangeStart: Int = -250,
    val yearRangeEnd: Int = 2026,
    val activeSection: NavigationSection = NavigationSection.EXPLORE,
    val selectedInstanceId: Int? = 0,
    val selectedBook: ArchivalBook? = ForbiddenLibraryRepository.initialArchivalBooks.firstOrNull(),
    val hoveredInstanceId: Int? = null,
    val hoveredBook: ArchivalBook? = null,
    val coverVariantOffsets: Map<String, Int> = emptyMap(),
    val isLeftFilterOpen: Boolean = false,
    val isRightDetailOpen: Boolean = false,
    val isAddRecordModalOpen: Boolean = false,
    val isAboutModalOpen: Boolean = false,
    val isCoverStudioModalOpen: Boolean = false,
    val isImmersiveFullscreen: Boolean = false,
    val autoRotateRings: Boolean = true,
    val qualityPreset: RenderQualityPreset = RenderQualityPreset.HIGH,
    val currentFps: Int = 60,
    val showCinematicIntro: Boolean = true
) {
    val selectedBookVariantOffset: Int
        get() = selectedBook?.let { coverVariantOffsets[it.id] ?: 0 } ?: 0

    val availableCountries: List<Pair<String, Int>>
        get() = allBooks
            .groupingBy { it.country }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { it.key to it.value }

    val matchingBooks: List<ArchivalBook>
        get() {
            val q = searchQuery.trim().lowercase()
            return allBooks.filter { book ->
                val matchesSearch = q.isEmpty() ||
                    book.title.lowercase().contains(q) ||
                    book.author.lowercase().contains(q) ||
                    book.country.lowercase().contains(q) ||
                    book.historicalBackground.lowercase().contains(q) ||
                    book.censorshipStatus.lowercase().contains(q) ||
                    book.language.lowercase().contains(q) ||
                    book.category.displayName.lowercase().contains(q)

                val matchesCategory = selectedCategories.isEmpty() || selectedCategories.contains(book.category)
                val matchesCountry = selectedCountries.isEmpty() || selectedCountries.contains(book.country)
                val matchesEra = book.publicationYear in selectedEra.startYear..selectedEra.endYear
                val matchesYearSlider = book.publicationYear in yearRangeStart..yearRangeEnd

                matchesSearch && matchesCategory && matchesCountry && matchesEra && matchesYearSlider
            }
        }

    val matchingBookIds: Set<String>
        get() = matchingBooks.mapTo(HashSet()) { it.id }

    val hasActiveFilter: Boolean
        get() = searchQuery.isNotBlank() ||
            selectedCategories.isNotEmpty() ||
            selectedCountries.isNotEmpty() ||
            selectedEra != HistoricalEra.ALL ||
            yearRangeStart > -250 ||
            yearRangeEnd < 2026
}

class ForbiddenLibraryViewModel : ViewModel() {

    val engine = Library3DEngine()

    private val _uiState = MutableStateFlow(ForbiddenLibraryUiState())
    val uiState: StateFlow<ForbiddenLibraryUiState> = _uiState.asStateFlow()

    init {
        engine.rebuildLibraryInstances(
            books = _uiState.value.allBooks,
            qualityPreset = _uiState.value.qualityPreset,
            variantOffsets = _uiState.value.coverVariantOffsets
        )
    }

    fun completeOrSkipIntro() {
        _uiState.update { it.copy(showCinematicIntro = false) }
    }

    fun replayIntro() {
        _uiState.update {
            it.copy(
                showCinematicIntro = true,
                isAboutModalOpen = false
            )
        }
    }

    fun selectNavigationSection(section: NavigationSection) {
        _uiState.update { state ->
            when (section) {
                NavigationSection.EXPLORE -> state.copy(
                    activeSection = section,
                    isAboutModalOpen = false
                )
                NavigationSection.COUNTRIES,
                NavigationSection.CATEGORIES -> state.copy(
                    activeSection = section,
                    isLeftFilterOpen = true,
                    isAboutModalOpen = false
                )
                NavigationSection.TIMELINE -> state.copy(
                    activeSection = section,
                    isAboutModalOpen = false
                )
                NavigationSection.ABOUT -> state.copy(
                    activeSection = section,
                    isAboutModalOpen = true
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        syncSelectionToFirstMatchIfNeeded()
    }

    fun toggleCategoryFilter(category: BookCategory) {
        _uiState.update { state ->
            val updated = state.selectedCategories.toMutableSet()
            if (!updated.add(category)) updated.remove(category)
            state.copy(selectedCategories = updated)
        }
        syncSelectionToFirstMatchIfNeeded()
    }

    fun toggleCountryFilter(country: String) {
        _uiState.update { state ->
            val updated = state.selectedCountries.toMutableSet()
            if (!updated.add(country)) updated.remove(country)
            state.copy(selectedCountries = updated)
        }
        syncSelectionToFirstMatchIfNeeded()
    }

    fun selectHistoricalEra(era: HistoricalEra) {
        _uiState.update { state ->
            val start = if (era == HistoricalEra.ALL) -250 else era.startYear.coerceAtLeast(-250)
            val end = if (era == HistoricalEra.ALL) 2026 else era.endYear.coerceAtMost(2026)
            state.copy(
                selectedEra = era,
                yearRangeStart = start,
                yearRangeEnd = end
            )
        }
        syncSelectionToFirstMatchIfNeeded()
    }

    fun updateYearRange(startYear: Int, endYear: Int) {
        _uiState.update { state ->
            state.copy(
                selectedEra = HistoricalEra.ALL,
                yearRangeStart = startYear.coerceIn(-250, 2026),
                yearRangeEnd = endYear.coerceIn(-250, 2026)
            )
        }
        syncSelectionToFirstMatchIfNeeded()
    }

    fun clearAllFilters() {
        _uiState.update { state ->
            state.copy(
                searchQuery = "",
                selectedCategories = emptySet(),
                selectedCountries = emptySet(),
                selectedEra = HistoricalEra.ALL,
                yearRangeStart = -250,
                yearRangeEnd = 2026
            )
        }
    }

    fun selectBookInstance(instanceId: Int, openDetailPanel: Boolean = true) {
        val inst = engine.instances.getOrNull(instanceId) ?: return
        val book = _uiState.value.allBooks.getOrNull(inst.archivalBookIndex)
            ?: _uiState.value.allBooks.find { it.id == inst.archivalBookId }
        _uiState.update { state ->
            state.copy(
                selectedInstanceId = instanceId,
                selectedBook = book,
                isRightDetailOpen = openDetailPanel
            )
        }
    }

    fun selectArchivalBookDirectly(book: ArchivalBook, focusCamera: Boolean = true) {
        val targetInstance = engine.instances.firstOrNull { it.archivalBookId == book.id }
        val instId = targetInstance?.instanceId
        _uiState.update { state ->
            state.copy(
                selectedInstanceId = instId,
                selectedBook = book,
                isRightDetailOpen = true
            )
        }
        if (focusCamera && instId != null) {
            engine.focusOnInstance(instId)
        }
    }

    fun stepSelectedBook(delta: Int) {
        val state = _uiState.value
        val pool = state.matchingBooks.ifEmpty { state.allBooks }
        if (pool.isEmpty()) return
        val currentIdx = pool.indexOfFirst { it.id == state.selectedBook?.id }.takeIf { it >= 0 } ?: 0
        val nextIdx = (currentIdx + delta + pool.size) % pool.size
        selectArchivalBookDirectly(pool[nextIdx], focusCamera = true)
    }

    fun hoverBookInstance(instanceId: Int?) {
        if (instanceId == null) {
            _uiState.update { it.copy(hoveredInstanceId = null, hoveredBook = null) }
            return
        }
        val inst = engine.instances.getOrNull(instanceId)
        val book = inst?.let { _uiState.value.allBooks.getOrNull(it.archivalBookIndex) }
        _uiState.update { it.copy(hoveredInstanceId = instanceId, hoveredBook = book) }
    }

    fun handleDoubleTapInstance(instanceId: Int?) {
        if (instanceId != null) {
            selectBookInstance(instanceId, openDetailPanel = true)
            engine.focusOnInstance(instanceId)
        } else {
            engine.camera.resetToDefault()
        }
    }

    fun focusCameraOnSelectedBook() {
        val instId = _uiState.value.selectedInstanceId
            ?: _uiState.value.selectedBook?.let { b ->
                engine.instances.firstOrNull { it.archivalBookId == b.id }?.instanceId
            }
        if (instId != null) {
            engine.focusOnInstance(instId)
        }
    }

    fun resetCamera() {
        engine.camera.resetToDefault()
        _uiState.update { it.copy(hoveredInstanceId = null, hoveredBook = null) }
    }

    fun adjustCameraZoom(zoomDelta: Float) {
        engine.camera.targetZoom = (engine.camera.targetZoom + zoomDelta).coerceIn(0.45f, 2.75f)
    }

    fun toggleLeftFilterPanel(forceOpen: Boolean? = null) {
        _uiState.update { state ->
            state.copy(isLeftFilterOpen = forceOpen ?: !state.isLeftFilterOpen)
        }
    }

    fun toggleRightDetailPanel(forceOpen: Boolean? = null) {
        _uiState.update { state ->
            state.copy(isRightDetailOpen = forceOpen ?: !state.isRightDetailOpen)
        }
    }

    fun toggleImmersiveFullscreen() {
        _uiState.update { state ->
            val enteringFullscreen = !state.isImmersiveFullscreen
            state.copy(
                isImmersiveFullscreen = enteringFullscreen,
                isLeftFilterOpen = if (enteringFullscreen) false else state.isLeftFilterOpen,
                isRightDetailOpen = if (enteringFullscreen) false else state.isRightDetailOpen
            )
        }
    }

    fun toggleAutoRotateRings() {
        _uiState.update { it.copy(autoRotateRings = !it.autoRotateRings) }
    }

    fun cycleRenderQuality() {
        val presets = RenderQualityPreset.entries
        val nextPreset = presets[(_uiState.value.qualityPreset.ordinal + 1) % presets.size]
        _uiState.update { it.copy(qualityPreset = nextPreset) }
        engine.rebuildLibraryInstances(
            books = _uiState.value.allBooks,
            qualityPreset = nextPreset,
            variantOffsets = _uiState.value.coverVariantOffsets
        )
    }

    fun mutateSelectedBookCoverVariant() {
        val book = _uiState.value.selectedBook ?: return
        val nextOffset = ((_uiState.value.coverVariantOffsets[book.id] ?: 0) + 1) % 12
        val updatedOffsets = _uiState.value.coverVariantOffsets.toMutableMap().apply {
            put(book.id, nextOffset)
        }
        _uiState.update { it.copy(coverVariantOffsets = updatedOffsets) }
        engine.rebuildLibraryInstances(
            books = _uiState.value.allBooks,
            qualityPreset = _uiState.value.qualityPreset,
            variantOffsets = updatedOffsets
        )
    }

    fun setCoverStudioModalOpen(open: Boolean) {
        _uiState.update { it.copy(isCoverStudioModalOpen = open) }
    }

    fun updateFps(fps: Int) {
        _uiState.update { it.copy(currentFps = fps) }
    }

    fun setAboutModalOpen(open: Boolean) {
        _uiState.update { state ->
            state.copy(
                isAboutModalOpen = open,
                activeSection = if (!open && state.activeSection == NavigationSection.ABOUT) {
                    NavigationSection.EXPLORE
                } else {
                    state.activeSection
                }
            )
        }
    }

    fun setAddRecordModalOpen(open: Boolean) {
        _uiState.update { it.copy(isAddRecordModalOpen = open) }
    }

    fun addVerifiedArchivalRecord(
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
    ) {
        val cleanTitle = title.trim()
        val cleanAuthor = author.trim()
        if (cleanTitle.isEmpty() || cleanAuthor.isEmpty()) return

        val indexNum = _uiState.value.allBooks.size + 1
        val countryCode = country.trim().take(2).uppercase().ifEmpty { "GL" }
        val newBook = ArchivalBook(
            id = "bk-verified-$indexNum-${System.currentTimeMillis() % 10000}",
            catalogCode = "VER-$countryCode-$publicationYear-$indexNum",
            title = cleanTitle,
            author = cleanAuthor,
            country = country.trim().ifEmpty { "International" },
            region = "Global Archive",
            publicationYear = publicationYear,
            language = language.trim().ifEmpty { "Multilingual" },
            category = category,
            historicalBackground = historicalBackground.trim().ifEmpty {
                "User-contributed archival record documenting the publication history and cultural context of $cleanTitle."
            },
            censorshipStatus = censorshipStatus.trim().ifEmpty {
                "Verified historical restriction record submitted to the Forbidden Library repository."
            },
            restrictionSummary = "User-Submitted Verified Entry ($publicationYear)",
            sourceReference = sourceReference.trim().ifEmpty { "Primary Archival Citation" },
            sourceUrl = sourceUrl.trim().ifEmpty { "https://www.unesco.org/en/memory-world" },
            coverStyle = BookCoverStyle(
                primaryColorHex = 0xFF162C28,
                accentFoilHex = 0xFFF5D061,
                spineColorHex = 0xFF0E1D1A,
                heightScale = 1.16f,
                thicknessScale = 1.20f,
                widthScale = 1.08f,
                pattern = CoverPattern.ASTRONOMICAL_RINGS
            ),
            isDemonstrationRecord = false
        )

        val updatedBooks = listOf(newBook) + _uiState.value.allBooks
        _uiState.update { state ->
            state.copy(
                allBooks = updatedBooks,
                selectedBook = newBook,
                isAddRecordModalOpen = false,
                isRightDetailOpen = true
            )
        }
        engine.rebuildLibraryInstances(
            books = updatedBooks,
            qualityPreset = _uiState.value.qualityPreset,
            variantOffsets = _uiState.value.coverVariantOffsets
        )
        selectArchivalBookDirectly(newBook, focusCamera = true)
    }

    private fun syncSelectionToFirstMatchIfNeeded() {
        val state = _uiState.value
        val matches = state.matchingBooks
        if (matches.isNotEmpty() && state.selectedBook != null && matches.none { it.id == state.selectedBook.id }) {
            val firstMatch = matches.first()
            val inst = engine.instances.firstOrNull { it.archivalBookId == firstMatch.id }
            _uiState.update {
                it.copy(
                    selectedBook = firstMatch,
                    selectedInstanceId = inst?.instanceId
                )
            }
        }
    }
}
