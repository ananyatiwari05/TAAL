package org.example.project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class BeatEditorState {

    var grid by mutableStateOf(
        List(8) { MutableList<Int?>(32) { null } }
    )

    var velocityGrid by mutableStateOf(
        List(8) { MutableList<Float>(32) { 1f } }
    )

    var currentStep by mutableStateOf(-1)

    fun placeTile(row: Int, col: Int, tileId: Int?) {
        grid = grid.toMutableList().apply {
            this[row] = this[row].toMutableList().apply {
                this[col] = tileId
            }
        }
    }

    fun assign(row: Int, col: Int, tileId: Int?) {
        placeTile(row, col, tileId)
    }

    fun setVelocity(row: Int, col: Int, velocity: Float) {
        velocityGrid = velocityGrid.toMutableList().apply {
            this[row] = this[row].toMutableList().apply {
                this[col] = velocity
            }
        }
    }

    val guitarGrid = mutableStateListOf<MutableList<String?>>().apply {
        repeat(16) {
            add(mutableStateListOf<String?>().apply {
                repeat(8) { add(null) }
            })
        }
    }

    fun toggleGuitarCell(row: Int, col: Int) {
        val current = guitarGrid[row][col]
        guitarGrid[row][col] = if (current == null) "guitar_note" else null
    }

    fun clearGrid() {
        grid = List(8) { MutableList<Int?>(32) { null } }
        guitarGrid.forEach { row ->
            for (i in row.indices) row[i] = null
        }
        currentStep = -1
    }
}