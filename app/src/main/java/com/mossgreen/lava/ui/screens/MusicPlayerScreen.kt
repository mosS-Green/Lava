package com.mossgreen.lava.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mossgreen.lava.music.MusicState
import com.mossgreen.lava.ui.components.AmbientAura

@Composable
fun MusicPlayerScreen(
    musicState: MusicState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    hasPermission: Boolean
) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        AmbientAura(albumArt = musicState.albumArt)

        if (!hasPermission) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Permission Required",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                }) {
                    Text("Grant Notification Access")
                }
            }
        } else {
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = musicState.title,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Artist
                Text(
                    text = musicState.artist,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 20.sp,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(bottom = 48.dp)
                )

                // Seekbar (Visual Only for now as bidirectional binding with service is complex)
                Slider(
                    value = if (musicState.duration > 0) musicState.position.toFloat() / musicState.duration else 0f,
                    onValueChange = {}, // Read-only for this MVP
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                // Controls
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Prev
                    IconButton(onClick = onPrev) {
                        // Use text or vector icon. Since we don't have vector xmls yet, use text or default icons if available.
                        // I'll create a simple Text toggle for now to avoid resource issues.
                        Text("<", color = Color.White, fontSize = 30.sp)
                    }

                    // Play/Pause
                    IconButton(onClick = onPlayPause, modifier = Modifier.size(64.dp)) {
                        Text(
                           if (musicState.isPlaying) "||" else ">",
                           color = Color.White, 
                           fontSize = 40.sp,
                           fontWeight = FontWeight.Bold
                        )
                    }

                    // Next
                    IconButton(onClick = onNext) {
                        Text(">", color = Color.White, fontSize = 30.sp)
                    }
                }
            }
        }
    }
}
