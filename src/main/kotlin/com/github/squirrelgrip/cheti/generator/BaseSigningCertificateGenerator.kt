package com.github.squirrelgrip.cheti.generator

import com.github.squirrelgrip.cheti.generator.BaseCertificateGenerator
import org.bouncycastle.asn1.x509.*
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import java.security.KeyPair
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.security.auth.x500.X500Principal

abstract class BaseSigningCertificateGenerator(
    issuer: X500Principal
) : BaseCertificateGenerator(
    issuer
) {
    override fun create(keyPair: KeyPair, subject: X500Principal, altSubject: Array<GeneralName>): X509Certificate {
        val serialNumber = generateSerialNumber()
        val certificateBuilder = generateCertificateBuilder(subject, serialNumber, keyPair.public)
        addExtensions(certificateBuilder, keyPair.public, getAuthorityKeyIdentifier(certificateBuilder, keyPair), altSubject)
        return JcaX509CertificateConverter().getCertificate(certificateBuilder.build(
            buildContentSigner(
                getIssuerPrivateKey(keyPair)
            )
        ))
    }

    abstract fun getIssuerPrivateKey(keyPair: KeyPair): PrivateKey

    abstract fun getAuthorityKeyIdentifier(certificateBuilder: JcaX509v3CertificateBuilder, keyPair: KeyPair): AuthorityKeyIdentifier

    override fun addAdditionalExtensions(certificateBuilder: JcaX509v3CertificateBuilder) {
        certificateBuilder.addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.keyCertSign or KeyUsage.digitalSignature))
    }


}