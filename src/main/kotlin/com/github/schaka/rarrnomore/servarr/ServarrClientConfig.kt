package com.github.schaka.rarrnomore.servarr

import com.github.schaka.rarrnomore.servarr.radarr.Radarr
import com.github.schaka.rarrnomore.servarr.radarr.RadarrProperties
import com.github.schaka.rarrnomore.servarr.sonarr.Sonarr
import com.github.schaka.rarrnomore.servarr.sonarr.SonarrProperties
import org.slf4j.LoggerFactory.getLogger
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class ServarrClientConfig {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val log = getLogger(javaClass.enclosingClass)
    }

    @Radarr
    @Bean
    fun radarrRestTemplate(builder: RestTemplateBuilder, properties: RadarrProperties): RestTemplate {
        return builder
            .rootUri("${properties.url}/api/v3")
            .defaultHeader("X-Api-Key", properties.apiKey)
            .build()
    }

    @Sonarr
    @Bean
    fun sonarrRestTemplate(builder: RestTemplateBuilder, properties: SonarrProperties): RestTemplate {
        return builder
            .rootUri("${properties.url}/api/v3")
            .defaultHeader("X-Api-Key", properties.apiKey)
            .build()
    }

}