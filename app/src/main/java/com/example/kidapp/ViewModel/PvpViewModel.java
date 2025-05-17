package com.example.kidapp.ViewModel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.kidapp.DB.FirebaseInitializer;
import com.example.kidapp.DB.PvpRoomHelper;
import com.example.kidapp.models.PvpRoom;
import com.example.kidapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class PvpViewModel extends ViewModel {
    private static final String TAG = "PvpViewModel";
    private final MutableLiveData<PvpRoom> currentRoom = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private String currentUserId;
    private String currentUserName;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private UserViewModel userViewModel;
    private Application application;

    public PvpViewModel() {
        // Đảm bảo Firebase đã được khởi tạo
        FirebaseInitializer.initializeFirebase();
        
        // Lấy thông tin người dùng hiện tại
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
            currentUserName = auth.getCurrentUser().getDisplayName();
            if (currentUserName == null || currentUserName.isEmpty()) {
                currentUserName = "Người chơi " + currentUserId.substring(0, 5);
            }
            Log.d(TAG, "Initialized with user: " + currentUserName + " (ID: " + currentUserId + ")");
        } else {
            Log.w(TAG, "No user is currently logged in");
        }
    }
    
    // Phương thức mới để thiết lập UserViewModel
    public void setUserViewModel(UserViewModel userViewModel) {
        this.userViewModel = userViewModel;
        updateUserNameFromViewModel();
    }
    
    // Phương thức mới để lấy tên người dùng từ UserViewModel
    private void updateUserNameFromViewModel() {
        if (userViewModel != null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if (email != null && !email.isEmpty()) {
                userViewModel.getUserByEmail(email).observeForever(user -> {
                    if (user != null) {
                        currentUserName = user.getUsername();
                        Log.d(TAG, "Updated username from UserViewModel: " + currentUserName);
                        
                        // Cập nhật lại phòng hiện tại nếu có
                        PvpRoom room = currentRoom.getValue();
                        if (room != null) {
                            if (currentUserId.equals(room.getHostId())) {
                                room.setHostName(currentUserName);
                            } else if (currentUserId.equals(room.getGuestId())) {
                                room.setGuestName(currentUserName);
                            }
                            PvpRoomHelper.updateRoom(room);
                        }
                    }
                });
            }
        }
    }

    // Tạo phòng mới
    public void createRoom(String roomName, String gameType, String gameId) {
        if (currentUserId == null) {
            errorMessage.setValue("Bạn cần đăng nhập để tạo phòng");
            Log.e(TAG, "Cannot create room: User not logged in");
            return;
        }

        // Kiểm tra và làm mới token
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.getIdToken(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Token refreshed successfully, proceeding with room creation");
                        proceedWithRoomCreation(roomName, gameType, gameId);
                    } else {
                        Log.e(TAG, "Failed to refresh token", task.getException());
                        errorMessage.setValue("Lỗi xác thực: " + (task.getException() != null ? task.getException().getMessage() : "không xác định"));
                    }
                });
        } else {
            // Fallback nếu không lấy được currentUser
            Log.w(TAG, "Current user is null, trying to create room anyway");
            proceedWithRoomCreation(roomName, gameType, gameId);
        }
    }
    
    private void proceedWithRoomCreation(String roomName, String gameType, String gameId) {
        Log.d(TAG, "Creating room: " + roomName + ", Game type: " + gameType);
        
        // Đặt timeout
        final boolean[] completed = {false};
        handler.postDelayed(() -> {
            if (!completed[0]) {
                Log.e(TAG, "Room creation timed out after 15 seconds");
                errorMessage.setValue("Tạo phòng không thành công (timeout). Vui lòng kiểm tra kết nối và thử lại.");
            }
        }, 15000); // 15 giây timeout
        
        PvpRoom room = new PvpRoom(roomName, currentUserId, currentUserName, gameType, gameId);
        PvpRoomHelper.createRoom(room, new PvpRoomHelper.OnRoomCreatedListener() {
            @Override
            public void onRoomCreated(PvpRoom room) {
                completed[0] = true;
                Log.d(TAG, "Room created successfully: " + room.getRoomId());
                currentRoom.setValue(room);
                // Bắt đầu lắng nghe thay đổi trong phòng
                listenForRoomChanges(room.getRoomId());
            }

            @Override
            public void onError(String message) {
                completed[0] = true;
                Log.e(TAG, "Error creating room: " + message);
                errorMessage.setValue("Lỗi khi tạo phòng: " + message);
            }
        });
    }

    // Tham gia vào phòng
    public void joinRoom(String roomId) {
        if (currentUserId == null) {
            errorMessage.setValue("Bạn cần đăng nhập để tham gia phòng");
            Log.e(TAG, "Cannot join room: User not logged in");
            return;
        }

        Log.d(TAG, "Joining room: " + roomId + " as " + currentUserName + " (ID: " + currentUserId + ")");
        
        // Đặt timeout cho việc tham gia phòng
        final boolean[] completed = {false};
        handler.postDelayed(() -> {
            if (!completed[0]) {
                Log.e(TAG, "Room joining timed out");
                errorMessage.postValue("Tham gia phòng không thành công (timeout). Vui lòng thử lại.");
            }
        }, 15000); // 15 giây timeout
        
        PvpRoomHelper.joinRoom(roomId, currentUserId, currentUserName, new PvpRoomHelper.OnRoomJoinedListener() {
            @Override
            public void onRoomJoined(PvpRoom room) {
                completed[0] = true;
                Log.d(TAG, "Successfully joined room: " + room.getRoomId());
                Log.d(TAG, "Room details - Name: " + room.getRoomName() + 
                      ", Host: " + room.getHostName() + ", Guest: " + room.getGuestName());
                
                // Kiểm tra xem phòng có đầy đủ thông tin không
                if (room.getRoomName() == null || room.getHostName() == null) {
                    Log.e(TAG, "Room data incomplete, fetching full data");
                    // Tải lại thông tin phòng đầy đủ
                    getFullRoomData(roomId);
                } else {
                    // Cập nhật phòng hiện tại
                    handler.post(() -> currentRoom.setValue(room));
                    
                    // Bắt đầu lắng nghe thay đổi trong phòng
                    listenForRoomChanges(room.getRoomId());
                }
            }

            @Override
            public void onError(String message) {
                completed[0] = true;
                Log.e(TAG, "Error joining room: " + message);
                handler.post(() -> errorMessage.setValue(message));
            }
        });
    }

    // Phương thức mới để tải thông tin phòng đầy đủ
    private void getFullRoomData(String roomId) {
        Log.d(TAG, "Getting full room data for room: " + roomId);
        // Đăng ký listener riêng để lấy dữ liệu phòng đầy đủ
        PvpRoomHelper.listenForRoomChanges(roomId, new PvpRoomHelper.OnRoomChangedListener() {
            @Override
            public void onRoomChanged(PvpRoom room) {
                Log.d(TAG, "Got full room data: " + room.getRoomId());
                Log.d(TAG, "Room Name: " + room.getRoomName() + ", Host: " + room.getHostName());
                
                // Cập nhật phòng hiện tại
                handler.post(() -> currentRoom.setValue(room));
                
                // Chuyển sang listener chính
                listenForRoomChanges(roomId);
                
                // Chỉ cần kích hoạt một lần
                PvpRoomHelper.removeRoomListener(roomId);
            }

            @Override
            public void onRoomDeleted() {
                Log.d(TAG, "Room has been deleted while fetching full data");
                handler.post(() -> {
                    currentRoom.setValue(null);
                    errorMessage.setValue("Phòng đã bị đóng bởi chủ phòng");
                });
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error fetching full room data: " + message);
                handler.post(() -> errorMessage.setValue("Lỗi khi tải dữ liệu phòng: " + message));
            }
        });
    }

    // Lắng nghe thay đổi phòng
    private void listenForRoomChanges(String roomId) {
        Log.d(TAG, "Starting to listen for changes in room: " + roomId);
        PvpRoomHelper.listenForRoomChanges(roomId, new PvpRoomHelper.OnRoomChangedListener() {
            @Override
            public void onRoomChanged(PvpRoom room) {
                Log.d(TAG, "Room updated: " + room.getRoomId() + ", Status: " + room.getStatus());
                Log.d(TAG, "Host: " + room.getHostName() + ", Guest: " + (room.getGuestName() != null ? room.getGuestName() : "none"));
                
                // Luôn cập nhật UI để đảm bảo thông tin mới nhất được hiển thị
                handler.post(() -> currentRoom.setValue(room));
            }
            
            @Override
            public void onRoomDeleted() {
                Log.d(TAG, "Room has been deleted from database");
                handler.post(() -> {
                    currentRoom.setValue(null);
                    errorMessage.setValue("Phòng đã bị đóng bởi chủ phòng");
                });
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error listening to room: " + message);
                handler.post(() -> errorMessage.setValue("Lỗi khi theo dõi phòng: " + message));
            }
        });
    }

    // Cập nhật điểm số
    public void updateScore(int newScore) {
        PvpRoom room = currentRoom.getValue();
        if (room != null && currentUserId != null) {
            Log.d(TAG, "Updating score in room " + room.getRoomId() + " for user " + currentUserId + ": " + newScore);
            PvpRoomHelper.updateScore(room.getRoomId(), currentUserId, newScore);
        }
    }

    // Rời phòng
    public void leaveRoom() {
        PvpRoom room = currentRoom.getValue();
        if (room == null) {
            Log.d(TAG, "No room to leave");
            return;
        }
        
        String roomId = room.getRoomId();
        Log.d(TAG, "Leaving room: " + roomId);
        
        try {
            // Đảm bảo loại bỏ listener trước
            PvpRoomHelper.removeRoomListener(roomId);
            
            // Đặt phòng hiện tại về null ngay lập tức để tránh gọi lại
            currentRoom.postValue(null);
            
            // Nếu là chủ phòng thì LUÔN xóa phòng
            if (currentUserId != null && currentUserId.equals(room.getHostId())) {
                Log.d(TAG, "User is host, deleting room: " + roomId);
                
                // Xóa phòng không cần callback để tránh treo
                PvpRoomHelper.deleteRoom(roomId);
            } 
            // Nếu là khách, cập nhật phòng để chỉ còn chủ phòng
            else if (currentUserId != null && currentUserId.equals(room.getGuestId())) {
                Log.d(TAG, "User is guest, updating room to waiting state: " + roomId);
                
                // Tạo bản sao của phòng để tránh lỗi
                PvpRoom updatedRoom = new PvpRoom();
                updatedRoom.setRoomId(roomId);
                updatedRoom.setRoomName(room.getRoomName());
                updatedRoom.setHostId(room.getHostId());
                updatedRoom.setHostName(room.getHostName());
                updatedRoom.setGameType(room.getGameType());
                updatedRoom.setGameId(room.getGameId());
                updatedRoom.setStatus("WAITING");
                updatedRoom.setCreatedAt(room.getCreatedAt());
                updatedRoom.setScores(room.getScores());
                
                // Cập nhật phòng không cần callback để tránh treo
                PvpRoomHelper.updateRoom(updatedRoom);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error leaving room", e);
            // Không gọi UI để tránh lỗi
        }
    }

    // Kết thúc trò chơi
    public void finishGame() {
        PvpRoom room = currentRoom.getValue();
        if (room != null) {
            Log.d(TAG, "Finishing game in room: " + room.getRoomId());
            room.setStatus("FINISHED");
            PvpRoomHelper.updateRoom(room);
        }
    }

    // Danh sách phòng có sẵn
    public LiveData<List<PvpRoom>> getAvailableRooms() {
        Log.d(TAG, "Getting available rooms");
        return PvpRoomHelper.getAvailableRooms();
    }

    // Getter cho LiveData
    public LiveData<PvpRoom> getCurrentRoom() {
        return currentRoom;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public String getCurrentUserName() {
        return currentUserName;
    }

    // Cập nhật trạng thái phòng
    public void updateRoomStatus(String status) {
        PvpRoom room = currentRoom.getValue();
        if (room != null) {
            Log.d(TAG, "Updating room status to: " + status + " for room: " + room.getRoomId());
            room.setStatus(status);
            PvpRoomHelper.updateRoom(room);
            
            // Cập nhật LiveData ngay lập tức để UI phản ứng nhanh hơn
            currentRoom.setValue(room);
        } else {
            Log.e(TAG, "Cannot update room status: currentRoom is null");
        }
    }

    // Phương thức mới để cập nhật phòng trực tiếp
    public void setCurrentRoomDirectly(PvpRoom room) {
        if (room != null) {
            Log.d(TAG, "Setting current room directly: " + room.getRoomId());
            Log.d(TAG, "Direct room data - Name: " + room.getRoomName() + 
                  ", Host: " + room.getHostName() + 
                  ", Guest: " + (room.getGuestName() != null ? room.getGuestName() : "none"));
            currentRoom.setValue(room);
        }
    }

    // Phương thức mới để đăng ký lắng nghe thay đổi phòng trực tiếp
    public void listenForRoomChangesDirectly(String roomId) {
        if (roomId == null || roomId.isEmpty()) {
            Log.e(TAG, "Cannot listen for room changes: Room ID is null or empty");
            return;
        }
        
        Log.d(TAG, "Setting up direct room listener for: " + roomId);
        
        // Đảm bảo xóa listener cũ nếu có
        PvpRoomHelper.removeRoomListener(roomId);
        
        // Đăng ký listener mới
        listenForRoomChanges(roomId);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Rời phòng khi ViewModel bị hủy
        Log.d(TAG, "ViewModel being cleared, leaving room if any");
        leaveRoom();
        
        // Hủy tất cả các callback đang chờ xử lý
        handler.removeCallbacksAndMessages(null);
    }
} 