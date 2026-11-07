package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ArchivalBook
import com.example.engine3d.GeneratedBookCoverAsset
import com.example.engine3d.ProceduralCoverTextureGenerator
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BorderHairlineGold
import com.example.ui.theme.BorderHairlineSubtle
import com.example.ui.theme.GlassPanelElevated
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.IvoryWhite
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ParchmentMuted
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Interactive 3D Textured Book Renderer that maps the procedurally generated front cover
 * and spine textures onto a 3D rotatable hardcover book mesh using 3D perspective matrix
 * homography on the native canvas.
 */
@Composable
fun InteractiveProceduralBook3DView(
    book: ArchivalBook,
    variantOffset: Int,
    modifier: Modifier = Modifier,
    initialYawDeg: Float = -24f
) {
    val coverAsset = remember(book.id, book.title, book.author, book.category, variantOffset) {
        ProceduralCoverTextureGenerator.getOrCreateCoverAsset(book, variantOffset)
    }

    var yawDeg by remember { mutableFloatStateOf(initialYawDeg) }
    var pitchDeg by remember { mutableFloatStateOf(8f) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    yawDeg = (yawDeg + dragAmount.x * 0.45f).coerceIn(-75f, 45f)
                    pitchDeg = (pitchDeg + dragAmount.y * 0.25f).coerceIn(-25f, 30f)
                }
            }
            .testTag("interactive_3d_book_cover")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val radYaw = Math.toRadians(yawDeg.toDouble()).toFloat()
            val radPitch = Math.toRadians(pitchDeg.toDouble()).toFloat()

            val cosY = cos(radYaw)
            val sinY = sin(radYaw)
            val sinP = sin(radPitch)

            val bookW = w * 0.66f
            val bookH = h * 0.84f
            val spineThick = w * 0.16f

            val cx = w * 0.52f
            val cy = h * 0.52f

            // Project Spine Width & Front Cover Width under 3D rotation
            val frontProjW = (bookW * cosY.coerceAtLeast(0.15f))
            val spineProjW = (spineThick * abs(sinY).coerceAtLeast(0.18f))
            val verticalSlant = sinP * 14f

            val spineLeftX = cx - frontProjW * 0.45f - spineProjW
            val hingeX = cx - frontProjW * 0.45f
            val rightEdgeX = hingeX + frontProjW

            val topY = cy - bookH * 0.5f
            val botY = cy + bookH * 0.5f

            // 1. 3D Top Gilded Page Block
            val pageDepthX = 8f * cosY
            val pageDepthY = -7f - abs(verticalSlant) * 0.35f
            val topPagesPath = Path().apply {
                moveTo(spineLeftX, topY + verticalSlant * 0.5f)
                lineTo(hingeX, topY)
                lineTo(rightEdgeX, topY + verticalSlant)
                lineTo(rightEdgeX + pageDepthX, topY + verticalSlant + pageDepthY)
                lineTo(hingeX + pageDepthX * 0.5f, topY + pageDepthY)
                close()
            }
            drawPath(topPagesPath, Color(0xFFE9E0CC))
            drawPath(topPagesPath, Color(coverAsset.spec.foilArgb).copy(alpha = 0.5f), style = Stroke(1f))

            // 2. Fore-edge Page Block on Right Side
            val foreEdgePath = Path().apply {
                moveTo(rightEdgeX, topY + verticalSlant)
                lineTo(rightEdgeX + pageDepthX, topY + verticalSlant + pageDepthY)
                lineTo(rightEdgeX + pageDepthX, botY + verticalSlant + pageDepthY)
                lineTo(rightEdgeX, botY + verticalSlant)
                close()
            }
            drawPath(foreEdgePath, Color(0xFFD6CCB6))

            // 3. Draw Procedurally Generated Spine Texture onto Spine Quad
            drawIntoCanvas { canvas ->
                val native = canvas.nativeCanvas
                native.save()
                val spineSrcW = coverAsset.spineBitmap.width.toFloat()
                val spineSrcH = coverAsset.spineBitmap.height.toFloat()
                val spineMatrix = android.graphics.Matrix()
                val srcPts = floatArrayOf(0f, 0f, spineSrcW, 0f, spineSrcW, spineSrcH, 0f, spineSrcH)
                val dstPts = floatArrayOf(
                    spineLeftX, topY + verticalSlant * 0.5f,
                    hingeX, topY,
                    hingeX, botY,
                    spineLeftX, botY + verticalSlant * 0.5f
                )
                spineMatrix.setPolyToPoly(srcPts, 0, dstPts, 0, 4)
                native.drawBitmap(coverAsset.spineBitmap, spineMatrix, android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG))
                native.restore()

                // 4. Draw Procedurally Generated Front Cover Texture onto 3D Perspective Quad
                native.save()
                val covSrcW = coverAsset.fullCoverBitmap.width.toFloat()
                val covSrcH = coverAsset.fullCoverBitmap.height.toFloat()
                val covMatrix = android.graphics.Matrix()
                val cSrcPts = floatArrayOf(0f, 0f, covSrcW, 0f, covSrcW, covSrcH, 0f, covSrcH)
                val cDstPts = floatArrayOf(
                    hingeX, topY,
                    rightEdgeX, topY + verticalSlant,
                    rightEdgeX, botY + verticalSlant,
                    hingeX, botY
                )
                covMatrix.setPolyToPoly(cSrcPts, 0, cDstPts, 0, 4)
                native.drawBitmap(coverAsset.fullCoverBitmap, covMatrix, android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG))
                native.restore()
            }

            // 5. Specular Hinge Highlight
            drawLine(
                color = Color.White.copy(alpha = 0.24f),
                start = Offset(hingeX, topY),
                end = Offset(hingeX, botY),
                strokeWidth = 1.6f
            )
        }
    }
}

