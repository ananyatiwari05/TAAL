//
// Created by ANSHUL on 3/16/2026.
//
#define MINIAUDIO_IMPLEMENTATION
#include "miniaudio.h"
#include "AudioEngine.h"

#if defined(__ANDROID__)
    #include <android/log.h>
    #define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "AudioEngine", __VA_ARGS__)
    #define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "AudioEngine", __VA_ARGS__)
#else
    #include <cstdio>
    #define LOGI(...) printf("AudioEngine [INFO]: " __VA_ARGS__); printf("\n")
    #define LOGE(...) printf("AudioEngine [ERROR]: " __VA_ARGS__); printf("\n")
#endif

AudioEngine::AudioEngine() : isInitialized(false) {
    pEngine = new ma_engine;
    if (ma_engine_init(nullptr, static_cast<ma_engine*>(pEngine)) != MA_SUCCESS) {
        isInitialized = true;
        LOGI("Engine Initialized Successfully.");
    } else {
        LOGE("Engine Initialization Failed.");
    }
}

AudioEngine::~AudioEngine() {
    if (isInitialized)
        ma_engine_uninit(static_cast<ma_engine*>(pEngine));
    delete static_cast<ma_engine*>(pEngine);
}

void AudioEngine::playSoundFromMemory(const uint8_t* audioData, size_t dataSize) {
    if (!isInitialized || !audioData || dataSize == 0) return;

    ma_engine* engine = static_cast<ma_engine*>(pEngine);

}

void ma_engine_play_sound_from_data_source(ma_engine, ma_decoder);