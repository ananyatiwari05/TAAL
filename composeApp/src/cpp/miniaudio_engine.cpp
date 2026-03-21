//
// Created by ANSHUL on 3/19/2026.
//

#define MINIAUDIO_IMPLEMENTATION
#include "miniaudio_engine.h"
#include <string.h>
#include <stdio.h>

ma_engine       g_engine         = {0};
bool            g_engineReady    = false;

PreloadedSound  g_preloaded[MAX_PRELOADED] = {{0}};
int             g_preloadedCount = 0;

ActiveSound     g_activeSounds[MAX_ACTIVE_SOUNDS] = {{0}};

static int findOrLoadPreloaded(const char* filename) {
    for (int i = 0; i < g_preloadedCount; ++i) {
        if (strcmp(g_preloaded[i].name, filename) == 0 && g_preloaded[i].loaded) return i;
    }
    if (g_preloadedCount >= MAX_PRELOADED) return -1;

    int idx = g_preloadedCount;
    PreloadedSound* snd = &g_preloaded[idx];

    if (!platformLoadAsset(filename, &snd->buffer, &snd->decodedData)) {
        return -1;
    }

    strncpy(snd->name, filename, sizeof(snd->name)-1);
    snd->loaded = true;
    g_preloadedCount++;
    return idx;
}

static int findFreeSlot(void) {
    for (int i = 0; i < MAX_ACTIVE_SOUNDS; ++i)
        if (!g_activeSounds[i].active) return i;
    return -1;
}

static void soundEndCallback(void* pUserData, ma_sound* pSound) {
    int slot = (int)(intptr_t)pUserData;
    if (slot >= 0 && slot < MAX_ACTIVE_SOUNDS) {
        ma_sound_uninit(pSound);
        g_activeSounds[slot].active = false;
    }
}

void engine_init(void) {
    if (g_engineReady) return;
    ma_engine_init(NULL, &g_engine);
    g_engineReady = true;
}

int64_t engine_play(const char* filename) {
    if (!g_engineReady) return -1;

    int preIdx = findOrLoadPreloaded(filename);
    if (preIdx < 0) return -1;

    int slot = findFreeSlot();
    if (slot < 0) return -1;

    ma_result r = ma_sound_init_from_data_source(&g_engine, &g_preloaded[preIdx].buffer, 0, NULL, &g_activeSounds[slot].sound);
    if (r != MA_SUCCESS) return -1;

    ma_sound_set_end_callback(&g_activeSounds[slot].sound, soundEndCallback, (void*)(intptr_t)slot);
    ma_sound_start(&g_activeSounds[slot].sound);
    g_activeSounds[slot].active = true;

    return (int64_t)slot;
}

void engine_stop(int64_t handle) {
    if (handle < 0 || handle >= MAX_ACTIVE_SOUNDS) return;
    int slot = (int)handle;
    if (g_activeSounds[slot].active) {
        ma_sound_stop(&g_activeSounds[slot].sound);
        ma_sound_uninit(&g_activeSounds[slot].sound);
        g_activeSounds[slot].active = false;
    }
}

void engine_stop_all(void) {
    for (int i = 0; i < MAX_ACTIVE_SOUNDS; ++i) {
        if (g_activeSounds[i].active) {
            ma_sound_stop(&g_activeSounds[i].sound);
            ma_sound_uninit(&g_activeSounds[i].sound);
            g_activeSounds[i].active = false;
        }
    }
}

void engine_dispose(void) {
    if (!g_engineReady) return;
    engine_stop_all();

    for (int i = 0; i < g_preloadedCount; ++i) {
        if (g_preloaded[i].loaded) {
            ma_audio_buffer_uninit(&g_preloaded[i].buffer);
            if (g_preloaded[i].decodedData) ma_free(g_preloaded[i].decodedData, NULL);
        }
    }
    g_preloadedCount = 0;

    ma_engine_uninit(&g_engine);
    g_engineReady = false;
}