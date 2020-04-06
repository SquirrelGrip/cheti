package com.github.squirrelgrip.cheti.generator

import com.github.squirrelgrip.cheti.CertificateKeyPair
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import java.security.KeyPair
import java.security.PrivateKey

class SigningCertificateGenerator(
    private val issuerCertificateKeyPair: CertificateKeyPair
) : BaseSigningCertificateGenerator(
    issuerCertificateKeyPair.certificate.subjectX500Principal
) {
    override fun getIssuerPrivateKey(keyPair: KeyPair): PrivateKey {
        return issuerCertificateKeyPair.keyPair.private
    }

    override fun getAuthorityKeyIdentifier(certificateBuilder: JcaX509v3CertificateBuilder, keyPair: KeyPair): AuthorityKeyIdentifier {
        return createAuthorityKeyIdentifier(
            issuerCertificateKeyPair.certificate
        )
    }

    override fun getBasicConstraints() = BasicConstraints(1)
}