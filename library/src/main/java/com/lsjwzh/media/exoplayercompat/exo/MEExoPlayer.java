package com.lsjwzh.media.exoplayercompat.exo;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.android.exoplayer.ExoPlayer;
import com.lsjwzh.media.exoplayercompat.IMediaPlayerCompat;
import com.memory.me.core.AppConfig;
import com.memory.me.core.AppEvent;
import com.memory.me.core.MEApplication;
import com.memory.me.entities.DialogItem;
import com.memory.me.media.IMediaPlayer;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;

/**
 * Created by panwenye on 14-8-18.
 */
public class MEExoPlayer implements IMediaPlayerCompat {
    private static final String TAG = MEExoPlayer.class.getSimpleName();
    ExoPlayerWrapper mExoPlayer;
    AtomicLong currentPosition = new AtomicLong(0);
    boolean isManualPlaying = false;
    SectionMediaMonitor monitor = new SectionMediaMonitor();
    Handler handler = new Handler();
    long startPosition;
    long opTime;
    boolean isVideoSizeChanged;
    //错误时最多重试次数
    int retryLimit = 1;
    //错误时最多重复次数
    int retryCount = 0;
    SurfaceHolder holder;
    MediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener;
    private boolean isPrepared;
    private long endPosition;
    private Runnable prepareCallBack;
    private long progressOffset;
    private boolean isReleased;
    private Runnable onPlayEndCallback;
    //更新进度
    Runnable taskToUpdateProgress = new Runnable() {
        @Override
        public void run() {
            if (isManualPlaying) {
                //if(System.currentTimeMillis()-opTime>200){
                currentPosition.set(mExoPlayer.getCurrentPosition());
                Log.d("MEMediaPlayer", "currentPosition" + currentPosition.get());
                //}

                if (currentPosition.get() >= endPosition && endPosition > 0) {
                    pause();
                    //单次触发onPlayEndCallback回调
                    if (onPlayEndCallback != null) {
                        //避免消息队列的原因，导致onPlayEndCallback冲突
                        execEndCallback(onPlayEndCallback);
                        onPlayEndCallback = null;
                    }
                    TimeSpanCompeteEvent event = new TimeSpanCompeteEvent();
                    event.sender = MEExoPlayer.this;
                    event.arg = new Long[]{startPosition, endPosition};
                    getEventBus().post(event);
                    return;
                }
                ProgressUpdateEvent progressUpdateEvent = new ProgressUpdateEvent();
                progressUpdateEvent.sender = MEExoPlayer.this;
                progressUpdateEvent.arg = currentPosition.get() + progressOffset;
                getEventBus().post(progressUpdateEvent);
            }
        }
    };
    //标记是否开始了prepare过程
    private boolean isStartPrepare = false;
    private String dataSource;

