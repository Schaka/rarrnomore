package com.github.schaka.rarrnomore.servarr.radarr

import com.fasterxml.jackson.annotation.JsonProperty

data class RadarrQueueItem(
    val id: Int,
    val movieId: Int,
    val downloadClient: String,
    @JsonProperty("downloadId")
    var hash: String,
    val indexer: String?
)