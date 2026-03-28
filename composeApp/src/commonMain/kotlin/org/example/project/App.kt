package org.example.project


import TileViewModel
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import org.example.project.ui.theme.*
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.example.project.auth.AuthRepository
import taal.composeapp.generated.resources.Res
import kotlin.time.ExperimentalTime
import androidx.compose.runtime.LaunchedEffect




@Composable
fun App(
    audioPlayer: AudioPlayer,
    authRepository: AuthRepository,
    audioImporter: AudioImporter,
    audioExporter: AudioExporter,
    onGoogleSignInClick: () -> Unit,
    settingsManager: SettingsManager
){
    var isLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        AppSettings.userMode = settingsManager.loadMode()
        isLoaded = true
    }

    if (!isLoaded) {
        Text("Loading...")
        return
    }

    var currentScreen by rememberSaveable { mutableStateOf("standards") }
    val beatEditorState = rememberBeatEditorState()
    val tileViewModel = remember { TileViewModel() }
    val metronome = remember { MetronomeEngine() }
    val sequencer = remember { StepSequencer(metronome, audioPlayer) }

    var showAuthScreen by remember { mutableStateOf(false) }
    var isLoggedIn by remember { mutableStateOf(false) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
            Box(Modifier.fillMaxSize()) {
                when (currentScreen) {
                    "standards" -> {
                        StandardsScreen(
                            onNavigateToProjects = { currentScreen = "projects" },
                            settingsManager = settingsManager
                        )
                    }

                    "projects" -> {
                        ProjectSelectionScreen(
                            tileViewModel = tileViewModel,
                            audioPlayer = audioPlayer,
                            onNavigateToMusic = { currentScreen = "music_pad" },
                            onNavigateBack = { currentScreen = "standards" }
                        )
                    }

                    "music_pad" -> {
                        MusicPadScreen(
                            state = beatEditorState,
                            onNavigateBack = { currentScreen = "projects" },
                            audioImporter = audioImporter,
                            tileViewModel = tileViewModel,
                            audioPlayer = audioPlayer,
                            metronome = metronome,
                            sequencer = sequencer,
                            onProfileClick = { showAuthScreen = true },
                            audioExporter = audioExporter
                        )
                    }
                }

                if (showAuthScreen) {
                    Dialog(onDismissRequest = { showAuthScreen = false }) {
                        LoginSignupScreen(
                            authRepository = authRepository,
                            onGoogleSignInClick = onGoogleSignInClick,
                            onLoginSuccess = {
                                isLoggedIn = true
                                showAuthScreen = false
                            }
                        )
                    }
                }
            }
        }
    }


}

