package com.example.kidapp.Service;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.kidapp.R;
import com.example.kidapp.models.Music;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    public static final String ACTION_PLAY = "PLAY";
    public static final String ACTION_PAUSE = "PAUSE";
    public static final String ACTION_STOP = "STOP";
    private static final String TAG = "MusicService";
    public static final String ACTION_NEXT = "NEXT";
    public static final String ACTION_PREVIOUS = "PREVIOUS";
    public static final String ACTION_SHUFFLE = "SHUFFLE";
    private final Handler handler = new Handler();

    private String currentMusicUrl;
    private List<Music> playlist;
    private int currentPosition;
    private boolean isShuffle = false;
    private Random random = new Random();


    public interface MusicUpdateListener {
        void onMusicUpdate();
    }

    private MusicUpdateListener updateListener;

    public void setUpdateListener(MusicUpdateListener listener) {
        this.updateListener = listener;
    }

    // Gọi listener khi có thay đổi
    private void notifyMusicUpdate() {
        if (updateListener != null) {
            updateListener.onMusicUpdate();
        }
    }

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
        if (intent == null) {
            return START_STICKY;
        }

        // Lưu playlist và position nếu được truyền vào
        List<Music> receivedPlaylist = intent.getParcelableArrayListExtra("playlist");
        if (receivedPlaylist != null && !receivedPlaylist.isEmpty()) {
            Log.d(TAG, "Received playlist with " + receivedPlaylist.size() + " items");
            playlist = receivedPlaylist;

            // Chỉ cập nhật currentPosition nếu playlist thay đổi
            int position = intent.getIntExtra("position", 0);
            if (position >= 0 && position < playlist.size()) {
                currentPosition = position;
                Log.d(TAG, "Current position set to: " + currentPosition);
            } else {
                Log.w(TAG, "Invalid position: " + position + ", using default: 0");
                currentPosition = 0;
            }
        }
        sendBroadcast(new Intent("MUSIC_UPDATE"));


        String action = intent.getAction();
        if (action != null) {
            Log.d(TAG, "Received action: " + action);
            switch (action) {
                case ACTION_PLAY:
                    String musicUrl = intent.getStringExtra("musicUrl");
                    if (musicUrl != null) {
                        playMusic(musicUrl);
                    } else {
                        playMusic();
                    }
                    break;
                case ACTION_PAUSE:
                    pauseMusic();
                    break;
                case ACTION_STOP:
                    stopMusic();
                    break;
                case ACTION_NEXT:
                    playNext();
                    break;
                case ACTION_PREVIOUS:
                    playPrevious();
                    break;
                case ACTION_SHUFFLE:
                    toggleShuffle();
                    break;
            }
        }

        return START_STICKY; // Service tự động khởi động lại nếu bị hủy
    }

    public void playMusic(String musicUrl) {
        try {
            // Check if URL is valid
            if (musicUrl == null || musicUrl.isEmpty()) {
                return;
            }

            // Check if we're already playing this URL (nếu đã pause thì tiếp tục phát từ vị trí đã dừng)
            if (mediaPlayer != null && currentMusicUrl != null && currentMusicUrl.equals(musicUrl)) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    sendPlayStatusBroadcast(true);
                }
                return;
            }

            // Otherwise, create a new MediaPlayer or reset the existing one
            if (mediaPlayer != null) {
                mediaPlayer.reset();
            } else {
                mediaPlayer = new MediaPlayer();
            }

            currentMusicUrl = musicUrl;

            // Set the data source to the music URL
            mediaPlayer.setDataSource(musicUrl);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();

                // Gửi broadcast khi đã chuẩn bị xong
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Music currentMusic = getCurrentMusic();
                    if (currentMusic != null) {
                        sendTrackChangedBroadcast(currentMusic);
                    }
                }, 100); // Thêm delay nhỏ
            });
            mediaPlayer.setOnPreparedListener(mp -> {
                Log.d(TAG, "Media player prepared successfully");
                mp.start();
                sendPlayStatusBroadcast(true);
                startUpdatingSeekBar(); // Thêm dòng này để cập nhật seekbar
            });

