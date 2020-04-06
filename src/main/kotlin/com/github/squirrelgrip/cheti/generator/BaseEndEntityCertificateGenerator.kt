package com.github.squirrelgrip.cheti.generator

import com.github.squirrelgrip.cheti.CertificateLoader
import com.github.squirrelgrip.cheti.configuration.CertificateConfiguration
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import java.security.KeyPair
import java.security.PublicKey
import java.security.cert.X509Certificate
import javax.security.auth.x500.X500Principal

abstract class BaseEndEntityCertificateGenerator(
    certificateLoader: CertificateLoader,
    certificateConfiguration: CertificateConfiguration
) : BaseCertificateGenerator(
    certificateLoader,
    certificateConfiguration
) {
    override fun create(keyPair: KeyPair): X509Certificate {
        return sign(signingRequest(keyPair))
    }

    fun sign(pkcS10CertificationRequest: PKCS10CertificationRequest): X509Certificate {
        val jcaPKCS10CertificationRequest = JcaPKCS10CertificationRequest(pkcS10CertificationRequest)
        val serialNumber = generateSerialNumber()
        val certificateBuilder = generateCertificateBuilder(serialNumber, jcaPKCS10CertificationRequest.publicKey)
        addExtensions(certificateBuilder, jcaPKCS10CertificationRequest.publicKey, createAuthorityKeyIdentifier(getIssuer().certificate))
        return JcaX509CertificateConverter().getCertificate(certificateBuilder.build(JcaContentSignerBuilder(signingAlgorithm).build(getIssuer().keyPair.private)))
    }

    fun signingRequest(keyPair: KeyPair) =
        JcaPKCS10CertificationRequestBuilder(getSubjectPrincipal(), keyPair.public).build(JcaContentSignerBuilder(signingAlgorithm).build(keyPair.private))

    override fun addExtensions(
        certificateBuilder: JcaX509v3CertificateBuilder,
        publicKey: PublicKey,
        authorityKeyIdentifier: AuthorityKeyIdentifier
    ) {
        super.addExtensions(certificateBuilder, publicKey, authorityKeyIdentifier)
        certificateBuilder.addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.keyEncipherment or KeyUsage.digitalSignature or KeyUsage.dataEncipherment))
    }

}