package com.github.squirrelgrip.cheti

import com.github.squirrelgrip.cheti.configuration.ChetiConfiguration
import com.github.squirrelgrip.cheti.exception.InvalidConfigurationException
import com.github.squirrelgrip.cheti.loader.CertificateLoader
import com.github.squirrelgrip.cheti.loader.ChainLoader
import com.github.squirrelgrip.cheti.loader.KeyStoreLoader
import com.github.squirrelgrip.cheti.template.TemplateProvider
import com.github.squirrelgrip.cheti.template.VelocityProvider
import com.github.squirrelgrip.extensions.io.toReader
import com.github.squirrelgrip.extensions.json.toInstance
import java.io.File
import java.io.InputStream
import java.net.InetAddress

class Cheti(
    val templateProvider: TemplateProvider = VelocityProvider()
) {
    fun loadConfiguration(file: File, context: Map<String, String> = emptyMap()): ChetiConfiguration {
        return templateProvider.loadTemplate(file, context).toInstance()
    }

    fun loadConfiguration(fileName: String, context: Map<String, String> = emptyMap()): ChetiConfiguration {
        return loadConfiguration(File(fileName), context)
    }

    fun loadConfiguration(inputStream: InputStream, context: Map<String, String> = emptyMap()): ChetiConfiguration {
        return templateProvider.loadTemplate(inputStream.toReader(), "cheti", context).toInstance()
    }

    private fun validate(chetiConfiguration: ChetiConfiguration) {
        val errors = chetiConfiguration.validate()
        if (!errors.isEmpty()) {
            errors.forEach { println(it) }
            throw InvalidConfigurationException()
        }
    }

    fun execute(chetiConfiguration: ChetiConfiguration) {
        validate(chetiConfiguration)
        val certificateLoader =
            CertificateLoader(chetiConfiguration.common)
        chetiConfiguration.certificates.forEach {
            certificateLoader.load(it)
        }
        val chainLoader = ChainLoader(certificateLoader)
        chetiConfiguration.chains.forEach {
            chainLoader.load(it)
        }
        val keyStoreLoader =
            KeyStoreLoader(certificateLoader, chainLoader)
        chetiConfiguration.keystores.forEach {
            keyStoreLoader.load(it)
        }
    }
}

fun main(args: Array<String>) {
    val cheti = Cheti()
    val configFile = args[0]
    val context = mapOf(
        "IP_ADDRESS" to getLocalAddress(),
        "HOSTNAME" to getHostName()
    )
    val chetiConfiguration = cheti.loadConfiguration(
        configFile, context
    )
    cheti.execute(chetiConfiguration)
}

fun getLocalAddress(): String {
    val arrayOfInetAddress = InetAddress.getAllByName(getHostName())
    return arrayOfInetAddress.map {
        println(it.hostAddress)
        it
    }.first {
        it.isSiteLocalAddress
    }.hostAddress
}

fun getHostName() =
    InetAddress.getLocalHost().hostName


