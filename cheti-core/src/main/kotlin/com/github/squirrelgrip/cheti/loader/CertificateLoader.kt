package com.github.squirrelgrip.cheti.loader

import com.github.squirrelgrip.cheti.model.CertificateKeyPair
import com.github.squirrelgrip.cheti.configuration.CertificateConfiguration
import com.github.squirrelgrip.cheti.configuration.CertificateType
import com.github.squirrelgrip.cheti.configuration.CommonConfiguration
import com.github.squirrelgrip.cheti.extension.toCertificate
import com.github.squirrelgrip.cheti.extension.toPrivateKey
import com.github.squirrelgrip.cheti.generator.*
import com.github.squirrelgrip.cheti.extension.write
import java.security.KeyPair

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
        val certificateKeyPair = CertificateKeyPair(
            certificate,
            keyPair
        ).apply {
            certificate.write(certificateConfiguration.certificateFile)
            keyPair.write(certificateConfiguration.keyFile)
        }
        return certificateKeyPair
    }

    private fun loadCertificateKeyPair(certificateConfiguration: CertificateConfiguration): CertificateKeyPair {
        val certificate = certificateConfiguration.certificateFile.toCertificate()
        val publicKey = certificate.publicKey
        val privateKey = certificateConfiguration.keyFile.toPrivateKey()
        return CertificateKeyPair(
            certificate,
            KeyPair(publicKey, privateKey)
        )
    }

    operator fun get(name: String): CertificateKeyPair? {
        return certificateMap.entries.firstOrNull { name == it.key.name }?.value
    }


}

