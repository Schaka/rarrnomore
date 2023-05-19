package com.github.schaka.rarrnomore.hooks.sonarr

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.schaka.rarrnomore.hooks.WebHookRequest

data class SonarrWebHookRequest(
    var series: SonarrSeries,
    var episodes: Any,
    override var eventType: String,

    // if you grab something manually (e.g. through Interactive Search) and Sonarr can't automatically map it to a show, the below properties aren't available
    var release: SonarrRelease?,
    override var downloadClientType: String?,
    override var downloadClient: String?,
    @JsonProperty("downloadId")
    override var hash: String?,
    ) : WebHookRequest(eventType, downloadClientType, downloadClient, hash)