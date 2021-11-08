package com.github.squirrelgrip.cheti.loader

import com.github.squirrelgrip.cheti.model.CertificateKeyPair
import com.github.squirrelgrip.cheti.exception.InvalidConfigurationException
import com.github.squirrelgrip.cheti.configuration.ChainConfiguration

class ChainLoader(
    private val certificateLoader: CertificateLoader
) {
    val chains: MutableMap<String, Array<CertificateKeyPair>> = mutableMapOf()

    fun load(chainConfiguration: ChainConfiguration): Array<CertificateKeyPair> {
        val chain = chainConfiguration.chain.map {
            certificateLoader[it] ?: throw InvalidConfigurationException()
        }.toTypedArray()
        chains[chainConfiguration.alias] = chain
        return chain
    }

    operator fun get(alias: String): Array<CertificateKeyPair>? = chains[alias]

}
