package com.lsjwzh.media.exoplayercompat.exo;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.SurfaceHolder;

import com.google.android.exoplayer.ExoPlayer;
import com.lsjwzh.media.exoplayercompat.MediaPlayerCompat;

/**
 * Created by panwenye on 14-8-18.
 */
 class ExoPlayerCompatImpl extends MediaPlayerCompat {
    private static final String TAG = ExoPlayerCompatImpl.class.getSimpleName();
    ExoPlayerWrapper mExoPlayer;
    SurfaceHolder holder;
    private boolean isPrepared;
    private boolean isReleased;
    //标记是否开始了prepare过程
    private boolean isStartPrepare = false;

    public ExoPlayerCompatImpl() {
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
    public void prepareAsync(final Runnable runnable) {
        if (!isStartPrepare) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    prepare();
                    return null;
                }
                @Override
                protected void onPostExecute(Void aVoid) {
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            }.execute();
        }
        isStartPrepare = true;
    }

    @Override
    public void seekTo(long position) {
        try {
            mExoPlayer.seekTo((int) position);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void seekTo(long position, final Runnable seekCompleteCallback) {
        if (getCurrentPosition() == position) {
            seekCompleteCallback.run();
            return;
        }
        seekTo(position);
        seekCompleteCallback.run();

    }

    @Override
    public void start() {
        try {
            mExoPlayer.getPlayerControl().start();
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
    }

    @Override
    public void stop() {
        if (mExoPlayer != null) {
            mExoPlayer.stop();
        }
    }

    @Override
    public void release() {
        mExoPlayer.release();
        mExoPlayer = null;
        isReleased = true;
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
        mExoPlayer.prepare();
        isStartPrepare = false;
        isPrepared = true;
    }

    public boolean isReleased() {
        return isReleased;
    }

    @Override
    public void setDataSource(Context context, String path) {
        mExoPlayer = new ExoPlayerWrapper(new DefaultRendererBuilder(context, Uri.parse(path)));
        mExoPlayer.addListener(new ExoPlayerWrapper.Listener() {
            @Override
            public void onStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == ExoPlayer.STATE_ENDED) {
                }
            }

            @Override
            public void onError(Exception e) {
            }

            @Override
            public void onVideoSizeChanged(int width, int height) {
            }
        });
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
    }


    @Override
    public void setVolume(float v, float v1) {
        mExoPlayer.setVolume(v);
    }

    @Override
    public void setAudioStreamType(int streamMusic) {
        //exoplayer have not this method
//        mediaPlayer.setAudioStreamType(streamMusic);
    }

}

