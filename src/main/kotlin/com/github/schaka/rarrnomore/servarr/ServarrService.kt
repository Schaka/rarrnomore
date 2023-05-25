package com.github.schaka.rarrnomore.servarr

import com.github.schaka.rarrnomore.hooks.TorrentInfo

interface ServarrService {

    fun deleteAndBlacklist(info: TorrentInfo)
}