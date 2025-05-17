package com.example.kidapp;

import android.app.Application;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.kidapp.DB.FirebaseInitializer;
import com.google.firebase.database.FirebaseDatabase;

public class KidAppApplication extends Application {
    private static final String TAG = "KidAppApplication";
    private static boolean persistenceEnabled = false;

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Bật persistence TRƯỚC khi sử dụng bất kỳ phương thức Firebase nào khác
        if (!persistenceEnabled) {
            try {
                // Đây là điểm quan trọng - phải gọi setPersistenceEnabled() TRƯỚC khi
                // bất kỳ phương thức Firebase nào khác được gọi
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
                persistenceEnabled = true;
                Log.d(TAG, "Firebase persistence enabled FIRST - SUCCESS");
            } catch (Exception e) {
                Log.e(TAG, "Firebase persistence setting error: " + e.getMessage(), e);
            }
        }
        
        // Kiểm tra kết nối internet
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        
        Log.d(TAG, "Internet connection available: " + isConnected);
        
        // Khởi tạo Firebase Realtime Database
        try {
            FirebaseInitializer.initializeFirebase();
            Log.d(TAG, "Firebase Realtime Database initialized successfully.");
            
            // In ra URL của Firebase
            String databaseUrl = FirebaseDatabase.getInstance().getReference().toString();
            Log.d(TAG, "Firebase Database URL: " + databaseUrl);
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
        }
    }
} 