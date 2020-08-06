#include <jni.h>
#include <string>
#include <cstdlib>

#include "modules/audio_processing/agc/legacy/gain_control.h"

#if defined(__cplusplus)
extern "C" {
#endif

JNIEXPORT jlong JNICALL
Java_com_hugh_libwebrtc_WebRtcAGCUtils_WebRtcAgc_1Create(JNIEnv *env, jobject obj) {
    return (long) WebRtcAgc_Create();
}


JNIEXPORT jint JNICALL
Java_com_hugh_libwebrtc_WebRtcAGCUtils_agcFree(JNIEnv *env, jobject obj,
                                                             jlong agcInst) {
    void *_agcInst = (void *) agcInst;
    if (_agcInst == nullptr)
        return -3;
    WebRtcAgc_Free(_agcInst);
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_hugh_libwebrtc_WebRtcAGCUtils_WebRtcAgc_1Init(JNIEnv *env,
                                                       jobject obj, jlong agcInst,
                                                       jint minLevel, jint maxLevel,
                                                       jint agcMode, jint fs) {
    void *_agcInst = (void *) agcInst;
    if (_agcInst == nullptr)
        return -3;
    return WebRtcAgc_Init(_agcInst, minLevel, maxLevel, agcMode, fs);
}

JNIEXPORT jint JNICALL
Java_com_hugh_libwebrtc_WebRtcAGCUtils_agcSetConfig(JNIEnv *env, jobject obj,
                                                                  jlong agcInst,
                                                                  jshort targetLevelDbfs,
                                                                  jshort compressionGaindB,
                                                                  jboolean limiterEnable
) {
    void *_agcInst = (void *) agcInst;
    if (_agcInst == nullptr)
        return -3;
    WebRtcAgcConfig setConfig;
    setConfig.targetLevelDbfs = targetLevelDbfs;
    setConfig.compressionGaindB = compressionGaindB;
    setConfig.limiterEnable = limiterEnable;
    return WebRtcAgc_set_config(_agcInst, setConfig);
}

JNIEXPORT jint JNICALL
Java_com_hugh_libwebrtc_WebRtcAGCUtils_agcProcess(JNIEnv *env, jobject obj,
                                                                jlong agcInst,
                                                                jshortArray inNear,
                                                                jint num_bands,
                                                                jint samples, jshortArray out,
                                                                jint inMicLevel,
                                                                jint outMicLevel,
                                                                jint echo,
                                                                jboolean saturationWarning) {
    void *_agcInst = (void *) agcInst;
    if (_agcInst == nullptr)
        return -3;
    jshort *cinNear = env->GetShortArrayElements(inNear, nullptr);
    jshort *cout = env->GetShortArrayElements(out, nullptr);

    int32_t gains[11] = {};
    jint ret = WebRtcAgc_Analyze(_agcInst, &cinNear, num_bands, samples, inMicLevel, &outMicLevel,
                                 echo, &saturationWarning, gains);
    if (ret == 0)
        ret = WebRtcAgc_Process(_agcInst, gains, &cinNear, num_bands, &cout);
    env->ReleaseShortArrayElements(inNear, cinNear, 0);
    env->ReleaseShortArrayElements(out, cout, 0);
    return ret;
}


JNIEXPORT jint JNICALL
Java_com_hugh_libwebrtc_WebRtcAGCUtils_agcAddFarend(JNIEnv *env, jobject obj,
                                                                  jlong agcInst,
                                                                  jshortArray inFar,
                                                                  jint samples) {
    void *_agcInst = (void *) agcInst;
    if (_agcInst == nullptr)
        return -3;
    short *cinFar = env->GetShortArrayElements(inFar, nullptr);
    jint ret = WebRtcAgc_AddFarend(_agcInst, cinFar, samples);
    env->ReleaseShortArrayElements(inFar, cinFar, 0);
    return ret;
}


JNIEXPORT jint JNICALL
Java_com_hugh_libwebrtc_WebRtcAGCUtils_agcAddMic(JNIEnv *env, jobject obj,
                                                               jlong agcInst,
                                                               jshortArray inMic,
                                                               jint num_bands, jint samples
) {
    void *_agcInst = (void *) agcInst;
    if (_agcInst == nullptr)
        return -3;
    short *cinMic = env->GetShortArrayElements(inMic, nullptr);
    jint ret = WebRtcAgc_AddMic(_agcInst, &cinMic, num_bands, samples);
    env->ReleaseShortArrayElements(inMic, cinMic, 0);
    return ret;
}


JNIEXPORT jint JNICALL
Java_com_hugh_libwebrtc_WebRtcAGCUtils_agcVirtualMic(JNIEnv *env, jobject obj,
                                                                   jlong agcInst,
                                                                   jshortArray inMic,
                                                                   jint num_bands,
                                                                   jint samples,
                                                                   jint micLevelIn,
                                                                   jint micLevelOut
) {
    void *_agcInst = (void *) agcInst;
    if (_agcInst == nullptr)
        return -3;
    jshort *cinMic = env->GetShortArrayElements(inMic, nullptr);
    jint ret = WebRtcAgc_VirtualMic(_agcInst, &cinMic, num_bands, samples, micLevelIn,
                                    &micLevelOut);
    env->ReleaseShortArrayElements(inMic, cinMic, 0);
    return ret;
}


#if defined(__cplusplus)
}
#endif

