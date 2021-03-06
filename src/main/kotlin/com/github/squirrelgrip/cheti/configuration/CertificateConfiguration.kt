package com.github.squirrelgrip.cheti.configuration

import com.github.squirrelgrip.cheti.extension.toCertificate
import java.io.File
import java.time.Instant
import java.time.Period


data class CertificateConfiguration(
    val name: String,
    val type: CertificateType,
    val generate: GenerationType? = null,
    val location: String? = null,
    val subject: Map<String, String>? = null,
    val issuer: String? = null,
    val validFor: String? = null,
    val extensions: ExtensionsConfiguration? = null
) {
    val certificateFile: File by lazy { File(File(location, name), "$name.crt") }
    val keyFile: File by lazy { File(File(location, name), "$name.key") }
    val validDuration: Period by lazy { Period.parse("P${validFor}") }

    fun prepare(commonConfiguration: CommonConfiguration): CertificateConfiguration {
        return this.copy(
            generate = generate ?: commonConfiguration.generate,
            location = location ?: commonConfiguration.location,
            issuer = issuer ?: commonConfiguration.issuer,
            validFor = validFor ?: commonConfiguration.validFor
        )
    }

    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        if (name.isBlank()) {
            errors.add("Certificate name cannot be blank.")
        }
        errors.addAll(validate(certificateFile))
        errors.addAll(validate(keyFile))
        errors.addAll(validateIssuer())
        return errors
    }

    private fun validateIssuer(): List<String> {
        val newErrors = mutableListOf<String>()
        if (type != CertificateType.ROOT) {
            if (issuer.isNullOrBlank()) {
                newErrors.add("$name certificate must have an issuer.")
            } else if (issuer == name) {
                newErrors.add("$name certificate cannot be an issuer for itself.")
            }
        }
        return newErrors.toList()
    }

    private fun validate(file: File): List<String> {
        val errors = mutableListOf<String>()
        if (file.exists()) {
            if (!file.canRead()) {
                errors.add("$file is not readable on the filesystem.")
            }
            if (generate == GenerationType.ALWAYS && !file.canWrite()) {
                errors.add("$file is not writable on the filesystem.")
            }
        } else {
            if (generate == GenerationType.NEVER) {
                errors.add("$file does not exist and will not be generated.")
            }
        }
        return errors.toList()
    }

    fun shouldCreate(): Boolean {
        if (certificateFile.exists() && keyFile.exists()) {
            if (generate == GenerationType.EXPIRED) {
                val certificate = certificateFile.toCertificate()
                return certificate.notAfter.toInstant().isBefore(Instant.now().plusSeconds(600))
            }
            return generate == GenerationType.ALWAYS
        }
        return generate != GenerationType.NEVER
    }

}