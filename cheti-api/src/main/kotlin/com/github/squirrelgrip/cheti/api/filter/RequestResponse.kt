package com.github.squirrelgrip.cheti.api.filter

import org.springframework.context.ApplicationEvent
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import javax.servlet.http.HttpServletRequest

data class RequestResponse(
    val request: Request,
    val response: Response,
    val requester: Requester
) {
    constructor(request: ContentCachingRequestWrapper, response: ContentCachingResponseWrapper): this(
        request.toRequest(),
        response.toResponse(),
        request.toRequestor()
    )
}

class RequestResponseApplicationEvent(
    source: Any,
    val requestResponse: RequestResponse
): ApplicationEvent(source)

private fun HttpServletRequest.toRequestor(): Requester {
    return Requester(
        this.remoteAddr,
        this.remotePort,
        this.remoteUser ?: "",
    )
}

data class Requester(
    val remoteHost: String,
    val remotePort: Int,
    val remoteUser: String
)

private fun ContentCachingRequestWrapper.toRequest(): Request {
    return Request(
        this.method,
        this.protocol,
        this.requestURI,
        this.queryString ?: "",
        this.localAddr,
        this.localPort,
        this.toHeaders(),
        this.contentAsByteArray.decodeToString()
    )
}

private fun ContentCachingRequestWrapper.toHeaders(): Map<String, String> {
    return this.headerNames.toList().associateWith {
        this.getHeader(it)
    }
}

private fun ContentCachingResponseWrapper.toResponse(): Response {
    return Response(
        this.status,
        this.toHeaders(),
        this.contentAsByteArray.decodeToString()
    )
}

private fun ContentCachingResponseWrapper.toHeaders(): Map<String, String> {
    return this.headerNames.toList().associateWith {
        this.getHeader(it)
    }
}

data class Request(
    val method: String,
    val protocol: String,
    val requestUri: String,
    val queryString: String,
    val localHost: String,
    val localPort: Int,
    val headers: Map<String, String>,
    val body: String
)

data class Response(
    val status: Int,
    val headers: Map<String, String>,
    val body: String
)
