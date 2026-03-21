//
// Created by ANSHUL on 3/16/2026.
//

#pragma once

#include <vector>
#include <cstdint>
#include <memory>

struct AudioSample {
    void* pDecodedData = nullptr;
    ma_audio_buffer buffer;
    ma_sound sound;

    // The destructor automatically cleans up miniaudio resources!
    ~AudioSample() {
        ma_sound_uninit(&sound);
        ma_audio_buffer_uninit(&buffer);
        if (pDecodedData) {
            ma_free(pDecodedData, nullptr);
        }
    }
};

class AudioEngine {
private:
    void* pEngine;
    bool isInitialized;

public:
    AudioEngine();
    ~AudioEngine();

    void playSoundFromMemory(const uint8_t* audioData, size_t dataSize);

    void stopAll();
};