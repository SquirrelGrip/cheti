package com.github.squirrelgrip.cheti.generator

import com.github.squirrelgrip.cheti.CertificateLoader
import com.github.squirrelgrip.cheti.configuration.CertificateConfiguration
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.X509Certificate

abstract class BaseSigningCertificateGenerator(
    certificateLoader: CertificateLoader,
    certificateConfiguration: CertificateConfiguration
) : BaseCertificateGenerator(
    certificateLoader,
    certificateConfiguration
) {
    override fun create(keyPair: KeyPair): X509Certificate {
        val serialNumber = generateSerialNumber()
        val certificateBuilder = generateCertificateBuilder(serialNumber, keyPair.public)
        addExtensions(
            certificateBuilder,
            keyPair.public,
            getAuthorityKeyIdentifier(certificateBuilder, keyPair)
        )
        return JcaX509CertificateConverter().getCertificate(
            certificateBuilder.build(
                buildContentSigner(
                    getIssuerPrivateKey(keyPair)
                )
            )
        )
    }

    abstract fun getIssuerPrivateKey(keyPair: KeyPair): PrivateKey

    abstract fun getAuthorityKeyIdentifier(certificateBuilder: JcaX509v3CertificateBuilder, keyPair: KeyPair): AuthorityKeyIdentifier

    override fun addExtensions(
        certificateBuilder: JcaX509v3CertificateBuilder,
        publicKey: PublicKey,
        authorityKeyIdentifier: AuthorityKeyIdentifier
    ) {
        super.addExtensions(certificateBuilder, publicKey, authorityKeyIdentifier)
        certificateBuilder.addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.keyCertSign or KeyUsage.digitalSignature))
    }

}