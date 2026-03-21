package org.example.project

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class BeatEditorState {

    var grid by mutableStateOf(
        List(8) { MutableList<Int?>(32) { null } }
    )

    var velocityGrid by mutableStateOf(
        List(8) { MutableList<Float>(32) { 1f } }
    )

    var currentStep by mutableStateOf(0)

    fun assign(row: Int, col: Int, tileId: Int?) {
        grid = grid.toMutableList().apply {
            this[row] = this[row].toMutableList().apply {
                this[col] = tileId
            }
        }
    }
    fun placeTile(row: Int, col: Int, tileId: Int) {
        grid = grid.toMutableList().apply {
            this[row] = this[row].toMutableList().apply {
                this[col] = tileId
            }
        }
    }

    fun setVelocity(row: Int, col: Int, velocity: Float) {
        velocityGrid = velocityGrid.toMutableList().apply {
            this[row] = this[row].toMutableList().apply {
                this[col] = velocity
            }
        }
    }
}