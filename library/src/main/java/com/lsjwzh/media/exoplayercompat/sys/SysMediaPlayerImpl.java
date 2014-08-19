package com.lsjwzh.media.exoplayercompat.sys;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.SurfaceHolder;

import com.lsjwzh.media.exoplayercompat.MediaPlayerCompat;

/**
 * Created by panwenye on 14-8-20.
 */
public class SysMediaPlayerImpl extends MediaPlayerCompat {
    MediaPlayer mMediaPlayer;

    @Override
    public void setDataSource(Context context, String path) {

    }

    @Override
    public void prepare() {

    }

    @Override
    public void prepareAsync(Runnable onPreparedComplete) {

    }

    @Override
    public void start() {

    }

    @Override
    public void seekTo(long position) {

    }

    @Override
    public void seekTo(long position, Runnable seekCompleteCallback) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void reset() {

    }

    @Override
    public void release() {

    }

    @Override
    public long getCurrentPosition() {
        return 0;
    }

    @Override
    public long getDuration() {
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public boolean isPrepared() {
        return false;
    }

    @Override
    public boolean isReleased() {
        return false;
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {

    }

    @Override
    public void setVolume(float v1, float v2) {

    }

    @Override
    public void setAudioStreamType(int streamMusic) {

    }
}
