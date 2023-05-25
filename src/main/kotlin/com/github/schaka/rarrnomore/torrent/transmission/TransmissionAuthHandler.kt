package com.github.schaka.rarrnomore.torrent.transmission

import com.github.schaka.rarrnomore.torrent.rest.TorrentClientProperties
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.UnknownContentTypeException
import java.lang.Exception
import java.time.LocalDateTime

class TransmissionAuthHandler(
    val properties: TorrentClientProperties,
    var lastSessionId: String = ""
) : ClientHttpRequestInterceptor {

    companion object {
        @JvmStatic
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        request.headers["X-Transmission-Session-Id"] = lastSessionId
        val response = execution.execute(request, body)

        if (response.statusCode == HttpStatus.CONFLICT) {
            lastSessionId = response.headers["X-Transmission-Session-Id"]?.get(0)
                ?: throw IllegalStateException("Can't find Transmission session id in response: $response")
            request.headers["X-Transmission-Session-Id"] = lastSessionId
            return execution.execute(request, body)
        }

        return response
    }
}