package com.hugh.component.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by chenyw on 2020/8/10.
 */
public class AudioRecordManager {
    private static final String TAG = "AudioRecordManager";
    /*录音管理类实例*/
    public static AudioRecordManager sInstance;
    /*录音实例*/
    protected AudioRecord mAudioRecord;
    /*录音线程*/
    private Thread mRecordThread;
    /*音频采集的输入源，麦克风*/
    private static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    /*音频采集的采样频率*/
    public static int SAMPLE_RATE_IN_HZ = 44100;
    /*音频采集的声道数,此处为单声道，后期处理可以转换成双声道的立体声,如果这里是MONO声道的话，会有变声的情况*/
    private static int CHANNEL_CONFIGURATION = AudioFormat.CHANNEL_IN_STEREO;
    /*音频采集的格式，数据位宽16位*/
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    /*音频缓冲区大小*/
    private int mBufferSizeInBytes = 0;
    /*是否正在录音*/
    private boolean mIsRecording = false;
    /*文件输出流*/
    private FileOutputStream mFileOutputStream;
    /*文件输出路径*/
    private String mOutputFilePath;

    /*单例模式*/
    private AudioRecordManager(){}
    public static AudioRecordManager getInstance(){
        if (null == sInstance){
            synchronized (AudioRecordManager.class){
                if (null == sInstance){
                    sInstance = new AudioRecordManager();
                    return sInstance;
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化配置
     */
    public void initConfig() throws Exception {

        if (null != mAudioRecord) mAudioRecord.release();

        //使用44.1kHz的采样率初始化录音器
        try {
            mBufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,CHANNEL_CONFIGURATION,AUDIO_FORMAT);
            mAudioRecord = new AudioRecord(AUDIO_SOURCE,SAMPLE_RATE_IN_HZ,CHANNEL_CONFIGURATION,AUDIO_FORMAT,mBufferSizeInBytes);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Log.e(TAG,"采样率44.1kHZ的AudioRecord初始化失败");
        }

/*        //44.1kHz采样率没有成功的话，再使用16kHz的采样率初始化录音器
        if (mAudioRecord == null || mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED){
            try {
                SAMPLE_RATE_IN_HZ = 16000;
                mBufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,CHANNEL_CONFIGURATION,AUDIO_FORMAT);
                mAudioRecord = new AudioRecord(AUDIO_SOURCE,SAMPLE_RATE_IN_HZ,CHANNEL_CONFIGURATION,AUDIO_FORMAT,mBufferSizeInBytes);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Log.e(TAG,"采样率16kHZ的AudioRecord初始化失败");
            }
        }*/

        //都失败的话，抛出异常
        if (mAudioRecord == null || mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) throw new Exception();

    }

    /**
     * 开始录音
     */
    public void startRecord(String filePath) throws Exception {

        if (mAudioRecord != null && mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED){
            try {
                mAudioRecord.startRecording();
            } catch (Exception e) {
                throw new Exception();
            }
        }else {
            throw new Exception();
        }

        mIsRecording = true;
        mRecordThread = new Thread(new RecordRunnable(),"RecordThread");

        try {
            this.mOutputFilePath = filePath;
            mRecordThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    /**
     * 结束录音
     */
    public void stopRecord(){
        try {
            if (mAudioRecord != null){

                mIsRecording = false;

                //关闭线程
                try {
                    if (mRecordThread != null){
                        mRecordThread.join();
                        mRecordThread = null;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //关闭录音，释放资源
                releaseAudioRecord();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭录音，释放资源
     */
    private void releaseAudioRecord(){
        if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }


    class RecordRunnable implements Runnable{
        @Override
        public void run() {
            try {
                mFileOutputStream = new FileOutputStream(mOutputFilePath);
                byte[] audioDataArray = new byte[mBufferSizeInBytes];
                while (mIsRecording){
                    int audioDataSize = getAudioRecordBufferSize(mBufferSizeInBytes,audioDataArray);
                    if (audioDataSize > 0) {
                        mFileOutputStream.write(audioDataArray);
                    }else {
                        mIsRecording = false;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if (null != mFileOutputStream){
                        mFileOutputStream.close();
                        mFileOutputStream = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取缓存区的大小
     * @param bufferSizeInBytes
     * @param audioDataArray
     * @return
     */
    private int getAudioRecordBufferSize(int bufferSizeInBytes, byte[] audioDataArray) {
        if (mAudioRecord != null){
            int size = mAudioRecord.read(audioDataArray,0,bufferSizeInBytes);
            return size;
        }else {
            return 0;
        }
    }

    /**
     * 是否正在录音
     */
    public boolean isRecording(){
        return mIsRecording;
    }
}
