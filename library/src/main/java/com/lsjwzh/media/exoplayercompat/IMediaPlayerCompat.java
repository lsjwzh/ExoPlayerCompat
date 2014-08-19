package com.lsjwzh.media.exoplayercompat;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * interface for MediaPlayerCompat
 * Created by panwenye on 14-8-19.
 */
public interface IMediaPlayerCompat {
    public interface EventListener{
        void onPrepared();
        void onStart();
        void onPlayComplete();
        void onSeekComplete(long positionBeforeSeek,long positionAfterSeek);
        void onPause();
        void onStop();
        void onReset();
        void onRelease();
        void onPositionUpdate(long position,long duration);
        void onVolumeChanged(float v1,float v2,float newV1,float newV2);
    }
    public void setDataSource(Context context,String path);
    public void prepare();
    public void prepareAsync(Runnable onPreparedComplete);
    public void start();
    public void seekTo(long position);
    public void seekTo(long position,Runnable seekCompleteCallback);
    public void pause();
    public void stop();
    public void reset();
    public void release();
    public long getCurrentPosition();
    public long getDuration();
    public boolean isPlaying();
    public boolean isPrepared();
    public boolean isReleased();

    public void setDisplay(SurfaceHolder holder);
    public void setVolume(float v1, float v2);
    public void setAudioStreamType(int streamMusic);

}
