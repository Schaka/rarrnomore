package com.github.schaka.rarrnomore.torrent

import com.github.schaka.rarrnomore.hooks.TorrentInfo
import com.github.schaka.rarrnomore.servarr.ServarrService
import com.github.schaka.rarrnomore.servarr.TorrentNotInQueueException
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TorrentManager(
    private val torrentService: TorrentService,
    private val torrentQueue: MutableList<TorrentQueueItem> = mutableListOf()
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    fun processGrab(torrentInfo: TorrentInfo, servarrService: ServarrService) {
        torrentQueue.add(TorrentQueueItem(torrentInfo, servarrService))
    }

    /**
     * Checks the entire torrent to see if any rar files are contained.
     * Media torrents needing extraction are terrible and scene releases should not be packed for p2p.
     */
    private fun torrentContainsRar(filenames: List<String>): Boolean {
        return filenames.any { filename -> filename.endsWith(".rar") || filename.endsWith(".r00") }
    }

    @Scheduled(fixedDelay = 5000)
    fun processServarrQueue() {
        val itemItr = torrentQueue.iterator()
        while (itemItr.hasNext()) {
            val queueItem = itemItr.next()

            // if 3 attempts have been made, abandon retries
            if (queueItem.attempts.get() >= 3) {
                log.error("Processing torrent ${queueItem.torrentInfo.torrentName} failed", queueItem.lastException)
                itemItr.remove()
            }

            if (!needToRetry(queueItem)) {
                continue
            }

            if (tryToProcess(queueItem)) {
                itemItr.remove()
            }

        }
    }

    private fun needToRetry(deleteItem: TorrentQueueItem): Boolean {
        return (deleteItem.attempts.get() == 0 && deleteItem.lastAttempt.plusMinutes(1).isBefore(LocalDateTime.now()))
                ||
                (deleteItem.attempts.get() > 0 && deleteItem.lastAttempt.plusMinutes(5).isBefore(LocalDateTime.now()))
    }


    /**
     * Retries and reports success
     */
    private fun tryToProcess(queueItem: TorrentQueueItem): Boolean {
        val servarrService = queueItem.servarrService

        try {
            rejectOrResumeTorrent(queueItem.torrentInfo, servarrService)
            return true // no exception, success!
        } catch (e: TorrentNotInQueueException) {
            increment(queueItem, e)
        } catch (e: TorrentHashNotFoundException) {
            increment(queueItem, e)
        } catch (e: Exception) {
            queueItem.lastException = e
            queueItem.attempts.set(3)
        }

        return false
    }

    private fun increment(queueItem: TorrentQueueItem, exception: Exception) {
        queueItem.attempts.addAndGet(1)
        queueItem.lastAttempt = LocalDateTime.now()
        queueItem.lastException = exception
    }

    private fun rejectOrResumeTorrent(torrentInfo: TorrentInfo, servarrService: ServarrService) {
        val info = torrentService.enrichTorrentInfo(torrentInfo)

        if (torrentContainsRar(info.filenames)) {
            // reject in Sonarr queue and delete
            servarrService.deleteAndBlacklist(torrentInfo)
            return
        }

        torrentService.resumeTorrent(info.hash)
    }
}