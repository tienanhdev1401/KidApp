package com.example.kidapp.DB;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseInitializer {
    private static final String TAG = "FirebaseInitializer";
    private static boolean isInitialized = false;
    private static FirebaseDatabase firebaseDatabase = null;
    

    private static final String FIREBASE_DB_URL = "https://kidapp-3615e-default-rtdb.asia-southeast1.firebasedatabase.app";

    public static void initializeFirebase() {
        if (!isInitialized) {
            try {
                // Sử dụng instance đã tạo hoặc tạo mới
                if (firebaseDatabase == null) {
                    // Sử dụng URL cụ thể thay vì mặc định
                    firebaseDatabase = FirebaseDatabase.getInstance(FIREBASE_DB_URL);
                    Log.d(TAG, "Using specific Firebase URL: " + FIREBASE_DB_URL);
                    
                    // KHÔNG bật persistence ở đây nữa, đã được bật trong Application
                    // firebaseDatabase.setPersistenceEnabled(true);
                    // Log.d(TAG, "Firebase offline persistence enabled");
                }
            } catch (Exception e) {
                Log.e(TAG, "Firebase initialization error: " + e.getMessage());
                // Nếu có lỗi, vẫn tiếp tục với instance mặc định
                if (firebaseDatabase == null) {
                    firebaseDatabase = FirebaseDatabase.getInstance();
                }
            }
            
            // Thiết lập các cấu hình khác sau khi đã khởi tạo
            try {
                // Establish connection to Firebase
                DatabaseReference rootRef = firebaseDatabase.getReference();
                rootRef.keepSynced(true);
                
                isInitialized = true;
                Log.d(TAG, "Firebase initialized successfully");
                
                // Kiểm tra kết nối thực tế
                checkConnectivity();
            } catch (Exception e) {
                Log.e(TAG, "Error in Firebase initialization: " + e.getMessage(), e);
            }
        }
    }
    
    public static DatabaseReference getPvpRoomsRef() {
        if (firebaseDatabase == null) {
            initializeFirebase();
        }
        return firebaseDatabase.getReference("pvp_rooms");
    }
    
    // Hàm kiểm tra kết nối thực tế đến Firebase server
    private static void checkConnectivity() {
        DatabaseReference connectedRef = firebaseDatabase.getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);
                if (connected != null && connected) {
                    Log.d(TAG, "Successfully connected to Firebase server");
                } else {
                    Log.e(TAG, "Not connected to Firebase server. Check your internet connection and Firebase console configuration.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Connection check was cancelled: " + error.getMessage());
            }
        });
        
        // Thử một hoạt động thực tế (đọc/ghi)
        DatabaseReference testRef = firebaseDatabase.getReference("connection_test");
        testRef.setValue("test_" + System.currentTimeMillis())
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Test write to Firebase successful");
                    testRef.removeValue(); // Dọn dẹp sau khi kiểm tra
                } else {
                    Log.e(TAG, "Test write to Firebase failed: " + 
                            (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                }
            });
    }
} 