package com.github.schaka.rarrnomore.torrent.transmission

import com.github.schaka.rarrnomore.hooks.TorrentInfo
import com.github.schaka.rarrnomore.torrent.TorrentHashNotFoundException
import com.github.schaka.rarrnomore.torrent.TorrentService
import com.github.schaka.rarrnomore.torrent.rest.Transmission
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.util.CollectionUtils
import org.springframework.web.client.RestTemplate

@ConditionalOnProperty("clients.torrent.type", havingValue = "TRANSMISSION")
@Service
class TransmissionService(
    @Transmission
    private var client: RestTemplate
) : TorrentService {

    override fun enrichTorrentInfo(info: TorrentInfo): TorrentInfo {
        val fileResponse = client.exchange(
            "/",
            HttpMethod.POST,
            HttpEntity(TransmissionRequest("torrent-get", object {
                val ids: List<String> = listOf(info.hash)
                val fields: List<String> = listOf("files")
            })),
            object : ParameterizedTypeReference<TransmissionResponse<TransmissionTorrentResponse>>() {}
        )

        val files = fileResponse.body?.arguments?.torrents
        if (CollectionUtils.isEmpty(files)) {
            throw TorrentHashNotFoundException("Torrent (${info.torrentName}) (${info.hash}) not in torrent client or files cannot be read")
        }

        info.addFiles(files!!.flatMap { it.files.map { it.name } })
        return info
    }

    override fun resumeTorrent(hash: String) {
        client.postForEntity(
            "/",
            TransmissionRequest("torrent-start", object {
                val ids: List<String> = listOf(hash)
            }),
            TransmissionResponse::class.java
        )
    }
}