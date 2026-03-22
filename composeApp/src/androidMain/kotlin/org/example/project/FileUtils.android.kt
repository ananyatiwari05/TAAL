package org.example.project

import java.io.File

actual fun getExportPath(fileName: String): String {

    val context = AppContextHolder.context

    val dir = File(context?.getExternalFilesDir(null), "taal_exports")

    if (!dir.exists()) dir.mkdirs()

    return File(dir, fileName).absolutePath
}