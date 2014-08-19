package com.lsjwzh.media.exoplayercompat;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Created by panwenye on 14-8-20.
 */
public class MediaMonitor implements Runnable {
    private final Object mLock = new Object();
    public Runnable task;
    volatile boolean isRunning = false;
    //记录monitorCurrentDialogItem相关的暂停状态计时器
    Handler innerHanlder;
    int runCount = 0;
    private Looper mLooper;
    private Runnable taskToWatchProgress = new Runnable() {
        @Override
        public void run() {
            synchronized (mLock) {
                if(task!=null){
                    try{
                        task.run();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                registerLoop();
            }
        }
    };

    /**
     * Creates a worker thread with the given name. The thread
     * then runs a {@link android.os.Looper}.
     */
    public MediaMonitor() {
        Thread t = new Thread(null, this, "monitor");

        t.start();
        synchronized (mLock) {
            while (mLooper == null) {
                try {
                    mLock.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        innerHanlder = new Handler(getLooper());
    }

    public Looper getLooper() {
        return mLooper;
    }

    private void registerLoop() {
        if (isRunning) {
            innerHanlder.removeCallbacksAndMessages(null);
            innerHanlder.postDelayed(taskToWatchProgress, 100);
        }
    }

    public void start() {
        synchronized (mLock) {
            if (!isRunning) {
                isRunning = true;
                innerHanlder.removeCallbacksAndMessages(null);
                innerHanlder.postDelayed(taskToWatchProgress, 100);
                Log.d("taskToWatchProgress", "start monitor");
            }
        }
    }

    public void run() {
        synchronized (mLock) {
            Looper.prepare();
            mLooper = Looper.myLooper();
            mLock.notifyAll();
        }
        Looper.loop();
    }

    public void pause() {
        synchronized (mLock) {
            isRunning = false;
        }
    }

    public void quit() {
        mLooper.quit();
    }


}

