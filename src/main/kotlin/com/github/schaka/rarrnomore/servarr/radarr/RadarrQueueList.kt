package com.github.schaka.rarrnomore.servarr.radarr

data class RadarrQueueList(
    val page: Int,
    val pageSize: Int,
    val totalRecords: Int,
    val records: List<RadarrQueueItem>
)