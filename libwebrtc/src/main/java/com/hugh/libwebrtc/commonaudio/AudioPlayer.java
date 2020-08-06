/*
 *  COPYRIGHT NOTICE  
 *  Copyright (C) 2016, Jhuster <lujun.hust@gmail.com>
 *  https://github.com/Jhuster/AudioDemo
 *   
 *  @license under the Apache License, Version 2.0 
 *
 *  @file    AudioPlayer.java
 *  
 *  @version 1.0     
 *  @author  Jhuster
 *  @date    2016/03/19
 */
package com.hugh.libwebrtc.commonaudio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class AudioPlayer {
    private static final String TAG = "AudioPlayer";
    private static final int DEFAULT_STREAM_TYPE = AudioManager.STREAM_MUSIC;
    private static final int DEFAULT_SAMPLE_RATE = 16000;
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int DEFAULT_PLAY_MODE = AudioTrack.MODE_STREAM;
    private AudioTrack mAudioTrack;

    /**
     * 初始化
     * */
    public boolean startPlayer() {
        return startPlayer(DEFAULT_STREAM_TYPE, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT);
    }

    private int playDelayMs = 0;
    public boolean startPlayer(int streamType, int sampleRateInHz, int channelConfig, int audioFormat) {
        if(mAudioTrack != null){
            return true;
        }
        int bufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        if (bufferSizeInBytes == AudioTrack.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid parameter !");
            return false;
        }
        int bytesPerSecond = (sampleRateInHz * (audioFormat == AudioFormat.ENCODING_PCM_8BIT ? 1 : 2) * (channelConfig == AudioFormat.CHANNEL_IN_MONO ? 1 : 2) );
        playDelayMs =  bufferSizeInBytes * 1000/ bytesPerSecond;
        mAudioTrack = new AudioTrack(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, DEFAULT_PLAY_MODE);
        return true;
    }

    public int getPlayDelayMS(){
        return playDelayMs;
    }


    /**
     * 停止播放
     * */
    public void stopPlayer() {
        if(mAudioTrack != null){
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
            Log.i(TAG, "Stop audio player success !");
        }
    }

    /**
     * 播放语音流
     * */
    public boolean play(short[] audioData, int offsetInBytes, int sizeInBytes) {
        if(mAudioTrack == null){
            Log.e(TAG, "AudioPlayer not started!");
            return false;
        }
        if (mAudioTrack.write(audioData, offsetInBytes, sizeInBytes) != sizeInBytes) {
            Log.e(TAG, "Could not write all the samples to the audio device !");
        }
        mAudioTrack.play();
        return true;
    }
}
