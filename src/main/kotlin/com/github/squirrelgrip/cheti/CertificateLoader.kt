package com.github.squirrelgrip.cheti

import com.github.squirrelgrip.cheti.configuration.CertificateConfiguration
import com.github.squirrelgrip.cheti.configuration.CertificateType
import com.github.squirrelgrip.cheti.configuration.CommonConfiguration
import com.github.squirrelgrip.cheti.generator.*
import com.github.squirrelgrip.extensions.file.toInputStream
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemReader
import java.io.File
import java.io.FileReader
import java.security.KeyFactory
import java.security.KeyPair
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec

class CertificateLoader(
    val commonConfiguration: CommonConfiguration
) {
    val certificateMap: MutableMap<CertificateConfiguration, CertificateKeyPair> = mutableMapOf()

    fun load(certificateConfiguration: CertificateConfiguration): CertificateKeyPair {
        return certificateMap.computeIfAbsent(certificateConfiguration) { create(certificateConfiguration) }
    }

    fun create(certificateConfiguration: CertificateConfiguration): CertificateKeyPair {
        if (certificateConfiguration.shouldCreate()) {
            return createCertificateKeyPair(certificateConfiguration)
        }
        return loadCertificateKeyPair(certificateConfiguration)
    }

    private fun createCertificateKeyPair(certificateConfiguration: CertificateConfiguration): CertificateKeyPair {
        val certificateGenerator = when (certificateConfiguration.type) {
            CertificateType.ROOT -> RootCertificateGenerator(this, certificateConfiguration)
            CertificateType.SIGNING -> SigningCertificateGenerator(this, certificateConfiguration)
            CertificateType.SERVER -> ServerCertificateGenerator(this, certificateConfiguration)
            CertificateType.CLIENT -> ClientCertificateGenerator(this, certificateConfiguration)
        }
        val keyPair = BaseCertificateGenerator.createKeyPair()
        val certificate = certificateGenerator.create(keyPair)
        val certificateKeyPair = CertificateKeyPair(certificate, keyPair).apply {
            certificate.write(certificateConfiguration.certificateFile)
            keyPair.write(certificateConfiguration.keyFile)
        }
        return certificateKeyPair
    }

    private fun loadCertificateKeyPair(certificateConfiguration: CertificateConfiguration): CertificateKeyPair {
        val certificate = loadCertificate(certificateConfiguration.certificateFile)
        val publicKey = certificate.publicKey
        val privateKey = loadPrivateKey(certificateConfiguration.keyFile)
        return CertificateKeyPair(
            certificate,
            KeyPair(publicKey, privateKey)
        )
    }

    private fun loadCertificate(certificateFile: File): X509Certificate {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        return certificateFactory.generateCertificate(certificateFile.toInputStream()) as X509Certificate
    }

    private fun loadPrivateKey(keyFile: File): PrivateKey {
        val pemReader = PemReader(FileReader(keyFile))
        val pemObject: PemObject = pemReader.readPemObject()
        val pemContent = pemObject.content
        pemReader.close()
        val encodedKeySpec = PKCS8EncodedKeySpec(pemContent)
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(encodedKeySpec)
    }

    operator fun get(name: String): CertificateKeyPair? {
        return certificateMap.entries.firstOrNull { name == it.key.name }?.value
    }


}
