package com.github.schaka.rarrnomore.torrent

import com.github.schaka.rarrnomore.hooks.TorrentInfo
import com.github.schaka.rarrnomore.servarr.ServarrService
import com.github.schaka.rarrnomore.servarr.TorrentNotInQueueException
import com.github.schaka.rarrnomore.torrent.rest.TorrentClientProperties
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.CopyOnWriteArrayList

@Component
class TorrentManager(
    private val torrentService: TorrentService,
    private val torrentClientProperties: TorrentClientProperties,
    private val torrentQueue: MutableList<TorrentQueueItem> = CopyOnWriteArrayList()
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
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
        val toBeRemoved = mutableListOf<TorrentQueueItem>()

        for (queueItem in torrentQueue) {
            // if 3 attempts have been made, abandon retries
            if (queueItem.attempts.get() >= 3) {
                log.error("Processing torrent ${queueItem.torrentInfo.torrentName} failed", queueItem.lastException)
                toBeRemoved.add(queueItem)
                continue
            }

            if (!needToRetry(queueItem)) {
                continue
            }

            if (tryToProcess(queueItem)) {
                toBeRemoved.add(queueItem)
            }
        }

        torrentQueue.removeAll(toBeRemoved)
    }

    private fun needToRetry(queueItem: TorrentQueueItem): Boolean {
        return (queueItem.attempts.get() == 0 && queueItem.lastAttempt.plusMinutes(1).isBefore(LocalDateTime.now()))
                ||
                (queueItem.attempts.get() > 0 && queueItem.lastAttempt.plusMinutes(5).isBefore(LocalDateTime.now()))
    }


    /**
     * Retries and reports success
     */
    private fun tryToProcess(queueItem: TorrentQueueItem): Boolean {
        val servarrService = queueItem.servarrService
        val torrentInfo = queueItem.torrentInfo

        try {
            log.trace("Attempting to reject or resume torrent ({}) ({})", torrentInfo.torrentName, torrentInfo.hash)
            rejectOrResumeTorrent(torrentInfo, servarrService)
            return true // no exception, success!
        } catch (e: TorrentNotInQueueException) {
            increment(queueItem, e)
        } catch (e: TorrentHashNotFoundException) {
            increment(queueItem, e)
        } catch (e: Exception) {
            log.error("Unexpected exception occurred, do not retry", e)
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

    private fun rejectOrResumeTorrent(info: TorrentInfo, servarrService: ServarrService) {
        val info = torrentService.enrichTorrentInfo(info)

        if (torrentContainsRar(info.filenames)) {
            // reject in Sonarr queue and delete
            servarrService.deleteAndBlacklist(info)
            return
        }

        if (torrentClientProperties.autoResume) {
            log.info("Torrent (${info.torrentName}) didn't contain rar files - resuming!")
            torrentService.resumeTorrent(info.hash)
        }
    }
}