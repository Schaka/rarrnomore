package com.github.schaka.rarrnomore.torrent.rest

import com.github.schaka.rarrnomore.torrent.TorrentClientType
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.torrent")
data class TorrentClientProperties(
    val type: TorrentClientType,
    val name: String,
    val url: String,
    val username: String,
    val password: String
)