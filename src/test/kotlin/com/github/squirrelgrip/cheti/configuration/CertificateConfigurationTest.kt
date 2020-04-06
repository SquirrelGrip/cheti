package com.github.squirrelgrip.cheti.configuration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import java.io.File

internal class CertificateConfigurationTest {

    val template = CertificateConfiguration(
        "Test",
        CertificateType.CLIENT,
        true,
        true,
        "target",
        emptyMap(),
        "issuer",
        emptyList()
    )
    val certificateFile = File("target", "Test.crt")
    val keyFile = File("target", "Test.key")

    lateinit var testSubject: CertificateConfiguration

    @BeforeEach
    fun beforeEach() {
        certificateFile.writeBytes(byteArrayOf())
        keyFile.writeBytes(byteArrayOf())
    }

    @AfterEach
    fun afterEach() {
        certificateFile.delete()
        keyFile.delete()
    }

    @Test
    fun `validate given certificate file is readonly`() {
        certificateFile.setWritable(false)
        certificateFile.setReadable(true)
        val testSubject = template.copy()

        val errors = testSubject.validate()

        assertThat(errors).containsExactly("target/Test.crt is not writable on the filesystem.")
    }

    @Test
    fun `validate given key file is readonly`() {
        keyFile.setWritable(false)
        keyFile.setReadable(true)
        val testSubject = template.copy()

        val errors = testSubject.validate()

        assertThat(errors).containsExactly("target/Test.key is not writable on the filesystem.")
    }

    @Test
    fun `validate given certificate file is not readable`() {
        certificateFile.setWritable(true)
        certificateFile.setReadable(false)
        val testSubject = template.copy()

        val errors = testSubject.validate()

        assertThat(errors).containsExactly("target/Test.crt is not readable on the filesystem.")
    }

    @Test
    fun `validate given key file is not readable`() {
        keyFile.setWritable(true)
        keyFile.setReadable(false)
        val testSubject = template.copy()

        val errors = testSubject.validate()

        assertThat(errors).containsExactly("target/Test.key is not readable on the filesystem.")
    }

    @Test
    fun `validate given certificate file has multiple issues`() {
        certificateFile.setWritable(false)
        certificateFile.setReadable(false)
        val testSubject = template.copy()

        val errors = testSubject.validate()

        assertThat(errors).containsExactly("target/Test.crt is not readable on the filesystem.", "target/Test.crt is not writable on the filesystem.")
    }

    @Test
    fun `validate given key file has multiple issues`() {
        keyFile.setWritable(false)
        keyFile.setReadable(false)
        val testSubject = template.copy()

        val errors = testSubject.validate()

        assertThat(errors).containsExactly("target/Test.key is not readable on the filesystem.", "target/Test.key is not writable on the filesystem.")
    }

    @Test
    fun `validate given certificate file does not exist`() {
        certificateFile.delete()
        val testSubject = template.copy(generate = false)

        val errors = testSubject.validate()

        assertThat(errors).containsExactly("target/Test.crt does not exist and will not be generated.")
    }

    @Test
    fun `validate given key file does not exist`() {
        keyFile.delete()
        val testSubject = template.copy(generate = false)

        val errors = testSubject.validate()

        assertThat(errors).containsExactly("target/Test.key does not exist and will not be generated.")
    }

    @Test
    fun `validate given no issuer on a non root certificate`() {
        val testSubject = template.copy(issuer = "")
        val errors = testSubject.validate()

        assertThat(errors).containsExactly("Test certificate must have an issuer.")
    }

    @Test
    fun `validate given no issuer on a root certificate`() {
        val testSubject = template.copy(issuer = "", type = CertificateType.ROOT)
        val errors = testSubject.validate()

        assertThat(errors).isEmpty()
    }

    @Test
    fun `validate given certificate is issuer for itself and is non root certificate`() {
        val testSubject = template.copy(issuer = "Test")
        val errors = testSubject.validate()

        assertThat(errors).containsExactly("Test certificate cannot be an issuer for itself.")
    }

    @Test
    fun `validate given certificate is issuer for itself and is root certificate`() {
        val testSubject = template.copy(issuer = "Test", type = CertificateType.ROOT)
        val errors = testSubject.validate()

        assertThat(errors).isEmpty()
    }

    @Test
    fun `validate given certificate has no name`() {
        val testSubject = template.copy(name = "")
        val errors = testSubject.validate()

        assertThat(errors).containsExactly("Certificate name cannot be blank.")

    }


}