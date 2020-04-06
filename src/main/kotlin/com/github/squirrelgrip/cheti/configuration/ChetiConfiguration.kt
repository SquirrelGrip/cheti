package com.github.squirrelgrip.cheti.configuration

import java.security.cert.Certificate

data class ChetiConfiguration(
    val common: CommonConfiguration,
    val certificates: List<CertificateConfiguration>
) {
    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        return errors.toList()
    }
}
