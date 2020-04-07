package com.github.squirrelgrip.cheti.generator

import com.github.squirrelgrip.cheti.CertificateKeyPair
import com.github.squirrelgrip.cheti.CertificateLoader
import com.github.squirrelgrip.cheti.InvalidConfigurationException
import com.github.squirrelgrip.cheti.configuration.CertificateConfiguration
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
    val certificateLoader: CertificateLoader,
    val certificateConfiguration: CertificateConfiguration
) {
    init {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    fun getIssuer(): CertificateKeyPair = certificateLoader[certificateConfiguration.issuer] ?: throw InvalidConfigurationException()

    abstract fun create(keyPair: KeyPair): X509Certificate

    open fun addAdditionalExtensions(certificateBuilder: JcaX509v3CertificateBuilder) {
        certificateConfiguration.extensions?.getExtensions()?.forEach {
            certificateBuilder.addExtension(it)
        }
    }

    open fun addExtensions(
        certificateBuilder: JcaX509v3CertificateBuilder,
        publicKey: PublicKey,
        authorityKeyIdentifier: AuthorityKeyIdentifier
    ) {
        certificateBuilder.addExtension(Extension.authorityKeyIdentifier, false, authorityKeyIdentifier)
        certificateBuilder.addExtension(Extension.basicConstraints, true, getBasicConstraints())
        certificateBuilder.addExtension(Extension.subjectKeyIdentifier, false, extensionUtils.createSubjectKeyIdentifier(publicKey))
        addAdditionalExtensions(certificateBuilder)
    }

    open fun getBasicConstraints() = BasicConstraints(false)

    fun generateCertificateBuilder(
        serialNumber: BigInteger,
        publicKey: PublicKey
    ): JcaX509v3CertificateBuilder {
        val fromDate = Instant.now()
        val tillDate =
            fromDate.atOffset(ZoneOffset.UTC).plus(certificateConfiguration.validDuration).toInstant()
        return JcaX509v3CertificateBuilder(
            getIssuerPrincipal(),
            serialNumber,
            fromDate.toDate(),
            tillDate.toDate(),
            getSubjectPrincipal(),
            publicKey
        )
    }

    fun getSubjectPrincipal(): X500Principal = generateDistinguishedName(certificateConfiguration.subject)

    open fun getIssuerPrincipal() = getIssuer().certificate.subjectX500Principal

    companion object {
        val secureRandom = SecureRandom()
        val signingAlgorithm = "SHA256WITHRSA"
        val extensionUtils = JcaX509ExtensionUtils()

        fun createAuthorityKeyIdentifier(certificate: X509Certificate) =
            extensionUtils.createAuthorityKeyIdentifier(
                certificate.publicKey,
                certificate.issuerX500Principal,
                certificate.serialNumber
            )

        fun buildCertificate(
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

    }

}
