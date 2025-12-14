package com.mossgreen.lava

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import com.mossgreen.lava.music.MusicService
import com.mossgreen.lava.ui.screens.MusicPlayerScreen
import com.mossgreen.lava.ui.theme.LavaTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            LavaTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val musicState by MusicService.musicState.collectAsState()
    
    // Permission Check
    val hasPermission = remember(context) {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        flat?.contains(packageName) == true
    }

    // AOD / Inactivity Logic
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var isDimmed by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        while(true) {
            val currentTime = System.currentTimeMillis()
            val timeSinceInteraction = currentTime - lastInteractionTime
            
            // Dim after 7 seconds
            if (timeSinceInteraction > 7000 && !isDimmed) {
                isDimmed = true
            }
            
            // Auto close if paused and inactive for 10s
            if (!musicState.isPlaying && timeSinceInteraction > 10000) {
                 // Close app or turn off screen (simulated by finishing activity to let system sleep)
                 (context as? Activity)?.finish()
            }

            delay(1000)
        }
    }
    
    // Reset interaction on touch
    val interactionSource = remember { MutableInteractionSource() }
    
    // Dim Overlay Alpha
    val dimAlpha by animateFloatAsState(targetValue = if (isDimmed) 0.8f else 0f, label = "dim_alpha")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                lastInteractionTime = System.currentTimeMillis()
                isDimmed = false
            }
    ) {
        MusicPlayerScreen(
            musicState = musicState,
            onPlayPause = {
                val controller = MusicService.activeController
                if (musicState.isPlaying) controller?.transportControls?.pause()
                else controller?.transportControls?.play()
                
                lastInteractionTime = System.currentTimeMillis()
                isDimmed = false
            },
            onNext = {
                MusicService.activeController?.transportControls?.skipToNext()
                lastInteractionTime = System.currentTimeMillis()
                isDimmed = false
            },
            onPrev = {
                MusicService.activeController?.transportControls?.skipToPrevious()
                lastInteractionTime = System.currentTimeMillis()
                isDimmed = false
            },
            hasPermission = hasPermission
        )
        
        // Dim Overlay
        if (dimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = dimAlpha))
            )
        }
    }
}
