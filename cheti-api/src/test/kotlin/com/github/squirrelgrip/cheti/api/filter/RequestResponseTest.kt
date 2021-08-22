package com.github.squirrelgrip.cheti.api.filter

import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalTime
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.WebTarget


internal class RequestResponseTest {
    @Test
    fun submitHttpAndReplay() {
        val webTarget: WebTarget = ClientBuilder.newClient().target("http://localhost:8080/cheti")
        val response = webTarget.path("/hello").request().buildGet().invoke()
        println(response.entity)
    }

    @Test
    fun consumeServerSentEvent() {
        val client = WebClient.create("http://localhost:8080/cheti")
        val type: ParameterizedTypeReference<ServerSentEvent<String>> =
            object : ParameterizedTypeReference<ServerSentEvent<String>>() {}

        val eventStream = client.get()
            .uri("/srb")
            .retrieve()
            .bodyToFlux(type)

        eventStream.subscribe(
            {
                println(
                    "Time: ${LocalTime.now()} - event: name[${it.event()}], id [${it.id()}], content[${it.data()}] "
                )
            },
            {
                println("Error receiving SSE: $it")
            }
        ) { println("Completed!!!") }
        Thread.sleep(60000)
    }

}