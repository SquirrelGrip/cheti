package com.github.squirrelgrip.cheti.api.controller

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import reactor.core.publisher.Flux
import java.io.OutputStream
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.util.concurrent.Executors


@RestController
class HelloController {
    @GetMapping("/hello")
    fun hello(): String =
        "Hello"

    @GetMapping("/stream-sse")
    fun streamEvents(): Flux<ServerSentEvent<String>> =
        Flux.interval(Duration.ofSeconds(1))
            .map {
                ServerSentEvent.builder<String>()
                    .id(it.toString())
                    .event("periodic-event")
                    .data("SSE - " + LocalTime.now().toString())
                    .build()
            }

    @GetMapping("/stream-sse-mvc")
    fun streamSseMvc(): SseEmitter {
        val emitter = SseEmitter()
        val sseMvcExecutor = Executors.newSingleThreadExecutor()
        sseMvcExecutor.execute {
            try {
                var i = 0
                while (true) {
                    val event = SseEmitter.event()
                        .data("SSE MVC - " + LocalTime.now().toString())
                        .id(i.toString())
                        .name("sse event - mvc")
                    emitter.send(event)
                    Thread.sleep(1000)
                    i++
                }
            } catch (ex: Exception) {
                emitter.completeWithError(ex)
            }
        }
        return emitter
    }

    private val executor = Executors.newCachedThreadPool()

    @GetMapping("/rbe")
    fun handleRbe(): ResponseEntity<ResponseBodyEmitter> {
        val emitter = ResponseBodyEmitter()
        executor.execute {
            try {
                for (i in 1..60) {
                    emitter.send(
                        "/rbe@${Instant.now()}", MediaType.TEXT_PLAIN
                    )
                    Thread.sleep(1000)
                }
                emitter.complete()
            } catch (ex: java.lang.Exception) {
                emitter.completeWithError(ex)
            }
        }
        return ResponseEntity<ResponseBodyEmitter>(emitter, HttpStatus.OK)
    }

    @GetMapping("/srb")
    fun handleSrb(): ResponseEntity<StreamingResponseBody> {
        val stream = StreamingResponseBody { out: OutputStream ->
            val msg = "/srb@${Instant.now()}"
            out.write(msg.toByteArray())
        }
        return ResponseEntity<StreamingResponseBody>(stream, HttpStatus.OK)
    }
}