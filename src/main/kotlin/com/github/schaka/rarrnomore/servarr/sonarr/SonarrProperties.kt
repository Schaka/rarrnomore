package com.github.schaka.rarrnomore.servarr.sonarr

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.sonarr")
data class SonarrProperties(
    val url: String,
    val apiKey: String
)