//package com.hugh.libwebrtc;
//
//public class MP3lame {
//    static {
//        System.loadLibrary("mp3lame");
//    }
//
//    public MP3lame(int inSamplerate,
//                   int inChannel,
//                   int outSamplerate,
//                   int outBitrate,
//                   int quality){
//        ctx = mp3lame_create(inSamplerate,inChannel,outSamplerate,outBitrate,quality);
//        if(ctx == 0){
//            throw new IllegalArgumentException("mp3lame_create failed");
//        }
//    }
//
//    @Override
//    protected void finalize() throws Throwable {
//        super.finalize();
//        release();
//    }
//    public void release(){
//        if(ctx != 0){
//            mp3lame_destory(ctx);
//            ctx = 0;
//        }
//    }
//
//    /**
//     * 开始编码数据
//     * @param data 输入的数据
//     * @return
//     */
//    public byte[] encode(short[] data) {
//        return mp3lame_encode(ctx,data);
//    }
//
//    /**
//     * 获取mp3文件尾
//     * @return
//     */
//    public byte[] flush() {
//        return mp3lame_flush(ctx);
//    }
//
//    private long ctx = 0;
//
//
//    private static native long mp3lame_create(int inSamplerate, int inChannel, int outSamplerate, int outBitrate, int quality);
//    private static native void mp3lame_destory(long ctx);
//    private static native byte[]  mp3lame_flush(long ctx);
//    private static native byte[]  mp3lame_encode(long ctx, short[] data);
//
//}
