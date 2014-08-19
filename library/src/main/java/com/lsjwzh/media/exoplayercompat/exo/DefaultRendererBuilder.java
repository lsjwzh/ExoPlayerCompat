/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lsjwzh.media.exoplayercompat.exo;

import android.content.Context;
import android.media.MediaCodec;
import android.net.Uri;

import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;

/**
 * A {@link } for streams that can be read using
 * {@link android.media.MediaExtractor}.
 */
public class DefaultRendererBuilder implements ExoPlayerWrapper.RendererBuilder {


    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int VIDEO_BUFFER_SEGMENTS = 200;
    private static final int AUDIO_BUFFER_SEGMENTS = 60;
  private final Context context;
  private final Uri uri;
//  private final TextView debugTextView;

  public DefaultRendererBuilder(Context context, Uri uri) {
    this.context = context;
    this.uri = uri;
//    this.debugTextView = debugTextView;
  }

  @Override
  public void buildRenderers(ExoPlayerWrapper player, ExoPlayerWrapper.RendererBuilderCallback callback) {
    // Build the video and audio renderers.
//      LoadControl loadControl =new DefaultLoadControl(
//              new BufferPool(BUFFER_SEGMENT_SIZE));
//      DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(player.getMainHandler(), player);
//      Representation[] videoRepresentations = new Representation[1];
//      MediaExtractor extractor = new MediaExtractor();
//      try {
//          extractor.setDataSource(context, uri, null);
//      } catch (IOException e) {
//          e.printStackTrace();
//      }
//      int[] trackStates = new int[extractor.getTrackCount()];
//      TrackInfo[] trackInfos = new TrackInfo[trackStates.length];
//      for (int i = 0; i < trackStates.length; i++) {
//          android.media.MediaFormat format = extractor.getTrackFormat(i);
//          long duration = format.containsKey(android.media.MediaFormat.KEY_DURATION) ?
//                  format.getLong(android.media.MediaFormat.KEY_DURATION) : TrackRenderer.UNKNOWN_TIME_US;
//          String mime = format.getString(android.media.MediaFormat.KEY_MIME);
//          trackInfos[i] = new TrackInfo(mime, duration);
//      }
//      videoRepresentations[0] = Representation.SingleSegmentRepresentation.newInstance(0, -1, uri.toString(), 0, format,uri,0,0,0,0,-1);
//// Build the video renderer.
//      String userAgent = "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
//      DataSource videoDataSource =new HttpDataSource(userAgent,
//              HttpDataSource.REJECT_PAYWALL_TYPES, bandwidthMeter);
//      ChunkSource videoChunkSource =new DashWebmChunkSource(videoDataSource,
//              new FormatEvaluator.AdaptiveEvaluator(bandwidthMeter), videoRepresentations);
//      ChunkSampleSource videoSampleSource =new ChunkSampleSource(videoChunkSource,
//              loadControl, VIDEO_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE,true){
//
//      };
//    MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(videoSampleSource,
//        null, true, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 5000,
//        player.getMainHandler(), player, 50);
//    MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(videoSampleSource,
//        null, true, player.getMainHandler(), player);

      FrameworkSampleSource sampleSource = new FrameworkSampleSource(context, uri, null, 1);
      MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(sampleSource,
              null, true, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 5000,
              player.getMainHandler(), player, 50);
      MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource,
              null, true, player.getMainHandler(), player);

    // Invoke the callback.
    TrackRenderer[] renderers = new TrackRenderer[ExoPlayerWrapper.RENDERER_COUNT];
    renderers[ExoPlayerWrapper.TYPE_VIDEO] = videoRenderer;
    renderers[ExoPlayerWrapper.TYPE_AUDIO] = audioRenderer;
//    renderers[DemoPlayer.TYPE_DEBUG] = debugRenderer;
    callback.onRenderers(null, null, renderers);
  }

}
