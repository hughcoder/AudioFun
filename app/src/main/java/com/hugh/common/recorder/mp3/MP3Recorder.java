package com.hugh.common.recorder.mp3;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;


import com.hugh.common.global.Constant;
import com.hugh.common.global.Variable;
import com.hugh.common.recorder.CommonThreadPool;
import com.hugh.common.util.CommonFunction;
import com.hugh.common.util.FileFunction;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class MP3Recorder {
    private static final String TAG = MP3Recorder.class.getSimpleName();
    private short[] audioRecordBuffer;

    private int audioRecordBufferSize;
    private int realSampleDuration;
    private int realSampleNumberInOneDuration;

    private final static int toTransformLocationNumber = 3;
    private final static int receiveSuperEaCycleNumber = 10;

    private final static int sampleDuration = 100;

    private static final int recordSleepDuration = 500;

    //自定义 每160帧作为一个周期，通知一下需要进行编码
    private static final int FRAME_COUNT = 160;

    private static final PCMFormat pcmFormat = PCMFormat.PCM_16BIT;

    private double amplitude;

    private AudioRecord audioRecord = null;

    private RecordThread recordThread;

    public MP3Recorder() {
        init();
    }

    private void init() {
        initAudioRecord();

        recordThread = new RecordThread();

        CommonThreadPool.getThreadPool().addCachedTask(recordThread);
    }

    private void initAudioRecord() {
        int audioRecordMinBufferSize = AudioRecord
                .getMinBufferSize(Constant.RecordSampleRate, AudioFormat.CHANNEL_IN_MONO,
                        pcmFormat.getAudioFormat());

        audioRecordBufferSize =
                Constant.RecordSampleRate * pcmFormat.getBytesPerFrame() / (1000 / sampleDuration);

        if (audioRecordMinBufferSize > audioRecordBufferSize) {
            audioRecordBufferSize = audioRecordMinBufferSize;
        }

        /* Get number of samples. Calculate the buffer size
         * (round up to the factor of given frame size)
		 * 使能被整除，方便下面的周期性通知
		 * */
        int bytesPerFrame = pcmFormat.getBytesPerFrame();
        int frameSize = audioRecordBufferSize / bytesPerFrame;

        if (frameSize % FRAME_COUNT != 0) {
            frameSize += (FRAME_COUNT - frameSize % FRAME_COUNT);
            audioRecordBufferSize = frameSize * bytesPerFrame;
        }

        audioRecordBuffer = new short[audioRecordBufferSize];

        double sampleNumberInOneMicrosecond = (double) Constant.RecordSampleRate / 1000;

        realSampleDuration = audioRecordBufferSize * 1000 /
                (Constant.RecordSampleRate * pcmFormat.getBytesPerFrame());

        realSampleNumberInOneDuration = (int) (sampleNumberInOneMicrosecond * realSampleDuration);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, Constant.RecordSampleRate,
                AudioFormat.CHANNEL_IN_MONO, pcmFormat.getAudioFormat(), audioRecordBufferSize);
    }

    public void release() {
        if (recordThread != null) {
            recordThread.release();
        }
    }

    /**
     * Start recording. Create an encoding thread. Start record from this
     * thread.
     */
    public boolean startRecordVoice(String recordFileUrl) {
        try {
            recordThread.startRecordVoice(recordFileUrl);
            return true;
        } catch (Exception e) {
            Log.e(TAG,"开始录音异常");

        }

        return false;
    }

    public boolean stopRecordVoice() {
        if (recordThread != null) {
            recordThread.stopRecordVoice();
        }

        return true;
    }

    /**
     * 此计算方法来自samsung开发范例
     *
     * @param buffer   buffer
     * @param readSize readSize
     */
    private void calculateRealVolume(short[] buffer, int readSize) {
        int sum = 0;

        for (int index = 0; index < readSize; index++) {
            // 这里没有做运算的优化，为了更加清晰的展示代码
            sum += Math.abs(buffer[index]);
        }

        if (readSize > 0) {
            amplitude = sum / readSize;
        }
    }

    public int getVolume() {
        int volume = (int) (Math.sqrt(amplitude)) * Constant.RecordVolumeMaxRank / 60;
        return volume;
    }

    private class RecordThread implements Runnable {
        private boolean running;
        private boolean recordVoice;

        private String recordFileUrl;

        public RecordThread() {
            running = true;
        }

        public void startRecordVoice(String recordFileUrl) throws IOException {
            if (!running) {
                return;
            }

            this.recordFileUrl = recordFileUrl;

            recordVoice = true;
        }

        public void stopRecordVoice() {
            recordVoice = false;
        }

        public void release() {
            running = false;
            recordVoice = false;
        }

        private void NoRecordPermission() {
//            PermissionFunction.ShowCheckPermissionNotice("录音");
            Log.e("mp3Recorder","无录音权限");
            stopRecordVoice();
        }

        @Override
        public void run() {
            while (running) {
                if (recordVoice) {
                    audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                            Constant.RecordSampleRate, AudioFormat.CHANNEL_IN_MONO,
                            pcmFormat.getAudioFormat(), audioRecordBufferSize);

                    try {
                        audioRecord.startRecording();
                    } catch (Exception e) {
                        NoRecordPermission();
                        continue;
                    }

                    BufferedOutputStream bufferedOutputStream = FileFunction
                            .GetBufferedOutputStreamFromFile(recordFileUrl);

                    while (recordVoice) {
                        int audioRecordReadDataSize =
                                audioRecord.read(audioRecordBuffer, 0, audioRecordBufferSize);

                        if (audioRecordReadDataSize > 0) {
                            calculateRealVolume(audioRecordBuffer, audioRecordReadDataSize);
                            if (bufferedOutputStream != null) {
                                try {
                                    byte[] outputByteArray = CommonFunction
                                            .GetByteBuffer(audioRecordBuffer,
                                                    audioRecordReadDataSize, Variable.isBigEnding);
                                    bufferedOutputStream.write(outputByteArray);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            NoRecordPermission();
                            continue;
                        }
                    }

                    if (bufferedOutputStream != null) {
                        try {
                            bufferedOutputStream.close();
                        } catch (Exception e) {
                        }
                    }

                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;
                }

                try {
                    Thread.sleep(recordSleepDuration);
                } catch (Exception e) {
                }
            }
        }
    }
}