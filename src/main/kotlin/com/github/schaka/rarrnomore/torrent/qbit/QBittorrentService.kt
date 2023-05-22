package com.github.schaka.rarrnomore.torrent.qbit

import com.github.schaka.rarrnomore.hooks.TorrentInfo
import com.github.schaka.rarrnomore.torrent.TorrentClientType
import com.github.schaka.rarrnomore.torrent.TorrentHashNotFoundException
import com.github.schaka.rarrnomore.torrent.TorrentService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.util.CollectionUtils
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import kotlin.IllegalStateException

@ConditionalOnProperty("clients.torrent.type", havingValue = "QBITTORRENT")
@Service
class QBittorrentService(
    @QBittorrent
    private var client: RestTemplate
) : TorrentService {

    override fun enrichTorrentInfo(info: TorrentInfo): TorrentInfo {
        val files = client.exchange(
            "/torrents/files?hash={hash}",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<QbitFileResponse>>() {},
            info.hash
        )

        if (CollectionUtils.isEmpty(files.body)) {
            throw TorrentHashNotFoundException("Torrent (${info.torrentName}) (${info.hash}) not in torrent client or files cannot be read")
        }

        info.addFiles(files.body!!.map(QbitFileResponse::name))
        return info
    }

    override fun resumeTorrent(hash: String) {
        val map = LinkedMultiValueMap<String, Any>()
        map.add("hashes", hash)
        client.postForEntity(
            "/torrents/resume",
            map,
            String::class.java
        )
    }


}