package com.example.engine3d

import com.example.data.ArchivalBook
import com.example.data.BookCategory
import com.example.data.RenderQualityPreset
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Represents a single instanced 3D book positioned inside the concentric circular library,
 * enriched with procedural cover texture descriptors derived from Title, Author, and Category.
 */
class BookInstance3D(
    val instanceId: Int,
    val archivalBookIndex: Int,
    val archivalBookId: String,
    val bookTitle: String,
    val bookAuthor: String,
    val bookCategory: BookCategory,
    val ringIndex: Int,          // 0..4 concentric rings
    val tierIndex: Int,          // 0..5 vertical tiers
    val baseAngleRad: Float,     // Position around the ring (0..2PI)
    val ringRadius: Float,       // Distance from center axis
    val baseY: Float,            // Vertical world coordinate
    val halfWidth: Float,        // Spine thickness /2
    val halfHeight: Float,       // Book height /2
    val halfDepth: Float,        // Book cover radial depth /2
    val tiltAngleRad: Float,     // Subtle shelf lean
    val coverColorArgb: Int,
    val spineColorArgb: Int,
    val foilColorArgb: Int,
    val accentGlowArgb: Int,
    val pageBlockColorArgb: Int,
    val proceduralSpec: ProceduralCoverSpec,
    val normalizedMotifPoints: FloatArray
)

/**
 * 3D atmospheric dust / light mote particle floating inside the rotunda.
 */
class AtmosphericParticle3D(
    var x: Float,
    var y: Float,
    var z: Float,
    val speedY: Float,
    val orbitSpeed: Float,
    val size: Float,
    val isViolet: Boolean,
    val alphaBase: Float
)

/**
 * Camera state with smooth spring-like interpolation toward target yaw, pitch, zoom, and lookAt offset.
 */
class Camera3DState {
    var yaw: Float = 0.35f
    var pitch: Float = 0.28f
    var zoom: Float = 1.0f

    var targetYaw: Float = 0.35f
    var targetPitch: Float = 0.28f
    var targetZoom: Float = 1.0f

    var focusX: Float = 0f
    var focusY: Float = 0f
    var focusZ: Float = 0f

    var targetFocusX: Float = 0f
    var targetFocusY: Float = 0f
    var targetFocusZ: Float = 0f

    var yawVelocity: Float = 0f
    var pitchVelocity: Float = 0f

    fun resetToDefault() {
        targetYaw = 0.35f
        targetPitch = 0.28f
        targetZoom = 1.0f
        targetFocusX = 0f
        targetFocusY = 0f
        targetFocusZ = 0f
        yawVelocity = 0f
        pitchVelocity = 0f
    }

    fun stepSimulation(dt: Float, isDragging: Boolean) {
        if (!isDragging) {
            targetYaw += yawVelocity * dt
            targetPitch = (targetPitch + pitchVelocity * dt).coerceIn(-1.15f, 1.15f)
            val damping = (1f - 5.5f * dt).coerceIn(0f, 1f)
            yawVelocity *= damping
            pitchVelocity *= damping
        }
        val lerpFactor = (10f * dt).coerceIn(0.05f, 0.45f)
        yaw += (targetYaw - yaw) * lerpFactor
        pitch += (targetPitch - pitch) * lerpFactor
        zoom += (targetZoom - zoom) * lerpFactor
        focusX += (targetFocusX - focusX) * lerpFactor
        focusY += (targetFocusY - focusY) * lerpFactor
        focusZ += (targetFocusZ - focusZ) * lerpFactor
    }
}

/**
 * High-performance, zero-per-frame-allocation 3D instanced engine for the Forbidden Library rotunda.
 * Seamlessly binds shared procedural cover specifications to every instanced 3D volume.
 */
class Library3DEngine {
    companion object {
        const val MAX_INSTANCES = 1500
        const val RING_COUNT = 5
        const val TIER_COUNT = 6
        val RING_RADII = floatArrayOf(155f, 220f, 290f, 365f, 445f)
        val RING_ROTATION_SPEEDS = floatArrayOf(0.045f, -0.032f, 0.024f, -0.018f, 0.012f)
        val TIER_Y_POSITIONS = floatArrayOf(-165f, -99f, -33f, 33f, 99f, 165f)
    }

