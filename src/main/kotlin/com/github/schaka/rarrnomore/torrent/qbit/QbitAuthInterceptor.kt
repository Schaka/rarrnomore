package com.github.schaka.rarrnomore.torrent.qbit

import com.github.schaka.rarrnomore.torrent.rest.TorrentClientProperties
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.lang.Exception
import java.time.LocalDateTime

class QbitAuthInterceptor(
    val properties: TorrentClientProperties,
    var lastLogin: LocalDateTime = LocalDateTime.MIN,
    var lastCookie: String = ""
) : ClientHttpRequestInterceptor {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        request.headers[HttpHeaders.COOKIE] = attemptAuthentication()
        return execution.execute(request, body)
    }

    private fun attemptAuthentication(): String {

        if (lastLogin.plusMinutes(50).isAfter(LocalDateTime.now())) {
            // no login required
            return lastCookie
        }

        try {
            val login = RestTemplate()
            val map = LinkedMultiValueMap<String, Any>()
            map.add("username", properties.username)
            map.add("password", properties.password)
            val loginID =
                login.postForEntity("${properties.url}/api/v2/auth/login", HttpEntity(map), String::class.java)
            val cookieHeader = loginID.headers[HttpHeaders.SET_COOKIE]?.find { s -> s.contains("SID") }!!
            lastLogin = LocalDateTime.now()
            lastCookie = cookieHeader
            return cookieHeader
        } catch (e: Exception) {
            log.error("Error connecting to torrent client", e)
        }

        throw IllegalStateException("Can't connect to QBittorrent");
    }
}