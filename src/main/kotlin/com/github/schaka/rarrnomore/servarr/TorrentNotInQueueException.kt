package com.github.schaka.rarrnomore.servarr

class TorrentNotInQueueException(override val message: String) : RuntimeException(message) {
}