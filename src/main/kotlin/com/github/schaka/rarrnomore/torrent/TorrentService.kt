package com.github.schaka.rarrnomore.torrent

import com.github.schaka.rarrnomore.hooks.TorrentInfo

interface TorrentService {

    /**
     * Checks the torrent's contents and returns filenames
     */
    @Throws(TorrentHashNotFoundException::class)
    fun enrichTorrentInfo(info: TorrentInfo): TorrentInfo

    fun resumeTorrent(hash: String)
}