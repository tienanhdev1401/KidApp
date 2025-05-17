package com.example.kidapp.Activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.example.kidapp.DB.FirebaseInitializer;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.PvpViewModel;
import com.example.kidapp.ViewModel.UserViewModel;
import com.example.kidapp.models.PvpRoom;

public class PvpWaitingRoomActivity extends AppCompatActivity {
    private static final String TAG = "PvpWaitingRoomActivityiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii";

    private TextView tvRoomName, tvGameType, tvHostName, tvGuestName;
    private TextView tvPlayers, tvRoomCode, tvWaiting, tvShareCode;
    private Button btnCancel, btnStart;
    private ImageView btnBack;
    private LottieAnimationView animationView;
    private CardView cardGuest;
    private PvpViewModel pvpViewModel;
    private boolean isLeavingRoom = false;
    private boolean hasRemovedObservers = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pvp_waiting_room);

        // Đảm bảo Firebase đã được khởi tạo
        FirebaseInitializer.initializeFirebase();
        
        Log.d(TAG, "Activity created");

        // Ánh xạ các view
        initViews();

        // Khởi tạo ViewModel
        pvpViewModel = new ViewModelProvider(this).get(PvpViewModel.class);
        
        // Khởi tạo và truyền UserViewModel để lấy tên người dùng
        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        pvpViewModel.setUserViewModel(userViewModel);

        // Kiểm tra Intent có dữ liệu phòng không
        Intent intent = getIntent();
        if (intent.hasExtra("ROOM_ID")) {
            String roomId = intent.getStringExtra("ROOM_ID");
            Log.d(TAG, "Received room ID from intent: " + roomId);
            
            // Tải dữ liệu phòng trực tiếp từ Firebase
            loadRoomDataDirectly(roomId);
        }

        setupObservers();

        // Sự kiện nút hủy
        btnCancel.setOnClickListener(v -> {
            Log.d(TAG, "Cancel button clicked");
            leaveRoom();
        });

        // Sự kiện nút quay lại
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            leaveRoom();
        });

        // Sự kiện nút bắt đầu
        btnStart.setOnClickListener(v -> {
            Log.d(TAG, "Start button clicked");
            PvpRoom room = pvpViewModel.getCurrentRoom().getValue();
            if (room != null && room.getGuestId() != null && !room.getGuestId().isEmpty()) {
                // Cập nhật trạng thái trước khi bắt đầu
                Log.d(TAG, "Updating room status to PLAYING");
                pvpViewModel.updateRoomStatus("PLAYING");
                // Game sẽ tự động bắt đầu khi cập nhật trạng thái phòng và
                // listener phát hiện trạng thái là PLAYING trong updateRoomUI
            } else {
                Toast.makeText(this, "Chưa đủ người chơi để bắt đầu", Toast.LENGTH_SHORT).show();
            }
        });

        // Sự kiện copy mã phòng
        tvRoomCode.setOnClickListener(v -> {
            PvpRoom room = pvpViewModel.getCurrentRoom().getValue();
            if (room != null) {
                copyToClipboard(room.getRoomId());
                Toast.makeText(this, "Đã sao chép mã phòng", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Room code copied: " + room.getRoomId());
            }
        });
    }

    private void initViews() {
        tvRoomName = findViewById(R.id.tvRoomName);
        tvGameType = findViewById(R.id.tvGameType);
        tvHostName = findViewById(R.id.tvHostName);
        tvGuestName = findViewById(R.id.tvGuestName);
        tvPlayers = findViewById(R.id.tvPlayers);
        tvRoomCode = findViewById(R.id.tvRoomCode);
        tvWaiting = findViewById(R.id.tvWaiting);
        tvShareCode = findViewById(R.id.tvShareCode);
        btnCancel = findViewById(R.id.btnCancel);
        btnStart = findViewById(R.id.btnStart);
        btnBack = findViewById(R.id.btnBack);
        animationView = findViewById(R.id.animationView);
        cardGuest = findViewById(R.id.cardGuest);
    }

    private void updateRoomUI(PvpRoom room) {
        if (room == null) {
            Log.d(TAG, "Room is null, finishing activity");
            finish();
            return;
        }

        Log.d(TAG, "Updating room UI for room: " + room.getRoomId());
        Log.d(TAG, "Room status: " + room.getStatus());
        Log.d(TAG, "Host: " + room.getHostName() + " (ID: " + room.getHostId() + ")");
        Log.d(TAG, "Guest: " + (room.getGuestName() != null ? room.getGuestName() : "null") + 
                " (ID: " + (room.getGuestId() != null ? room.getGuestId() : "null") + ")");

        // Kiểm tra xem thông tin phòng có đầy đủ không
        if (room.getRoomName() == null || room.getHostName() == null) {
            Log.e(TAG, "Room data incomplete, cannot update UI");
            return;
        }

        // Cập nhật thông tin phòng
        tvRoomName.setText("Tên phòng: " + room.getRoomName());
        tvRoomCode.setText("Mã phòng: " + room.getRoomId());
        tvHostName.setText(room.getHostName() != null ? room.getHostName() : "Chủ phòng");

        // Cập nhật loại game
        String gameTypeText = "Trò chơi: ";
        switch (room.getGameType()) {
            case "FLIP_CARD":
                gameTypeText += "Lật thẻ nhớ hình";
                break;
            case "PUZZLE":
                gameTypeText += "Xếp hình";
                break;
            case "GUESS_WORD":
                gameTypeText += "Đoán chữ";
                break;
            case "MATH":
                gameTypeText += "Làm toán";
                break;
            default:
                gameTypeText += "Không xác định";
                break;
        }
        tvGameType.setText(gameTypeText);

        // Cập nhật thông tin khách
        String currentUserId = pvpViewModel.getCurrentUserId();
        boolean isHost = currentUserId != null && currentUserId.equals(room.getHostId());
        Log.d(TAG, "Current user is host: " + isHost);

        boolean hasGuest = room.getGuestId() != null && !room.getGuestId().isEmpty();
        
        Log.d(TAG, "Has guest: " + hasGuest + ", Guest name: " + 
                (room.getGuestName() != null ? room.getGuestName() : "null"));
        
        if (hasGuest) {
            tvGuestName.setTextColor(getResources().getColor(android.R.color.black));
            tvGuestName.setTypeface(null, android.graphics.Typeface.NORMAL);
            tvGuestName.setText(room.getGuestName() != null ? room.getGuestName() : "Người chơi");
            tvPlayers.setText("Người chơi (2/2)");
            tvWaiting.setText("Sẵn sàng để bắt đầu!");
            
            // Chỉ chủ phòng mới có thể bắt đầu game
            btnStart.setVisibility(isHost ? View.VISIBLE : View.GONE);
            btnStart.setEnabled(isHost);
            
            // Ẩn nội dung chia sẻ
            tvShareCode.setVisibility(View.GONE);
            
            Log.d(TAG, "Guest joined: " + room.getGuestName() + ", Start button enabled: " + isHost);
        } else {
            tvGuestName.setTextColor(getResources().getColor(android.R.color.darker_gray));
            tvGuestName.setTypeface(null, android.graphics.Typeface.ITALIC);
            tvGuestName.setText("Đang chờ người chơi...");
            tvPlayers.setText("Người chơi (1/2)");
            tvWaiting.setText("Đang chờ người chơi khác tham gia...");
            btnStart.setEnabled(false);
            btnStart.setVisibility(isHost ? View.VISIBLE : View.GONE);
            
            // Hiển thị nội dung chia sẻ
            tvShareCode.setVisibility(View.VISIBLE);
            
            Log.d(TAG, "Waiting for guest to join, Start button disabled");
        }

        // Đổi tên nút hủy phòng tùy vào vai trò
        if (isHost) {
            btnCancel.setText("Hủy Phòng");
        } else {
            btnCancel.setText("Rời Phòng");
        }

        // Nếu phòng đã bắt đầu chơi, chuyển đến màn hình game
        if ("PLAYING".equals(room.getStatus())) {
            Log.d(TAG, "Room status is PLAYING, starting game");
            startGame();
        }
    }

    private void startGame() {
        PvpRoom room = pvpViewModel.getCurrentRoom().getValue();
        if (room == null) return;

        Log.d(TAG, "Starting game of type: " + room.getGameType());
        
        // Tạo Intent dựa trên loại game
        Intent gameIntent = null;
        
        switch (room.getGameType()) {
            case "FLIP_CARD":
                gameIntent = new Intent(this, GameLatTheActivity.class);
                Log.d(TAG, "Creating intent for FLIP_CARD game");
                break;
            case "PUZZLE":
                gameIntent = new Intent(this, GameXepHinhActivity.class);
                Log.d(TAG, "Creating intent for PUZZLE game");
                break;
            case "GUESS_WORD":
                gameIntent = new Intent(this, GameDoanChuActivity.class);
                Log.d(TAG, "Creating intent for GUESS_WORD game");
                break;
            case "MATH":
                gameIntent = new Intent(this, MathQuizActivity.class);
                Log.d(TAG, "Creating intent for MATH game");
                break;
        }

        if (gameIntent != null) {
            // Thêm thông tin room vào intent
            gameIntent.putExtra("ROOM_ID", room.getRoomId());
            gameIntent.putExtra("IS_PVP_MODE", true);
            gameIntent.putExtra("GAME_LEVEL", room.getGameId());
            
            Log.d(TAG, "Starting game activity with ROOM_ID: " + room.getRoomId());
            startActivity(gameIntent);
        } else {
            Log.e(TAG, "Could not create game intent for game type: " + room.getGameType());
            Toast.makeText(this, "Không thể bắt đầu trò chơi này", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Room Code", text);
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public void onBackPressed() {
        // Hiển thị dialog xác nhận khi nhấn nút back
        leaveRoom();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Activity being destroyed, isLeavingRoom = " + isLeavingRoom);
        
        // Xóa observer bất kể lí do gì để ngăn chặn callback không mong muốn
        if (!hasRemovedObservers) {
            Log.d(TAG, "Removing all observers in onDestroy");
            pvpViewModel.getCurrentRoom().removeObservers(this);
            pvpViewModel.getErrorMessage().removeObservers(this);
            hasRemovedObservers = true;
        }
        
        // Chỉ gọi leaveRoom nếu activity bị hủy không phải do người dùng nhấn nút rời phòng
        if (isFinishing() && !isLeavingRoom && pvpViewModel != null) {
            Log.d(TAG, "Activity finishing without explicit leave, leaving room in background");
            isLeavingRoom = true; // Đánh dấu đang rời phòng
            
            // Xử lý rời phòng ở background thread
            new Thread(() -> {
                try {
                    pvpViewModel.leaveRoom();
                    Log.d(TAG, "Room left in background from onDestroy");
                } catch (Exception e) {
                    Log.e(TAG, "Error leaving room from onDestroy", e);
                }
            }).start();
        }
    }

    // Tách phương thức thiết lập observer để dễ quản lý
    private void setupObservers() {
        // Theo dõi phòng hiện tại
        pvpViewModel.getCurrentRoom().observe(this, room -> {
            Log.d(TAG, "Current room updated: " + (room != null ? room.getRoomId() : "null"));
            
            if (room == null) {
                // Chỉ kết thúc activity khi không phải đang trong quá trình chủ động rời phòng
                if (!isLeavingRoom) {
                    Log.d(TAG, "Room is null, finishing activity (from observer)");
                    Toast.makeText(this, "Phòng đã bị đóng", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
            
            // Log thông tin phòng để debug
            Log.d(TAG, "Room ID: " + room.getRoomId());
            Log.d(TAG, "Room Name: " + room.getRoomName());
            Log.d(TAG, "Host ID: " + room.getHostId() + ", Host Name: " + room.getHostName());
            Log.d(TAG, "Guest ID: " + (room.getGuestId() != null ? room.getGuestId() : "none") + 
                    ", Guest Name: " + (room.getGuestName() != null ? room.getGuestName() : "none"));
            Log.d(TAG, "Status: " + room.getStatus());
            
            // Cập nhật UI chỉ khi room có dữ liệu đầy đủ
            if (room.getRoomName() != null && room.getHostName() != null) {
                updateRoomUI(room);
            } else {
                Log.e(TAG, "Room data incomplete, will update when complete data arrives");
            }
        });

        // Theo dõi thông báo lỗi
        pvpViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Log.e(TAG, "Error: " + errorMessage);
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                
                // Nếu lỗi là phòng đã bị đóng, quay lại màn hình lobby
                if (errorMessage.contains("Phòng đã bị đóng")) {
                    finish();
                }
            }
        });
    }

    // Phương thức để rời phòng an toàn - đã thiết kế lại hoàn toàn
    private void leaveRoom() {
        // Kiểm tra đã trong quá trình rời phòng chưa để tránh gọi nhiều lần
        if (isLeavingRoom) {
            Log.d(TAG, "Already leaving room, ignoring duplicate request");
            return;
        }
        
        // Đánh dấu đang rời phòng 
        isLeavingRoom = true;
        
        // Lưu lại thông tin phòng hiện tại trước khi xóa observer
        final PvpRoom currentRoom = pvpViewModel.getCurrentRoom().getValue();
        
        // Xóa tất cả observer ngay lập tức để ngăn chặn callback không mong muốn
        Log.d(TAG, "Removing all observers");
        pvpViewModel.getCurrentRoom().removeObservers(this);
        pvpViewModel.getErrorMessage().removeObservers(this);
        hasRemovedObservers = true;
        
        // Chạy tiến trình rời phòng trong background thread
        new Thread(() -> {
            try {
                if (currentRoom != null) {
                    Log.d(TAG, "Leaving room in background: " + currentRoom.getRoomId());
                }
                pvpViewModel.leaveRoom();
                Log.d(TAG, "Room left in background");
            } catch (Exception e) {
                Log.e(TAG, "Error leaving room in background", e);
            }
        }).start();
        
        // Kết thúc Activity ngay lập tức mà không đợi tiến trình hoàn tất
        Log.d(TAG, "Finishing activity immediately");
        runOnUiThread(() -> {
            finish();
            // Thêm hiệu ứng transition để cải thiện UX
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    // Phương thức mới để tải dữ liệu phòng trực tiếp từ Firebase
    private void loadRoomDataDirectly(String roomId) {
        Log.d(TAG, "Loading room data directly from Firebase for room: " + roomId);
        
        // Tham chiếu đến phòng trong Firebase
        com.google.firebase.database.DatabaseReference roomRef = 
            FirebaseInitializer.getPvpRoomsRef().child(roomId);
        
        roomRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.e(TAG, "Room does not exist: " + roomId);
                    Toast.makeText(PvpWaitingRoomActivity.this, 
                        "Phòng không tồn tại hoặc đã bị xóa", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                
                try {
                    Log.d(TAG, "Raw room data from Firebase: " + dataSnapshot.toString());
                    
                    // Tạo đối tượng phòng mới
                    PvpRoom room = new PvpRoom();
                    room.setRoomId(roomId);
                    
                    // Đọc từng trường dữ liệu
                    if (dataSnapshot.child("roomName").exists()) {
                        room.setRoomName(dataSnapshot.child("roomName").getValue(String.class));
                        Log.d(TAG, "Room Name: " + room.getRoomName());
                    }
                    
                    if (dataSnapshot.child("hostId").exists()) {
                        room.setHostId(dataSnapshot.child("hostId").getValue(String.class));
                        Log.d(TAG, "Host ID: " + room.getHostId());
                    }
                    
                    if (dataSnapshot.child("hostName").exists()) {
                        room.setHostName(dataSnapshot.child("hostName").getValue(String.class));
                        Log.d(TAG, "Host Name: " + room.getHostName());
                    }
                    
                    if (dataSnapshot.child("guestId").exists()) {
                        room.setGuestId(dataSnapshot.child("guestId").getValue(String.class));
                        Log.d(TAG, "Guest ID: " + room.getGuestId());
                    }
                    
                    if (dataSnapshot.child("guestName").exists()) {
                        room.setGuestName(dataSnapshot.child("guestName").getValue(String.class));
                        Log.d(TAG, "Guest Name: " + room.getGuestName());
                    }
                    
                    if (dataSnapshot.child("gameType").exists()) {
                        room.setGameType(dataSnapshot.child("gameType").getValue(String.class));
                        Log.d(TAG, "Game Type: " + room.getGameType());
                    }
                    
                    if (dataSnapshot.child("gameId").exists()) {
                        room.setGameId(dataSnapshot.child("gameId").getValue(String.class));
                    }
                    
                    if (dataSnapshot.child("status").exists()) {
                        room.setStatus(dataSnapshot.child("status").getValue(String.class));
                        Log.d(TAG, "Status: " + room.getStatus());
                    }
                    
                    if (dataSnapshot.child("createdAt").exists()) {
                        room.setCreatedAt(dataSnapshot.child("createdAt").getValue(Long.class));
                    }
                    
                    // Đọc scores nếu có
                    if (dataSnapshot.child("scores").exists()) {
                        java.util.Map<String, Integer> scores = new java.util.HashMap<>();
                        for (com.google.firebase.database.DataSnapshot scoreSnapshot : 
                                dataSnapshot.child("scores").getChildren()) {
                            String userId = scoreSnapshot.getKey();
                            Integer score = scoreSnapshot.getValue(Integer.class);
                            if (userId != null && score != null) {
                                scores.put(userId, score);
                            }
                        }
                        room.setScores(scores);
                    }
                    
                    // Cập nhật ViewModel
                    pvpViewModel.setCurrentRoomDirectly(room);
                    
                    // Đăng ký lắng nghe thay đổi phòng
                    pvpViewModel.listenForRoomChangesDirectly(roomId);
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing room data", e);
                    Toast.makeText(PvpWaitingRoomActivity.this, 
                        "Lỗi khi đọc dữ liệu phòng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Log.e(TAG, "Error loading room data", databaseError.toException());
                Toast.makeText(PvpWaitingRoomActivity.this, 
                    "Lỗi kết nối đến server: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
} 