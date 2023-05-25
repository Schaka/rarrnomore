package com.github.schaka.rarrnomore.hooks

import com.fasterxml.jackson.annotation.JsonProperty

open class WebHookRequest(
    open var eventType: String,
    // if you grab something manually (e.g. through Interactive Search) and Sonarr can't automatically map it to a show, the below properties aren't available
    open var downloadClientType: String?,
    open var downloadClient: String?,
    @JsonProperty("downloadId")
    open var hash: String?,
    )