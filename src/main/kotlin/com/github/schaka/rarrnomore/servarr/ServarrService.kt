package com.github.schaka.rarrnomore.servarr

import com.github.schaka.rarrnomore.hooks.TorrentInfo

interface ServarrService {

    @Throws(TorrentNotInQueueException::class)
    fun deleteAndBlacklist(info: TorrentInfo)
}