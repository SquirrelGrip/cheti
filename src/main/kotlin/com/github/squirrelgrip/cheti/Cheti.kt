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

    //    fun getRootCertificate(name: String = "RootCA", subject: Map<String, String> = emptyMap()): CertificateKeyPair {
//        return createAndLoad(
//            name,
//            RootCertificateGenerator(
//                BaseCertificateGenerator.generateDistinguishedName(subject, name)
//            ),
//            subject
//        )
//    }
//    fun getRootCertificate(
//        certificateConfiguration: CertificateConfiguration
//    ): CertificateKeyPair {
//        return createAndLoad(
//            RootCertificateGenerator(),
//            certificateConfiguration
//        )
//    }

//    fun getSigningCertificate(
//        rootCertificateKeyPair: CertificateKeyPair,
//        certName: String = "SigningCA",
//        subject: Map<String, String> = emptyMap()
//    ): CertificateKeyPair {
//        return createAndLoad(
//            certName,
//            SigningCertificateGenerator(
//                rootCertificateKeyPair
//            ),
//            subject
//        )
//    }
//
//    fun getSigningCertificate(
//        rootCertificateKeyPair: CertificateKeyPair,
//        certificateConfiguration: CertificateConfiguration
//    ): CertificateKeyPair {
//        return createAndLoad(
//            SigningCertificateGenerator(
//                this,
//                rootCertificateKeyPair
//            ),
//            certificateConfiguration
//        )
//    }

//    fun getServerCertificate(
//        signingCertificateKeyPair: CertificateKeyPair,
//        certName: String = getHostName(),
//        subject: Map<String, String> = emptyMap()
//    ): CertificateKeyPair {
//        val altSubject = arrayOf(
//            GeneralName(GeneralName.iPAddress, getLocalAddress()),
//            GeneralName(GeneralName.dNSName, getHostName())
//        )
//        return createAndLoad(
//            certName,
//            ServerCertificateGenerator(
//                signingCertificateKeyPair
//            ),
//            subject,
//            altSubject
//        )
//    }

//    fun getServerCertificate(
//        signingCertificateKeyPair: CertificateKeyPair,
//        certificateConfiguration: CertificateConfiguration
//    ): CertificateKeyPair {
//        val altSubject = arrayOf(
//            GeneralName(GeneralName.iPAddress, getLocalAddress()),
//            GeneralName(GeneralName.dNSName, getHostName())
//        )
//        return createAndLoad(
//            ServerCertificateGenerator(
//                this,
//                signingCertificateKeyPair
//            ),
//            certificateConfiguration,
//            altSubject
//        )
//    }
//
//    fun getClientCertificate(
//        signingCertificateKeyPair: CertificateKeyPair,
//        certificateConfiguration: CertificateConfiguration
//    ): CertificateKeyPair {
//        val altSubject = arrayOf<GeneralName>(
////            GeneralName(GeneralName.iPAddress, getLocalAddress()),
////            GeneralName(GeneralName.dNSName, getHostName())
//        )
//        return createAndLoad(
//            ServerCertificateGenerator(
//                this,
//                signingCertificateKeyPair
//            ),
//            certificateConfiguration,
//            altSubject
//        )
//    }

    //    private fun createAndLoad(
