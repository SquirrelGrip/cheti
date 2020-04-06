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
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemReader
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.net.InetAddress
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey
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

    fun loadCertificateKeyPair(name: String): CertificateKeyPair {
        val certificate = loadCertificate(name)
        val publicKey = certificate.publicKey
        val privateKey = loadPrivateKey(name)
        return CertificateKeyPair(
            certificate,
            KeyPair(publicKey, privateKey)
        )
    }

    private fun loadCertificate(name: String): X509Certificate {
        val certFile = File(certDir(name), "${name}.crt")
        val certificateFactory = getInstance("X.509")
        return certificateFactory.generateCertificate(FileInputStream(certFile)) as X509Certificate
    }

    private fun loadPrivateKey(name: String): PrivateKey {
        val pemReader = PemReader(FileReader(File(certDir(name), "${name}.key")))
        val pemObject: PemObject = pemReader.readPemObject()
        val pemContent = pemObject.content
        pemReader.close()
        val encodedKeySpec = PKCS8EncodedKeySpec(pemContent)
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(encodedKeySpec)
    }

    fun getRootCertificate(name: String = "RootCA", subject: Map<String, String> = emptyMap()): CertificateKeyPair {
        return createAndLoad(
            name,
            RootCertificateGenerator(
                BaseCertificateGenerator.generateDistinguishedName(subject, name)
            ),
            subject
        )
    }

    fun getSigningCertificate(
        rootCertificateKeyPair: CertificateKeyPair,
        certName: String = "SigningCA",
        subject: Map<String, String> = emptyMap()
    ): CertificateKeyPair {
        return createAndLoad(
            certName,
            SigningCertificateGenerator(
                rootCertificateKeyPair
            ),
            subject
        )
    }

    fun getServerCertificate(
        signingCertificateKeyPair: CertificateKeyPair,
        certName: String = getHostName(),
        subject: Map<String, String> = emptyMap()
    ): CertificateKeyPair {
        val altSubject = arrayOf(
            GeneralName(GeneralName.iPAddress, getLocalAddress()),
            GeneralName(GeneralName.dNSName, getHostName())
        )
        return createAndLoad(
            certName,
            ServerCertificateGenerator(
                signingCertificateKeyPair
            ),
            subject,
            altSubject
        )
    }

    fun getClientCertificate(
        signingCertificateKeyPair: CertificateKeyPair,
        certName: String = getHostName(),
        subject: Map<String, String> = emptyMap()
    ): CertificateKeyPair {
        val altSubject = arrayOf(
            GeneralName(GeneralName.iPAddress, getLocalAddress()),
            GeneralName(GeneralName.dNSName, getHostName())
        )
        return createAndLoad(
            certName,
            ServerCertificateGenerator(
                signingCertificateKeyPair
            ),
            subject,
            altSubject
        )
    }

    private fun createAndLoad(
        certName: String,
        certificateGenerator: BaseCertificateGenerator,
        subject: Map<String, String>,
        altSubject: Array<GeneralName> = emptyArray()
    ): CertificateKeyPair {
        if (!File(certDir(certName), "$certName.crt").exists()) {
            println("Creating KeyPair for $certName")
            val keyPair = BaseCertificateGenerator.createKeyPair().apply {
                this.write(writer(certName, "key"))
            }
            println("Signing Certificate for $certName")
            certificateGenerator.create(
                keyPair,
                BaseCertificateGenerator.generateDistinguishedName(subject, certName),
                altSubject
            ).apply {
                this.write(writer(certName, "crt"))
            }
        }
        return loadCertificateKeyPair(certName)
    }

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

    fun execute(chetiConfiguration: ChetiConfiguration) {
        val certificateKeyPairs = mutableMapOf<CertificateConfiguration, CertificateKeyPair>()
        val certificateChains = mutableMapOf<String, List<CertificateKeyPair>>()
        chetiConfiguration.certificates.sortedBy { it.type }.forEach {
            val issuer = getIssuerCertificate(certificateKeyPairs, it)
            val certificateKeyPair = when (it.type) {
                CertificateType.ROOT -> getRootCertificate(it.name, it.subject)
                CertificateType.SIGNING -> getSigningCertificate(issuer!!, it.name, it.subject)
                CertificateType.SERVER -> getServerCertificate(issuer!!, it.name, it.subject)
                CertificateType.CLIENT -> getClientCertificate(issuer!!, it.name, it.subject)
            }
            certificateKeyPairs[it] = certificateKeyPair
            if (it.type == CertificateType.SERVER || it.type == CertificateType.CLIENT) {
                val rootCertificateKeyPair = getIssuerCertificate(
                    certificateKeyPairs,
                    getIssuerCertificateConfiguration(certificateKeyPairs, it)!!
                )
                certificateChains[it.name] = listOf(
                    certificateKeyPair,
                    issuer!!,
                    rootCertificateKeyPair!!
                )
            }
        }
        KeyStore.getInstance("PKCS12").apply {
            this.load(null, null)
            certificateChains.forEach { (key, value) ->
                this.setKeyEntry(
                    key,
                    value[0].keyPair.private,
                    "password".toCharArray(),
                    value.map { it.certificate }.toTypedArray()
                )
            }
        }.write(extension = "p12")
        KeyStore.getInstance("JKS").apply {
            this.load(null, null)
            certificateChains.forEach { (key, value) ->
                this.setKeyEntry(
                    key,
                    value[0].keyPair.private,
                    "password".toCharArray(),
                    value.map { it.certificate }.toTypedArray()
                )
            }
        }.write(extension = "jks")
    }

    fun getIssuerCertificate(
        certificateKeyPairs: MutableMap<CertificateConfiguration, CertificateKeyPair>,
        certificateConfiguration: CertificateConfiguration
    ): CertificateKeyPair? {
        return getIssuer(certificateKeyPairs, certificateConfiguration)?.value
    }

    private fun getIssuer(
        certificateKeyPairs: MutableMap<CertificateConfiguration, CertificateKeyPair>,
        certificateConfiguration: CertificateConfiguration
    ): MutableMap.MutableEntry<CertificateConfiguration, CertificateKeyPair>? {
        return certificateKeyPairs.entries.firstOrNull {
            it.key.name == certificateConfiguration.issuer
        }
    }

    fun getIssuerCertificateConfiguration(
        certificateKeyPairs: MutableMap<CertificateConfiguration, CertificateKeyPair>,
        certificateConfiguration: CertificateConfiguration
    ): CertificateConfiguration? {
        return getIssuer(certificateKeyPairs, certificateConfiguration)?.key
    }

}

fun main(args: Array<String>) {
    val chetiConfiguration = Cheti.loadConfiguration(File("cheti.json"))
    Cheti.execute(chetiConfiguration)
//    val rootCertificateKeyPair = Cheti.getRootCertificate()
//    val signingCertificateKeyPair = Cheti.getSigningCertificate(rootCertificateKeyPair)
//    val serverCertificateKeyPair = Cheti.getServerCertificate(signingCertificateKeyPair)
//
//    val chain = arrayOf<Certificate>(
//        serverCertificateKeyPair.certificate,
//        signingCertificateKeyPair.certificate,
//        rootCertificateKeyPair.certificate
//    )
//    Cheti.createPKCS12KeyStore(
//        chain,
//        serverCertificateKeyPair.keyPair.private
//    ).write(extension = "p12")
//    Cheti.createJKSKeyStore(
//        chain,
//        serverCertificateKeyPair.keyPair.private
//    ).write(extension = "jks")
}
