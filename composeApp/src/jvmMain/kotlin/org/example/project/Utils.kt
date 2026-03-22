package org.example.project

import java.awt.Desktop
import java.io.File

actual fun openExportsFolder() {
    val dir = File(getExportPath(""))
    if (!dir.exists()) dir.mkdirs()

    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(dir)
    }
}