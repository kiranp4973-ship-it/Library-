package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BorderHairlineGold
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.IvoryWhite
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ParchmentMuted
import kotlinx.coroutines.delay

/**
 * Implements the 5-stage Cinematic Introduction:
 * 1. Pure black screen (#050505).
 * 2. Slowly reveals the title FORBIDDEN LIBRARY.
 * 3. Shows subtitle: "Explore the boundaries of knowledge."
 * 4. Reveals the circular 3D library with a cinematic camera movement.
 * 5. Displays subtle instruction: "Drag to explore · Scroll to zoom · Select a book."
 */
@Composable
fun CinematicIntroOverlay(
    visible: Boolean,
    onIntroCameraProgress: (Float) -> Unit,
    onFinishOrSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var stage by remember(visible) { mutableIntStateOf(0) }
    val backdropAlpha = remember(visible) { Animatable(1f) }
    val cameraSweep = remember(visible) { Animatable(0f) }

    LaunchedEffect(visible) {
        if (!visible) {
            onIntroCameraProgress(1f)
            return@LaunchedEffect
        }
        stage = 1 // Pure black screen
        backdropAlpha.snapTo(1f)
        cameraSweep.snapTo(0f)
        onIntroCameraProgress(0f)

        delay(450)
        stage = 2 // Slowly reveal FORBIDDEN LIBRARY

        delay(1100)
        stage = 3 // Show subtitle: "Explore the boundaries of knowledge."

        delay(1100)
        stage = 4 // Reveal circular 3D library with cinematic camera movement
        // Animate backdrop from pure black to translucent vignette while camera sweeps in
        backdropAlpha.animateTo(
            targetValue = 0.32f,
            animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(stage, visible) {
        if (visible && stage >= 4) {
            cameraSweep.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2400, easing = FastOutSlowInEasing)
            ) {
                onIntroCameraProgress(value)
            }
            stage = 5 // Display subtle instruction
            delay(1800)
            onIntroCameraProgress(1f)
            onFinishOrSkip()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(700)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBlack.copy(alpha = backdropAlpha.value))
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(24.dp)
                .testTag("cinematic_intro_overlay")
        ) {
            // Top-right Skip Intro button (always accessible)
            OutlinedButton(
                onClick = {
                    onIntroCameraProgress(1f)
                    onFinishOrSkip()
                },
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, BorderHairlineGold),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ObsidianBlack.copy(alpha = 0.65f),
                    contentColor = GoldBright
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .testTag("skip_intro_button")
            ) {
                Text(
                    text = stringResource(R.string.skip_intro),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.skip_intro),
                    modifier = Modifier.size(14.dp)
                )
            }

            // Center Monumental Title & Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 20.dp)
            ) {
                AnimatedVisibility(
                    visible = stage >= 2,
                    enter = fadeIn(tween(1100))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DIGITAL ARCHIVE INSTALLATION · MMXXVI",
                                style = MaterialTheme.typography.labelLarge,
                                color = GoldAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.intro_title),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 36.sp,
                                letterSpacing = 6.sp
                            ),
                            color = IvoryWhite,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        HorizontalDivider(
                            modifier = Modifier.width(140.dp),
                            thickness = 1.dp,
                            color = GoldAccent.copy(alpha = 0.65f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                AnimatedVisibility(
                    visible = stage >= 3,
                    enter = fadeIn(tween(1000))
                ) {
                    Text(
                        text = stringResource(R.string.intro_subtitle),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 18.sp,
                            letterSpacing = 2.sp
                        ),
                        color = ParchmentMuted,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                AnimatedVisibility(
                    visible = stage >= 4,
                    enter = fadeIn(tween(900))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .border(1.dp, BorderHairlineGold, RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            ObsidianBlack.copy(alpha = 0.85f),
                                            Color(0xFF141008).copy(alpha = 0.85f),
                                            ObsidianBlack.copy(alpha = 0.85f)
                                        )
                                    ),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Drag to explore · Scroll/Pinch to zoom · Select a book",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldBright,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                onIntroCameraProgress(1f)
                                onFinishOrSkip()
                            },
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = ObsidianBlack
                            ),
                            modifier = Modifier.testTag("enter_archive_button")
                        ) {
                            Text(
                                text = stringResource(R.string.enter_archive),
                                style = MaterialTheme.typography.labelLarge.copy(color = ObsidianBlack)
                            )
                        }
                    }
                }
            }
        }
    }
}
