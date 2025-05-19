package com.example.kidapp.Activity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kidapp.Adapter.PvpRoomAdapter;
import com.example.kidapp.DB.FirebaseInitializer;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.PvpViewModel;
import com.example.kidapp.ViewModel.UserViewModel;
import com.example.kidapp.models.PvpRoom;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class PvpLobbyActivity extends AppCompatActivity {
    private static final String TAG = "PvpLobbyActivity";

    private RecyclerView recyclerViewRooms;
    private LinearLayout layoutNoRooms;
    private MaterialButton btnCreateRoom;
    private ImageView btnBack;
    private PvpViewModel pvpViewModel;
    private PvpRoomAdapter roomAdapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pvp_lobby);

        // Đảm bảo Firebase đã được khởi tạo
        FirebaseInitializer.initializeFirebase();

        // Kiểm tra đăng nhập
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Log.d(TAG, "User logged in: " + currentUser.getEmail());

        // Ánh xạ các view
        recyclerViewRooms = findViewById(R.id.recyclerViewRooms);
        layoutNoRooms = findViewById(R.id.layoutNoRooms);
        btnCreateRoom = findViewById(R.id.btnCreateRoom);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        
        if (progressBar == null) {
            // Thêm ProgressBar nếu layout chưa có
            addProgressBarToLayout();
        } else {
            progressBar.setVisibility(View.GONE);
        }

        // Khởi tạo ViewModel
        pvpViewModel = new ViewModelProvider(this).get(PvpViewModel.class);
        
        // Khởi tạo và truyền UserViewModel để lấy tên người dùng
        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        pvpViewModel.setUserViewModel(userViewModel);

        // Thiết lập RecyclerView
        recyclerViewRooms.setLayoutManager(new LinearLayoutManager(this));
        roomAdapter = new PvpRoomAdapter(new ArrayList<>(), this::onRoomClicked);
        recyclerViewRooms.setAdapter(roomAdapter);

        // Theo dõi danh sách phòng
        pvpViewModel.getAvailableRooms().observe(this, rooms -> {
            Log.d(TAG, "Rooms updated, count: " + (rooms != null ? rooms.size() : 0));
            updateRoomsList(rooms);
        });

        // Theo dõi phòng hiện tại
        pvpViewModel.getCurrentRoom().observe(this, room -> {
            if (room != null) {
                Log.d(TAG, "Current room updated: " + room.getRoomId());
                progressBar.setVisibility(View.GONE);
                
                // Kiểm tra dữ liệu phòng có đầy đủ không
                if (room.getRoomName() == null || room.getHostName() == null) {
                    Log.e(TAG, "Room data incomplete, will wait for full data");
                    return;
                }
                
                // Log thông tin phòng trước khi chuyển màn hình
                Log.d(TAG, "Room data before transition - Name: " + room.getRoomName() + 
                      ", Host: " + room.getHostName() + 
                      ", Guest: " + (room.getGuestName() != null ? room.getGuestName() : "none"));
                
                // Nếu đã vào phòng, chuyển sang màn hình chờ
                Intent intent = new Intent(PvpLobbyActivity.this, PvpWaitingRoomActivity.class);
                // Truyền thêm roomId để tránh vấn đề đồng bộ
                intent.putExtra("ROOM_ID", room.getRoomId());
                startActivity(intent);
            }
        });

        // Theo dõi thông báo lỗi
        pvpViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Log.e(TAG, "Error: " + errorMessage);
                progressBar.setVisibility(View.GONE);
                btnCreateRoom.setEnabled(true);
                showErrorDialog(errorMessage);
            }
        });

        // Thiết lập sự kiện nút tạo phòng
        btnCreateRoom.setOnClickListener(v -> {
            Log.d(TAG, "Create room button clicked");
            showCreateRoomDialog();
        });

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());
    }
    
    private void addProgressBarToLayout() {
        // Tạo ProgressBar mới và thêm vào layout chính
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        progressBar.setId(View.generateViewId());
        progressBar.setVisibility(View.GONE);
        
        androidx.constraintlayout.widget.ConstraintLayout mainLayout = findViewById(R.id.constraintLayout);
        if (mainLayout == null) {
            // Fallback nếu không tìm thấy ConstraintLayout
            Log.w(TAG, "Main constraint layout not found, cannot add progress bar");
            return;
        }
        
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params = 
            new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT,
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
        
        params.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        params.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        
        progressBar.setLayoutParams(params);
        mainLayout.addView(progressBar);
        this.progressBar = progressBar;
    }

    private void updateRoomsList(List<PvpRoom> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            Log.d(TAG, "No rooms available");
            layoutNoRooms.setVisibility(View.VISIBLE);
            recyclerViewRooms.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "Displaying " + rooms.size() + " rooms");
            layoutNoRooms.setVisibility(View.GONE);
            recyclerViewRooms.setVisibility(View.VISIBLE);
            roomAdapter.updateRooms(rooms);
        }
    }

    private void onRoomClicked(PvpRoom room) {
        Log.d(TAG, "Room clicked: " + room.getRoomName());
        new AlertDialog.Builder(this)
                .setTitle("Tham gia phòng")
                .setMessage("Bạn muốn tham gia phòng \"" + room.getRoomName() + "\"?")
                .setPositiveButton("Tham gia", (dialog, which) -> {
                    Log.d(TAG, "Joining room: " + room.getRoomId());
                    progressBar.setVisibility(View.VISIBLE);
                    pvpViewModel.joinRoom(room.getRoomId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Lỗi")
                .setMessage(message)
                .setPositiveButton("Đồng ý", null)
                .show();
    }

    private void showCreateRoomDialog() {
        // Kiểm tra kết nối internet trước
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        
        if (!isConnected) {
            showErrorDialog("Không có kết nối internet! Vui lòng kiểm tra kết nối của bạn và thử lại.");
            return;
        }
        
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_room, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Ánh xạ các view của dialog
        TextInputEditText etRoomName = dialogView.findViewById(R.id.etRoomName);
        RadioGroup rgGameType = dialogView.findViewById(R.id.rgGameType);
        Spinner spinnerGameLevel = dialogView.findViewById(R.id.spinnerGameLevel);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnCreate = dialogView.findViewById(R.id.btnCreate);

        // Thiết lập spinner độ khó
        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Dễ", "Trung bình", "Khó"});
        spinnerGameLevel.setAdapter(levelAdapter);

        // Sự kiện nút hủy
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Sự kiện nút tạo phòng
        btnCreate.setOnClickListener(v -> {
            String roomName = etRoomName.getText().toString().trim();
            if (roomName.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên phòng", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy loại game được chọn
            int selectedGameTypeId = rgGameType.getCheckedRadioButtonId();
            RadioButton selectedRadioButton = dialogView.findViewById(selectedGameTypeId);
            String gameType = getGameTypeFromRadioButton(selectedRadioButton);

            // Lấy độ khó
            String gameLevel = spinnerGameLevel.getSelectedItem().toString();
            String gameId = gameLevel.toLowerCase(); // Đơn giản hóa, trong thực tế nên sử dụng ID thật

            Log.d(TAG, "Creating new room: " + roomName + ", Game type: " + gameType + ", Level: " + gameId);
            
            // Hiển thị đang tải và vô hiệu hóa nút tạo phòng
            progressBar.setVisibility(View.VISIBLE);
            btnCreateRoom.setEnabled(false);
            
            // Tạo phòng
            pvpViewModel.createRoom(roomName, gameType, gameId);
            dialog.dismiss();
        });
    }

    private String getGameTypeFromRadioButton(RadioButton radioButton) {
        if (radioButton == null) return "FLIP_CARD"; // Mặc định

        int id = radioButton.getId();
        if (id == R.id.rbFlipCard) return "FLIP_CARD";
        if (id == R.id.rbPuzzle) return "PUZZLE";
        if (id == R.id.rbGuessWord) return "GUESS_WORD";
        if (id == R.id.rbMath) return "MATH";
        
        return "FLIP_CARD"; // Mặc định
    }
} 