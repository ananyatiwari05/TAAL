//
// Created by ANSHUL on 3/19/2026.
//

#include "miniaudio_engine.h"
#include <jni.h>
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#define LOG_TAG "MiniAudio"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static AAssetManager* g_assetManager = nullptr;

bool platformLoadAsset(const char* path, ma_audio_buffer* outBuffer, void** outDecodedData) {
    AAsset* asset = AAssetManager_open(g_assetManager, path, AASSET_MODE_BUFFER);
    if (!asset) {
        LOGE("Cannot open asset: %s", path);
        return false;
    }

    const void* rawData = AAsset_getBuffer(asset);
    ma_uint64 size = AAsset_getLength(asset);

    ma_decoder_config cfg = ma_decoder_config_init(ma_format_f32,
            ma_engine_get_channels(&g_engine),
            ma_engine_get_sample_rate(&g_engine));

    ma_uint64 frames = 0;
    ma_result r = ma_decode_memory(rawData, size, &cfg, &frames, outDecodedData);
    AAsset_close(asset);

    if (r != MA_SUCCESS || !*outDecodedData) return false;

    ma_audio_buffer_config bufCfg = ma_audio_buffer_config_init(
            ma_format_f32, cfg.channels, frames, *outDecodedData, NULL);

    r = ma_audio_buffer_init(&bufCfg, outBuffer);
    return r == MA_SUCCESS;
}

extern "C"
JNIEXPORT void JNICALL
Java_org_example_project_AudioEngineWrapper_initEngine(JNIEnv* env, jobject thiz, jobject assetManager) {
g_assetManager = AAssetManager_fromJava(env, assetManager);
engine_init();
LOGI("MiniAudio engine ready (polyphony %d)", MAX_ACTIVE_SOUNDS);
}

extern "C"
JNIEXPORT jlong JNICALL
        Java_org_example_project_AudioEngineWrapper_playAsset(JNIEnv* env, jobject thiz, jstring fileName) {
const char* str = env->GetStringUTFChars(fileName, NULL);
jlong handle = engine_play(str);
env->ReleaseStringUTFChars(fileName, str);
return handle;
}

extern "C"
JNIEXPORT void JNICALL Java_org_example_project_AudioEngineWrapper_stopSound(JNIEnv*, jobject, jlong h) { engine_stop(h); }
extern "C"
JNIEXPORT void JNICALL Java_org_example_project_AudioEngineWrapper_stopAll(JNIEnv*, jobject) { engine_stop_all(); }
extern "C"
JNIEXPORT void JNICALL Java_org_example_project_AudioEngineWrapper_disposeEngine(JNIEnv*, jobject) { engine_dispose(); }