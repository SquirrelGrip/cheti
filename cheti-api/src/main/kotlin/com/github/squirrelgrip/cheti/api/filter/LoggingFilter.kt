package com.github.squirrelgrip.cheti.api.filter

import com.fasterxml.jackson.databind.SerializationFeature
import com.github.squirrelgrip.extension.json.Json
import com.github.squirrelgrip.extension.json.toJson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class LoggingFilter(
    val applicationEventPublisher: ApplicationEventPublisher
) : Filter {
    companion object {
        val LOG: Logger = LoggerFactory.getLogger(LoggingFilter::class.java)
    }

    init {
        Json.objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
    }

    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain
    ) {
        val req = ContentCachingRequestWrapper(request as HttpServletRequest)
        val res = ContentCachingResponseWrapper(response as HttpServletResponse)
        chain.doFilter(req, res)
        val requestResponse = RequestResponse(req, res)
        applicationEventPublisher.publishEvent(RequestResponseApplicationEvent(this, requestResponse))
        println(requestResponse.toJson())
        res.copyBodyToResponse()
    }
}
