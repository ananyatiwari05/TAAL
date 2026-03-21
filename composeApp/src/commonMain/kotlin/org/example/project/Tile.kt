package org.example.project

import androidx.compose.runtime.*

data class Tile(
    val id: Int,
    val instrument: InstrumentType,
    val beat: Beat? = null,
    var isEdited: MutableState<Boolean> = mutableStateOf(false)
)