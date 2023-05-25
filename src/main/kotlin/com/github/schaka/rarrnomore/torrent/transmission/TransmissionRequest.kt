package com.github.schaka.rarrnomore.torrent.transmission

data class TransmissionRequest<T>(
    val method: String,
    val arguments: T
)
