package com.music.stream.neptune.data.entity

data class RadioCountryAggModel(
    val name: String = "",
    val count: Int = 0,
    val code: String = ""
)

data class RadioGenreAggModel(
    val value: String = "",
    val count: Int = 0
)
