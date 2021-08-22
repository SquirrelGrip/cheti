package com.github.squirrelgrip.cheti.configuration

data class CommonConfiguration(
    val location: String = "certs",
    val generate: GenerationType = GenerationType.ONCE,
    val issuer: String = "SigningCA",
    val validFor: String = "1D",
    val password: String = "pass:password"
)
