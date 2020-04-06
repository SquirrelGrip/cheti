package com.github.squirrelgrip.cheti.configuration

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x509.Extension

abstract class BaseExtensionConfiguration<T>(
    val critical: Boolean = false,
    val value: T
) {
    abstract fun getObjectIdentifier(): ASN1ObjectIdentifier

    abstract fun getExtensionValue(): ByteArray

    fun toExtension(): Extension {
        return Extension(getObjectIdentifier(), critical, getExtensionValue())
    }

}
