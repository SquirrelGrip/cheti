package com.github.squirrelgrip.cheti.configuration

import java.io.File


data class CertificateConfiguration(
    val name: String,
    val type: CertificateType,
    val generate: GenerationType = GenerationType.ONCE,
    val location: String,
    val subject: Map<String, String> = emptyMap(),
    val issuer: String = "",
    val extensions: ExtensionsConfiguration
) {
    val certificateFile: File by lazy { File(File(location, name), "$name.crt") }
    val keyFile: File by lazy { File(File(location, name), "$name.key") }

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
            if (issuer.isBlank()) {
                newErrors.add("${name} certificate must have an issuer.")
            } else if (issuer == name) {
                newErrors.add("${name} certificate cannot be an issuer for itself.")
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
            return generate == GenerationType.ALWAYS
        }
        return generate != GenerationType.NEVER
    }

}