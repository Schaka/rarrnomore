package com.github.schaka.rarrnomore.servarr.sonarr

import com.fasterxml.jackson.annotation.JsonProperty

data class SonarQueueItem(
    val id: Int,
    val seriesId: Int,
    val episodeId: Int,
    val seasonNumber: Int,
    val downloadClient: String?,
    @JsonProperty("downloadId")
    var hash: String,
    val indexer: String?,
    val outputPath: String?
)