package com.github.schaka.rarrnomore.servarr.sonarr

import com.github.schaka.rarrnomore.hooks.TorrentInfo
import com.github.schaka.rarrnomore.servarr.ServarrService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class SonarrService(

    @Sonarr
    val client: RestTemplate

) : ServarrService {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    override fun deleteAndBlacklist(info: TorrentInfo) {
        val queue = client.getForEntity("/queue?includeUnknownSeriesItems=true", SonarrQueueList::class.java)
        val itemToDelete = queue.body?.records?.find { it -> it.hash.lowercase() == info.hash }
            ?: throw IllegalStateException("Torrent with hash ${info.hash} not found in queue")

        log.info("Found torrent {} (id: {}) at indexer {} with rar files - deleting.",
            info.torrentName, itemToDelete.id, itemToDelete.indexer)
        log.trace("Rar files found in deleted torrent {}: {}", info.torrentName, info.filenames)

        client.delete("/queue/{id}?removeFromClient=true&blocklist=true", itemToDelete.id)
    }
}