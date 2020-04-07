package com.github.squirrelgrip.cheti.configuration

import java.io.File

data class KeyStoreConfiguration(
    val name: String,
    val type: String = "JKS",
    val password: String = "password",
    val location: String = ".",
    val chains: List<String> = emptyList()
) {
    val keyStoreFile: File = File(location, name)

    fun validate(declaredChains: List<ChainConfiguration>): List<String> {
        val errors = mutableListOf<String>()
        val chainNames = declaredChains.map {
            it.alias
        }
        chains.forEach {
            if (!chainNames.contains(it)) {
                errors.add("Chain contains unknown certificate ${it}.")
            }
        }
        return errors.toList()
    }
}