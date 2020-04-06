package com.github.squirrelgrip.cheti

import com.github.squirrelgrip.extensions.file.toWriter
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import java.io.File
import java.io.FileOutputStream
import java.io.Writer
import java.security.KeyPair
import java.security.KeyStore
import java.security.cert.X509Certificate

fun KeyStore.write(directory: File = rootDir(), name: String = "keystore", extension: String = "p12", password: String = "password") {
    FileOutputStream(File(directory, "$name.$extension")).use { fileOutputStream ->
        this.store(fileOutputStream, password.toCharArray())
    }
}

fun X509Certificate.write(writer: Writer) {
    val pemWriter = PemWriter(writer)
    pemWriter.writeObject(PemObject("CERTIFICATE", this.encoded))
    pemWriter.close()
}

fun KeyPair.write(writer: Writer) {
    val pemWriter = PemWriter(writer)
    pemWriter.writeObject(PemObject("PRIVATE KEY", this.private.encoded))
    pemWriter.close()
}

fun writer(certName: String, extension: String = "crt"): Writer {
    return File(certDir(certName), "$certName.$extension").toWriter()
}

fun certDir(certName: String) = File(rootDir(), certName).apply {
    mkdirs()
}

fun rootDir(name: String = "bouncy-certs") = File(name).apply {
    mkdirs()
}