    val camera = Camera3DState()
    var ringRotationAngles = FloatArray(RING_COUNT)
    var instances: List<BookInstance3D> = emptyList()
        private set
    var particles: List<AtmosphericParticle3D> = emptyList()
        private set

    // Pre-allocated screen projection & depth-sorting buffers for 60 FPS rendering
    val visibleIndices = IntArray(MAX_INSTANCES)
    val depthValues = FloatArray(MAX_INSTANCES)
    var visibleCount = 0

    // Projected 2D coordinates of the 8 corners of each visible book instance: instanceId * 8
    val projectedCornersX = FloatArray(MAX_INSTANCES * 8)
    val projectedCornersY = FloatArray(MAX_INSTANCES * 8)
    val projectedCenterX = FloatArray(MAX_INSTANCES)
    val projectedCenterY = FloatArray(MAX_INSTANCES)
    val projectedScreenRadius = FloatArray(MAX_INSTANCES)
    val instanceLightingFactor = FloatArray(MAX_INSTANCES)
    val instanceFogFactor = FloatArray(MAX_INSTANCES)
    val instanceWorldAngle = FloatArray(MAX_INSTANCES)

    /**
     * Procedurally generates the 3D circular library of instanced books mapped to archival records
     * and binds each book's procedural cover design (derived from title, author, and category).
     */
    fun rebuildLibraryInstances(
        books: List<ArchivalBook>,
        qualityPreset: RenderQualityPreset,
        variantOffsets: Map<String, Int> = emptyMap()
    ) {
        if (books.isEmpty()) {
            instances = emptyList()
            return
        }

        // Pre-compute shared procedural specs and motif geometry per archival book (+ 4 edition variants)
        val specPool = Array(books.size) { bIdx ->
            val book = books[bIdx]
            val userVariant = variantOffsets[book.id] ?: 0
            Array(4) { subVariant ->
                val spec = ProceduralCoverTextureGenerator.deriveProceduralSpec(
                    book = book,
                    variantOffset = userVariant * 17 + subVariant
                )
                val motif = buildFastMotifArray(spec)
                spec to motif
            }
        }

        val targetTotal = qualityPreset.instanceCount.coerceAtMost(MAX_INSTANCES)
        val newInstances = ArrayList<BookInstance3D>(targetTotal)

        val radiusSum = RING_RADII.sum()
        var instanceCounter = 0

        for (ringIdx in 0 until RING_COUNT) {
            val radius = RING_RADII[ringIdx]
            val ringShare = (targetTotal * (radius / radiusSum)).toInt().coerceAtLeast(TIER_COUNT * 8)
            val booksPerTier = (ringShare / TIER_COUNT).coerceAtLeast(8)

            for (tierIdx in 0 until TIER_COUNT) {
                val baseY = TIER_Y_POSITIONS[tierIdx]
                val angleStep = (2.0 * PI / booksPerTier).toFloat()

                for (slot in 0 until booksPerTier) {
                    if (instanceCounter >= targetTotal) break

                    val seed = (ringIdx * 1009 + tierIdx * 313 + slot * 97)
                    val archivalIdx = (seed and 0x7FFFFFFF) % books.size
                    val archivalBook = books[archivalIdx]
                    val style = archivalBook.coverStyle

                    val subVariant = (slot + ringIdx + tierIdx) and 3
                    val (procSpec, motifPoints) = specPool[archivalIdx][subVariant]

                    val jitterAngle = (((seed * 31) % 100) / 100f - 0.5f) * angleStep * 0.22f
                    val angle = slot * angleStep + jitterAngle + (tierIdx * 0.11f)

                    val halfW = (3.4f * style.thicknessScale * (0.9f + ((seed % 5) * 0.05f))).coerceIn(2.2f, 5.5f)
                    val halfH = (11.5f * style.heightScale * (0.92f + ((seed % 7) * 0.025f))).coerceIn(8.5f, 15.5f)
                    val halfD = (8.2f * style.widthScale).coerceIn(6.5f, 11.0f)

                    val tilt = if (seed % 11 == 0) 0.12f else if (seed % 17 == 0) -0.10f else 0f

                    newInstances.add(
                        BookInstance3D(
                            instanceId = instanceCounter,
                            archivalBookIndex = archivalIdx,
                            archivalBookId = archivalBook.id,
                            bookTitle = archivalBook.title,
                            bookAuthor = archivalBook.author,
                            bookCategory = archivalBook.category,
                            ringIndex = ringIdx,
                            tierIndex = tierIdx,
                            baseAngleRad = angle,
                            ringRadius = radius,
                            baseY = baseY,
                            halfWidth = halfW,
                            halfHeight = halfH,
                            halfDepth = halfD,
                            tiltAngleRad = tilt,
                            coverColorArgb = procSpec.primaryClothArgb,
                            spineColorArgb = procSpec.secondaryClothArgb,
                            foilColorArgb = procSpec.foilArgb,
                            accentGlowArgb = procSpec.accentGlowArgb,
                            pageBlockColorArgb = 0xFFE8DFCE.toInt(),
                            proceduralSpec = procSpec,
                            normalizedMotifPoints = motifPoints
                        )
                    )
                    instanceCounter++
                }
            }
        }
        instances = newInstances

        // Rebuild atmospheric 3D particles
        val pCount = qualityPreset.particleCount
        val newParticles = ArrayList<AtmosphericParticle3D>(pCount)
        for (i in 0 until pCount) {
            val seed = i * 7919
            val angle = ((seed % 628) / 100f)
            val dist = 60f + (seed % 440)
            val py = -240f + (seed % 480)
            newParticles.add(
                AtmosphericParticle3D(
                    x = cos(angle) * dist,
                    y = py.toFloat(),
                    z = sin(angle) * dist,
                    speedY = -6f - (seed % 12),
                    orbitSpeed = 0.04f + ((seed % 10) * 0.008f),
                    size = 1.4f + (seed % 4) * 0.7f,
                    isViolet = (i % 4 == 0),
                    alphaBase = 0.30f + ((seed % 50) / 100f)
                )
            )
        }
        particles = newParticles
    }