    public MEExoPlayer() {
        monitor.task = taskToUpdateProgress;
        //保持屏幕常亮
//        mediaPlayer.setScreenOnWhilePlaying(true);
        // mediaPlayer.setWakeMode();

//        mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
//            @Override
//            public boolean onInfo(MediaPlayer mp, int what, int extra) {
//                InfoNotifyEvent event = new InfoNotifyEvent();
//                event.sender = MEExoPlayer.this;
//                event.arg = new Integer[]{what, extra};
//                getEventBus().post(event);
//                return false;
//            }
//        });

    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void execEndCallback(final Runnable finalonPlayEndCallback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (finalonPlayEndCallback != null) {
                    finalonPlayEndCallback.run();
                }
            }
        });
    }

    @Override
    public boolean isPlaying() {
        try {
            return mediaPlayer.getPlayerControl().isPlaying();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isPrepared() {
        return isPrepared;
    }

    @Override
    public void prepareAsync() throws IOException {
        if(AppConfig.DEBUG) Log.d(TAG,"prepareAsync");
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
        opTime = System.currentTimeMillis();
        isStartPrepare = true;
    }

    /**
     *
     * @param startPos
     * @param endPos
     * @param onStartCallback 正式开始播放后调用的回调函数
     */
    @Override
    public void start(long startPos, long endPos, final Runnable onStartCallback, final Runnable onPlayEndCallback) {
        this.startPosition = startPos;
        this.endPosition = endPos;
        this.onPlayEndCallback = onPlayEndCallback;
        seekTo(startPos, new Runnable() {
            @Override
            public void run() {
                start();
                if (onStartCallback != null) {
                    onStartCallback.run();
                }
            }
        });
//        }
    }

    @Override
    public void seekTo(long position) {
        //ToDo 解决seek到0时，位置不对的问题
//        if (position == 0) {
//            position = 10;
//        }
        try {
            mediaPlayer.seekTo((int) position);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        opTime = System.currentTimeMillis();
    }

    @Override
    public void seekTo(long position, final Runnable seekCompleteCallback) {
        //ToDo 解决seek到0时，位置不对的问题
//        if (position == 0) {
//            position = 10;
//        }
        if (getCurrentPosition() == position) {
            seekCompleteCallback.run();
            return;
        }
//        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
//            @Override
//            public void onSeekComplete(MediaPlayer mp) {
//                mediaPlayer.setOnSeekCompleteListener(null);
//                seekCompleteCallback.run();
//            }
//        });
        seekTo(position);
        seekCompleteCallback.run();

    }

    @Override
    public void start() {
        try {
            mediaPlayer.getPlayerControl().start();
            monitor.start();
            opTime = System.currentTimeMillis();
            isManualPlaying = true;
            StartEvent event = new StartEvent();
            event.sender = MEExoPlayer.this;
            event.arg = null;
            getEventBus().post(event);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void pause() {
        //avoid pause called in state 8
        if (!mediaPlayer.getPlayerControl().isPlaying()) {
            return;
        }
        mediaPlayer.getPlayerControl().pause();
        opTime = System.currentTimeMillis();

        monitor.pause();
        isManualPlaying = false;
        handler.removeCallbacksAndMessages(null);
        PauseEvent event = new PauseEvent();
        event.sender = this;
        event.arg = getCurrentPosition();
        getEventBus().post(event);
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    @Override
    @DebugLog
    public void release() {
        //清理一切资源，尤其是异步或者其他线程的操作
        handler.removeCallbacksAndMessages(null);
        opTime = System.currentTimeMillis();
        monitor.task = null;
        monitor.quit();
        mediaPlayer.release();
        mediaPlayer = null;
        isReleased = true;
    }

    @Override
    public long getCurrentPosition() {
        return currentPosition.get();
    }

    @Override
    public long getDuration() {
        opTime = System.currentTimeMillis();
        return mediaPlayer.getDuration();
    }

    public void prepare() throws IOException {
        mediaPlayer.prepare();

        Log.d(TAG, "onPrepared");
        isStartPrepare = false;
        isPrepared = true;
            PrepareEvent event = new PrepareEvent();
            event.sender = MEExoPlayer.this;
            event.arg = null;
            getEventBus().post(event);
            if (prepareCallBack != null) {
                prepareCallBack.run();
            }
    }

    public void setProgressOffset(long progressOffset) {
        this.progressOffset = progressOffset;
    }

    public long getEndPosition() {
        return endPosition;
    }

    /**
     * 设置为0时，自动播放至手动停止
     *
     * @param endPosition
     */
    public void setEndPosition(long endPosition) {
        this.endPosition = endPosition;
    }

    public Runnable getPrepareCallBack() {
        return prepareCallBack;
    }

//    @Override
//    public void setOnVideoSizeChangedListener(MediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener) {
//        mediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
//    }

    public void setPrepareCallBack(Runnable prepareCallBack) {
        this.prepareCallBack = prepareCallBack;
    }

    public boolean isReleased() {
        return isReleased;
    }

    public void setReleased(boolean released) {
        isReleased = released;
    }

    public String getDataSource() {
        return dataSource;
    }

    @Override
    public void setDataSource(String path) throws IOException {
        opTime = System.currentTimeMillis();
        dataSource = path;
        mediaPlayer = new ExoPlayerWrapper(new DefaultRendererBuilder(MEApplication.get(), Uri.parse(path)));
        mediaPlayer.addListener(new ExoPlayerWrapper.Listener() {
            @Override
            public void onStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == ExoPlayer.STATE_ENDED) {
                    Log.d(TAG, "onCompletion");
                    monitor.pause();
                    isManualPlaying = false;
                    if (onPlayEndCallback != null) {
                        //避免消息队列的原因，导致onPlayEndCallback冲突
                        execEndCallback(onPlayEndCallback);
                        onPlayEndCallback = null;
                    }
                    //避免最后一句接收不到TimeSpanCompeteEvent事件
                    if (endPosition > 0) {
                        TimeSpanCompeteEvent event = new TimeSpanCompeteEvent();
                        event.sender = MEExoPlayer.this;
                        event.arg = new Long[]{startPosition, endPosition};
                        getEventBus().post(event);
                    }

                    CompleteEvent completeEvent = new CompleteEvent();
                    completeEvent.sender = MEExoPlayer.this;
                    completeEvent.arg = currentPosition.get();
                    getEventBus().post(completeEvent);
                }
            }

            @Override
            public void onError(Exception e) {
                if (AppConfig.DEBUG) Log.d(TAG, e.toString());
                if (retryCount < retryLimit && isStartPrepare) {
                    try {
                        SystemClock.sleep(50);
                        prepareAsync();
                    } catch (IOException e2) {
                        ErrorEvent event = new ErrorEvent();
                        event.sender = MEExoPlayer.this;
                        //IOException 用0,0，
                        event.arg = new Integer[]{0, 0};
                        getEventBus().post(event);
                    } catch (IllegalStateException e3) {
                        //ToDo 记录异常信息
                    }
                } else {
                    ErrorEvent event = new ErrorEvent();
                    event.sender = MEExoPlayer.this;
                    event.arg = new Integer[]{0, 0};
                    getEventBus().post(event);
                }
            }

            @Override
            public void onVideoSizeChanged(int width, int height) {
                if (onVideoSizeChangedListener != null) {
                    onVideoSizeChangedListener.onVideoSizeChanged(null, width, height);
                }
                isVideoSizeChanged = true;
            }
        });
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        opTime = System.currentTimeMillis();
        try {
//            if(holder!=this.holder){
            mediaPlayer.setSurface(holder==null?null:holder.getSurface());
//                this.holder = holder;
//            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            this.holder = null;
        }

    }

    @Override
    public void reset() {
        opTime = System.currentTimeMillis();
        currentPosition.set(0);
        isManualPlaying = false;
        isPrepared = false;
        this.setDisplay(null);
        startPosition = endPosition = 0;
//        mediaPlayer.getPlayerControl().();
        //ToDo
    }

    @Override
    public void setOnVideoSizeChangedListener(MediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener) {
        this.onVideoSizeChangedListener = onVideoSizeChangedListener;
    }

    @Override
    public void setVolume(float v, float v1) {
        mediaPlayer.setVolume(v);
    }

    @Override
    public void setAudioStreamType(int streamMusic) {
//        mediaPlayer.setAudioStreamType(streamMusic);
    }

    public class SectionMediaMonitor implements Runnable {
        private final Object mLock = new Object();
        public Runnable task;
        volatile boolean isRunning = false;
        //记录monitorCurrentDialogItem相关的暂停状态计时器
        Map<DialogItem, Date> pauseTimeMap = new HashMap<DialogItem, Date>();
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
        public SectionMediaMonitor() {
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
}

