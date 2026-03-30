package org.example.project

import TileViewModel
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.jetbrains.compose.resources.painterResource
import taal.composeapp.generated.resources.Res
import taal.composeapp.generated.resources.drum
import taal.composeapp.generated.resources.guitar
import taal.composeapp.generated.resources.saxophone

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BeatEditorScreen(
    categories: List<InstrumentCategory>,
    tileViewModel: TileViewModel,
    state: BeatEditorState,
    modifier: Modifier = Modifier,
    currentStep: Int,
    onTileLongPress: (Int, Int) -> Unit
) {

    val horizontalScroll = rememberScrollState()
    var selectedTileId by remember { mutableStateOf<Int?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

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

            val allTiles = categories
                .flatMap { it.tiles }
                .filter { it.beat != null }

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

                Box(
                    modifier = Modifier
                        .size(65.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.DarkGray)
                        .clickable {
                            showAddDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = Color.White, fontSize = 30.sp)
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

                val rowHorizontalScroll = rememberScrollState()
                val rowVerticalScroll = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .verticalScroll(rowVerticalScroll)
                        .horizontalScroll(rowHorizontalScroll)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                rowHorizontalScroll.dispatchRawDelta(-dragAmount.x)
                                rowVerticalScroll.dispatchRawDelta(-dragAmount.y)
                            }
                        }
                ) {

                    Row(
                        modifier = Modifier.height(70.dp),
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
                                            val currentVelocity =
                                                state.velocityGrid[instrumentIndex][stepIndex]
                                            val newVelocity = when {
                                                currentVelocity > 0.75f -> 0.5f
                                                currentVelocity > 0.5f -> 0.25f
                                                else -> 1f
                                            }
                                            state.setVelocity(
                                                instrumentIndex,
                                                stepIndex,
                                                newVelocity
                                            )
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        }
        if (showAddDialog) {
            Dialog(onDismissRequest = { showAddDialog = false }) {

                AddBeatDialog(
                    categories = categories,
                    onSelectTile = { tile ->

                        val category = categories.first {
                            it.tiles.contains(tile)
                        }

                        tileViewModel.addTile(
                            categoryTitle = category.title,
                            baseTile = tile,
                            beat = tile.beat!!
                        )

                        selectedTileId = tile.id
                        showAddDialog = false
                    },
                    onDismiss = { showAddDialog = false }
                )
            }
        }

    }

}
