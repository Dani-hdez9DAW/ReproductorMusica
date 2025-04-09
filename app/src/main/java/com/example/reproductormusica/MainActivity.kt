package com.example.reproductormusica

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.reproductormusica.ui.theme.ReproductorMusicaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaPlayer = MediaPlayer.create(this, R.raw.abracadabra)

        setContent {
            ReproductorMusicaTheme {
                MusicPlayerScreen(mediaPlayer)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}

@Composable
fun MusicPlayerScreen(mediaPlayer: MediaPlayer) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0) }

    // Update progress every second
    LaunchedEffect(isPlaying) {
        while (isActive && isPlaying) {
            currentPosition = mediaPlayer.currentPosition
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color.Green)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("SONG TITLE", color = Color.White)
        Text("Singer", color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = currentPosition.toFloat(),
            onValueChange = { mediaPlayer.seekTo(it.toInt()) },
            valueRange = 0f..mediaPlayer.duration.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = Color.Green,
                activeTrackColor = Color.Green
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        IconButton(onClick = {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                isPlaying = false
            } else {
                mediaPlayer.start()
                isPlaying = true
            }
        }) {
            Icon(
                painter = painterResource(
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                ),
                contentDescription = "Play/Pause",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
