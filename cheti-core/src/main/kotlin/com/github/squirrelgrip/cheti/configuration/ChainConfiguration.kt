package com.github.squirrelgrip.cheti.configuration

data class ChainConfiguration(
    val alias: String,
    val chain: List<String> = emptyList()
) {
    fun validate(decalredCertificates: List<CertificateConfiguration>): List<String> {
        val errors = mutableListOf<String>()
        val certificateNames = decalredCertificates.map {
            it.name
        }
        chain.forEach {
            if (!certificateNames.contains(it)) {
                errors.add("Keystore contains unknown alias ${it}.")
            }
        }
        return errors.toList()
    }

}
