package org.example.project

import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.DrawableResource

data class Instruments(
    val name: String,
    val color: Color,
    val iconRes: DrawableResource
)
