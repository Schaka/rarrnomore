package com.github.schaka.rarrnomore.torrent

import com.github.schaka.rarrnomore.hooks.TorrentInfo
import com.github.schaka.rarrnomore.servarr.ServarrService
import org.springframework.stereotype.Component

@Component
class TorrentManager(
    private val torrentService: TorrentService
) {

    fun rejectOrResumeTorrent(torrentInfo: TorrentInfo, servarrService: ServarrService) {
        val info = torrentService.enrichTorrentInfo(torrentInfo)

        if(torrentContainsRar(info.filenames)) {
            // reject in Sonarr queue and delete
            servarrService.deleteAndBlacklist(info)
            return
        }

        torrentService.resumeTorrent(info.hash)
    }

    /**
     * Checks the entire torrent to see if any rar files are contained.
     * Media torrents needing extraction are terrible and scene releases should not be packed for p2p.
     */
    private fun torrentContainsRar(filenames: List<String>): Boolean {
        return filenames.any { filename -> filename.endsWith(".rar") || filename.endsWith(".r00") }
    }
}