//        certName: String,
//        certificateGenerator: BaseCertificateGenerator,
//        subject: Map<String, String>,
//        altSubject: Array<GeneralName> = emptyArray()
//    ): CertificateKeyPair {
//        if (!File(certDir(certName), "$certName.crt").exists()) {
//            println("Creating KeyPair for $certName")
//            val keyPair = BaseCertificateGenerator.createKeyPair().apply {
//                this.write(writer(certName, "key"))
//            }
//            println("Signing Certificate for $certName")
//            certificateGenerator.create(
//                keyPair,
//                BaseCertificateGenerator.generateDistinguishedName(subject, certName),
//                altSubject
//            ).apply {
//                this.write(writer(certName, "crt"))
//            }
//        }
//        return loadCertificateKeyPair(certName)
//    }
//
//    private fun createAndLoad(
//        certificateGenerator: BaseCertificateGenerator,
//        certificateConfiguration: CertificateConfiguration,
//        altSubject: Array<GeneralName> = emptyArray()
//    ): CertificateKeyPair {
//        if (!certificateConfiguration.certificateFile.exists() && !certificateConfiguration.keyFile.exists()) {
//            println("Creating KeyPair for ${certificateConfiguration.name}")
//            val keyPair = BaseCertificateGenerator.createKeyPair().apply {
//                write(certificateConfiguration.keyFile)
//            }
//            println("Signing Certificate for ${certificateConfiguration.name}")
//            certificateGenerator.create(
//                keyPair,
//                BaseCertificateGenerator.generateDistinguishedName(
//                    certificateConfiguration.subject,
//                    certificateConfiguration.name
//                ), altSubject
//            ).apply {
//                write(certificateConfiguration.certificateFile)
//            }
//        }
//        return loadCertificateKeyPair(certificateConfiguration.name)
//    }

    fun getLocalAddress() =
        InetAddress.getAllByName(InetAddress.getLocalHost().hostName).first { it.isSiteLocalAddress }.hostAddress

    fun getHostName() =
        InetAddress.getLocalHost().hostName

    fun loadConfiguration(file: File): ChetiConfiguration {
        val chetiConfiguration = file.toInstance<ChetiConfiguration>()
        val errors = chetiConfiguration.validate()
        if (!errors.isEmpty()) {
            errors.forEach { println(it) }
            throw InvalidConfigurationException()
        }
        return chetiConfiguration
    }

//    fun createAndLoad(chetiConfiguration: ChetiConfiguration) {
//        val certificateKeyPairs = mutableMapOf<CertificateConfiguration, CertificateKeyPair>()
////        val certificateChains = mutableMapOf<String, List<CertificateKeyPair>>()
//        chetiConfiguration.certificates.sortedBy { it.type }.forEach {
//            val issuer = getIssuerCertificate(certificateKeyPairs, it)
//            val certificateKeyPair = when (it.type) {
//                CertificateType.ROOT -> RootCertificateGenerator()
//                CertificateType.SIGNING -> getSigningCertificate(issuer!!, it)
//                CertificateType.SERVER -> getServerCertificate(issuer!!, it)
//                CertificateType.CLIENT -> getClientCertificate(issuer!!, it)
//            }
//            certificateKeyPairs[it] = certificateKeyPair
////            if (it.type == CertificateType.SERVER || it.type == CertificateType.CLIENT) {
////                val rootCertificateKeyPair = getIssuerCertificate(
////                    certificateKeyPairs,
////                    getIssuerCertificateConfiguration(certificateKeyPairs, it)!!
////                )
////                certificateChains[it.name] = listOf(
////                    certificateKeyPair,
////                    issuer!!,
////                    rootCertificateKeyPair!!
////                )
////            }
//        }
////        KeyStore.getInstance("PKCS12").apply {
////            this.load(null, null)
////            certificateChains.forEach { (key, value) ->
////                this.setKeyEntry(
////                    key,
////                    value[0].keyPair.private,
////                    "password".toCharArray(),
////                    value.map { it.certificate }.toTypedArray()
////                )
////            }
////        }.write(extension = "p12")
////        KeyStore.getInstance("JKS").apply {
////            this.load(null, null)
////            certificateChains.forEach { (key, value) ->
////                this.setKeyEntry(
////                    key,
////                    value[0].keyPair.private,
////                    "password".toCharArray(),
////                    value.map { it.certificate }.toTypedArray()
////                )
////            }
////        }.write(extension = "jks")
//    }

//    fun createAndLoad(chetiConfiguration: ChetiConfiguration) {
//        val certificateKeyPairs = mutableMapOf<CertificateConfiguration, CertificateKeyPair>()
//        chetiConfiguration.certificates.sortedBy { it.type }.associate {
//            it to certificateFactory.create(it)
//        }
//    }
}

fun main(args: Array<String>) {
    val chetiConfiguration = Cheti.loadConfiguration(File("cheti.json"))
    val certificateLoader = CertificateLoader(chetiConfiguration.common)
    chetiConfiguration.certificates.forEach {
        val certificateKeyPair = certificateLoader.load(it)
    }
}
