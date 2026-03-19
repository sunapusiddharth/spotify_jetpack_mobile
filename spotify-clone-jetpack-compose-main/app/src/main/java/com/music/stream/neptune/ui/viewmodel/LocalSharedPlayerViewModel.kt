package com.music.stream.neptune.ui.viewmodel

import androidx.compose.runtime.staticCompositionLocalOf

val LocalSharedPlayerViewModel = staticCompositionLocalOf<PlayerViewModel> {
    error("Shared PlayerViewModel not provided")
}