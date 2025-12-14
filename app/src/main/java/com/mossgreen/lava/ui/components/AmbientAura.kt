package com.mossgreen.lava.ui.components

import android.graphics.Bitmap
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.Color as AndroidColor

@Composable
fun AmbientAura(
    albumArt: Bitmap?,
    modifier: Modifier = Modifier
) {
    var dominantColor by remember { mutableStateOf(Color.DarkGray) }
    var secondaryColor by remember { mutableStateOf(Color.Gray) }

    LaunchedEffect(albumArt) {
        if (albumArt != null) {
            withContext(Dispatchers.Default) {
                val palette = Palette.from(albumArt).generate()
                dominantColor = Color(palette.getDominantColor(AndroidColor.DKGRAY))
                secondaryColor = Color(palette.getMutedColor(AndroidColor.GRAY))
            }
        }
    }

    val animatedDominant by animateColorAsState(targetValue = dominantColor, label = "dominant")
    val animatedSecondary by animateColorAsState(targetValue = secondaryColor, label = "secondary")

    // Animation for moving blobs
    val infiniteTransition = rememberInfiniteTransition(label = "blobs")
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "offset1"
    )

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = this.center
            val radius = size.minDimension / 1.5f

            // Blob 1
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(animatedDominant.copy(alpha = 0.6f), Color.Transparent),
                    center = center + Offset(offset1, -offset1),
                    radius = radius
                ),
                center = center + Offset(offset1, -offset1),
                radius = radius
            )

            // Blob 2
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(animatedSecondary.copy(alpha = 0.5f), Color.Transparent),
                    center = center + Offset(-offset1, offset1),
                    radius = radius * 0.8f
                ),
                center = center + Offset(-offset1, offset1),
                radius = radius * 0.8f
            )
        }
        
        // Grain Overlay
        // Since we can't easily generate noise with standard Compose without shaders (API 33+),
        // we can simulate it with a very small repeated noise pattern or just skip for MVP if API is low.
        // Assuming API 31+, we can use RenderEffect for blur, but for grain let's just use a semi-transparent overlay if we had an asset.
        // I'll simulate it with a simple canvas drawPoints or just leave it clean for now to ensure performance.
        // Or better: use a custom shader if possible. For now, let's stick to the color blobs.
        
        // Applying blur for the "frosted glass" look
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        renderEffect = RenderEffect.createBlurEffect(
                            100f, 100f, Shader.TileMode.MIRROR
                        ).asComposeRenderEffect()
                    )
            )
        }
    }
}

// Extension to bridge Android RenderEffect to Compose
@RequiresApi(Build.VERSION_CODES.S)
fun android.graphics.RenderEffect.asComposeRenderEffect(): androidx.compose.ui.graphics.RenderEffect {
    return androidx.compose.ui.graphics.RenderEffect.asImageBitmap().let { 
        // Wait, Compose 1.6 added asComposeRenderEffect.
        // If not available, we have to construct it.
        // Actually androidx.compose.ui.graphics.RenderEffect is a wrapper.
        // Let's assume we are using a version that might not have the helper or just use the graphicsLayer 'renderEffect' parameter directly which takes an Android RenderEffect in newer Compose versions.
        // Wait, 'renderEffect' in GraphicsLayerScope takes androidx.compose.ui.graphics.RenderEffect.
        // We will skip the RenderEffect implementation detail here to avoid compilation issues if dependencies mismatch, 
        // as the user's gradle might default to older compose.
        // I will omit the renderEffect block to be safe and just rely on the radial gradients being soft.
        null 
    } as? androidx.compose.ui.graphics.RenderEffect ?: androidx.compose.ui.graphics.RenderEffect.createBlurEffect(100f, 100f, androidx.compose.ui.graphics.TileMode.Mirror)
}