//            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
//                Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
//                // Send error broadcast so UI can display message
//                sendErrorBroadcast("Không thể phát nhạc. Vui lòng thử lại sau.");
//                return false;
//            });
//
//            mediaPlayer.setOnCompletionListener(mp -> {
//                Log.d(TAG, "Music playback completed");
//                sendPlayStatusBroadcast(false);
//                // Tự động chuyển bài hát tiếp theo khi phát xong
//                playNext();
//            });

        } catch (Exception e) {
            Log.e(TAG, "General error in playMusic: " + e.getMessage());
            e.printStackTrace();
            sendErrorBroadcast("Đã xảy ra lỗi khi phát nhạc.");
        }
    }

    public void playMusic() {
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
            sendPlayStatusBroadcast(true);
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            sendBroadcast(new Intent("MUSIC_UPDATE")); // Thêm dòng này

            sendPlayStatusBroadcast(false);
        }
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            sendPlayStatusBroadcast(false);
        }
        stopSelf(); // Dừng Service khi hoàn thành
    }


    public void setPlaylist(List<Music> playlist, int position) {
        this.playlist = playlist;
        this.currentPosition = position;
    }

    public void playNext() {
        if (playlist == null || playlist.isEmpty()) {
            return;
        }

        if (mediaPlayer == null) {
            return;
        }
        // Nếu chỉ có 1 bài hát trong playlist, phát lại bài hát đó
        if (playlist.size() == 1) {
            Music music = playlist.get(0);
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(0); // Quay về đầu bài hát
            } else {
                playMusic(music.getMusicUrl());
            }
            return;
        }

        if (isShuffle) {
            // Đảm bảo bài hát random khác bài hát hiện tại
            int oldPosition = currentPosition;
            do {
                currentPosition = random.nextInt(playlist.size());
            } while (currentPosition == oldPosition && playlist.size() > 1);
        } else {
            currentPosition = (currentPosition + 1) % playlist.size();
        }

        Log.d(TAG, "playNext: Moving to position " + currentPosition + " in playlist of size " + playlist.size());
        Music nextMusic = playlist.get(currentPosition);

        playMusic(nextMusic.getMusicUrl());
        sendTrackChangedBroadcast(nextMusic);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            sendTrackChangedBroadcast(nextMusic);
        }, 300); // Delay 300ms để xử lý prepareAsync
        mediaPlayer.setOnPreparedListener(mp -> {
            mp.start();
            sendTrackChangedBroadcast(nextMusic); // Gửi broadcast sau khi prepare xong
        });
        sendTrackChangedBroadcast(nextMusic);
        sendBroadcast(new Intent("MUSIC_UPDATE")); // Thêm dòng này
    }

    public void playPrevious() {
        if (playlist == null || playlist.isEmpty()) {
            Log.e(TAG, "playPrevious: playlist is null or empty");
            return;
        }

        if (mediaPlayer == null) {
            Log.e(TAG, "playPrevious: mediaPlayer is null");
            return;
        }

        // Nếu chỉ có 1 bài hát trong playlist, phát lại bài hát đó
        if (playlist.size() == 1) {
            Music music = playlist.get(0);
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(0); // Quay về đầu bài hát
            } else {
                playMusic(music.getMusicUrl());
            }
            return;
        }

        if (isShuffle) {
            // Đảm bảo bài hát random khác bài hát hiện tại
            int oldPosition = currentPosition;
            do {
                currentPosition = random.nextInt(playlist.size());
            } while (currentPosition == oldPosition && playlist.size() > 1);
        } else {
            currentPosition = (currentPosition - 1 + playlist.size()) % playlist.size();
        }

        Log.d(TAG, "playPrevious: Moving to position " + currentPosition + " in playlist of size " + playlist.size());
        Music prevMusic = playlist.get(currentPosition);
        playMusic(prevMusic.getMusicUrl());
        sendTrackChangedBroadcast(prevMusic);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            sendTrackChangedBroadcast(prevMusic);
        }, 300); // Delay 300ms để xử lý prepareAsync
        mediaPlayer.setOnPreparedListener(mp -> {
            mp.start();
            sendTrackChangedBroadcast(prevMusic); // Gửi broadcast sau khi prepare xong
        });
        sendTrackChangedBroadcast(prevMusic);
        sendBroadcast(new Intent("MUSIC_UPDATE")); // Thêm dòng này
    }

    public void toggleShuffle() {
        isShuffle = !isShuffle;
        sendShuffleStatusBroadcast(isShuffle);
    }

    private void sendTrackChangedBroadcast(Music music) {
        if (music == null) {
            Log.e(TAG, "sendTrackChangedBroadcast: music is null");
            return;
        }
        
        Log.d(TAG, "Broadcast đã được gửi đi với music: " + music.getMusicName());
        
        // Đảm bảo gửi thông tin đầy đủ
        Intent intent = new Intent("TRACK_CHANGED");
        intent.putExtra("currentMusic", music);
        
        // Thêm flag để đảm bảo broadcast được nhận
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES | Intent.FLAG_RECEIVER_FOREGROUND);
        sendBroadcast(intent);
        
        // Gửi thêm broadcast cập nhật tổng thể
        sendBroadcast(new Intent("MUSIC_UPDATE"));
        
        // Thông báo cho listener
        notifyMusicUpdate();
    }

    private void sendShuffleStatusBroadcast(boolean isShuffle) {
        Intent intent = new Intent("SHUFFLE_STATUS");
        intent.putExtra("isShuffle", isShuffle);
        sendBroadcast(intent);
    }
    public void resumeMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            sendBroadcast(new Intent("MUSIC_UPDATE")); // Thêm dòng này
            sendPlayStatusBroadcast(true);
        }
    }
    private void sendPlayStatusBroadcast(boolean isPlaying) {
        Intent intent = new Intent("MUSIC_PLAY_STATUS");
        intent.putExtra("isPlaying", isPlaying);
        sendBroadcast(intent);
    }

    private void sendErrorBroadcast(String errorMessage) {
        Intent intent = new Intent("MUSIC_ERROR");
        intent.putExtra("errorMessage", errorMessage);
        sendBroadcast(intent);
    }

    public List<Music> getPlaylist() {
        return playlist;
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }
    public Music getCurrentMusic() {
        if(playlist != null && currentPosition >= 0 && currentPosition < playlist.size()){
            return playlist.get(currentPosition);
        }
        return null;
    }
    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public String getCurrentMusicUrl() {
        return currentMusicUrl;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    private final Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                handler.postDelayed(this, 500);
            }
        }
    };

    public void startUpdatingSeekBar() {
        handler.post(updateSeekBar);
    }
}