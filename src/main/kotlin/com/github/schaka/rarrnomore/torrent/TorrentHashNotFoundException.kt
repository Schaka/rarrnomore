package com.github.schaka.rarrnomore.torrent

class TorrentHashNotFoundException(override val message: String) : RuntimeException(message) {
}