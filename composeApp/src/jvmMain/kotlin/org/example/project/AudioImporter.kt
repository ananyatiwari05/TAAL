package org.example.project

actual class AudioImporter {

    actual fun pickAudio(onAudioPicked: (String) -> Unit) {

        val chooser = javax.swing.JFileChooser()

        chooser.fileFilter = javax.swing.filechooser.FileNameExtensionFilter(
            "WAV Audio Files",
            "wav"
        )

        val result = chooser.showOpenDialog(null)

        if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
            val originalFile = chooser.selectedFile

            val appDir = java.io.File(System.getProperty("user.home"), "TAAL")
            if (!appDir.exists()) appDir.mkdirs()

            val newFile = java.io.File(
                appDir,
                "imported_${System.currentTimeMillis()}.wav"
            )

            originalFile.copyTo(newFile, overwrite = true)

            onAudioPicked(newFile.absolutePath)
        }

    }
}