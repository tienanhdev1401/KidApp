package com.example.kidapp.Activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.kidapp.R;
import com.example.kidapp.ViewModel.UserViewModel;
import com.example.kidapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.bumptech.glide.Glide;

import java.util.Calendar;

public class ProfileDetailActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        // Ánh xạ view
        de.hdodenhof.circleimageview.CircleImageView profileImage = findViewById(R.id.profile_image);
        TextView tvUsername = findViewById(R.id.tv_username);
        TextView tvEmail = findViewById(R.id.tv_email);
        EditText etDob = findViewById(R.id.et_dob);
        EditText etPhone = findViewById(R.id.et_phone);
        RadioGroup radioGender = findViewById(R.id.radio_gender);
        RadioButton radioMale = findViewById(R.id.radio_male);
        RadioButton radioFemale = findViewById(R.id.radio_female);
        Button btnEdit = findViewById(R.id.btn_edit);
        Button btnSave = findViewById(R.id.btn_save);
        ImageView cameraIcon = findViewById(R.id.camera_icon);

        // Lấy user từ FirebaseAuth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        // Lấy user từ database qua ViewModel
        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getUserByEmail(currentUser.getEmail()).observe(this, user -> {
            if (user != null) {
                tvUsername.setText(user.getUsername() != null ? user.getUsername() : "");
                tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                etDob.setText(user.getDateOfBirth() != null ? user.getDateOfBirth() : "");
                etPhone.setText(user.getPhone() != null ? user.getPhone() : "");
                if (user.getGender() != null) {
                    if (user.getGender().equalsIgnoreCase("Nam")) {
                        radioMale.setChecked(true);
                    } else if (user.getGender().equalsIgnoreCase("Nữ")) {
                        radioFemale.setChecked(true);
                    } else {
                        radioMale.setChecked(false);
                        radioFemale.setChecked(false);
                    }
                } else {
                    radioMale.setChecked(false);
                    radioFemale.setChecked(false);
                }
                // Hiển thị avatar
                if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    Glide.with(this).load(user.getAvatarUrl()).into(profileImage);
                } else {
                    profileImage.setImageResource(R.drawable.animal_avatar);
                }
            }
        });

        // Ban đầu: disable các trường, chỉ hiện nút chỉnh sửa
        setEditable(false, etDob, etPhone, radioMale, radioFemale);
        btnSave.setVisibility(View.GONE);
        btnEdit.setVisibility(View.VISIBLE);

        // Ẩn camera icon ban đầu
        cameraIcon.setVisibility(View.GONE);

        // Sự kiện nút chỉnh sửa
        btnEdit.setOnClickListener(v -> {
            fadeTransition(btnEdit, btnSave);
            setEditable(true, etDob, etPhone, radioMale, radioFemale);
            cameraIcon.setVisibility(View.VISIBLE);
        });

        // Sự kiện nút lưu
        btnSave.setOnClickListener(v -> {
            String gender = radioMale.isChecked() ? "Nam" : (radioFemale.isChecked() ? "Nữ" : "");
            String dob = etDob.getText().toString();
            String phone = etPhone.getText().toString();
            String avatarUrl = null;
            if (selectedImageUri != null) {
                avatarUrl = selectedImageUri.toString(); // chỉ lưu uri local
            } else if (currentUser != null && currentUser.getPhotoUrl() != null) {
                avatarUrl = currentUser.getPhotoUrl().toString();
            }
            userViewModel.updateUser(currentUser.getEmail(), gender, dob, phone, avatarUrl);
            fadeTransition(btnSave, btnEdit);
            setEditable(false, etDob, etPhone, radioMale, radioFemale);
            cameraIcon.setVisibility(View.GONE);
        });

        // Chọn ngày sinh khi bấm vào icon lịch (drawableEnd)
        etDob.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (etDob.getRight() - etDob.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    if (etDob.isEnabled()) {
                        Calendar calendar = Calendar.getInstance();
                        DatePickerDialog datePickerDialog = new DatePickerDialog(
                                this,
                                (view, year, month, dayOfMonth) -> {
                                    String dob = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                                    etDob.setText(dob);
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                        );
                        datePickerDialog.show();
                    }
                    return true;
                }
            }
            return false;
        });

        // Chọn ảnh từ máy khi bấm vào icon camera
        cameraIcon.setOnClickListener(v -> {
            if (cameraIcon.getVisibility() == View.VISIBLE) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // You can add any additional back button behavior here
    }

    // Hàm enable/disable các trường
    private void setEditable(boolean editable, EditText etDob, EditText etPhone, RadioButton radioMale, RadioButton radioFemale) {
        etDob.setEnabled(editable);
        etPhone.setEnabled(editable);
        radioMale.setEnabled(editable);
        radioFemale.setEnabled(editable);
    }

    // Hiệu ứng chuyển fade giữa 2 nút
    private void fadeTransition(View from, View to) {
        AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(200);
        AlphaAnimation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(200);
        from.startAnimation(fadeOut);
        from.setVisibility(View.GONE);
        // Delay để đảm bảo hiệu ứng mượt
        new Handler().postDelayed(() -> {
            to.setVisibility(View.VISIBLE);
            to.startAnimation(fadeIn);
        }, 200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            de.hdodenhof.circleimageview.CircleImageView profileImage = findViewById(R.id.profile_image);
            profileImage.setImageURI(selectedImageUri);
        }
    }
}