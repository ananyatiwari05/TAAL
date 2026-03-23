package org.example.project

expect class AudioExporter {
    fun exportBeat(
        state: BeatEditorState,
        categories: List<InstrumentCategory>,
        bpm: Int,
        outputPath: String
    )
    fun exportMidi(
        state: BeatEditorState,
        categories: List<InstrumentCategory>,
        bpm: Int,
        outputPath: String
    )



}