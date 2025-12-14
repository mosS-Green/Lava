package com.mossgreen.lava.music

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicService : NotificationListenerService() {

    companion object {
        private val _musicState = MutableStateFlow(MusicState())
        val musicState: StateFlow<MusicState> = _musicState.asStateFlow()
        
        // Very basic way to expose controller actions to UI. 
        // In a real app we'd bind to the service or use a Repository.
        var activeController: MediaController? = null
            private set
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("MusicService", "Listener connected")
        checkActiveSessions()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        checkActiveSessions()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        checkActiveSessions()
    }

    private fun checkActiveSessions() {
        try {
            val component = ComponentName(this, MusicService::class.java)
            val controllers = getActiveNotifications()
                .mapNotNull { 
                    // This is a simplified check. Ideally we use MediaSessionManager 
                    // but NotificationListenerService gives us access to sessions linked to notifs.
                    // Actually, let's use the ActiveSessions API from NotificationListener.
                    null 
                }
            
            // Correct approach: Use getActiveMediaSessions
            // Note: Requires COMPONENT_ENABLED_STATE_ENABLED for the permission to be active
            val mediaSessions = try {
                getActiveMediaSessions(component) 
            } catch (e: SecurityException) {
                // Permission not granted yet
                emptyList()
            }
            
            if (mediaSessions.isNotEmpty()) {
                // Pick the first active one or the one playing
                val controller = mediaSessions.firstOrNull { 
                    val state = it.playbackState?.state
                    state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_BUFFERING
                } ?: mediaSessions.firstOrNull()

                updateController(controller)
            }
        } catch (e: Exception) {
            Log.e("MusicService", "Error checking sessions", e)
        }
    }

    private fun updateController(controller: MediaController?) {
        activeController = controller
        if (controller == null) return

        controller.registerCallback(object : MediaController.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackState?) {
                updateState(controller)
            }
            override fun onMetadataChanged(metadata: MediaMetadata?) {
                updateState(controller)
            }
        })
        updateState(controller)
    }

    private fun updateState(controller: MediaController) {
        val metadata = controller.metadata
        val playbackState = controller.playbackState
        
        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Unknown Title"
        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Unknown Artist"
        val albumArt = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) 
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
        
        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING
        val duration = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        val position = playbackState?.position ?: 0L

        _musicState.value = MusicState(
            title = title,
            artist = artist,
            isPlaying = isPlaying,
            albumArt = albumArt,
            duration = duration,
            position = position
        )
    }
}
