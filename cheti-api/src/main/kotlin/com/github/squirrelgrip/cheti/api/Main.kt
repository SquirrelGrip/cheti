package com.github.squirrelgrip.cheti.api

import com.github.squirrelgrip.cheti.api.filter.LoggingFilter
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.ApplicationEventPublisher

import org.springframework.context.annotation.Bean




@SpringBootApplication
class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Main::class.java, *args)
        }
    }

    @Bean
    fun loggingFilter(
        applicationEventPublisher: ApplicationEventPublisher
    ): FilterRegistrationBean<LoggingFilter> {
        return FilterRegistrationBean<LoggingFilter>().apply {
            filter = LoggingFilter(applicationEventPublisher)
            addUrlPatterns("/*")
        }
    }
}