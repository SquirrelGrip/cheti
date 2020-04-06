package com.github.squirrelgrip.cheti

import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier
import org.junit.jupiter.api.Test
import java.security.cert.X509Certificate


internal class ChetiTest {
    @Test
    fun `verify created root`() {
        val expectedSubject = "CN=RootCA,O=SquirrelGrip,L=Singapore,ST=Singapore,C=SG"

        val testSubject = Cheti.getRootCertificate()

        testSubject.certificate.checkValidity()
        assertThat(testSubject.certificate.version).isEqualTo(3)

        assertThat(testSubject.certificate.subjectX500Principal.name).isEqualTo(expectedSubject)
        assertThat(testSubject.certificate.issuerX500Principal.name).isEqualTo(expectedSubject)

        assertThat(testSubject.certificate.basicConstraints).isEqualTo(2)
        assertThat(testSubject.certificate.keyUsage).isEqualTo(arrayOf(true, false, false, false, false, true, false, false, false))

        assertThat(testSubject.certificate.criticalExtensionOIDs).hasSize(2)
        assertThat(testSubject.certificate.nonCriticalExtensionOIDs).containsExactlyInAnyOrder(Extension.subjectKeyIdentifier.id, Extension.authorityKeyIdentifier.id)

        assertThat(testSubject.certificate.subjectAlternativeNames).isNull()
        assertThat(testSubject.certificate.extendedKeyUsage).isNull()
    }

    @Test
    fun `verify created root using subject`() {
        val expectedSubject = "CN=cn,O=organisation,L=location,ST=state,C=c"

        val testSubject = Cheti.getRootCertificate("CA", mapOf(
            "c" to "c",
            "st" to "state",
            "l" to "location",
            "o" to "organisation",
            "cn" to "cn"
        ))

        testSubject.certificate.checkValidity()
        assertThat(testSubject.certificate.version).isEqualTo(3)

        assertThat(testSubject.certificate.subjectX500Principal.name).isEqualTo(expectedSubject)
        assertThat(testSubject.certificate.issuerX500Principal.name).isEqualTo(expectedSubject)

        assertThat(testSubject.certificate.basicConstraints).isEqualTo(2)
        assertThat(testSubject.certificate.keyUsage).isEqualTo(arrayOf(true, false, false, false, false, true, false, false, false))

        assertThat(testSubject.certificate.criticalExtensionOIDs).hasSize(2)
        assertThat(testSubject.certificate.nonCriticalExtensionOIDs).containsExactlyInAnyOrder(Extension.subjectKeyIdentifier.id, Extension.authorityKeyIdentifier.id)

        assertThat(testSubject.certificate.subjectAlternativeNames).isNull()
        assertThat(testSubject.certificate.extendedKeyUsage).isNull()
    }

    @Test
    fun `verify created signing`() {
        val expectedSubject = "CN=SigningCA,O=SquirrelGrip,L=Singapore,ST=Singapore,C=SG"
        val expectedIssuer = "CN=RootCA,O=SquirrelGrip,L=Singapore,ST=Singapore,C=SG"

        val testSubject = Cheti.getSigningCertificate(Cheti.getRootCertificate())

        testSubject.certificate.checkValidity()
        assertThat(testSubject.certificate.version).isEqualTo(3)

        assertThat(testSubject.certificate.subjectX500Principal.name).isEqualTo(expectedSubject)
        assertThat(testSubject.certificate.issuerX500Principal.name).isEqualTo(expectedIssuer)

        assertThat(testSubject.certificate.basicConstraints).isEqualTo(1)
        assertThat(testSubject.certificate.keyUsage).isEqualTo(arrayOf(true, false, false, false, false, true, false, false, false))

        assertThat(testSubject.certificate.criticalExtensionOIDs).hasSize(2)
        assertThat(testSubject.certificate.nonCriticalExtensionOIDs).containsExactlyInAnyOrder(Extension.subjectKeyIdentifier.id, Extension.authorityKeyIdentifier.id)

        assertThat(testSubject.certificate.subjectAlternativeNames).isNull()
        assertThat(testSubject.certificate.extendedKeyUsage).isNull()
    }

    @Test
    fun `verify created server`() {
        val expectedSubject = "CN=Server,O=SquirrelGrip,L=Singapore,ST=Singapore,C=SG"
        val expectedIssuer = "CN=SigningCA,O=SquirrelGrip,L=Singapore,ST=Singapore,C=SG"

        val testSubject = Cheti.getServerCertificate(Cheti.getSigningCertificate(Cheti.getRootCertificate()), "Server")

        testSubject.certificate.checkValidity()
        assertThat(testSubject.certificate.version).isEqualTo(3)

        assertThat(testSubject.certificate.subjectX500Principal.name).isEqualTo(expectedSubject)
        assertThat(testSubject.certificate.issuerX500Principal.name).isEqualTo(expectedIssuer)

        assertThat(testSubject.certificate.basicConstraints).isEqualTo(-1)
        assertThat(testSubject.certificate.keyUsage).isEqualTo(arrayOf(true, false, true, true, false, false, false, false, false))
        assertThat(testSubject.certificate.criticalExtensionOIDs).hasSize(2)
        assertThat(testSubject.certificate.nonCriticalExtensionOIDs).containsExactlyInAnyOrder(Extension.subjectKeyIdentifier.id, Extension.authorityKeyIdentifier.id, Extension.extendedKeyUsage.id, Extension.subjectAlternativeName.id)

        assertThat(testSubject.certificate.subjectAlternativeNames).hasSize(2)
        assertThat(testSubject.certificate.extendedKeyUsage).containsExactly("1.3.6.1.5.5.7.3.1")
    }

}