package org.example.project

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

actual fun openExportsFolder() {

    val context = AppContextHolder.context

    val dir = File(context?.getExternalFilesDir(null), "taal_exports")

    if (!dir.exists()) dir.mkdirs()

    val uri = FileProvider.getUriForFile(
        context!!,
        context?.packageName + ".provider",
        dir
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "*/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context?.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Install a file manager", Toast.LENGTH_SHORT).show()
    }
}