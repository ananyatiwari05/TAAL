package org.example.project

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
class MetronomeEngine {

    private var job: Job? = null
    private var timerJob: Job? = null
    private var startMark = TimeSource.Monotonic.markNow()

    var bpm = 120
    var swing = 0f

    private val _step = MutableStateFlow(0)
    val step: StateFlow<Int> = _step

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime

    fun start() {

        if (job != null) return

        _step.value = 0
        startMark = TimeSource.Monotonic.markNow()

        val scope = CoroutineScope(Dispatchers.Default)

        job = scope.launch {

            val baseStepNs = 60_000_000_000L / (bpm * 4)
            var nextTick = TimeSource.Monotonic.markNow()

            while (isActive) {

                val step = _step.value

                val isSwingStep = step % 2 == 1

                val swingOffset = if (isSwingStep) {
                    (baseStepNs * swing).toLong()
                } else {
                    0L
                }

                val stepDuration = baseStepNs + swingOffset

                val now = TimeSource.Monotonic.markNow()

                if (now >= nextTick) {
                    _step.value = (step + 1) % 32
                    nextTick += stepDuration.nanoseconds
                } else {
                    val remaining = nextTick - now
                    delay(remaining.inWholeMilliseconds.coerceAtLeast(0))
                }
            }
        }
        timerJob = scope.launch {
            while (isActive) {
                _elapsedTime.value = startMark.elapsedNow().inWholeSeconds
                delay(200)
            }
        }
    }

    fun stop() {
        job?.cancel()
        timerJob?.cancel()

        job = null
        timerJob = null

        _step.value = 0
        _elapsedTime.value = 0
    }
}