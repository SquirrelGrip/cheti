package com.github.squirrelgrip.cheti.configuration

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.GeneralNames

class AltSubjectExtensionConfiguration(
    critical: Boolean = false,
    value: Array<AltSubject> = emptyArray()
) : BaseExtensionConfiguration<Array<AltSubject>>(
    critical,
    value
) {
    override fun getObjectIdentifier(): ASN1ObjectIdentifier {
        return Extension.subjectAlternativeName
    }

    override fun getExtensionValue(): ByteArray {
        return GeneralNames(value.map { it.toGeneralName() }.toTypedArray()).encoded
    }

}
