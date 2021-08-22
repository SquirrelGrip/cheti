package com.github.squirrelgrip.cheti.configuration

import org.bouncycastle.asn1.x509.GeneralName

data class AltSubject(
    val type: AltSubjectType,
    val value: String
) {
    fun toGeneralName() = GeneralName(type.ordinal, value)
}
