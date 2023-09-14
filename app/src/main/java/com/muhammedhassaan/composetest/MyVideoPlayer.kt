package com.muhammedhassaan.composetest

import android.graphics.Bitmap
import android.os.Build
import android.view.WindowInsetsController
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.exoplayer2.Player.STATE_ENDED

val RUNNING_R_OR_HIGHER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlayerControls(
    modifier: Modifier = Modifier,
    isVisible: () -> Boolean,
    isPlaying: () -> Boolean,
    videoTimer: () -> Long,
    bufferedPercentage: () -> Int,
    playbackState: () -> Int,
    totalDuration: () -> Long,
    isFullScreen: Boolean,
    onPauseToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onReplay: () -> Unit,
    onForward: () -> Unit,
    onSeekChanged: (newValue: Float) -> Unit,
    isCompleted: () -> Boolean,
    currentCompletedTime: Long,
    thumbnailFrameList: List<Bitmap>,
    thumbnailIntervals: List<Long>
) {

    val systemUiController: SystemUiController = rememberSystemUiController()
    handleSystemBars(systemUiController, isFullScreen)


    val visible = remember(isVisible()) { isVisible() }

    val playing = remember(isPlaying()) { isPlaying() }

    val duration = remember(totalDuration()) { totalDuration().coerceAtLeast(0) }

    val timer = remember(videoTimer()) { videoTimer() }

    val buffer = remember(bufferedPercentage()) { bufferedPercentage() }

    val playerState = remember(playbackState()) {
        playbackState()
    }

    val context = LocalContext.current

    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.6f))
        ) {


            val controlButtonModifier: Modifier = remember(isFullScreen) {
                if (isFullScreen) {
                    Modifier
                        .padding(horizontal = 16.dp)
                        .size(36.dp)
                } else {
                    Modifier.size(24.dp)
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                horizontalArrangement = if (isFullScreen) {
                    Arrangement.Center
                } else {
                    Arrangement.SpaceEvenly
                }
            ) {
                IconButton(
                    modifier = controlButtonModifier,
                    onClick = onPrevious
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        painter = painterResource(id = com.google.android.exoplayer2.ui.R.drawable.exo_ic_skip_previous),
                        contentDescription = stringResource(id = R.string.play_previous)
                    )
                }

                IconButton(
                    modifier = controlButtonModifier,
                    onClick = onReplay
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        painter = painterResource(id = R.drawable.ic_gobackward_15),
                        contentDescription = stringResource(id = R.string.rewind_15)
                    )
                }

                IconButton(
                    modifier = controlButtonModifier,
                    onClick = onPauseToggle
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        painter = painterResource(
                            id =
                            when {
                                playing -> {
                                    R.drawable.ic_pause
                                }

                                playing.not() && playerState == STATE_ENDED -> {
                                    R.drawable.ic_replay
                                }

                                else -> {
                                    R.drawable.ic_play
                                }
                            }
                        ),
                        contentDescription = stringResource(id = R.string.toggle_play)
                    )
                }


                IconButton(
                    modifier = controlButtonModifier,
                    onClick = {
                        if (videoTimer() < currentCompletedTime) onForward() else {/*Do Nothing*/}
                    }
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        painter = painterResource(id = R.drawable.ic_goforward_15),
                        contentDescription = stringResource(id = R.string.forward_15),
                        alpha = if (videoTimer() < currentCompletedTime) 1f else 0.5f
                    )
                }

                IconButton(
                    modifier = controlButtonModifier,
                    onClick = {
                        if (isCompleted()) onNext() else { /*Do Nothing*/ }
                    }
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        painter = painterResource(id = com.google.android.exoplayer2.ui.R.drawable.exo_ic_skip_next),
                        contentDescription = stringResource(id = R.string.play_next),
                        alpha = if (isCompleted()) 1f else 0.5f
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = if (isFullScreen) 32.dp else 16.dp)
                    .animateEnterExit(
                        enter = slideInVertically(
                            initialOffsetY = { fullHeight: Int -> fullHeight }
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { fullHeight: Int -> fullHeight }
                        )
                    )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(0.9f)) {
                        Slider(
                            value = buffer.toFloat(),
                            enabled = false,
                            onValueChange = { /*do nothing*/ },
                            valueRange = 0f..100f,
                            colors =
                            SliderDefaults.colors(
                                disabledThumbColor = Color.Transparent,
                                disabledActiveTrackColor = Color(0xFF9E9E9E)
                            )
                        )

                        Slider(
                            value = timer.toFloat(),
                            onValueChange = {
                                onSeekChanged.invoke(it)
                            },
                            valueRange = 0f..duration.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White
                            )
                        )
                    }

                    IconButton(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(24.dp)
                            .weight(0.1f)
                            .animateEnterExit(
                                enter = slideInVertically(
                                    initialOffsetY = { fullHeight: Int -> fullHeight }
                                ),
                                exit = slideOutVertically(
                                    targetOffsetY = { fullHeight: Int -> fullHeight }
                                )
                            ),
                        onClick = {
                            if (isFullScreen.not()) {
                                context.setLandscape()
                            } else {
                                context.setPortrait()
                            }
                        }
                    ) {
                        Image(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            painter = painterResource(
                                id = if (isFullScreen) {
                                    R.drawable.ic_exit_fullscreen
                                } else {
                                    R.drawable.ic_enter_fullscreen
                                }
                            ),
                            contentDescription = stringResource(id = R.string.toggle_full_screen)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .animateEnterExit(
                            enter = slideInVertically(
                                initialOffsetY = { fullHeight: Int -> fullHeight }
                            ),
                            exit = slideOutVertically(
                                targetOffsetY = { fullHeight: Int -> fullHeight }
                            )
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = timer.formatMinSec(),
                        color = Color.White
                    )
                    Text(
                        text = duration.formatMinSec(),
                        color = Color.White
                    )
                }
            }
        }
    }

}

private fun handleSystemBars(systemUiController: SystemUiController, hide: Boolean) {
    if (hide) {
        if (RUNNING_R_OR_HIGHER) {
            systemUiController.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        systemUiController.isSystemBarsVisible = false
    } else {
        systemUiController.isSystemBarsVisible = true
    }
}

@Composable
fun Thumb(
    modifier: Modifier = Modifier,
    currentFrame: Bitmap,
    offset: Dp
) {

    Box(
        modifier = modifier
            .size(50.dp)
            .offset(x = (offset), y = (-40).dp)
            .width(24.dp)
            .wrapContentHeight()
    ) {
        Image(
            modifier = Modifier.align(Alignment.Center),
            bitmap = currentFrame.asImageBitmap(),
            contentDescription = "current frame"
        )
    }
}


@RequiresApi(Build.VERSION_CODES.R)
@Preview(showBackground = true)
@Composable
private fun PreviewPlayerControls() {
    PlayerControls(
        modifier = Modifier.fillMaxSize(),
        isVisible = { true },
        isPlaying = { true },
        videoTimer = { 0L },
        totalDuration = { 0 },
        bufferedPercentage = { 50 },
        isFullScreen = false,
        onForward = {},
        onNext = {},
        onPauseToggle = {},
        onPrevious = {},
        onReplay = {},
        onSeekChanged = {},
        playbackState = { 1 },
        isCompleted = { false },
        currentCompletedTime = 15000,
        thumbnailFrameList = emptyList(),
        thumbnailIntervals = emptyList()
    )
}