package com.example.kidapp;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.security.Provider;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    public static final String ACTION_PLAY = "PLAY";
    public static final String ACTION_PAUSE = "PAUSE";
    public static final String ACTION_STOP = "STOP";



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case ACTION_PLAY:
                    playMusic();
                    break;
                case ACTION_PAUSE:
                    pauseMusic();
                    break;
                case ACTION_STOP:
                    stopMusic();
                    break;
            }
        }
        return START_STICKY; // Service tự động khởi động lại nếu bị hủy
    }

    private void playMusic() {
        if (mediaPlayer == null) { // Kiểm tra và khởi tạo MediaPlayer nếu chưa có
            mediaPlayer = new MediaPlayer();
            try {
                AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.music_sound);
                if (afd != null) {
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    afd.close();
                    mediaPlayer.prepare();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }


    private void pauseMusic() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    private void stopMusic() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
        stopSelf(); // Dừng Service khi hoàn thành
    }

    private void sendPlayStatusBroadcast(boolean isPlaying) {
        Intent intent = new Intent("MUSIC_PLAY_STATUS");
        intent.putExtra("isPlaying", isPlaying);
        sendBroadcast(intent);
    }
    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;

    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    // MusicService.java
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


}