package com.github.schaka.rarrnomore.servarr.sonarr

import com.github.schaka.rarrnomore.hooks.TorrentInfo
import com.github.schaka.rarrnomore.servarr.ServarrService
import com.github.schaka.rarrnomore.servarr.TorrentNotInQueueException
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
@RegisterReflectionForBinding(classes = [SonarrQueueList::class, SonarQueueItem::class])
class SonarrService(

    @Sonarr
    val client: RestTemplate

) : ServarrService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun deleteAndBlacklist(info: TorrentInfo) {
        val queue = client.getForEntity("/queue?includeUnknownSeriesItems=true&pageSize=10000", SonarrQueueList::class.java)
        log.trace("Queue items found: {}", queue.body?.records)
        val itemToDelete = queue.body?.records?.find { it.hash?.lowercase() == info.hash.lowercase() }
            ?: throw TorrentNotInQueueException("Torrent with hash ${info.hash} not found in queue")

        log.info(
            "Found torrent {} (id: {}) at indexer {} with rar files - deleting.",
            info.torrentName, itemToDelete.id, info.indexer
        )
        log.trace("Rar files found in deleted torrent {}: {}", info.torrentName, info.filenames)

        client.delete("/queue/{id}?removeFromClient=true&blocklist=true", itemToDelete.id)
    }
}