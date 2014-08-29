package com.lsjwzh.media.exoplayercompat.exo;

import android.content.Context;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.SurfaceHolder;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.lsjwzh.media.exoplayercompat.MediaMonitor;
import com.lsjwzh.media.exoplayercompat.MediaPlayerCompat;

import java.io.IOException;

/**
 * Created by panwenye on 14-8-18.
 */
 public class ExoPlayerCompatImpl extends MediaPlayerCompat {
    private static final String TAG = ExoPlayerCompatImpl.class.getSimpleName();
    ExoPlayerWrapper mExoPlayer;
    SurfaceHolder holder;
    private boolean isPrepared;
    private boolean isReleased;
    private boolean isStartPrepare = false;
    Handler handler = new Handler();
    private MediaMonitor mMediaMonitor;
    private boolean mIsBuffering;
    private boolean mOnlyAudio;


    public ExoPlayerCompatImpl() {
    }
    public ExoPlayerCompatImpl(boolean onlyAudio) {
        mOnlyAudio = onlyAudio;
    }

    @Override
    public boolean isPlaying() {
        try {
            return mExoPlayer.getPlayerControl().isPlaying();
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
    public void seekTo(long position) {
        try {
            mExoPlayer.seekTo((int) position);
            for(EventListener listener : getListeners()){
                listener.onSeekComplete(position);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void start() {
        try {
            mExoPlayer.getPlayerControl().start();
            if(mMediaMonitor!=null){
                mMediaMonitor.start();
            }
            for(EventListener listener : getListeners()){
                listener.onStart();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void pause() {
        //avoid pause called in state 8
        if (!mExoPlayer.getPlayerControl().isPlaying()) {
            return;
        }
        mExoPlayer.getPlayerControl().pause();
        if(mMediaMonitor!=null){
            mMediaMonitor.pause();
        }
        for(EventListener listener : getListeners()){
            listener.onPause();
        }
    }

    @Override
    public void stop() {
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            if(mMediaMonitor!=null){
                mMediaMonitor.pause();
            }
            for(EventListener listener : getListeners()){
                listener.onStop();
            }
        }
    }

    @Override
    public void release() {
        if(mMediaMonitor!=null){
            mMediaMonitor.quit();
        }
        //ensure that player do not release at ui thread to avoid ANR in some rom
        if(Looper.myLooper()==Looper.getMainLooper()){
            new Thread(){
                @Override
                public void run() {
                    if(mExoPlayer!=null) {
                        mExoPlayer.release();
                        mExoPlayer = null;
                        isReleased = true;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(ExoPlayerCompatImpl.this==null){
                                        return;
                                    }
                                    for(EventListener listener : getListeners()) {
                                        listener.onRelease();
                                    }
                                }
                            });

                    }
                }
            }.start();
        }else {
            mExoPlayer.release();
            mExoPlayer = null;
            isReleased = true;

            for(EventListener listener : getListeners()){
                listener.onRelease();
            }
        }
    }

    @Override
    public long getCurrentPosition() {
        return mExoPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return mExoPlayer.getDuration();
    }

    public void prepare() {
        if (isStartPrepare) {
            return;
        }
        isStartPrepare = true;
        mExoPlayer.prepare();
    }

    @Override
    public void prepareAsync() {
        if (isStartPrepare) {
            return;
        }
        isStartPrepare = true;
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                mExoPlayer.prepare();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
            }
        }.execute();
    }

    public boolean isReleased() {
        return isReleased;
    }

    @Override
    public void setDataSource(Context context, String path) {
        mExoPlayer = new ExoPlayerWrapper(new DefaultRendererBuilder(context, Uri.parse(path),mOnlyAudio));
        mExoPlayer.addListener(new ExoPlayerWrapper.Listener() {
            @Override
            public void onStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == ExoPlayer.STATE_ENDED) {
                    for(EventListener listener : getListeners()){
                        listener.onPlayComplete();
                    }
                }else if (playbackState == ExoPlayer.STATE_BUFFERING) {
                    mIsBuffering = true;
                    int bufferedPercentage = mExoPlayer.getBufferedPercentage();
                    if(bufferedPercentage==100){
                        mIsBuffering = false;
                    }
                    //xx 很可能到不了100%？
                    for(EventListener listener : getListeners()){
                        listener.onBuffering(bufferedPercentage);
                    }
                }else if(mIsBuffering && playbackState == ExoPlayer.STATE_READY){
                    mIsBuffering = false;
                    for(EventListener listener : getListeners()){
                        listener.onBuffering(100);
                    }
                }
                if(isStartPrepare && playbackState == ExoPlayer.STATE_READY){
                    isStartPrepare = false;
                    isPrepared = true;
                    for(EventListener listener : getListeners()){
                        listener.onPrepared();
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                for(EventListener listener : getListeners()){
                    listener.onError(e);
                }
            }

            @Override
            public void onVideoSizeChanged(int width, int height) {
                for(EventListener listener : getListeners()){
                    listener.onVideoSizeChanged(width,height);
                }
            }
        });
        mExoPlayer.setInternalErrorListener(new ExoPlayerWrapper.InternalErrorListener() {
            @Override
            public void onRendererInitializationError(Exception e) {
                for(EventListener listener : getListeners()){
                    listener.onError(e);
                }
            }

            @Override
            public void onAudioTrackInitializationError(MediaCodecAudioTrackRenderer.AudioTrackInitializationException e) {
                for(EventListener listener : getListeners()){
                    listener.onError(e);
                }
            }

            @Override
            public void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e) {
                for(EventListener listener : getListeners()){
                    listener.onError(e);
                }
            }

            @Override
            public void onCryptoError(MediaCodec.CryptoException e) {
                for(EventListener listener : getListeners()){
                    listener.onError(e);
                }
            }

            @Override
            public void onUpstreamError(int sourceId, IOException e) {
                for(EventListener listener : getListeners()){
                    listener.onError(e);
                }
            }

            @Override
            public void onConsumptionError(int sourceId, IOException e) {
                for(EventListener listener : getListeners()){
                    listener.onError(e);
                }
            }

            @Override
            public void onDrmSessionManagerError(Exception e) {
                for(EventListener listener : getListeners()){
                    listener.onError(e);
                }
            }
        });
        //use MediaMonitor to update position change
        if(mMediaMonitor==null) {
            mMediaMonitor = new MediaMonitor();
            mMediaMonitor.task = new Runnable() {
                @Override
                public void run() {
                    if (mExoPlayer != null) {
                        int currentPosition = mExoPlayer.getCurrentPosition();
                        int duration = mExoPlayer.getDuration();
                        for (EventListener listener : getListeners()) {
                            listener.onPositionUpdate(currentPosition, duration);
                        }
                    }
                }
            };
        }
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        try {
            mExoPlayer.setSurface(holder == null ? null : holder.getSurface());
        } catch (Exception e) {
            e.printStackTrace();
            this.holder = null;
        }

    }

    @Override
    public void reset() {
        isPrepared = false;
        this.setDisplay(null);
        if(mMediaMonitor!=null){
            mMediaMonitor.pause();
        }
        for(EventListener listener : getListeners()){
            listener.onReset();
        }
    }


    @Override
    public void setVolume(float v, float v1) {
        mExoPlayer.setVolume(v);

        for(EventListener listener : getListeners()){
            listener.onVolumeChanged(v,v);
        }
    }

    @Override
    public void setAudioStreamType(int streamMusic) {
        //exoplayer have not this method
//        mediaPlayer.setAudioStreamType(streamMusic);
    }

}

