package com.github.schaka.rarrnomore.hooks

import com.github.schaka.rarrnomore.hooks.radarr.RadarrWebHookRequest
import com.github.schaka.rarrnomore.hooks.sonarr.SonarrWebHookRequest
import com.github.schaka.rarrnomore.servarr.radarr.RadarrService
import com.github.schaka.rarrnomore.servarr.sonarr.SonarrService
import com.github.schaka.rarrnomore.torrent.TorrentManager
import com.github.schaka.rarrnomore.torrent.rest.TorrentClientProperties
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/hook")
class HookController(
    val sonarrService: SonarrService,
    val radarrService: RadarrService,
    val torrentManager: TorrentManager,
    val torrentClientProperties: TorrentClientProperties
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    @PostMapping("/sonarr")
    fun sonarr(@RequestBody request: SonarrWebHookRequest): ResponseEntity<Void> {

        if (!validateRequest(request)) {
            return ResponseEntity.noContent().build()
        }

        log.trace("{}", request)
        torrentManager.processGrab(toTorrentInfo(request), sonarrService)

        return ResponseEntity.noContent().build()
    }

    @PostMapping("/radarr")
    fun radarr(@RequestBody request: RadarrWebHookRequest): ResponseEntity<Void> {

        if (!validateRequest(request)) {
            return ResponseEntity.noContent().build()
        }

        log.trace("{}", request)
        torrentManager.processGrab(toTorrentInfo(request), radarrService)

        return ResponseEntity.noContent().build()
    }

    fun validateRequest(request: WebHookRequest): Boolean {

        if (request.eventType != "Grab") {
            log.warn("Received request not applicable for event type 'Grab': {}", request)
            return false
        }

        if (request.hash == null) {
            log.warn("Received test request or request without hash: {}", request)
            return false
        }

        if (torrentClientProperties.type.servarrName != request.downloadClientType) {
            log.warn("Client type doesn't match: {}", request)
            return false
        }

        if (torrentClientProperties.name != request.downloadClient) {
            log.warn("Client name doesn't match: {}", request)
            return false
        }

        return true
    }

    fun toTorrentInfo(request: SonarrWebHookRequest): TorrentInfo {
        return TorrentInfo(
            request.hash?.lowercase()!!,
            request.downloadClientType!!,
            request.downloadClient!!,
            request.release!!.indexer,
            request.release!!.releaseTitle
        )
    }

    fun toTorrentInfo(request: RadarrWebHookRequest): TorrentInfo {
        return TorrentInfo(
            request.hash?.lowercase()!!,
            request.downloadClientType!!,
            request.downloadClient!!,
            request.release!!.indexer,
            request.release!!.releaseTitle
        )
    }
}