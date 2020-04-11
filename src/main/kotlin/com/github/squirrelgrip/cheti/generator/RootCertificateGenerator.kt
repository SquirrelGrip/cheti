package com.github.squirrelgrip.cheti.generator

import com.github.squirrelgrip.cheti.loader.CertificateLoader
import com.github.squirrelgrip.cheti.configuration.CertificateConfiguration
import org.bouncycastle.asn1.x509.*
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import java.security.KeyPair
import java.security.PrivateKey

class RootCertificateGenerator(
    certificateLoader: CertificateLoader,
    certificateConfiguration: CertificateConfiguration
): BaseSigningCertificateGenerator(
    certificateLoader,
    certificateConfiguration
) {
    override fun getIssuerPrincipal() = getSubjectPrincipal()

    override fun getIssuerPrivateKey(keyPair: KeyPair): PrivateKey {
        return keyPair.private
    }

    override fun getAuthorityKeyIdentifier(certificateBuilder: JcaX509v3CertificateBuilder, keyPair: KeyPair): AuthorityKeyIdentifier {
        val certificate =
            buildCertificate(
                certificateBuilder,
                getIssuerPrivateKey(keyPair)
            )
        return createAuthorityKeyIdentifier(
            certificate
        )
    }

    override fun getBasicConstraints() = BasicConstraints(2)

}