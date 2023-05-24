package com.github.schaka.rarrnomore.servarr.sonarr

import com.github.schaka.rarrnomore.hooks.TorrentInfo
import com.github.schaka.rarrnomore.servarr.ServarrService
import com.github.schaka.rarrnomore.servarr.TorrentNotInQueueException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class SonarrService(

    @Sonarr
    val client: RestTemplate

) : ServarrService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun deleteAndBlacklist(info: TorrentInfo) {
        val queue = client.getForEntity("/queue?includeUnknownSeriesItems=true", SonarrQueueList::class.java)
        val itemToDelete = queue.body?.records?.find { it -> it.hash.lowercase() == info.hash.lowercase() }
            ?: throw TorrentNotInQueueException("Torrent with hash ${info.hash} not found in queue")

        log.info(
            "Found torrent {} (id: {}) at indexer {} with rar files - deleting.",
            info.torrentName, itemToDelete.id, info.indexer
        )
        log.trace("Rar files found in deleted torrent {}: {}", info.torrentName, info.filenames)

        client.delete("/queue/{id}?removeFromClient=true&blocklist=true", itemToDelete.id)
    }
}