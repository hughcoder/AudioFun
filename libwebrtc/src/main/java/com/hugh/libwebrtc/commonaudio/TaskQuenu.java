package com.hugh.libwebrtc.commonaudio;

import java.util.concurrent.LinkedBlockingDeque;

public class TaskQuenu {
    private Runnable mDoTask = new Runnable() {
        @Override
        public void run() {
            while(true){
                try {
                    Task task = mTaskQuenu.take();
                    task.run();
                } catch (ExitInterruptedException e){
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public static class ExitInterruptedException extends Exception {
        public ExitInterruptedException(String s) {
            super(s);
        }
    }

    public static interface Task{
        void run() throws ExitInterruptedException;
    }

    private Thread mThread = new Thread(mDoTask);
    private LinkedBlockingDeque<Task> mTaskQuenu = new LinkedBlockingDeque<Task>();

    public TaskQuenu(){
        super();
        mThread.start();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        exit();
    }
    public void exit(){
        if(mThread != null){
            try {
                mTaskQuenu.put(new Task() {
                    @Override
                    public void run() throws ExitInterruptedException {
                        throw new ExitInterruptedException("任务列队退出");
                    }
                });
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mThread = null;
        }
    }

    public void async(Task task){
        try {
            mTaskQuenu.put(task);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public int size(){
        return mTaskQuenu.size();
    }
    public void pollLast(){
        mTaskQuenu.pollLast();
    }
    public void clear(){mTaskQuenu.clear();}

}
