package com.hugh.libwebrtc;

/**
 * Created by chenyw on 2020/8/4.
 */
public class WebRtcAGCUtils {
    static {
        System.loadLibrary("legacy_agc-lib");
    }

    public static native long WebRtcAgc_Create();

    public static native int agcFree(long agcInst);

    public static native int WebRtcAgc_Init(long agcInst, int minLevel, int maxLevel, int agcMode, int fs);

    public static native int agcSetConfig(long agcInst, short targetLevelDbfs, short compressionGaindB, boolean limiterEnable);

    public static native int agcProcess(long agcInst, short[] inNear, int num_bands, int samples, short[] out,
                                        int inMicLevel, int outMicLevel, int echo, boolean saturationWarning);

    public static native int agcAddFarend(long agcInst, short[] inFar, int samples);

    public static native int agcAddMic(long agcInst, short[] inMic, int num_bands, int samples);

    public static native int agcVirtualMic(long agcInst, short[] inMic, int num_bands, int samples, int micLevelIn, int micLevelOut);


    /**
     * 自动增益
     */
    public static class WebRtcAgc {
        /**
         * 构建AGC对象
         *
         * @param minLevel Minimum possible mic level
         * @param maxLevel Maximum possible mic level
         * @param agcMode  : 0 - Unchanged
         * : 1 - Adaptive Analog Automatic Gain Control -3dBOv
         * : 2 - Adaptive Digital Automatic Gain Control -3dBOv
         * : 3 - Fixed Digital Gain 0dB
         * @param fs       Sampling frequency
         */


        private long ctx = 0;
        private int num_bands = 1;

        public WebRtcAgc(int minLevel,
                         int maxLevel,
                         int agcMode,
                         int fs) {
            ctx = WebRtcAgc_Create();
            if (ctx == 0) {
                throw new RuntimeException("WebRtcAgc_Create failed");
            }
            WebRtcAgc_Init(ctx, minLevel, maxLevel, agcMode, fs);
        }

        /**
         * 释放JNI底层对象
         */
        public void release() {
            if (ctx != 0) {
                agcFree(ctx);
                ctx = 0;
            }
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            release();
        }

        /**
         * 配置AGC
         *
         * @param targetLevelDbfs   default 3 (-3 dBOv), dbfs表示相对于full scale的下降值，0表示full scale，越小声音越大
         * @param compressionGaindB default 9 dB,在Fixed模式下，越大声音越大
         * @param limiterEnable     default true (on)
         * @return
         */
        public int setConfig(short targetLevelDbfs,
                             short compressionGaindB,
                             boolean limiterEnable) {
            return agcSetConfig(ctx, targetLevelDbfs, compressionGaindB, limiterEnable);
        }

        /**
         * 输入远端数据
         * This function processes a 10/20ms frame of far-end speech to determine
         * if there is active speech. Far-end speech length can be either 10ms or
         * 20ms. The length of the input speech vector must be given in samples
         * (80/160 when FS=8000, and 160/320 when FS=16000 or FS=32000).
         *
         * @param inFar   Far-end input speech vector (10 or 20ms)
         * @param samples Number of samples in input vector
         * @return 0:success ,-1 error
         */
        public int addFarend(short[] inFar, int samples) {
            return agcAddFarend(ctx, inFar, samples);
        }

        /**
         * This function processes a 10/20ms frame of microphone speech to determine
         * if there is active speech. Microphone speech length can be either 10ms or
         * 20ms. The length of the input speech vector must be given in samples
         * (80/160 when FS=8000, and 160/320 when FS=16000 or FS=32000). For very low
         * input levels, the input signal is increased in level by multiplying and
         * overwriting the samples in inMic[].
         * <p>
         * This function should be called before any further processing of the
         * near-end microphone signal.
         *
         * @param inMic   Microphone input speech vector (10 or 20 ms)
         * @param samples Number of samples in input vector
         * @return 0:success , -1 :error
         */
        // num_bands业务层维护
        public int addMic(short[] inMic, int samples) {
            return agcAddMic(ctx, inMic, num_bands, samples);
        }


        /**
         * virtualMic方法返回值对象
         */
        public static class ResultOfVirtualMic {
            /**
             * 函数执行返回值,0:success,-1:error
             */
            public int ret;

            /**
             * Adjusted microphone level after processing
             */
            public int micLevelOut;
        }

        /**
         * This function replaces the analog microphone with a virtual one.
         * It is a digital gain applied to the input signal and is used in the
         * agcAdaptiveDigital mode where no microphone level is adjustable.
         * Microphone speech length can be either 10ms or 20ms. The length of the
         * input speech vector must be given in samples (80/160 when FS=8000, and
         * 160/320 when FS=16000 or FS=32000).
         *
         * @param inMic      Microphone input speech vector for (10 or 20 ms),and Microphone output after processing
         * @param samples    Number of samples in input vector
         * @param micLevelIn Input level of microphone (static)
         * @return 结果对象
         * @see ResultOfVirtualMic
         */
        public ResultOfVirtualMic virtualMic(short[] inMic,
                                             int samples,
                                             int micLevelIn, int micLevelOutArr) {
            ResultOfVirtualMic obj = new ResultOfVirtualMic();
//            int[] micLevelOutArr = new int[1];
            obj.ret = agcVirtualMic(ctx, inMic, num_bands, samples, micLevelIn, micLevelOutArr);
//            obj.micLevelOut = micLevelOutArr[0];
            return obj;
        }


        /**
         * process方法返回值对象
         */
        public static class ResultOfProcess {
            /**
             * 函数执行返回值,0:success,-1:error
             */
            public int ret;

            /**
             * 增益处理后的数据
             */
            public short[] out;

            /**
             * Adjusted microphone volume level
             */
            public int outMicLevel;

            /**
             * A returned value of 1 indicates a saturation event
             * has occurred and the volume cannot be further
             * reduced. Otherwise will be set to 0.
             */
            public int saturationWarning;
        }


        /**
         * This function processes a 10/20ms frame and adjusts (normalizes) the gain
         * both analog and digitally. The gain adjustments are done only during
         * active periods of speech. The input speech length can be either 10ms or
         * 20ms and the output is of the same length. The length of the speech
         * vectors must be given in samples (80/160 when FS=8000, and 160/320 when
         * FS=16000 or FS=32000). The echo parameter can be used to ensure the AGC will
         * not adjust upward in the presence of echo.
         * <p>
         * This function should be called after processing the near-end microphone
         * signal, in any case after any echo cancellation.
         *
         * @param inNear     Near-end input speech vector (10 or 20 ms)
         * @param samples    Number of samples in input/output vector
         * @param inMicLevel Current microphone volume level
         * @param echo       Set to 0 if the signal passed to add_mic is
         *                   almost certainly free of echo; otherwise set
         *                   to 1. If you have no information regarding echo
         *                   set to 0.
         * @return 返回值对象
         * @see ResultOfProcess
         */
        public ResultOfProcess process(short[] inNear,
                                       int samples,
                                       int inMicLevel,
                                       int outMicLevel,
                                       int echo) {
            ResultOfProcess obj = new ResultOfProcess();
            obj.out = new short[inNear.length];
            int[] saturationWarningArr = new int[1];
            obj.ret = agcProcess(ctx, inNear, num_bands, samples, obj.out, inMicLevel, outMicLevel, echo, false);
            obj.saturationWarning = saturationWarningArr[0];
            return obj;
        }

    }

}