    private fun buildFastMotifArray(spec: ProceduralCoverSpec): FloatArray {
        val segments = spec.radialSymmetryOrder.coerceIn(4, 10)
        val out = FloatArray(segments * 4)
        val k = spec.harmonicFrequency
        for (i in 0 until segments) {
            val a0 = (2f * PI.toFloat() * i) / segments
            val a1 = (2f * PI.toFloat() * ((i + k) % segments)) / segments
            out[i * 4 + 0] = cos(a0) * 0.25f
            out[i * 4 + 1] = sin(a0) * 0.20f
            out[i * 4 + 2] = cos(a1) * 0.25f
            out[i * 4 + 3] = sin(a1) * 0.20f
        }
        return out
    }

    /**
     * Advances ring rotations, floating particles, and camera physics.
     */
    fun updateFrame(
        dt: Float,
        isDragging: Boolean,
        autoRotateRings: Boolean,
        introCameraProgress: Float
    ) {
        val clampedDt = dt.coerceIn(0.001f, 0.05f)

        if (autoRotateRings) {
            for (r in 0 until RING_COUNT) {
                ringRotationAngles[r] = (ringRotationAngles[r] + RING_ROTATION_SPEEDS[r] * clampedDt) % (2f * PI.toFloat())
            }
        }

        for (i in particles.indices) {
            val p = particles[i]
            p.y += p.speedY * clampedDt
            if (p.y < -250f) p.y = 250f
            val cosO = cos(p.orbitSpeed * clampedDt)
            val sinO = sin(p.orbitSpeed * clampedDt)
            val nx = p.x * cosO - p.z * sinO
            val nz = p.x * sinO + p.z * cosO
            p.x = nx
            p.z = nz
        }

        if (introCameraProgress < 1f) {
            val t = smoothStep(introCameraProgress)
            camera.yaw = 1.6f * (1f - t) + camera.targetYaw * t
            camera.pitch = 0.92f * (1f - t) + camera.targetPitch * t
            camera.zoom = 0.42f * (1f - t) + camera.targetZoom * t
        } else {
            camera.stepSimulation(clampedDt, isDragging)
        }
    }

