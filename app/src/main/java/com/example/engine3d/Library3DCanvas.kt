package com.example.engine3d

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CosmicViolet
import com.example.ui.theme.DeepArchiveBlue
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.IvoryWhite
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.VioletGlow
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun Library3DViewport(
    engine: Library3DEngine,
    matchingBookIds: Set<String>,
    hasActiveFilter: Boolean,
    selectedInstanceId: Int?,
    hoveredInstanceId: Int?,
    autoRotateRings: Boolean,
    introCameraProgress: Float,
    onSelectInstance: (Int) -> Unit,
    onHoverInstance: (Int?) -> Unit,
    onDoubleTapInstance: (Int?) -> Unit,
    onFpsUpdated: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var frameTick by remember { mutableLongStateOf(0L) }
    var isUserDragging by remember { mutableStateOf(false) }
    val reusableFacePath = remember { Path() }
    val reusableRingPath = remember { Path() }

    // Continuous 60 FPS animation loop
    LaunchedEffect(engine, autoRotateRings, introCameraProgress) {
        var lastNanos = 0L
        var fpsFrames = 0
        var fpsAccumSeconds = 0f

        while (true) {
            withFrameNanos { nanos ->
                val dt = if (lastNanos == 0L) 0.016f else ((nanos - lastNanos) / 1_000_000_000f)
                lastNanos = nanos

                engine.updateFrame(
                    dt = dt,
                    isDragging = isUserDragging,
                    autoRotateRings = autoRotateRings,
                    introCameraProgress = introCameraProgress
                )
                frameTick = nanos

                fpsFrames++
                fpsAccumSeconds += dt
                if (fpsAccumSeconds >= 0.6f) {
                    val currentFps = (fpsFrames / fpsAccumSeconds).toInt().coerceIn(1, 120)
                    onFpsUpdated(currentFps)
                    fpsFrames = 0
                    fpsAccumSeconds = 0f
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("library_3d_viewport")
            .pointerInput(matchingBookIds, hasActiveFilter) {
                detectTapGestures(
                    onTap = { offset ->
                        val hitIdx = engine.hitTestBookAt(
                            touchX = offset.x,
                            touchY = offset.y,
                            matchingBookIds = matchingBookIds,
                            hasActiveFilter = hasActiveFilter
                        )
                        if (hitIdx != null) {
                            onSelectInstance(hitIdx)
                        }
                    },
                    onDoubleTap = { offset ->
                        val hitIdx = engine.hitTestBookAt(
                            touchX = offset.x,
                            touchY = offset.y,
                            matchingBookIds = matchingBookIds,
                            hasActiveFilter = hasActiveFilter
                        )
                        onDoubleTapInstance(hitIdx)
                    },
                    onLongPress = { offset ->
                        val hitIdx = engine.hitTestBookAt(
                            touchX = offset.x,
                            touchY = offset.y,
                            matchingBookIds = matchingBookIds,
                            hasActiveFilter = hasActiveFilter
                        )
                        onHoverInstance(hitIdx)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoomChange, _ ->
                    isUserDragging = true
                    if (abs(zoomChange - 1f) > 0.004f) {
                        engine.camera.targetZoom = (engine.camera.targetZoom * zoomChange).coerceIn(0.45f, 2.75f)
                    }
                    val sensitivity = 0.0048f / engine.camera.zoom.coerceAtLeast(0.6f)
                    engine.camera.targetYaw -= pan.x * sensitivity
                    engine.camera.targetPitch = (engine.camera.targetPitch + pan.y * sensitivity)
                        .coerceIn(-1.12f, 1.12f)
                    engine.camera.yawVelocity = -pan.x * sensitivity * 14f
                    engine.camera.pitchVelocity = pan.y * sensitivity * 10f
                    isUserDragging = false
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val unusedTick = frameTick
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas

            // 1. Atmospheric background nebula & volumetric core glow
            drawAtmosphericVoid(w, h, engine.camera)

            // 2. Architectural 3D circular library rings & central knowledge column
            drawRotundaArchitecture(w, h, engine, reusableRingPath)

            // 3. Project and depth-sort all 3D book volumes
            engine.projectAndSortInstances(
                viewportWidth = w,
                viewportHeight = h,
                selectedInstanceId = selectedInstanceId,
                hoveredInstanceId = hoveredInstanceId
            )

            // 4. Draw depth-sorted 3D book cuboids with procedural cover textures & motifs
            val visCount = engine.visibleCount
            for (s in 0 until visCount) {
                val instIdx = engine.visibleIndices[s]
                val inst = engine.instances[instIdx]
                val isMatching = !hasActiveFilter || matchingBookIds.contains(inst.archivalBookId)
                val isSelected = (inst.instanceId == selectedInstanceId)
                val isHovered = (inst.instanceId == hoveredInstanceId)

                drawBookCuboid3D(
                    engine = engine,
                    instIdx = instIdx,
                    inst = inst,
                    isMatching = isMatching,
                    isSelected = isSelected,
                    isHovered = isHovered,
                    facePath = reusableFacePath
                )
            }

            // 5. Floating 3D dust motes & volumetric light particles
            drawAtmosphericParticles3D(w, h, engine)
        }
    }
}

private fun DrawScope.drawAtmosphericVoid(
    w: Float,
    h: Float,
    camera: Camera3DState
) {
    val cx = w * 0.5f - camera.focusX * 0.25f
    val cy = h * 0.5f - camera.focusY * 0.25f
    val maxR = min(w, h) * 0.85f * camera.zoom.coerceIn(0.6f, 1.5f)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                CosmicViolet.copy(alpha = 0.20f),
                DeepArchiveBlue.copy(alpha = 0.14f),
                ObsidianBlack.copy(alpha = 0.0f)
            ),
            center = Offset(cx, cy),
            radius = maxR * 1.15f
        ),
        radius = maxR * 1.15f,
        center = Offset(cx, cy)
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                AmberPrimary.copy(alpha = 0.24f),
                GoldAccent.copy(alpha = 0.10f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = maxR * 0.58f
        ),
        radius = maxR * 0.58f,
        center = Offset(cx, cy)
    )
}

private fun DrawScope.drawRotundaArchitecture(
    w: Float,
    h: Float,
    engine: Library3DEngine,
    ringPath: Path
) {
    val centerX = w * 0.5f
    val centerY = h * 0.5f
    val minDim = min(w, h)
    val fovScale = minDim * 0.92f * engine.camera.zoom
    val camDist = 680f

    val cosYaw = cos(engine.camera.yaw)
    val sinYaw = sin(engine.camera.yaw)
    val cosPitch = cos(engine.camera.pitch)
    val sinPitch = sin(engine.camera.pitch)

    fun projectPoint(wx: Float, wy: Float, wz: Float): Offset? {
        val px = wx - engine.camera.focusX
        val py = wy - engine.camera.focusY
        val pz = wz - engine.camera.focusZ

        val rx = px * cosYaw - pz * sinYaw
        val rz1 = px * sinYaw + pz * cosYaw
        val ry = py * cosPitch - rz1 * sinPitch
        val rz = py * sinPitch + rz1 * cosPitch + camDist
        if (rz < 70f) return null
        val s = fovScale / rz
        return Offset(centerX + rx * s, centerY + ry * s)
    }

    val segments = 48
    for (ringIdx in 0 until Library3DEngine.RING_COUNT) {
        val radius = Library3DEngine.RING_RADII[ringIdx]
        val ringAlpha = (0.16f - ringIdx * 0.02f).coerceAtLeast(0.06f)

        for (tierIdx in 0 until Library3DEngine.TIER_COUNT step 2) {
            val yLevel = Library3DEngine.TIER_Y_POSITIONS[tierIdx]
            ringPath.reset()
            var started = false

            for (seg in 0..segments) {
                val angle = (seg.toFloat() / segments) * (2f * PI.toFloat())
                val pt = projectPoint(cos(angle) * radius, yLevel, sin(angle) * radius)
                if (pt != null) {
                    if (!started) {
                        ringPath.moveTo(pt.x, pt.y)
                        started = true
                    } else {
                        ringPath.lineTo(pt.x, pt.y)
                    }
                }
            }
            if (started) {
                drawPath(
                    path = ringPath,
                    color = if (ringIdx % 2 == 0) GoldAccent.copy(alpha = ringAlpha) else VioletGlow.copy(alpha = ringAlpha),
                    style = Stroke(width = 1.1f)
                )
            }
        }
    }

    val topCore = projectPoint(0f, -210f, 0f)
    val botCore = projectPoint(0f, 210f, 0f)
    if (topCore != null && botCore != null) {
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    GoldBright.copy(alpha = 0.45f),
                    AmberPrimary.copy(alpha = 0.65f),
                    GoldBright.copy(alpha = 0.45f),
                    Color.Transparent
                )
            ),
            start = topCore,
            end = botCore,
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawBookCuboid3D(
    engine: Library3DEngine,
    instIdx: Int,
    inst: BookInstance3D,
    isMatching: Boolean,
    isSelected: Boolean,
    isHovered: Boolean,
    facePath: Path
) {
    val baseOffset = instIdx * 8
    val px = engine.projectedCornersX
    val py = engine.projectedCornersY

    val light = engine.instanceLightingFactor[instIdx]
    val fog = engine.instanceFogFactor[instIdx]
    val visibilityAlpha = if (isMatching) 1f else 0.14f

    val coverColor = shadeAndFogColor(
        argb = inst.coverColorArgb,
        light = light * (if (isSelected) 1.35f else 1.0f),
        fog = if (isSelected) fog * 0.25f else fog,
        alpha = visibilityAlpha
    )
    val spineColor = shadeAndFogColor(
        argb = inst.spineColorArgb,
        light = light * 0.88f,
        fog = if (isSelected) fog * 0.25f else fog,
        alpha = visibilityAlpha
    )
    val pageColor = shadeAndFogColor(
        argb = inst.pageBlockColorArgb,
        light = light * 1.05f,
        fog = fog,
        alpha = visibilityAlpha
    )
    val foilColor = shadeAndFogColor(
        argb = inst.foilColorArgb,
        light = (light * 1.25f).coerceAtMost(1.5f),
        fog = fog * 0.4f,
        alpha = if (isMatching) 0.95f else 0.18f
    )
    val accentColor = shadeAndFogColor(
        argb = inst.accentGlowArgb,
        light = (light * 1.25f).coerceAtMost(1.5f),
        fog = fog * 0.35f,
        alpha = if (isMatching) 0.85f else 0.15f
    )

    if (isSelected || isHovered) {
        val cx = engine.projectedCenterX[instIdx]
        val cy = engine.projectedCenterY[instIdx]
        val r = engine.projectedScreenRadius[instIdx] * (if (isSelected) 2.2f else 1.7f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    if (isSelected) GoldBright.copy(alpha = 0.55f) else AmberPrimary.copy(alpha = 0.38f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = r.coerceAtLeast(12f)
            ),
            radius = r.coerceAtLeast(12f),
            center = Offset(cx, cy)
        )
    }

    // 1. Determine visible Side Cover Face (-w: corners 0, 4, 6, 2 or +w: corners 1, 5, 7, 3)
    val sideCrossNeg = cross2D(
        px[baseOffset + 0], py[baseOffset + 0],
        px[baseOffset + 4], py[baseOffset + 4],
        px[baseOffset + 6], py[baseOffset + 6]
    )
    val cTopLeft: Int
    val cTopRight: Int
    val cBotRight: Int
    val cBotLeft: Int
    if (sideCrossNeg > 0f) {
        cTopLeft = 0; cTopRight = 4; cBotRight = 6; cBotLeft = 2
    } else {
        cTopLeft = 1; cTopRight = 5; cBotRight = 7; cBotLeft = 3
    }
    drawQuad(facePath, px, py, baseOffset, cTopLeft, cTopRight, cBotRight, cBotLeft, coverColor)

    // Render perspective-projected procedural cover design (foil frame + title lines + category motif)
    // when the book cover face is large enough on screen (LOD optimization for 60 FPS instancing)
    val screenRad = engine.projectedScreenRadius[instIdx]
    if (isMatching && (screenRad > 9.5f || isSelected || isHovered)) {
        drawProceduralCoverQuadOverlay(
            px = px,
            py = py,
            baseOffset = baseOffset,
            iTL = cTopLeft,
            iTR = cTopRight,
            iBR = cBotRight,
            iBL = cBotLeft,
            inst = inst,
            foilColor = foilColor,
            accentColor = accentColor,
            isSelected = isSelected,
            screenRadius = screenRad
        )
    }

    // 2. Top/Bottom Page Block Edge (-h: 0, 1, 5, 4 or +h: 2, 3, 7, 6)
    if (engine.camera.pitch >= 0f) {
        drawQuad(facePath, px, py, baseOffset, 0, 1, 5, 4, pageColor)
    } else {
        drawQuad(facePath, px, py, baseOffset, 2, 3, 7, 6, pageColor)
    }

    // 3. Inner Spine Face (-d: 0, 1, 3, 2)
    val distInnerSq = hypotSq(px[baseOffset + 0] - px[baseOffset + 4], py[baseOffset + 0] - py[baseOffset + 4])
    drawQuad(facePath, px, py, baseOffset, 0, 1, 3, 2, spineColor)

    // 4. Procedural Spine Ribs & Title Band
    if (distInnerSq > 8f && isMatching) {
        val topLeftX = px[baseOffset + 0]
        val topLeftY = py[baseOffset + 0]
        val topRightX = px[baseOffset + 1]
        val topRightY = py[baseOffset + 1]
        val botLeftX = px[baseOffset + 2]
        val botLeftY = py[baseOffset + 2]
        val botRightX = px[baseOffset + 3]
        val botRightY = py[baseOffset + 3]

        val ribs = inst.proceduralSpec.spineRibCount
        for (rIdx in 1..ribs) {
            val frac = rIdx.toFloat() / (ribs + 1)
            if (frac in 0.35f..0.65f) continue
            val lx = topLeftX + (botLeftX - topLeftX) * frac
            val ly = topLeftY + (botLeftY - topLeftY) * frac
            val rx = topRightX + (botRightX - topRightX) * frac
            val ry = topRightY + (botRightY - topRightY) * frac
            drawLine(
                color = foilColor,
                start = Offset(lx, ly),
                end = Offset(rx, ry),
                strokeWidth = if (isSelected) 2.0f else 1.1f
            )
        }

        // Vertical spine title plate line in the center of the spine
        if (screenRad > 13f || isSelected) {
            val midTopX = (topLeftX + topRightX) * 0.5f
            val midTopY = (topLeftY + topRightY) * 0.5f
            val midBotX = (botLeftX + botRightX) * 0.5f
            val midBotY = (botLeftY + botRightY) * 0.5f
            val sStartX = midTopX + (midBotX - midTopX) * 0.34f
            val sStartY = midTopY + (midBotY - midTopY) * 0.34f
            val sEndX = midTopX + (midBotX - midTopX) * 0.66f
            val sEndY = midTopY + (midBotY - midTopY) * 0.66f
            drawLine(
                color = accentColor,
                start = Offset(sStartX, sStartY),
                end = Offset(sEndX, sEndY),
                strokeWidth = if (isSelected) 2.2f else 1.3f
            )
        }
    }

    if (isSelected || isHovered) {
        val strokeColor = if (isSelected) GoldBright else IvoryWhite.copy(alpha = 0.85f)
        facePath.reset()
        facePath.moveTo(px[baseOffset + 0], py[baseOffset + 0])
        facePath.lineTo(px[baseOffset + 1], py[baseOffset + 1])
        facePath.lineTo(px[baseOffset + 3], py[baseOffset + 3])
        facePath.lineTo(px[baseOffset + 2], py[baseOffset + 2])
        facePath.close()
        drawPath(
            path = facePath,
            color = strokeColor,
            style = Stroke(width = if (isSelected) 2.2f else 1.4f)
        )
    }
}

/**
 * Projects each book's unique procedural cover design (foil frame, title typography bars,
 * category geometric motif, and author footer) directly onto the 3D perspective cover quad
 * using bilinear UV interpolation — zero bitmap allocations per frame!
 */
private fun DrawScope.drawProceduralCoverQuadOverlay(
    px: FloatArray,
    py: FloatArray,
    baseOffset: Int,
    iTL: Int,
    iTR: Int,
    iBR: Int,
    iBL: Int,
    inst: BookInstance3D,
    foilColor: Color,
    accentColor: Color,
    isSelected: Boolean,
    screenRadius: Float
) {
    val tlX = px[baseOffset + iTL]; val tlY = py[baseOffset + iTL]
    val trX = px[baseOffset + iTR]; val trY = py[baseOffset + iTR]
    val brX = px[baseOffset + iBR]; val brY = py[baseOffset + iBR]
    val blX = px[baseOffset + iBL]; val blY = py[baseOffset + iBL]

    // Check if the cover quad is wide enough in screen space to show surface details
    val coverWidthSq = hypotSq(trX - tlX, trY - tlY)
    if (coverWidthSq < 20f) return

    fun bilerpX(u: Float, v: Float): Float {
        val topX = tlX + (trX - tlX) * u
        val botX = blX + (brX - blX) * u
        return topX + (botX - topX) * v
    }

    fun bilerpY(u: Float, v: Float): Float {
        val topY = tlY + (trY - tlY) * u
        val botY = blY + (brY - blY) * u
        return topY + (botY - topY) * v
    }

    val strokeW = if (isSelected) 1.6f else 0.9f

    // 1. Outer Gold Foil Frame (UV box [0.12, 0.10] .. [0.88, 0.90])
    val f0x = bilerpX(0.12f, 0.10f); val f0y = bilerpY(0.12f, 0.10f)
    val f1x = bilerpX(0.88f, 0.10f); val f1y = bilerpY(0.88f, 0.10f)
    val f2x = bilerpX(0.88f, 0.90f); val f2y = bilerpY(0.88f, 0.90f)
    val f3x = bilerpX(0.12f, 0.90f); val f3y = bilerpY(0.12f, 0.90f)

    drawLine(foilColor, Offset(f0x, f0y), Offset(f1x, f1y), strokeW)
    drawLine(foilColor, Offset(f1x, f1y), Offset(f2x, f2y), strokeW)
    drawLine(foilColor, Offset(f2x, f2y), Offset(f3x, f3y), strokeW)
    drawLine(foilColor, Offset(f3x, f3y), Offset(f0x, f0y), strokeW)

    // 2. Title Plate Lines (derived from title length) & Author Footer Line
    val titleLenRatio = (inst.bookTitle.length.coerceIn(8, 36) / 36f) * 0.28f
    val t1L = Offset(bilerpX(0.5f - titleLenRatio, 0.22f), bilerpY(0.5f - titleLenRatio, 0.22f))
    val t1R = Offset(bilerpX(0.5f + titleLenRatio, 0.22f), bilerpY(0.5f + titleLenRatio, 0.22f))
    drawLine(IvoryWhite.copy(alpha = foilColor.alpha), t1L, t1R, strokeW * 1.25f)

    // Author signature bar near bottom
    val authorLenRatio = (inst.bookAuthor.length.coerceIn(6, 24) / 24f) * 0.24f
    val aL = Offset(bilerpX(0.5f - authorLenRatio, 0.82f), bilerpY(0.5f - authorLenRatio, 0.82f))
    val aR = Offset(bilerpX(0.5f + authorLenRatio, 0.82f), bilerpY(0.5f + authorLenRatio, 0.82f))
    drawLine(foilColor, aL, aR, strokeW)

    // 3. Procedural Category & Harmonic Motif Geometry in Center Medallion (UV center = 0.5, 0.52)
    if (screenRadius > 14f || isSelected) {
        val pts = inst.normalizedMotifPoints
        var idx = 0
        while (idx + 3 < pts.size) {
            val u0 = 0.5f + pts[idx]
            val v0 = 0.52f + pts[idx + 1]
            val u1 = 0.5f + pts[idx + 2]
            val v1 = 0.52f + pts[idx + 3]
            drawLine(
                color = if ((idx and 4) == 0) foilColor else accentColor,
                start = Offset(bilerpX(u0, v0), bilerpY(u0, v0)),
                end = Offset(bilerpX(u1, v1), bilerpY(u1, v1)),
                strokeWidth = strokeW
            )
            idx += 4
        }
    }
}

private fun DrawScope.drawAtmosphericParticles3D(
    w: Float,
    h: Float,
    engine: Library3DEngine
) {
    val centerX = w * 0.5f
    val centerY = h * 0.5f
    val minDim = min(w, h)
    val fovScale = minDim * 0.92f * engine.camera.zoom
    val camDist = 680f

    val cosYaw = cos(engine.camera.yaw)
    val sinYaw = sin(engine.camera.yaw)
    val cosPitch = cos(engine.camera.pitch)
    val sinPitch = sin(engine.camera.pitch)

    val particles = engine.particles
    for (i in particles.indices) {
        val p = particles[i]
        val px = p.x - engine.camera.focusX
        val py = p.y - engine.camera.focusY
        val pz = p.z - engine.camera.focusZ

        val rx = px * cosYaw - pz * sinYaw
        val rz1 = px * sinYaw + pz * cosYaw
        val ry = py * cosPitch - rz1 * sinPitch
        val rz = py * sinPitch + rz1 * cosPitch + camDist
        if (rz < 90f || rz > 1350f) continue

        val s = fovScale / rz
        val sx = centerX + rx * s
        val sy = centerY + ry * s
        if (sx < 0f || sx > w || sy < 0f || sy > h) continue

        val depthAtten = (1f - (rz - 150f) / 1200f).coerceIn(0.15f, 1f)
        val radius = (p.size * s * 0.85f).coerceIn(0.8f, 4.2f)
        val color = if (p.isViolet) {
            VioletGlow.copy(alpha = (p.alphaBase * depthAtten).coerceIn(0.05f, 0.7f))
        } else {
            GoldBright.copy(alpha = (p.alphaBase * depthAtten).coerceIn(0.08f, 0.8f))
        }
        drawCircle(
            color = color,
            radius = radius,
            center = Offset(sx, sy)
        )
    }
}

private fun DrawScope.drawQuad(
    path: Path,
    px: FloatArray,
    py: FloatArray,
    baseOffset: Int,
    i0: Int,
    i1: Int,
    i2: Int,
    i3: Int,
    color: Color
) {
    path.reset()
    path.moveTo(px[baseOffset + i0], py[baseOffset + i0])
    path.lineTo(px[baseOffset + i1], py[baseOffset + i1])
    path.lineTo(px[baseOffset + i2], py[baseOffset + i2])
    path.lineTo(px[baseOffset + i3], py[baseOffset + i3])
    path.close()
    drawPath(path = path, color = color)
}

private fun cross2D(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return (x1 - x0) * (y2 - y0) - (y1 - y0) * (x2 - x0)
}

private fun hypotSq(dx: Float, dy: Float): Float = dx * dx + dy * dy

private fun shadeAndFogColor(
    argb: Int,
    light: Float,
    fog: Float,
    alpha: Float
): Color {
    val r0 = ((argb shr 16) and 0xFF) / 255f
    val g0 = ((argb shr 8) and 0xFF) / 255f
    val b0 = (argb and 0xFF) / 255f

    val litR = (r0 * light + 0.04f * light).coerceIn(0f, 1f)
    val litG = (g0 * light + 0.025f * light).coerceIn(0f, 1f)
    val litB = (b0 * light).coerceIn(0f, 1f)

    val fogR = 0.02f
    val fogG = 0.02f
    val fogB = 0.025f

    val finalR = litR * (1f - fog) + fogR * fog
    val finalG = litG * (1f - fog) + fogG * fog
    val finalB = litB * (1f - fog) + fogB * fog

    return Color(finalR, finalG, finalB, alpha.coerceIn(0f, 1f))
}
