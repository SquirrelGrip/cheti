package com.github.squirrelgrip.cheti

import com.github.squirrelgrip.cheti.configuration.ChetiConfiguration
import com.github.squirrelgrip.extensions.json.toInstance
import java.io.File
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
        val errors = chetiConfiguration.validate()
        if (!errors.isEmpty()) {
            errors.forEach { println(it) }
            throw InvalidConfigurationException()
        }
        return chetiConfiguration
    }

}

fun main(args: Array<String>) {
    val chetiConfiguration = Cheti.loadConfiguration(File("cheti.json"))
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

fun getLocalAddress() =
    InetAddress.getAllByName(InetAddress.getLocalHost().hostName).first { it.isSiteLocalAddress }.hostAddress

fun getHostName() =
    InetAddress.getLocalHost().hostName


