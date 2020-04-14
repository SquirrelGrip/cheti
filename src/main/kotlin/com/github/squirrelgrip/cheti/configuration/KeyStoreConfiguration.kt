package com.github.squirrelgrip.cheti.configuration

import com.github.squirrelgrip.cheti.exception.InvalidConfigurationException
import java.io.File

data class KeyStoreConfiguration(
    val name: String,
    val type: String = "JKS",
    private val password: String = "pass:password",
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

    fun password(): CharArray {
        val split = password.split(":")
        return when (split[0]) {
            "pass" -> split[1]
            "env" -> System.getenv(split[1])
            "file" -> File(split[1]).readText()
            else -> throw InvalidConfigurationException("Unknown password prefix; should be one of pass:, env: or file:")
        }.toCharArray()
    }
}