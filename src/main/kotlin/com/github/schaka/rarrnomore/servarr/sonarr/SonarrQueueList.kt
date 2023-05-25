package com.github.schaka.rarrnomore.servarr.sonarr

data class SonarrQueueList(
    val page: Int,
    val pageSize: Int,
    val totalRecords: Int,
    val records: List<SonarQueueItem>
)