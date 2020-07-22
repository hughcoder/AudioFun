package com.hugh.sound.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;


import com.hugh.sound.common.schedulers.SchedulerProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;


public class AudioPlayerHelper {

    public static final int STATUS_ERROR = 0;
    public static final int STATUS_START = 1;
    public static final int STATUS_PAUSE = 2;
    public static final int STATUS_RESUME = 3;
    public static final int STATUS_STOP = 4;
    public static final int STATUS_COMPLETE = 5;

    private MediaPlayer mAudioPlayer;
    private String mCurrentAudio;
    private Disposable mDisposable;
    private List<AudioPlayListener> mAudioPlayListenerList;

    public AudioPlayerHelper() {
        init();
    }

    private void init() {
        mAudioPlayListenerList = new ArrayList<>();
        mAudioPlayer = new MediaPlayer();
        mAudioPlayer.setOnPreparedListener(mp -> {
            mAudioPlayer.start();
            for (AudioPlayListener audioPlayListener : mAudioPlayListenerList) {
                if (audioPlayListener != null) {
                    audioPlayListener.onAudioStatus(mCurrentAudio, STATUS_START);
                }
            }
            mDisposable = Flowable.interval(0, 50, TimeUnit.MILLISECONDS)
                    .onBackpressureDrop()
                    .subscribeOn(SchedulerProvider.getInstance().computation())
                    .observeOn(SchedulerProvider.getInstance().ui())
                    .doOnNext(aLong -> {
                        if (mAudioPlayer != null && mAudioPlayer.isPlaying()) {
                            int position = mAudioPlayer.getCurrentPosition();
                            int duration = mAudioPlayer.getDuration();
                            for (AudioPlayListener audioPlayListener : mAudioPlayListenerList) {
                                if (audioPlayListener != null) {
                                    audioPlayListener.onAudioProgress(mCurrentAudio, position, duration);
                                }
                            }
                        }

                    })
                    .doOnError(throwable -> Log.e("aaa",throwable.getMessage())).subscribe();
        });
        mAudioPlayer.setOnCompletionListener(mp -> {
            if (mDisposable != null) {
                mDisposable.dispose();
            }
            if (mCurrentAudio != null) {
                for (AudioPlayListener audioPlayListener : mAudioPlayListenerList) {
                    if (audioPlayListener != null) {
                        audioPlayListener.onAudioStatus(mCurrentAudio, STATUS_STOP);
                        audioPlayListener.onAudioStatus(mCurrentAudio, STATUS_COMPLETE);
                    }
                }
            }
        });
    }

    public void playAudio(String audio) {
        playSameAudio(audio);
    }

    public void playOrStop(String audio) {
        if (!isEmpty(audio)) {
            if (mAudioPlayer.isPlaying()) {
                stop();
                if (!audio.equals(mCurrentAudio)) {
                    play(audio);
                }
            } else {
                play(audio);
            }
            mCurrentAudio = audio;
        } else {
            Log.w("aaa","audio is empty");
        }
    }

    public void playSameAudio(String audio) {
        if (! isEmpty(audio)) {
            if (mAudioPlayer.isPlaying()) {
                stop();
                play(audio);
            } else {
                play(audio);
            }
            mCurrentAudio = audio;
        } else {
            Log.w("aaa","audio is empty");
        }
    }

    public void pause() {
        if (mAudioPlayer != null) {
            mAudioPlayer.pause();
        }
        if (mCurrentAudio != null) {
            for (AudioPlayListener audioPlayListener : mAudioPlayListenerList) {
                if (audioPlayListener != null) {
                    audioPlayListener.onAudioStatus(mCurrentAudio, STATUS_PAUSE);
                }
            }
        }
    }

    public void resume() {
        mAudioPlayer.start();
        if (mCurrentAudio != null) {
            for (AudioPlayListener audioPlayListener : mAudioPlayListenerList) {
                if (audioPlayListener != null) {
                    audioPlayListener.onAudioStatus(mCurrentAudio, STATUS_START);
                }
            }
        }
    }

    public void stop() {
        if (mAudioPlayer != null) {
            mAudioPlayer.stop();
        }
        if (mCurrentAudio != null) {
            for (AudioPlayListener audioPlayListener : mAudioPlayListenerList) {
                if (audioPlayListener != null) {
                    audioPlayListener.onAudioStatus(mCurrentAudio, STATUS_STOP);
                }
            }
        }
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    public boolean isPlaying() {
        return mAudioPlayer.isPlaying();
    }

    private void play(String audio) {
        try {
            mAudioPlayer.reset();
            mAudioPlayer.setDataSource(audio);
            mAudioPlayer.prepareAsync();
        } catch (IOException e) {
            for (AudioPlayListener audioPlayListener : mAudioPlayListenerList) {
                if (audioPlayListener != null) {
                    audioPlayListener.onAudioStatus(mCurrentAudio, STATUS_ERROR);
                }
            }
        }
    }

    public void playAudio(Context context, Uri uri) {
        mCurrentAudio = uri.toString();
        try {
            mAudioPlayer.reset();
            mAudioPlayer.setDataSource(context, uri);
            mAudioPlayer.prepareAsync();
        } catch (IOException e) {
            for (AudioPlayListener audioPlayListener : mAudioPlayListenerList) {
                if (audioPlayListener != null) {
                    audioPlayListener.onAudioStatus(mCurrentAudio, STATUS_ERROR);
                }
            }
        }
    }


    public void playSameLocalAudio(String audio) {
        if (! isEmpty(audio)) {
            if (mAudioPlayer.isPlaying()) {
                stop();
                playLocal(audio);
            } else {
                playLocal(audio);
            }
            mCurrentAudio = audio;
        } else {
            Log.w("aaa","audio is empty");
        }
    }

    private void playLocal(String audio) {
        try {
            mAudioPlayer.reset();
            mAudioPlayer.setDataSource(audio);
            mAudioPlayer.prepareAsync();
        } catch (IOException e) {
//            FZLogger.e(e.getMessage());
            for (AudioPlayListener audioPlayListener : mAudioPlayListenerList) {
                if (audioPlayListener != null) {
                    audioPlayListener.onAudioStatus(mCurrentAudio, STATUS_ERROR);
                }
            }
        }
    }

    public void seekTo(int msec) {
        if (mAudioPlayer != null) {
            mAudioPlayer.seekTo(msec);
        }
    }

    public void addAudioPlayListener(AudioPlayListener audioPlayListener) {
        mAudioPlayListenerList.add(audioPlayListener);
    }

    public void removeAudioPlayListener(AudioPlayListener audioPlayListener) {
        mAudioPlayListenerList.remove(audioPlayListener);
    }

    public void destroy() {
        if (mAudioPlayer != null) {
            mAudioPlayer.stop();
            mAudioPlayer.release();
            mAudioPlayer = null;
        }
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mAudioPlayListenerList.clear();
    }
    static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public interface AudioPlayListener {

        default void onAudioProgress(String audio, int position, int total) {}

        default void onAudioStatus(String audio, int status) {}
    }

}
