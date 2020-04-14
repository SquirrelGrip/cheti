package com.github.squirrelgrip.cheti.configuration

data class ChetiConfiguration(
    val common: CommonConfiguration = CommonConfiguration(),
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
        // All certificates must have an issuer and the issuer is declared and a SigningCA type
        return errors.toList()
    }

    fun prepare(): ChetiConfiguration {
        val preparedCertificates = certificates.map {
            it.prepare(common)
        }
        val preparedKeyStores = keystores.map {
            it.prepare(common)
        }
        return this.copy(
            certificates = preparedCertificates,
            keystores = preparedKeyStores
        )
    }
}
