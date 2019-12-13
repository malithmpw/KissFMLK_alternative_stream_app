package com.fm.kiss;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.view.Surface;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;

public class MusicService extends Service {
    private final static String FOREGROUND_CHANNEL_ID = "foreground_channel_id";
    private final static int NOTIFICATION_ID_FOREGROUND_SERVICE = 94543534;

    private NotificationManager mNotificationManager;

    public static final String url = "http://s3.voscast.com:8404/;stream1472803644663/1";
    public static final String PLAY = "kiss.action.start";
    public static final String PAUSE = "kiss.action.pause";
    public static final String STOP = "kiss.action.stop";
    public static final String REFRESH = "kiss.action.refresh";
    public static final String START_RECORD = "kiss.action.start_record";
    public static final String STOP_RECORD = "kiss.action.stop_record";
    private ExoPlayer exoPlayer;
    public static boolean running = false;
    public static boolean isPaused = false;
    public static int kissAudioSessionId = -1;

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        if (intent.getAction() != null) {
            // if user starts the service
            switch (intent.getAction()) {
                case PLAY:
                    isPaused = false;
                    startForeground(NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification(false));
                    startStreaming();
                    break;
                case PAUSE:
                    startForeground(NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification(true));
                    if (!isPaused) {
                        isPaused = true;
                        stopStreaming();
                        sendStopBroadcast();
                    } else {
                        isPaused = false;
                        sendPlayingBroadcast();
                        startForeground(NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification(false));
                        startStreaming();
                    }
                    break;
                case STOP:
                    stopStreaming();
                    stopForeground(true);
                    sendStopBroadcast();
                    stopSelf();
                    MusicService.running = false;
                    break;
                case REFRESH:
                    Toast.makeText(getApplicationContext(), "Stream Refreshed!", Toast.LENGTH_SHORT).show();
                    stopStreaming();
                    startStreaming();
                    break;
                default:
                    stopForeground(true);
                    stopSelf();
            }
        }
        return START_NOT_STICKY;
    }

    private void sendStopBroadcast() {
        Intent localIntent = new Intent("KissAudioSession");
        localIntent.putExtra("StreamingStopped", true);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(localIntent);
    }

    private void sendPlayingBroadcast() {
        Intent localIntent = new Intent("KissAudioSession");
        localIntent.putExtra("StreamingStopped", false);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(localIntent);
    }


    private void initializeMediaPlayer() {

        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        DefaultLoadControl loadControl = new DefaultLoadControl();
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(getApplicationContext());
        exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
        ((SimpleExoPlayer) exoPlayer).addAnalyticsListener(new AnalyticsListener() {
            @Override
            public void onPlayerStateChanged(EventTime eventTime, boolean playWhenReady, int playbackState) {

            }

            @Override
            public void onTimelineChanged(EventTime eventTime, int reason) {

            }

            @Override
            public void onPositionDiscontinuity(EventTime eventTime, int reason) {

            }

            @Override
            public void onSeekStarted(EventTime eventTime) {

            }

            @Override
            public void onSeekProcessed(EventTime eventTime) {

            }

            @Override
            public void onPlaybackParametersChanged(EventTime eventTime, PlaybackParameters playbackParameters) {

            }

            @Override
            public void onRepeatModeChanged(EventTime eventTime, int repeatMode) {

            }

            @Override
            public void onShuffleModeChanged(EventTime eventTime, boolean shuffleModeEnabled) {

            }

            @Override
            public void onLoadingChanged(EventTime eventTime, boolean isLoading) {

            }

            @Override
            public void onPlayerError(EventTime eventTime, ExoPlaybackException error) {

            }

            @Override
            public void onTracksChanged(EventTime eventTime, TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadStarted(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {

            }

            @Override
            public void onLoadCompleted(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {

            }

            @Override
            public void onLoadCanceled(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {

            }

            @Override
            public void onLoadError(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData, IOException error, boolean wasCanceled) {

            }

            @Override
            public void onDownstreamFormatChanged(EventTime eventTime, MediaSourceEventListener.MediaLoadData mediaLoadData) {

            }

            @Override
            public void onUpstreamDiscarded(EventTime eventTime, MediaSourceEventListener.MediaLoadData mediaLoadData) {

            }

            @Override
            public void onMediaPeriodCreated(EventTime eventTime) {

            }

            @Override
            public void onMediaPeriodReleased(EventTime eventTime) {

            }

            @Override
            public void onReadingStarted(EventTime eventTime) {

            }

            @Override
            public void onBandwidthEstimate(EventTime eventTime, int totalLoadTimeMs, long totalBytesLoaded, long bitrateEstimate) {

            }

            @Override
            public void onViewportSizeChange(EventTime eventTime, int width, int height) {

            }

            @Override
            public void onNetworkTypeChanged(EventTime eventTime, @Nullable NetworkInfo networkInfo) {

            }

            @Override
            public void onMetadata(EventTime eventTime, Metadata metadata) {

            }

            @Override
            public void onDecoderEnabled(EventTime eventTime, int trackType, DecoderCounters decoderCounters) {

            }

            @Override
            public void onDecoderInitialized(EventTime eventTime, int trackType, String decoderName, long initializationDurationMs) {

            }

            @Override
            public void onDecoderInputFormatChanged(EventTime eventTime, int trackType, Format format) {

            }

            @Override
            public void onDecoderDisabled(EventTime eventTime, int trackType, DecoderCounters decoderCounters) {

            }

            @Override
            public void onAudioSessionId(EventTime eventTime, int audioSessionId) {
                kissAudioSessionId = audioSessionId;
                Intent localIntent = new Intent("KissAudioSession");
                localIntent.putExtra("KissAudioSessionId", audioSessionId);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(localIntent);
            }

            @Override
            public void onAudioUnderrun(EventTime eventTime, int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {

            }

            @Override
            public void onDroppedVideoFrames(EventTime eventTime, int droppedFrames, long elapsedMs) {

            }

            @Override
            public void onVideoSizeChanged(EventTime eventTime, int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

            }

            @Override
            public void onRenderedFirstFrame(EventTime eventTime, Surface surface) {

            }

            @Override
            public void onDrmKeysLoaded(EventTime eventTime) {

            }

            @Override
            public void onDrmSessionManagerError(EventTime eventTime, Exception error) {

            }

            @Override
            public void onDrmKeysRestored(EventTime eventTime) {

            }

            @Override
            public void onDrmKeysRemoved(EventTime eventTime) {

            }
        });
    }

    private void startStreaming() {
        initializeMediaPlayer();
        String userAgent = Util.getUserAgent(getApplicationContext(), getApplicationContext().getString(R.string.app_name));
        MediaSource mediaSource = new ExtractorMediaSource
                .Factory(new DefaultDataSourceFactory(getApplicationContext(), userAgent))
                .setExtractorsFactory(new DefaultExtractorsFactory())
                .createMediaSource(Uri.parse(url));
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
    }


    private void stopStreaming() {
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
        }
    }


    private Notification prepareNotification(boolean isPaused) {
        // handle build version above android oreo
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
                mNotificationManager.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null) {
            CharSequence name = "Kiss Fm";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(FOREGROUND_CHANNEL_ID, name, importance);
            channel.enableVibration(false);
            mNotificationManager.createNotificationChannel(channel);
        }
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(PLAY);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // make a stop intent
        Intent stopIntent = new Intent(this, MusicService.class);
        stopIntent.setAction(STOP);
        PendingIntent pendingStopIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // make a refresh intent
        Intent refreshIntent = new Intent(this, MusicService.class);
        refreshIntent.setAction(REFRESH);
        PendingIntent pendingRefreshIntent = PendingIntent.getService(this, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // make a pause intent
        Intent pauseIntent = new Intent(this, MusicService.class);
        pauseIntent.setAction(PAUSE);
        PendingIntent pendingPauseIntent = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_service_layout);

        remoteViews.setOnClickPendingIntent(R.id.btn_stop, pendingStopIntent);

        remoteViews.setOnClickPendingIntent(R.id.btn_refresh, pendingRefreshIntent);

        remoteViews.setOnClickPendingIntent(R.id.btn_pause, pendingPauseIntent);
        if (isPaused) {
            remoteViews.setImageViewResource(R.id.btn_pause, R.drawable.music_play);
            remoteViews.setImageViewResource(R.id.btn_stop, R.drawable.music_off);
            remoteViews.setImageViewResource(R.id.btn_refresh, R.drawable.music_refresh);
        } else {
            remoteViews.setImageViewResource(R.id.btn_pause, R.drawable.music_pause);
            remoteViews.setImageViewResource(R.id.btn_stop, R.drawable.music_off);
            remoteViews.setImageViewResource(R.id.btn_refresh, R.drawable.music_refresh);
        }

        // notification builder
        NotificationCompat.Builder notificationBuilder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder = new NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID);
        } else {
            notificationBuilder = new NotificationCompat.Builder(this);
        }
        notificationBuilder
                .setContent(remoteViews)
                .setSmallIcon(R.drawable.ic_kiss_name)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOnlyAlertOnce(false)
                .setOngoing(true)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        }

        return notificationBuilder.build();
    }
}
