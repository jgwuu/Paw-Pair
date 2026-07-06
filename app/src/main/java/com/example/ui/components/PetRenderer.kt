package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun PetRenderer(
    type: String,
    isSleeping: Boolean,
    hunger: Float,
    happiness: Float,
    equippedHat: String?,
    groomingAction: String = "NONE",
    level: Int = 1,
    modifier: Modifier = Modifier,
    interactionTrigger: Long = 0L,
    isSilhouette: Boolean = false,
    equippedAccessory: String? = null
) {
    val stage = when {
        level >= 200 -> 4
        level >= 100 -> 3
        level >= 50 -> 2
        else -> 1
    }
    val stageScaleBoost = when (stage) {
        4 -> 1.12f
        3 -> 1.08f
        2 -> 1.04f
        else -> 1.0f
    }
    // Breathing & bobbing animation
    val infiniteTransition = rememberInfiniteTransition(label = "PetBreathing")
    
    // Bubble float animation
    val bubbleYOffset by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubbleY"
    )

    // Shower flow animation
    val showerYOffset by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "showerY"
    )

    // Tooth sparkles/scrub offset
    val brushXOffset by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "brushX"
    )

    val bobbingOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobbing"
    )

    val animScaleY by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    // Dynamic tail wag animation spec
    val tailWag by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tailWag"
    )

    // Ecstatic sparkles scaling / alpha pulse
    val sparkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkleAlpha"
    )

    // Base colors configuration
    val baseColor = when (type) {
        "SHIBA" -> Color(0xFFE5A65D) // Gold Warm Orange
        "SLIME" -> Color(0xFF4AC4F3) // Cute Sky Cyan
        "KITTY" -> Color(0xFFF1B7C6) // Soft Rose Pink
        "DRACO" -> Color(0xFF4CB050) // Emerald Green
        "AXOLOTL" -> Color(0xFFEC407A) // Bright Rose Pink
        else -> Color(0xFF9E9E9E)
    }

    val bellyColor = when (type) {
        "SHIBA" -> Color(0xFFFFF3E0)
        "SLIME" -> Color(0xFFA1E3FF)
        "KITTY" -> Color(0xFFFFF1F3)
        "DRACO" -> Color(0xFFC7F7C4)
        "AXOLOTL" -> Color(0xFFFCE4EC)
        else -> Color(0xFFE0E0E0)
    }

    val interactionAnim = remember { Animatable(0f) }
    LaunchedEffect(interactionTrigger) {
        if (interactionTrigger > 0L) {
            interactionAnim.snapTo(1f)
            interactionAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1300, easing = EaseInOutSine)
            )
        }
    }
    val ia = interactionAnim.value
    val interactJump = if (stage == 1 && ia > 0f) -55f * kotlin.math.sin(ia * Math.PI * 3.0).toFloat() else 0f
    val interactLevitate = if (stage == 3 && ia > 0f) -45f * kotlin.math.sin(ia * Math.PI).toFloat() else 0f
    val interactSpin = if (stage == 2 && ia > 0f) ia * 360f else 0f
    val interactScale = if (stage == 4 && ia > 0f) 1f + 0.22f * kotlin.math.sin(ia * Math.PI * 4.0).toFloat() else 1f

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                translationY = (if (isSleeping) bobbingOffset * 0.3f else bobbingOffset) + interactJump + interactLevitate
                scaleX = stageScaleBoost * interactScale
                scaleY = animScaleY * stageScaleBoost * interactScale
                rotationZ = (if (isSleeping) 0f else rotation) + interactSpin
            }
    ) {
        val centerX = size.width / 2f
        val minDim = minOf(size.width, size.height)
        val r = if (minDim > 0) minDim * 0.31f else 90.dp.toPx() // adaptive safe radius so auras never clip or overlap UI
        val centerY = size.height / 2f + r * 0.12f

        if (isSilhouette) {
            drawContext.canvas.saveLayer(
                androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height),
                androidx.compose.ui.graphics.Paint().apply {
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                        Color(0xFF1E293B),
                        androidx.compose.ui.graphics.BlendMode.SrcIn
                    )
                }
            )
        }

        // 1. Draw Multi-Layered Ground Shadows
        drawOval(
            color = Color(0x1A000000),
            topLeft = Offset(centerX - r * 1.05f, centerY + r * 0.62f - bobbingOffset * 0.3f),
            size = Size(r * 2.1f, 32.dp.toPx())
        )
        drawOval(
            color = Color(0x2E000000),
            topLeft = Offset(centerX - r * 0.85f, centerY + r * 0.65f - bobbingOffset * 0.5f),
            size = Size(r * 1.7f, 22.dp.toPx())
        )

        // 1.8. Stage 3 (Level 100+) & Stage 4 (Level 200+) Legendary Cosmic & Divine Background Auras
        if (stage >= 3) {
            val animAngle = ((System.currentTimeMillis() / 25) % 360).toFloat()
            when (type) {
                "SHIBA" -> {
                    // Amaterasu Solar Fire Disc floating behind shoulders
                    drawCircle(
                        color = Color(0xFFFF6D00).copy(alpha = 0.4f),
                        radius = r * 1.35f,
                        center = Offset(centerX, centerY - r * 0.2f)
                    )
                    drawCircle(
                        color = Color(0xFFFFD54F).copy(alpha = 0.6f),
                        radius = r * 1.15f,
                        center = Offset(centerX, centerY - r * 0.2f),
                        style = Stroke(width = 6.dp.toPx())
                    )
                    for (i in 0..7) {
                        val rad = Math.toRadians((animAngle + i * 45).toDouble())
                        val px = centerX + Math.cos(rad).toFloat() * r * 1.25f
                        val py = (centerY - r * 0.2f) + Math.sin(rad).toFloat() * r * 1.25f
                        drawCircle(Color(0xFFFF3D00), radius = 8.dp.toPx(), center = Offset(px, py))
                    }
                }
                "SLIME" -> {
                    // Majestic Planetary Saturn Rings around body base
                    drawOval(
                        color = Color(0xFFE040FB).copy(alpha = 0.4f),
                        topLeft = Offset(centerX - r * 1.6f, centerY - r * 0.1f),
                        size = Size(r * 3.2f, r * 0.65f),
                        style = Stroke(width = 8.dp.toPx())
                    )
                    drawOval(
                        color = Color(0xFF00E5FF).copy(alpha = 0.5f),
                        topLeft = Offset(centerX - r * 1.45f, centerY - r * 0.05f),
                        size = Size(r * 2.9f, r * 0.5f),
                        style = Stroke(width = 5.dp.toPx())
                    )
                }
                "KITTY" -> {
                    // Angelic Golden Halo / Lotus Pedestal
                    drawOval(
                        color = Color(0xFFFFD54F).copy(alpha = 0.5f),
                        topLeft = Offset(centerX - r * 1.4f, centerY + r * 0.35f),
                        size = Size(r * 2.8f, r * 0.55f)
                    )
                    drawCircle(
                        color = Color(0xFFFFF59D).copy(alpha = 0.7f),
                        radius = r * 0.8f,
                        center = Offset(centerX, centerY - r * 0.8f),
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
                "DRACO" -> {
                    // Elemental Plasma Halo behind wings
                    drawCircle(
                        color = Color(0xFFFF1744).copy(alpha = 0.35f),
                        radius = r * 1.45f,
                        center = Offset(centerX, centerY - r * 0.3f)
                    )
                    drawCircle(
                        color = Color(0xFFFFC107).copy(alpha = 0.6f),
                        radius = r * 1.3f,
                        center = Offset(centerX, centerY - r * 0.3f),
                        style = Stroke(width = 5.dp.toPx())
                    )
                }
                "AXOLOTL" -> {
                    // Bioluminescent Ocean Nebula Halo
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFF80AB).copy(alpha = 0.5f), Color(0xFF80D8FF).copy(alpha = 0.3f), Color.Transparent),
                            center = Offset(centerX, centerY - r * 0.3f),
                            radius = r * 1.6f
                        ),
                        radius = r * 1.6f,
                        center = Offset(centerX, centerY - r * 0.3f)
                    )
                }
            }
        }

        if (stage == 4) {
            val fastAngle = ((System.currentTimeMillis() / 15) % 360).toFloat()
            when (type) {
                "SHIBA" -> {
                    // Massive 3D Solar Corona behind Shiba
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFF176).copy(alpha = 0.8f), Color(0xFFFF6D00).copy(alpha = 0.5f), Color.Transparent),
                            center = Offset(centerX, centerY - r * 0.3f),
                            radius = r * 1.9f
                        ),
                        radius = r * 1.9f,
                        center = Offset(centerX, centerY - r * 0.3f)
                    )
                    // Orbiting 3D Magatama Fireballs
                    for (i in 0..5) {
                        val rad = Math.toRadians((fastAngle + i * 60).toDouble())
                        val px = centerX + Math.cos(rad).toFloat() * r * 1.5f
                        val py = (centerY - r * 0.3f) + Math.sin(rad).toFloat() * r * 0.8f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White, Color(0xFFFF3D00)),
                                center = Offset(px - 3f, py - 3f),
                                radius = 14.dp.toPx()
                            ),
                            radius = 12.dp.toPx(),
                            center = Offset(px, py)
                        )
                    }
                }
                "SLIME" -> {
                    // Quantum Dimensional Core Rings
                    drawOval(
                        brush = Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFFE040FB), Color(0xFF00E5FF))),
                        topLeft = Offset(centerX - r * 1.8f, centerY - r * 0.4f),
                        size = Size(r * 3.6f, r * 1.1f),
                        style = Stroke(width = 5.dp.toPx())
                    )
                    for (i in 0..7) {
                        val rad = Math.toRadians((fastAngle + i * 45).toDouble())
                        val px = centerX + Math.cos(rad).toFloat() * r * 1.6f
                        val py = centerY + Math.sin(rad).toFloat() * r * 0.6f
                        drawCircle(Color(0xFFEA80FC), radius = 9.dp.toPx(), center = Offset(px, py))
                    }
                }
                "KITTY" -> {
                    // Divine Goddess Moon Throne
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFF59D).copy(alpha = 0.8f), Color(0xFFF48FB1).copy(alpha = 0.3f), Color.Transparent),
                            center = Offset(centerX, centerY - r * 0.5f),
                            radius = r * 1.9f
                        ),
                        radius = r * 1.9f,
                        center = Offset(centerX, centerY - r * 0.5f)
                    )
                    for (i in 0..4) {
                        val rad = Math.toRadians((fastAngle * 0.7f + i * 72).toDouble())
                        val px = centerX + Math.cos(rad).toFloat() * r * 1.5f
                        val py = (centerY - r * 0.5f) + Math.sin(rad).toFloat() * r * 1.2f
                        drawCircle(Color(0xFFFFD54F), radius = 11.dp.toPx(), center = Offset(px, py))
                    }
                }
                "DRACO" -> {
                    // Primordial Supernova Plasma Field
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFF1744).copy(alpha = 0.7f), Color(0xFFFF9800).copy(alpha = 0.3f), Color.Transparent),
                            center = Offset(centerX, centerY - r * 0.3f),
                            radius = r * 2.0f
                        ),
                        radius = r * 2.0f,
                        center = Offset(centerX, centerY - r * 0.3f)
                    )
                }
                "AXOLOTL" -> {
                    // Cosmic Lotus Water Ring & Aurora Boreal
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFEA80FC).copy(alpha = 0.7f), Color(0xFF00E5FF).copy(alpha = 0.4f), Color.Transparent),
                            center = Offset(centerX, centerY - r * 0.3f),
                            radius = r * 1.9f
                        ),
                        radius = r * 1.9f,
                        center = Offset(centerX, centerY - r * 0.3f)
                    )
                    for (i in 0..5) {
                        val rad = Math.toRadians((fastAngle * 0.8f + i * 60).toDouble())
                        val px = centerX + Math.cos(rad).toFloat() * r * 1.4f
                        val py = (centerY - r * 0.3f) + Math.sin(rad).toFloat() * r * 1.0f
                        drawCircle(Color(0xFF80D8FF), radius = 10.dp.toPx(), center = Offset(px, py))
                    }
                }
            }
        }

        // 1.5. Floating Ambient Sparkles/Hearts when happy & awake
        if (!isSleeping && happiness >= 65f) {
            val animStep = (System.currentTimeMillis() / 60) % 360
            val pY = centerY - r * 0.6f + Math.sin(Math.toRadians((animStep * 2).toDouble())).toFloat() * 12.dp.toPx()
            val pX1 = centerX - r * 1.15f
            val pX2 = centerX + r * 1.15f
            // Little sparkle dots floating around
            drawCircle(Color(0xFFFF80AB).copy(alpha = 0.6f), radius = 6.dp.toPx(), center = Offset(pX1, pY))
            drawCircle(Color(0xFFFFD54F).copy(alpha = 0.7f), radius = 4.dp.toPx(), center = Offset(pX1 - 8.dp.toPx(), pY + 14.dp.toPx()))
            drawCircle(Color(0xFFFF80AB).copy(alpha = 0.6f), radius = 5.dp.toPx(), center = Offset(pX2, pY - 10.dp.toPx()))
            drawCircle(Color(0xFF80D8FF).copy(alpha = 0.7f), radius = 6.dp.toPx(), center = Offset(pX2 + 6.dp.toPx(), pY + 12.dp.toPx()))
        }

        // 2. Draw Sleeping ZZzz elements
        if (isSleeping) {
            val zOffset = (System.currentTimeMillis() / 40) % 200
            val zAlpha1 = (1f - (zOffset / 200f)).coerceIn(0f, 1f)
            val zSize1 = 15.dp.toPx() + (zOffset / 10f)
            
            // Draw floating 'Z's
            val pathZ = Path().apply {
                moveTo(centerX + r * 0.6f + (zOffset * 0.15f), centerY - r * 0.8f - (zOffset * 0.4f))
                lineTo(centerX + r * 0.6f + zSize1 + (zOffset * 0.15f), centerY - r * 0.8f - (zOffset * 0.4f))
                lineTo(centerX + r * 0.6f + (zOffset * 0.15f), centerY - r * 0.8f + zSize1 - (zOffset * 0.4f))
                lineTo(centerX + r * 0.6f + zSize1 + (zOffset * 0.15f), centerY - r * 0.8f + zSize1 - (zOffset * 0.4f))
            }
            drawPath(
                path = pathZ,
                color = Color(0xFFAB47BC).copy(alpha = zAlpha1),
                style = Stroke(width = 4.dp.toPx())
            )
        }

        // 3. Draw Unique Features (Ears/Wings/Horns) BEFORE body
        when (type) {
            "SHIBA" -> {
                // Outer ear shadow depth
                val leftEarShadow = Path().apply {
                    moveTo(centerX - r * 0.68f, centerY - r * 0.48f)
                    lineTo(centerX - r * 0.87f, centerY - r * 1.22f)
                    lineTo(centerX - r * 0.28f, centerY - r * 0.78f)
                    close()
                }
                drawPath(leftEarShadow, Color.Black.copy(alpha = 0.12f))

                // Shiba ears (Triangles on top)
                val leftEar = Path().apply {
                    moveTo(centerX - r * 0.7f, centerY - r * 0.5f)
                    lineTo(centerX - r * 0.85f, centerY - r * 1.2f)
                    lineTo(centerX - r * 0.3f, centerY - r * 0.8f)
                    close()
                }
                drawPath(leftEar, baseColor)
                // Inner left ear with dual-tone depth
                val leftInnerEar = Path().apply {
                    moveTo(centerX - r * 0.65f, centerY - r * 0.58f)
                    lineTo(centerX - r * 0.77f, centerY - r * 1.08f)
                    lineTo(centerX - r * 0.4f, centerY - r * 0.78f)
                    close()
                }
                drawPath(leftInnerEar, Color(0xFFFFCDD2))
                val leftInnerEarDeep = Path().apply {
                    moveTo(centerX - r * 0.67f, centerY - r * 0.65f)
                    lineTo(centerX - r * 0.75f, centerY - r * 0.98f)
                    lineTo(centerX - r * 0.48f, centerY - r * 0.78f)
                    close()
                }
                drawPath(leftInnerEarDeep, Color(0xFFF8BBD0))

                // Right ear shadow
                val rightEarShadow = Path().apply {
                    moveTo(centerX + r * 0.68f, centerY - r * 0.48f)
                    lineTo(centerX + r * 0.87f, centerY - r * 1.22f)
                    lineTo(centerX + r * 0.28f, centerY - r * 0.78f)
                    close()
                }
                drawPath(rightEarShadow, Color.Black.copy(alpha = 0.12f))

                val rightEar = Path().apply {
                    moveTo(centerX + r * 0.7f, centerY - r * 0.5f)
                    lineTo(centerX + r * 0.85f, centerY - r * 1.2f)
                    lineTo(centerX + r * 0.3f, centerY - r * 0.8f)
                    close()
                }
                drawPath(rightEar, baseColor)
                // Inner right ear
                val rightInnerEar = Path().apply {
                    moveTo(centerX + r * 0.65f, centerY - r * 0.58f)
                    lineTo(centerX + r * 0.77f, centerY - r * 1.08f)
                    lineTo(centerX + r * 0.4f, centerY - r * 0.78f)
                    close()
                }
                drawPath(rightInnerEar, Color(0xFFFFCDD2))
                val rightInnerEarDeep = Path().apply {
                    moveTo(centerX + r * 0.67f, centerY - r * 0.65f)
                    lineTo(centerX + r * 0.75f, centerY - r * 0.98f)
                    lineTo(centerX + r * 0.48f, centerY - r * 0.78f)
                    close()
                }
                drawPath(rightInnerEarDeep, Color(0xFFF8BBD0))
            }
            "KITTY" -> {
                // Cat ears
                val leftEar = Path().apply {
                    moveTo(centerX - r * 0.75f, centerY - r * 0.6f)
                    lineTo(centerX - r * 0.82f, centerY - r * 1.15f)
                    lineTo(centerX - r * 0.35f, centerY - r * 0.75f)
                    close()
                }
                drawPath(leftEar, baseColor)
                val leftInnerEar = Path().apply {
                    moveTo(centerX - r * 0.68f, centerY - r * 0.65f)
                    lineTo(centerX - r * 0.74f, centerY - r * 1.05f)
                    lineTo(centerX - r * 0.42f, centerY - r * 0.75f)
                    close()
                }
                drawPath(leftInnerEar, Color(0xFFF8BBD0))
                // Kitty fluff strands inside ear
                drawLine(Color(0xFFFFF1F3), Offset(centerX - r * 0.65f, centerY - r * 0.7f), Offset(centerX - r * 0.6f, centerY - r * 0.9f), strokeWidth = 3.dp.toPx())
                drawLine(Color(0xFFFFF1F3), Offset(centerX - r * 0.55f, centerY - r * 0.72f), Offset(centerX - r * 0.5f, centerY - r * 0.88f), strokeWidth = 3.dp.toPx())

                val rightEar = Path().apply {
                    moveTo(centerX + r * 0.75f, centerY - r * 0.6f)
                    lineTo(centerX + r * 0.82f, centerY - r * 1.15f)
                    lineTo(centerX + r * 0.35f, centerY - r * 0.75f)
                    close()
                }
                drawPath(rightEar, baseColor)
                val rightInnerEar = Path().apply {
                    moveTo(centerX + r * 0.68f, centerY - r * 0.65f)
                    lineTo(centerX + r * 0.74f, centerY - r * 1.05f)
                    lineTo(centerX + r * 0.42f, centerY - r * 0.75f)
                    close()
                }
                drawPath(rightInnerEar, Color(0xFFF8BBD0))
                drawLine(Color(0xFFFFF1F3), Offset(centerX + r * 0.65f, centerY - r * 0.7f), Offset(centerX + r * 0.6f, centerY - r * 0.9f), strokeWidth = 3.dp.toPx())
                drawLine(Color(0xFFFFF1F3), Offset(centerX + r * 0.55f, centerY - r * 0.72f), Offset(centerX + r * 0.5f, centerY - r * 0.88f), strokeWidth = 3.dp.toPx())
            }
            "DRACO" -> {
                // Dragon horns (orange-yellow with ridges)
                drawOval(
                    color = Color(0xFFFFB74D),
                    topLeft = Offset(centerX - r * 0.4f, centerY - r * 1.1f),
                    size = Size(20.dp.toPx(), 45.dp.toPx())
                )
                drawOval(
                    color = Color(0xFFFF9800),
                    topLeft = Offset(centerX - r * 0.4f + 3.dp.toPx(), centerY - r * 1.05f),
                    size = Size(14.dp.toPx(), 35.dp.toPx())
                )
                drawOval(
                    color = Color(0xFFFFB74D),
                    topLeft = Offset(centerX + r * 0.4f - 20.dp.toPx(), centerY - r * 1.1f),
                    size = Size(20.dp.toPx(), 45.dp.toPx())
                )
                drawOval(
                    color = Color(0xFFFF9800),
                    topLeft = Offset(centerX + r * 0.4f - 17.dp.toPx(), centerY - r * 1.05f),
                    size = Size(14.dp.toPx(), 35.dp.toPx())
                )

                // Dragon Wings (drawn on background) with detailed membrane struts
                val leftWing = Path().apply {
                    moveTo(centerX - r * 0.7f, centerY - r * 0.1f)
                    lineTo(centerX - r * 1.65f, centerY - r * 0.6f)
                    lineTo(centerX - r * 1.45f, centerY - r * 0.1f)
                    lineTo(centerX - r * 1.6f, centerY + r * 0.3f)
                    lineTo(centerX - r * 0.6f, centerY + r * 0.35f)
                    close()
                }
                drawPath(leftWing, Color(0xFF1B5E20))
                val leftWingInner = Path().apply {
                    moveTo(centerX - r * 0.75f, centerY - r * 0.05f)
                    lineTo(centerX - r * 1.55f, centerY - r * 0.5f)
                    lineTo(centerX - r * 1.38f, centerY - r * 0.08f)
                    lineTo(centerX - r * 1.5f, centerY + r * 0.22f)
                    lineTo(centerX - r * 0.65f, centerY + r * 0.28f)
                    close()
                }
                drawPath(leftWingInner, Color(0xFF2E7D32))
                // Wing bone struts
                drawLine(Color(0xFF1B5E20), Offset(centerX - r * 0.7f, centerY - r * 0.1f), Offset(centerX - r * 1.38f, centerY - r * 0.08f), strokeWidth = 3.dp.toPx())
                
                val rightWing = Path().apply {
                    moveTo(centerX + r * 0.7f, centerY - r * 0.1f)
                    lineTo(centerX + r * 1.65f, centerY - r * 0.6f)
                    lineTo(centerX + r * 1.45f, centerY - r * 0.1f)
                    lineTo(centerX + r * 1.6f, centerY + r * 0.3f)
                    lineTo(centerX + r * 0.6f, centerY + r * 0.35f)
                    close()
                }
                drawPath(rightWing, Color(0xFF1B5E20))
                val rightWingInner = Path().apply {
                    moveTo(centerX + r * 0.75f, centerY - r * 0.05f)
                    lineTo(centerX + r * 1.55f, centerY - r * 0.5f)
                    lineTo(centerX + r * 1.38f, centerY - r * 0.08f)
                    lineTo(centerX + r * 1.5f, centerY + r * 0.22f)
                    lineTo(centerX + r * 0.65f, centerY + r * 0.28f)
                    close()
                }
                drawPath(rightWingInner, Color(0xFF2E7D32))
                drawLine(Color(0xFF1B5E20), Offset(centerX + r * 0.7f, centerY - r * 0.1f), Offset(centerX + r * 1.38f, centerY - r * 0.08f), strokeWidth = 3.dp.toPx())
            }
        }

        // 3.5. Draw Tail for animation (drawn behind body)
        if (!isSleeping) {
            when (type) {
                "SHIBA" -> {
                    val tailPath = Path().apply {
                        moveTo(centerX + r * 0.7f, centerY + r * 0.2f)
                        cubicTo(
                            centerX + r * 1.35f, centerY + r * 0.1f - (tailWag * 0.5f),
                            centerX + r * 1.45f, centerY - r * 0.35f + tailWag,
                            centerX + r * 0.85f, centerY - r * 0.25f
                        )
                    }
                    drawPath(
                        path = tailPath,
                        color = baseColor,
                        style = Stroke(width = r * 0.22f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                    // White tip of curled Shiba tail
                    drawCircle(Color(0xFFFFF8E1), radius = 14.dp.toPx(), center = Offset(centerX + r * 0.85f, centerY - r * 0.25f))
                }
                "KITTY" -> {
                    val tailPath = Path().apply {
                        moveTo(centerX - r * 0.7f, centerY + r * 0.3f)
                        cubicTo(
                            centerX - r * 1.25f, centerY + r * 0.4f - (tailWag * 0.6f),
                            centerX - r * 1.45f, centerY + (tailWag * 0.8f),
                            centerX - r * 1.55f, centerY - r * 0.2f + tailWag
                        )
                    }
                    drawPath(
                        path = tailPath,
                        color = baseColor,
                        style = Stroke(width = r * 0.14f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                    // Darker stripe marks on tail
                    drawCircle(Color(0xFFD81B60).copy(alpha = 0.3f), radius = 8.dp.toPx(), center = Offset(centerX - r * 1.1f, centerY + r * 0.3f - (tailWag * 0.2f)))
                    drawCircle(Color(0xFFD81B60).copy(alpha = 0.3f), radius = 8.dp.toPx(), center = Offset(centerX - r * 1.4f, centerY + r * 0.1f + (tailWag * 0.4f)))
                }
                "DRACO" -> {
                    // Dragon tail with custom end spade brush
                    val tailPath = Path().apply {
                        moveTo(centerX - r * 0.7f, centerY + r * 0.3f)
                        cubicTo(
                            centerX - r * 1.3f, centerY + r * 0.5f - (tailWag * 0.4f),
                            centerX - r * 1.5f, centerY + (tailWag * 1.1f),
                            centerX - r * 1.3f, centerY - r * 0.1f + tailWag
                        )
                    }
                    drawPath(
                        path = tailPath,
                        color = baseColor,
                        style = Stroke(width = r * 0.16f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                    // Yellow/Orange tail spade tip with fiery inner core
                    drawCircle(
                        color = Color(0xFFFF9800),
                        radius = 14.dp.toPx(),
                        center = Offset(centerX - r * 1.35f, centerY - r * 0.1f + tailWag)
                    )
                    drawCircle(
                        color = Color(0xFFFFEE58),
                        radius = 7.dp.toPx(),
                        center = Offset(centerX - r * 1.35f, centerY - r * 0.1f + tailWag)
                    )
                }
                "AXOLOTL" -> {
                    // Axolotl delicate translucent tail fin
                    val tailPath = Path().apply {
                        moveTo(centerX - r * 0.7f, centerY + r * 0.2f)
                        cubicTo(
                            centerX - r * 1.3f, centerY + r * 0.3f - (tailWag * 0.5f),
                            centerX - r * 1.5f, centerY + (tailWag * 0.9f),
                            centerX - r * 1.35f, centerY - r * 0.15f + tailWag
                        )
                    }
                    drawPath(
                        path = tailPath,
                        color = baseColor,
                        style = Stroke(width = r * 0.18f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                    // Neon pink fin fringe
                    drawCircle(Color(0xFFFF80AB), radius = 12.dp.toPx(), center = Offset(centerX - r * 1.35f, centerY - r * 0.05f + tailWag))
                }
            }
        }

        // 4. Draw Main Body & 3D Plush Volumetric Shading
        val highlightColor = when (type) {
            "SHIBA" -> Color(0xFFFFD180)
            "SLIME" -> Color(0xFFB3E5FC)
            "KITTY" -> Color(0xFFFCE4EC)
            "DRACO" -> Color(0xFFA5D6A7)
            "AXOLOTL" -> Color(0xFFFF80AB)
            else -> Color(0xFFE0E0E0)
        }
        val shadowColor = when (type) {
            "SHIBA" -> Color(0xFFB26A00)
            "SLIME" -> Color(0xFF0288D1)
            "KITTY" -> Color(0xFFC2185B)
            "DRACO" -> Color(0xFF1B5E20)
            "AXOLOTL" -> Color(0xFF880E4F)
            else -> Color(0xFF616161)
        }
        val body3dBrush = Brush.radialGradient(
            colors = listOf(highlightColor, baseColor, shadowColor),
            center = Offset(centerX - r * 0.35f, centerY - r * 0.45f),
            radius = r * 1.8f
        )

        when (type) {
            "SLIME" -> {
                // Slimes are 3D squishy jelly spheres!
                val slimePath = Path().apply {
                    moveTo(centerX - r * 1.05f, centerY + r * 0.5f)
                    cubicTo(
                        centerX - r * 1.2f, centerY - r * 0.3f,
                        centerX - r * 0.6f, centerY - r * 1.05f,
                        centerX, centerY - r * 0.95f
                    )
                    cubicTo(
                        centerX + r * 0.6f, centerY - r * 1.05f,
                        centerX + r * 1.2f, centerY - r * 0.3f,
                        centerX + r * 1.05f, centerY + r * 0.5f
                    )
                    cubicTo(
                        centerX + r * 0.8f, centerY + r * 0.85f,
                        centerX - r * 0.8f, centerY + r * 0.85f,
                        centerX - r * 1.05f, centerY + r * 0.5f
                    )
                }
                drawPath(slimePath, brush = body3dBrush)
                // Outer translucent 3D gel layer
                val slimeOuter = Path().apply {
                    moveTo(centerX - r * 1.08f, centerY + r * 0.52f)
                    cubicTo(
                        centerX - r * 1.25f, centerY - r * 0.32f,
                        centerX - r * 0.62f, centerY - r * 1.08f,
                        centerX, centerY - r * 0.98f
                    )
                    cubicTo(
                        centerX + r * 0.62f, centerY - r * 1.08f,
                        centerX + r * 1.25f, centerY - r * 0.32f,
                        centerX + r * 1.08f, centerY + r * 0.52f
                    )
                    cubicTo(
                        centerX + r * 0.82f, centerY + r * 0.88f,
                        centerX - r * 0.82f, centerY + r * 0.88f,
                        centerX - r * 1.08f, centerY + r * 0.52f
                    )
                }
                drawPath(slimeOuter, Color.White.copy(alpha = 0.22f))
                // Glossy 3D Specular Highlight on upper-left dome
                drawOval(
                    color = Color.White.copy(alpha = 0.55f),
                    topLeft = Offset(centerX - r * 0.65f, centerY - r * 0.72f),
                    size = Size(r * 0.75f, r * 0.32f)
                )
                drawOval(
                    color = Color.White.copy(alpha = 0.85f),
                    topLeft = Offset(centerX - r * 0.5f, centerY - r * 0.66f),
                    size = Size(r * 0.3f, r * 0.15f)
                )
            }
            else -> {
                // Main 3D volumetric plush body base
                drawRoundRect(
                    brush = body3dBrush,
                    topLeft = Offset(centerX - r, centerY - r * 0.8f),
                    size = Size(r * 2f, r * 1.5f),
                    cornerRadius = CornerRadius(60.dp.toPx(), 60.dp.toPx())
                )
                // Bottom 3D plush ambient occlusion curve
                val bottomShadow = Path().apply {
                    moveTo(centerX - r * 0.98f, centerY + r * 0.2f)
                    cubicTo(
                        centerX - r * 0.6f, centerY + r * 0.72f,
                        centerX + r * 0.6f, centerY + r * 0.72f,
                        centerX + r * 0.98f, centerY + r * 0.2f
                    )
                    lineTo(centerX + r * 0.98f, centerY + r * 0.55f)
                    cubicTo(
                        centerX + r * 0.6f, centerY + r * 0.72f,
                        centerX - r * 0.6f, centerY + r * 0.72f,
                        centerX - r * 0.98f, centerY + r * 0.55f
                    )
                    close()
                }
                drawPath(bottomShadow, Color.Black.copy(alpha = 0.18f))

                // Glossy 3D Specular Highlight & Rim Curve
                drawOval(
                    color = Color.White.copy(alpha = 0.35f),
                    topLeft = Offset(centerX - r * 0.65f, centerY - r * 0.72f),
                    size = Size(r * 0.85f, r * 0.35f)
                )
                drawArc(
                    color = Color.White.copy(alpha = 0.30f),
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(centerX - r * 0.9f, centerY - r * 0.75f),
                    size = Size(r * 1.8f, r * 0.8f),
                    style = Stroke(width = 6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
            }
        }

        // 5. Draw Belly Patch & Extra High-Detail Markings
        when (type) {
            "SHIBA" -> {
                // Fluffy side cheek fur tufts
                val leftTuft = Path().apply {
                    moveTo(centerX - r * 0.95f, centerY - r * 0.2f)
                    lineTo(centerX - r * 1.15f, centerY - r * 0.1f)
                    lineTo(centerX - r * 0.98f, centerY)
                    lineTo(centerX - r * 1.12f, centerY + r * 0.12f)
                    lineTo(centerX - r * 0.95f, centerY + r * 0.22f)
                }
                drawPath(leftTuft, baseColor)
                val rightTuft = Path().apply {
                    moveTo(centerX + r * 0.95f, centerY - r * 0.2f)
                    lineTo(centerX + r * 1.15f, centerY - r * 0.1f)
                    lineTo(centerX + r * 0.98f, centerY)
                    lineTo(centerX + r * 1.12f, centerY + r * 0.12f)
                    lineTo(centerX + r * 0.95f, centerY + r * 0.22f)
                }
                drawPath(rightTuft, baseColor)

                drawOval(
                    color = bellyColor,
                    topLeft = Offset(centerX - r * 0.6f, centerY - r * 0.1f),
                    size = Size(r * 1.2f, r * 0.75f)
                )
                // Cute white eyebrow dots with soft depth
                drawCircle(Color.Black.copy(alpha = 0.1f), radius = 7.5f.dp.toPx(), center = Offset(centerX - r * 0.45f, centerY - r * 0.40f))
                drawCircle(Color.White, radius = 7.dp.toPx(), center = Offset(centerX - r * 0.45f, centerY - r * 0.42f))
                drawCircle(Color.Black.copy(alpha = 0.1f), radius = 7.5f.dp.toPx(), center = Offset(centerX + r * 0.45f, centerY - r * 0.40f))
                drawCircle(Color.White, radius = 7.dp.toPx(), center = Offset(centerX + r * 0.45f, centerY - r * 0.42f))
                
                // Cute white muzzle patch
                drawOval(
                    color = Color(0xFFFFF8E1),
                    topLeft = Offset(centerX - r * 0.38f, centerY - r * 0.05f),
                    size = Size(r * 0.76f, r * 0.38f)
                )
                // Front paws with pink pad + 3 toe beans per paw!
                drawCircle(Color(0xFFFFF3E0), radius = 19.dp.toPx(), center = Offset(centerX - r * 0.5f, centerY + r * 0.65f))
                drawCircle(Color(0xFFFFF3E0), radius = 19.dp.toPx(), center = Offset(centerX + r * 0.5f, centerY + r * 0.65f))
                // Left paw beans
                drawCircle(Color(0xFFF8BBD0), radius = 6.5f.dp.toPx(), center = Offset(centerX - r * 0.5f, centerY + r * 0.67f))
                drawCircle(Color(0xFFF48FB1), radius = 2.5f.dp.toPx(), center = Offset(centerX - r * 0.5f - 7.dp.toPx(), centerY + r * 0.57f))
                drawCircle(Color(0xFFF48FB1), radius = 2.5f.dp.toPx(), center = Offset(centerX - r * 0.5f, centerY + r * 0.55f))
                drawCircle(Color(0xFFF48FB1), radius = 2.5f.dp.toPx(), center = Offset(centerX - r * 0.5f + 7.dp.toPx(), centerY + r * 0.57f))
                // Right paw beans
                drawCircle(Color(0xFFF8BBD0), radius = 6.5f.dp.toPx(), center = Offset(centerX + r * 0.5f, centerY + r * 0.67f))
                drawCircle(Color(0xFFF48FB1), radius = 2.5f.dp.toPx(), center = Offset(centerX + r * 0.5f - 7.dp.toPx(), centerY + r * 0.57f))
                drawCircle(Color(0xFFF48FB1), radius = 2.5f.dp.toPx(), center = Offset(centerX + r * 0.5f, centerY + r * 0.55f))
                drawCircle(Color(0xFFF48FB1), radius = 2.5f.dp.toPx(), center = Offset(centerX + r * 0.5f + 7.dp.toPx(), centerY + r * 0.57f))
            }
            "SLIME" -> {
                drawOval(
                    color = bellyColor,
                    topLeft = Offset(centerX - r * 0.7f, centerY + r * 0.1f),
                    size = Size(r * 1.4f, r * 0.45f)
                )
                // Glossy top specular reflection curves
                drawArc(
                    color = Color.White.copy(alpha = 0.65f),
                    startAngle = 200f,
                    sweepAngle = 60f,
                    useCenter = false,
                    topLeft = Offset(centerX - r * 0.85f, centerY - r * 0.85f),
                    size = Size(r * 1.7f, r * 1.5f),
                    style = Stroke(width = 6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
                // Inner glowing bubbles & floating sparkle stars inside slime body
                drawCircle(Color.White.copy(alpha = 0.5f), radius = 11.dp.toPx(), center = Offset(centerX + r * 0.5f, centerY - r * 0.2f))
                drawCircle(Color.White.copy(alpha = 0.3f), radius = 5.dp.toPx(), center = Offset(centerX + r * 0.53f, centerY - r * 0.23f))
                drawCircle(Color.White.copy(alpha = 0.35f), radius = 7.dp.toPx(), center = Offset(centerX + r * 0.3f, centerY + r * 0.3f))
                drawCircle(Color.White.copy(alpha = 0.45f), radius = 9.dp.toPx(), center = Offset(centerX - r * 0.55f, centerY + r * 0.2f))
                drawCircle(Color.White.copy(alpha = 0.6f), radius = 4.dp.toPx(), center = Offset(centerX - r * 0.3f, centerY - r * 0.45f))
            }
            "KITTY" -> {
                // Fluffy side cheek fur tufts
                val leftTuft = Path().apply {
                    moveTo(centerX - r * 0.95f, centerY - r * 0.15f)
                    lineTo(centerX - r * 1.15f, centerY - r * 0.05f)
                    lineTo(centerX - r * 0.98f, centerY + r * 0.05f)
                    lineTo(centerX - r * 1.12f, centerY + r * 0.18f)
                    lineTo(centerX - r * 0.95f, centerY + r * 0.25f)
                }
                drawPath(leftTuft, baseColor)
                val rightTuft = Path().apply {
                    moveTo(centerX + r * 0.95f, centerY - r * 0.15f)
                    lineTo(centerX + r * 1.15f, centerY - r * 0.05f)
                    lineTo(centerX + r * 0.98f, centerY + r * 0.05f)
                    lineTo(centerX + r * 1.12f, centerY + r * 0.18f)
                    lineTo(centerX + r * 0.95f, centerY + r * 0.25f)
                }
                drawPath(rightTuft, baseColor)

                drawOval(
                    color = bellyColor,
                    topLeft = Offset(centerX - r * 0.6f, centerY - r * 0.1f),
                    size = Size(r * 1.2f, r * 0.75f)
                )
                // Forehead tabby stripes with high definition
                val stripePath = Path().apply {
                    moveTo(centerX - r * 0.25f, centerY - r * 0.8f)
                    lineTo(centerX - r * 0.15f, centerY - r * 0.5f)
                    lineTo(centerX - r * 0.05f, centerY - r * 0.8f)
                    lineTo(centerX, centerY - r * 0.48f)
                    lineTo(centerX + r * 0.05f, centerY - r * 0.8f)
                    lineTo(centerX + r * 0.15f, centerY - r * 0.5f)
                    lineTo(centerX + r * 0.25f, centerY - r * 0.8f)
                }
                drawPath(stripePath, Color(0xFFAD1457).copy(alpha = 0.35f), style = Stroke(width = 4.5f.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                // Front paws with pink pad + 3 toe beans per paw!
                drawCircle(Color(0xFFFFF1F3), radius = 17.dp.toPx(), center = Offset(centerX - r * 0.45f, centerY + r * 0.65f))
                drawCircle(Color(0xFFFFF1F3), radius = 17.dp.toPx(), center = Offset(centerX + r * 0.45f, centerY + r * 0.65f))
                // Left paw beans
                drawCircle(Color(0xFFF48FB1), radius = 6.dp.toPx(), center = Offset(centerX - r * 0.45f, centerY + r * 0.67f))
                drawCircle(Color(0xFFF06292), radius = 2.2f.dp.toPx(), center = Offset(centerX - r * 0.45f - 6.dp.toPx(), centerY + r * 0.57f))
                drawCircle(Color(0xFFF06292), radius = 2.2f.dp.toPx(), center = Offset(centerX - r * 0.45f, centerY + r * 0.55f))
                drawCircle(Color(0xFFF06292), radius = 2.2f.dp.toPx(), center = Offset(centerX - r * 0.45f + 6.dp.toPx(), centerY + r * 0.57f))
                // Right paw beans
                drawCircle(Color(0xFFF48FB1), radius = 6.dp.toPx(), center = Offset(centerX + r * 0.45f, centerY + r * 0.67f))
                drawCircle(Color(0xFFF06292), radius = 2.2f.dp.toPx(), center = Offset(centerX + r * 0.45f - 6.dp.toPx(), centerY + r * 0.57f))
                drawCircle(Color(0xFFF06292), radius = 2.2f.dp.toPx(), center = Offset(centerX + r * 0.45f, centerY + r * 0.55f))
                drawCircle(Color(0xFFF06292), radius = 2.2f.dp.toPx(), center = Offset(centerX + r * 0.45f + 6.dp.toPx(), centerY + r * 0.57f))
            }
            "DRACO" -> {
                // Dorsal back spines / scutes along top head & back
                for (i in 0..3) {
                    val sx = centerX - r * 0.6f + i * (r * 0.4f)
                    val sy = centerY - r * 0.78f
                    val spine = Path().apply {
                        moveTo(sx - 8.dp.toPx(), sy)
                        lineTo(sx, sy - 14.dp.toPx())
                        lineTo(sx + 8.dp.toPx(), sy)
                        close()
                    }
                    drawPath(spine, Color(0xFF2E7D32))
                }

                drawOval(
                    color = bellyColor,
                    topLeft = Offset(centerX - r * 0.6f, centerY - r * 0.1f),
                    size = Size(r * 1.2f, r * 0.75f)
                )
                // Horizontal dragon belly ribs with detailed definition
                for (i in 1..4) {
                    val ry = (centerY - r * 0.05f) + i * (r * 0.15f)
                    drawLine(
                        color = Color(0xFF66BB6A),
                        start = Offset(centerX - r * 0.42f, ry),
                        end = Offset(centerX + r * 0.42f, ry),
                        strokeWidth = 3.5f.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
                // Little happy smoke spirals from snout
                drawCircle(Color(0x88FFFFFF), radius = 7.dp.toPx(), center = Offset(centerX + r * 0.25f, centerY - r * 0.05f))
                drawCircle(Color(0x55FFFFFF), radius = 10.dp.toPx(), center = Offset(centerX + r * 0.38f, centerY - r * 0.15f))
                // Clawed front paws
                drawCircle(Color(0xFF388E3C), radius = 16.dp.toPx(), center = Offset(centerX - r * 0.45f, centerY + r * 0.65f))
                drawCircle(Color(0xFF388E3C), radius = 16.dp.toPx(), center = Offset(centerX + r * 0.45f, centerY + r * 0.65f))
                // Little white claws
                drawCircle(Color(0xFFFFF8E1), radius = 3.dp.toPx(), center = Offset(centerX - r * 0.45f - 8.dp.toPx(), centerY + r * 0.73f))
                drawCircle(Color(0xFFFFF8E1), radius = 3.dp.toPx(), center = Offset(centerX - r * 0.45f, centerY + r * 0.75f))
                drawCircle(Color(0xFFFFF8E1), radius = 3.dp.toPx(), center = Offset(centerX - r * 0.45f + 8.dp.toPx(), centerY + r * 0.73f))
                drawCircle(Color(0xFFFFF8E1), radius = 3.dp.toPx(), center = Offset(centerX + r * 0.45f - 8.dp.toPx(), centerY + r * 0.73f))
                drawCircle(Color(0xFFFFF8E1), radius = 3.dp.toPx(), center = Offset(centerX + r * 0.45f, centerY + r * 0.75f))
                drawCircle(Color(0xFFFFF8E1), radius = 3.dp.toPx(), center = Offset(centerX + r * 0.45f + 8.dp.toPx(), centerY + r * 0.73f))
            }
            "AXOLOTL" -> {
                // 3 pairs of feathery external gills on each side of head!
                for (i in -1..1) {
                    val angleOffset = i * 22f
                    // Left gill stalk
                    drawArc(
                        color = Color(0xFFAD1457),
                        startAngle = 160f + angleOffset,
                        sweepAngle = 45f,
                        useCenter = false,
                        topLeft = Offset(centerX - r * 1.55f, centerY - r * 0.75f + (i * r * 0.22f)),
                        size = Size(r * 0.9f, r * 0.45f),
                        style = Stroke(width = 6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                    // Left neon pink feathery fringes
                    drawCircle(Color(0xFFFF4081), radius = 9.dp.toPx(), center = Offset(centerX - r * 1.15f, centerY - r * 0.5f + (i * r * 0.2f)))
                    drawCircle(Color(0xFFFF80AB), radius = 6.dp.toPx(), center = Offset(centerX - r * 1.25f, centerY - r * 0.52f + (i * r * 0.2f)))
                    
                    // Right gill stalk
                    drawArc(
                        color = Color(0xFFAD1457),
                        startAngle = -25f - angleOffset,
                        sweepAngle = 45f,
                        useCenter = false,
                        topLeft = Offset(centerX + r * 0.65f, centerY - r * 0.75f + (i * r * 0.22f)),
                        size = Size(r * 0.9f, r * 0.45f),
                        style = Stroke(width = 6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                    // Right neon pink feathery fringes
                    drawCircle(Color(0xFFFF4081), radius = 9.dp.toPx(), center = Offset(centerX + r * 1.15f, centerY - r * 0.5f + (i * r * 0.2f)))
                    drawCircle(Color(0xFFFF80AB), radius = 6.dp.toPx(), center = Offset(centerX + r * 1.25f, centerY - r * 0.52f + (i * r * 0.2f)))
                }

                drawOval(
                    color = bellyColor,
                    topLeft = Offset(centerX - r * 0.58f, centerY - r * 0.1f),
                    size = Size(r * 1.16f, r * 0.75f)
                )
                // Cute amphibious webbed paws
                drawCircle(Color(0xFFF48FB1), radius = 16.dp.toPx(), center = Offset(centerX - r * 0.45f, centerY + r * 0.65f))
                drawCircle(Color(0xFFF48FB1), radius = 16.dp.toPx(), center = Offset(centerX + r * 0.45f, centerY + r * 0.65f))
                // Webbed toes
                drawCircle(Color(0xFFF8BBD0), radius = 4.dp.toPx(), center = Offset(centerX - r * 0.45f - 6.dp.toPx(), centerY + r * 0.72f))
                drawCircle(Color(0xFFF8BBD0), radius = 4.dp.toPx(), center = Offset(centerX - r * 0.45f, centerY + r * 0.75f))
                drawCircle(Color(0xFFF8BBD0), radius = 4.dp.toPx(), center = Offset(centerX - r * 0.45f + 6.dp.toPx(), centerY + r * 0.72f))
                drawCircle(Color(0xFFF8BBD0), radius = 4.dp.toPx(), center = Offset(centerX + r * 0.45f - 6.dp.toPx(), centerY + r * 0.72f))
                drawCircle(Color(0xFFF8BBD0), radius = 4.dp.toPx(), center = Offset(centerX + r * 0.45f, centerY + r * 0.75f))
                drawCircle(Color(0xFFF8BBD0), radius = 4.dp.toPx(), center = Offset(centerX + r * 0.45f + 6.dp.toPx(), centerY + r * 0.72f))
            }
        }

        // 5.8. Stage 2 (Level 50+), Stage 3 (Level 100+), & Stage 4 (Level 200+) Forefront 3D Accessories & Markings
        if (stage >= 2) {
            when (type) {
                "SHIBA" -> {
                    // Golden wrist cuffs on front paws
                    drawRoundRect(
                        color = Color(0xFFFFD54F),
                        topLeft = Offset(centerX - r * 0.65f, centerY + r * 0.52f),
                        size = Size(32.dp.toPx(), 8.dp.toPx()),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0xFFFFD54F),
                        topLeft = Offset(centerX + r * 0.35f, centerY + r * 0.52f),
                        size = Size(32.dp.toPx(), 8.dp.toPx()),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                    // Royal Samurai Scarf/Cape across shoulders
                    val capePath = Path().apply {
                        moveTo(centerX - r * 0.7f, centerY - r * 0.2f)
                        lineTo(centerX, centerY + r * 0.05f)
                        lineTo(centerX + r * 0.7f, centerY - r * 0.2f)
                        lineTo(centerX + r * 0.85f, centerY - r * 0.05f)
                        lineTo(centerX, centerY + r * 0.25f)
                        lineTo(centerX - r * 0.85f, centerY - r * 0.05f)
                        close()
                    }
                    drawPath(capePath, if (stage >= 3) Color(0xFFD32F2F) else Color(0xFF1976D2))
                    // Golden crest in center of scarf
                    drawCircle(Color(0xFFFFB300), radius = 10.dp.toPx(), center = Offset(centerX, centerY + r * 0.12f))
                    drawCircle(Color(0xFFFFF8E1), radius = 5.dp.toPx(), center = Offset(centerX, centerY + r * 0.12f))

                    if (stage >= 3) {
                        // Divine Okami forehead & cheek swirl marks
                        drawCircle(Color(0xFFD32F2F), radius = 6.dp.toPx(), center = Offset(centerX, centerY - r * 0.5f))
                    }
                }
                "SLIME" -> {
                    // Royal Floating Gold Crown above dome
                    val crownY = centerY - r * 1.15f + bobbingOffset * 0.5f
                    val crownPath = Path().apply {
                        moveTo(centerX - 24.dp.toPx(), crownY)
                        lineTo(centerX - 28.dp.toPx(), crownY - 26.dp.toPx())
                        lineTo(centerX - 10.dp.toPx(), crownY - 12.dp.toPx())
                        lineTo(centerX, crownY - 30.dp.toPx())
                        lineTo(centerX + 10.dp.toPx(), crownY - 12.dp.toPx())
                        lineTo(centerX + 28.dp.toPx(), crownY - 26.dp.toPx())
                        lineTo(centerX + 24.dp.toPx(), crownY)
                        close()
                    }
                    drawPath(crownPath, Color(0xFFFFD54F))
                    drawPath(crownPath, Color(0xFFFFA000), style = Stroke(width = 2.dp.toPx()))
                    // Crown jewels
                    drawCircle(Color(0xFFE91E63), radius = 4.dp.toPx(), center = Offset(centerX, crownY - 10.dp.toPx()))
                    drawCircle(Color(0xFF00E5FF), radius = 3.5f.dp.toPx(), center = Offset(centerX - 16.dp.toPx(), crownY - 8.dp.toPx()))
                    drawCircle(Color(0xFF00E5FF), radius = 3.5f.dp.toPx(), center = Offset(centerX + 16.dp.toPx(), crownY - 8.dp.toPx()))

                    if (stage >= 3) {
                        // Floating orbiting crystal shards
                        val shardAngle = ((System.currentTimeMillis() / 30) % 360).toFloat()
                        for (i in 0..2) {
                            val rad = Math.toRadians((shardAngle + i * 120).toDouble())
                            val sx = centerX + Math.cos(rad).toFloat() * r * 1.2f
                            val sy = centerY + Math.sin(rad).toFloat() * r * 0.6f
                            drawRoundRect(
                                color = Color(0xFFB388FF),
                                topLeft = Offset(sx - 6.dp.toPx(), sy - 12.dp.toPx()),
                                size = Size(12.dp.toPx(), 24.dp.toPx()),
                                cornerRadius = CornerRadius(6.dp.toPx())
                            )
                        }
                    }
                }
                "KITTY" -> {
                    // Glowing Golden Crescent Moon on Forehead
                    drawArc(
                        color = Color(0xFFFFD54F),
                        startAngle = 120f,
                        sweepAngle = 200f,
                        useCenter = true,
                        topLeft = Offset(centerX - 10.dp.toPx(), centerY - r * 0.65f),
                        size = Size(20.dp.toPx(), 20.dp.toPx())
                    )
                    // Silky Royal Mantle draped over shoulders/back
                    val mantlePath = Path().apply {
                        moveTo(centerX - r * 0.8f, centerY - r * 0.1f)
                        lineTo(centerX + r * 0.8f, centerY - r * 0.1f)
                        lineTo(centerX + r * 0.7f, centerY + r * 0.4f)
                        lineTo(centerX - r * 0.7f, centerY + r * 0.4f)
                        close()
                    }
                    drawPath(mantlePath, if (stage >= 3) Color(0xFFF06292).copy(alpha = 0.5f) else Color(0xFF7E57C2).copy(alpha = 0.5f))
                    // Golden mantle trim
                    drawLine(Color(0xFFFFD54F), Offset(centerX - r * 0.7f, centerY + r * 0.4f), Offset(centerX + r * 0.7f, centerY + r * 0.4f), strokeWidth = 3.dp.toPx())

                    if (stage >= 3) {
                        // Floating Diamond Tiara over ears
                        drawCircle(Color(0xFF80D8FF), radius = 6.dp.toPx(), center = Offset(centerX, centerY - r * 0.95f))
                    }
                }
                "DRACO" -> {
                    // Golden armor plating on head & snout
                    val armorPath = Path().apply {
                        moveTo(centerX - r * 0.35f, centerY - r * 0.75f)
                        lineTo(centerX + r * 0.35f, centerY - r * 0.75f)
                        lineTo(centerX, centerY - r * 0.5f)
                        close()
                    }
                    drawPath(armorPath, Color(0xFFFFB300).copy(alpha = 0.7f))

                    if (stage >= 3) {
                        // Floating Sun Plasma Orb floating between crown horns
                        drawCircle(
                            color = Color(0xFFFF6D00),
                            radius = 16.dp.toPx(),
                            center = Offset(centerX, centerY - r * 1.25f)
                        )
                        drawCircle(
                            color = Color(0xFFFFF176),
                            radius = 9.dp.toPx(),
                            center = Offset(centerX, centerY - r * 1.25f)
                        )
                    }
                }
                "AXOLOTL" -> {
                    // Pearl shell collar
                    drawRoundRect(
                        color = Color(0xFFF8BBD0),
                        topLeft = Offset(centerX - r * 0.55f, centerY + r * 0.42f),
                        size = Size(r * 1.1f, 10.dp.toPx()),
                        cornerRadius = CornerRadius(5.dp.toPx())
                    )
                    drawCircle(Color(0xFF80D8FF), radius = 8.dp.toPx(), center = Offset(centerX, centerY + r * 0.45f))
                    if (stage >= 3) {
                        // Star Coral Tiara
                        drawCircle(Color(0xFFEA80FC), radius = 7.dp.toPx(), center = Offset(centerX, centerY - r * 0.92f))
                    }
                }
            }
        }

        if (stage == 4) {
            when (type) {
                "SHIBA" -> {
                    // Glowing Divine Third Eye Ruby & Golden Collar
                    drawCircle(
                        brush = Brush.radialGradient(listOf(Color.White, Color(0xFFFF1744)), center = Offset(centerX - 2f, centerY - r * 0.55f - 2f), radius = 8.dp.toPx()),
                        radius = 7.dp.toPx(),
                        center = Offset(centerX, centerY - r * 0.55f)
                    )
                    drawRoundRect(
                        color = Color(0xFFFFD54F),
                        topLeft = Offset(centerX - r * 0.5f, centerY + r * 0.05f),
                        size = Size(r * 1f, 10.dp.toPx()),
                        cornerRadius = CornerRadius(5.dp.toPx())
                    )
                }
                "SLIME" -> {
                    // Hovering Galactic Core inside Slime chest
                    drawCircle(
                        brush = Brush.radialGradient(listOf(Color.White, Color(0xFFE040FB), Color(0xFF311B92)), center = Offset(centerX, centerY + r * 0.2f), radius = 24.dp.toPx()),
                        radius = 20.dp.toPx(),
                        center = Offset(centerX, centerY + r * 0.2f)
                    )
                }
                "KITTY" -> {
                    // Diamond Stardust Collar & Floating Astral Tiara
                    drawCircle(
                        brush = Brush.radialGradient(listOf(Color.White, Color(0xFF80D8FF)), center = Offset(centerX, centerY + r * 0.25f), radius = 14.dp.toPx()),
                        radius = 12.dp.toPx(),
                        center = Offset(centerX, centerY + r * 0.25f)
                    )
                }
                "DRACO" -> {
                    // Glowing Ruby Dragon Heart Shield on chest
                    val shieldPath = Path().apply {
                        moveTo(centerX, centerY + r * 0.05f)
                        lineTo(centerX + r * 0.35f, centerY + r * 0.2f)
                        lineTo(centerX, centerY + r * 0.5f)
                        lineTo(centerX - r * 0.35f, centerY + r * 0.2f)
                        close()
                    }
                    drawPath(shieldPath, brush = Brush.radialGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF3D00)), center = Offset(centerX, centerY + r * 0.2f), radius = r * 0.4f))
                }
                "AXOLOTL" -> {
                    // Floating Heart of Ocean Pearl Orb
                    drawCircle(
                        brush = Brush.radialGradient(listOf(Color.White, Color(0xFF80D8FF), Color(0xFFE040FB)), center = Offset(centerX, centerY + r * 0.2f), radius = 22.dp.toPx()),
                        radius = 18.dp.toPx(),
                        center = Offset(centerX, centerY + r * 0.2f)
                    )
                }
            }
        }

        // 5.9. Clothing / Outfits OVERLAY (Tailored to fit each pet's exact body shape)
        if (equippedAccessory != null) {
            val clothCode = equippedAccessory.replace("CLOTH_", "")
            
            val petBodyClipPath = Path().apply {
                if (type == "SLIME") {
                    moveTo(centerX - r * 1.05f, centerY + r * 0.5f)
                    cubicTo(
                        centerX - r * 1.2f, centerY - r * 0.3f,
                        centerX - r * 0.6f, centerY - r * 1.05f,
                        centerX, centerY - r * 0.95f
                    )
                    cubicTo(
                        centerX + r * 0.6f, centerY - r * 1.05f,
                        centerX + r * 1.2f, centerY - r * 0.3f,
                        centerX + r * 1.05f, centerY + r * 0.5f
                    )
                    cubicTo(
                        centerX + r * 0.8f, centerY + r * 0.85f,
                        centerX - r * 0.8f, centerY + r * 0.85f,
                        centerX - r * 1.05f, centerY + r * 0.5f
                    )
                } else {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            left = centerX - r,
                            top = centerY - r * 0.8f,
                            right = centerX + r,
                            bottom = centerY + r * 0.7f,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(60.dp.toPx(), 60.dp.toPx())
                        )
                    )
                }
            }

            clipPath(petBodyClipPath) {
                val chestTop = centerY + r * 0.14f
                val chestBottom = centerY + r * 0.85f
                val chestHeight = chestBottom - chestTop
                val chestLeft = centerX - r * 1.1f
                val chestRight = centerX + r * 1.1f
                val chestWidth = chestRight - chestLeft

                when (clothCode) {
                    "SPORTSWR" -> {
                        drawRect(
                            color = Color(0xFF1976D2),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRect(
                            color = Color(0xFFFFD54F),
                            topLeft = Offset(centerX - chestWidth * 0.15f, chestTop),
                            size = Size(chestWidth * 0.3f, chestHeight)
                        )
                        drawArc(
                            color = Color.White,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(centerX - r * 0.4f, chestTop - r * 0.2f),
                            size = Size(r * 0.8f, r * 0.4f),
                            style = Stroke(width = 4.dp.toPx())
                        )
                        drawCircle(Color.White, radius = 10.dp.toPx(), center = Offset(centerX, chestTop + chestHeight * 0.45f))
                        drawCircle(Color(0xFF1976D2), radius = 6.dp.toPx(), center = Offset(centerX, chestTop + chestHeight * 0.45f))
                    }
                    "PAJAMAS" -> {
                        drawRect(
                            color = Color(0xFF1A237E),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawArc(
                            color = Color(0xFF3949AB),
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(centerX - r * 0.45f, chestTop - r * 0.2f),
                            size = Size(r * 0.9f, r * 0.4f),
                            style = Stroke(width = 5.dp.toPx())
                        )
                        for (i in -1..1 step 2) {
                            drawCircle(Color(0xFFFFD54F), radius = 6.dp.toPx(), center = Offset(centerX + i * (r * 0.45f), chestTop + chestHeight * 0.35f))
                            drawCircle(Color(0xFFFFD54F), radius = 4.dp.toPx(), center = Offset(centerX + i * (r * 0.25f), chestTop + chestHeight * 0.65f))
                        }
                    }
                    "RAINCOAT" -> {
                        drawRect(
                            color = Color(0xFFFFD600),
                            topLeft = Offset(chestLeft, chestTop - 4.dp.toPx()),
                            size = Size(chestWidth, chestHeight + 4.dp.toPx())
                        )
                        drawRect(
                            color = Color(0xFFFBC02D),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, 8.dp.toPx())
                        )
                        drawCircle(Color(0xFF0288D1), radius = 5.dp.toPx(), center = Offset(centerX, chestTop + chestHeight * 0.3f))
                        drawCircle(Color(0xFF0288D1), radius = 5.dp.toPx(), center = Offset(centerX, chestTop + chestHeight * 0.65f))
                        drawRoundRect(
                            color = Color(0xFFF57F17),
                            topLeft = Offset(centerX - r * 0.65f, chestTop + chestHeight * 0.45f),
                            size = Size(r * 0.35f, r * 0.25f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color(0xFFF57F17),
                            topLeft = Offset(centerX + r * 0.3f, chestTop + chestHeight * 0.45f),
                            size = Size(r * 0.35f, r * 0.25f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                    "HOODIE" -> {
                        drawRect(
                            color = Color(0xFFEC407A),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(centerX - r * 0.5f, chestTop + chestHeight * 0.35f),
                            size = Size(r * 1.0f, chestHeight * 0.45f),
                            cornerRadius = CornerRadius(12.dp.toPx())
                        )
                        drawLine(Color.White, Offset(centerX - r * 0.2f, chestTop), Offset(centerX - r * 0.2f, chestTop + chestHeight * 0.3f), strokeWidth = 3.dp.toPx())
                        drawLine(Color.White, Offset(centerX + r * 0.2f, chestTop), Offset(centerX + r * 0.2f, chestTop + chestHeight * 0.3f), strokeWidth = 3.dp.toPx())
                    }
                    "DETECTIVE" -> {
                        drawRect(
                            color = Color(0xFF8D6E63),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRect(
                            color = Color(0xFF4E342E),
                            topLeft = Offset(chestLeft, chestTop + chestHeight * 0.45f),
                            size = Size(chestWidth, 12.dp.toPx())
                        )
                        drawRect(
                            color = Color(0xFFFFD54F),
                            topLeft = Offset(centerX - 10.dp.toPx(), chestTop + chestHeight * 0.45f - 2.dp.toPx()),
                            size = Size(20.dp.toPx(), 16.dp.toPx())
                        )
                        val lapel = Path().apply {
                            moveTo(centerX - r * 0.3f, chestTop)
                            lineTo(centerX + r * 0.3f, chestTop)
                            lineTo(centerX, chestTop + chestHeight * 0.45f)
                            close()
                        }
                        drawPath(lapel, Color(0xFF6D4C41))
                    }
                    "SUIT" -> {
                        drawRect(
                            color = Color(0xFF212121),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        val shirtPath = Path().apply {
                            moveTo(centerX - r * 0.35f, chestTop)
                            lineTo(centerX + r * 0.35f, chestTop)
                            lineTo(centerX, chestTop + chestHeight * 0.55f)
                            close()
                        }
                        drawPath(shirtPath, Color.White)
                        val tiePath = Path().apply {
                            moveTo(centerX - 8.dp.toPx(), chestTop + 2.dp.toPx())
                            lineTo(centerX + 8.dp.toPx(), chestTop + 2.dp.toPx())
                            lineTo(centerX + 12.dp.toPx(), chestTop + 14.dp.toPx())
                            lineTo(centerX, chestTop + 8.dp.toPx())
                            lineTo(centerX - 12.dp.toPx(), chestTop + 14.dp.toPx())
                            close()
                        }
                        drawPath(tiePath, Color(0xFFD32F2F))
                        drawCircle(Color(0xFF212121), radius = 3.dp.toPx(), center = Offset(centerX, chestTop + chestHeight * 0.65f))
                    }
                    "KIMONO" -> {
                        drawRect(
                            color = Color(0xFF880E4F),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        val innerFold = Path().apply {
                            moveTo(centerX - r * 0.4f, chestTop)
                            lineTo(centerX + r * 0.4f, chestTop)
                            lineTo(centerX - r * 0.2f, chestTop + chestHeight * 0.45f)
                            lineTo(centerX - r * 0.5f, chestTop + chestHeight * 0.45f)
                            close()
                        }
                        drawPath(innerFold, Color(0xFFF8BBD0))
                        drawRect(
                            color = Color(0xFFFFD54F),
                            topLeft = Offset(chestLeft, chestTop + chestHeight * 0.35f),
                            size = Size(chestWidth, 16.dp.toPx())
                        )
                        drawRect(
                            color = Color(0xFFE040FB),
                            topLeft = Offset(chestLeft, chestTop + chestHeight * 0.35f + 6.dp.toPx()),
                            size = Size(chestWidth, 4.dp.toPx())
                        )
                    }
                    "MAGICIAN" -> {
                        drawRect(
                            color = Color(0xFF4A148C),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRect(
                            color = Color(0xFF00E5FF),
                            topLeft = Offset(centerX - 6.dp.toPx(), chestTop),
                            size = Size(12.dp.toPx(), chestHeight)
                        )
                        drawCircle(Color(0xFFFFD54F), radius = 7.dp.toPx(), center = Offset(centerX - r * 0.45f, chestTop + chestHeight * 0.35f))
                        drawCircle(Color(0xFFFFD54F), radius = 7.dp.toPx(), center = Offset(centerX + r * 0.45f, chestTop + chestHeight * 0.35f))
                    }
                    "SUPERHERO" -> {
                        drawRect(
                            color = Color(0xFF1565C0),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRect(
                            color = Color(0xFFD32F2F),
                            topLeft = Offset(chestLeft, chestTop + chestHeight * 0.5f),
                            size = Size(chestWidth, 12.dp.toPx())
                        )
                        drawRect(
                            color = Color(0xFFFFD54F),
                            topLeft = Offset(centerX - 8.dp.toPx(), chestTop + chestHeight * 0.5f - 2.dp.toPx()),
                            size = Size(16.dp.toPx(), 16.dp.toPx())
                        )
                        val emblem = Path().apply {
                            moveTo(centerX, chestTop + 6.dp.toPx())
                            lineTo(centerX - 18.dp.toPx(), chestTop + 22.dp.toPx())
                            lineTo(centerX, chestTop + 38.dp.toPx())
                            lineTo(centerX + 18.dp.toPx(), chestTop + 22.dp.toPx())
                            close()
                        }
                        drawPath(emblem, Color(0xFFFFD54F))
                    }
                    "ROYAL_ROBE" -> {
                        drawRect(
                            color = Color(0xFFB71C1C),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, 16.dp.toPx())
                        )
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(centerX - 12.dp.toPx(), chestTop),
                            size = Size(24.dp.toPx(), chestHeight)
                        )
                        drawCircle(Color.Black, radius = 2.5f.dp.toPx(), center = Offset(centerX - r * 0.5f, chestTop + 8.dp.toPx()))
                        drawCircle(Color.Black, radius = 2.5f.dp.toPx(), center = Offset(centerX + r * 0.5f, chestTop + 8.dp.toPx()))
                        drawCircle(Color(0xFFFFD54F), radius = 6.dp.toPx(), center = Offset(centerX, chestTop + 16.dp.toPx()))
                    }
                    "ASTRONAUT" -> {
                        drawRect(
                            color = Color(0xFFF5F5F5),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRect(
                            color = Color(0xFF90A4AE),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, 10.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color(0xFF0288D1),
                            topLeft = Offset(centerX - 24.dp.toPx(), chestTop + chestHeight * 0.25f),
                            size = Size(48.dp.toPx(), 28.dp.toPx()),
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )
                        drawCircle(Color(0xFF00E676), radius = 4.dp.toPx(), center = Offset(centerX - 12.dp.toPx(), chestTop + chestHeight * 0.25f + 14.dp.toPx()))
                        drawCircle(Color(0xFFFF1744), radius = 4.dp.toPx(), center = Offset(centerX, chestTop + chestHeight * 0.25f + 14.dp.toPx()))
                        drawCircle(Color(0xFFFFD54F), radius = 4.dp.toPx(), center = Offset(centerX + 12.dp.toPx(), chestTop + chestHeight * 0.25f + 14.dp.toPx()))
                    }
                    "ARMOR" -> {
                        drawRect(
                            color = Color(0xFFFFB300),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawArc(
                            color = Color(0xFFFFD54F),
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(centerX - r * 0.6f, chestTop - r * 0.2f),
                            size = Size(r * 1.2f, r * 0.4f),
                            style = Stroke(width = 6.dp.toPx())
                        )
                        drawCircle(Color(0xFF00E676), radius = 10.dp.toPx(), center = Offset(centerX, chestTop + chestHeight * 0.4f))
                        drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(centerX - 3.dp.toPx(), chestTop + chestHeight * 0.4f - 3.dp.toPx()))
                    }
                    "DINOSAUR" -> {
                        drawRect(
                            color = Color(0xFF43A047),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRoundRect(
                            color = Color(0xFFFFD54F),
                            topLeft = Offset(centerX - r * 0.5f, chestTop + chestHeight * 0.15f),
                            size = Size(r * 1.0f, chestHeight * 0.85f),
                            cornerRadius = CornerRadius(20.dp.toPx())
                        )
                        for (i in 0..2) {
                            val spikePath = Path().apply {
                                moveTo(centerX - r * 0.1f, chestTop + i * 18.dp.toPx())
                                lineTo(centerX + r * 0.1f, chestTop + i * 18.dp.toPx())
                                lineTo(centerX, chestTop + i * 18.dp.toPx() + 10.dp.toPx())
                                close()
                            }
                            drawPath(spikePath, Color(0xFFF57F17))
                        }
                    }
                    "CHEF_APRON" -> {
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRect(
                            color = Color(0xFFD32F2F),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, 10.dp.toPx())
                        )
                        drawLine(Color(0xFFD32F2F), Offset(centerX - r * 0.4f, chestTop), Offset(centerX - r * 0.2f, chestTop + chestHeight * 0.4f), strokeWidth = 4.dp.toPx())
                        drawLine(Color(0xFFD32F2F), Offset(centerX + r * 0.4f, chestTop), Offset(centerX + r * 0.2f, chestTop + chestHeight * 0.4f), strokeWidth = 4.dp.toPx())
                        drawRoundRect(
                            color = Color(0xFFEEEEEE),
                            topLeft = Offset(centerX - r * 0.35f, chestTop + chestHeight * 0.45f),
                            size = Size(r * 0.7f, chestHeight * 0.35f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                    "DOCTOR" -> {
                        drawRect(
                            color = Color(0xFF00ACC1),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth * 0.42f, chestHeight)
                        )
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(centerX + chestWidth * 0.08f, chestTop),
                            size = Size(chestWidth * 0.42f, chestHeight)
                        )
                        drawRect(
                            color = Color(0xFFD32F2F),
                            topLeft = Offset(centerX + r * 0.35f, chestTop + chestHeight * 0.35f),
                            size = Size(12.dp.toPx(), 4.dp.toPx())
                        )
                        drawRect(
                            color = Color(0xFFD32F2F),
                            topLeft = Offset(centerX + r * 0.35f + 4.dp.toPx(), chestTop + chestHeight * 0.35f - 4.dp.toPx()),
                            size = Size(4.dp.toPx(), 12.dp.toPx())
                        )
                    }
                    "PIRATE_COAT" -> {
                        drawRect(
                            color = Color(0xFFB71C1C),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRect(
                            color = Color(0xFFFFD54F),
                            topLeft = Offset(chestLeft, chestTop + chestHeight * 0.2f),
                            size = Size(chestWidth, 6.dp.toPx())
                        )
                        drawRect(
                            color = Color(0xFF212121),
                            topLeft = Offset(chestLeft, chestTop + chestHeight * 0.5f),
                            size = Size(chestWidth, 14.dp.toPx())
                        )
                        drawRect(
                            color = Color(0xFFFFD54F),
                            topLeft = Offset(centerX - 10.dp.toPx(), chestTop + chestHeight * 0.5f - 2.dp.toPx()),
                            size = Size(20.dp.toPx(), 18.dp.toPx())
                        )
                    }
                    "NINJA_SUIT" -> {
                        drawRect(
                            color = Color(0xFF263238),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawLine(Color(0xFFD32F2F), Offset(chestLeft, chestTop + chestHeight * 0.2f), Offset(chestRight, chestTop + chestHeight * 0.6f), strokeWidth = 10.dp.toPx())
                        drawCircle(Color(0xFFCFD8DC), radius = 6.dp.toPx(), center = Offset(centerX + r * 0.4f, chestTop + chestHeight * 0.3f))
                        drawCircle(Color(0xFF263238), radius = 2.dp.toPx(), center = Offset(centerX + r * 0.4f, chestTop + chestHeight * 0.3f))
                    }
                    "FAIRY_DRESS" -> {
                        drawRect(
                            color = Color(0xFFF8BBD0),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRect(
                            color = Color(0xFFA5D6A7),
                            topLeft = Offset(chestLeft, chestTop + chestHeight * 0.35f),
                            size = Size(chestWidth, 12.dp.toPx())
                        )
                        drawCircle(Color.White, radius = 5.dp.toPx(), center = Offset(centerX - r * 0.4f, chestTop + chestHeight * 0.6f))
                        drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(centerX + r * 0.4f, chestTop + chestHeight * 0.7f))
                        drawCircle(Color(0xFFFFF59D), radius = 6.dp.toPx(), center = Offset(centerX, chestTop + chestHeight * 0.35f + 6.dp.toPx()))
                    }
                    "COWBOY_VEST" -> {
                        drawRect(
                            color = Color(0xFFE53935),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRect(
                            color = Color(0xFF6D4C41),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth * 0.35f, chestHeight)
                        )
                        drawRect(
                            color = Color(0xFF6D4C41),
                            topLeft = Offset(centerX + chestWidth * 0.15f, chestTop),
                            size = Size(chestWidth * 0.35f, chestHeight)
                        )
                        drawCircle(Color(0xFFFFD54F), radius = 7.dp.toPx(), center = Offset(centerX - r * 0.45f, chestTop + chestHeight * 0.35f))
                    }
                    "TUXEDO_GOLD" -> {
                        drawRect(
                            color = Color(0xFF121212),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        val shirtPath = Path().apply {
                            moveTo(centerX - r * 0.35f, chestTop)
                            lineTo(centerX + r * 0.35f, chestTop)
                            lineTo(centerX, chestTop + chestHeight * 0.65f)
                            close()
                        }
                        drawPath(shirtPath, Color.White)
                        val bowtie = Path().apply {
                            moveTo(centerX - 10.dp.toPx(), chestTop + 4.dp.toPx())
                            lineTo(centerX + 10.dp.toPx(), chestTop + 14.dp.toPx())
                            lineTo(centerX + 10.dp.toPx(), chestTop + 4.dp.toPx())
                            lineTo(centerX - 10.dp.toPx(), chestTop + 14.dp.toPx())
                            close()
                        }
                        drawPath(bowtie, Color(0xFFFFD54F))
                        drawLine(Color(0xFFFFD54F), Offset(centerX - r * 0.35f, chestTop), Offset(centerX, chestTop + chestHeight * 0.65f), strokeWidth = 3.dp.toPx())
                        drawLine(Color(0xFFFFD54F), Offset(centerX + r * 0.35f, chestTop), Offset(centerX, chestTop + chestHeight * 0.65f), strokeWidth = 3.dp.toPx())
                    }
                    "HAWAIIAN" -> {
                        drawRect(
                            color = Color(0xFF00ACC1),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        for (i in -1..1) {
                            drawCircle(Color(0xFFFF7043), radius = 8.dp.toPx(), center = Offset(centerX + i * r * 0.5f, chestTop + chestHeight * 0.3f))
                            drawCircle(Color(0xFFFFD54F), radius = 3.dp.toPx(), center = Offset(centerX + i * r * 0.5f, chestTop + chestHeight * 0.3f))
                            drawCircle(Color(0xFFFF7043), radius = 7.dp.toPx(), center = Offset(centerX + i * r * 0.3f, chestTop + chestHeight * 0.7f))
                        }
                    }
                    "GOTHIC" -> {
                        drawRect(
                            color = Color(0xFF4A148C),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRect(
                            color = Color(0xFF1A1A1A),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth * 0.3f, chestHeight)
                        )
                        drawRect(
                            color = Color(0xFF1A1A1A),
                            topLeft = Offset(centerX + chestWidth * 0.2f, chestTop),
                            size = Size(chestWidth * 0.3f, chestHeight)
                        )
                        drawCircle(Color(0xFFE040FB), radius = 5.dp.toPx(), center = Offset(centerX, chestTop + chestHeight * 0.3f))
                    }
                    "SAMURAI_ARMOR" -> {
                        drawRect(
                            color = Color(0xFFC62828),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        for (i in 1..3) {
                            drawRect(
                                color = Color(0xFFFFD54F),
                                topLeft = Offset(chestLeft, chestTop + i * chestHeight * 0.22f),
                                size = Size(chestWidth, 4.dp.toPx())
                            )
                        }
                        drawCircle(Color(0xFF212121), radius = 8.dp.toPx(), center = Offset(centerX, chestTop + chestHeight * 0.45f))
                    }
                    "PHARAOH_ROBE" -> {
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawArc(
                            color = Color(0xFFFFD54F),
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(centerX - r * 0.8f, chestTop - r * 0.2f),
                            size = Size(r * 1.6f, r * 0.6f),
                            style = Stroke(width = 10.dp.toPx())
                        )
                        drawArc(
                            color = Color(0xFF00E5FF),
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(centerX - r * 0.7f, chestTop - r * 0.15f),
                            size = Size(r * 1.4f, r * 0.5f),
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }
                    "ROBOT_SHELL" -> {
                        drawRect(
                            color = Color(0xFF78909C),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawLine(Color(0xFF37474F), Offset(centerX, chestTop), Offset(centerX, chestBottom), strokeWidth = 3.dp.toPx())
                        drawCircle(Color(0xFF00E5FF), radius = 12.dp.toPx(), center = Offset(centerX, chestTop + chestHeight * 0.4f))
                        drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(centerX, chestTop + chestHeight * 0.4f))
                    }
                    "CYBER_JACKET" -> {
                        drawRect(
                            color = Color(0xFF1A1A2E),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawLine(Color(0xFF00FFFF), Offset(centerX - r * 0.3f, chestTop), Offset(centerX, chestBottom), strokeWidth = 5.dp.toPx())
                        drawRect(
                            color = Color(0xFFFF007F),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, 8.dp.toPx())
                        )
                    }
                    "KNIGHT_SILVER" -> {
                        drawRect(
                            color = Color(0xFFCFD8DC),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRect(
                            color = Color(0xFF1565C0),
                            topLeft = Offset(centerX - r * 0.4f, chestTop),
                            size = Size(r * 0.8f, chestHeight)
                        )
                        drawRect(
                            color = Color(0xFFFFD54F),
                            topLeft = Offset(centerX - r * 0.4f, chestTop),
                            size = Size(r * 0.8f, chestHeight),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                    "MERMAID" -> {
                        drawRect(
                            color = Color(0xFF00BFA5),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawCircle(Color(0xFFE040FB), radius = 10.dp.toPx(), center = Offset(centerX - r * 0.35f, chestTop + chestHeight * 0.3f))
                        drawCircle(Color(0xFFE040FB), radius = 10.dp.toPx(), center = Offset(centerX + r * 0.35f, chestTop + chestHeight * 0.3f))
                        for (i in -2..2) {
                            drawCircle(Color.White, radius = 3.dp.toPx(), center = Offset(centerX + i * 10.dp.toPx(), chestTop + 8.dp.toPx()))
                        }
                    }
                    "VIKING_PELT" -> {
                        drawRect(
                            color = Color(0xFF5D4037),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRect(
                            color = Color(0xFF3E2723),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight * 0.35f)
                        )
                        drawLine(Color(0xFF212121), Offset(chestLeft, chestTop), Offset(chestRight, chestBottom), strokeWidth = 8.dp.toPx())
                        drawCircle(Color(0xFFFFB300), radius = 7.dp.toPx(), center = Offset(centerX, chestTop + chestHeight * 0.4f))
                    }
                    "CHRISTMAS" -> {
                        drawRect(
                            color = Color(0xFFD32F2F),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(chestLeft, chestTop + chestHeight * 0.25f),
                            size = Size(chestWidth, 10.dp.toPx())
                        )
                        drawRect(
                            color = Color(0xFF2E7D32),
                            topLeft = Offset(chestLeft, chestTop + chestHeight * 0.6f),
                            size = Size(chestWidth, 10.dp.toPx())
                        )
                    }
                    "HALLOWEEN" -> {
                        drawRect(
                            color = Color(0xFFEF6C00),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRect(
                            color = Color(0xFF2E7D32),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, 10.dp.toPx())
                        )
                        val eye1 = Path().apply {
                            moveTo(centerX - r * 0.4f, chestTop + chestHeight * 0.3f)
                            lineTo(centerX - r * 0.2f, chestTop + chestHeight * 0.3f)
                            lineTo(centerX - r * 0.3f, chestTop + chestHeight * 0.2f)
                            close()
                        }
                        drawPath(eye1, Color(0xFF212121))
                        val eye2 = Path().apply {
                            moveTo(centerX + r * 0.2f, chestTop + chestHeight * 0.3f)
                            lineTo(centerX + r * 0.4f, chestTop + chestHeight * 0.3f)
                            lineTo(centerX + r * 0.3f, chestTop + chestHeight * 0.2f)
                            close()
                        }
                        drawPath(eye2, Color(0xFF212121))
                    }
                    "OVERALLS" -> {
                        drawRect(
                            color = Color(0xFFFFD54F),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRect(
                            color = Color(0xFF1565C0),
                            topLeft = Offset(centerX - r * 0.5f, chestTop + chestHeight * 0.25f),
                            size = Size(r * 1.0f, chestHeight * 0.75f)
                        )
                        drawLine(Color(0xFF1565C0), Offset(centerX - r * 0.4f, chestTop), Offset(centerX - r * 0.4f, chestTop + chestHeight * 0.25f), strokeWidth = 6.dp.toPx())
                        drawLine(Color(0xFF1565C0), Offset(centerX + r * 0.4f, chestTop), Offset(centerX + r * 0.4f, chestTop + chestHeight * 0.25f), strokeWidth = 6.dp.toPx())
                        drawCircle(Color(0xFFFFB300), radius = 4.dp.toPx(), center = Offset(centerX - r * 0.4f, chestTop + chestHeight * 0.28f))
                        drawCircle(Color(0xFFFFB300), radius = 4.dp.toPx(), center = Offset(centerX + r * 0.4f, chestTop + chestHeight * 0.28f))
                    }
                    "BALLET" -> {
                        drawRect(
                            color = Color(0xFFF48FB1),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRect(
                            color = Color(0xFFAD1457),
                            topLeft = Offset(chestLeft, chestTop + chestHeight * 0.45f),
                            size = Size(chestWidth, 8.dp.toPx())
                        )
                    }
                    "ROCKSTAR" -> {
                        drawRect(
                            color = Color(0xFF212121),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(centerX - r * 0.35f, chestTop),
                            size = Size(r * 0.7f, chestHeight)
                        )
                        val bolt = Path().apply {
                            moveTo(centerX + 4.dp.toPx(), chestTop + chestHeight * 0.3f)
                            lineTo(centerX - 6.dp.toPx(), chestTop + chestHeight * 0.5f)
                            lineTo(centerX, chestTop + chestHeight * 0.5f)
                            lineTo(centerX - 4.dp.toPx(), chestTop + chestHeight * 0.7f)
                            lineTo(centerX + 6.dp.toPx(), chestTop + chestHeight * 0.48f)
                            lineTo(centerX, chestTop + chestHeight * 0.48f)
                            close()
                        }
                        drawPath(bolt, Color(0xFFFFD600))
                    }
                    "WIZARD_STAR" -> {
                        drawRect(
                            color = Color(0xFF311B92),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        for (i in -1..1) {
                            drawCircle(Color(0xFFFFD54F), radius = 5.dp.toPx(), center = Offset(centerX + i * r * 0.5f, chestTop + chestHeight * 0.35f))
                            drawCircle(Color(0xFFFFD54F), radius = 4.dp.toPx(), center = Offset(centerX + i * r * 0.25f, chestTop + chestHeight * 0.65f))
                        }
                    }
                    "GOLDEN_KING" -> {
                        drawRect(
                            color = Color(0xFFFFD54F),
                            topLeft = Offset(chestLeft, chestTop),
                            size = Size(chestWidth, chestHeight)
                        )
                        drawLine(Color(0xFFB71C1C), Offset(chestLeft, chestTop), Offset(chestRight, chestBottom), strokeWidth = 14.dp.toPx())
                        drawCircle(Color(0xFFFF1744), radius = 8.dp.toPx(), center = Offset(centerX, chestTop + chestHeight * 0.45f))
                        drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(centerX - 2.dp.toPx(), chestTop + chestHeight * 0.45f - 2.dp.toPx()))
                    }
                }
            }
        }

        // 6. Whiskers for kitty
        if (type == "KITTY") {
            // Left whiskers
            drawLine(Color(0xFF424242), Offset(centerX - r * 0.7f, centerY - 5.dp.toPx()), Offset(centerX - r * 1.2f, centerY - 10.dp.toPx()), strokeWidth = 3.dp.toPx())
            drawLine(Color(0xFF424242), Offset(centerX - r * 0.7f, centerY + 5.dp.toPx()), Offset(centerX - r * 1.3f, centerY + 7.dp.toPx()), strokeWidth = 3.dp.toPx())
            // Right whiskers
            drawLine(Color(0xFF424242), Offset(centerX + r * 0.7f, centerY - 5.dp.toPx()), Offset(centerX + r * 1.2f, centerY - 10.dp.toPx()), strokeWidth = 3.dp.toPx())
            drawLine(Color(0xFF424242), Offset(centerX + r * 0.7f, centerY + 5.dp.toPx()), Offset(centerX + r * 1.3f, centerY + 7.dp.toPx()), strokeWidth = 3.dp.toPx())
        }

        // 7. Draw Eyes based on condition
        val eyeRadius = 10.5f.dp.toPx()
        val leftEyeX = centerX - r * 0.45f
        val rightEyeX = centerX + r * 0.45f
        val eyeY = centerY - r * 0.2f

        val isSad = happiness < 40f
        val isEcstatic = happiness >= 80f && !isSleeping

        if (isSleeping) {
            // Closed happy/relaxed sleeping eyes: u  u
            drawArc(
                color = Color(0xFF333333),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(leftEyeX - 10.dp.toPx(), eyeY - 5.dp.toPx()),
                size = Size(20.dp.toPx(), 14.dp.toPx()),
                style = Stroke(width = 4.dp.toPx())
            )
            drawArc(
                color = Color(0xFF333333),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(rightEyeX - 10.dp.toPx(), eyeY - 5.dp.toPx()),
                size = Size(20.dp.toPx(), 14.dp.toPx()),
                style = Stroke(width = 4.dp.toPx())
            )
        } else if (isSad) {
            // Sad downward curving eyes
            drawArc(
                color = Color(0xFF333333),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(leftEyeX - 12.dp.toPx(), eyeY - 5.dp.toPx()),
                size = Size(22.dp.toPx(), 14.dp.toPx()),
                style = Stroke(width = 4.dp.toPx())
            )
            drawArc(
                color = Color(0xFF333333),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(rightEyeX - 12.dp.toPx(), eyeY - 5.dp.toPx()),
                size = Size(22.dp.toPx(), 14.dp.toPx()),
                style = Stroke(width = 4.dp.toPx())
            )
        } else if (isEcstatic) {
            // Happy happy squinting eyes: ^  ^
            val strokeW = 5.dp.toPx()
            // Left eye path ^
            val leftEyePath = Path().apply {
                moveTo(leftEyeX - 12.dp.toPx(), eyeY + 6.dp.toPx())
                lineTo(leftEyeX, eyeY - 8.dp.toPx())
                lineTo(leftEyeX + 12.dp.toPx(), eyeY + 6.dp.toPx())
            }
            drawPath(leftEyePath, Color(0xFF1E1E1E), style = Stroke(width = strokeW))

            // Right eye path ^
            val rightEyePath = Path().apply {
                moveTo(rightEyeX - 12.dp.toPx(), eyeY + 6.dp.toPx())
                lineTo(rightEyeX, eyeY - 8.dp.toPx())
                lineTo(rightEyeX + 12.dp.toPx(), eyeY + 6.dp.toPx())
            }
            drawPath(rightEyePath, Color(0xFF1E1E1E), style = Stroke(width = strokeW))
        } else {
            // High-Detail Anime Glass Eyes!
            // 1. Base dark outline & dark iris
            drawCircle(Color(0xFF121212), radius = eyeRadius, center = Offset(leftEyeX, eyeY))
            drawCircle(Color(0xFF121212), radius = eyeRadius, center = Offset(rightEyeX, eyeY))
            
            // 2. Bottom colored iris highlight crescent (Golden amber or deep aqua glow)
            val irisGlowColor = when (type) {
                "DRACO" -> Color(0xFFFFB300)
                "SLIME" -> Color(0xFF40C4FF)
                "AXOLOTL" -> Color(0xFFFF4081)
                "KITTY" -> Color(0xFFE040FB)
                else -> Color(0xFF8D6E63)
            }
            drawArc(
                color = irisGlowColor,
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(leftEyeX - eyeRadius * 0.85f, eyeY - eyeRadius * 0.85f),
                size = Size(eyeRadius * 1.7f, eyeRadius * 1.7f),
                style = Stroke(width = 3.5f.dp.toPx())
            )
            drawArc(
                color = irisGlowColor,
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(rightEyeX - eyeRadius * 0.85f, eyeY - eyeRadius * 0.85f),
                size = Size(eyeRadius * 1.7f, eyeRadius * 1.7f),
                style = Stroke(width = 3.5f.dp.toPx())
            )

            // 3. Triple Crystal White Catchlights
            // Main top-left primary shine
            drawCircle(Color.White, radius = eyeRadius * 0.38f, center = Offset(leftEyeX - 3.5f.dp.toPx(), eyeY - 3.5f.dp.toPx()))
            drawCircle(Color.White, radius = eyeRadius * 0.38f, center = Offset(rightEyeX - 3.5f.dp.toPx(), eyeY - 3.5f.dp.toPx()))
            // Bottom-right secondary shine
            drawCircle(Color.White, radius = eyeRadius * 0.18f, center = Offset(leftEyeX + 4.dp.toPx(), eyeY + 4.dp.toPx()))
            drawCircle(Color.White, radius = eyeRadius * 0.18f, center = Offset(rightEyeX + 4.dp.toPx(), eyeY + 4.dp.toPx()))
            // Tiny sparkle dot accent
            drawCircle(Color.White.copy(alpha = 0.8f), radius = eyeRadius * 0.12f, center = Offset(leftEyeX - 4.dp.toPx(), eyeY + 2.dp.toPx()))
            drawCircle(Color.White.copy(alpha = 0.8f), radius = eyeRadius * 0.12f, center = Offset(rightEyeX - 4.dp.toPx(), eyeY + 2.dp.toPx()))
        }

        // 8. Cheeks Blush & Cute Diagonal Blush Lines
        if (!isSleeping && happiness >= 50f) {
            val leftCheekX = leftEyeX - 15.dp.toPx()
            val rightCheekX = rightEyeX + 15.dp.toPx()
            val cheekY = eyeY + 12.dp.toPx()
            
            // Soft layered blush glow
            drawCircle(Color(0x44FF5252), radius = 12.dp.toPx(), center = Offset(leftCheekX, cheekY))
            drawCircle(Color(0x66FF8A80), radius = 8.dp.toPx(), center = Offset(leftCheekX, cheekY))
            drawCircle(Color(0x44FF5252), radius = 12.dp.toPx(), center = Offset(rightCheekX, cheekY))
            drawCircle(Color(0x66FF8A80), radius = 8.dp.toPx(), center = Offset(rightCheekX, cheekY))

            // Three cute anime blush lines across each cheek
            for (j in -1..1) {
                val dx = j * 4.dp.toPx()
                drawLine(
                    color = Color(0xAAFF1744),
                    start = Offset(leftCheekX + dx - 2.dp.toPx(), cheekY - 3.dp.toPx()),
                    end = Offset(leftCheekX + dx + 2.dp.toPx(), cheekY + 3.dp.toPx()),
                    strokeWidth = 1.5f.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                drawLine(
                    color = Color(0xAAFF1744),
                    start = Offset(rightCheekX + dx - 2.dp.toPx(), cheekY - 3.dp.toPx()),
                    end = Offset(rightCheekX + dx + 2.dp.toPx(), cheekY + 3.dp.toPx()),
                    strokeWidth = 1.5f.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }

        // 9. Draw Nose & Mouth
        val mouthY = centerY + r * 0.05f
        if (isSleeping) {
            // tiny sleeping breath triangle or line
            drawLine(Color(0xFF424242), Offset(centerX - 4.dp.toPx(), mouthY), Offset(centerX + 4.dp.toPx(), mouthY), strokeWidth = 3.dp.toPx())
        } else if (isSad) {
            // inverted smile
            drawArc(
                color = Color(0xFF424242),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(centerX - 10.dp.toPx(), mouthY),
                size = Size(20.dp.toPx(), 12.dp.toPx()),
                style = Stroke(width = 3.5f.dp.toPx())
            )
        } else if (isEcstatic) {
            // cute tongue smiley mouth
            val pathMouth = Path().apply {
                moveTo(centerX - 12.dp.toPx(), mouthY)
                cubicTo(
                    centerX - 6.dp.toPx(), mouthY + 18.dp.toPx(),
                    centerX + 6.dp.toPx(), mouthY + 18.dp.toPx(),
                    centerX + 12.dp.toPx(), mouthY
                )
                close()
            }
            drawPath(pathMouth, Color(0xFFD32F2F)) // open mouth
            drawCircle(Color(0xFFFF8A80), radius = 5.dp.toPx(), center = Offset(centerX, mouthY + 8.dp.toPx())) // pink tongue
        } else {
            // standard kitty smile :3 style or cute puppy nose
            if (type == "SHIBA") {
                // tiny black nose
                val nosePath = Path().apply {
                    moveTo(centerX - 6.dp.toPx(), mouthY - 4.dp.toPx())
                    lineTo(centerX + 6.dp.toPx(), mouthY - 4.dp.toPx())
                    lineTo(centerX, mouthY + 2.dp.toPx())
                    close()
                }
                drawPath(nosePath, Color(0xFF212121))
            } else if (type == "AXOLOTL") {
                // wide cute amphibian smile
                drawArc(
                    color = Color(0xFFAD1457),
                    startAngle = 10f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(centerX - 14.dp.toPx(), mouthY - 2.dp.toPx()),
                    size = Size(28.dp.toPx(), 14.dp.toPx()),
                    style = Stroke(width = 3.5f.dp.toPx())
                )
            }
            if (type != "AXOLOTL") {
                // standard smile curve bottom
                drawArc(
                    color = Color(0xFF424242),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(centerX - 8.dp.toPx(), mouthY),
                    size = Size(16.dp.toPx(), 10.dp.toPx()),
                    style = Stroke(width = 3.5f.dp.toPx())
                )
            }
        }

        // 10. Accessories OVERLAY
        if (equippedHat != null) {
            // Coordinates on top of head
            val hatY = centerY - r * 0.95f
            when (equippedHat) {
                "CROWN" -> {
                    // Golden crown with jewels
                    val crownPath = Path().apply {
                        moveTo(centerX - r * 0.45f, hatY + r * 0.2f)
                        lineTo(centerX - r * 0.5f, hatY - r * 0.3f)
                        lineTo(centerX - r * 0.2f, hatY - r * 0.05f)
                        lineTo(centerX, hatY - r * 0.4f)
                        lineTo(centerX + r * 0.2f, hatY - r * 0.05f)
                        lineTo(centerX + r * 0.5f, hatY - r * 0.3f)
                        lineTo(centerX + r * 0.45f, hatY + r * 0.2f)
                        close()
                    }
                    drawPath(crownPath, Color(0xFFFFD54F)) // gold
                    drawPath(crownPath, Color(0xFFFFA000), style = Stroke(width = 3.dp.toPx())) // dark outline

                    // Jewel circles
                    drawCircle(Color(0xFFE91E63), radius = 5.dp.toPx(), center = Offset(centerX, hatY - r * 0.2f)) // center ruby
                    drawCircle(Color(0xFF0D47A1), radius = 4.dp.toPx(), center = Offset(centerX - r * 0.45f, hatY - r * 0.2f)) // left sapphire
                    drawCircle(Color(0xFF0D47A1), radius = 4.dp.toPx(), center = Offset(centerX + r * 0.45f, hatY - r * 0.2f)) // right sapphire
                }
                "TOP_HAT" -> {
                    // Sleek magician hat
                    // Brim:
                    drawRoundRect(
                        color = Color(0xFF212121),
                        topLeft = Offset(centerX - r * 0.65f, hatY),
                        size = Size(r * 1.3f, 12.dp.toPx()),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                    // Hat body:
                    drawRoundRect(
                        color = Color(0xFF212121),
                        topLeft = Offset(centerX - r * 0.42f, hatY - r * 0.5f),
                        size = Size(r * 0.84f, r * 0.5f),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                    // Red band:
                    drawRect(
                        color = Color(0xFFD32F2F),
                        topLeft = Offset(centerX - r * 0.42f, hatY - 8.dp.toPx()),
                        size = Size(r * 0.84f, 8.dp.toPx())
                    )
                }
                "SUNGLASSES" -> {
                    // Cool circular glasses over the eye level
                    val glassY = eyeY - 2.dp.toPx()
                    val glassR = 20.dp.toPx()
                    // Draw Left Glass
                    drawCircle(Color(0xEE2A2A2A), radius = glassR, center = Offset(leftEyeX, glassY))
                    drawCircle(Color(0xFF757575), radius = glassR, style = Stroke(width = 3.5.dp.toPx()), center = Offset(leftEyeX, glassY))
                    // Draw Right Glass
                    drawCircle(Color(0xEE2A2A2A), radius = glassR, center = Offset(rightEyeX, glassY))
                    drawCircle(Color(0xFF757575), radius = glassR, style = Stroke(width = 3.5.dp.toPx()), center = Offset(rightEyeX, glassY))
                    // Connecting bridge
                    drawLine(Color(0xFF757575), Offset(leftEyeX + glassR, glassY), Offset(rightEyeX - glassR, glassY), strokeWidth = 5.dp.toPx())
                }
                "BOWTIE" -> {
                    // Bowtie under the neck
                    val tieY = centerY + r * 0.65f
                    val leftTie = Path().apply {
                        moveTo(centerX, tieY)
                        lineTo(centerX - 24.dp.toPx(), tieY - 12.dp.toPx())
                        lineTo(centerX - 24.dp.toPx(), tieY + 12.dp.toPx())
                        close()
                    }
                    val rightTie = Path().apply {
                        moveTo(centerX, tieY)
                        lineTo(centerX + 24.dp.toPx(), tieY - 12.dp.toPx())
                        lineTo(centerX + 24.dp.toPx(), tieY + 12.dp.toPx())
                        close()
                    }
                    drawPath(leftTie, Color(0xFFD32F2F))
                    drawPath(rightTie, Color(0xFFD32F2F))
                    drawCircle(Color(0xFFB71C1C), radius = 6.dp.toPx(), center = Offset(centerX, tieY))
                }
                "HALO" -> {
                    // Glowing magical halo above head
                    val haloY = centerY - r * 1.15f
                    drawOval(
                        color = Color(0xFFFFF59D),
                        topLeft = Offset(centerX - r * 0.5f, haloY),
                        size = Size(r, 18.dp.toPx()),
                        style = Stroke(width = 5.dp.toPx())
                    )
                    // glowing halo aura
                    drawOval(
                        color = Color(0x77FFF59D),
                        topLeft = Offset(centerX - r * 0.55f, haloY - 2.dp.toPx()),
                        size = Size(r * 1.1f, 22.dp.toPx()),
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
                "WITCH" -> {
                    // Wide dark purple brim
                    drawOval(
                        color = Color(0xFF311B92),
                        topLeft = Offset(centerX - r * 0.8f, hatY - 5.dp.toPx()),
                        size = Size(r * 1.6f, 20.dp.toPx())
                    )
                    // Pointy cone
                    val witchPath = Path().apply {
                        moveTo(centerX - r * 0.4f, hatY)
                        lineTo(centerX, hatY - r * 0.8f)
                        lineTo(centerX + r * 0.4f, hatY)
                        close()
                    }
                    drawPath(witchPath, Color(0xFF4A148C))
                    // Gold buckle
                    drawRect(
                        color = Color(0xFFFFD54F),
                        topLeft = Offset(centerX - 12.dp.toPx(), hatY - 18.dp.toPx()),
                        size = Size(24.dp.toPx(), 14.dp.toPx())
                    )
                }
                "REINDEER" -> {
                    // Left antler
                    val leftAntler = Path().apply {
                        moveTo(centerX - r * 0.4f, hatY + 10.dp.toPx())
                        lineTo(centerX - r * 0.7f, hatY - r * 0.4f)
                        lineTo(centerX - r * 0.5f, hatY - r * 0.45f)
                        lineTo(centerX - r * 0.35f, hatY - r * 0.1f)
                    }
                    drawPath(leftAntler, Color(0xFF5D4037), style = Stroke(width = 6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                    // Right antler
                    val rightAntler = Path().apply {
                        moveTo(centerX + r * 0.4f, hatY + 10.dp.toPx())
                        lineTo(centerX + r * 0.7f, hatY - r * 0.4f)
                        lineTo(centerX + r * 0.5f, hatY - r * 0.45f)
                        lineTo(centerX + r * 0.35f, hatY - r * 0.1f)
                    }
                    drawPath(rightAntler, Color(0xFF5D4037), style = Stroke(width = 6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                    // Red nose berry
                    drawCircle(Color(0xFFD32F2F), radius = 7.dp.toPx(), center = Offset(centerX, mouthY - 4.dp.toPx()))
                }
                "STRAW_HAT" -> {
                    // Straw brim
                    drawOval(
                        color = Color(0xFFFFF176),
                        topLeft = Offset(centerX - r * 0.85f, hatY - 5.dp.toPx()),
                        size = Size(r * 1.7f, 22.dp.toPx())
                    )
                    // Straw dome
                    drawArc(
                        color = Color(0xFFFDD835),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(centerX - r * 0.5f, hatY - r * 0.45f),
                        size = Size(r, r * 0.55f)
                    )
                    // Red ribbon band
                    drawRect(
                        color = Color(0xFFE53935),
                        topLeft = Offset(centerX - r * 0.5f, hatY - 6.dp.toPx()),
                        size = Size(r, 8.dp.toPx())
                    )
                }
                "DETECTIVE" -> {
                    // Deerstalker checkered brown cap
                    drawRoundRect(
                        color = Color(0xFF8D6E63),
                        topLeft = Offset(centerX - r * 0.5f, hatY - r * 0.35f),
                        size = Size(r, r * 0.45f),
                        cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                    )
                    // Visor
                    drawOval(
                        color = Color(0xFF5D4037),
                        topLeft = Offset(centerX - r * 0.6f, hatY - 2.dp.toPx()),
                        size = Size(r * 1.2f, 16.dp.toPx())
                    )
                }
                "CAP" -> {
                    // Blue sports cap dome
                    drawArc(
                        color = Color(0xFF1976D2),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(centerX - r * 0.52f, hatY - r * 0.45f),
                        size = Size(r * 1.04f, r * 0.55f)
                    )
                    // Forward visor
                    drawRoundRect(
                        color = Color(0xFF0D47A1),
                        topLeft = Offset(centerX - r * 0.6f, hatY - 4.dp.toPx()),
                        size = Size(r * 1.2f, 14.dp.toPx()),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                    // White emblem button
                    drawCircle(Color.White, radius = 5.dp.toPx(), center = Offset(centerX, hatY - r * 0.2f))
                }
                "CAT_EARS" -> {
                    // Pink headband
                    drawArc(
                        color = Color(0xFFEC407A),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(centerX - r * 0.65f, hatY - r * 0.2f),
                        size = Size(r * 1.3f, r * 0.5f),
                        style = Stroke(width = 6.dp.toPx())
                    )
                    // Left ear
                    val lEar = Path().apply {
                        moveTo(centerX - r * 0.5f, hatY - r * 0.1f)
                        lineTo(centerX - r * 0.6f, hatY - r * 0.45f)
                        lineTo(centerX - r * 0.25f, hatY - r * 0.15f)
                        close()
                    }
                    drawPath(lEar, Color(0xFFF48FB1))
                    // Right ear
                    val rEar = Path().apply {
                        moveTo(centerX + r * 0.5f, hatY - r * 0.1f)
                        lineTo(centerX + r * 0.6f, hatY - r * 0.45f)
                        lineTo(centerX + r * 0.25f, hatY - r * 0.15f)
                        close()
                    }
                    drawPath(rEar, Color(0xFFF48FB1))
                }
                "CHEF" -> {
                    // White chef hat band
                    drawRect(
                        color = Color(0xFFFAFAFA),
                        topLeft = Offset(centerX - r * 0.45f, hatY - 12.dp.toPx()),
                        size = Size(r * 0.9f, 16.dp.toPx())
                    )
                    // Puffy cloud dome
                    drawCircle(Color.White, radius = 24.dp.toPx(), center = Offset(centerX - r * 0.25f, hatY - r * 0.35f))
                    drawCircle(Color.White, radius = 28.dp.toPx(), center = Offset(centerX, hatY - r * 0.45f))
                    drawCircle(Color.White, radius = 24.dp.toPx(), center = Offset(centerX + r * 0.25f, hatY - r * 0.35f))
                }
                "HEADPHONES" -> {
                    // Headband arch
                    drawArc(
                        color = Color(0xFF212121),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(centerX - r * 0.75f, hatY - r * 0.25f),
                        size = Size(r * 1.5f, r * 0.6f),
                        style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                    // Left earcup
                    drawRoundRect(
                        color = Color(0xFF303030),
                        topLeft = Offset(centerX - r * 0.82f, centerY - r * 0.4f),
                        size = Size(18.dp.toPx(), 36.dp.toPx()),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                    // Green LED Left
                    drawCircle(Color(0xFF00E676), radius = 5.dp.toPx(), center = Offset(centerX - r * 0.82f + 9.dp.toPx(), centerY - r * 0.4f + 18.dp.toPx()))
                    // Right earcup
                    drawRoundRect(
                        color = Color(0xFF303030),
                        topLeft = Offset(centerX + r * 0.82f - 18.dp.toPx(), centerY - r * 0.4f),
                        size = Size(18.dp.toPx(), 36.dp.toPx()),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                    // Green LED Right
                    drawCircle(Color(0xFF00E676), radius = 5.dp.toPx(), center = Offset(centerX + r * 0.82f - 9.dp.toPx(), centerY - r * 0.4f + 18.dp.toPx()))
                }
                "FLOWER" -> {
                    val fx = centerX + r * 0.45f
                    val fy = hatY + r * 0.1f
                    // Pink blossom petals
                    for (angle in 0..300 step 60) {
                        val rad = Math.toRadians(angle.toDouble())
                        val px = fx + Math.cos(rad).toFloat() * 12.dp.toPx()
                        val py = fy + Math.sin(rad).toFloat() * 12.dp.toPx()
                        drawCircle(Color(0xFFFF80AB), radius = 9.dp.toPx(), center = Offset(px, py))
                    }
                    // Yellow center
                    drawCircle(Color(0xFFFFD54F), radius = 7.dp.toPx(), center = Offset(fx, fy))
                }
                "SCARF" -> {
                    val tieY = centerY + r * 0.62f
                    // Cozy striped scarf loop around neck
                    drawRoundRect(
                        color = Color(0xFFC62828),
                        topLeft = Offset(centerX - r * 0.6f, tieY - 10.dp.toPx()),
                        size = Size(r * 1.2f, 20.dp.toPx()),
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                    )
                    // Scarf hanging end
                    drawRoundRect(
                        color = Color(0xFFB71C1C),
                        topLeft = Offset(centerX + r * 0.2f, tieY),
                        size = Size(24.dp.toPx(), 45.dp.toPx()),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
                "MONOCLE" -> {
                    // Gold monocle circle over right eye
                    val glassR = 18.dp.toPx()
                    drawCircle(Color(0x33FFF59D), radius = glassR, center = Offset(rightEyeX, eyeY))
                    drawCircle(Color(0xFFFFD54F), radius = glassR, style = Stroke(width = 3.dp.toPx()), center = Offset(rightEyeX, eyeY))
                    // Chain down to cheek
                    drawLine(Color(0xFFFFD54F), Offset(rightEyeX + glassR, eyeY), Offset(rightEyeX + glassR + 8.dp.toPx(), eyeY + 25.dp.toPx()), strokeWidth = 2.dp.toPx())
                }
                "MUSTACHE" -> {
                    val my = mouthY - 5.dp.toPx()
                    val stPath = Path().apply {
                        moveTo(centerX, my)
                        cubicTo(centerX - 15.dp.toPx(), my - 6.dp.toPx(), centerX - 25.dp.toPx(), my + 8.dp.toPx(), centerX - 18.dp.toPx(), my + 3.dp.toPx())
                        moveTo(centerX, my)
                        cubicTo(centerX + 15.dp.toPx(), my - 6.dp.toPx(), centerX + 25.dp.toPx(), my + 8.dp.toPx(), centerX + 18.dp.toPx(), my + 3.dp.toPx())
                    }
                    drawPath(stPath, Color(0xFF212121), style = Stroke(width = 4.5f.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                }
                "PARTY" -> {
                    // Colorful cone hat
                    val pPath = Path().apply {
                        moveTo(centerX - r * 0.35f, hatY + 5.dp.toPx())
                        lineTo(centerX + r * 0.15f, hatY - r * 0.65f)
                        lineTo(centerX + r * 0.35f, hatY + 5.dp.toPx())
                        close()
                    }
                    drawPath(pPath, Color(0xFF00BCD4))
                    // Yellow pompom on top
                    drawCircle(Color(0xFFFFEB3B), radius = 9.dp.toPx(), center = Offset(centerX + r * 0.15f, hatY - r * 0.65f))
                }
                "DIAMOND" -> {
                    val tieY = centerY + r * 0.65f
                    // Silver necklace chain
                    drawArc(
                        color = Color(0xFFE0E0E0),
                        startAngle = 20f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(centerX - r * 0.55f, tieY - 15.dp.toPx()),
                        size = Size(r * 1.1f, 30.dp.toPx()),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    // Diamond gem
                    val gemPath = Path().apply {
                        moveTo(centerX, tieY + 22.dp.toPx())
                        lineTo(centerX - 9.dp.toPx(), tieY + 10.dp.toPx())
                        lineTo(centerX, tieY + 4.dp.toPx())
                        lineTo(centerX + 9.dp.toPx(), tieY + 10.dp.toPx())
                        close()
                    }
                    drawPath(gemPath, Color(0xFF00E5FF))
                    drawPath(gemPath, Color.White, style = Stroke(width = 1.5f.dp.toPx()))
                }
                "PIRATE" -> {
                    // Black tricorn pirate hat with gold trim & skull
                    val piratePath = Path().apply {
                        moveTo(centerX - r * 0.75f, hatY + 8.dp.toPx())
                        lineTo(centerX - r * 0.5f, hatY - r * 0.45f)
                        lineTo(centerX + r * 0.5f, hatY - r * 0.45f)
                        lineTo(centerX + r * 0.75f, hatY + 8.dp.toPx())
                        close()
                    }
                    drawPath(piratePath, Color(0xFF1E293B))
                    drawPath(piratePath, Color(0xFFFFD54F), style = Stroke(width = 3.dp.toPx()))
                    drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(centerX, hatY - r * 0.15f))
                }
                "NINJA" -> {
                    // Dark navy headband across forehead with flowing knot
                    drawRect(
                        color = Color(0xFF0F172A),
                        topLeft = Offset(centerX - r * 0.65f, hatY - r * 0.05f),
                        size = Size(r * 1.3f, 16.dp.toPx())
                    )
                    // Silver metal plate
                    drawRoundRect(
                        color = Color(0xFF94A3B8),
                        topLeft = Offset(centerX - r * 0.25f, hatY - r * 0.02f),
                        size = Size(r * 0.5f, 10.dp.toPx()),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                }
                "VIKING" -> {
                    // Metal helmet
                    drawArc(
                        color = Color(0xFF64748B),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(centerX - r * 0.55f, hatY - r * 0.45f),
                        size = Size(r * 1.1f, r * 0.55f)
                    )
                    // Left horn
                    val lHorn = Path().apply {
                        moveTo(centerX - r * 0.5f, hatY - r * 0.1f)
                        lineTo(centerX - r * 0.85f, hatY - r * 0.55f)
                        lineTo(centerX - r * 0.45f, hatY - r * 0.25f)
                    }
                    drawPath(lHorn, Color(0xFFF1F5F9), style = Stroke(width = 7.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                    // Right horn
                    val rHorn = Path().apply {
                        moveTo(centerX + r * 0.5f, hatY - r * 0.1f)
                        lineTo(centerX + r * 0.85f, hatY - r * 0.55f)
                        lineTo(centerX + r * 0.45f, hatY - r * 0.25f)
                    }
                    drawPath(rHorn, Color(0xFFF1F5F9), style = Stroke(width = 7.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                }
                "ANGEL_WINGS" -> {
                    // Glowing white wings floating behind ears
                    drawOval(
                        color = Color.White,
                        topLeft = Offset(centerX - r * 1.1f, centerY - r * 0.4f),
                        size = Size(r * 0.5f, r * 0.8f)
                    )
                    drawOval(
                        color = Color.White,
                        topLeft = Offset(centerX + r * 0.6f, centerY - r * 0.4f),
                        size = Size(r * 0.5f, r * 0.8f)
                    )
                    // Gold halo
                    drawOval(
                        color = Color(0xFFFFD54F),
                        topLeft = Offset(centerX - r * 0.4f, hatY - r * 0.35f),
                        size = Size(r * 0.8f, 14.dp.toPx()),
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
                "DRAGON_WINGS" -> {
                    // Crimson bat/dragon wings
                    val lWing = Path().apply {
                        moveTo(centerX - r * 0.4f, centerY)
                        lineTo(centerX - r * 1.2f, centerY - r * 0.6f)
                        lineTo(centerX - r * 0.9f, centerY + r * 0.2f)
                        close()
                    }
                    val rWing = Path().apply {
                        moveTo(centerX + r * 0.4f, centerY)
                        lineTo(centerX + r * 1.2f, centerY - r * 0.6f)
                        lineTo(centerX + r * 0.9f, centerY + r * 0.2f)
                        close()
                    }
                    drawPath(lWing, Color(0xFFB71C1C))
                    drawPath(rWing, Color(0xFFB71C1C))
                }
                "UNICORN" -> {
                    // Spiraled unicorn horn
                    val horn = Path().apply {
                        moveTo(centerX - r * 0.18f, hatY + 4.dp.toPx())
                        lineTo(centerX, hatY - r * 0.7f)
                        lineTo(centerX + r * 0.18f, hatY + 4.dp.toPx())
                        close()
                    }
                    drawPath(horn, Color(0xFFE040FB))
                    drawPath(horn, Color(0xFFFF80AB), style = Stroke(width = 2.dp.toPx()))
                }
                "SUPERHERO" -> {
                    // Flowing red cape
                    val cape = Path().apply {
                        moveTo(centerX - r * 0.45f, centerY + r * 0.4f)
                        lineTo(centerX - r * 0.85f, centerY + r * 1.3f)
                        lineTo(centerX + r * 0.85f, centerY + r * 1.3f)
                        lineTo(centerX + r * 0.45f, centerY + r * 0.4f)
                        close()
                    }
                    drawPath(cape, Color(0xFFD32F2F))
                    drawCircle(Color(0xFFFFD54F), radius = 8.dp.toPx(), center = Offset(centerX, centerY + r * 0.45f))
                }
                "TIARA" -> {
                    // Sparkly tiara
                    val tiara = Path().apply {
                        moveTo(centerX - r * 0.4f, hatY + 6.dp.toPx())
                        lineTo(centerX - r * 0.25f, hatY - r * 0.25f)
                        lineTo(centerX, hatY - r * 0.45f)
                        lineTo(centerX + r * 0.25f, hatY - r * 0.25f)
                        lineTo(centerX + r * 0.4f, hatY + 6.dp.toPx())
                        close()
                    }
                    drawPath(tiara, Color(0xFFE040FB))
                    drawCircle(Color(0xFF00E5FF), radius = 5.dp.toPx(), center = Offset(centerX, hatY - r * 0.45f))
                }
                "ALIEN" -> {
                    // Green spring antennae
                    drawLine(Color(0xFF00E676), Offset(centerX - r * 0.3f, hatY), Offset(centerX - r * 0.5f, hatY - r * 0.5f), strokeWidth = 4.dp.toPx())
                    drawCircle(Color(0xFF00E676), radius = 8.dp.toPx(), center = Offset(centerX - r * 0.5f, hatY - r * 0.5f))
                    drawLine(Color(0xFF00E676), Offset(centerX + r * 0.3f, hatY), Offset(centerX + r * 0.5f, hatY - r * 0.5f), strokeWidth = 4.dp.toPx())
                    drawCircle(Color(0xFF00E676), radius = 8.dp.toPx(), center = Offset(centerX + r * 0.5f, hatY - r * 0.5f))
                }
                "FLOWER_CROWN" -> {
                    // Hawaiian flower garland crown
                    for (i in -3..3) {
                        val fx = centerX + (i * r * 0.18f)
                        drawCircle(if (i % 2 == 0) Color(0xFFFF4081) else Color(0xFFFFAB40), radius = 9.dp.toPx(), center = Offset(fx, hatY + 4.dp.toPx()))
                        drawCircle(Color(0xFFFFF176), radius = 3.dp.toPx(), center = Offset(fx, hatY + 4.dp.toPx()))
                    }
                }
                "ICE_CROWN" -> {
                    val icePath = Path().apply {
                        moveTo(centerX - r * 0.45f, hatY + r * 0.15f)
                        lineTo(centerX - r * 0.5f, hatY - r * 0.45f)
                        lineTo(centerX - r * 0.2f, hatY - r * 0.1f)
                        lineTo(centerX, hatY - r * 0.55f)
                        lineTo(centerX + r * 0.2f, hatY - r * 0.1f)
                        lineTo(centerX + r * 0.5f, hatY - r * 0.45f)
                        lineTo(centerX + r * 0.45f, hatY + r * 0.15f)
                        close()
                    }
                    drawPath(icePath, Color(0xFF00E5FF).copy(alpha = 0.85f))
                    drawPath(icePath, Color.White, style = Stroke(width = 2.dp.toPx()))
                    drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(centerX, hatY - r * 0.55f))
                }
                "FLAME" -> {
                    for (i in -2..2) {
                        val fx = centerX + i * (r * 0.18f)
                        drawCircle(Color(0xFFFF3D00), radius = 12.dp.toPx(), center = Offset(fx, hatY - 4.dp.toPx()))
                        drawCircle(Color(0xFFFFD54F), radius = 6.dp.toPx(), center = Offset(fx, hatY - 2.dp.toPx()))
                    }
                }
                "SAMURAI" -> {
                    val crest = Path().apply {
                        moveTo(centerX, hatY + 5.dp.toPx())
                        lineTo(centerX - r * 0.6f, hatY - r * 0.5f)
                        lineTo(centerX - r * 0.2f, hatY - r * 0.15f)
                        lineTo(centerX, hatY - r * 0.6f)
                        lineTo(centerX + r * 0.2f, hatY - r * 0.15f)
                        lineTo(centerX + r * 0.6f, hatY - r * 0.5f)
                        close()
                    }
                    drawPath(crest, Color(0xFFB71C1C))
                    drawPath(crest, Color(0xFFFFD54F), style = Stroke(width = 2.5f.dp.toPx()))
                    drawCircle(Color(0xFFFFD54F), radius = 7.dp.toPx(), center = Offset(centerX, hatY))
                }
                "PHARAOH" -> {
                    drawArc(
                        color = Color(0xFF1976D2),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(centerX - r * 0.65f, hatY - r * 0.45f),
                        size = Size(r * 1.3f, r * 0.55f)
                    )
                    for (i in -2..2) {
                        drawRect(
                            color = Color(0xFFFFD54F),
                            topLeft = Offset(centerX + i * 14.dp.toPx() - 4.dp.toPx(), hatY - r * 0.45f),
                            size = Size(8.dp.toPx(), r * 0.35f)
                        )
                    }
                    drawCircle(Color(0xFFFFD54F), radius = 6.dp.toPx(), center = Offset(centerX, hatY - r * 0.2f))
                }
                "GALAXY" -> {
                    drawOval(
                        color = Color(0xFFE040FB).copy(alpha = 0.7f),
                        topLeft = Offset(centerX - r * 0.8f, hatY - 10.dp.toPx()),
                        size = Size(r * 1.6f, 24.dp.toPx()),
                        style = Stroke(width = 4.dp.toPx())
                    )
                    drawCircle(Color(0xFF00E5FF), radius = 6.dp.toPx(), center = Offset(centerX - r * 0.8f, hatY))
                    drawCircle(Color(0xFFFFD54F), radius = 5.dp.toPx(), center = Offset(centerX + r * 0.8f, hatY))
                }
                "RAINBOW" -> {
                    val rRadii = listOf(Color(0xFFFF5252), Color(0xFFFFEB3B), Color(0xFF40C4FF))
                    rRadii.forEachIndexed { idx, col ->
                        drawArc(
                            color = col,
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(centerX - r * 0.6f + idx * 4.dp.toPx(), hatY - r * 0.5f + idx * 4.dp.toPx()),
                            size = Size(r * 1.2f - idx * 8.dp.toPx(), r * 0.6f - idx * 8.dp.toPx()),
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }
                    drawCircle(Color.White, radius = 8.dp.toPx(), center = Offset(centerX - r * 0.6f, hatY - r * 0.2f))
                    drawCircle(Color.White, radius = 8.dp.toPx(), center = Offset(centerX + r * 0.6f, hatY - r * 0.2f))
                }
                "CYBER_VISOR" -> {
                    val vY = eyeY - 4.dp.toPx()
                    drawRoundRect(
                        color = Color(0xFFF50057),
                        topLeft = Offset(centerX - r * 0.65f, vY),
                        size = Size(r * 1.3f, 18.dp.toPx()),
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0xFF00E5FF),
                        topLeft = Offset(centerX - r * 0.55f, vY + 4.dp.toPx()),
                        size = Size(r * 1.1f, 10.dp.toPx()),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                }
                "MAGICAL_AURA" -> {
                    drawCircle(Color(0xFFFFD54F), radius = 7.dp.toPx(), center = Offset(centerX - r * 0.5f, hatY - r * 0.3f))
                    drawCircle(Color(0xFFEA80FC), radius = 8.dp.toPx(), center = Offset(centerX, hatY - r * 0.6f))
                    drawCircle(Color(0xFF00E5FF), radius = 7.dp.toPx(), center = Offset(centerX + r * 0.5f, hatY - r * 0.3f))
                }
                "ROYAL_CAPE" -> {
                    drawArc(
                        color = Color(0xFFFFD54F),
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(centerX - r * 0.7f, hatY - 10.dp.toPx()),
                        size = Size(r * 1.4f, 40.dp.toPx()),
                        style = Stroke(width = 8.dp.toPx())
                    )
                }
                "DIAMOND_RING" -> {
                    drawOval(
                        color = Color(0xFFFFD54F),
                        topLeft = Offset(centerX - r * 0.4f, hatY - r * 0.4f),
                        size = Size(r * 0.8f, 16.dp.toPx()),
                        style = Stroke(width = 4.dp.toPx())
                    )
                    drawCircle(Color(0xFF00E5FF), radius = 7.dp.toPx(), center = Offset(centerX, hatY - r * 0.45f))
                }
                "GOGGLES" -> {
                    drawRect(
                        color = Color(0xFF5D4037),
                        topLeft = Offset(centerX - r * 0.7f, hatY),
                        size = Size(r * 1.4f, 12.dp.toPx())
                    )
                    drawCircle(Color(0xFFCFD8DC), radius = 14.dp.toPx(), center = Offset(leftEyeX, hatY + 6.dp.toPx()))
                    drawCircle(Color(0xFF0288D1), radius = 10.dp.toPx(), center = Offset(leftEyeX, hatY + 6.dp.toPx()))
                    drawCircle(Color(0xFFCFD8DC), radius = 14.dp.toPx(), center = Offset(rightEyeX, hatY + 6.dp.toPx()))
                    drawCircle(Color(0xFF0288D1), radius = 10.dp.toPx(), center = Offset(rightEyeX, hatY + 6.dp.toPx()))
                }
                "PARTY_GLASSES" -> {
                    val gY = eyeY - 6.dp.toPx()
                    drawRoundRect(
                        color = Color(0xFFE040FB),
                        topLeft = Offset(centerX - r * 0.68f, gY),
                        size = Size(r * 1.36f, 22.dp.toPx()),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                    drawRect(Color(0xFF18FFFF), topLeft = Offset(leftEyeX - 10.dp.toPx(), gY + 6.dp.toPx()), size = Size(20.dp.toPx(), 4.dp.toPx()))
                    drawRect(Color(0xFF18FFFF), topLeft = Offset(leftEyeX - 10.dp.toPx(), gY + 14.dp.toPx()), size = Size(20.dp.toPx(), 4.dp.toPx()))
                    drawRect(Color(0xFF18FFFF), topLeft = Offset(rightEyeX - 10.dp.toPx(), gY + 6.dp.toPx()), size = Size(20.dp.toPx(), 4.dp.toPx()))
                    drawRect(Color(0xFF18FFFF), topLeft = Offset(rightEyeX - 10.dp.toPx(), gY + 14.dp.toPx()), size = Size(20.dp.toPx(), 4.dp.toPx()))
                }
                "GOLDEN_BELL" -> {
                    val neckY = centerY + r * 0.55f
                    drawRoundRect(
                        color = Color(0xFFD32F2F),
                        topLeft = Offset(centerX - r * 0.55f, neckY),
                        size = Size(r * 1.1f, 10.dp.toPx()),
                        cornerRadius = CornerRadius(5.dp.toPx())
                    )
                    drawCircle(Color(0xFFFFD54F), radius = 9.dp.toPx(), center = Offset(centerX, neckY + 10.dp.toPx()))
                    drawCircle(Color(0xFF37474F), radius = 3.dp.toPx(), center = Offset(centerX, neckY + 13.dp.toPx()))
                }
                "FLOWER_GARLAND" -> {
                    val neckY = centerY + r * 0.55f
                    for (i in -3..3) {
                        val fx = centerX + i * (r * 0.16f)
                        val fy = neckY + Math.abs(i) * 3.dp.toPx()
                        drawCircle(if (i % 2 == 0) Color(0xFFFF4081) else Color(0xFF00E676), radius = 8.dp.toPx(), center = Offset(fx, fy))
                        drawCircle(Color(0xFFFFD54F), radius = 3.dp.toPx(), center = Offset(fx, fy))
                    }
                }
                "STAR_PIN" -> {
                    drawCircle(Color(0xFFFFD54F), radius = 10.dp.toPx(), center = Offset(centerX - r * 0.55f, hatY))
                    drawCircle(Color(0xFFFFF176), radius = 5.dp.toPx(), center = Offset(centerX - r * 0.55f, hatY))
                }
            }
        }

        // 11. Eco-sparkle decoration for ecstatic pets (> 80% happiness)
        if (isEcstatic) {
            val sparkleTime = (System.currentTimeMillis() / 15) % 360
            val rotationRad = Math.toRadians(sparkleTime.toDouble())
            
            // Draw four little cute 4-point golden star shapes around head
            val sparklePoints = listOf(
                Offset(centerX - r * 0.9f, centerY - r * 0.8f),
                Offset(centerX + r * 0.9f, centerY - r * 0.8f),
                Offset(centerX - r * 1.1f, centerY + r * 0.1f),
                Offset(centerX + r * 1.1f, centerY + r * 0.1f)
            )

            sparklePoints.forEachIndexed { i, pt ->
                val offsetPulse = if (i % 2 == 0) sparkleAlpha * 12f else -sparkleAlpha * 12f
                val sizeVal = 6.dp.toPx() * sparkleAlpha
                val starPath = Path().apply {
                    moveTo(pt.x, pt.y - sizeVal - offsetPulse)
                    quadraticTo(pt.x, pt.y, pt.x + sizeVal, pt.y)
                    quadraticTo(pt.x, pt.y, pt.x, pt.y + sizeVal + offsetPulse)
                    quadraticTo(pt.x, pt.y, pt.x - sizeVal, pt.y)
                    quadraticTo(pt.x, pt.y, pt.x, pt.y - sizeVal - offsetPulse)
                    close()
                }
                drawPath(starPath, Color(0xFFFFF176).copy(alpha = sparkleAlpha))
            }
        }

        // 12. Animated Grooming overlay variety
        if (groomingAction == "SOAP") {
            // Draw floating translucent pastel blue bubbles
            val bubblePositions = listOf(
                Offset(centerX - r * 0.4f, centerY - r * 0.2f + bubbleYOffset),
                Offset(centerX + r * 0.4f, centerY - r * 0.5f - bubbleYOffset),
                Offset(centerX - r * 0.7f, centerY + r * 0.1f - bubbleYOffset * 0.6f),
                Offset(centerX + r * 0.6f, centerY + r * 0.3f + bubbleYOffset * 0.8f),
                Offset(centerX - r * 0.1f, centerY - r * 0.8f + bubbleYOffset * 1.2f),
                Offset(centerX + r * 0.2f, centerY + r * 0.1f - bubbleYOffset)
            )
            bubblePositions.forEach { pos ->
                drawCircle(
                    color = Color(0x66B2EBF2),
                    radius = 16.dp.toPx(),
                    center = pos
                )
                drawCircle(
                    color = Color(0xCCFFFFFF),
                    radius = 16.dp.toPx(),
                    style = Stroke(width = 1.5.dp.toPx()),
                    center = pos
                )
                // bubble shine highlight
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(pos.x - 6.dp.toPx(), pos.y - 6.dp.toPx())
                )
            }
        } else if (groomingAction == "SHOWER") {
            // Draw custom water rinse lines streaming down
            val dropPositionsX = listOf(-r * 0.8f, -r * 0.5f, -r * 0.2f, 0f, r * 0.2f, r * 0.5f, r * 0.8f)
            dropPositionsX.forEach { dx ->
                val startY = centerY - r * 1.4f
                val deltaY = showerYOffset
                drawLine(
                    color = Color(0x994FC3F7),
                    start = Offset(centerX + dx, startY + deltaY),
                    end = Offset(centerX + dx, startY + deltaY + 20.dp.toPx()),
                    strokeWidth = 3.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        } else if (groomingAction == "BRUSH") {
            // Scrub toothbrush and sparkles near the mouth area
            val mouthY = centerY + r * 0.05f
            val mouthCenter = Offset(centerX, mouthY)
            val brushX = mouthCenter.x + brushXOffset
            val brushY = mouthCenter.y

            // Draw bristles
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(brushX - 10.dp.toPx(), brushY - 8.dp.toPx()),
                size = Size(20.dp.toPx(), 8.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
            // Draw handle
            drawRoundRect(
                color = Color(0xFFFF4081),
                topLeft = Offset(brushX + 8.dp.toPx(), brushY - 5.dp.toPx()),
                size = Size(25.dp.toPx(), 4.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // Sparkle stars around mouth
            val sparkleXOffset = if (brushXOffset > 0) 18.dp.toPx() else -18.dp.toPx()
            val sparklePoints = listOf(
                Offset(mouthCenter.x - 22.dp.toPx(), mouthCenter.y - 12.dp.toPx()),
                Offset(mouthCenter.x + 22.dp.toPx(), mouthCenter.y - 12.dp.toPx()),
                Offset(mouthCenter.x + sparkleXOffset, mouthCenter.y + 10.dp.toPx())
            )
            sparklePoints.forEach { pt ->
                drawCircle(
                    color = Color(0xFFFFB74D),
                    radius = 4.dp.toPx(),
                    center = pt
                )
            }
        }

        // 13. SPECIAL INTERACTION STAGE ANIMATION EFFECTS
        if (ia > 0f && !isSleeping) {
            when (stage) {
                1 -> {
                    // Stage 1: Bebé / Cachorro -> Brincos alegres con corazones rosas flotando
                    for (i in 0..3) {
                        val angle = Math.toRadians((i * 90 - 45).toDouble())
                        val dist = r * (0.6f + (1f - ia) * 0.7f)
                        val hx = centerX + Math.cos(angle).toFloat() * dist
                        val hy = (centerY - r * 0.3f) + Math.sin(angle).toFloat() * dist - (1f - ia) * 40.dp.toPx()
                        drawCircle(
                            color = Color(0xFFFF4081).copy(alpha = ia),
                            radius = 12.dp.toPx() * ia,
                            center = Offset(hx, hy)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = ia),
                            radius = 5.dp.toPx() * ia,
                            center = Offset(hx - 2.dp.toPx(), hy - 2.dp.toPx())
                        )
                    }
                }
                2 -> {
                    // Stage 2: Joven / Evolución 1 -> Anillo de estrellas doradas en giro
                    val ringRadius = r * (0.7f + (1f - ia) * 1.3f)
                    for (i in 0 until 8) {
                        val angle = Math.toRadians((i * 45 + (1f - ia) * 180f).toDouble())
                        val sx = centerX + Math.cos(angle).toFloat() * ringRadius
                        val sy = (centerY - r * 0.1f) + Math.sin(angle).toFloat() * ringRadius
                        drawCircle(
                            color = Color(0xFFFFD54F).copy(alpha = ia),
                            radius = 10.dp.toPx() * ia,
                            center = Offset(sx, sy)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = ia),
                            radius = 4.dp.toPx() * ia,
                            center = Offset(sx, sy)
                        )
                    }
                }
                3 -> {
                    // Stage 3: Místico / Evolución 2 -> Círculo rúnico mágico y espiral mística
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFE040FB).copy(alpha = ia * 0.8f), Color(0xFF00E5FF).copy(alpha = ia * 0.4f), Color.Transparent),
                            center = Offset(centerX, centerY + r * 0.65f),
                            radius = r * 1.4f
                        ),
                        topLeft = Offset(centerX - r * 1.4f, centerY + r * 0.5f),
                        size = Size(r * 2.8f, r * 0.35f)
                    )
                    for (i in 0 until 6) {
                        val spiralAngle = Math.toRadians((i * 60 + (1f - ia) * 360f).toDouble())
                        val sDist = r * 0.9f
                        val sx = centerX + Math.cos(spiralAngle).toFloat() * sDist
                        val sy = (centerY + r * 0.4f) - (i * r * 0.2f) - (1f - ia) * 50.dp.toPx()
                        drawCircle(
                            color = if (i % 2 == 0) Color(0xFFEA80FC).copy(alpha = ia) else Color(0xFF18FFFF).copy(alpha = ia),
                            radius = 8.dp.toPx() * ia,
                            center = Offset(sx, sy)
                        )
                    }
                }
                4 -> {
                    // Stage 4: Legendario / Supremo -> Corona auroral y ondas de choque cósmicas
                    for (wave in 1..3) {
                        val waveRadius = r * (0.4f + (1f - ia) * 0.8f * wave)
                        drawCircle(
                            color = if (wave == 1) Color(0xFFFFD700).copy(alpha = ia * 0.7f) else Color(0xFFFF4081).copy(alpha = ia * 0.5f),
                            radius = waveRadius,
                            center = Offset(centerX, centerY - r * 0.1f),
                            style = Stroke(width = 4.dp.toPx() * ia)
                        )
                    }
                    for (i in 0 until 10) {
                        val angle = Math.toRadians((i * 36).toDouble())
                        val dist = r * (0.5f + (1f - ia) * 1.6f)
                        val px = centerX + Math.cos(angle).toFloat() * dist
                        val py = (centerY - r * 0.1f) + Math.sin(angle).toFloat() * dist
                        drawCircle(
                            color = Color(0xFFFFF176).copy(alpha = ia),
                            radius = 7.dp.toPx() * ia,
                            center = Offset(px, py)
                        )
                    }
                }
            }
        }

        if (isSilhouette) {
            drawContext.canvas.restore()

            // Draw mystical mysterious aura & glowing question mark runes over the dark silhouette
            drawCircle(
                color = Color(0xFFAB47BC).copy(alpha = sparkleAlpha * 0.28f),
                radius = r * 1.35f,
                center = Offset(centerX, centerY - r * 0.1f)
            )
            for (i in 0 until 8) {
                val angle = Math.toRadians((i * 45).toDouble())
                val dist = r * (0.95f + sparkleAlpha * 0.18f)
                val sx = centerX + Math.cos(angle).toFloat() * dist
                val sy = (centerY - r * 0.1f) + Math.sin(angle).toFloat() * dist
                drawCircle(
                    color = if (i % 2 == 0) Color(0xFFEA80FC) else Color(0xFF00E5FF),
                    radius = (3 + sparkleAlpha * 2.8f).dp.toPx(),
                    center = Offset(sx, sy)
                )
            }
        }
    }
}
