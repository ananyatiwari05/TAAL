package org.example.project

import TileViewModel
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import org.example.project.ui.theme.*

// --- 1. THE MAIN APP SWITCHER ---
@Composable
@Preview
fun App() {
    // This state variable holds the name of the current screen
    var currentScreen by remember { mutableStateOf("standards") }

    MaterialTheme {
        // Switch based on the current screen state
        when (currentScreen) {
            "standards" -> {
                // If the user clicks a button here, change state to "projects"
                StandardsScreen(
                    onNavigateToProjects = { currentScreen = "projects" }
                )
            }
            "projects" -> {
                // If user clicks New Project -> go to music.
                // If they want to go back -> go to standards.
                ProjectSelectionScreen(
                    onNavigateToMusic = { currentScreen = "music_pad" },
                    onNavigateBack = { currentScreen = "standards" }
                )
            }
            "music_pad" -> {
                // Pass a callback to TopBar so the user can go back
                MusicPadScreen(
                    onNavigateBack = { currentScreen = "projects" }
                )
            }
        }
    }
}

// --- 2. THE MUSIC PAD SCREEN (Updated) ---
@Composable
fun MusicPadScreen(onNavigateBack: () -> Unit) {
    val viewModel = remember { TileViewModel() }

    var showBeatSelector by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedTile by remember { mutableStateOf<Tile?>(null) }

    val beats = remember {
        listOf(
            Beat("b1", "Classic Beat", "drum.wav"),
            Beat("b2", "Rock Beat", "rock.wav"),
            Beat("b3", "Jazz Beat", "jazz.wav")
        )
    }

    val audioPlayer = remember { AudioPlayer() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // Pass the back action down to the TopBar
            TopBar(onBackClick = onNavigateBack)

            Spacer(Modifier.height(16.dp))

            SoundGrid(
                categories = viewModel.categories,
                audioPlayer = audioPlayer,
                modifier = Modifier.weight(1f),
                onLongPress = { categoryTitle, tile ->
                    selectedCategory = categoryTitle
                    selectedTile = tile
                    showBeatSelector = true
                }
            )
        }

        BottomControls(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )

        if (showBeatSelector && selectedTile != null) {
            Dialog(onDismissRequest = { showBeatSelector = false }) {
                BeatSelector(
                    beats = beats,
                    audioPlayer = audioPlayer,
                    onSaveToTile = { beat ->
                        viewModel.assignBeat(
                            selectedCategory!!,
                            selectedTile!!.id,
                            beat
                        )
                        showBeatSelector = false
                    },
                    onCreateNewTile = { beat ->
                        viewModel.addTile(
                            selectedCategory!!,
                            selectedTile!!,
                            beat
                        )
                        showBeatSelector = false
                    },
                    onDismiss = { showBeatSelector = false }
                )
            }
        }
    }
}

// --- 3. THE TOP BAR (Updated) ---
@Composable
fun TopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        // Changed this to a back arrow and hooked up the click
        IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Box(
            modifier = Modifier
                .background(Color.DarkGray, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = "00:00:00",
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
        Row {
            IconButton({}) { Icon(Icons.Default.Mic, null, tint = Color.White) }
            IconButton({}) { Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = "Metronome",
                tint = Color.White
            ) }
            IconButton({}) { Icon(Icons.Default.VolumeUp, null, tint = Color.White) }
            IconButton({}) { Icon(Icons.Default.Menu, null, tint = Color.White) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SoundGrid(
    categories: List<InstrumentCategory>,
    audioPlayer: AudioPlayer,
    modifier: Modifier = Modifier,
    onLongPress: (String, Tile) -> Unit
){

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        items(categories.size) { categoryIndex ->

            val category = categories[categoryIndex]

            Column {

                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyHorizontalGrid(
                    rows = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {

                    items(category.tiles.size) { index ->

                        val tile = category.tiles[index]

                        SoundPad(
                            color = tile.instrument.color,
                            icon = painterResource(tile.instrument.iconRes),
                            onClick = {
                                tile.beat?.let {
                                    audioPlayer.playSound(it.fileName)
                                } ?: audioPlayer.playSound(tile.instrument.name)
                            },
                            onLongPress = {
                                onLongPress(category.title, tile)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SoundPad(
    color: Color,
    icon: Painter,
    onClick: () -> Unit,
    onLongPress: () -> Unit
){
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        label = ""
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .background(color)
            .combinedClickable(
                onClick = {
                    pressed = true
                    onClick()
                },
                onLongClick = {
                    onLongPress()
                }
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(80)
            pressed = false
        }
    }
}

@Composable
fun BottomControls(modifier: Modifier = Modifier) {

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = BottomBarColor,
        tonalElevation = 8.dp
    ) {

        Row(
            modifier = Modifier
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {

            Icon(Icons.Default.GraphicEq, null, tint = Color.White)
            Icon(Icons.Default.Piano, null, tint = Color.White)
        }
    }
}