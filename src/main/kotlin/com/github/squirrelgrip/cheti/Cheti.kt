package com.github.squirrelgrip.cheti

import com.github.squirrelgrip.cheti.configuration.ChetiConfiguration
import com.github.squirrelgrip.extensions.file.toReader
import com.github.squirrelgrip.extensions.io.toReader
import com.github.squirrelgrip.extensions.json.toInstance
import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.runtime.RuntimeSingleton
import java.io.*
import java.net.InetAddress


object Cheti {

    fun loadConfiguration(file: File, context: Map<String, String> = emptyMap()): ChetiConfiguration {
        return loadTemplate(file, context).toInstance()
    }

    fun loadConfiguration(fileName: String, context: Map<String, String> = emptyMap()): ChetiConfiguration {
        return loadConfiguration(File(fileName), context)
    }

    fun loadConfiguration(inputStream: InputStream, context: Map<String, String> = emptyMap()): ChetiConfiguration {
        return loadTemplate(inputStream.toReader(), "cheti", context).toInstance()
    }

    private fun validate(chetiConfiguration: ChetiConfiguration) {
        val errors = chetiConfiguration.validate()
        if (!errors.isEmpty()) {
            errors.forEach { println(it) }
            throw InvalidConfigurationException()
        }
    }

    fun execute(chetiConfiguration: ChetiConfiguration) {
        validate(chetiConfiguration)
        val certificateLoader = CertificateLoader(chetiConfiguration.common)
        chetiConfiguration.certificates.forEach {
            certificateLoader.load(it)
        }
        val chainLoader = ChainLoader(certificateLoader)
        chetiConfiguration.chains.forEach {
            chainLoader.load(it)
        }
        val keyStoreLoader = KeyStoreLoader(certificateLoader, chainLoader)
        chetiConfiguration.keystores.forEach {
            keyStoreLoader.load(it)
        }
    }

    private fun loadTemplate(
        file: File,
        context: Map<String, String>
    ): String {
        return loadTemplate(file.toReader(), file.name, context)
    }

    private fun loadTemplate(
        reader: Reader,
        templateName: String,
        context: Map<String, String>
    ): String {
        val runtimeServices = RuntimeSingleton.getRuntimeServices()
        val simpleNode = runtimeServices.parse(reader, templateName)

        val template = Template()
        template.setRuntimeServices(runtimeServices)
        template.data = simpleNode
        template.initDocument()

        val velocityContext = VelocityContext()
        context.forEach { key, value ->
            velocityContext.put(key, value)
        }

        val stringWriter = StringWriter()
        template.merge(velocityContext, stringWriter)
        return stringWriter.toString()
    }

}

fun main(args: Array<String>) {
    val chetiConfiguration = Cheti.loadConfiguration(args[0],
        mapOf(
            "IP_ADDRESS" to getLocalAddress(),
            "HOSTNAME" to getHostName()
        )
    )
    Cheti.execute(chetiConfiguration)
}

fun getLocalAddress() =
    InetAddress.getAllByName(getHostName()).first { it.isSiteLocalAddress }.hostAddress

fun getHostName() =
    InetAddress.getLocalHost().hostName


