package com.example.kidapp.DB;

import android.util.Log;

/**
 * Lớp singleton để quản lý trạng thái phòng PVP toàn cục,
 * tránh việc xóa phòng khi chuyển activity
 */
public class PvpRoomManager {
    private static final String TAG = "PvpRoomManager";
    private static PvpRoomManager instance;
    
    private boolean preventRoomDeletion = false;
    private String currentRoomId = null;
    private int roomInstanceCount = 0;
    
    private PvpRoomManager() {
        // Private constructor
    }
    
    public static synchronized PvpRoomManager getInstance() {
        if (instance == null) {
            instance = new PvpRoomManager();
        }
        return instance;
    }
    
    /**
     * Đánh dấu phòng không bị xóa khi chuyển activity
     */
    public void setPreventRoomDeletion(String roomId, boolean prevent) {
        Log.d(TAG, "Setting prevent room deletion for " + roomId + " to " + prevent);
        preventRoomDeletion = prevent;
        currentRoomId = roomId;
    }
    
    /**
     * Kiểm tra xem phòng có bị ngăn xóa hay không
     */
    public boolean shouldPreventDeletion(String roomId) {
        boolean shouldPrevent = preventRoomDeletion && (roomId != null && roomId.equals(currentRoomId));
        Log.d(TAG, "Checking if should prevent deletion for " + roomId + ": " + shouldPrevent);
        return shouldPrevent;
    }
    
    /**
     * Đăng ký sử dụng phòng
     */
    public void registerRoomUsage(String roomId) {
        if (roomId != null && roomId.equals(currentRoomId)) {
            roomInstanceCount++;
            Log.d(TAG, "Registered room usage for " + roomId + ", count: " + roomInstanceCount);
        }
    }
    
    /**
     * Hủy đăng ký sử dụng phòng
     */
    public void unregisterRoomUsage(String roomId) {
        if (roomId != null && roomId.equals(currentRoomId)) {
            roomInstanceCount--;
            Log.d(TAG, "Unregistered room usage for " + roomId + ", count: " + roomInstanceCount);
            
            // Nếu không còn ai sử dụng phòng, reset trạng thái
            if (roomInstanceCount <= 0) {
                roomInstanceCount = 0;
                preventRoomDeletion = false;
                currentRoomId = null;
                Log.d(TAG, "No more usage for room, resetting state");
            }
        }
    }
    
    /**
     * Reset trạng thái toàn cục
     */
    public void reset() {
        preventRoomDeletion = false;
        currentRoomId = null;
        roomInstanceCount = 0;
        Log.d(TAG, "Reset room manager state");
    }
} 