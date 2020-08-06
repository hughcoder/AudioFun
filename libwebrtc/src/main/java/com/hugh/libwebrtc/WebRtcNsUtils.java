package com.hugh.libwebrtc;

/**
 * Created by chenyw on 2020/7/28.
 */
public class WebRtcNsUtils {
    static {
        System.loadLibrary("legacy_ns-lib");
    }

    public static native long nsCreate();

    public static native int nsInit(long nsHandler, int frequency);

    /**
     * @param mode 0: Mild, 1: Medium , 2: Aggressive
     * @return 0 - Ok
     * -1 - Error
     */
    public static native int WebRtcNs_set_policy(long nsHandler, int mode);

    public static native int nsProcess(long nsHandler, float[] spframe, int num_bands, float[] outframe);

    public static native int nsFree(long nsHandler);

    public static native long WebRtcNsx_Create();

    public static native int WebRtcNsx_Init(long nsxHandler, int frequency);

    /**
     * @param mode 0: Mild, 1: Medium , 2: Aggressive
     * @return 0 - Ok
     * -1 - Error
     */
    public static native int nsxSetPolicy(long nsxHandler, int mode);

    public static native int WebRtcNsx_Process(long nsxHandler, short[] speechFrame, int num_bands, short[] outframe);

    public static native int WebRtcNsx_Free(long nsxHandler);

    private static native short[] WebRtcNs_ProcessShort(long NS_inst, short[] spframe, int sampleMS);

    /**
     * 低频噪音消除
     */
    public static class WebRtcNs{
        /**
         * 构建NS对象
         * @param fs 采样率
         * @param mode 模式(0: Mild, 1: Medium , 2: Aggressive)
         */
        public WebRtcNs(int fs,int mode){
            ctx = WebRtcNsx_Create();
            if(ctx == 0){
                throw new RuntimeException("WebRtcNs_Create failed");
            }
            WebRtcNsx_Init(ctx,fs);
            WebRtcNs_set_policy(ctx,mode);
        }

        /**
         * 立即释放JNI对象
         */
        public void release(){
            if(ctx != 0 ){
                WebRtcNsx_Free(ctx);
                ctx = 0;
            }
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            release();
        }

        /**
         * 消除低频背景噪声
         * @param spframe 采集后需要消噪的声音
         * @param sampleMS 采样毫秒数，必须为10ms的整数倍
         * @return null出现错误，否则为消噪之后的数据
         */
        public short [] process(short[] spframe,int sampleMS){
            return WebRtcNs_ProcessShort(ctx,spframe, sampleMS);
        }

        private long ctx = 0;
    }
}
