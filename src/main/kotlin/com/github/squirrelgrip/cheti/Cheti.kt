package com.github.squirrelgrip.cheti

import com.github.squirrelgrip.cheti.configuration.ChetiConfiguration
import com.github.squirrelgrip.extensions.json.toInstance
import java.io.File
import java.io.InputStream
import java.net.InetAddress

object Cheti {

    fun loadConfiguration(file: File): ChetiConfiguration = file.toInstance()

    fun loadConfiguration(fileName: String): ChetiConfiguration = loadConfiguration(File(fileName))

    fun loadConfiguration(inputStream: InputStream): ChetiConfiguration = inputStream.toInstance()

    private fun validate(chetiConfiguration: ChetiConfiguration) {
        val errors = chetiConfiguration.validate()
        if (!errors.isEmpty()) {
            errors.forEach { println(it) }
            throw InvalidConfigurationException()
        }
    }

    fun execute(chetiConfiguration: ChetiConfiguration) {
        validate(chetiConfiguration)
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
    val chetiConfiguration = Cheti.loadConfiguration(args[0])
    Cheti.execute(chetiConfiguration)
}

fun getLocalAddress() =
    InetAddress.getAllByName(InetAddress.getLocalHost().hostName).first { it.isSiteLocalAddress }.hostAddress

fun getHostName() =
    InetAddress.getLocalHost().hostName


