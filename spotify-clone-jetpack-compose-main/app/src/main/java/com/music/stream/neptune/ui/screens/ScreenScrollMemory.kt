package com.music.stream.neptune.ui.screens

data class SavedLazyListPosition(
    val index: Int = 0,
    val offset: Int = 0
)

object ScreenScrollMemory {
    val scrollOffsets: MutableMap<String, Int> = mutableMapOf()
    val lazyListPositions: MutableMap<String, SavedLazyListPosition> = mutableMapOf()
}