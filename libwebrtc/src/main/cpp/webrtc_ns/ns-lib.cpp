#include <jni.h>
#include <string>
#include <cstdlib>
#include <functional>
#include "modules/audio_processing/legacy_ns/noise_suppression.h"
#include "modules/audio_processing/legacy_ns/noise_suppression_x.h"



#if defined(__cplusplus)
extern "C" {
#endif

JNIEXPORT jlong JNICALL
Java_com_hugh_libwebrtc_WebRtcNsUtils_nsCreate(JNIEnv *env, jclass obj) {
    return (long) WebRtcNs_Create();
}


JNIEXPORT jint JNICALL
Java_com_hugh_libwebrtc_WebRtcNsUtils_nsInit(JNIEnv *env, jclass obj, jlong nsHandler,
                                             jint frequency) {
    NsHandle *handler = (NsHandle *) nsHandler;
    if (handler == nullptr) {
        return -3;
    }
    return WebRtcNs_Init(handler, frequency);
}


JNIEXPORT jint JNICALL
Java_com_hugh_libwebrtc_WebRtcNsUtils_WebRtcNs_1set_1policy(JNIEnv *env, jclass obj,
                                                            jlong nsHandler, jint mode) {
    NsHandle *handle = (NsHandle *) nsHandler;
    if (handle == nullptr) {
        return -3;
    }
    return WebRtcNs_set_policy(handle, mode);
}


JNIEXPORT jint JNICALL
Java_com_hugh_libwebrtc_WebRtcNsUtils_nsProcess(JNIEnv *env,
                                                jclass obj, jlong nsHandler,
                                                jfloatArray spframe, jint num_bands,
                                                jfloatArray outframe) {
    NsHandle *handle = (NsHandle *) nsHandler;
    if (handle == nullptr) {
        return -3;
    }
    jfloat *cspframe = env->GetFloatArrayElements(spframe, nullptr);
    jfloat *coutframe = env->GetFloatArrayElements(outframe, nullptr);
    WebRtcNs_Process(handle, &cspframe, num_bands, &coutframe);
    env->ReleaseFloatArrayElements(spframe, cspframe, 0);
    env->ReleaseFloatArrayElements(outframe, coutframe, 0);
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_hugh_libwebrtc_WebRtcNsUtils_nsFree(JNIEnv *env,
                                             jclass obj, jlong
                                             nsHandler) {
    NsHandle *handle = (NsHandle *) nsHandler;
    if (handle == nullptr) {
        return -3;
    }
    WebRtcNs_Free(handle);
    return 0;
}

JNIEXPORT jlong JNICALL
Java_com_hugh_libwebrtc_WebRtcNsUtils_WebRtcNsx_1Create(JNIEnv *env, jclass obj) {
    return (long) WebRtcNsx_Create();
}


JNIEXPORT jint JNICALL
Java_com_hugh_libwebrtc_WebRtcNsUtils_WebRtcNsx_1Init(JNIEnv *env, jclass obj, jlong nsHandler,
                                                      jint frequency
) {
    NsxHandle *handler = (NsxHandle *) nsHandler;
    if (handler == nullptr) {
        return -3;
    }
    return WebRtcNsx_Init(handler, frequency);
}


JNIEXPORT jint JNICALL
Java_com_hugh_libwebrtc_WebRtcNsUtils_nsxSetPolicy(JNIEnv *env,
                                                   jclass obj, jlong
                                                   nsHandler,
                                                   jint mode
) {
    NsxHandle *handle = (NsxHandle *) nsHandler;
    if (handle == nullptr) {
        return -3;
    }
    return WebRtcNsx_set_policy(handle, mode);
}


JNIEXPORT jint JNICALL
Java_com_hugh_libwebrtc_WebRtcNsUtils_WebRtcNsx_1Process(JNIEnv *env,
                                                         jclass obj, jlong
                                                        nsHandler,
                                                         jshortArray speechFrame,
                                                         jint num_bands,
                                                         jshortArray outframe) {
    NsxHandle *handle = (NsxHandle *) nsHandler;
    if (handle == nullptr) {
        return -3;
    }
    jshort *cspeechFrame = env->GetShortArrayElements(speechFrame, nullptr);
    jshort *coutframe = env->GetShortArrayElements(outframe, nullptr);
    WebRtcNsx_Process(handle, &cspeechFrame, num_bands, &coutframe);
    env->ReleaseShortArrayElements(speechFrame, cspeechFrame, 0);
    env->ReleaseShortArrayElements(outframe, coutframe, 0);
    return 0;
}

JNIEXPORT jshortArray JNICALL
Java_com_hugh_libwebrtc_WebRtcNsUtils_WebRtcNs_1ProcessShort(JNIEnv* env, jclass cls, jlong ctx, jshortArray in,
                                                             jint sampleMS) {
    NsxHandle *handle = (NsxHandle *) ctx;
    short *in_ptr = env->GetShortArrayElements(in, 0);

    auto in_len = env->GetArrayLength(in);
    short out_ptr[in_len];

    auto pkt_count = sampleMS / 10;
    for (int i = 0; i < pkt_count; ++i) {
        int offset = i * (in_len / pkt_count);
        short *ptr_in = in_ptr + offset;
        short *ptr_out = out_ptr + offset;
        WebRtcNsx_Process(handle, (const short *const *) &ptr_in, 1, &ptr_out);
    }

    auto out = env->NewShortArray(in_len);
    env->SetShortArrayRegion(out, 0, in_len, out_ptr);
    return out;
}

JNIEXPORT jint JNICALL
Java_com_hugh_libwebrtc_WebRtcNsUtils_WebRtcNsx_1Free(JNIEnv *env, jclass obj, jlong nsHandler) {
    NsxHandle *handle = (NsxHandle *) nsHandler;
    if (handle == nullptr) {
        return -3;
    }
    WebRtcNsx_Free(handle);
    return 0;
}

#if defined(__cplusplus)
}
#endif