@Composable
@OptIn(ExperimentalTime::class)
fun MusicPadScreen(
    state: BeatEditorState,
    onNavigateBack: () -> Unit,
    audioImporter: AudioImporter,
    tileViewModel: TileViewModel,
    audioPlayer: AudioPlayer,
    metronome: MetronomeEngine,
    sequencer: StepSequencer,
    onProfileClick: () -> Unit,
    audioExporter :AudioExporter
){
    var showAudioEditor by remember { mutableStateOf(false) }
    var drumEditorState by remember { mutableStateOf<DrumEditorState?>(null) }
    var pianoEditorState by remember { mutableStateOf<PianoEditorState?>(null) }
    var metronomeRunning by remember { mutableStateOf(false) }
    var isEditorMode by remember { mutableStateOf(false) }
    var showBeatSelector by remember { mutableStateOf(false) }
    var showPianoEditor by remember { mutableStateOf(false) }
    var showDrumEditor by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedTile by remember { mutableStateOf<Tile?>(null) }
    var showRecorder by remember { mutableStateOf(false) }
    val recorder = remember { VoiceRecorder() }
    val currentStep by metronome.step.collectAsState()
    var swing by remember { mutableStateOf(0f) }
    var swingEnabled by remember { mutableStateOf(false) }
    var showGuitarEditor by remember { mutableStateOf(false) }
    var guitarEditorState by remember { mutableStateOf(GuitarEditorState()) }
    var showAbout by remember { mutableStateOf(false) }

    var showSaveDialog by remember { mutableStateOf(false) }

    val onExportClick= {
        showSaveDialog = true
    }


    val beats = remember {
        listOf(
            Beat("b1", "Classic Beat", "drum.wav"),
            Beat("b2", "Rock Beat", "rock.wav"),
            Beat("b3", "Jazz Beat", "jazz.wav")
        )
    }

    fun addTileToBeatEditor(
        state: BeatEditorState,
        tileViewModel: TileViewModel,
        selectedCategory: String,
        tileId: Int,
        step: Int
    ) {
        val instrumentIndex = tileViewModel.categories.indexOfFirst {
            it.title == selectedCategory
        }
        if (instrumentIndex == -1) return ;

        val stepIndex = state.grid[instrumentIndex].indexOfFirst { it == null }
        val finalStep = if (stepIndex == -1) currentStep else stepIndex
        state.placeTile(instrumentIndex, finalStep, tileId)
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            TopBar(
                onBackClick = onNavigateBack,
                metronome = metronome,
                onProfileClick = onProfileClick,
                metronomeRunning = metronomeRunning,
                onToggleMetronome = {
                    metronomeRunning = !metronomeRunning
                    if (metronomeRunning) {
                        metronome.start()
                        sequencer.start()
                    } else {
                        metronome.stop()
                    }
                },
                audioPlayer = audioPlayer,
                onMicClick = {
                    showRecorder = true
                },
                swing = swing,
                onSwingChange = {
                    swing = it
                    metronome.swing = it
                },
                onExportClick = onExportClick,
                onInfoClick = { showAbout = true }
            )
            Spacer(Modifier.height(16.dp))

            key(isEditorMode) {
                if (isEditorMode) {
                    SoundGrid(
                        categories = tileViewModel.categories,
                        audioPlayer = audioPlayer,
                        modifier = Modifier.fillMaxSize(),
                        metronome = metronome,
                        sequencer = sequencer,
                        onLongPress = { categoryTitle, tile ->

                            if (AppSettings.userMode == UserMode.BEGINNER) return@SoundGrid

                            selectedCategory = categoryTitle
                            selectedTile = tile

                            if (tile.instrument.name == "piano") {
                                pianoEditorState = tile.beat?.pianoPattern ?: PianoEditorState()
                                showPianoEditor = true
                            } else if (tile.instrument.name == "drum") {
                                drumEditorState = tile.beat?.drumPattern ?: DrumEditorState()
                                showDrumEditor = true
                            }
                            else if (tile.instrument.name == "guitar") {
                                guitarEditorState = tile.beat?.guitarPattern ?: GuitarEditorState()
                                showGuitarEditor = true
                            }

                            else {
                                showBeatSelector = true
                            }
                        }
                    )
                } else {
                    BeatEditorScreen(
                        categories = tileViewModel.categories,
                        tileViewModel = tileViewModel,
                        state = state,
                        currentStep = currentStep,
                        modifier = Modifier.fillMaxSize(),
                        onTileLongPress = { instrumentIndex, stepIndex ->
                            val category = tileViewModel.categories[instrumentIndex]
                            val tile = category.tiles.firstOrNull()
                            selectedCategory = category.title
                            selectedTile = tile
                            if (tile?.instrument?.name == "piano") {
                                showPianoEditor = true
                            } else {
                                showAudioEditor = true
                            }
                        }
                    )
                }

            }
        }

        BottomControls(
            isEditorMode = isEditorMode,
            onToggle = { isEditorMode = !isEditorMode },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
        )

        if (showBeatSelector && selectedTile != null) {
            Dialog(onDismissRequest = { showBeatSelector = false }) {
                Column(modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp)) {
                    BeatSelector(
                        beats = beats,
                        audioPlayer = audioPlayer,
                        onSaveToTile = { beat ->
                            tileViewModel.assignBeat(selectedCategory!!, selectedTile!!.id, beat)
                            addTileToBeatEditor(
                                state,
                                tileViewModel,
                                selectedCategory!!,
                                selectedTile!!.id,
                                currentStep
                            )
                            showBeatSelector = false
                            selectedTile!!.isEdited.value = true
                        },
                        onCreateNewTile = { beat ->
                            tileViewModel.addTile(selectedCategory!!, selectedTile!!, beat)
                            selectedTile!!.isEdited.value = true
                            showBeatSelector = false
                        },
                        onImportAudio = {
                            audioImporter.pickAudio { path ->
                                tileViewModel.assignBeat(selectedCategory!!, selectedTile!!.id, Beat("imported", "Imported Audio", path))
                                showBeatSelector = false
                            }
                        },
                        onDismiss = { showBeatSelector = false }
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

        if (showPianoEditor && selectedTile != null) {
            Dialog(onDismissRequest = { showPianoEditor = false }) {
                Box(modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.8f)) {
                    PianoRollEditor(
                        state = pianoEditorState ?: PianoEditorState(),
                        audioPlayer = audioPlayer,
                        onSave = {
                            tileViewModel.assignBeat(selectedCategory!!, selectedTile!!.id, Beat(
                                    id = "piano_${Clock.System.now().toEpochMilliseconds()}",
                                    name = "Piano Pattern",
                                    pianoPattern = pianoEditorState
                                )
                            )
                            addTileToBeatEditor(
                                state,
                                tileViewModel,
                                selectedCategory!!,
                                selectedTile!!.id,
                                currentStep
                            )
                            showPianoEditor = false
                            selectedTile!!.isEdited.value = true
                        },
                        onClose = { showPianoEditor = false }
                    )
                }
            }
        }

        if (showDrumEditor && selectedTile != null && drumEditorState != null) {
            Dialog(onDismissRequest = { showDrumEditor = false }) {
                Column {
                    DrumBeatEditor(
                        state = drumEditorState!!,
                        audioPlayer = audioPlayer,
                        onSave = {
                            tileViewModel.assignBeat(
                                selectedCategory!!,
                                selectedTile!!.id,
                                Beat(
                                    id = "custom_${Clock.System.now().toEpochMilliseconds()}",
                                    name = "Custom Beat",
                                    drumPattern = drumEditorState!!
                                )
                            )
                            addTileToBeatEditor(
                                state,
                                tileViewModel,
                                selectedCategory!!,
                                selectedTile!!.id,
                                currentStep
                            )
                            showDrumEditor = false
                            selectedTile!!.isEdited.value = true
                        },
                        onClose = { showDrumEditor = false }
                    )
                }
            }
        }

        if (showGuitarEditor && selectedTile != null) {
            Dialog(onDismissRequest = { showGuitarEditor = false }) {

                GuitarBeatsEditor(
                    audioPlayer = audioPlayer,
                    state = guitarEditorState,

                    onSave = { updatedState ->

                        guitarEditorState = updatedState

                        tileViewModel.assignBeat(
                            selectedCategory!!,
                            selectedTile!!.id,
                            Beat(
                                id = "guitar_${Clock.System.now().toEpochMilliseconds()}",
                                name = "Guitar Pattern",
                                guitarPattern = updatedState
                            )
                        )


                        addTileToBeatEditor(
                            state,
                            tileViewModel,
                            selectedCategory!!,
                            selectedTile!!.id,
                            currentStep
                        )

                        showGuitarEditor = false
                        selectedTile!!.isEdited.value = true
                    },

                    onClose = { showGuitarEditor = false }
                )
            }
        }

        if (showAudioEditor && selectedTile != null) {
            Dialog(onDismissRequest = { showAudioEditor = false }) {
                AudioEditor(
                    fileName = selectedTile!!.beat?.fileName ?: selectedTile!!.instrument.name,
                    onClose = { showAudioEditor = false },
                    onSave = { path ->

                        if (selectedTile != null && selectedCategory != null) {

                            tileViewModel.assignBeat(
                                selectedCategory!!,
                                selectedTile!!.id,
                                Beat("edited", "Edited Audio", path)
                            )
                            addTileToBeatEditor(
                                state,
                                tileViewModel,
                                selectedCategory!!,
                                selectedTile!!.id,
                                currentStep
                            )
                        }

                        showAudioEditor = false
                    }
                )
            }
        }
        if (showRecorder) {
            VoiceRecorderDialog(
                recorder = recorder,
                onDismiss = { showRecorder = false },
                onSave = { path ->
                    tileViewModel.recordedAudios.add(path)

                    tileViewModel.assignBeat(
                        selectedCategory ?: "Default",
                        selectedTile?.id ?: return@VoiceRecorderDialog,
                        Beat("recorded", "Voice Recording", path)
                    )
                    addTileToBeatEditor(
                        state,
                        tileViewModel,
                        selectedCategory ?: "Default",
                        selectedTile!!.id,
                        currentStep
                    )
                    showRecorder = false
                }
            )
        }

        if (showSaveDialog) {
            Dialog(onDismissRequest = { showSaveDialog = false }) {
                Column(
                    modifier = Modifier
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {

                    Text("Export Beat", color = Color.White, fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val fileName = "beat_${Clock.System.now().toEpochMilliseconds()}.wav"

                            audioExporter.exportBeat(
                                state,
                                tileViewModel.categories,
                                metronome.bpm,
                                fileName
                            )

                            showSaveDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Export as WAV")
                    }

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val fileName = "midi_${Clock.System.now().toEpochMilliseconds()}.mid"

                            audioExporter.exportMidi(
                                state,
                                tileViewModel.categories,
                                metronome.bpm,
                                fileName
                            )

                            showSaveDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Export as MIDI")
                    }
                }
            }
        }

        LaunchedEffect(currentStep) {

            tileViewModel.categories.forEachIndexed { instrumentIndex, category ->

                val tileId = state.grid[instrumentIndex][currentStep] ?: return@forEachIndexed

                val tile = category.tiles.find { it.id == tileId } ?: return@forEachIndexed
                val beat = tile.beat

                when {

                    beat?.guitarPattern != null -> {
                        sequencer.addGuitarPattern(beat.guitarPattern)
                    }

                    beat?.drumPattern != null -> {
                        sequencer.addDrumPattern(beat.drumPattern)
                    }

                    beat?.pianoPattern != null -> {
                        sequencer.addPianoPattern(beat.pianoPattern)
                    }

                    beat?.fileName != null -> {

                        val file = beat.fileName

                        if (file.startsWith("/") || file.startsWith("content://")) {
                            audioPlayer.playImported(file)
                        } else {
                            audioPlayer.playSound(file)
                        }
                    }
                }
            }
        }
        if (showAbout) {
            AboutPage(
                onClose = { showAbout = false }
            )
        }
    }
}


@Composable
fun TopBar(
    onBackClick: () -> Unit,
    metronome: MetronomeEngine,
    onProfileClick: () -> Unit,
    metronomeRunning: Boolean,
    onToggleMetronome: () -> Unit,
    audioPlayer: AudioPlayer,
    onMicClick: () -> Unit,
    swing: Float,
    onSwingChange: (Float) -> Unit,
    onExportClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val elapsedTime by metronome.elapsedTime.collectAsState()

    var showVolumeSlider by remember { mutableStateOf(false) }
    var volume by remember { mutableStateOf(1f) }
    var showAbout by remember { mutableStateOf(false) }

    Column {

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .background(Color.DarkGray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = formatTime(elapsedTime),
                    color = Color.White
                )
            }

            Row(
                modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (AppSettings.userMode != UserMode.BEGINNER) {
                    IconButton(onClick = onMicClick) {
                        Icon(Icons.Default.Mic, null, tint = Color.White)
                    }
                }

                IconButton(onClick = onToggleMetronome) {
                    Icon(
                        Icons.Default.Speed,
                        "Metronome",
                        tint = if (metronomeRunning) Color.Green else Color.White
                    )
                }


                IconButton(onClick = {
                    showVolumeSlider = !showVolumeSlider
                }) {
                    Icon(Icons.Default.VolumeUp, "Volume", tint = Color.White)
                }

                if (AppSettings.userMode != UserMode.BEGINNER) {
                    IconButton(onClick = onExportClick) {
                        Icon(Icons.Default.Save, "Export", tint = Color.White)
                    }
                }

                IconButton(onClick = {
                    openExportsFolder()
                }) {
                    Icon(Icons.Default.Folder, contentDescription = "Open Exports", tint = Color.White)
                }


                IconButton(onClick = { onInfoClick() } ) {
                    Icon(Icons.Default.Info, null, tint = Color.White)
                }

                IconButton(onClick = onProfileClick) {
                    Icon(
                        Icons.Filled.AccountCircle,
                        "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }


        if (showVolumeSlider) {
            androidx.compose.material3.Slider(
                value = volume,
                onValueChange = {
                    volume = it
                    audioPlayer.setVolume(it)
                },
                valueRange = 0f..1f,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }


        if (AppSettings.userMode == UserMode.ADVANCED) {

            Text("Swing", color = Color.White)

            Slider(
                value = swing,
                onValueChange = onSwingChange,
                valueRange = 0f..0.5f,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SoundGrid(
    categories: List<InstrumentCategory>,
    audioPlayer: AudioPlayer,
    metronome: MetronomeEngine,
    sequencer: StepSequencer,
    modifier: Modifier = Modifier,
    onLongPress: (String, Tile) -> Unit
){
    var activeTiles by remember { mutableStateOf(setOf<Int>()) }
    val currentStep by metronome.step.collectAsState()
    val scope = rememberCoroutineScope()

    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(24.dp)) {

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
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                ) {

                    items(category.tiles.size) { index ->
                        val tile = category.tiles[index]
                        val column = index / 2

                        SoundPad(
                            color = tile.instrument.color,
                            icon = painterResource(tile.instrument.iconRes),
                            isActive = tile.id in activeTiles,
                            isPlayhead = column == currentStep,
                            isEdited = tile.isEdited.value,
                            onClick = {

                                activeTiles = if (tile.id in activeTiles) {
                                    activeTiles - tile.id
                                } else {
                                    activeTiles + tile.id
                                }
                                val beat = tile.beat

                                scope.launch {

                                    if (beat?.guitarPattern != null) {

                                        val pattern = beat.guitarPattern

                                        pattern.grid.forEachIndexed { row, cols ->

                                            cols.forEachIndexed { col, isActive ->

                                                if (isActive) {
                                                    val note = audioPlayer.getGuitarNoteByIndex(row)
                                                    audioPlayer.playSound(note)
                                                }
                                            }
                                        }

                                    } else if (beat?.pianoPattern != null) {
                                        sequencer.addPianoPattern(beat.pianoPattern)

                                    } else if (beat?.drumPattern != null) {
                                        sequencer.addDrumPattern(beat.drumPattern)

                                    } else if (beat?.fileName != null) {

                                        val file = beat.fileName

                                        if (file.startsWith("content://") || file.startsWith("/")) {
                                            audioPlayer.playImported(file)
                                        } else {
                                            audioPlayer.playSound(file)
                                        }

                                    } else {

                                        when (tile.instrument.name) {
                                            "drum" -> audioPlayer.playSound("kick")
                                            "piano" -> audioPlayer.playSound("piano_c4")
                                            "harmonium" -> audioPlayer.playSound("piano_c3")
                                            "violin" -> audioPlayer.playSound("piano_g4")
                                        }
                                    }
                                }
                            },
                            onLongPress = { onLongPress(category.title, tile) },
                                    stepIndex = column
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
    isActive: Boolean,
    isPlayhead: Boolean,
    isEdited: Boolean,
    stepIndex: Int,
    onClick: () -> Unit,
    onLongPress: () -> Unit
){
    var pressed by remember { mutableStateOf(false) }
    var beatOn by remember { mutableStateOf(isEdited) }

    val animatedColor by animateColorAsState(
        if (isActive) Color.White else color,
        label = ""
    )

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        label = ""
    )
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(20.dp))
            .background(
                when {
                    isPlayhead -> Color.White.copy(alpha = 0.25f)
                    isActive -> animatedColor
                    isEdited -> PaleGray
                    else -> color
                }
            )
            .border(
                if (stepIndex % 4 == 0) 2.dp else 0.dp,
                Color.White,
                RoundedCornerShape(12.dp)
            )
            .combinedClickable(
                onClick = { pressed = true; onClick() },
                onLongClick = { onLongPress() }
            )

            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()

                        if (event.buttons.isSecondaryPressed) {
                            onLongPress()
                        }
                    }
                }
            }
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
            delay(150)
            pressed = false
        }
    }
}

@Composable
fun BottomControls(
    isEditorMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = BottomBarColor,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {

            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = if (!isEditorMode) Color.White else Color.Gray,
                modifier = Modifier.clickable { onToggle() }
            )

            Icon(
                imageVector = Icons.Default.Piano,
                contentDescription = null,
                tint = if (isEditorMode) Color.White else Color.Gray,
                modifier = Modifier.clickable { onToggle() }
            )
        }
    }
}

fun formatTime(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60

    val h = hrs.toString().padStart(2, '0')
    val m = mins.toString().padStart(2, '0')
    val s = secs.toString().padStart(2, '0')

    return "$h:$m:$s"
}

@Composable
fun VoiceRecorderDialog(
    recorder: VoiceRecorder,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {

    var isRecording by remember { mutableStateOf(false) }
    var recordedPath by remember { mutableStateOf<String?>(null) }
    var seconds by remember { mutableStateOf(0) }


    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (true) {
                delay(1000)
                seconds++
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {

        Column(
            modifier = Modifier
                .background(Color(0xFF1E1E1E), RoundedCornerShape(16.dp))
                .padding(20.dp)
                .fillMaxWidth()
        ) {


            Text(
                "Voice Recorder",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))


            Row(verticalAlignment = Alignment.CenterVertically) {

                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.Red, RoundedCornerShape(50))
                    )
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = formatTimer(seconds),
                    color = Color.White
                )
            }

            Spacer(Modifier.height(20.dp))


            Button(
                onClick = {
                    if (!isRecording) {
                        seconds = 0
                        recorder.startRecording()
                        isRecording = true
                    } else {
                        recordedPath = recorder.stopRecording()
                        isRecording = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color.Red else Color(0xFF4CAF50)
                )
            ) {
                Text(
                    if (isRecording) "Stop Recording" else "Start Recording",
                    color = Color.White
                )
            }

            Spacer(Modifier.height(16.dp))


            recordedPath?.let { path ->

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Button(
                        onClick = { recorder.playRecording(path) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Play")
                    }

                    Button(
                        onClick = { onSave(path) },
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Text("Save")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))


            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color.DarkGray
                )
            ) {
                Text("Close", color = Color.White)
            }
        }
    }
}
fun formatTimer(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}

@Composable
fun RecordingsList(
    tileViewModel: TileViewModel,
    audioPlayer: AudioPlayer
) {
    LazyColumn {

        items(tileViewModel.recordedAudios.size) { index ->

            val path = tileViewModel.recordedAudios[index]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .background(Color(0xFF2A2A2A), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    Text(
                        text = "Recording ${index + 1}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = path.substringAfterLast("/"),
                        color = Color.Gray,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                }

                Row {

                    IconButton(onClick = {
                        audioPlayer.playImported(path)
                    }) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.White)
                    }

                    IconButton(onClick = {
                        tileViewModel.recordedAudios.removeAt(index)
                    }) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red)
                    }
                }
            }
        }
    }


}