package com.github.schaka.rarrnomore.servarr.radarr

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.radarr")
data class RadarrProperties(
    val url: String,
    val apiKey: String
)