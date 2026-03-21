package org.example.project

actual class AudioExporter {
    actual fun exportBeat(
        state: BeatEditorState,
        categories: List<InstrumentCategory>,
        bpm: Int,
        outputPath: String
    ) {
    }
}