package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.NavigationSection
import com.example.engine3d.Library3DViewport
import com.example.ui.components.AboutInstallationModal
import com.example.ui.components.AddVerifiedRecordModal
import com.example.ui.components.BottomTimelineAndCameraBar
import com.example.ui.components.CinematicIntroOverlay
import com.example.ui.components.LeftArchiveFilterPanel
import com.example.ui.components.ProceduralCover3DStudioModal
import com.example.ui.components.RightBookDetailPanel
import com.example.ui.components.TopInstallationNavBar
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BorderHairlineGold
import com.example.ui.theme.ForbiddenLibraryTheme
import com.example.ui.theme.GlassPanelBg
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.IvoryWhite
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ParchmentMuted
import com.example.viewmodel.ForbiddenLibraryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForbiddenLibraryTheme {
                ForbiddenLibraryExperience()
            }
        }
    }
}

@Composable
fun ForbiddenLibraryExperience(
    viewModel: ForbiddenLibraryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var introCameraProgress by remember { mutableFloatStateOf(1f) }

    BackHandler(
        enabled = uiState.isCoverStudioModalOpen ||
            uiState.isAboutModalOpen ||
            uiState.isAddRecordModalOpen ||
            uiState.isRightDetailOpen ||
            uiState.isLeftFilterOpen ||
            uiState.isImmersiveFullscreen
    ) {
        when {
            uiState.isCoverStudioModalOpen -> viewModel.setCoverStudioModalOpen(false)
            uiState.isAboutModalOpen -> viewModel.setAboutModalOpen(false)
            uiState.isAddRecordModalOpen -> viewModel.setAddRecordModalOpen(false)
            uiState.isRightDetailOpen -> viewModel.toggleRightDetailPanel(false)
            uiState.isLeftFilterOpen -> viewModel.toggleLeftFilterPanel(false)
            uiState.isImmersiveFullscreen -> viewModel.toggleImmersiveFullscreen()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        val isWideScreen = maxWidth >= 720.dp
        val activeFilterCount = (if (uiState.searchQuery.isNotBlank()) 1 else 0) +
            uiState.selectedCategories.size +
            uiState.selectedCountries.size +
            (if (uiState.selectedEra.id != "all" || uiState.yearRangeStart > -250 || uiState.yearRangeEnd < 2026) 1 else 0)

        // 1. Full-Screen Interactive 3D Circular Library World (Center)
        Library3DViewport(
            engine = viewModel.engine,
            matchingBookIds = uiState.matchingBookIds,
            hasActiveFilter = uiState.hasActiveFilter,
            selectedInstanceId = uiState.selectedInstanceId,
            hoveredInstanceId = uiState.hoveredInstanceId,
            autoRotateRings = uiState.autoRotateRings,
            introCameraProgress = introCameraProgress,
            onSelectInstance = { instId ->
                viewModel.selectBookInstance(instId, openDetailPanel = true)
            },
            onHoverInstance = { instId ->
                viewModel.hoverBookInstance(instId)
            },
            onDoubleTapInstance = { instId ->
                viewModel.handleDoubleTapInstance(instId)
            },
            onFpsUpdated = { fps ->
                viewModel.updateFps(fps)
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Architectural HUD Overlay (Top, Left, Right, Bottom)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // Top Navigation & Telemetry Bar
            AnimatedVisibility(
                visible = !uiState.isImmersiveFullscreen && !uiState.showCinematicIntro,
                enter = fadeIn() + slideInVertically { -it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 },
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TopInstallationNavBar(
                        activeSection = uiState.activeSection,
                        hasActiveFilter = uiState.hasActiveFilter,
                        activeFilterCount = activeFilterCount,
                        isLeftFilterOpen = uiState.isLeftFilterOpen,
                        isRightDetailOpen = uiState.isRightDetailOpen,
                        isImmersiveFullscreen = uiState.isImmersiveFullscreen,
                        qualityPreset = uiState.qualityPreset,
                        currentFps = uiState.currentFps,
                        onSelectSection = viewModel::selectNavigationSection,
                        onToggleLeftFilter = { viewModel.toggleLeftFilterPanel() },
                        onToggleRightDetail = { viewModel.toggleRightDetailPanel() },
                        onOpenAddRecord = { viewModel.setAddRecordModalOpen(true) },
                        onCycleQuality = viewModel::cycleRenderQuality,
                        onToggleImmersive = viewModel::toggleImmersiveFullscreen
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Subtle Gesture Hint Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .border(1.dp, BorderHairlineGold, RoundedCornerShape(3.dp))
                            .background(ObsidianBlack.copy(alpha = 0.72f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Drag to orbit 3D rings · Pinch to zoom · Tap a book · Double-tap to focus",
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentMuted
                        )
                    }
                }
            }

            // Floating Exit Immersive Mode Button when HUD is hidden
            AnimatedVisibility(
                visible = uiState.isImmersiveFullscreen && !uiState.showCinematicIntro,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Surface(
                    color = GlassPanelBg,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, BorderHairlineGold),
                    modifier = Modifier
                        .clickable { viewModel.toggleImmersiveFullscreen() }
                        .padding(4.dp)
                        .testTag("exit_immersive_hud_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FullscreenExit,
                            contentDescription = "Restore Interface",
                            tint = GoldBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RESTORE HUD",
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldBright
                        )
                    }
                }
            }

            // Hovered / Long-Pressed Book Floating Tooltip
            val hoveredBook = uiState.hoveredBook
            AnimatedVisibility(
                visible = hoveredBook != null && !uiState.showCinematicIntro,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 180.dp)
            ) {
                if (hoveredBook != null) {
                    Surface(
                        color = ObsidianBlack.copy(alpha = 0.90f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, GoldBright),
                        modifier = Modifier
                            .clickable {
                                viewModel.selectArchivalBookDirectly(hoveredBook, focusCamera = true)
                                viewModel.hoverBookInstance(null)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${hoveredBook.country.uppercase()} · ${hoveredBook.formattedYear}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberPrimary
                            )
                            Text(
                                text = hoveredBook.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = IvoryWhite
                            )
                            Text(
                                text = hoveredBook.author,
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldBright
                            )
                        }
                    }
                }
            }

            // Middle Workspace: Left Filter Panel & Right Book Detail Panel
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = if (uiState.isImmersiveFullscreen) 48.dp else 118.dp,
                        bottom = if (uiState.isImmersiveFullscreen) 12.dp else if (uiState.activeSection == NavigationSection.TIMELINE) 144.dp else 94.dp
                    )
            ) {
                // Left Panel: Search, Category Filters, Country Filters, Matching Directory
                AnimatedVisibility(
                    visible = uiState.isLeftFilterOpen && !uiState.isImmersiveFullscreen && !uiState.showCinematicIntro,
                    enter = slideInHorizontally { -it } + fadeIn(),
                    exit = slideOutHorizontally { -it } + fadeOut(),
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    LeftArchiveFilterPanel(
                        searchQuery = uiState.searchQuery,
                        selectedCategories = uiState.selectedCategories,
                        selectedCountries = uiState.selectedCountries,
                        availableCountries = uiState.availableCountries,
                        matchingBooks = uiState.matchingBooks,
                        selectedBookId = uiState.selectedBook?.id,
                        hasActiveFilter = uiState.hasActiveFilter,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onToggleCategory = viewModel::toggleCategoryFilter,
                        onToggleCountry = viewModel::toggleCountryFilter,
                        onSelectBookFromList = { book ->
                            viewModel.selectArchivalBookDirectly(book, focusCamera = true)
                            if (!isWideScreen) {
                                viewModel.toggleLeftFilterPanel(false)
                            }
                        },
                        onClearAllFilters = viewModel::clearAllFilters,
                        onClosePanel = { viewModel.toggleLeftFilterPanel(false) },
                        modifier = Modifier
                            .width(if (isWideScreen) 330.dp else 320.dp)
                            .fillMaxHeight()
                    )
                }

                // Right Panel: Selected Book Information, Procedural 3D Cover, Historical Context, Censorship Status
                AnimatedVisibility(
                    visible = uiState.isRightDetailOpen && !uiState.isImmersiveFullscreen && !uiState.showCinematicIntro,
                    enter = slideInHorizontally { it } + fadeIn(),
                    exit = slideOutHorizontally { it } + fadeOut(),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    RightBookDetailPanel(
                        book = uiState.selectedBook,
                        selectedInstanceId = uiState.selectedInstanceId,
                        coverVariantOffset = uiState.selectedBookVariantOffset,
                        onFocusIn3D = viewModel::focusCameraOnSelectedBook,
                        onOpenCoverStudio = { viewModel.setCoverStudioModalOpen(true) },
                        onMutateCoverVariant = viewModel::mutateSelectedBookCoverVariant,
                        onPreviousBook = { viewModel.stepSelectedBook(-1) },
                        onNextBook = { viewModel.stepSelectedBook(1) },
                        onClosePanel = { viewModel.toggleRightDetailPanel(false) },
                        modifier = Modifier
                            .width(if (isWideScreen) 360.dp else 330.dp)
                            .fillMaxHeight()
                    )
                }
            }

            // Bottom Timeline & Camera Control Bar
            AnimatedVisibility(
                visible = !uiState.isImmersiveFullscreen && !uiState.showCinematicIntro,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                BottomTimelineAndCameraBar(
                    selectedEra = uiState.selectedEra,
                    yearRangeStart = uiState.yearRangeStart,
                    yearRangeEnd = uiState.yearRangeEnd,
                    showExpandedTimelineScrubber = (uiState.activeSection == NavigationSection.TIMELINE),
                    total3DVolumes = viewModel.engine.instances.size,
                    matchingRecordsCount = uiState.matchingBooks.size,
                    totalRecordsCount = uiState.allBooks.size,
                    selectedBook = uiState.selectedBook,
                    autoRotateRings = uiState.autoRotateRings,
                    hasActiveFilter = uiState.hasActiveFilter,
                    onSelectEra = viewModel::selectHistoricalEra,
                    onUpdateYearRange = viewModel::updateYearRange,
                    onResetCamera = viewModel::resetCamera,
                    onFocusSelectedBook = viewModel::focusCameraOnSelectedBook,
                    onToggleAutoRotate = viewModel::toggleAutoRotateRings,
                    onZoomIn = { viewModel.adjustCameraZoom(0.28f) },
                    onZoomOut = { viewModel.adjustCameraZoom(-0.28f) },
                    onOpenSelectedBookDetail = { viewModel.toggleRightDetailPanel(true) },
                    onClearFilters = viewModel::clearAllFilters
                )
            }
        }

        // 3. Cinematic Introduction Sequence Overlay (First Launch or Replay)
        CinematicIntroOverlay(
            visible = uiState.showCinematicIntro,
            onIntroCameraProgress = { progress ->
                introCameraProgress = progress
            },
            onFinishOrSkip = viewModel::completeOrSkipIntro
        )

        // 4. About the Installation Modal
        if (uiState.isAboutModalOpen) {
            AboutInstallationModal(
                qualityPreset = uiState.qualityPreset,
                totalRecords = uiState.allBooks.size,
                onReplayIntro = viewModel::replayIntro,
                onCycleQuality = viewModel::cycleRenderQuality,
                onDismiss = { viewModel.setAboutModalOpen(false) }
            )
        }

        // 5. Add Verified Archival Record Modal
        if (uiState.isAddRecordModalOpen) {
            AddVerifiedRecordModal(
                onAddRecord = viewModel::addVerifiedArchivalRecord,
                onDismiss = { viewModel.setAddRecordModalOpen(false) }
            )
        }

        // 6. Procedural 3D Book Cover Texture Studio Modal
        val activeBook = uiState.selectedBook
        if (uiState.isCoverStudioModalOpen && activeBook != null) {
            ProceduralCover3DStudioModal(
                book = activeBook,
                variantOffset = uiState.selectedBookVariantOffset,
                onMutateVariant = viewModel::mutateSelectedBookCoverVariant,
                onDismiss = { viewModel.setCoverStudioModalOpen(false) }
            )
        }
    }
}
