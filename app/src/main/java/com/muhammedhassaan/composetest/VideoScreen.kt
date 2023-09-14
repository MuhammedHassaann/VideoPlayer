package com.muhammedhassaan.composetest

import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.exoplayer2.ExoPlayer

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun ModuleVideosScreen(){


    val videos = listOf(
        Video(
            id = 1,
            videoUrl = "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
            completedTime = 20 * 1000,
            isCompleted = false
        ),
        Video(
            id = 2,
            videoUrl = "https://dsqqu7oxq6o1v.cloudfront.net/motion-array-1657486-54Bme6tor9-high.mp4",
            completedTime = 10 * 1000,
            isCompleted = false
        ),
        Video(
            id = 3,
            videoUrl = "https://dsqqu7oxq6o1v.cloudfront.net/preview-940860-1XnUtHycu9-high.mp4",
            completedTime = 13 * 1000,
            isCompleted = false
        )
    )

    ShowModuleVideos(
        videos = videos
    ){ latestTime, videoId->
        Log.i("TAG", "GameVideosScreen: $latestTime")
        Log.i("TAG", "GameVideosScreen: $videoId")
    }

}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
private fun ShowModuleVideos(
    modifier: Modifier = Modifier,
    videos: List<Video>,
    onVideoStop: (Long, Int) -> Unit
) {

    val context = LocalContext.current

    val videos = remember(videos) {
        videos
    }

    val videosItems = videos.toMediaItems()

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(15000)
            .setSeekForwardIncrementMs(15000)
            .build().apply {
                this.setMediaItems(videosItems)
                this.prepare()
                this.playWhenReady = false
            }
    }

 /*   val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(key1 = Unit) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                if (exoPlayer.isPlaying.not()) {
                    exoPlayer.play()
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                exoPlayer.pause()
                super.onStop(owner)
            }
        }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }*/

    ModuleVideoPlayer(
        modifier = modifier,
        exoPlayer = exoPlayer,
        videos = videos,
        onVideoStop = onVideoStop
    )
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
private fun ModuleVideoPlayer(
    modifier: Modifier,
    exoPlayer: ExoPlayer,
    videos: List<Video>,
    onVideoStop: (Long, Int) -> Unit
) {

    val configuration = LocalConfiguration.current


    when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> {
            PortraitView(
                modifier = modifier,
                exoPlayer = exoPlayer,
                videos = videos,
                onVideoStop = onVideoStop
            )
        }
        else -> {
            LandscapeView(
                exoPlayer = exoPlayer,
                videos = videos,
                onVideoStop = onVideoStop
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
private fun PortraitView(
    modifier: Modifier = Modifier,
    exoPlayer: ExoPlayer,
    videos: List<Video>,
    onVideoStop: (Long, Int) -> Unit
) {

    Column(modifier = Modifier.systemBarsPadding()) {
        PlayerView(
            modifier = modifier
                .height(200.dp)
                .fillMaxWidth(),
            exoPlayer = exoPlayer,
            videos = videos,
            isFullScreen = false,
            onVideoStop = onVideoStop
        )

    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
private fun LandscapeView(
    exoPlayer: ExoPlayer,
    videos: List<Video>,
    onVideoStop: (Long, Int) -> Unit
) {

    Box(modifier = Modifier.fillMaxSize()) {
        PlayerView(
            modifier = Modifier.fillMaxSize(),
            exoPlayer = exoPlayer,
            videos = videos,
            isFullScreen = true,
            onVideoStop = onVideoStop
        )
    }
}