package com.muhammedhassaan.composetest

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.google.android.exoplayer2.MediaItem
import java.util.concurrent.TimeUnit

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun Context.setLandscape() {
    val activity = this.findActivity()
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
}

@SuppressLint("SourceLockedOrientationActivity")
fun Context.setPortrait() {
    val activity = this.findActivity()
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}

fun Modifier.noRippleClickable(enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    clickable(
        indication = null,
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        onClick.invoke()
    }
}

fun Long.formatMinSec(): String {
    return if (this == 0L) {
        "00 : 00"
    } else {
        String.format(
            "%02d : %02d",
            TimeUnit.MILLISECONDS.toMinutes(this),
            TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(this)
            )
        )
    }
}

fun List<Video>.toMediaItems(): List<MediaItem>{
    return this.map {
        MediaItem.Builder()
            .setUri(Uri.parse(it.videoUrl))
            .setMediaId(it.id.toString())
            .build()
    }
}

fun String.toMediaItem(): MediaItem{
    return MediaItem.Builder().setUri(Uri.parse(this)).build()
}

object Constants {
    const val PLAYER_CONTROLS_VISIBILITY = 5 * 1000L //5 seconds
    const val PLAYER_SEEK_BACK_INCREMENT = 15 * 1000L //5 seconds
    const val PLAYER_SEEK_FORWARD_INCREMENT = 15 * 1000L //10 seconds
}