package com.github.schaka.rarrnomore.torrent.rest

import com.github.schaka.rarrnomore.torrent.qbit.QBittorrent
import com.github.schaka.rarrnomore.torrent.qbit.QbitAuthInterceptor
import org.slf4j.LoggerFactory.getLogger
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders.COOKIE
import org.springframework.http.HttpHeaders.SET_COOKIE
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.lang.Exception

@Configuration
class TorrentClientConfig {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val log = getLogger(javaClass.enclosingClass)
    }

    @ConditionalOnProperty("clients.torrent.type", havingValue = "qbittorrent")
    @QBittorrent
    @Bean
    fun qBittorrentTemplate(builder: RestTemplateBuilder, properties: TorrentClientProperties): RestTemplate {
        return builder
            .rootUri("${properties.url}/api/v2")
            .interceptors(listOf(QbitAuthInterceptor(properties)))
            .build()
    }
}