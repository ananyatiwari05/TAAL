package org.example.project

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import taal.composeapp.generated.resources.Res
import taal.composeapp.generated.resources.drum
import taal.composeapp.generated.resources.guitar
import taal.composeapp.generated.resources.saxophone

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BeatEditorScreen(
    categories: List<InstrumentCategory>,
    state: BeatEditorState,
    modifier: Modifier = Modifier,
    currentStep: Int,
    onTileLongPress: (Int, Int) -> Unit
) {

    val horizontalScroll = rememberScrollState()
    var selectedTileId by remember { mutableStateOf<Int?>(null) }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {

        LazyColumn(
            modifier = Modifier
                .width(90.dp)
                .fillMaxHeight()
                .background(Color(0xFF2F2F2F), RoundedCornerShape(16.dp))
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val allTiles = categories.flatMap { it.tiles }

            items(allTiles.size) { index ->

                val tile = allTiles[index]

                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selectedTileId == tile.id)
                                Color.White
                            else
                                tile.instrument.color
                        )
                        .clickable {
                            selectedTileId = tile.id
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(tile.instrument.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFF3A3A3A), RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            itemsIndexed(categories) { instrumentIndex, category ->

                val instrument = category.tiles.first().instrument

                Row(
                    modifier = Modifier
                        .horizontalScroll(horizontalScroll)
                        .height(70.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    repeat(32) { stepIndex ->

                        val tileId = state.grid[instrumentIndex][stepIndex]
                        val active = tileId != null
                        val isPlayingStep = stepIndex == currentStep

                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isPlayingStep -> Color.White.copy(alpha = 0.3f)
                                        active -> instrument.color
                                        else -> Color(0xFF555555)
                                    }
                                )
                                .combinedClickable(
                                    onClick = {
                                        state.assign(instrumentIndex, stepIndex, selectedTileId)
                                    },
                                    onLongClick = {
                                        val currentVelocity = state.velocityGrid[instrumentIndex][stepIndex]
                                        val newVelocity = when {
                                            currentVelocity > 0.75f -> 0.5f
                                            currentVelocity > 0.5f -> 0.25f
                                            else -> 1f
                                        }
                                        state.setVelocity(instrumentIndex, stepIndex, newVelocity)
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun BeatEditorScreenPreview() {

    val instruments = listOf(
        InstrumentCategory(
            "Drums",
            listOf(
                Tile(
                    id = 0,
                    InstrumentType(
                        name = "drum",
                        color = Color.Red,
                        iconRes = Res.drawable.drum
                    )
                )
            )
        ),
        InstrumentCategory(
            "Guitars",
            listOf(
                Tile(
                    id = 1,
                    InstrumentType(
                        name = "guitar",
                        color = Color(0xFFFF9800),
                        iconRes = Res.drawable.guitar
                    )
                )
            )
        ),
        InstrumentCategory(
            "Sax",
            listOf(
                Tile(
                    id = 2,
                    InstrumentType(
                        name = "sax",
                        color = Color.Yellow,
                        iconRes = Res.drawable.saxophone
                    )
                )
            )
        )
    )

    val state = rememberBeatEditorState()

    BeatEditorScreen(
        categories = instruments,
        state = state,
        currentStep = 0,
        onTileLongPress = { _, _ -> }
    )
}