package com.example.kidapp.DB;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.kidapp.models.PvpRoom;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PvpRoomHelper {
    private static final String TAG = "PvpRoomHelper";
    private static final DatabaseReference roomsRef = FirebaseInitializer.getPvpRoomsRef();
    private static final Map<String, ValueEventListener> roomListeners = new java.util.concurrent.ConcurrentHashMap<>();

    // Tạo phòng mới
    public static void createRoom(PvpRoom room, OnRoomCreatedListener listener) {
        // Đảm bảo Firebase đã được khởi tạo
        FirebaseInitializer.initializeFirebase();
        
        // Kiểm tra kết nối trước
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);
                if (connected != null && connected) {
                    Log.d(TAG, "Firebase is connected, proceeding with room creation");
                    proceedWithRoomCreation(room, listener);
                } else {
                    Log.e(TAG, "Firebase is not connected");

                    Log.d(TAG, "Attempting offline room creation");
                    tryOfflineRoomCreation(room, listener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase connection check failed", error.toException());
                if (listener != null) {
                    listener.onError("Lỗi kiểm tra kết nối: " + error.getMessage());
                }
            }
        });
    }
    
    private static void proceedWithRoomCreation(PvpRoom room, OnRoomCreatedListener listener) {
        // Tạo key mới từ Firebase
        DatabaseReference newRoomRef = roomsRef.push();
        String roomId = newRoomRef.getKey();
        
        if (roomId == null) {
            Log.e(TAG, "Failed to generate roomId");
            if (listener != null) {
                listener.onError("Không thể tạo ID phòng");
            }
            return;
        }
        
        room.setRoomId(roomId);
        
        // Thử với dữ liệu đơn giản trước
        newRoomRef.child("test").setValue("test")
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Test write successful, proceeding with full room data");
                
                // Đặt thời gian timeout cho việc ghi
                final boolean[] success = {false};
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (!success[0]) {
                        Log.e(TAG, "Room creation timed out");
                        if (listener != null) {
                            listener.onError("Tạo phòng bị timeout. Vui lòng thử lại.");
                        }
                    }
                }, 10000); // 10 giây timeout
                
                // Nếu thành công, tiếp tục lưu dữ liệu đầy đủ
                newRoomRef.setValue(room)
                    .addOnSuccessListener(aVoid2 -> {
                        success[0] = true;
                        Log.d(TAG, "Room created successfully with ID: " + roomId);
                        if (listener != null) {
                            listener.onRoomCreated(room);
                        }
                    })
                    .addOnFailureListener(e -> {
                        success[0] = true;
                        Log.e(TAG, "Error creating full room data", e);
                        if (listener != null) {
                            listener.onError("Lỗi lưu dữ liệu phòng: " + e.getMessage());
                        }
                    })
                    .addOnCanceledListener(() -> {
                        success[0] = true;
                        Log.e(TAG, "Room creation canceled");
                        if (listener != null) {
                            listener.onError("Tạo phòng bị hủy");
                        }
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Cannot even create test data", e);
                if (listener != null) {
                    listener.onError("Không thể kết nối đến server: " + e.getMessage());
                }
            })
            .addOnCanceledListener(() -> {
                Log.e(TAG, "Test write canceled");
                if (listener != null) {
                    listener.onError("Kiểm tra kết nối bị hủy");
                }
            });
    }

    private static void tryOfflineRoomCreation(PvpRoom room, OnRoomCreatedListener listener) {
        try {
            // Tạo key mới từ một UUID ngẫu nhiên
            String roomId = "offline_" + java.util.UUID.randomUUID().toString().substring(0, 8);
            room.setRoomId(roomId);
            
            // Lưu dữ liệu vào một tham chiếu
            DatabaseReference roomRef = roomsRef.child(roomId);
            roomRef.setValue(room)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Room created successfully in offline mode: " + roomId);
                    if (listener != null) {
                        listener.onRoomCreated(room);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating room in offline mode", e);
                    if (listener != null) {
                        listener.onError("Lỗi khi tạo phòng offline: " + e.getMessage());
                    }
                });
            
        } catch (Exception e) {
            Log.e(TAG, "Error in offline room creation", e);
            if (listener != null) {
                listener.onError("Không thể tạo phòng offline: " + e.getMessage());
            }
        }
    }

    // Cập nhật thông tin phòng
    public static void updateRoom(PvpRoom room) {
        if (room.getRoomId() == null) {
            Log.e(TAG, "Cannot update room: Room ID is null");
            return;
        }

        roomsRef.child(room.getRoomId()).setValue(room)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Room updated successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating room", e));
    }

    // Lắng nghe khi có sự thay đổi trong phòng
    public static void listenForRoomChanges(String roomId, OnRoomChangedListener listener) {
        Log.d(TAG, "Starting to listen for room changes: " + roomId);
        
        // Đảm bảo xóa bất kỳ listener cũ nào trước khi thêm mới
        ValueEventListener existingListener = roomListeners.get(roomId);
        if (existingListener != null) {
            roomsRef.child(roomId).removeEventListener(existingListener);
        }
        
        ValueEventListener roomListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.d(TAG, "Room " + roomId + " no longer exists");
                    if (listener != null) {
                        listener.onRoomDeleted();
                        return;
                    }
                    return;
                }
                
                try {
                    // Log toàn bộ dữ liệu nhận được
                    Log.d(TAG, "Raw room data: " + dataSnapshot.toString());
                    
                    // Tạo phòng mới từ dataSnapshot
                    PvpRoom room = new PvpRoom();
                    room.setRoomId(roomId);
                    
                    // Lấy các trường dữ liệu trực tiếp
                    if (dataSnapshot.child("roomName").exists()) {
                        room.setRoomName(dataSnapshot.child("roomName").getValue(String.class));
                    }
                    
                    if (dataSnapshot.child("hostId").exists()) {
                        room.setHostId(dataSnapshot.child("hostId").getValue(String.class));
                    }
                    
                    if (dataSnapshot.child("hostName").exists()) {
                        room.setHostName(dataSnapshot.child("hostName").getValue(String.class));
                    }
                    
                    if (dataSnapshot.child("guestId").exists()) {
                        room.setGuestId(dataSnapshot.child("guestId").getValue(String.class));
                    }
                    
                    if (dataSnapshot.child("guestName").exists()) {
                        room.setGuestName(dataSnapshot.child("guestName").getValue(String.class));
                    }
                    
                    if (dataSnapshot.child("gameType").exists()) {
                        room.setGameType(dataSnapshot.child("gameType").getValue(String.class));
                    }
                    
                    if (dataSnapshot.child("gameId").exists()) {
                        room.setGameId(dataSnapshot.child("gameId").getValue(String.class));
                    }
                    
                    if (dataSnapshot.child("status").exists()) {
                        room.setStatus(dataSnapshot.child("status").getValue(String.class));
                    }
                    
                    if (dataSnapshot.child("createdAt").exists()) {
                        room.setCreatedAt(dataSnapshot.child("createdAt").getValue(Long.class));
                    }
                    
                    // Lấy scores nếu có
                    if (dataSnapshot.child("scores").exists()) {
                        Map<String, Integer> scores = new HashMap<>();
                        for (DataSnapshot scoreSnapshot : dataSnapshot.child("scores").getChildren()) {
                            String userId = scoreSnapshot.getKey();
                            Integer score = scoreSnapshot.getValue(Integer.class);
                            if (userId != null && score != null) {
                                scores.put(userId, score);
                            }
                        }
                        room.setScores(scores);
                    }
                    
                    // Log thông tin phòng
                    Log.d(TAG, "Parsed room info from Firebase:");
                    Log.d(TAG, "Room ID: " + room.getRoomId() + ", Name: " + room.getRoomName());
                    Log.d(TAG, "Host: " + room.getHostName() + " (ID: " + room.getHostId() + ")");
                    Log.d(TAG, "Guest: " + room.getGuestName() + " (ID: " + room.getGuestId() + ")");
                    Log.d(TAG, "Status: " + room.getStatus() + ", Game: " + room.getGameType());
                    
                    if (listener != null) {
                        listener.onRoomChanged(room);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing room data", e);
                    if (listener != null) {
                        listener.onError("Lỗi xử lý dữ liệu phòng: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error listening for room changes", databaseError.toException());
                if (listener != null) {
                    listener.onError(databaseError.getMessage());
                }
            }
        };
        
        // Lưu trữ listener để có thể gỡ bỏ sau này
        roomListeners.put(roomId, roomListener);
        
        // Đăng ký listener
        roomsRef.child(roomId).addValueEventListener(roomListener);
    }
    
    // Loại bỏ listener khi không cần thiết nữa
    public static void removeRoomListener(String roomId) {
        if (roomId != null && roomListeners.containsKey(roomId)) {
            ValueEventListener listener = roomListeners.get(roomId);
            if (listener != null) {
                Log.d(TAG, "Removing room listener for: " + roomId);
                roomsRef.child(roomId).removeEventListener(listener);
                roomListeners.remove(roomId);
            }
        }
    }

    // Xóa phòng
    public static void deleteRoom(String roomId) {
        deleteRoom(roomId, null);
    }

    // Xóa phòng với callback
    public static void deleteRoom(String roomId, OnRoomDeletedListener listener) {
        if (roomId == null || roomId.isEmpty()) {
            Log.e(TAG, "Cannot delete room: Room ID is null or empty");
            if (listener != null) {
                listener.onError("Room ID is null or empty");
            }
            return;
        }
        
        Log.d(TAG, "Deleting room: " + roomId);
        
        // Đảm bảo gỡ bỏ listener trước khi xóa phòng
        removeRoomListener(roomId);
        
        roomsRef.child(roomId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Room deleted successfully: " + roomId);
                    if (listener != null) {
                        listener.onRoomDeleted();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting room: " + roomId, e);
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }

    // Cập nhật thông tin phòng với callback
    public static void updateRoomWithCallback(PvpRoom room, OnRoomUpdatedListener listener) {
        if (room == null || room.getRoomId() == null) {
            Log.e(TAG, "Cannot update room: Room or Room ID is null");
            if (listener != null) {
                listener.onError("Room or Room ID is null");
            }
            return;
        }

        Log.d(TAG, "Updating room with callback: " + room.getRoomId());
        roomsRef.child(room.getRoomId()).setValue(room)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Room updated successfully with callback: " + room.getRoomId());
                    if (listener != null) {
                        listener.onRoomUpdated();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating room with callback", e);
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }

    // Lấy danh sách tất cả các phòng (đang chờ)
    public static LiveData<List<PvpRoom>> getAvailableRooms() {
        MutableLiveData<List<PvpRoom>> roomsLiveData = new MutableLiveData<>();
        
        // Danh sách phòng
        List<PvpRoom> roomsList = new ArrayList<>();
        
        // Đăng ký lắng nghe thay đổi trạng thái phòng
        ChildEventListener roomsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                PvpRoom room = snapshot.getValue(PvpRoom.class);
                if (room != null && "WAITING".equals(room.getStatus())) {
                    Log.d(TAG, "Room added: " + room.getRoomName() + " (" + room.getRoomId() + ")");
                    roomsList.add(room);
                    roomsLiveData.setValue(new ArrayList<>(roomsList));
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                PvpRoom updatedRoom = snapshot.getValue(PvpRoom.class);
                if (updatedRoom != null) {
                    Log.d(TAG, "Room changed: " + updatedRoom.getRoomName() + ", Status: " + updatedRoom.getStatus());
                    
                    // Tìm phòng trong danh sách
                    int index = -1;
                    for (int i = 0; i < roomsList.size(); i++) {
                        if (roomsList.get(i).getRoomId().equals(updatedRoom.getRoomId())) {
                            index = i;
                            break;
                        }
                    }
                    
                    // Cập nhật hoặc xóa phòng tùy thuộc vào trạng thái
                    if ("WAITING".equals(updatedRoom.getStatus())) {
                        if (index >= 0) {
                            roomsList.set(index, updatedRoom);
                        } else {
                            roomsList.add(updatedRoom);
                        }
                    } else {
                        if (index >= 0) {
                            roomsList.remove(index);
                        }
                    }
                    
                    roomsLiveData.setValue(new ArrayList<>(roomsList));
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                PvpRoom removedRoom = snapshot.getValue(PvpRoom.class);
                if (removedRoom != null) {
                    Log.d(TAG, "Room removed: " + removedRoom.getRoomName());
                    
                    // Tìm và xóa phòng khỏi danh sách
                    for (int i = 0; i < roomsList.size(); i++) {
                        if (roomsList.get(i).getRoomId().equals(removedRoom.getRoomId())) {
                            roomsList.remove(i);
                            break;
                        }
                    }
                    
                    roomsLiveData.setValue(new ArrayList<>(roomsList));
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Không cần xử lý
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading rooms", error.toException());
            }
        };
        
        // Đăng ký listener
        roomsRef.orderByChild("status").equalTo("WAITING").addChildEventListener(roomsListener);

        return roomsLiveData;
    }

    // Tham gia vào phòng
    public static void joinRoom(String roomId, String guestId, String guestName, OnRoomJoinedListener listener) {
        roomsRef.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                PvpRoom room = dataSnapshot.getValue(PvpRoom.class);
                if (room != null) {
                    if ("WAITING".equals(room.getStatus())) {
                        // Thêm khách vào phòng nhưng giữ nguyên trạng thái WAITING
                        room.setGuestId(guestId);
                        room.setGuestName(guestName);
                        room.getScores().put(guestId, 0);
                        
                        roomsRef.child(roomId).setValue(room)
                                .addOnSuccessListener(aVoid -> {
                                    if (listener != null) {
                                        listener.onRoomJoined(room);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (listener != null) {
                                        listener.onError("Không thể tham gia phòng: " + e.getMessage());
                                    }
                                });
                    } else {
                        if (listener != null) {
                            listener.onError("Phòng đã đầy hoặc đã bắt đầu chơi");
                        }
                    }
                } else {
                    if (listener != null) {
                        listener.onError("Không tìm thấy phòng");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (listener != null) {
                    listener.onError("Lỗi: " + databaseError.getMessage());
                }
            }
        });
    }

    // Cập nhật điểm số
    public static void updateScore(String roomId, String playerId, int newScore) {
        roomsRef.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                PvpRoom room = dataSnapshot.getValue(PvpRoom.class);
                if (room != null) {
                    room.updateScore(playerId, newScore);
                    roomsRef.child(roomId).setValue(room);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error updating score", databaseError.toException());
            }
        });
    }

    // Interface cho các callback
    public interface OnRoomCreatedListener {
        void onRoomCreated(PvpRoom room);
        void onError(String errorMessage);
    }

    public interface OnRoomChangedListener {
        void onRoomChanged(PvpRoom room);
        void onRoomDeleted();
        void onError(String errorMessage);
    }

    public interface OnRoomJoinedListener {
        void onRoomJoined(PvpRoom room);
        void onError(String errorMessage);
    }

    // Interface cho callback khi xóa phòng
    public interface OnRoomDeletedListener {
        void onRoomDeleted();
        void onError(String errorMessage);
    }

    // Interface cho callback khi cập nhật phòng
    public interface OnRoomUpdatedListener {
        void onRoomUpdated();
        void onError(String errorMessage);
    }
} 