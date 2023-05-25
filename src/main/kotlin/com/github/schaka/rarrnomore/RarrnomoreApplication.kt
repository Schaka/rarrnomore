package com.github.schaka.rarrnomore

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties
@ConfigurationPropertiesScan
@SpringBootApplication
class RarrnomoreApplication

fun main(args: Array<String>) {
    runApplication<RarrnomoreApplication>(*args)
}
