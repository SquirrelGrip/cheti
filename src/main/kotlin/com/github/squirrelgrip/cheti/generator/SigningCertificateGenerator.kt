package com.github.squirrelgrip.cheti.generator

import com.github.squirrelgrip.cheti.CertificateLoader
import com.github.squirrelgrip.cheti.configuration.CertificateConfiguration
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import java.security.KeyPair
import java.security.PrivateKey

class SigningCertificateGenerator(
    certificateLoader: CertificateLoader,
    certificateConfiguration: CertificateConfiguration
) : BaseSigningCertificateGenerator(
    certificateLoader,
    certificateConfiguration
) {
    override fun getIssuerPrivateKey(keyPair: KeyPair): PrivateKey {
        return getIssuer().keyPair.private
    }

    override fun getAuthorityKeyIdentifier(certificateBuilder: JcaX509v3CertificateBuilder, keyPair: KeyPair): AuthorityKeyIdentifier {
        return createAuthorityKeyIdentifier(getIssuer().certificate)
    }

    override fun getBasicConstraints() = BasicConstraints(1)
}