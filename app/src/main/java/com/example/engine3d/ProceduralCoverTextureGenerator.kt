package com.example.engine3d

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.data.ArchivalBook
import com.example.data.BookCategory
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Deterministic procedural parameters derived from a book's Title, Author, and Category.
 */
data class ProceduralCoverSpec(
    val bookId: String,
    val seedHash: Int,
    val hexSignature: String,
    val category: BookCategory,
    val primaryClothArgb: Int,
    val secondaryClothArgb: Int,
    val foilArgb: Int,
    val accentGlowArgb: Int,
    val borderStyleIndex: Int,
    val motifComplexity: Int,
    val harmonicFrequency: Int,
    val radialSymmetryOrder: Int,
    val spineRibCount: Int,
    val titleInitials: String,
    val authorInitials: String
)

/**
 * Holds both the full-resolution procedural cover bitmap (for close-up 3D book inspection)
 * and a lightweight mipmapped thumbnail bitmap + precomputed normalized motif vectors
 * for zero-allocation 60 FPS instanced 3D rendering in the circular library.
 */
class GeneratedBookCoverAsset(
    val spec: ProceduralCoverSpec,
    val fullCoverBitmap: Bitmap,
    val fullCoverImageBitmap: ImageBitmap,
    val spineBitmap: Bitmap,
    val spineImageBitmap: ImageBitmap,
    val instancedMipBitmap: Bitmap,
    val instancedMipImageBitmap: ImageBitmap,
    // Normalized (0..1) procedural glyph lines for ultra-fast instanced 3D face overlays
    val normalizedMotifPoints: FloatArray
)

/**
 * Procedurally generates unique book cover textures and instanced 3D surface descriptors
 * derived from each book's title, author, and category.
 * Uses an LRU/Concurrent cache keyed by book metadata so thousands of instanced 3D books
 * share pre-baked GPU/Canvas textures without redundant allocation.
 */
object ProceduralCoverTextureGenerator {

    private val assetCache = ConcurrentHashMap<String, GeneratedBookCoverAsset>()

    fun getOrCreateCoverAsset(book: ArchivalBook, variantSeedOffset: Int = 0): GeneratedBookCoverAsset {
        val cacheKey = "${book.id}:${book.title}:${book.author}:${book.category.name}:$variantSeedOffset"
        assetCache[cacheKey]?.let { return it }

        val spec = deriveProceduralSpec(book, variantSeedOffset)
        val fullCover = renderProceduralFrontCoverBitmap(book, spec, width = 320, height = 460)
        val spine = renderProceduralSpineBitmap(book, spec, width = 76, height = 460)
        val mip = Bitmap.createScaledBitmap(fullCover, 64, 92, true)
        val motifPoints = buildNormalizedMotifVectors(spec)

        val asset = GeneratedBookCoverAsset(
            spec = spec,
            fullCoverBitmap = fullCover,
            fullCoverImageBitmap = fullCover.asImageBitmap(),
            spineBitmap = spine,
            spineImageBitmap = spine.asImageBitmap(),
            instancedMipBitmap = mip,
            instancedMipImageBitmap = mip.asImageBitmap(),
            normalizedMotifPoints = motifPoints
        )
        assetCache[cacheKey] = asset
        return asset
    }

    fun clearCache() {
        assetCache.clear()
    }

    /**
     * Derives deterministic visual parameters from Title, Author, and Category.
     */
    fun deriveProceduralSpec(book: ArchivalBook, variantOffset: Int = 0): ProceduralCoverSpec {
        val rawKey = "${book.title.trim().lowercase()}|${book.author.trim().lowercase()}|${book.category.name}|$variantOffset"
        var h = -0x7ee3623b
        for (ch in rawKey) {
            h = h xor ch.code
            h *= 0x01000193
        }
        val positiveHash = h and 0x7FFFFFFF

        // Category-influenced rich base cloth palette + metallic foil
        val baseColor = book.coverStyle.primaryColorHex.toInt()
        val spineColor = book.coverStyle.spineColorHex.toInt()
        val foilBase = book.coverStyle.accentFoilHex.toInt()

        val hueShift = ((positiveHash % 36) - 18)
        val primaryCloth = shiftColorLuminanceAndTint(baseColor, hueShift, 1.05f)
        val secondaryCloth = shiftColorLuminanceAndTint(spineColor, -hueShift, 0.78f)
        val foilArgb = shiftColorLuminanceAndTint(foilBase, (positiveHash shr 4) % 16, 1.12f)
        val accentGlow = book.category.badgeColorHex.toInt()

        val symmetry = when (book.category) {
            BookCategory.SCIENCE -> 6 + (positiveHash % 7) * 2
            BookCategory.PHILOSOPHY -> 3 + (positiveHash % 6)
            BookCategory.POLITICS -> 4 + (positiveHash % 4) * 2
            BookCategory.LITERATURE -> 8 + (positiveHash % 5) * 2
            BookCategory.HISTORY -> 12
            BookCategory.CULTURE -> 5 + (positiveHash % 7)
        }

        val titleInitials = book.title
            .split(" ")
            .filter { it.isNotBlank() && it.first().isLetterOrDigit() }
            .take(3)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "FL" }

