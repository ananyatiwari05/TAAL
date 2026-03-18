//
// Created by ANSHUL on 3/16/2026.
//
#include <jni.h>
#include <android/asset_manager_jni.h>
#include "AudioEngine.h"

extern "C" {

JNIEXPORT jlong JNICALL
Java_org_example_project_AudioEngineWrapper_createEngine(JNIEnv *env, jobject thiz) {
    return reinterpret_cast<jlong>(new AudioEngine());
}

JNIEXPORT void JNICALL
Java_org_example_project_AudioEngineWrapper_playAsset(
        JNIEnv *env, jobject thiz,
        jlong engineHandle,
        jobject assetManager,
        jstring fileName
        ){
    if (engineHandle == 0 || !filename || !assetManager) return;)

    auto* engine =
}
}
