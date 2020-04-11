package com.github.squirrelgrip.cheti.model

import java.security.KeyPair
import java.security.cert.X509Certificate

class CertificateKeyPair(
    val certificate: X509Certificate,
    val keyPair: KeyPair
)