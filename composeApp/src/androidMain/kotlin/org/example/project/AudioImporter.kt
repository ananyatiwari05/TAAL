package org.example.project

import android.app.Activity
import android.content.Intent
import android.net.Uri

actual class AudioImporter {

    private var callback: ((String) -> Unit)? = null

    actual fun pickAudio(onAudioPicked: (String) -> Unit) {

        val activity = currentActivity ?: return
        callback = onAudioPicked

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)


            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }

        activity.startActivityForResult(intent, 1001)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {

            val uri: Uri = data?.data ?: return


            currentActivity?.contentResolver?.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            callback?.invoke(uri.toString())
        }
    }

    companion object {
        var currentActivity: Activity? = null
    }
}