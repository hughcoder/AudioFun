#include <jni.h>
#include <string>
#include "_android_log_print.h"
#include "noise_suppression.h"
//#include "analog_agc.h"
#include "ns_core.h"
#include "ns/noise_suppression.h"
#include "ns/signal_processing_library.h"


#ifdef __cplusplus
extern "C" {
#endif

//音量增益
void *agcHandle = NULL;
// 降噪处理句柄
NsHandle *pNs_inst = NULL;
JNIEXPORT jshortArray JNICALL
Java_com_hugh_libwebrtc_WebRtcUtils_webRtcNsProcess(JNIEnv *env, jclass type, jint freq, jint len,
                                                    jshortArray proData_) {

    jshort *proData = env->GetShortArrayElements(proData_, NULL);
    int dataLen = env->GetArrayLength(proData_);
//    LOGD("webRtcNsProcess dataLen=== %d", dataLen);
    int size = freq / 100;

    if (pNs_inst) {
        for (int i = 0; i < dataLen; i += size) {
            if (dataLen - i >= size) {
                if (size == 80) {
                    float *shBufferIn[80] = {0};
                    float *shBufferOut[80] = {0};
                    memcpy(shBufferIn, (char *) (proData + i), size * sizeof(short));
                    WebRtcNs_Process(pNs_inst, shBufferIn, 2, shBufferOut);
                    memcpy(proData + i, shBufferOut, size * sizeof(short));
                    LOGD("Noise_Suppression WebRtcNs_Process success");
                } else {
                    float *shBufferIn[160] = {0};
                    float *shBufferOut[160] = {0};
                    memcpy(shBufferIn, (char *) (proData + i), size * sizeof(short));
                    WebRtcNs_Process(pNs_inst, shBufferIn, 2, shBufferOut);
                    memcpy(proData + i, shBufferOut, size * sizeof(short));
                    LOGD("Noise_Suppression WebRtcNs_Process success");
                }
            }
        }
    } else {
        LOGD("pNs_inst null==");
    }

    env->ReleaseShortArrayElements(proData_, proData, 0);

    return proData_;
}

JNIEXPORT jshortArray JNICALL
Java_com_hugh_libwebrtc_WebRtcUtils_webRtcNsProcess32k(JNIEnv *env, jclass type, jint len,
                                                       jshortArray proData_) {

    jshort *proData = env->GetShortArrayElements(proData_, NULL);
    int dataLen = env->GetArrayLength(proData_);

    if (pNs_inst) {
        short shBufferIn[320] = {0};
        short shBufferOut[320] = {0};


        int filter_state1[6], filter_state2[6];
        memset(filter_state1, 0, sizeof(filter_state1));
        memset(filter_state2, 0, sizeof(filter_state2));

        int Synthesis_state1[6], Synthesis_state12[6];
        memset(Synthesis_state1, 0, sizeof(Synthesis_state1));
        memset(Synthesis_state12, 0, sizeof(Synthesis_state12));

        for (int i = 0; i < dataLen; i += sizeof(short) * 160) {
            if (dataLen - i >= sizeof(short) * 160) {

                short shInL[160], shInH[160];
                short shOutL[160] = {0}, shOutH[160] = {0};

                memcpy(shBufferIn, (proData + i), 320 * sizeof(short));
                //以高频和低频的方式传入函数内部
                WebRtcSpl_AnalysisQMF(shBufferIn,shBufferIn, shInL, shInH, filter_state1, filter_state2);

//                if (0 != WebRtcNs_Process(pNs_inst, shInL, shInH, shOutL, shOutH)) {
//                    LOGE("Noise_Suppression WebRtcNs_Process err! \n");
//                } else {
//                    //合成数据
//                    WebRtcSpl_SynthesisQMF(shOutL, shOutH, shBufferOut, Synthesis_state1,
//                                           Synthesis_state12);
//
//                    memcpy(proData + i, shBufferOut, 320 * sizeof(short));
//                    LOGD("Noise_Suppression WebRtcNs_Process");
//                }
            }
        }
    } else {
        LOGD("pNs_inst null==");
    }

    env->ReleaseShortArrayElements(proData_, proData, 0);

    return proData_;
}

JNIEXPORT jint JNICALL
Java_com_hugh_libwebrtc_WebRtcUtils_webRtcNsFree(JNIEnv *env, jclass type) {
    //释放内存
    int _result = -1;
    if (pNs_inst) {
        WebRtcNs_Free(pNs_inst);
        pNs_inst = NULL;
        LOGD("Noise_Suppression webRtcNsFree");
    }
    return _result;
}

JNIEXPORT void JNICALL
Java_com_hugh_libwebrtc_WebRtcUtils_webRtcNsInit(JNIEnv *env, jclass type, jint freq) {

    //创建降噪句柄
    pNs_inst = WebRtcNs_Create();
    LOGD("WebRtcNs_Create ==");
    //初始化 采样率 8k 16k 32k
    int result =  WebRtcNs_Init(pNs_inst, freq);
    if(result == -1){
        LOGD("WebRtcNs_Init fail");
    }
    // 模式有 1轻度 2中度 3强
    WebRtcNs_set_policy(pNs_inst, 2);
}

#ifdef __cplusplus
}
#endif