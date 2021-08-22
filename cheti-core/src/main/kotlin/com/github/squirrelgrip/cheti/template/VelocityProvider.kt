package com.github.squirrelgrip.cheti.template

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.runtime.RuntimeSingleton
import org.apache.velocity.runtime.log.NullLogChute
import java.io.Reader
import java.io.StringWriter


class VelocityProvider: TemplateProvider {
    override fun loadTemplate(
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