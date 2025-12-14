package com.mossgreen.lava.ui.components

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.Color as AndroidColor
import kotlin.random.Random

@Composable
fun AmbientAura(
    albumArt: Bitmap?,
    modifier: Modifier = Modifier
) {
    var dominantColor by remember { mutableStateOf(Color.DarkGray) }
    var secondaryColor by remember { mutableStateOf(Color.Gray) }
    var tertiaryColor by remember { mutableStateOf(Color.DarkGray) }

    LaunchedEffect(albumArt) {
        if (albumArt != null) {
            withContext(Dispatchers.Default) {
                val palette = Palette.from(albumArt).generate()
                dominantColor = Color(palette.getDominantColor(AndroidColor.DKGRAY))
                secondaryColor = Color(palette.getMutedColor(AndroidColor.GRAY))
                tertiaryColor = Color(palette.getVibrantColor(AndroidColor.DKGRAY))
            }
        }
    }

    val animatedDominant by animateColorAsState(targetValue = dominantColor, label = "dominant")
    val animatedSecondary by animateColorAsState(targetValue = secondaryColor, label = "secondary")
    val animatedTertiary by animateColorAsState(targetValue = tertiaryColor, label = "tertiary")

    // Animation for moving blobs
    val infiniteTransition = rememberInfiniteTransition(label = "blobs")
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "offset1"
    )
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "offset2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Apply blur on API 31+ for frosted glass effect
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        // Note: Compose's graphicsLayer blur requires RenderEffect which 
                        // is handled internally. We'll use alpha for a soft look instead
                        // as true blur on the canvas itself is complex without shader assets.
                        alpha = 0.95f
                    }
                }
        ) {
            val center = this.center
            val baseRadius = size.minDimension / 2.5f

            // Blob 1 - Dominant color
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedDominant.copy(alpha = 0.5f),
                        animatedDominant.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = center + Offset(offset1, -offset1 * 0.7f),
                    radius = baseRadius
                ),
                center = center + Offset(offset1, -offset1 * 0.7f),
                radius = baseRadius
            )

            // Blob 2 - Secondary color
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedSecondary.copy(alpha = 0.4f),
                        animatedSecondary.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center + Offset(-offset1, offset2),
                    radius = baseRadius * 0.85f
                ),
                center = center + Offset(-offset1, offset2),
                radius = baseRadius * 0.85f
            )

            // Blob 3 - Tertiary/Vibrant color
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedTertiary.copy(alpha = 0.35f),
                        animatedTertiary.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    center = center + Offset(offset2 * 0.5f, offset1 * 0.8f),
                    radius = baseRadius * 0.7f
                ),
                center = center + Offset(offset2 * 0.5f, offset1 * 0.8f),
                radius = baseRadius * 0.7f
            )
        }

        // Grain overlay simulation using random alpha points
        // For a real grain effect, you'd use a pre-generated noise texture asset
        // This is a lightweight approximation
        Canvas(modifier = Modifier.fillMaxSize()) {
            val grainDensity = 2000
            repeat(grainDensity) {
                val x = Random.nextFloat() * size.width
                val y = Random.nextFloat() * size.height
                val alpha = Random.nextFloat() * 0.08f
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = 1f,
                    center = Offset(x, y)
                )
            }
        }
    }
}

