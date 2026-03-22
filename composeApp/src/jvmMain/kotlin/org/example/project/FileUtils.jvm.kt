package org.example.project

import java.awt.Desktop
import java.io.File

actual fun getExportPath(fileName: String): String {
    val dir = File(System.getProperty("user.home"), "taal_exports")
    if (!dir.exists()) dir.mkdirs()
    return File(dir, fileName).absolutePath
}

