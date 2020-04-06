package com.github.squirrelgrip.cheti.generator

import org.bouncycastle.asn1.x509.*
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import java.security.KeyPair
import java.security.PrivateKey
import javax.security.auth.x500.X500Principal

class RootCertificateGenerator(subject: X500Principal): BaseSigningCertificateGenerator(subject) {

    override fun getIssuerPrivateKey(keyPair: KeyPair): PrivateKey {
        return keyPair.private
    }

    override fun getAuthorityKeyIdentifier(certificateBuilder: JcaX509v3CertificateBuilder, keyPair: KeyPair): AuthorityKeyIdentifier {
        val certificate =
            getCertificate(
                certificateBuilder,
                getIssuerPrivateKey(keyPair)
            )
        return createAuthorityKeyIdentifier(
            certificate
        )
    }

    override fun getBasicConstraints() = BasicConstraints(2)

}