        val authorInitials = book.author
            .split(" ")
            .filter { it.isNotBlank() && it.first().isLetter() }
            .take(2)
            .joinToString(".") { it.first().uppercase() }
            .ifEmpty { "A" }

        return ProceduralCoverSpec(
            bookId = book.id,
            seedHash = positiveHash,
            hexSignature = "0x" + positiveHash.toString(16).uppercase().padStart(8, '0'),
            category = book.category,
            primaryClothArgb = primaryCloth,
            secondaryClothArgb = secondaryCloth,
            foilArgb = foilArgb,
            accentGlowArgb = accentGlow,
            borderStyleIndex = positiveHash % 4,
            motifComplexity = 3 + (positiveHash % 6),
            harmonicFrequency = 2 + ((positiveHash shr 3) % 7),
            radialSymmetryOrder = symmetry,
            spineRibCount = 4 + (positiveHash % 3),
            titleInitials = titleInitials,
            authorInitials = authorInitials
        )
    }

    /**
     * Generates a rich hardcover front texture incorporating Title, Author, Category emblem,
     * cloth weave, metallic foil borders, and procedural geometric art.
     */
    private fun renderProceduralFrontCoverBitmap(
        book: ArchivalBook,
        spec: ProceduralCoverSpec,
        width: Int,
        height: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val w = width.toFloat()
        val h = height.toFloat()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Base Hardcover Linen Cloth Gradient
        paint.shader = LinearGradient(
            0f, 0f, w, h,
            intArrayOf(spec.primaryClothArgb, spec.secondaryClothArgb, 0xFF07070B.toInt()),
            floatArrayOf(0f, 0.68f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null

        // 2. Subtle Procedural Cloth Weave Hatching
        paint.color = (spec.foilArgb and 0x00FFFFFF) or 0x10000000
        paint.strokeWidth = 1f
        val step = 7f
        var yLine = 0f
        while (yLine < h) {
            canvas.drawLine(0f, yLine, w, yLine, paint)
            yLine += step
        }
        var xLine = 0f
        while (xLine < w) {
            canvas.drawLine(xLine, 0f, xLine, h, paint)
            xLine += step * 1.5f
        }

        // 3. Spine Hinge Shadow Groove on Left Edge
        val hingePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, w * 0.08f, 0f,
                intArrayOf(0xAA000000.toInt(), 0x33FFFFFF, 0x00000000),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w * 0.08f, h, hingePaint)

        // 4. Radial Category Aura in the Central Medallion Area
        val centerX = w * 0.52f
        val centerY = h * 0.50f
        val auraRadius = w * 0.40f
        val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                centerX, centerY, auraRadius,
                intArrayOf(
                    (spec.accentGlowArgb and 0x00FFFFFF) or 0x44000000,
                    (spec.foilArgb and 0x00FFFFFF) or 0x18000000,
                    0x00000000
                ),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(centerX, centerY, auraRadius, auraPaint)

        // 5. Metallic Foil Architectural Frame & Corner Flourishes
        val foilStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = spec.foilArgb
            strokeWidth = 2.4f
        }
        val foilThin = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = (spec.foilArgb and 0x00FFFFFF) or 0x99000000.toInt()
            strokeWidth = 1.2f
        }

        val margin = w * 0.075f
        val outerRect = RectF(margin, margin, w - margin * 0.8f, h - margin)
        canvas.drawRect(outerRect, foilStroke)

        val innerMargin = margin + 8f
        val innerRect = RectF(innerMargin, innerMargin, w - margin * 0.8f - 8f, h - innerMargin)
        canvas.drawRect(innerRect, foilThin)

        // Ornamental corner geometry based on borderStyleIndex
        val cornerSize = 18f
        drawCornerOrnaments(canvas, innerRect, cornerSize, spec.borderStyleIndex, foilStroke)

        // 6. Category-Specific Procedural Central Artwork (derived from Title + Author seed)
        drawCategoryProceduralArtwork(
            canvas = canvas,
            cx = centerX,
            cy = centerY,
            radius = w * 0.27f,
            spec = spec,
            foilStroke = foilStroke,
            foilThin = foilThin
        )

        // 7. Top Header Band: Category Label & Catalog Year
        val monoPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = (spec.foilArgb and 0x00FFFFFF) or 0xDD000000.toInt()
            textSize = w * 0.036f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            letterSpacing = 0.14f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "${book.category.displayName.uppercase()} · ${book.formattedYear}",
            centerX,
            innerRect.top + w * 0.068f,
            monoPaint
        )

        // Horizontal divider under category header
        canvas.drawLine(
            innerRect.left + 18f,
            innerRect.top + w * 0.095f,
            innerRect.right - 18f,
            innerRect.top + w * 0.095f,
            foilThin
        )

        // 8. Procedural Book Title Plate (Multi-line Wrapped Editorial Serif Typography)
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF7F3E8.toInt()
            textSize = if (book.title.length > 38) w * 0.056f else w * 0.066f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            setShadowLayer(4f, 0f, 2f, 0xCC000000.toInt())
        }
        val maxTextWidth = (innerRect.width() - 28f).toInt().coerceAtLeast(80)
        val titleLayout = StaticLayout.Builder
            .obtain(book.title.uppercase(), 0, book.title.length, titlePaint, maxTextWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setMaxLines(3)
            .setLineSpacing(0f, 1.05f)
            .build()

        canvas.save()
        val titleStartY = innerRect.top + w * 0.125f
        canvas.translate(centerX - maxTextWidth / 2f, titleStartY)
        titleLayout.draw(canvas)
        canvas.restore()

        // 9. Bottom Author Plate & Country Colophon
        val footerDividerY = innerRect.bottom - h * 0.145f
        canvas.drawLine(
            innerRect.left + 18f,
            footerDividerY,
            innerRect.right - 18f,
            footerDividerY,
            foilThin
        )

        val authorPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.foilArgb
            textSize = w * 0.046f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.06f
            textAlign = Paint.Align.CENTER
        }
        val cleanAuthor = if (book.author.length > 28) book.author.take(26) + "…" else book.author
        canvas.drawText(
            cleanAuthor.uppercase(),
            centerX,
            footerDividerY + h * 0.055f,
            authorPaint
        )

        val colophonPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFB8B2A6.toInt()
            textSize = w * 0.032f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            letterSpacing = 0.1f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "${book.country.uppercase()} · ${spec.hexSignature}",
            centerX,
            innerRect.bottom - h * 0.035f,
            colophonPaint
        )

        return bitmap
    }

    /**
     * Generates a matching procedural 3D book spine texture with raised ribs, foil bands,
     * vertical title lettering, author initials, and category emblem.
     */
    private fun renderProceduralSpineBitmap(
        book: ArchivalBook,
        spec: ProceduralCoverSpec,
        width: Int,
        height: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val w = width.toFloat()
        val h = height.toFloat()

        // 1. Cylindrical Spine Shading Gradient
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, w, 0f,
                intArrayOf(
                    spec.secondaryClothArgb,
                    spec.primaryClothArgb,
                    spec.secondaryClothArgb
                ),
                floatArrayOf(0f, 0.48f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        val foilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.foilArgb
            strokeWidth = 2.2f
            style = Paint.Style.STROKE
        }

        // 2. Raised Spine Ribs & Gold Foil Bands
        val ribCount = spec.spineRibCount
        for (i in 1..ribCount) {
            val frac = i.toFloat() / (ribCount + 1)
            // Leave center window open for vertical spine title
            if (frac in 0.30f..0.72f) continue
            val ry = h * frac
            canvas.drawLine(3f, ry - 2f, w - 3f, ry - 2f, foilPaint)
            canvas.drawLine(3f, ry + 2f, w - 3f, ry + 2f, foilPaint)
        }

        // Top & Bottom Foil Caps
        canvas.drawRect(4f, 10f, w - 4f, h - 10f, foilPaint)

        // 3. Vertical Rotated Title & Author along Spine Center
        canvas.save()
        canvas.translate(w * 0.54f, h * 0.50f)
        canvas.rotate(-90f)

        val spineTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF4F1EA.toInt()
            textSize = w * 0.25f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.08f
        }
        val shortTitle = if (book.title.length > 26) book.title.take(24) + "…" else book.title
        canvas.drawText(shortTitle.uppercase(), 0f, -w * 0.06f, spineTitlePaint)

        val spineAuthorPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.foilArgb
            textSize = w * 0.18f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        val shortAuthor = book.author.split(" ").lastOrNull() ?: book.author
        canvas.drawText(shortAuthor.uppercase(), 0f, w * 0.18f, spineAuthorPaint)
        canvas.restore()

        // 4. Top Monogram & Bottom Year Badge
        val badgePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.foilArgb
            textSize = w * 0.20f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(spec.titleInitials.take(2), w * 0.5f, h * 0.08f, badgePaint)
        canvas.drawText(book.publicationYear.toString().takeLast(4), w * 0.5f, h * 0.94f, badgePaint)

        return bitmap
    }

    private fun drawCornerOrnaments(
        canvas: Canvas,
        rect: RectF,
        size: Float,
        styleIdx: Int,
        paint: Paint
    ) {
        val corners = arrayOf(
            floatArrayOf(rect.left, rect.top, 1f, 1f),
            floatArrayOf(rect.right, rect.top, -1f, 1f),
            floatArrayOf(rect.left, rect.bottom, 1f, -1f),
            floatArrayOf(rect.right, rect.bottom, -1f, -1f)
        )
        for (c in corners) {
            val cx = c[0]
            val cy = c[1]
            val dx = c[2] * size
            val dy = c[3] * size
            when (styleIdx) {
                0 -> {
                    canvas.drawRect(
                        minOf(cx, cx + dx * 0.6f),
                        minOf(cy, cy + dy * 0.6f),
                        maxOf(cx, cx + dx * 0.6f),
                        maxOf(cy, cy + dy * 0.6f),
                        paint
                    )
                }
                1 -> {
                    canvas.drawLine(cx, cy + dy, cx + dx, cy, paint)
                    canvas.drawLine(cx, cy + dy * 0.6f, cx + dx * 0.6f, cy, paint)
                }
                else -> {
                    canvas.drawCircle(cx + dx * 0.45f, cy + dy * 0.45f, size * 0.25f, paint)
                }
            }
        }
    }

    private fun drawCategoryProceduralArtwork(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        spec: ProceduralCoverSpec,
        foilStroke: Paint,
        foilThin: Paint
    ) {
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = (spec.accentGlowArgb and 0x00FFFFFF) or 0xCC000000.toInt()
            strokeWidth = 1.6f
        }
        val fillFoil = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = spec.foilArgb
        }

        // Outer medallion ring
        canvas.drawCircle(cx, cy, radius, foilStroke)
        canvas.drawCircle(cx, cy, radius * 0.88f, foilThin)

        val n = spec.radialSymmetryOrder
        val path = Path()

        when (spec.category) {
            BookCategory.SCIENCE -> {
                // Epicyclic astrolabe & Lissajous orbital harmonics
                for (ring in 1..spec.motifComplexity) {
                    val r = radius * (0.22f + 0.62f * (ring.toFloat() / spec.motifComplexity))
                    canvas.drawCircle(cx, cy, r, if (ring % 2 == 0) foilThin else accentPaint)
                    val nodeAngle = (spec.seedHash % 360) * (PI.toFloat() / 180f) + ring * 1.1f
                    val nx = cx + cos(nodeAngle) * r
                    val ny = cy + sin(nodeAngle) * r
                    canvas.drawCircle(nx, ny, 4.5f, fillFoil)
                }
                for (i in 0 until n) {
                    val a = (2f * PI.toFloat() * i) / n
                    canvas.drawLine(
                        cx + cos(a) * radius * 0.18f,
                        cy + sin(a) * radius * 0.18f,
                        cx + cos(a) * radius * 0.88f,
                        cy + sin(a) * radius * 0.88f,
                        foilThin
                    )
                }
            }

            BookCategory.PHILOSOPHY -> {
                // Sacred geometry interlocked polygons & dialectical axes
                for (layer in 0..2) {
                    val rot = layer * (PI.toFloat() / n)
                    val r = radius * (0.82f - layer * 0.18f)
                    path.reset()
                    for (i in 0..n) {
                        val a = rot + (2f * PI.toFloat() * (i % n)) / n
                        val px = cx + cos(a) * r
                        val py = cy + sin(a) * r
                        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                    }
                    path.close()
                    canvas.drawPath(path, if (layer == 0) foilStroke else accentPaint)
                }
                canvas.drawCircle(cx, cy, radius * 0.16f, foilStroke)
            }

            BookCategory.POLITICS -> {
                // Sovereign sunburst + redaction bars + structural chevron
                for (i in 0 until n * 2) {
                    val a = (PI.toFloat() * i) / n
                    val innerR = if (i % 2 == 0) radius * 0.25f else radius * 0.45f
                    canvas.drawLine(
                        cx + cos(a) * innerR,
                        cy + sin(a) * innerR,
                        cx + cos(a) * radius * 0.84f,
                        cy + sin(a) * radius * 0.84f,
                        if (i % 2 == 0) foilStroke else accentPaint
                    )
                }
                val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    color = (spec.accentGlowArgb and 0x00FFFFFF) or 0xAA000000.toInt()
                }
                canvas.drawRect(cx - radius * 0.55f, cy - 10f, cx + radius * 0.55f, cy - 2f, barPaint)
                canvas.drawRect(cx - radius * 0.38f, cy + 4f, cx + radius * 0.38f, cy + 12f, fillFoil)
            }

            BookCategory.LITERATURE -> {
                // Guilloché spirograph rosette curve derived from harmonicFrequency
                path.reset()
                val steps = 120
                val k = spec.harmonicFrequency.toFloat()
                for (s in 0..steps) {
                    val t = (s.toFloat() / steps) * 2f * PI.toFloat()
                    val r = radius * (0.48f + 0.36f * cos(k * t))
                    val px = cx + r * cos(t)
                    val py = cy + r * sin(t)
                    if (s == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                canvas.drawPath(path, foilStroke)
                canvas.drawCircle(cx, cy, radius * 0.22f, accentPaint)
            }

            BookCategory.HISTORY -> {
                // Cartographic compass rose & meridian graticule
                for (lat in 1..3) {
                    val r = radius * (lat * 0.26f)
                    canvas.drawOval(cx - r, cy - r * 0.6f, cx + r, cy + r * 0.6f, foilThin)
                    canvas.drawOval(cx - r * 0.6f, cy - r, cx + r * 0.6f, cy + r, foilThin)
                }
                // 8-point compass star
                path.reset()
                for (i in 0 until 16) {
                    val a = (PI.toFloat() * i) / 8f
                    val r = if (i % 2 == 0) radius * 0.84f else radius * 0.24f
                    val px = cx + cos(a) * r
                    val py = cy + sin(a) * r
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                canvas.drawPath(path, foilStroke)
            }

            BookCategory.CULTURE -> {
                // Interlaced harmonic rings around the perimeter
                val subRadius = radius * 0.36f
                for (i in 0 until n) {
                    val a = (2f * PI.toFloat() * i) / n
                    val ox = cx + cos(a) * (radius * 0.42f)
                    val oy = cy + sin(a) * (radius * 0.42f)
                    canvas.drawCircle(ox, oy, subRadius, if (i % 2 == 0) foilStroke else accentPaint)
                }
            }
        }

        // Central Author/Title Monogram Seal
        canvas.drawCircle(cx, cy, radius * 0.14f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = spec.secondaryClothArgb
        })
        canvas.drawCircle(cx, cy, radius * 0.14f, foilStroke)

        val monoCenterPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.foilArgb
            textSize = radius * 0.13f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(spec.titleInitials.take(2), cx, cy + radius * 0.045f, monoCenterPaint)
    }

    /**
     * Precomputes normalized (0..1) 2D motif line segments for fast instanced 3D rendering
     * on the visible cover face of 3D books in the circular library.
     */
    private fun buildNormalizedMotifVectors(spec: ProceduralCoverSpec): FloatArray {
        val segments = spec.radialSymmetryOrder.coerceIn(4, 12)
        // Each segment stores (x0, y0, x1, y1) in [-0.32f .. +0.32f] relative to face center
        val out = FloatArray(segments * 4)
        val k = spec.harmonicFrequency
        for (i in 0 until segments) {
            val a0 = (2f * PI.toFloat() * i) / segments
            val a1 = (2f * PI.toFloat() * ((i + k) % segments)) / segments
            val r0 = 0.26f
            val r1 = 0.26f
            out[i * 4 + 0] = cos(a0) * r0
            out[i * 4 + 1] = sin(a0) * r0
            out[i * 4 + 2] = cos(a1) * r1
            out[i * 4 + 3] = sin(a1) * r1
        }
        return out
    }

    private fun shiftColorLuminanceAndTint(argb: Int, delta: Int, scale: Float): Int {
        val r = ((((argb shr 16) and 0xFF) * scale).toInt() + delta).coerceIn(12, 245)
        val g = ((((argb shr 8) and 0xFF) * scale).toInt() + delta / 2).coerceIn(12, 245)
        val b = (((argb and 0xFF) * scale).toInt() - delta / 2).coerceIn(12, 245)
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }
}
