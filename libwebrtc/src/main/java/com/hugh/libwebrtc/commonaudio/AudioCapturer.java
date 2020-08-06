package com.hugh.libwebrtc.commonaudio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;



public class AudioCapturer {
    private static final String TAG = "AudioCapturer";
    private static final int DEFAULT_SOURCE = MediaRecorder.AudioSource.MIC;
    public static final int DEFAULT_SAMPLE_RATE = 16000;
    public static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int DEFAULT_DATA_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord mAudioRecord;
    private Thread mCaptureThread;
    private boolean mIsCaptureStarted = false;
    private int mRecordDelayMS = 0;
    private int mMiniBufferSize = 0;

    private OnAudioCapturedListener mOnAudioEncodedListener;

    public interface OnAudioCapturedListener {
        void onAudioCaptured(short[] audioData, int stamp);
    }	


    /**
     * set listener
     * */
    public void setOnAudioCapturedListener(OnAudioCapturedListener listener) {
        mOnAudioEncodedListener = listener;
    }


    /**
     * 开始采集
     * */
    public boolean startCapture() {
        return startCapture(DEFAULT_SOURCE, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_DATA_FORMAT);
    }


    private boolean startCapture(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {
        if (mIsCaptureStarted) {
            Log.e(TAG, "Capture already started !");
            return false;
        }

        mMiniBufferSize  = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        if (mMiniBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid parameter !");
            return false;
        }
        int bytesPerSecond = (sampleRateInHz * (audioFormat == AudioFormat.ENCODING_PCM_8BIT ? 1 : 2) * (channelConfig == AudioFormat.CHANNEL_IN_MONO ? 1 : 2) );
        mRecordDelayMS = mMiniBufferSize * 1000/ bytesPerSecond;

        mAudioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, mMiniBufferSize);

        mIsCaptureStarted = true;
        mAudioRecord.startRecording();
        mCaptureThread = new Thread(new AudioCaptureRunnable());
        mCaptureThread.start();
        Log.i(TAG, "Start audio capture success ,capture delay:" + mRecordDelayMS);
        return true;
    }

    /**
     *停止采集
     * */
    public void stopCapture() {
        if(mCaptureThread != null){
            mIsCaptureStarted = false;
            try {
                mCaptureThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mCaptureThread = null;
        }
        if(mAudioRecord != null){
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
        Log.i(TAG, "Stop audio capture success !");
    }

    private class AudioCaptureRunnable implements Runnable {
        @Override
        public void run() {
            while (mIsCaptureStarted) {
                short[] audioCapture = new short[mMiniBufferSize/2];
                int ret = mAudioRecord.read(audioCapture, 0, audioCapture.length);
                if(ret > 0){
                    if(mOnAudioEncodedListener != null){
                        mOnAudioEncodedListener.onAudioCaptured(audioCapture, (int) Ticker.Instance().elapsedTime());
                    }
                }else{
                    Log.e(TAG,"AudioRecord.read failed:" + ret);
                }
            }
        }
    }
    public int getRecordDelayMS(){
        return mRecordDelayMS;
    }
}
