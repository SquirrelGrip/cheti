package com.github.squirrelgrip.cheti.loader

import com.github.squirrelgrip.cheti.configuration.KeyStoreConfiguration
import com.github.squirrelgrip.extensions.file.toOutputStream
import java.io.File
import java.security.KeyStore
import java.security.PrivateKey

class KeyStoreLoader(
    val certificateLoader: CertificateLoader,
    val chainLoader: ChainLoader
) {
    fun load(keyStoreConfiguration: KeyStoreConfiguration): KeyStore {
        return KeyStore.getInstance(keyStoreConfiguration.type).apply {
            this.load(null, null)
            keyStoreConfiguration.chains.forEach {alias ->
                val chainCertificateKeyPair = chainLoader[alias]
                if (chainCertificateKeyPair != null && chainCertificateKeyPair.isNotEmpty()) {
                    val chain = chainCertificateKeyPair.map { it.certificate }.toTypedArray()
                    val privateKey: PrivateKey = chainCertificateKeyPair.first().keyPair.private
                    this.setKeyEntry(alias, privateKey, keyStoreConfiguration.password(), chain)
                }
            }
            store(keyStoreConfiguration.keyStoreFile.toOutputStream(), keyStoreConfiguration.password())
            File("${keyStoreConfiguration.keyStoreFile.absolutePath}.pwd").writeText(keyStoreConfiguration.password().toString())
        }
    }

}
