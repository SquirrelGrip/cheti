package com.github.squirrelgrip.cheti.extension

import com.github.squirrelgrip.cheti.exception.InvalidConfigurationException
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ExtensionsKtTest {

    @Test
    fun password() {
        assertThat("pass:password".password()).isEqualTo("password".toCharArray())
        assertThat("".password()).isEqualTo("".toCharArray())
        assertThat("password".password()).isEqualTo("password".toCharArray())
        assertThrows(InvalidConfigurationException::class.java) { "try:password".password() }
    }
}