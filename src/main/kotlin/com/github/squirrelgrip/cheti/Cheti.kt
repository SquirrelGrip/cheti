package com.github.squirrelgrip.cheti

import com.github.squirrelgrip.cheti.configuration.ChetiConfiguration
import com.github.squirrelgrip.cheti.exception.InvalidConfigurationException
import com.github.squirrelgrip.cheti.loader.CertificateLoader
import com.github.squirrelgrip.cheti.loader.ChainLoader
import com.github.squirrelgrip.cheti.loader.KeyStoreLoader
import com.github.squirrelgrip.cheti.template.TemplateProvider
import com.github.squirrelgrip.cheti.template.VelocityProvider
import com.github.squirrelgrip.extension.file.toReader
import com.github.squirrelgrip.extension.io.toReader
import com.github.squirrelgrip.extension.json.toInstance
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.net.InetAddress

class Cheti(
    unpreparedChetiConfiguration: ChetiConfiguration
) {
    val chetiConfiguration: ChetiConfiguration = unpreparedChetiConfiguration.prepare()

    constructor(
        file: File,
        context: Map<String, String> = emptyMap(),
        templateProvider: TemplateProvider = VelocityProvider()
    ) : this(file.toReader(), context, templateProvider)

    constructor(
        fileName: String,
        context: Map<String, String> = emptyMap(),
        templateProvider: TemplateProvider = VelocityProvider()
    ) : this(File(fileName), context, templateProvider)

    constructor(
        inputStream: InputStream,
        context: Map<String, String> = emptyMap(),
        templateProvider: TemplateProvider = VelocityProvider()
    ) : this(inputStream.toReader(), context, templateProvider)

    constructor(
        reader: Reader,
        context: Map<String, String> = emptyMap(),
        templateProvider: TemplateProvider = VelocityProvider()
    ) : this(templateProvider.loadTemplate(reader, "cheti", context).toInstance<ChetiConfiguration>())


    private fun validate() {
        val errors = chetiConfiguration.validate()
        if (errors.isNotEmpty()) {
            errors.forEach { println(it) }
            throw InvalidConfigurationException()
        }
    }

    fun execute() {
        validate()
        val certificateLoader = CertificateLoader(chetiConfiguration.common)
        chetiConfiguration.certificates.forEach {
            certificateLoader.load(it)
        }
        val chainLoader = ChainLoader(certificateLoader)
        chetiConfiguration.chains.forEach {
            chainLoader.load(it)
        }
        val keyStoreLoader = KeyStoreLoader(certificateLoader, chainLoader)
        chetiConfiguration.keystores.forEach {
            keyStoreLoader.load(it)
        }
    }
}

fun main(args: Array<String>) {
    val configFile = args[0]
    val context = mapOf(
        "IP_ADDRESS" to getLocalAddress(),
        "HOSTNAME" to getHostName()
    )
    val cheti = Cheti(configFile, context)
    cheti.execute()
}

fun getLocalAddress(): String {
    return InetAddress.getAllByName(getHostName()).first {
        it.isSiteLocalAddress
    }.hostAddress
}

fun getHostName(): String =
    InetAddress.getLocalHost().hostName


