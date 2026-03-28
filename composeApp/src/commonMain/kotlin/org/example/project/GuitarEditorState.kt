package org.example.project

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

val guitarNotesCount = 44
class GuitarEditorState
{
    val rows = guitarNotesCount

    val cols = 16

    var playheadGuitar by mutableStateOf(0)

    var grid by mutableStateOf(List(rows){ MutableList(cols){ false} })

    fun toggle(row: Int, col: Int) {
        grid = grid.toMutableList().apply {
            this[row] = this[row].toMutableList().apply {
                this[col] = !this[col]
            }
        }
    }

    fun clear() {
        grid = List(rows) { MutableList(cols) { false } }
    }

    fun deepCopy(): GuitarEditorState {
        val newState = GuitarEditorState()

        newState.grid = grid.map { row ->
            row.toMutableList()
        }

        newState.playheadGuitar = playheadGuitar

        return newState
    }

}