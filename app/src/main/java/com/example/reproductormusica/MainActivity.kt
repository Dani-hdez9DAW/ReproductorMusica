package com.example.reproductormusica

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.reproductormusica.ui.theme.ReproductorMusicaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    private val songs = arrayOf(R.raw.abracadabra, R.raw.meportobonito, R.raw.fifteenminutes,R.raw.esadivaeurovision)
    private val covers = arrayOf(
        R.drawable.abracadabra,
        R.drawable.meportobonito,
        R.drawable.fifteenminutes,
        R.drawable.esadivaeurovision
    )
    private lateinit var initialMediaPlayer: MediaPlayer
    private lateinit var currentMediaPlayer: MutableState<MediaPlayer>
    private lateinit var currentSongIndex: MutableState<Int>
    private val categories = arrayOf("Pop", "Reggaetón", "Electropop","Eurovision")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initialMediaPlayer = MediaPlayer.create(this, songs[0])
        initialMediaPlayer.setOnCompletionListener { playNextSong() }

        currentMediaPlayer = mutableStateOf(initialMediaPlayer)
        currentSongIndex = mutableStateOf(0)

        setContent {
            ReproductorMusicaTheme {
                MusicPlayerScreen(
                    mediaPlayer = currentMediaPlayer.value,
                    currentSongIndex = currentSongIndex.value,
                    coverResId = covers[currentSongIndex.value],
                    onNext = ::playNextSong,
                    onPrevious = ::playPreviousSong,
                    category = categories[currentSongIndex.value]
                )
            }
        }
    }

    private fun playNextSong() {
        try {
            currentMediaPlayer.value.release()
            currentSongIndex.value = (currentSongIndex.value + 1) % songs.size
            val newPlayer = MediaPlayer.create(this, songs[currentSongIndex.value])
            newPlayer.setOnCompletionListener { playNextSong() }
            currentMediaPlayer.value = newPlayer
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playPreviousSong() {
        try {
            currentMediaPlayer.value.release()
            currentSongIndex.value =
                if (currentSongIndex.value - 1 < 0) songs.size - 1 else currentSongIndex.value - 1
            val newPlayer = MediaPlayer.create(this, songs[currentSongIndex.value])
            newPlayer.setOnCompletionListener { playNextSong() }
            currentMediaPlayer.value = newPlayer
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentMediaPlayer.value.release()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(
    mediaPlayer: MediaPlayer,
    currentSongIndex: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    @DrawableRes coverResId: Int,
    category: String
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0) }
    var totalDuration by remember { mutableStateOf(1) }

    // Animaciones al cambiar de canción
    val scale = animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 500),
        label = "ImageScale"
    ).value

    // Actualiza el progreso cada 500ms
    LaunchedEffect(mediaPlayer) {
        while (isActive) {
            currentPosition = try {
                mediaPlayer.currentPosition
            } catch (e: IllegalStateException) {
                0
            }
            delay(500)
        }
    }

    // Actualiza duración total al cambiar canción
    LaunchedEffect(mediaPlayer) {
        totalDuration = try {
            mediaPlayer.duration.takeIf { it > 0 } ?: 1
        } catch (e: IllegalStateException) {
            1
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Music Player", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.DarkGray)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp)) // MÁS espacio superior

            // Portada con animación
            androidx.compose.animation.AnimatedVisibility(visible = true) {
                Image(
                    painter = painterResource(id = coverResId),
                    contentDescription = "Album Cover",
                    modifier = Modifier
                        .size(320.dp * scale)
                        .padding(16.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            }

            // Texto con fade y pequeño slide
            androidx.compose.animation.AnimatedVisibility(visible = true) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Song ${currentSongIndex + 1}", color = Color.White)
                    Text("Artist Name", color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp)) // Espacio entre artista y categoría
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = Color(0xFF1DB954),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clip(MaterialTheme.shapes.large)
                    ) {
                        Text(
                            text = category,
                            color = Color.Black,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                }
            }

            Spacer(modifier = Modifier.height(32.dp)) // Más separación

            // Barra de progreso animada
            AnimatedProgressBar(
                currentPosition = currentPosition,
                totalDuration = totalDuration,
                onSeek = { newValue ->
                    try {
                        currentPosition = newValue
                        mediaPlayer.seekTo(newValue)
                    } catch (e: IllegalStateException) {
                        // Ignorado
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botones de control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ControlButton(
                    iconRes = R.drawable.ic_previous,
                    contentDescription = "Previous",
                    onClick = {
                        try {
                            mediaPlayer.pause()
                        } catch (_: Exception) {
                        }
                        isPlaying = false
                        onPrevious()
                    }
                )
                ControlButton(
                    iconRes = R.drawable.ic_rewind,
                    contentDescription = "Rewind",
                    onClick = {
                        try {
                            val newPos = (mediaPlayer.currentPosition - 10000).coerceAtLeast(0)
                            mediaPlayer.seekTo(newPos)
                        } catch (_: Exception) {
                        }
                    }
                )
                PlayPauseButton(
                    isPlaying = isPlaying,
                    onClick = {
                        try {
                            if (mediaPlayer.isPlaying) {
                                mediaPlayer.pause()
                                isPlaying = false
                            } else {
                                mediaPlayer.start()
                                isPlaying = true
                            }
                        } catch (_: Exception) {
                            isPlaying = false
                        }
                    }
                )
                ControlButton(
                    iconRes = R.drawable.ic_forward,
                    contentDescription = "Forward",
                    onClick = {
                        try {
                            val newPos = (mediaPlayer.currentPosition + 10000)
                                .coerceAtMost(totalDuration)
                            mediaPlayer.seekTo(newPos)
                        } catch (_: Exception) {
                        }
                    }
                )
                ControlButton(
                    iconRes = R.drawable.ic_next,
                    contentDescription = "Next",
                    onClick = {
                        try {
                            mediaPlayer.pause()
                        } catch (_: Exception) {
                        }
                        isPlaying = false
                        onNext()
                    }
                )
            }
        }
    }
}


@Composable
fun ControlButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(40.dp) // ANTES: 32.dp
        )
    }
}

@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.White,
            modifier = Modifier.size(64.dp)
        )
    }
}

@Composable
fun formatTime(milliseconds: Int): String {
    val minutes = (milliseconds / 1000) / 60
    val seconds = (milliseconds / 1000) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
fun AnimatedProgressBar(
    currentPosition: Int,
    totalDuration: Int,
    onSeek: (Int) -> Unit
) {
    val animatedPosition by animateFloatAsState(
        targetValue = currentPosition.toFloat(),
        animationSpec = tween(durationMillis = 300), // Animación suave
        label = "SliderAnimation"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Slider(
            value = animatedPosition,
            onValueChange = { newValue ->
                onSeek(newValue.toInt())
            },
            valueRange = 0f..totalDuration.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp) // Más alto
                .padding(horizontal = 32.dp), // Más padding horizontal
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF1DB954),
                activeTrackColor = Color(0xFF1DB954),
                inactiveTrackColor = Color.DarkGray,
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                formatTime(currentPosition),
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                formatTime(totalDuration),
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
