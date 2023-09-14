package com.muhammedhassaan.composetest

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.muhammedhassaan.composetest.ui.theme.Typography

class MainActivity : ComponentActivity() {


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            ModuleVideosScreen()
        }
    }


    private fun hideSystemBars() {
        // Configure the behavior of the hidden system bars
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
/*
    private fun showSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
    }*/
}



@Composable
fun ScoreView(
    score: Int,
    scoreCallback: ()->Unit) {
    Column {
        Text(
            text = "Your Score is : $score"
        )
        Button(onClick = { scoreCallback()}) {
            Text(text = "Add points")
        }
    }
}

@Composable
fun MyVideoPlayer(uri: Uri){
    val context = LocalContext.current

    val exoPlayer = remember{
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
        }
    }

    val styledPlayer = StyledPlayerView(context)
    styledPlayer.player = exoPlayer
    
    DisposableEffect(AndroidView(factory = {styledPlayer})){
        exoPlayer.prepare()

        onDispose {
            exoPlayer.release()
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyEditText(){
    Column {
        var text by remember { mutableStateOf("") }
        Text(text = text , style = Typography.bodyMedium)
        TextField(value = "", onValueChange = { text = it})
    }
}

@Preview(showBackground = true)
@Composable
fun ScorePreview() {
    var score by remember { mutableStateOf(0) }
    ScoreView(score) {
        score+=2
    }
}