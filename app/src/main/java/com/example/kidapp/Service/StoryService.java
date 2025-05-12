package com.example.kidapp.Service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.VideoView;

import androidx.annotation.Nullable;

import com.example.kidapp.models.Story;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StoryService extends Service {
    private MediaPlayer mediaPlayer;
    public static final String ACTION_PLAY = "PLAY";
    public static final String ACTION_PAUSE = "PAUSE";
    public static final String ACTION_STOP = "STOP";
    public static final String ACTION_NEXT = "NEXT";
    public static final String ACTION_PREVIOUS = "PREVIOUS";
    private static final String TAG = "StoryService";

    private String currentVideoUrl;
    private List<Story> playlist;
    private int currentPosition;
    private boolean isShuffle = false;
    private Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Thêm phần này để hỗ trợ kết nối với VideoView
    private SurfaceHolder videoSurface;

    public interface StoryUpdateListener {
        void onStoryUpdate();
        void onProgressUpdate(int currentPosition, int duration);
    }

    private StoryUpdateListener updateListener;

    public void setUpdateListener(StoryUpdateListener listener) {
        this.updateListener = listener;
    }

    // Gọi listener khi có thay đổi
    private void notifyStoryUpdate() {
        if (updateListener != null) {
            updateListener.onStoryUpdate();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new StoryBinder();
    }

    public class StoryBinder extends Binder {
        public StoryService getService() {
            return StoryService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }

        // Lưu playlist và position nếu được truyền vào
        ArrayList<Story> receivedPlaylist = intent.getParcelableArrayListExtra("playlist");
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
        sendBroadcast(new Intent("STORY_UPDATE"));

        String action = intent.getAction();
        if (action != null) {
            Log.d(TAG, "Received action: " + action);
            switch (action) {
                case ACTION_PLAY:
                    String videoUrl = intent.getStringExtra("videoUrl");
                    if (videoUrl != null) {
                        playVideo(videoUrl);
                    } else {
                        playCurrentStory();
                    }
                    break;
                case ACTION_PAUSE:
                    pauseVideo();
                    break;
                case ACTION_STOP:
                    stopVideo();
                    break;
                case ACTION_NEXT:
                    playNext();
                    break;
                case ACTION_PREVIOUS:
                    playPrevious();
                    break;
            }
        }

        return START_STICKY; // Service tự động khởi động lại nếu bị hủy
    }

    public void playVideo(String videoUrl) {
        try {
            // Check if URL is valid
            if (videoUrl == null || videoUrl.isEmpty()) {
                return;
            }

            // Check if we're already playing this URL (nếu đã pause thì tiếp tục phát từ vị trí đã dừng)
            if (mediaPlayer != null && currentVideoUrl != null && currentVideoUrl.equals(videoUrl)) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    sendPlayStatusBroadcast(true);
                    startUpdatingProgress();
                }
                return;
            }

            // Otherwise, create a new MediaPlayer or reset the existing one
            if (mediaPlayer != null) {
                mediaPlayer.reset();
            } else {
                mediaPlayer = new MediaPlayer();
            }

            // Kết nối với SurfaceHolder nếu có
            if (videoSurface != null) {
                mediaPlayer.setDisplay(videoSurface);
            }

            currentVideoUrl = videoUrl;

            // Set the data source to the video URL
            mediaPlayer.setDataSource(videoUrl);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                Log.d(TAG, "Media player prepared successfully");
                mp.start();
                sendPlayStatusBroadcast(true);
                startUpdatingProgress();
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
                // Send error broadcast so UI can display message
                sendErrorBroadcast("Không thể phát video. Vui lòng thử lại sau.");
                return false;
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d(TAG, "Video playback completed");
                sendPlayStatusBroadcast(false);
                // Tự động chuyển video tiếp theo khi phát xong
                playNext();
            });

        } catch (IOException e) {
            Log.e(TAG, "Error playing video: " + e.getMessage());
            e.printStackTrace();
            sendErrorBroadcast("Đã xảy ra lỗi khi phát video.");
        }
    }

    public void playCurrentStory() {
        if (playlist != null && !playlist.isEmpty() && currentPosition >= 0 && currentPosition < playlist.size()) {
            Story currentStory = playlist.get(currentPosition);
            String videoUrl = currentStory.getStoryVideoUrl();
            if (videoUrl != null && !videoUrl.isEmpty()) {
                playVideo(videoUrl);
                Story story = getCurrentStory();
                if (story != null) {
                    sendStoryChangedBroadcast(story);
                }
            } else {
                sendErrorBroadcast("Không tìm thấy video cho truyện này.");
            }
        }
    }

    public void pauseVideo() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            sendPlayStatusBroadcast(false);
            handler.removeCallbacks(updateProgressRunnable);
        }
    }

    public void resumeVideo() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            sendPlayStatusBroadcast(true);
            startUpdatingProgress();
        }
    }

    private void stopVideo() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            sendPlayStatusBroadcast(false);
            handler.removeCallbacks(updateProgressRunnable);
        }
    }

    public void playNext() {
        if (playlist == null || playlist.isEmpty()) {
            return;
        }

        if (isShuffle) {
            currentPosition = random.nextInt(playlist.size());
        } else {
            currentPosition = (currentPosition + 1) % playlist.size();
        }

        playCurrentStory();
    }

    public void playPrevious() {
        if (playlist == null || playlist.isEmpty()) {
            return;
        }

        if (isShuffle) {
            currentPosition = random.nextInt(playlist.size());
        } else {
            currentPosition = (currentPosition - 1 + playlist.size()) % playlist.size();
        }

        playCurrentStory();
    }

    public void toggleShuffle() {
        isShuffle = !isShuffle;
        sendShuffleStatusBroadcast(isShuffle);
    }

    private void sendStoryChangedBroadcast(Story story) {
        Intent intent = new Intent("STORY_CHANGED");
        intent.putExtra("storyTitle", story.getStoryTitle());
        intent.putExtra("storyId", story.getStoryId());
        sendBroadcast(intent);
    }

    private void sendPlayStatusBroadcast(boolean isPlaying) {
        Intent intent = new Intent("STORY_PLAY_STATUS");
        intent.putExtra("isPlaying", isPlaying);
        sendBroadcast(intent);
    }

    private void sendErrorBroadcast(String errorMessage) {
        Intent intent = new Intent("STORY_ERROR");
        intent.putExtra("errorMessage", errorMessage);
        sendBroadcast(intent);
    }

    private void sendShuffleStatusBroadcast(boolean isShuffle) {
        Intent intent = new Intent("STORY_SHUFFLE_STATUS");
        intent.putExtra("isShuffle", isShuffle);
        sendBroadcast(intent);
    }

    public List<Story> getPlaylist() {
        return playlist;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setPlaylist(List<Story> playlist, int position) {
        this.playlist = playlist;
        if (position >= 0 && position < playlist.size()) {
            this.currentPosition = position;
        } else {
            this.currentPosition = 0;
        }
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public int getCurrentVideoPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public Story getCurrentStory() {
        if (playlist != null && !playlist.isEmpty() && currentPosition >= 0 && currentPosition < playlist.size()) {
            return playlist.get(currentPosition);
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            handler.removeCallbacks(updateProgressRunnable);
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private final Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();
                
                if (updateListener != null) {
                    updateListener.onProgressUpdate(currentPosition, duration);
                }
            }
            handler.postDelayed(this, 1000);
        }
    };

    public void startUpdatingProgress() {
        handler.removeCallbacks(updateProgressRunnable);
        handler.post(updateProgressRunnable);
    }

    public void setDisplay(SurfaceHolder holder) {
        this.videoSurface = holder;
        if (mediaPlayer != null && videoSurface != null) {
            mediaPlayer.setDisplay(videoSurface);
        }
    }

    public void attachToVideoView(VideoView videoView) {
        if (videoView != null) {
            videoView.setVisibility(android.view.View.VISIBLE);
            videoView.setOnPreparedListener(null);
            videoView.setOnCompletionListener(null);
            videoView.setOnErrorListener(null);
            
            if (mediaPlayer != null && currentVideoUrl != null) {
                try {
                    // Không thể dùng setMediaPlayer vì VideoView không có phương thức này
                    // Thay vào đó, ta sẽ cấu hình VideoView để chơi cùng URL
                    videoView.setVideoURI(Uri.parse(currentVideoUrl));
                    
                    // Đồng bộ trạng thái với mediaPlayer 
                    if (mediaPlayer.isPlaying()) {
                        videoView.seekTo(mediaPlayer.getCurrentPosition());
                        videoView.start();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error attaching to VideoView: " + e.getMessage());
                }
            }
        }
    }
} 