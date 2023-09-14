package com.muhammedhassaan.composetest

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.STATE_ENDED
import com.google.android.exoplayer2.ui.StyledPlayerView
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun PlayerView(
    modifier: Modifier = Modifier,
    exoPlayer: ExoPlayer,
    videos: List<Video>,
    isFullScreen: Boolean,
    onVideoStop: (Long, Int) -> Unit
) {
    val context = LocalContext.current

    BackHandler(
        enabled = isFullScreen
    ) {
        context.setPortrait()
    }

    Box(modifier = modifier) {

        var shouldShowControls by remember { mutableStateOf(false) }

        var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }

        var playbackState by remember { mutableStateOf(exoPlayer.playbackState) }

        var currentVideoId by rememberSaveable {
            mutableStateOf(videos[0].id.toString())
        }

        var completedTime by rememberSaveable {
            mutableStateOf(videos[0].completedTime)
        }

        var currentTimeSavable by rememberSaveable {
            mutableStateOf(completedTime)
        }

        var currentCompletedTime by rememberSaveable {
            mutableStateOf(currentTimeSavable)
        }

        var videoTimer by rememberSaveable { mutableStateOf(currentTimeSavable) }

        var totalDuration by rememberSaveable { mutableStateOf(0L) }

        var bufferedPercentage by rememberSaveable { mutableStateOf(0) }

        LaunchedEffect(key1 = shouldShowControls) {
            if (shouldShowControls) {
                delay(Constants.PLAYER_CONTROLS_VISIBILITY)
                shouldShowControls = false
            }
        }

        // Create a coroutine scope for managing the timer
        val coroutineScope = rememberCoroutineScope()


        val isVideoCompleted by rememberSaveable() {
            mutableStateOf(videos[currentVideoId.toInt()].isCompleted)

        }
        val retriever = remember {
            MediaMetadataRetriever()
        }
        var framesIntervals by remember(totalDuration) {
            mutableStateOf(totalDuration / 1000 / 20)
        }
        val thumbnailFramesList = remember {
            mutableListOf<Bitmap>()
        }
        val thumbnailIntervals = remember {
            mutableListOf<Long>()
        }


        DisposableEffect(key1 = true) {
            val listener = object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    super.onEvents(player, events)
                    isPlaying = player.isPlaying
                    totalDuration = player.duration

                    coroutineScope.launch {
                        while (true) {
                            videoTimer = player.currentPosition//.coerceAtLeast(0L)
                            currentTimeSavable = videoTimer
                            if (videoTimer >= currentCompletedTime) {
                                currentCompletedTime = videoTimer
                            }
                            delay(1000)
                        }
                    }
                    bufferedPercentage = player.bufferedPercentage
                    playbackState = player.playbackState

                    framesIntervals = totalDuration / 1000 / 20
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    currentVideoId = mediaItem?.mediaId.toString()
                    completedTime = videos[currentVideoId.toInt() - 1].completedTime
                    exoPlayer.seekTo(completedTime)
                }
            }

            exoPlayer.addListener(listener)
            exoPlayer.seekTo(currentTimeSavable)
            /*            retriever.setDataSource(
                            context,
                            Uri.Builder().path(videos[currentVideo.toInt()].videoUrl).build()
                        )*/

            onDispose {
                exoPlayer.removeListener(listener)
                coroutineScope.cancel()
                retriever.release()
                onVideoStop(currentCompletedTime, currentVideoId.toInt())
            }
        }

        LaunchedEffect(key1 = framesIntervals) {
            if (framesIntervals != 0L) {
                for (i in 0 until 20) {
                    val time = i * framesIntervals
                    thumbnailIntervals.add(time)
                    retriever.getFrameAtTime(
                        time * 1000000,
                        MediaMetadataRetriever.OPTION_CLOSEST
                    )?.let { thumbnailFramesList.add(it) }
                }
            }
        }

        VideoPlayer(
            modifier = Modifier.fillMaxSize(),
            exoPlayer = exoPlayer,
            onPlayerClick = {
                shouldShowControls = shouldShowControls.not()
            }
        )

        PlayerControls(
            modifier = Modifier.fillMaxSize(),
            isVisible = { shouldShowControls },
            isPlaying = { isPlaying },
            playbackState = { playbackState },
            totalDuration = { totalDuration },
            bufferedPercentage = { bufferedPercentage },
            isFullScreen = isFullScreen,
            onPrevious = { exoPlayer.seekToPrevious() },
            onNext = { exoPlayer.seekToNext() },
            onReplay = { exoPlayer.seekBack() },
            onForward = {
                if ((exoPlayer.seekForwardIncrement + videoTimer) > currentCompletedTime) {
                    exoPlayer.seekTo(currentCompletedTime)
                } else {

                    exoPlayer.seekForward()
                }
            },
            onPauseToggle = {
                when {
                    exoPlayer.isPlaying -> {
                        exoPlayer.pause()
                    }

                    exoPlayer.isPlaying.not() && playbackState == STATE_ENDED -> {
                        exoPlayer.seekTo(0, 0)
                        exoPlayer.playWhenReady = true
                    }

                    else -> {
                        exoPlayer.play()
                    }
                }
                isPlaying = isPlaying.not()
            },
            onSeekChanged = { position ->
                if (isVideoCompleted) {
                    exoPlayer.seekTo(position.toLong())
                } else if (position < currentCompletedTime) {
                    exoPlayer.seekTo(position.toLong())
                } else {
                    exoPlayer.seekTo(currentCompletedTime)
                }
            },
            videoTimer = { videoTimer },
            isCompleted = { isVideoCompleted },
            currentCompletedTime = currentCompletedTime,
            thumbnailFrameList = thumbnailFramesList,
            thumbnailIntervals = thumbnailIntervals
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun VideoPlayer(
    modifier: Modifier = Modifier,
    exoPlayer: ExoPlayer,
    onPlayerClick: () -> Unit
) {
    val context = LocalContext.current

    Box(modifier = modifier
        .background(Color.DarkGray)
        .pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = { offset ->

                    if (offset.x < size.width / 2) {
                        // Left side of the player: Seek backward
                        exoPlayer.seekTo(exoPlayer.currentPosition - 15000)
                    } else {
                        // Right side of the player: Seek forward
                        exoPlayer.seekTo(exoPlayer.currentPosition + 15000)
                    }
                },
                onTap = {
                    onPlayerClick.invoke()
                }
            )
        }
    ) {
        AndroidView(modifier = modifier, factory = {
            StyledPlayerView(context).apply {
                player = exoPlayer
                useController = false
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        })
    }
}