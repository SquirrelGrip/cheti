package com.github.squirrelgrip.cheti.extension

import com.github.squirrelgrip.extensions.file.toInputStream
import com.github.squirrelgrip.extensions.file.toReader
import com.github.squirrelgrip.extensions.file.toWriter
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemReader
import org.bouncycastle.util.io.pem.PemWriter
import java.io.File
import java.io.FileOutputStream
import java.io.Writer
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec

fun KeyStore.write(directory: File = rootDir(), name: String = "keystore.jks", password: String = "password") {
    FileOutputStream(File(directory, "$name")).use { fileOutputStream ->
        this.store(fileOutputStream, password.toCharArray())
    }
}

fun X509Certificate.write(writer: Writer) {
    val pemWriter = PemWriter(writer)
    pemWriter.writeObject(PemObject("CERTIFICATE", this.encoded))
    pemWriter.close()
}

fun X509Certificate.write(file: File) {
    file.parentFile.mkdirs()
    this.write(file.toWriter())
}

fun KeyPair.write(file: File) {
    file.parentFile.mkdirs()
    this.write(file.toWriter())
}

fun KeyPair.write(writer: Writer) {
    val pemWriter = PemWriter(writer)
    pemWriter.writeObject(PemObject("PRIVATE KEY", this.private.encoded))
    pemWriter.close()
}

fun File.toCertificate(): X509Certificate =
    CertificateFactory.getInstance("X.509").generateCertificate(toInputStream()) as X509Certificate

fun File.toPrivateKey(): PrivateKey {
    val encodedKeySpec = PemReader(toReader()).use {
        PKCS8EncodedKeySpec(it.readPemObject().content)
    }
    return KeyFactory.getInstance("RSA").generatePrivate(encodedKeySpec)
}

fun certDir(certName: String) = File(rootDir(), certName).apply {
    mkdirs()
}

fun rootDir(name: String = "target/certs") = File(name).apply {
    mkdirs()
}