/**
 * Full-featured 3D Procedural Cover Texture Studio Modal inspecting the deterministic
 * texture synthesis derived from Title, Author, and Category.
 */
@Composable
fun ProceduralCover3DStudioModal(
    book: ArchivalBook,
    variantOffset: Int,
    onMutateVariant: () -> Unit,
    onDismiss: () -> Unit
) {
    val coverAsset: GeneratedBookCoverAsset = remember(book.id, book.title, book.author, book.category, variantOffset) {
        ProceduralCoverTextureGenerator.getOrCreateCoverAsset(book, variantOffset)
    }
    val spec = coverAsset.spec

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = GlassPanelElevated,
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, BorderHairlineGold),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 620.dp)
                .padding(vertical = 18.dp)
                .testTag("procedural_cover_studio_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            tint = GoldBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "PROCEDURAL 3D COVER TEXTURE ENGINE",
                                style = MaterialTheme.typography.labelLarge,
                                color = GoldAccent
                            )
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = IvoryWhite
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_cover_studio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Cover Studio",
                            tint = ParchmentMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive 3D Book Preview + Unwrapped UV Spine & Cover Texture Maps
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .border(1.dp, BorderHairlineGold, RoundedCornerShape(4.dp))
                        .background(ObsidianBlack.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    // Left: Interactive Drag-to-Rotate 3D Textured Book
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ViewInAr,
                                contentDescription = null,
                                tint = GoldBright,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "DRAG TO ROTATE 3D MESH",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldBright
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        InteractiveProceduralBook3DView(
                            book = book,
                            variantOffset = variantOffset,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }

                    // Right: Unwrapped Procedural UV Maps (Spine UV + Front Cover UV)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Text(
                            text = "UNWRAPPED UV TEXTURE ATLAS",
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Image(
                                bitmap = coverAsset.spineImageBitmap,
                                contentDescription = "Procedural Spine UV Texture",
                                contentScale = ContentScale.FillHeight,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(2.dp))
                                    .border(1.dp, BorderHairlineSubtle, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Image(
                                bitmap = coverAsset.fullCoverImageBitmap,
                                contentDescription = "Procedural Front Cover UV Texture",
                                contentScale = ContentScale.FillHeight,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(2.dp))
                                    .border(1.dp, BorderHairlineGold, RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Deterministic Seed & Parameter Telemetry Grid
                Text(
                    text = "DETERMINISTIC SYNTHESIS PARAMETERS (FROM TITLE + AUTHOR + CATEGORY)",
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldAccent
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderHairlineSubtle, RoundedCornerShape(3.dp))
                        .background(ObsidianBlack.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                        .padding(10.dp)
                ) {
                    TelemetrySpecRow("PROCEDURAL SEED HASH", "${spec.hexSignature} (Edition Variant #${variantOffset + 1})")
                    TelemetrySpecRow("TITLE MONOGRAM & PLATE", "${spec.titleInitials} · \"${book.title}\"")
                    TelemetrySpecRow("AUTHOR SIGNATURE", "${spec.authorInitials} · ${book.author}")
                    TelemetrySpecRow("CATEGORY GENERATOR", "${spec.category.displayName} (${spec.radialSymmetryOrder}-fold radial symmetry)")
                    TelemetrySpecRow("HARMONIC FREQUENCY", "k = ${spec.harmonicFrequency} · ${spec.motifComplexity} concentric rings · ${spec.spineRibCount} spine ribs")
                    TelemetrySpecRow("INSTANCED LOD PIPELINE", "Shared GPU Texture Cache + Bilinear 3D Quad UV Projection")
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BorderHairlineSubtle)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(3.dp),
                        border = BorderStroke(1.dp, BorderHairlineSubtle),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "CLOSE STUDIO",
                            style = MaterialTheme.typography.labelMedium,
                            color = ParchmentMuted
                        )
                    }

                    Button(
                        onClick = onMutateVariant,
                        shape = RoundedCornerShape(3.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = ObsidianBlack
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("mutate_cover_variant_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SYNTHESIZE NEW EDITION VARIANT",
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
private fun TelemetrySpecRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = ParchmentMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = AmberPrimary
        )
    }
}
