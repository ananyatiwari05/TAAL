//
// Created by ANSHUL on 3/19/2026.
//

#pragma once
#include "miniaudio.h"
#include <stdbool.h>
#include <stdint.h>

#define MAX_PRELOADED 64
#define MAX_ACTIVE_SOUNDS 32

typedef struct {
    char name[128];
    ma_audio_buffer buffer;
    void* decodedData;
    bool loaded;
} PreloadedSound;

typedef struct {
    ma_sound sound;
    bool active;
} ActiveSound;

// These are defined in the .cpp
extern ma_engine g_engine;
extern bool g_engineReady;

// Platform-specific asset loader (you implement this per platform)
extern bool platformLoadAsset(const char* path, ma_audio_buffer* outBuffer, void** outDecodedData);

// Public API (called from JNI or directly from other platforms)
void engine_init(void);
int64_t engine_play(const char* filename);   // returns handle or -1
void engine_stop(int64_t handle);
void engine_stop_all(void);
void engine_dispose(void);