    /**
     * Smoothly flies the camera to focus on a specific 3D book instance in the circular rotunda.
     */
    fun focusOnInstance(instanceId: Int) {
        val inst = instances.getOrNull(instanceId) ?: return
        val worldAngle = inst.baseAngleRad + ringRotationAngles[inst.ringIndex]
        val wx = cos(worldAngle) * inst.ringRadius
        val wy = inst.baseY
        val wz = sin(worldAngle) * inst.ringRadius

        camera.targetFocusX = wx * 0.55f
        camera.targetFocusY = wy * 0.75f
        camera.targetFocusZ = wz * 0.55f

        camera.targetYaw = -worldAngle + (PI.toFloat() * 0.5f)
        camera.targetPitch = (wy / 420f).coerceIn(-0.45f, 0.45f)
        camera.targetZoom = 1.85f
        camera.yawVelocity = 0f
        camera.pitchVelocity = 0f
    }

    /**
     * Projects all 3D book cuboids into 2D viewport coordinates and sorts them back-to-front.
     */
    fun projectAndSortInstances(
        viewportWidth: Float,
        viewportHeight: Float,
        selectedInstanceId: Int?,
        hoveredInstanceId: Int?
    ) {
        val count = instances.size
        if (count == 0 || viewportWidth <= 0f || viewportHeight <= 0f) {
            visibleCount = 0
            return
        }

        val centerX = viewportWidth * 0.5f
        val centerY = viewportHeight * 0.5f
        val minDim = min(viewportWidth, viewportHeight)
        val fovScale = minDim * 0.92f * camera.zoom
        val camDist = 680f

        val cosYaw = cos(camera.yaw)
        val sinYaw = sin(camera.yaw)
        val cosPitch = cos(camera.pitch)
        val sinPitch = sin(camera.pitch)

        var visIdx = 0

        for (i in 0 until count) {
            val inst = instances[i]
            val worldAngle = inst.baseAngleRad + ringRotationAngles[inst.ringIndex]
            instanceWorldAngle[i] = worldAngle

            val isSelectedOrHovered = (inst.instanceId == selectedInstanceId || inst.instanceId == hoveredInstanceId)
            val radialPullOut = if (inst.instanceId == selectedInstanceId) -18f else if (inst.instanceId == hoveredInstanceId) -10f else 0f
            val r = inst.ringRadius + radialPullOut

            val cosA = cos(worldAngle)
            val sinA = sin(worldAngle)

            val wx = cosA * r - camera.focusX
            val wy = (inst.baseY - inst.halfHeight) - camera.focusY
            val wz = sinA * r - camera.focusZ

            val rx = wx * cosYaw - wz * sinYaw
            val rz1 = wx * sinYaw + wz * cosYaw
            val ry = wy * cosPitch - rz1 * sinPitch
            val rz = wy * sinPitch + rz1 * cosPitch + camDist

            if (rz < 90f || rz > 1550f) continue

            val invZ = fovScale / rz
            val scX = centerX + rx * invZ
            val scY = centerY + ry * invZ

            val approxRadius = max(inst.halfHeight, inst.halfDepth) * invZ * 1.45f
            if (scX + approxRadius < -40f || scX - approxRadius > viewportWidth + 40f ||
                scY + approxRadius < -40f || scY - approxRadius > viewportHeight + 40f
            ) {
                continue
            }

            projectedCenterX[i] = scX
            projectedCenterY[i] = scY
            projectedScreenRadius[i] = approxRadius

            val tx = -sinA
            val tz = cosA
            val rxAxis = cosA
            val rzAxis = sinA

            val baseOffset = i * 8
            for (corner in 0 until 8) {
                val sx = if ((corner and 1) == 0) -inst.halfWidth else inst.halfWidth
                val sy = if ((corner and 2) == 0) -inst.halfHeight else inst.halfHeight
                val sz = if ((corner and 4) == 0) -inst.halfDepth else inst.halfDepth

                val tiltedX = sx + sy * inst.tiltAngleRad

                val vx = wx + tx * tiltedX + rxAxis * sz
                val vy = wy + sy
                val vz = wz + tz * tiltedX + rzAxis * sz

                val crx = vx * cosYaw - vz * sinYaw
                val crz1 = vx * sinYaw + vz * cosYaw
                val cry = vy * cosPitch - crz1 * sinPitch
                val crz = max(40f, vy * sinPitch + crz1 * cosPitch + camDist)

                val cScale = fovScale / crz
                projectedCornersX[baseOffset + corner] = centerX + crx * cScale
                projectedCornersY[baseOffset + corner] = centerY + cry * cScale
            }

            val normalCameraZ = -(rxAxis * sinYaw + rzAxis * cosYaw)
            val coreProximity = (1f - (inst.ringRadius - 140f) / 340f).coerceIn(0.25f, 1f)
            val rimLight = 0.55f + 0.45f * abs(normalCameraZ)
            instanceLightingFactor[i] = (rimLight * (0.65f + 0.35f * coreProximity) + if (isSelectedOrHovered) 0.35f else 0f)
                .coerceIn(0.25f, 1.35f)

            instanceFogFactor[i] = ((rz - 220f) / 1080f).coerceIn(0f, 0.82f)

            visibleIndices[visIdx] = i
            depthValues[visIdx] = rz
            visIdx++
        }

        visibleCount = visIdx
        quicksortByDepthDesc(0, visibleCount - 1)
    }

