package com.github.squirrelgrip.cheti

import com.github.squirrelgrip.cheti.configuration.*
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.GeneralName
import org.junit.jupiter.api.Test


internal class ChetiTest {
    private val commonConfiguration: CommonConfiguration = CommonConfiguration(
        "."
    )
    val testSubject: CertificateLoader = CertificateLoader(commonConfiguration)

    val location = "target/certs"
    val rootCertificateConfiguration = CertificateConfiguration(
        "RootCA",
        CertificateType.ROOT,
        GenerationType.ONCE,
        location,
        mapOf(
            "c" to "c",
            "st" to "state",
            "l" to "location",
            "o" to "organisation",
            "cn" to "RootCA"
        ),
        "",
        ExtensionsConfiguration()
    )
    val signingCertificateConfiguration = CertificateConfiguration(
        "SigningCA",
        CertificateType.SIGNING,
        GenerationType.ONCE,
        location,
        mapOf(
            "c" to "c",
            "st" to "state",
            "l" to "location",
            "o" to "organisation",
            "cn" to "SigningCA"
        ),
        "RootCA",
        ExtensionsConfiguration()
    )
    val serverCertificateConfiguration = CertificateConfiguration(
        "Server",
        CertificateType.SERVER,
        GenerationType.ONCE,
        location,
        mapOf(
            "cn" to "Server"
        ),
        "SigningCA",
        ExtensionsConfiguration(
            AltSubjectExtensionConfiguration(
                false, arrayOf(
                    AltSubject(AltSubjectType.IPAddress, "10.0.1.200"),
                    AltSubject(AltSubjectType.DNSName, "macbook-pro.local")
                )
            )
        )
    )

    @Test
    fun `verify created root`() {
        val expectedSubject = "CN=RootCA,O=organisation,L=location,ST=state,C=c"
        val certificateKeyPair = testSubject.load(rootCertificateConfiguration)

        certificateKeyPair.certificate.checkValidity()
        assertThat(certificateKeyPair.certificate.version).isEqualTo(3)

        assertThat(certificateKeyPair.certificate.subjectX500Principal.name).isEqualTo(expectedSubject)
        assertThat(certificateKeyPair.certificate.issuerX500Principal.name).isEqualTo(expectedSubject)

        assertThat(certificateKeyPair.certificate.basicConstraints).isEqualTo(2)
        assertThat(certificateKeyPair.certificate.keyUsage).isEqualTo(
            arrayOf(
                true,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false
            )
        )

        assertThat(certificateKeyPair.certificate.criticalExtensionOIDs).hasSize(2)
        assertThat(certificateKeyPair.certificate.nonCriticalExtensionOIDs).containsExactlyInAnyOrder(
            Extension.subjectKeyIdentifier.id,
            Extension.authorityKeyIdentifier.id
        )

        assertThat(certificateKeyPair.certificate.subjectAlternativeNames).isNull()
        assertThat(certificateKeyPair.certificate.extendedKeyUsage).isNull()
    }

    @Test
    fun `verify created signing`() {
        val expectedSubject = "CN=SigningCA,O=organisation,L=location,ST=state,C=c"
        val expectedIssuer = "CN=RootCA,O=organisation,L=location,ST=state,C=c"

        testSubject.load(rootCertificateConfiguration)
        val certificateKeyPair = testSubject.load(signingCertificateConfiguration)

        certificateKeyPair.certificate.checkValidity()
        assertThat(certificateKeyPair.certificate.version).isEqualTo(3)

        assertThat(certificateKeyPair.certificate.subjectX500Principal.name).isEqualTo(expectedSubject)
        assertThat(certificateKeyPair.certificate.issuerX500Principal.name).isEqualTo(expectedIssuer)

        assertThat(certificateKeyPair.certificate.basicConstraints).isEqualTo(1)
        assertThat(certificateKeyPair.certificate.keyUsage).isEqualTo(
            arrayOf(
                true,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false
            )
        )

        assertThat(certificateKeyPair.certificate.criticalExtensionOIDs).hasSize(2)
        assertThat(certificateKeyPair.certificate.nonCriticalExtensionOIDs).containsExactlyInAnyOrder(
            Extension.subjectKeyIdentifier.id,
            Extension.authorityKeyIdentifier.id
        )

        assertThat(certificateKeyPair.certificate.subjectAlternativeNames).isNull()
        assertThat(certificateKeyPair.certificate.extendedKeyUsage).isNull()
    }

    @Test
    fun `verify created server`() {
        val expectedSubject = "CN=Server"
        val expectedIssuer = "CN=SigningCA,O=organisation,L=location,ST=state,C=c"

        testSubject.load(rootCertificateConfiguration)
        testSubject.load(signingCertificateConfiguration)
        val certificateKeyPair = testSubject.load(serverCertificateConfiguration)

        certificateKeyPair.certificate.checkValidity()
        assertThat(certificateKeyPair.certificate.version).isEqualTo(3)

        assertThat(certificateKeyPair.certificate.subjectX500Principal.name).isEqualTo(expectedSubject)
        assertThat(certificateKeyPair.certificate.issuerX500Principal.name).isEqualTo(expectedIssuer)

        assertThat(certificateKeyPair.certificate.basicConstraints).isEqualTo(-1)
        assertThat(certificateKeyPair.certificate.keyUsage).isEqualTo(
            arrayOf(
                true,
                false,
                true,
                true,
                false,
                false,
                false,
                false,
                false
            )
        )
        assertThat(certificateKeyPair.certificate.criticalExtensionOIDs).hasSize(2)
        assertThat(certificateKeyPair.certificate.nonCriticalExtensionOIDs).containsExactlyInAnyOrder(
            Extension.subjectKeyIdentifier.id,
            Extension.authorityKeyIdentifier.id,
            Extension.extendedKeyUsage.id,
            Extension.subjectAlternativeName.id
        )

        assertThat(certificateKeyPair.certificate.subjectAlternativeNames).hasSize(2)
        assertThat(certificateKeyPair.certificate.extendedKeyUsage).containsExactly("1.3.6.1.5.5.7.3.1")
    }

}