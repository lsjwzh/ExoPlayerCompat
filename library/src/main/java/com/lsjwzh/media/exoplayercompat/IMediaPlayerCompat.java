package com.lsjwzh.media.exoplayercompat;

import android.media.MediaPlayer;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * interface for MediaPlayerCompat
 * Created by panwenye on 14-8-19.
 */
public interface IMediaPlayerCompat {
    public void setDataSource(String path);
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
    public void setOnVideoSizeChangedListener(MediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener);
    public void setVolume(float v1, float v2);
    public void setAudioStreamType(int streamMusic);

}
