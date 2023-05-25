package com.github.schaka.rarrnomore.torrent.rest

import com.github.schaka.rarrnomore.torrent.qbit.QBittorrent
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
        val loginCookie = attemptAuthentication(properties)
        return builder
            .rootUri("${properties.url}/api/v2")
            .defaultHeader(COOKIE, loginCookie)
            .build()
    }

    private fun attemptAuthentication(properties: TorrentClientProperties): String {
        try {
            val login = RestTemplate()
            val map = LinkedMultiValueMap<String, Any>()
            map.add("username", properties.username)
            map.add("password", properties.password)
            val loginID = login.postForEntity("${properties.url}/api/v2/auth/login", HttpEntity(map), String::class.java)
            return loginID.headers[SET_COOKIE]?.find{ s -> s.contains("SID") } !!
        } catch (e: Exception) {
            log.error("Error connecting to torrent client", e)
        }

        throw IllegalStateException("Can't start application, no torrent client connection possible");
    }
}