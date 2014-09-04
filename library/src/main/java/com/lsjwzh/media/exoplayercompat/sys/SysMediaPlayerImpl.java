package com.lsjwzh.media.exoplayercompat.sys;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.SurfaceHolder;

import com.lsjwzh.media.exoplayercompat.MediaMonitor;
import com.lsjwzh.media.exoplayercompat.MediaPlayerCompat;

import java.io.IOException;

/**
 * Created by panwenye on 14-8-20.
 */
public class SysMediaPlayerImpl extends MediaPlayerCompat {
    StrongerMediaPlayer mMediaPlayer;
    private boolean mIsPrepared;
    private boolean mIsReleased;
    MediaMonitor mMediaMonitor;
    /**
     * seekTo will cause a error if mediaplayer have not started
     */
    private boolean mIsStarted;

    @Override
    public void setDataSource(Context context, String path) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new StrongerMediaPlayer(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    UnknownMediaPlayerException unknownMediaPlayerException = new UnknownMediaPlayerException();
                    unknownMediaPlayerException.what = what;
                    unknownMediaPlayerException.extra = extra;
                    return true;
                }
            });
            //use MediaMonitor to update position change
            mMediaMonitor = new MediaMonitor();
            mMediaMonitor.task = new Runnable() {
                @Override
                public void run() {
                    if (mMediaPlayer != null) {
                        int currentPosition = mMediaPlayer.getCurrentPosition();
                        int duration = mMediaPlayer.getDuration();
                        for (EventListener listener : getListeners()) {
                            listener.onPositionUpdate(currentPosition, duration);
                        }
                    }
                }
            };
            mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    //fix bug: BufferingUpdate still can been triggered when mediaplayer is playing,
                    if(isPlaying()){
                        return;
                    }
                    for (EventListener listener : getListeners()) {
                        listener.onBuffering(percent);
                    }
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    for (EventListener listener : getListeners()) {
                        listener.onPlayComplete();
                    }
                }
            });
        }
        try {
            mMediaPlayer.setDataSource(path);
        } catch (IOException e) {
            for (EventListener listener : getListeners()) {
                listener.onError(e);
            }
        }
    }

    @Override
    public void prepare() {
        try {
            mMediaPlayer.prepare();
            mIsPrepared = true;
        } catch (IOException e) {
            for (EventListener listener : getListeners()) {
                listener.onError(e);
            }
        }
    }

    @Override
    public void prepareAsync() {
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mIsPrepared = true;
                for (EventListener listener : getListeners()) {
                    listener.onPrepared();
                }
            }
        });
        mMediaPlayer.prepareAsync();
    }

    @Override
    public void start() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            mIsStarted = true;
            if (mMediaMonitor != null) {
                mMediaMonitor.start();
            }
            for (EventListener listener : getListeners()) {
                listener.onStart();
            }
        }
    }

    @Override
    public void seekTo(final long position) {
        //seekTo will cause a error if mediaplayer have not been started
        if(!mIsStarted){
            start();
            pause();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    for (EventListener listener : getListeners()) {
                        listener.onSeekComplete(position);
                    }
                }
            });
            mMediaPlayer.seekTo((int) position);
        }
    }

    @Override
    public void pause() {
        if(!mIsStarted){
            return;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            if (mMediaMonitor != null) {
                mMediaMonitor.pause();
            }
            for (EventListener listener : getListeners()) {
                listener.onPause();
            }
        }
    }

    @Override
    public void stop() {
        if(!mIsStarted){
            return;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            if (mMediaMonitor != null) {
                mMediaMonitor.pause();
            }
            for (EventListener listener : getListeners()) {
                listener.onStop();
            }
        }
    }

    @Override
    public void reset() {
        if(!mIsStarted){
            return;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            if (mMediaMonitor != null) {
                mMediaMonitor.pause();
            }
            for (EventListener listener : getListeners()) {
                listener.onReset();
            }
        }
    }

    @Override
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            if (mMediaMonitor != null) {
                mMediaMonitor.quit();
            }
            mIsReleased = true;
            for (EventListener listener : getListeners()) {
                listener.onRelease();
            }
        }
    }

    @Override
    public long getCurrentPosition() {
        if(!mIsStarted){
            return 0;
        }
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public long getDuration() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        if(!mIsStarted){
            return false;
        }
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public boolean isPrepared() {
        return mIsPrepared;
    }

    @Override
    public boolean isReleased() {
        return mIsReleased;
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setDisplay(holder);
        }
    }

    @Override
    public void setVolume(float v1, float v2) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(v1, v2);
        }
    }

    @Override
    public void setAudioStreamType(int streamMusic) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setAudioStreamType(streamMusic);
        }
    }
}
