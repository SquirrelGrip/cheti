package com.github.squirrelgrip.cheti.configuration

import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class KeyStoreConfigurationTest {

    var testSubject = KeyStoreConfiguration("test")

    @Test
    fun password() {
        assertThat(testSubject.password()).isEqualTo("password".toCharArray())
    }

    @Test
    fun `password from environment variable`() {
        val (key, value) = System.getenv().entries.first()
        var copyOfTestSubject = testSubject.copy(
            password = "env:$key"
        )
        assertThat(copyOfTestSubject.password()).isEqualTo(value.toCharArray())
    }

    @Test
    fun `password from file variable`() {
        var copyOfTestSubject = testSubject.copy(
            password = "file:src/test/resources/password.pwd"
        )
        assertThat(copyOfTestSubject.password()).isEqualTo("test password".toCharArray())
    }
}