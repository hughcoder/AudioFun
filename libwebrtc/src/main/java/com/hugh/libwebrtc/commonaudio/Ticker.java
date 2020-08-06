package com.hugh.libwebrtc.commonaudio;


public class Ticker {
    public void resetTime(){
        mStartTime = System.currentTimeMillis();
    }
    public long elapsedTime(){
        return System.currentTimeMillis() - mStartTime;
    }

    public static synchronized Ticker Instance(){
        if(instance == null){
            instance = new Ticker();
        }
        return instance;
    }

    public Ticker(){
        resetTime();
    }

    private long mStartTime;
    private static Ticker instance;
}
