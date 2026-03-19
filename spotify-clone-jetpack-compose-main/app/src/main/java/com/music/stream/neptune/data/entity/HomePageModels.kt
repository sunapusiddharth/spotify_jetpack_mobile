package com.music.stream.neptune.data.entity

data class HomePageInfoModel(
    val results: List<HomePageSectionModel> = emptyList(),
    val page: Int = 0
)

data class HomePageSectionModel(
    val cardType: String = "",
    val label: String = "",
    val path: String = "",
    val cards: List<HomePageCardModel> = emptyList(),
    val id: String = ""
)

data class HomePageCardModel(
    val image: String = "",
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val type: String = "",
    val path: String = "",
    val song: SongsModel? = null
)