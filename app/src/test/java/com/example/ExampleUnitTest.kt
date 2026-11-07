package com.example

import com.example.data.BookCategory
import com.example.data.ForbiddenLibraryRepository
import com.example.data.HistoricalEra
import com.example.data.RenderQualityPreset
import com.example.engine3d.Library3DEngine
import com.example.engine3d.ProceduralCoverTextureGenerator
import com.example.viewmodel.ForbiddenLibraryViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun engineGeneratesAndProjects3DBookVolumesWithProceduralSpecs() {
        val engine = Library3DEngine()
        engine.rebuildLibraryInstances(
            books = ForbiddenLibraryRepository.initialArchivalBooks,
            qualityPreset = RenderQualityPreset.HIGH
        )
        assertTrue("Expected > 1000 3D book volumes", engine.instances.size >= 1000)

        // Verify procedural cover spec and motif vectors are bound to every instanced volume
        val sampleInstance = engine.instances.first()
        assertNotNull(sampleInstance.proceduralSpec)
        assertTrue(sampleInstance.normalizedMotifPoints.isNotEmpty())
        assertTrue(sampleInstance.proceduralSpec.hexSignature.startsWith("0x"))

        engine.updateFrame(dt = 0.016f, isDragging = false, autoRotateRings = true, introCameraProgress = 1f)
        engine.projectAndSortInstances(
            viewportWidth = 1080f,
            viewportHeight = 2400f,
            selectedInstanceId = 0,
            hoveredInstanceId = null
        )
        assertTrue("Expected visible projected 3D books", engine.visibleCount > 0)
    }

    @Test
    fun proceduralCoverGeneratorProducesUniqueSpecsFromTitleAuthorCategory() {
        val books = ForbiddenLibraryRepository.initialArchivalBooks
        val galileoSpec0 = ProceduralCoverTextureGenerator.deriveProceduralSpec(books[0], variantOffset = 0)
        val galileoSpec1 = ProceduralCoverTextureGenerator.deriveProceduralSpec(books[0], variantOffset = 1)
        val miltonSpec0 = ProceduralCoverTextureGenerator.deriveProceduralSpec(books[2], variantOffset = 0)

        assertNotEquals(galileoSpec0.seedHash, galileoSpec1.seedHash)
        assertNotEquals(galileoSpec0.seedHash, miltonSpec0.seedHash)
        assertEquals(BookCategory.SCIENCE, galileoSpec0.category)
        assertEquals(BookCategory.POLITICS, miltonSpec0.category)
    }

    @Test
    fun viewModelFiltersAndMutatesProceduralCoverVariants() {
        val vm = ForbiddenLibraryViewModel()
        val initialCount = vm.uiState.value.allBooks.size
        assertEquals(24, initialCount)

        // Mutate procedural cover variant
        assertEquals(0, vm.uiState.value.selectedBookVariantOffset)
        vm.mutateSelectedBookCoverVariant()
        assertEquals(1, vm.uiState.value.selectedBookVariantOffset)

        // Search filter
        vm.updateSearchQuery("Galileo")
        assertEquals(1, vm.uiState.value.matchingBooks.size)
        assertEquals("Italy", vm.uiState.value.matchingBooks.first().country)

        vm.clearAllFilters()
        assertFalse(vm.uiState.value.hasActiveFilter)

        // Category filter
        vm.toggleCategoryFilter(BookCategory.SCIENCE)
        assertTrue(vm.uiState.value.matchingBooks.all { it.category == BookCategory.SCIENCE })

        // Timeline filter
        vm.clearAllFilters()
        vm.selectHistoricalEra(HistoricalEra.ENLIGHTENMENT)
        assertTrue(vm.uiState.value.matchingBooks.all { it.publicationYear in 1600..1799 })
    }
}