    fun hitTestBookAt(
        touchX: Float,
        touchY: Float,
        matchingBookIds: Set<String>,
        hasActiveFilter: Boolean
    ): Int? {
        for (sortIdx in visibleCount - 1 downTo 0) {
            val instIdx = visibleIndices[sortIdx]
            val inst = instances[instIdx]
            if (hasActiveFilter && !matchingBookIds.contains(inst.archivalBookId)) continue

            val cx = projectedCenterX[instIdx]
            val cy = projectedCenterY[instIdx]
            val hitRadius = max(projectedScreenRadius[instIdx] * 1.15f, 22f)
            val dx = touchX - cx
            val dy = touchY - cy
            if (dx * dx + dy * dy <= hitRadius * hitRadius) {
                return instIdx
            }
        }

        if (hasActiveFilter) {
            for (sortIdx in visibleCount - 1 downTo 0) {
                val instIdx = visibleIndices[sortIdx]
                val cx = projectedCenterX[instIdx]
                val cy = projectedCenterY[instIdx]
                val hitRadius = max(projectedScreenRadius[instIdx], 18f)
                val dx = touchX - cx
                val dy = touchY - cy
                if (dx * dx + dy * dy <= hitRadius * hitRadius) {
                    return instIdx
                }
            }
        }
        return null
    }

    private fun quicksortByDepthDesc(low: Int, high: Int) {
        if (low >= high) return
        val pivotDepth = depthValues[(low + high) ushr 1]
        var i = low
        var j = high
        while (i <= j) {
            while (depthValues[i] > pivotDepth) i++
            while (depthValues[j] < pivotDepth) j--
            if (i <= j) {
                val tmpIdx = visibleIndices[i]
                visibleIndices[i] = visibleIndices[j]
                visibleIndices[j] = tmpIdx

                val tmpDepth = depthValues[i]
                depthValues[i] = depthValues[j]
                depthValues[j] = tmpDepth
                i++
                j++
            }
        }
        if (low < j) quicksortByDepthDesc(low, j)
        if (i < high) quicksortByDepthDesc(i, high)
    }

    private fun smoothStep(x: Float): Float {
        val t = x.coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }
}
