package org.example.project

val guitarNotesCount = 22

data class GuitarEditorState(
    val rows: Int = guitarNotesCount,
    val cols: Int = 8,
    val grid: MutableList<MutableList<Boolean>> = MutableList(guitarNotesCount) {
        MutableList(8) { false }
    }
) {

    fun deepCopy(): GuitarEditorState {
        return GuitarEditorState(
            rows = rows,
            cols = cols,
            grid = grid.map { it.toMutableList() }.toMutableList()
        )
    }
}