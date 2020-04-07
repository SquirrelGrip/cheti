package com.github.squirrelgrip.cheti.configuration

import java.security.cert.Certificate

data class ChetiConfiguration(
    val common: CommonConfiguration,
    val certificates: List<CertificateConfiguration>,
    val chains: List<ChainConfiguration> = emptyList(),
    val keystores: List<KeyStoreConfiguration> = emptyList()
) {
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        chains.forEach {
            errors.addAll(it.validate(certificates))
        }
        keystores.forEach {
            errors.addAll(it.validate(chains))
        }
        return errors.toList()
    }
}
