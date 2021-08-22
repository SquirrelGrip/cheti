package com.github.squirrelgrip.cheti.template

import com.github.squirrelgrip.extension.file.toReader
import java.io.File
import java.io.Reader

interface TemplateProvider {
    fun loadTemplate(file: File, context: Map<String, String>): String =
        loadTemplate(file, file.name, context)

    fun loadTemplate(file: File, templateName: String, context: Map<String, String>): String =
        loadTemplate(file.toReader(), templateName, context)

    fun loadTemplate(reader: Reader, templateName: String, context: Map<String, String>): String
}