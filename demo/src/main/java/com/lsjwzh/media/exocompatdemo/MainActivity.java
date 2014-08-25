package com.lsjwzh.media.exocompatdemo;

import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.VideoSurfaceView;
import com.lsjwzh.media.exoplayercompat.MediaPlayerCompat;
import com.lsjwzh.media.exoplayercompat.exo.DefaultRendererBuilder;
import com.lsjwzh.media.exoplayercompat.exo.ExoPlayerCompatImpl;
import com.lsjwzh.media.exoplayercompat.exo.ExoPlayerWrapper;


public class MainActivity extends ActionBarActivity implements SurfaceHolder.Callback,
        ExoPlayer.Listener{

    VideoSurfaceView surface_view;

    private MediaPlayerCompat player;
    private boolean playerNeedsPrepare;

    private boolean autoPlay = true;
    private int playerPosition;
    private boolean enableBackgroundAudio = false;
    private EventLogger eventLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surface_view = (VideoSurfaceView)findViewById(R.id.surface_view);
        surface_view.getHolder().addCallback(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        preparePlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!enableBackgroundAudio) {
            releasePlayer();
        } else {
            player.setDisplay(null);
        }
    }


    private void preparePlayer() {
        if (player == null) {
            player = new ExoPlayerCompatImpl();
            //new ExoPlayerWrapper(new DefaultRendererBuilder(this, Uri.parse("http://mofunsky-video.qiniudn.com/90/193/2014082012541850633.webm")));
//            player.addListener(this);
            player.setDataSource(this,"http://mofunsky-video.qiniudn.com/90/193/2014082012541850633.webm");
            player.seekTo(playerPosition);
            playerNeedsPrepare = true;
//            mediaController.setMediaPlayer(player.getPlayerControl());
//            mediaController.setEnabled(true);
            eventLogger = new EventLogger();
            eventLogger.startSession();
        }
        player.addListener(new MediaPlayerCompat.EventListener() {
            @Override
            public void onPrepared() {
                player.setDisplay(surface_view.getHolder());
                maybeStartPlayback();
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onPlayComplete() {

            }

            @Override
            public void onSeekComplete(long positionAfterSeek) {

            }

            @Override
            public void onPause() {

            }

            @Override
            public void onStop() {

            }

            @Override
            public void onReset() {

            }

            @Override
            public void onRelease() {

            }

            @Override
            public void onPositionUpdate(long position, long duration) {

            }

            @Override
            public void onVolumeChanged(float newV1, float newV2) {

            }

            @Override
            public void onBuffering(int percentage) {

            }

            @Override
            public void onError(Exception e) {

            }

            @Override
            public void onVideoSizeChanged(int width, int height) {

            }
        });
        if (playerNeedsPrepare) {
            player.prepareAsync();
            playerNeedsPrepare = false;

        }
    }

    private void maybeStartPlayback() {
        if (autoPlay && (surface_view.getHolder().getSurface().isValid())) {
            player.start();
            autoPlay = false;
        }
    }

    private void releasePlayer() {
        if (player != null) {
            playerPosition = (int) player.getCurrentPosition();
            player.release();
            player = null;
            eventLogger.endSession();
            eventLogger = null;
        }
    }

    // DemoPlayer.Listener implementation

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // SurfaceHolder.Callback implementation

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (player != null) {
            player.setDisplay(holder);
            maybeStartPlayback();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player != null) {
            player.setDisplay(null);
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onPlayWhenReadyCommitted() {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }
}
