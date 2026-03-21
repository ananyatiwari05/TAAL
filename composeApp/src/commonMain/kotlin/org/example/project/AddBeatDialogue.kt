package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource

@Composable
fun AddBeatDialog(
    categories: List<InstrumentCategory>,
    onSelectTile: (Tile) -> Unit,
    onDismiss: () -> Unit
) {

    val instruments = categories.map { it.title }
    var selectedCategory by remember { mutableStateOf<InstrumentCategory?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .fillMaxHeight(0.6f)
            .background(Color.DarkGray, RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Row (
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Type",
                color = Color.White,
                fontSize = 20.sp
            )
            IconButton(onClick = { onDismiss() }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedCategory == null) {
                items(categories.size) { index ->
                    val category = categories[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCategory = category
                            }
                            .background(category.tiles.first().instrument.color, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Image(
                            painter = painterResource(category.tiles.first().instrument.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = category.title,
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            else {

                val tilesWithBeats = selectedCategory!!.tiles.filter { it.beat != null }

                items(tilesWithBeats.size) { index ->

                    val tile = tilesWithBeats[index]

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectTile(tile)
                            }
                            .background(Color(0xFF2A2A2A), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Beat $index",
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                val beat = tile.beat
                                if (beat?.fileName != null) {
                                    //audioPlayer.playSound(beat.fileName)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "← Back",
                        color = Color.White,
                        modifier = Modifier
                            .clickable { selectedCategory = null }
                            .padding(8.dp)
                    )
                }
            }
        }

    }
}