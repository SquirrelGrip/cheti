package com.github.squirrelgrip.cheti.generator

import com.github.squirrelgrip.cheti.CertificateKeyPair
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.KeyPurposeId
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder

class ServerCertificateGenerator(
    issuerCertificateKeyPair: CertificateKeyPair
) : BaseEndEntityCertificateGenerator(
    issuerCertificateKeyPair
) {
    override fun addAdditionalExtensions(certificateBuilder: JcaX509v3CertificateBuilder) {
        certificateBuilder.addExtension(Extension.extendedKeyUsage, false, ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth))
    }

}