package com.github.schaka.rarrnomore.torrent.qbit

import com.fasterxml.jackson.annotation.JsonProperty

data class QbitFileResponse(
    val availability: Int,
    val index: Int,
    @JsonProperty("is_seed")
    val isSeeding: Boolean,
    val name: String,
    val priority: Int,
    val progress: Int,
    val size: Int
)