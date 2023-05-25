package com.github.schaka.rarrnomore.torrent

import com.github.schaka.rarrnomore.hooks.TorrentInfo
import com.github.schaka.rarrnomore.servarr.ServarrService
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

class TorrentQueueItem(
    val torrentInfo: TorrentInfo,
    val servarrService: ServarrService,
    var lastAttempt: LocalDateTime = LocalDateTime.now(),
    val attempts: AtomicInteger = AtomicInteger(0),
    var lastException: Exception? = null
) {
}