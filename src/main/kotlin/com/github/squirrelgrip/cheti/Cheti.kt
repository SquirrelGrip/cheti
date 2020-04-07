package com.github.squirrelgrip.cheti

import com.github.squirrelgrip.cheti.configuration.ChetiConfiguration
import com.github.squirrelgrip.extensions.json.toInstance
import java.io.File
import java.io.InputStream
import java.net.InetAddress
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate


object Cheti {

    fun createPKCS12KeyStore(chain: Array<Certificate>, privateKey: PrivateKey) =
        KeyStore.getInstance("PKCS12").apply {
            this.load(null, null)
            this.setKeyEntry("localhost", privateKey, "password".toCharArray(), chain)
        }

    fun createJKSKeyStore(chain: Array<Certificate>, privateKey: PrivateKey) =
        KeyStore.getInstance("JKS").apply {
            this.load(null, null)
            this.setKeyEntry("localhost", privateKey, "password".toCharArray(), chain)
        }

    fun loadConfiguration(file: File): ChetiConfiguration {
        val chetiConfiguration = file.toInstance<ChetiConfiguration>()
        validate(chetiConfiguration)
        return chetiConfiguration
    }

    fun loadConfiguration(inputStream: InputStream): ChetiConfiguration {
        val chetiConfiguration = inputStream.toInstance<ChetiConfiguration>()
        validate(chetiConfiguration)
        return chetiConfiguration
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

    fun loadConfiguration(fileName: String): ChetiConfiguration {
        return loadConfiguration(File(fileName))
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


