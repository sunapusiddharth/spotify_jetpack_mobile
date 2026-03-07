package com.music.stream.neptune.data.entity

data class SearchResultModel(
    val total: Int = 0,
    val took: Int = 0,
    val cards: List<SearchCardModel> = emptyList(),
    val type: String = ""
)

data class SearchCardModel(
    val play_url: String = "",
    val id: String = "",
    val image: String = "",
    val name: String = "",
    val artist: String = "",
    val type: String = ""
)
