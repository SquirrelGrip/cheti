package com.github.squirrelgrip.cheti.configuration

import com.github.squirrelgrip.cheti.exception.InvalidConfigurationException
import com.github.squirrelgrip.cheti.extension.password
import java.io.File

data class KeyStoreConfiguration(
    val name: String,
    val type: String = "JKS",
    val password: String?,
    val location: String?,
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
                errors.add("Chain contains unknown certificate $it.")
            }
        }
        return errors.toList()
    }

    fun password() = password!!.password()

    fun prepare(common: CommonConfiguration):KeyStoreConfiguration {
        return this.copy(
            password = password ?: common.password,
            location = location ?: common.location
        )
    }
}