package com.lsjwzh.media.exoplayercompat;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.SurfaceHolder;

import com.lsjwzh.media.exoplayercompat.exo.ExoPlayerCompatImpl;
import com.lsjwzh.media.exoplayercompat.sys.SysMediaPlayerImpl;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * interface for MediaPlayerCompat
 * Created by panwenye on 14-8-19.
 */
public abstract class MediaPlayerCompat {
    public class UnknownMediaPlayerException extends Exception{
        public int what;
        public int extra;

    }
    public interface EventListener{
        void onPrepared();
        void onStart();
        void onPlayComplete();
        void onSeekComplete(long positionAfterSeek);
        void onPause();
        void onStop();
        void onReset();
        void onRelease();
        void onPositionUpdate(long position,long duration);
        void onVolumeChanged(float newV1,float newV2);
        void onBuffering(int loadedPercentage);
        void onError(Exception e);
        void onVideoSizeChanged(int width, int height);
    }
    public static MediaPlayerCompat exoMediaPlayerInstance(){
        return new ExoPlayerCompatImpl();
    }
    public static MediaPlayerCompat exoMediaPlayerInstance(boolean onlyAudio){
        return new ExoPlayerCompatImpl(onlyAudio);
    }

    public static MediaPlayerCompat sysMediaPlayerInstance(){
        return new SysMediaPlayerImpl();
    }

    final LinkedList<EventListener> mListeners = new LinkedList<EventListener>();


    /**
     * @param context only needed in ExoPlayer,SysMediaPlayer will ignore this arg
     * @param path data source uri
     */
    public abstract void setDataSource(Context context,String path);
    /**
     * prepare the media.
     */
    public abstract void prepare();
    public abstract void prepareAsync();
    public abstract void start();
    public abstract void seekTo(long position);
//    public abstract void seekTo(long position,Runnable seekCompleteCallback);
    public abstract void pause();
    public abstract void stop();
    public abstract void reset();
    public abstract void release();
    public abstract long getCurrentPosition();
    public abstract long getDuration();
    public abstract boolean isPlaying();
    public abstract boolean isPrepared();
    public abstract boolean isReleased();

    public abstract void setDisplay(SurfaceHolder holder);
    public abstract void setVolume(float v1, float v2);
    public abstract void setAudioStreamType(int streamMusic);

    public void addListener(EventListener listener){
        if(!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }
    public void removeListener(EventListener listener){
        mListeners.remove(listener);
    }
    public List<EventListener> getListeners(){
        return mListeners;
    }
}
