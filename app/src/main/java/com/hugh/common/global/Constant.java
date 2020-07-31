package com.hugh.common.global;

import com.hugh.audiofun.BuildConfig;

/**
 * Created by chenyw on 2020/7/30.
 */
public class Constant {
    public static final boolean Debug = BuildConfig.DEBUG;

    public static final int NoExistIndex = -1;

    public static final int OneSecond = 1000;

    public static final int RecordSampleRate = 44100; // 采样率
    public static final int RecordByteNumber = 2; // 采样率
    public static final int RecordChannelNumber = 1;  // 与DEFAULT_CHANNEL_CONFIG相关，因为是mono单声，所以是1
    public static final int RecordDataNumberInOneSecond =
            RecordSampleRate * RecordByteNumber * RecordChannelNumber;
    // 与DEFAULT_CHANNEL_CONFIG相关，因为是mono单声，所以是1
    public static final int BehaviorSampleRate = 44100; // 采样率
    public static final int LameMp3Quality = 7; // Lame Default Settings
    public static final int LameBehaviorChannelNumber = RecordChannelNumber;
    // 与DEFAULT_CHANNEL_CONFIG相关，因为是mono单声，所以是1
    public static final int lameRecordBitRate = 64;
    // Encoded bit rate. MP3 file will be encoded with bit rate 64kbps
    public static final int LameBehaviorBitRate = 128;

    public static final int MusicCutEndOffset = 2;

    public static final int MaxDecodeProgress = 50;
    public static final int NormalMaxProgress = 100;

    public static final int RecordVolumeMaxRank = 9;

    public static final int ThreadPoolCount = 5;

    public static final float VoiceWeight = 1.8f;
    public static final float VoiceBackgroundWeight = 0.2f;

    public static final String IGeneImageSuffix = ".ipg";
    public static final String JPGSuffix = ".jpg";
    public static final String PngSuffix = ".png";
    public static final String MusicSuffix = ".mp3";
    public static final String LyricSuffix = ".lrc";
    public static final String RecordSuffix = ".mp3";
    public static final String PcmSuffix = ".pcm";
}
