package com.github.squirrelgrip.cheti.generator

import com.github.squirrelgrip.cheti.loader.CertificateLoader
import com.github.squirrelgrip.cheti.configuration.CertificateConfiguration
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.KeyPurposeId
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import java.security.PublicKey

class ClientCertificateGenerator(
    certificateLoader: CertificateLoader,
    certificateConfiguration: CertificateConfiguration
) : BaseEndEntityCertificateGenerator(
    certificateLoader,
    certificateConfiguration
) {
    override fun addExtensions(
        certificateBuilder: JcaX509v3CertificateBuilder,
        publicKey: PublicKey,
        authorityKeyIdentifier: AuthorityKeyIdentifier
    ) {
        super.addExtensions(certificateBuilder, publicKey, authorityKeyIdentifier)
        certificateBuilder.addExtension(Extension.extendedKeyUsage, false, ExtendedKeyUsage(KeyPurposeId.id_kp_clientAuth))
    }

}