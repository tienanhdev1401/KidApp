package com.example.kidapp.Activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.kidapp.R;
import com.example.kidapp.Service.CloudinaryService;
import com.example.kidapp.ViewModel.UserViewModel;
import com.example.kidapp.models.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileDetailActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri = null;
    private de.hdodenhof.circleimageview.CircleImageView profileImage;
    private EditText etDob, etPhone;
    private RadioButton radioMale, radioFemale;
    private Button btnEdit, btnSave;
    private ImageView cameraIcon, backBtn;
    private UserViewModel userViewModel;
    private FirebaseUser currentUser;
    private TextView tvUsername, tvEmail;
    private RadioGroup radioGender;

    private CloudinaryService cloudinaryService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        // Khởi tạo CloudinaryService
        cloudinaryService = new CloudinaryService(this);

        // Ánh xạ view
        profileImage = findViewById(R.id.profile_image);
        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
        etDob = findViewById(R.id.et_dob);
        etPhone = findViewById(R.id.et_phone);
        radioGender = findViewById(R.id.radio_gender);
        radioMale = findViewById(R.id.radio_male);
        radioFemale = findViewById(R.id.radio_female);
        btnEdit = findViewById(R.id.btn_edit);
        btnSave = findViewById(R.id.btn_save);
        cameraIcon = findViewById(R.id.camera_icon);
        backBtn = findViewById(R.id.backBtn);

        // Xử lý sự kiện khi nhấn nút back
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                }
        });

        // Lấy user từ FirebaseAuth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Lấy user từ database qua ViewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
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
                // Hiển thị avatar bằng Glide
                if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    Glide.with(this).load(user.getAvatarUrl()).placeholder(R.drawable.animal_avatar).into(profileImage);
                } else {
                    profileImage.setImageResource(R.drawable.animal_avatar);
                }
            } else {
                Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
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

            // Nếu có ảnh mới được chọn, chuẩn bị tải lên Cloudinary
            if (selectedImageUri != null) {
                // Chuyển Uri sang File và tải lên trong AsyncTask
                new UploadImageTask(gender, dob, phone).execute(selectedImageUri);
            } else {
                // Nếu không có ảnh mới, chỉ cập nhật các thông tin khác
                updateUserProfile(currentUser.getEmail(), gender, dob, phone, null);
            }

            // Ẩn nút lưu và hiện nút chỉnh sửa
            fadeTransition(btnSave, btnEdit);
            // Disable các trường chỉnh sửa
            setEditable(false, etDob, etPhone, radioMale, radioFemale);
            // Ẩn icon camera
            cameraIcon.setVisibility(View.GONE);
        });

        // Chọn ngày sinh khi bấm vào icon lịch (drawableEnd)
        etDob.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (etDob.getRight() - etDob.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    if (etDob.isEnabled()) {
                        Calendar calendar = Calendar.getInstance();
                        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                            String dobText = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                            etDob.setText(dobText);
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
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
            // Hiển thị ảnh đã chọn lên ImageView ngay lập tức
            profileImage.setImageURI(selectedImageUri);
        }
    }

    // Phương thức cập nhật thông tin người dùng trong Firestore
    private void updateUserProfile(String email, String gender, String dob, String phone, String avatarUrl) {
        // Gọi ViewModel để thực hiện cập nhật bằng phương thức updateUser đã có
        userViewModel.updateUser(email, gender, dob, phone, avatarUrl);

        // Có thể thêm Toast hoặc xử lý phản hồi từ ViewModel tại đây nếu cần
    }

    // AsyncTask để tải ảnh lên Cloudinary trong nền
    private class UploadImageTask extends AsyncTask<Uri, Void, String> {
        private String gender, dob, phone;

        public UploadImageTask(String gender, String dob, String phone) {
            this.gender = gender;
            this.dob = dob;
            this.phone = phone;
        }

        @Override
        protected String doInBackground(Uri... uris) {
            if (uris == null || uris.length == 0 || uris[0] == null) {
                return null;
            }

            Uri imageUri = uris[0];
            File imageFile = null;
            try {
                // Tạo tệp tạm thời từ Uri
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                if (inputStream != null) {
                    imageFile = new File(getCacheDir(), "upload_" + UUID.randomUUID().toString() + ".jpg");
                    FileOutputStream outputStream = new FileOutputStream(imageFile);
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                    }
                    inputStream.close();
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            if (imageFile != null && imageFile.exists()) {
                // Gọi phương thức tải ảnh từ CloudinaryService
                String resultUrl = cloudinaryService.uploadImage(imageFile);

                // Xóa tệp tạm thời sau khi tải lên
                imageFile.delete();

                return resultUrl;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String resultUrl) {
            // Chạy trên luồng UI sau khi tải lên hoàn tất
            if (resultUrl != null && !resultUrl.isEmpty()) {
                // Cập nhật thông tin người dùng với URL avatar mới
                updateUserProfile(currentUser.getEmail(), gender, dob, phone, resultUrl);
                Toast.makeText(ProfileDetailActivity.this, "Tải ảnh lên Cloudinary thành công!", Toast.LENGTH_SHORT).show();
            } else {
                // Xử lý trường hợp tải ảnh lên thất bại
                 Toast.makeText(ProfileDetailActivity.this, "Tải ảnh lên Cloudinary thất bại!", Toast.LENGTH_SHORT).show();
                // Vẫn cập nhật các thông tin khác ngay cả khi tải ảnh lỗi
                updateUserProfile(currentUser.getEmail(), gender, dob, phone, null);
            }
        }
    }
}



