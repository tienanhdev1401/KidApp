package com.example.kidapp.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PvpRoom implements Serializable {
    private String roomId;
    private String roomName;
    private String hostId;
    private String hostName;
    private String guestId;
    private String guestName;
    private String gameType; // FLIP_CARD, PUZZLE, GUESS_WORD, MATH, etc.
    private String gameId; // ID của game cụ thể
    private String status; // WAITING, PLAYING, FINISHED
    private long createdAt;
    private Map<String, Integer> scores; // userId -> score

    // Constructor mặc định (cần thiết cho Firebase)
    public PvpRoom() {
        this.scores = new HashMap<>();
    }

    // Constructor với các tham số
    public PvpRoom(String roomName, String hostId, String hostName, String gameType, String gameId) {
        this.roomName = roomName;
        this.hostId = hostId;
        this.hostName = hostName;
        this.gameType = gameType;
        this.gameId = gameId;
        this.status = "WAITING";
        this.createdAt = System.currentTimeMillis();
        this.scores = new HashMap<>();
        scores.put(hostId, 0);
    }

    // Getters và setters
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, Integer> getScores() {
        return scores;
    }

    public void setScores(Map<String, Integer> scores) {
        this.scores = scores;
    }

    // Phương thức hỗ trợ
    public void addGuest(String guestId, String guestName) {
        this.guestId = guestId;
        this.guestName = guestName;
        this.scores.put(guestId, 0);
    }

    public void updateScore(String playerId, int newScore) {
        this.scores.put(playerId, newScore);
    }

    public int getScoreByPlayerId(String playerId) {
        return this.scores.getOrDefault(playerId, 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PvpRoom otherRoom = (PvpRoom) obj;
        
        // So sánh các trường cơ bản
        if (!roomId.equals(otherRoom.roomId)) return false;
        
        // So sánh trạng thái
        if (!status.equals(otherRoom.status)) return false;
        
        // So sánh thông tin người chơi
        boolean guestEqual = (guestId == null && otherRoom.guestId == null) || 
                            (guestId != null && guestId.equals(otherRoom.guestId));
                            
        boolean guestNameEqual = (guestName == null && otherRoom.guestName == null) || 
                                (guestName != null && guestName.equals(otherRoom.guestName));
        
        return guestEqual && guestNameEqual;
    }
    
    @Override
    public int hashCode() {
        int result = roomId.hashCode();
        result = 31 * result + (guestId != null ? guestId.hashCode() : 0);
        result = 31 * result + status.hashCode();
        return result;
    }
} 