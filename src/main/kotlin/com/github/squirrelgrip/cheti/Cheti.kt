package com.github.squirrelgrip.cheti

import com.github.squirrelgrip.cheti.configuration.CertificateConfiguration
import com.github.squirrelgrip.cheti.configuration.CertificateType
import com.github.squirrelgrip.cheti.configuration.ChetiConfiguration
import com.github.squirrelgrip.cheti.generator.BaseCertificateGenerator
import com.github.squirrelgrip.cheti.generator.RootCertificateGenerator
import com.github.squirrelgrip.cheti.generator.ServerCertificateGenerator
import com.github.squirrelgrip.cheti.generator.SigningCertificateGenerator
import com.github.squirrelgrip.extensions.json.toInstance
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemReader
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.net.InetAddress
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateFactory.getInstance
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec


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
}

fun getLocalAddress() =
    InetAddress.getAllByName(InetAddress.getLocalHost().hostName).first { it.isSiteLocalAddress }.hostAddress

fun getHostName() =
    InetAddress.getLocalHost().hostName


