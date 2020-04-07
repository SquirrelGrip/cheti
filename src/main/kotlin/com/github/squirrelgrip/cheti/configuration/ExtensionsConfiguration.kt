package com.github.squirrelgrip.cheti.configuration

import org.bouncycastle.asn1.x509.Extension

data class ExtensionsConfiguration(
    val altSubject: AltSubjectExtensionConfiguration? = null
) {
    fun getExtensions(): List<Extension> {
        val extensions = mutableListOf<Extension>()

        this.javaClass.declaredFields.forEach {
            if (BaseExtensionConfiguration::class.java.isAssignableFrom(it.type)) {
                val any = it.get(this)
                if (any != null) {
                    val extensionConfiguration = any as BaseExtensionConfiguration<*>
                    extensions.add(extensionConfiguration.toExtension())
                }
            }
        }
        return extensions.toList()
    }
}
