package com.github.squirrelgrip.cheti.generator

import com.github.squirrelgrip.cheti.CertificateKeyPair
import org.bouncycastle.asn1.x509.*
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
    val issuerCertificateKeyPair: CertificateKeyPair
) : BaseCertificateGenerator(
    issuerCertificateKeyPair.certificate.subjectX500Principal
) {
    override fun create(keyPair: KeyPair, subject: X500Principal, altSubject: Array<GeneralName>): X509Certificate {
        return sign(signingRequest(keyPair, subject), altSubject)
    }

    fun sign(pkcS10CertificationRequest: PKCS10CertificationRequest, altSubject: Array<GeneralName>): X509Certificate {
        val jcaPKCS10CertificationRequest = JcaPKCS10CertificationRequest(pkcS10CertificationRequest)
        val subject = X500Principal(jcaPKCS10CertificationRequest.subject.encoded)
        val serialNumber =
            generateSerialNumber()
        val certificateBuilder = generateCertificateBuilder(subject, serialNumber, jcaPKCS10CertificationRequest.publicKey)
        addExtensions(certificateBuilder, jcaPKCS10CertificationRequest.publicKey,
            createAuthorityKeyIdentifier(
                issuerCertificateKeyPair.certificate
            ), altSubject)
        return JcaX509CertificateConverter().getCertificate(certificateBuilder.build(JcaContentSignerBuilder(
            signingAlgorithm
        ).build(issuerCertificateKeyPair.keyPair.private)))
    }

    fun signingRequest(keyPair: KeyPair, subject: X500Principal) =
        JcaPKCS10CertificationRequestBuilder(subject, keyPair.public).build(JcaContentSignerBuilder(signingAlgorithm).build(keyPair.private))

    override fun addExtensions(
        certificateBuilder: JcaX509v3CertificateBuilder,
        publicKey: PublicKey,
        authorityKeyIdentifier: AuthorityKeyIdentifier,
        altSubject: Array<GeneralName>
    ) {
        super.addExtensions(certificateBuilder, publicKey, authorityKeyIdentifier, altSubject)
        certificateBuilder.addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.keyEncipherment or KeyUsage.digitalSignature or KeyUsage.dataEncipherment))
    }

}