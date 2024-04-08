package com.github.schaka.rarrnomore.torrent

import com.github.schaka.rarrnomore.torrent.qbit.QBittorrentService
import com.github.schaka.rarrnomore.torrent.rest.TorrentClientProperties
import com.github.schaka.rarrnomore.torrent.transmission.TransmissionService
import org.springframework.stereotype.Component
import java.lang.IllegalStateException

@Component
class TorrentsServiceResolver(
        private val qBittorrentService: QBittorrentService?,
        private val transmissionService: TransmissionService?,
        private val torrentClientProperties: TorrentClientProperties
) {

    fun resolve(): TorrentService {
        return when(torrentClientProperties.type) {
            TorrentClientType.QBITTORRENT -> qBittorrentService ?: throw IllegalStateException("Properties for qbittorrent not set correctly")
            TorrentClientType.TRANSMISSION -> transmissionService ?: throw IllegalStateException("Properties for transmission not set correctly")
        }
    }
}