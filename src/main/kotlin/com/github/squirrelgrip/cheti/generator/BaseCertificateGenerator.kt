package com.github.squirrelgrip.cheti.generator

import com.github.squirrelgrip.extensions.time.toDate
import org.bouncycastle.asn1.x500.X500NameBuilder
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x509.*
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.math.BigInteger
import java.security.*
import java.security.cert.X509Certificate
import java.time.Instant
import java.time.ZoneOffset
import javax.security.auth.x500.X500Principal

abstract class BaseCertificateGenerator(
    val issuer: X500Principal
) {
    init {
        Security.addProvider(BouncyCastleProvider())
    }

    abstract fun create(
        keyPair: KeyPair,
        subject: X500Principal,
        altSubject: Array<GeneralName> = arrayOf()
    ): X509Certificate

    abstract fun addAdditionalExtensions(certificateBuilder: JcaX509v3CertificateBuilder)

    open fun addExtensions(
        certificateBuilder: JcaX509v3CertificateBuilder,
        publicKey: PublicKey,
        authorityKeyIdentifier: AuthorityKeyIdentifier,
        altSubject: Array<GeneralName>
    ) {
        certificateBuilder.addExtension(Extension.authorityKeyIdentifier, false, authorityKeyIdentifier)
        certificateBuilder.addExtension(Extension.basicConstraints, true, getBasicConstraints())
        certificateBuilder.addExtension(
            Extension.subjectKeyIdentifier,
            false,
            extensionUtils.createSubjectKeyIdentifier(publicKey)
        )
        if (altSubject.isNotEmpty()) {
            certificateBuilder.addExtension(Extension.subjectAlternativeName, false, GeneralNames(altSubject))
        }
        addAdditionalExtensions(certificateBuilder)
    }

    open fun getBasicConstraints() = BasicConstraints(false)

    fun generateCertificateBuilder(
        subject: X500Principal,
        serialNumber: BigInteger,
        publicKey: PublicKey
    ): JcaX509v3CertificateBuilder {
        val fromDate = Instant.now()
        val tillDate =
            fromDate.atOffset(ZoneOffset.UTC).plusYears(1).withSecond(0).withMinute(0).withHour(12).toInstant()
        return JcaX509v3CertificateBuilder(
            issuer,
            serialNumber,
            fromDate.toDate(),
            tillDate.toDate(),
            subject,
            publicKey
        )
    }

    companion object {
        val secureRandom = SecureRandom()
        val signingAlgorithm = "SHA256WITHRSA"
        val extensionUtils = JcaX509ExtensionUtils()

        fun generateDistinguishedName(cn: String) = X500Principal(
            X500NameBuilder()
                .addRDN(BCStyle.C, "SG")
                .addRDN(BCStyle.ST, "Singapore")
                .addRDN(BCStyle.L, "Singapore")
                .addRDN(BCStyle.O, "SquirrelGrip")
//            .addRDN(BCStyle.OU, "SG")
                .addRDN(BCStyle.CN, cn)
                .build().encoded
        )

        fun createAuthorityKeyIdentifier(certificate: X509Certificate) =
            extensionUtils.createAuthorityKeyIdentifier(
                certificate.publicKey,
                certificate.issuerX500Principal,
                certificate.serialNumber
            )

        fun getCertificate(
            certificateBuilder: JcaX509v3CertificateBuilder,
            privateKey: PrivateKey
        ) =
            JcaX509CertificateConverter().getCertificate(
                certificateBuilder.build(
                    buildContentSigner(
                        privateKey
                    )
                )
            )

        fun createKeyPair(): KeyPair =
            KeyPairGenerator.getInstance("RSA", "BC").apply {
                this.initialize(
                    4096,
                    secureRandom
                )
            }.generateKeyPair()

        fun generateSerialNumber() =
            BigInteger(
                160,
                secureRandom
            )

        fun buildContentSigner(privateKey: PrivateKey) =
            JcaContentSignerBuilder(signingAlgorithm).build(privateKey)

        fun generateDistinguishedName(map: Map<String, String>): X500Principal {
            val builder = X500NameBuilder()
                    map.forEach {(key, value) ->
                        val oid = BCStyle.INSTANCE.attrNameToOID(key)
                        builder.addRDN(oid, value)
                    }
            return X500Principal(builder.build().encoded)
        }

        fun generateDistinguishedName(
            subject: Map<String, String>,
            certName: String
        ): X500Principal {
            return if (subject.isEmpty()) {
                generateDistinguishedName(certName)
            } else {
                generateDistinguishedName(subject)
            }
        }



    }

}
