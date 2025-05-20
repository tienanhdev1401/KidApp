package com.example.kidapp.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kidapp.Adapter.ManualStoryPageAdapter;
import com.example.kidapp.Adapter.StoryElementAdapter;
import com.example.kidapp.BuildConfig;
import com.example.kidapp.R;
import com.example.kidapp.Service.CloudinaryService;
import com.example.kidapp.ViewModel.ManualStoryViewModel;
import com.example.kidapp.ViewModel.StoryElementViewModel;
import com.example.kidapp.models.ManualStory;
import com.example.kidapp.models.StoryElement;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManualStoryCreatorActivity extends AppCompatActivity implements ManualStoryPageAdapter.OnPageClickListener {

    private Toolbar toolbar;
    private EditText etStoryTitle;
    private ImageView ivCoverImage;
    private RecyclerView rvPages;
    private ExtendedFloatingActionButton fabAddPage;
    private View loadingView;
    private MaterialCardView cardStoryElements;
    private TextView tvSelectedElements;
    private Button btnSelectElements;

    private ManualStoryViewModel viewModel;
    private StoryElementViewModel storyElementViewModel;
    private ManualStoryPageAdapter adapter;
    private ManualStory story;
    
    // Phần tử truyện đã chọn
    private StoryElement selectedSetting;
    private List<StoryElement> selectedCharacters = new ArrayList<>();
    private List<StoryElement> selectedItems = new ArrayList<>();
    private static final int MAX_CHARACTER_SELECTIONS = 3;
    private static final int MAX_ITEM_SELECTIONS = 3;
    
    private Uri currentPhotoUri;
    private int selectedPagePosition = -1;
    private boolean isEditingCover = false;
    
    private static final int REQUEST_PERMISSION_CODE = 123;
    
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(this, "Cần cấp quyền để chọn ảnh", Toast.LENGTH_SHORT).show();
                }
            });
    
    private final ActivityResultLauncher<Intent> takePictureLauncher = 
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    processSelectedImage(currentPhotoUri);
                }
            });
    
    private final ActivityResultLauncher<Intent> pickImageLauncher = 
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        processSelectedImage(selectedImageUri);
                    }
                }
            });

    private CloudinaryService cloudinaryService;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_story_creator);
        
        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ManualStoryViewModel.class);
        storyElementViewModel = new ViewModelProvider(this, new StoryElementViewModel.Factory())
                .get(StoryElementViewModel.class);
        
        // Ánh xạ view
        toolbar = findViewById(R.id.toolbar);
        etStoryTitle = findViewById(R.id.etStoryTitle);
        ivCoverImage = findViewById(R.id.ivCoverImage);
        rvPages = findViewById(R.id.rvPages);
        fabAddPage = findViewById(R.id.fabAddPage);
        loadingView = findViewById(R.id.loadingView);
        cardStoryElements = findViewById(R.id.cardStoryElements);
        tvSelectedElements = findViewById(R.id.tvSelectedElements);
        btnSelectElements = findViewById(R.id.btnSelectElements);
        
        // Thiết lập toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tạo truyện thủ công");
        }
        
        // Khởi tạo truyện mới
        viewModel.createNewStory("Truyện mới");
        story = viewModel.getCurrentStory();
        
        // Thiết lập RecyclerView
        setupRecyclerView();
        
        // Thiết lập listener
        setupListeners();
        
        // Theo dõi trạng thái loading
        viewModel.getIsLoading().observe(this, isLoading -> {
            loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        // Theo dõi thông báo lỗi
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Observer cho các phần tử truyện
        setupStoryElementObservers();
        
        // Làm mới dữ liệu từ repository
        Log.d("ManualStory", "Gọi refreshData trong onCreate");
        storyElementViewModel.refreshData();

        // Khởi tạo CloudinaryService và ExecutorService
        cloudinaryService = new CloudinaryService(this);
        executorService = Executors.newSingleThreadExecutor();
    }

    private void setupStoryElementObservers() {
        storyElementViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                loadingView.setVisibility(View.VISIBLE);
            } else {
                loadingView.setVisibility(View.GONE);
                // Log khi đã tải xong dữ liệu
                Log.d("ManualStory", "Đã tải xong dữ liệu, ẩn loadingView");
            }
        });

        storyElementViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                Log.e("ManualStory", "Lỗi: " + error);
            }
        });
        
        // Theo dõi dữ liệu characters
        storyElementViewModel.getCharacters().observe(this, characters -> {
            Log.d("ManualStory", "Activity nhận được dữ liệu characters: " + characters.size() + " nhân vật");
        });
        
        // Theo dõi dữ liệu settings
        storyElementViewModel.getSettings().observe(this, settings -> {
            Log.d("ManualStory", "Activity nhận được dữ liệu settings: " + settings.size() + " bối cảnh");
        });
        
        // Theo dõi dữ liệu items
        storyElementViewModel.getItems().observe(this, items -> {
            Log.d("ManualStory", "Activity nhận được dữ liệu items: " + items.size() + " vật phẩm");
        });
    }

    private void setupRecyclerView() {
        adapter = new ManualStoryPageAdapter(this, new ArrayList<>(), this);
        rvPages.setLayoutManager(new LinearLayoutManager(this));
        rvPages.setAdapter(adapter);
        
        // Thêm hỗ trợ kéo thả để sắp xếp lại các trang
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, 
                                 @NonNull RecyclerView.ViewHolder viewHolder, 
                                 @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                
                viewModel.movePage(fromPosition, toPosition);
                adapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }
            
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                showDeleteConfirmDialog(position);
            }
        };
        
        new ItemTouchHelper(callback).attachToRecyclerView(rvPages);
    }

    private void setupListeners() {
        // Cập nhật tiêu đề
        etStoryTitle.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String title = etStoryTitle.getText().toString().trim();
                if (!title.isEmpty()) {
                    story.setTitle(title);
                }
            }
        });
        
        // Sự kiện click vào ảnh bìa
        ivCoverImage.setOnClickListener(v -> {
            isEditingCover = true;
            checkPermissionAndOpenImagePicker();
        });
        
        // Sự kiện click vào nút thêm trang
        fabAddPage.setOnClickListener(v -> {
            showAddPageDialog();
        });
        
        // Sự kiện click vào nút chọn phần tử truyện
        btnSelectElements.setOnClickListener(v -> {
            showElementSelectionDialog();
        });
    }

    @Override
    public void onPageClick(int position) {
        // Hiển thị menu popup với các tùy chọn
        PopupMenu popupMenu = new PopupMenu(this, rvPages.findViewHolderForAdapterPosition(position).itemView);
        popupMenu.getMenuInflater().inflate(R.menu.menu_page_options, popupMenu.getMenu());
        
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit_page) {
                // Mở dialog chỉnh sửa trang
                showEditPageDialog(position);
                return true;
            } else if (id == R.id.action_preview_page) {
                // Mở dialog xem trước trang
                showPreviewPageDialog(position);
                return true;
            } else if (id == R.id.action_delete_page) {
                // Hiển thị dialog xác nhận xóa
                showDeleteConfirmDialog(position);
                return true;
            }
            return false;
        });
        
        popupMenu.show();
    }

    @Override
    public void onImageClick(int position) {
        // Chọn ảnh mới cho trang
        selectedPagePosition = position;
        isEditingCover = false;
        checkPermissionAndOpenImagePicker();
    }

    private void checkPermissionAndOpenImagePicker() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13 trở lên cần quyền READ_MEDIA_IMAGES thay cho READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA}, 
                        REQUEST_PERMISSION_CODE);
            } else {
                openImagePicker();
            }
        } else {
            // Android 12 trở xuống vẫn dùng READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 
                        REQUEST_PERMISSION_CODE);
            } else {
                openImagePicker();
            }
        }
    }

    private void openImagePicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ảnh từ")
                .setItems(new CharSequence[]{"Máy ảnh", "Thư viện"}, (dialog, which) -> {
                    if (which == 0) {
                        // Máy ảnh
                        openCamera();
                    } else {
                        // Thư viện
                        openGallery();
                    }
                });
        builder.show();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Lỗi tạo file ảnh", Toast.LENGTH_SHORT).show();
            }
            
            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);
                takePictureLauncher.launch(takePictureIntent);
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void processSelectedImage(Uri imageUri) {
        try {
            File imageFile = createImageFileFromUri(imageUri);
            
            // Hiển thị ảnh ngay lập tức (trước khi upload)
            if (isEditingCover) {
                Glide.with(this).load(imageUri).into(ivCoverImage);
            } else if (selectedPagePosition != -1) {
                // Cập nhật UI trang trước khi upload
                adapter.updatePageImageLocally(selectedPagePosition, imageUri);
            }
            
            // Upload ảnh lên Cloudinary trong background
            executorService.execute(() -> {
                try {
                    String cloudinaryUrl = cloudinaryService.uploadImage(imageFile);
                    
                    // Sau khi upload thành công, cập nhật UI trên main thread
                    runOnUiThread(() -> {
                        if (isEditingCover) {
                            story.setCoverImageUrl(cloudinaryUrl);
                            isEditingCover = false;
                        } else if (selectedPagePosition != -1) {
                            ManualStory.Page page = story.getPages().get(selectedPagePosition);
                            
                            // Trường hợp đặc biệt: nếu trang đã có bối cảnh và nhân vật
                            if (page.getSetting() != null) {
                                new AlertDialog.Builder(ManualStoryCreatorActivity.this)
                                    .setTitle("Cảnh báo")
                                    .setMessage("Trang này đã có bối cảnh [" + page.getSetting().getName() + "]. Ảnh của trang sẽ được tự động tạo dựa trên bối cảnh. Bạn có muốn thay thế bằng ảnh thủ công này không?")
                                    .setPositiveButton("Đúng vậy, thay thế", (dialog, which) -> {
                                        page.setImageUrl(cloudinaryUrl);
                                        viewModel.updatePage(selectedPagePosition, page);
                                        adapter.notifyItemChanged(selectedPagePosition);
                                        Toast.makeText(ManualStoryCreatorActivity.this, "Đã thay thế ảnh tự động bằng ảnh thủ công", Toast.LENGTH_SHORT).show();
                                    })
                                    .setNegativeButton("Không, giữ ảnh bối cảnh", null)
                                    .show();
                            } else {
                                page.setImageUrl(cloudinaryUrl);
                                viewModel.updatePage(selectedPagePosition, page);
                                adapter.notifyItemChanged(selectedPagePosition);
                                Toast.makeText(ManualStoryCreatorActivity.this, "Đã cập nhật ảnh trang", Toast.LENGTH_SHORT).show();
                            }
                            
                            selectedPagePosition = -1;
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(ManualStoryCreatorActivity.this, 
                            "Lỗi upload ảnh: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
                } finally {
                    // Xóa file tạm sau khi upload
                    imageFile.delete();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private File createImageFileFromUri(Uri uri) throws IOException {
        File destinationFile = createImageFile();
        
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream != null) {
                java.nio.file.Files.copy(
                        inputStream,
                        destinationFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IOException("Không thể sao chép tệp ảnh", e);
        }
        
        return destinationFile;
    }

    private void showAddPageDialog() {        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_page, null);
        builder.setView(dialogView);
        
        EditText etPageContent = dialogView.findViewById(R.id.etPageContent);
        TextInputLayout tilPageContent = dialogView.findViewById(R.id.tilPageContent);
        tilPageContent.setHint("Nội dung trang mới");
        
        // Thêm các nút chọn phần tử cho trang
        Button btnSelectPageElements = dialogView.findViewById(R.id.btnSelectPageElements);
        TextView tvSelectedPageElements = dialogView.findViewById(R.id.tvSelectedPageElements);
        
        // Khởi tạo các phần tử tạm thời cho trang mới
        final StoryElement[] pageSetting = {null};
        final List<StoryElement> pageCharacters = new ArrayList<>();
        final List<StoryElement> pageItems = new ArrayList<>();
        
        btnSelectPageElements.setOnClickListener(v -> {
            // Hiển thị dialog chọn phần tử cho trang
            showElementSelectionDialogForPage(pageSetting[0], pageCharacters, pageItems, 
                (setting, characters, items) -> {
                    // Cập nhật các phần tử đã chọn
                    pageSetting[0] = setting;
                    pageCharacters.clear();
                    pageCharacters.addAll(characters);
                    pageItems.clear();
                    pageItems.addAll(items);
                    
                    // Cập nhật UI hiển thị các phần tử đã chọn
                    updateSelectedPageElementsText(tvSelectedPageElements, 
                        pageSetting[0], pageCharacters, pageItems);
                });
        });
        
        // Thêm thông tin về cách ảnh trang được tạo
        TextView tvPageImageInfo = new TextView(this);
        tvPageImageInfo.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT));
        tvPageImageInfo.setPadding(16, 8, 16, 8);
        tvPageImageInfo.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        tvPageImageInfo.setText("Lưu ý: Ảnh trang sẽ được tự động tạo dựa trên bối cảnh đã chọn.");
        ((LinearLayout) dialogView).addView(tvPageImageInfo, ((LinearLayout) dialogView).getChildCount() - 1); // Thêm vào trước nút cuối cùng
        
        builder.setTitle("Thêm trang mới")
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String content = etPageContent.getText().toString().trim();
                    if (!content.isEmpty()) {
                        // Tạo trang mới với bối cảnh, nhân vật và vật phẩm đã chọn
                        ManualStory.Page newPage = new ManualStory.Page("", content);
                        newPage.setSetting(pageSetting[0]);
                        newPage.setCharacters(pageCharacters);
                        newPage.setItems(pageItems);
                        
                        // Tự động cập nhật ảnh nếu có bối cảnh
                        if (pageSetting[0] != null && pageSetting[0].getImageUrl() != null) {
                            newPage.setImageUrl(pageSetting[0].getImageUrl());
                        }
                        
                        viewModel.addPage(newPage);
                        updatePages();
                        
                        // Hiển thị thông báo về ảnh trang
                        if (pageSetting[0] != null) {
                            Toast.makeText(this, "Ảnh trang đã được tự động cập nhật từ bối cảnh", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Hủy", null);
        
        builder.show();
    }

    private void showEditPageDialog(int position) {
        ManualStory.Page page = story.getPages().get(position);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_page, null);
        builder.setView(dialogView);
        
        EditText etPageContent = dialogView.findViewById(R.id.etPageContent);
        etPageContent.setText(page.getContent());
        
        // Thêm các nút chọn phần tử cho trang
        Button btnSelectPageElements = dialogView.findViewById(R.id.btnSelectPageElements);
        TextView tvSelectedPageElements = dialogView.findViewById(R.id.tvSelectedPageElements);
        
        // Hiển thị các phần tử đã chọn
        updateSelectedPageElementsText(tvSelectedPageElements, 
            page.getSetting(), page.getCharacters(), page.getItems());
        
        // Thêm thông tin về cách ảnh trang được tạo
        TextView tvPageImageInfo = new TextView(this);
        tvPageImageInfo.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT));
        tvPageImageInfo.setPadding(16, 8, 16, 8);
        tvPageImageInfo.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        tvPageImageInfo.setText("Lưu ý: Khi thay đổi bối cảnh, ảnh trang sẽ được tự động cập nhật.");
        ((LinearLayout) dialogView).addView(tvPageImageInfo, ((LinearLayout) dialogView).getChildCount() - 1); // Thêm vào trước nút cuối cùng
        
        // Thêm hiển thị hình ảnh hiện tại của trang nếu có
        if (page.getImageUrl() != null && !page.getImageUrl().isEmpty()) {
            ImageView ivPageImage = new ImageView(this);
            ivPageImage.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 
                    300)); // Chiều cao cố định cho ảnh
            ivPageImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ((LinearLayout) dialogView).addView(ivPageImage, 0); // Thêm vào đầu dialog
            
            // Hiển thị ảnh
            Glide.with(this)
                .load(page.getImageUrl())
                .placeholder(R.drawable.img_placeholder)
                .error(R.drawable.img_placeholder)
                .into(ivPageImage);
        }
        
        btnSelectPageElements.setOnClickListener(v -> {
            // Hiển thị dialog chọn phần tử cho trang
            showElementSelectionDialogForPage(page.getSetting(), page.getCharacters(), page.getItems(), 
                (setting, characters, items) -> {
                    // Cập nhật các phần tử đã chọn
                    page.setSetting(setting);
                    page.setCharacters(characters);
                    page.setItems(items);
                    
                    // Cập nhật UI hiển thị các phần tử đã chọn
                    updateSelectedPageElementsText(tvSelectedPageElements, 
                        setting, characters, items);
                    
                    // Cập nhật ảnh trang nếu có bối cảnh và nhân vật
                    updatePageImageBasedOnElements(position, page);
                });
        });
        
        builder.setTitle("Chỉnh sửa trang")
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String content = etPageContent.getText().toString().trim();
                    if (!content.isEmpty()) {
                        page.setContent(content);
                        viewModel.updatePage(position, page);
                        adapter.notifyItemChanged(position);
                    }
                })
                .setNegativeButton("Hủy", null);
        
        builder.show();
    }
    
    private void updatePageImageBasedOnElements(int position, ManualStory.Page page) {
        // Nếu trang đã có bối cảnh, thì tạo/cập nhật ảnh trang
        if (page.getSetting() != null) {
            // Đầu tiên, sử dụng ảnh của bối cảnh làm ảnh trang
            String imageUrl = page.getSetting().getImageUrl();
            
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Cập nhật ảnh trang (bối cảnh)
                page.setImageUrl(imageUrl);
                
                // Thông báo cho người dùng về việc cập nhật ảnh
                Toast.makeText(this, "Đã cập nhật trang với bối cảnh và nhân vật", Toast.LENGTH_SHORT).show();
                
                // Cập nhật UI
                viewModel.updatePage(position, page);
                adapter.notifyItemChanged(position);
                
                // Log thông tin nhân vật và vật phẩm
                if (page.getCharacters() != null && !page.getCharacters().isEmpty()) {
                    StringBuilder characterInfo = new StringBuilder("Nhân vật: ");
                    for (StoryElement character : page.getCharacters()) {
                        characterInfo.append(character.getName()).append(", ");
                    }
                    Log.d("ManualStory", characterInfo.toString());
                }
                
                if (page.getItems() != null && !page.getItems().isEmpty()) {
                    StringBuilder itemInfo = new StringBuilder("Vật phẩm: ");
                    for (StoryElement item : page.getItems()) {
                        itemInfo.append(item.getName()).append(", ");
                    }
                    Log.d("ManualStory", itemInfo.toString());
                }
            }
        }
    }

    private void showDeleteConfirmDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xóa trang")
                .setMessage("Bạn có chắc muốn xóa trang này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.removePage(position);
                    updatePages();
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    adapter.notifyItemChanged(position);
                });
        
        builder.show();
    }

    private void updatePages() {
        adapter.updatePages(story.getPages());
    }

    private void saveStory() {
        String title = etStoryTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề truyện", Toast.LENGTH_SHORT).show();
            return;
        }
        
        story.setTitle(title);
        
        if (story.getPages().isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm ít nhất một trang", Toast.LENGTH_SHORT).show();
            return;
        }
        
        viewModel.saveStory(story).observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Đã lưu truyện thành công", Toast.LENGTH_SHORT).show();
                // Chuyển đến màn hình đọc truyện
                openStoryReader();
            }
        });
    }

    private void openStoryReader() {
        Intent intent = new Intent(this, ManualStoryReaderActivity.class);
        intent.putExtra("STORY_ID", story.getId());
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manual_story_creator, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_save) {
            saveStory();
            return true;
        } else if (id == R.id.action_preview) {
            // Xem trước trước khi lưu
            if (story.getPages().isEmpty()) {
                Toast.makeText(this, "Vui lòng thêm ít nhất một trang", Toast.LENGTH_SHORT).show();
            } else {
                // Mở màn hình xem trước trang đầu tiên
                showPreviewPageDialog(0);
            }
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Hiển thị hộp thoại xác nhận nếu có thay đổi chưa lưu
        new AlertDialog.Builder(this)
                .setTitle("Thoát")
                .setMessage("Bạn có muốn lưu truyện trước khi thoát?")
                .setPositiveButton("Lưu", (dialog, which) -> saveStory())
                .setNegativeButton("Không lưu", (dialog, which) -> finish())
                .setNeutralButton("Hủy", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            // Kiểm tra xem quyền đã được cấp chưa
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                // Người dùng từ chối cấp quyền
                boolean isPermanentlyDenied = false;
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    isPermanentlyDenied = !shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES);
                } else {
                    isPermanentlyDenied = !shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE);
                }

                if (isPermanentlyDenied) {
                    // Người dùng đã chọn "Không hỏi lại" - hướng dẫn họ đến cài đặt
                    new AlertDialog.Builder(this)
                        .setTitle("Cần quyền truy cập")
                        .setMessage("Ứng dụng cần quyền truy cập để chọn ảnh. Vui lòng cấp quyền trong cài đặt của ứng dụng.")
                        .setPositiveButton("Đi đến Cài đặt", (dialog, which) -> {
                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
                } else {
                    Toast.makeText(this, "Cần cấp quyền để chọn ảnh", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showElementSelectionDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_story_elements_selection, null);
        dialog.setContentView(dialogView);
        
        // Thiết lập RecyclerViews cho nhân vật, bối cảnh và vật phẩm
        RecyclerView rvCharacters = dialogView.findViewById(R.id.rvCharacters);
        RecyclerView rvSettings = dialogView.findViewById(R.id.rvSettings);
        RecyclerView rvItems = dialogView.findViewById(R.id.rvItems);
        
        TextView tvCharacterCount = dialogView.findViewById(R.id.tvCharacterCount);
        TextView tvItemCount = dialogView.findViewById(R.id.tvItemCount);
        
        // Thiết lập LayoutManagers - ĐẢM BẢO PHẢI CÓ LAYOUT MANAGER CHO RECYCLERVIEW
        LinearLayoutManager charactersLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager settingsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager itemsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        
        rvCharacters.setLayoutManager(charactersLayoutManager);
        rvSettings.setLayoutManager(settingsLayoutManager);
        rvItems.setLayoutManager(itemsLayoutManager);
        
        // Debug TextView
        TextView tvDebug = new TextView(this);
        tvDebug.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT));
        tvDebug.setPadding(16, 16, 16, 16);
        tvDebug.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        tvDebug.setText("Đang tải dữ liệu...");
        ((LinearLayout) dialogView).addView(tvDebug, 1); // Thêm vào sau tiêu đề
        
        // Timeout xử lý trường hợp loading mãi không thành công
        new Handler().postDelayed(() -> {
            if (storyElementViewModel.getIsLoading().getValue() != null && 
                storyElementViewModel.getIsLoading().getValue()) {
                tvDebug.setText("Tải dữ liệu quá lâu. Đang cố gắng tải lại...");
                storyElementViewModel.refreshData();
                
                // Timeout thứ hai để đảm bảo UI không bị treo vĩnh viễn
                new Handler().postDelayed(() -> {
                    tvDebug.setText("Đã hoàn thành tải dữ liệu (timeout)");
                    // Không thể trực tiếp gọi setValue() vì nó là protected
                    // Thay vào đó, hiển thị UI trực tiếp dù có loading hay không
                    loadingView.setVisibility(View.GONE);
                }, 5000);
            }
        }, 10000);
        
        // Thiết lập Adapters
        StoryElementAdapter characterAdapter = new StoryElementAdapter(
                (element, isSelected) -> {
                    // Khi chọn/bỏ chọn nhân vật
                    if (isSelected) {
                        selectedCharacters.add(element);
                    } else {
                        selectedCharacters.removeIf(e -> e.getId().equals(element.getId()));
                    }
                    tvCharacterCount.setText(selectedCharacters.size() + "/" + MAX_CHARACTER_SELECTIONS);
                    updateSelectedElementsText();
                }, 
                StoryElement.ElementType.CHARACTER, 
                MAX_CHARACTER_SELECTIONS
        );
        
        StoryElementAdapter settingAdapter = new StoryElementAdapter(
                (element, isSelected) -> {
                    // Khi chọn/bỏ chọn bối cảnh
                    if (isSelected) {
                        // Chỉ cho phép chọn một bối cảnh
                        selectedSetting = element;
                    } else {
                        selectedSetting = null;
                    }
                    updateSelectedElementsText();
                },
                StoryElement.ElementType.SETTING,
                1
        );
        
        StoryElementAdapter itemAdapter = new StoryElementAdapter(
                (element, isSelected) -> {
                    // Khi chọn/bỏ chọn vật phẩm
                    if (isSelected) {
                        selectedItems.add(element);
                    } else {
                        selectedItems.removeIf(e -> e.getId().equals(element.getId()));
                    }
                    tvItemCount.setText(selectedItems.size() + "/" + MAX_ITEM_SELECTIONS);
                    updateSelectedElementsText();
                },
                StoryElement.ElementType.ITEM,
                MAX_ITEM_SELECTIONS
        );
        
        rvCharacters.setAdapter(characterAdapter);
        rvSettings.setAdapter(settingAdapter);
        rvItems.setAdapter(itemAdapter);
        
        // Log dữ liệu ban đầu
        Log.d("ManualStory", "Dialog khởi tạo, trạng thái RecyclerViews: " +
            "Characters: " + (rvCharacters.getAdapter() != null) +
            ", Settings: " + (rvSettings.getAdapter() != null) +
            ", Items: " + (rvItems.getAdapter() != null));
        
        // Quan sát dữ liệu từ ViewModel
        storyElementViewModel.getCharacters().observe(this, characters -> {
            Log.d("ManualStory", "getCharacters gọi lại: số lượng = " + characters.size());
            tvDebug.setText("Đã tải: " + characters.size() + " nhân vật, " + 
                (settingAdapter.getItemCount()) + " bối cảnh, " + 
                (itemAdapter.getItemCount()) + " vật phẩm");
            
            characterAdapter.updateData(characters);
            characterAdapter.notifyDataSetChanged(); // Đảm bảo refresh adapter
            
            // Preselect đã chọn trước đó
            for (StoryElement character : selectedCharacters) {
                for (int i = 0; i < characters.size(); i++) {
                    StoryElement item = characters.get(i);
                    if (item.getId() != null && item.getId().equals(character.getId())) {
                        characterAdapter.toggleSelection(item);
                        break;
                    }
                }
            }
        });
        
        storyElementViewModel.getSettings().observe(this, settings -> {
            Log.d("ManualStory", "getSettings gọi lại: số lượng = " + settings.size());
            tvDebug.setText("Đã tải: " + (characterAdapter.getItemCount()) + " nhân vật, " + 
                settings.size() + " bối cảnh, " + 
                (itemAdapter.getItemCount()) + " vật phẩm");
            
            settingAdapter.updateData(settings);
            settingAdapter.notifyDataSetChanged(); // Đảm bảo refresh adapter
            
            // Preselect đã chọn trước đó
            if (selectedSetting != null) {
                // Đầu tiên xóa tất cả lựa chọn
                settingAdapter.clearSelections();
                // Sau đó chọn setting hiện tại
                for (StoryElement setting : settings) {
                    if (setting.getId() != null && setting.getId().equals(selectedSetting.getId())) {
                        settingAdapter.toggleSelection(setting);
                        break;
                    }
                }
            }
        });
        
        storyElementViewModel.getItems().observe(this, items -> {
            Log.d("ManualStory", "getItems gọi lại: số lượng = " + items.size());
            tvDebug.setText("Đã tải: " + (characterAdapter.getItemCount()) + " nhân vật, " + 
                (settingAdapter.getItemCount()) + " bối cảnh, " + 
                items.size() + " vật phẩm");
            
            itemAdapter.updateData(items);
            itemAdapter.notifyDataSetChanged(); // Đảm bảo refresh adapter
            
            // Preselect đã chọn trước đó
            for (StoryElement item : selectedItems) {
                for (int i = 0; i < items.size(); i++) {
                    StoryElement element = items.get(i);
                    if (element.getId() != null && item.getId() != null && 
                        element.getId().equals(item.getId())) {
                        itemAdapter.toggleSelection(element);
                        break;
                    }
                }
            }
        });
        
        // Thiết lập nút thêm nhân vật, bối cảnh và vật phẩm mới
        Button btnAddCharacter = dialogView.findViewById(R.id.btnAddCharacter);
        Button btnAddSetting = dialogView.findViewById(R.id.btnAddSetting);
        Button btnAddItem = dialogView.findViewById(R.id.btnAddItem);
        
        btnAddCharacter.setOnClickListener(v -> {
            showAddCustomElementDialog(StoryElement.ElementType.CHARACTER);
            dialog.dismiss();
        });
        
        btnAddSetting.setOnClickListener(v -> {
            showAddCustomElementDialog(StoryElement.ElementType.SETTING);
            dialog.dismiss();
        });
        
        btnAddItem.setOnClickListener(v -> {
            showAddCustomElementDialog(StoryElement.ElementType.ITEM);
            dialog.dismiss();
        });
        
        // Thiết lập nút "Hoàn thành"
        Button btnDone = dialogView.findViewById(R.id.btnDone);
        btnDone.setOnClickListener(v -> {
            // Lưu các lựa chọn vào story
            saveElementsToStory();
            dialog.dismiss();
        });
        
        dialog.show();
        
        // Kiểm tra và làm mới dữ liệu nếu cần
        if ((storyElementViewModel.getCharacters().getValue() == null || storyElementViewModel.getCharacters().getValue().isEmpty()) ||
            (storyElementViewModel.getSettings().getValue() == null || storyElementViewModel.getSettings().getValue().isEmpty()) ||
            (storyElementViewModel.getItems().getValue() == null || storyElementViewModel.getItems().getValue().isEmpty())) {
            Log.d("ManualStory", "Dữ liệu chưa được tải, gọi refreshData");
            storyElementViewModel.refreshData();
        } else {
            Log.d("ManualStory", "Dữ liệu đã được tải sẵn, không cần refreshData");
            // Dữ liệu đã tải rồi, cập nhật trạng thái debug
            int characterCount = storyElementViewModel.getCharacters().getValue().size();
            int settingCount = storyElementViewModel.getSettings().getValue().size();
            int itemCount = storyElementViewModel.getItems().getValue().size();
            tvDebug.setText("Đã tải sẵn: " + characterCount + " nhân vật, " + 
                settingCount + " bối cảnh, " + 
                itemCount + " vật phẩm");
        }
    }
    
    private void showAddCustomElementDialog(StoryElement.ElementType type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_custom_element, null);
        builder.setView(dialogView);
        
        EditText etElementName = dialogView.findViewById(R.id.etElementName);
        ImageView ivElementImage = dialogView.findViewById(R.id.ivElementImage);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        
        final Uri[] selectedImageUri = new Uri[1];
        
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
            // Sau khi chọn ảnh, URI sẽ được lưu trong selectedImageUri[0]
            // và hình ảnh sẽ được hiển thị trong ivElementImage
        });
        
        String title = "";
        switch (type) {
            case CHARACTER:
                title = "Thêm nhân vật mới";
                break;
            case SETTING:
                title = "Thêm bối cảnh mới";
                break;
            case ITEM:
                title = "Thêm vật phẩm mới";
                break;
        }
        
        builder.setTitle(title)
               .setPositiveButton("Thêm", (dialog, which) -> {
                   String name = etElementName.getText().toString().trim();
                   if (name.isEmpty()) {
                       Toast.makeText(this, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();
                       return;
                   }
                   
                   if (selectedImageUri[0] == null) {
                       Toast.makeText(this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
                       return;
                   }
                   
                   // Tạo phần tử mới và thêm vào database
                   // Tạm thời lưu URI ảnh cục bộ
                   StoryElement newElement = new StoryElement(name, selectedImageUri[0].toString(), type);
                   addCustomElement(newElement);
               })
               .setNegativeButton("Hủy", null);
        
        builder.show();
    }
    
    private void addCustomElement(StoryElement element) {
        // Đây là phiên bản đơn giản, chỉ thêm vào danh sách local
        // Trong phiên bản thực tế, bạn sẽ lưu vào database
        switch (element.getElementType()) {
            case CHARACTER:
                selectedCharacters.add(element);
                break;
            case SETTING:
                selectedSetting = element;
                break;
            case ITEM:
                selectedItems.add(element);
                break;
        }
        updateSelectedElementsText();
    }
    
    private void saveElementsToStory() {
        // Lưu các phần tử đã chọn vào model ManualStory
        
        // Lưu bối cảnh
        if (selectedSetting != null) {
            story.setSetting(selectedSetting);
        }
        
        // Lưu nhân vật
        story.clearCharacters();
        for (StoryElement character : selectedCharacters) {
            story.addCharacter(character);
        }
        
        // Lưu vật phẩm
        story.clearItems();
        for (StoryElement item : selectedItems) {
            story.addItem(item);
        }
        
        // Cập nhật UI để hiển thị các phần tử đã chọn
        updateSelectedElementsText();
    }
    
    private void updateSelectedElementsText() {
        StringBuilder sb = new StringBuilder();
        
        if (selectedSetting != null) {
            sb.append("Bối cảnh: ").append(selectedSetting.getName()).append("\n");
        }
        
        if (!selectedCharacters.isEmpty()) {
            sb.append("Nhân vật: ");
            for (int i = 0; i < selectedCharacters.size(); i++) {
                sb.append(selectedCharacters.get(i).getName());
                if (i < selectedCharacters.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("\n");
        }
        
        if (!selectedItems.isEmpty()) {
            sb.append("Vật phẩm: ");
            for (int i = 0; i < selectedItems.size(); i++) {
                sb.append(selectedItems.get(i).getName());
                if (i < selectedItems.size() - 1) {
                    sb.append(", ");
                }
            }
        }
        
        String text = sb.toString().trim();
        if (text.isEmpty()) {
            tvSelectedElements.setText("Chưa có phần tử nào được chọn");
        } else {
            tvSelectedElements.setText(text);
        }
    }

    // Thêm interface để xử lý callback chọn phần tử
    public interface ElementsSelectionCallback {
        void onElementsSelected(StoryElement setting, List<StoryElement> characters, List<StoryElement> items);
    }
    
    private void showElementSelectionDialogForPage(StoryElement currentSetting,
                                                List<StoryElement> currentCharacters,
                                                List<StoryElement> currentItems,
                                                ElementsSelectionCallback callback) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_story_elements_selection, null);
        dialog.setContentView(dialogView);
        
        // Thiết lập RecyclerViews cho nhân vật, bối cảnh và vật phẩm
        RecyclerView rvCharacters = dialogView.findViewById(R.id.rvCharacters);
        RecyclerView rvSettings = dialogView.findViewById(R.id.rvSettings);
        RecyclerView rvItems = dialogView.findViewById(R.id.rvItems);
        
        TextView tvCharacterCount = dialogView.findViewById(R.id.tvCharacterCount);
        TextView tvItemCount = dialogView.findViewById(R.id.tvItemCount);
        
        // Thiết lập LayoutManagers
        LinearLayoutManager charactersLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager settingsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager itemsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        
        rvCharacters.setLayoutManager(charactersLayoutManager);
        rvSettings.setLayoutManager(settingsLayoutManager);
        rvItems.setLayoutManager(itemsLayoutManager);
        
        // Tạo các danh sách tạm thời cho phần tử đã chọn
        final StoryElement[] selectedPageSetting = {currentSetting};
        final List<StoryElement> selectedPageCharacters = new ArrayList<>(currentCharacters != null ? currentCharacters : new ArrayList<>());
        final List<StoryElement> selectedPageItems = new ArrayList<>(currentItems != null ? currentItems : new ArrayList<>());
        
        // Cập nhật các TextView hiển thị số lượng đã chọn
        tvCharacterCount.setText(selectedPageCharacters.size() + "/" + MAX_CHARACTER_SELECTIONS);
        tvItemCount.setText(selectedPageItems.size() + "/" + MAX_ITEM_SELECTIONS);
        
        // Debug TextView
        TextView tvDebug = new TextView(this);
        tvDebug.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT));
        tvDebug.setPadding(16, 16, 16, 16);
        tvDebug.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        tvDebug.setText("Đang tải dữ liệu...");
        ((LinearLayout) dialogView).addView(tvDebug, 1);
        
        // Thiết lập Adapters
        StoryElementAdapter characterAdapter = new StoryElementAdapter(
                (element, isSelected) -> {
                    // Khi chọn/bỏ chọn nhân vật
                    if (isSelected) {
                        selectedPageCharacters.add(element);
                    } else {
                        selectedPageCharacters.removeIf(e -> e.getId().equals(element.getId()));
                    }
                    tvCharacterCount.setText(selectedPageCharacters.size() + "/" + MAX_CHARACTER_SELECTIONS);
                }, 
                StoryElement.ElementType.CHARACTER, 
                MAX_CHARACTER_SELECTIONS
        );
        
        StoryElementAdapter settingAdapter = new StoryElementAdapter(
                (element, isSelected) -> {
                    // Khi chọn/bỏ chọn bối cảnh
                    if (isSelected) {
                        // Chỉ cho phép chọn một bối cảnh
                        selectedPageSetting[0] = element;
                    } else {
                        selectedPageSetting[0] = null;
                    }
                },
                StoryElement.ElementType.SETTING,
                1
        );
        
        StoryElementAdapter itemAdapter = new StoryElementAdapter(
                (element, isSelected) -> {
                    // Khi chọn/bỏ chọn vật phẩm
                    if (isSelected) {
                        selectedPageItems.add(element);
                    } else {
                        selectedPageItems.removeIf(e -> e.getId().equals(element.getId()));
                    }
                    tvItemCount.setText(selectedPageItems.size() + "/" + MAX_ITEM_SELECTIONS);
                },
                StoryElement.ElementType.ITEM,
                MAX_ITEM_SELECTIONS
        );
        
        rvCharacters.setAdapter(characterAdapter);
        rvSettings.setAdapter(settingAdapter);
        rvItems.setAdapter(itemAdapter);
        
        // Quan sát dữ liệu từ ViewModel
        storyElementViewModel.getCharacters().observe(this, characters -> {
            tvDebug.setText("Đã tải: " + characters.size() + " nhân vật, " + 
                (settingAdapter.getItemCount()) + " bối cảnh, " + 
                (itemAdapter.getItemCount()) + " vật phẩm");
            
            characterAdapter.updateData(characters);
            
            // Preselect các phần tử đã chọn trước đó
            if (currentCharacters != null) {
                for (StoryElement character : currentCharacters) {
                    for (int i = 0; i < characters.size(); i++) {
                        StoryElement item = characters.get(i);
                        if (item.getId() != null && character.getId() != null && 
                            item.getId().equals(character.getId())) {
                            characterAdapter.toggleSelection(item);
                            break;
                        }
                    }
                }
            }
        });
        
        storyElementViewModel.getSettings().observe(this, settings -> {
            tvDebug.setText("Đã tải: " + (characterAdapter.getItemCount()) + " nhân vật, " + 
                settings.size() + " bối cảnh, " + 
                (itemAdapter.getItemCount()) + " vật phẩm");
            
            settingAdapter.updateData(settings);
            
            // Preselect bối cảnh đã chọn trước đó
            if (currentSetting != null) {
                settingAdapter.clearSelections();
                for (StoryElement setting : settings) {
                    if (setting.getId() != null && currentSetting.getId() != null && 
                        setting.getId().equals(currentSetting.getId())) {
                        settingAdapter.toggleSelection(setting);
                        break;
                    }
                }
            }
        });
        
        storyElementViewModel.getItems().observe(this, items -> {
            tvDebug.setText("Đã tải: " + (characterAdapter.getItemCount()) + " nhân vật, " + 
                (settingAdapter.getItemCount()) + " bối cảnh, " + 
                items.size() + " vật phẩm");
            
            itemAdapter.updateData(items);
            
            // Preselect các vật phẩm đã chọn trước đó
            if (currentItems != null) {
                for (StoryElement item : currentItems) {
                    for (int i = 0; i < items.size(); i++) {
                        StoryElement element = items.get(i);
                        if (element.getId() != null && item.getId() != null && 
                            element.getId().equals(item.getId())) {
                            itemAdapter.toggleSelection(element);
                            break;
                        }
                    }
                }
            }
        });
        
        // Thiết lập nút "Hoàn thành"
        Button btnDone = dialogView.findViewById(R.id.btnDone);
        btnDone.setOnClickListener(v -> {
            // Gọi callback với các phần tử đã chọn
            callback.onElementsSelected(selectedPageSetting[0], selectedPageCharacters, selectedPageItems);
            dialog.dismiss();
        });
        
        dialog.show();
        
        // Làm mới dữ liệu nếu cần
        if ((storyElementViewModel.getCharacters().getValue() == null || storyElementViewModel.getCharacters().getValue().isEmpty()) ||
            (storyElementViewModel.getSettings().getValue() == null || storyElementViewModel.getSettings().getValue().isEmpty()) ||
            (storyElementViewModel.getItems().getValue() == null || storyElementViewModel.getItems().getValue().isEmpty())) {
            storyElementViewModel.refreshData();
        } else {
            // Dữ liệu đã tải rồi, cập nhật trạng thái debug
            int characterCount = storyElementViewModel.getCharacters().getValue().size();
            int settingCount = storyElementViewModel.getSettings().getValue().size();
            int itemCount = storyElementViewModel.getItems().getValue().size();
            tvDebug.setText("Đã tải sẵn: " + characterCount + " nhân vật, " + 
                settingCount + " bối cảnh, " + 
                itemCount + " vật phẩm");
        }
    }
    
    private void updateSelectedPageElementsText(TextView textView, 
                                              StoryElement setting,
                                              List<StoryElement> characters,
                                              List<StoryElement> items) {
        StringBuilder sb = new StringBuilder();
        
        if (setting != null) {
            sb.append("Bối cảnh: ").append(setting.getName()).append("\n");
        }
        
        if (characters != null && !characters.isEmpty()) {
            sb.append("Nhân vật: ");
            for (int i = 0; i < characters.size(); i++) {
                sb.append(characters.get(i).getName());
                if (i < characters.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("\n");
        }
        
        if (items != null && !items.isEmpty()) {
            sb.append("Vật phẩm: ");
            for (int i = 0; i < items.size(); i++) {
                sb.append(items.get(i).getName());
                if (i < items.size() - 1) {
                    sb.append(", ");
                }
            }
        }
        
        String text = sb.toString().trim();
        if (text.isEmpty()) {
            textView.setText("Chưa có phần tử nào được chọn");
        } else {
            textView.setText(text);
        }
    }

    private void showPreviewPageDialog(int position) {
        ManualStory.Page page = story.getPages().get(position);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_preview_page, null);
        builder.setView(dialogView);
        
        // Ánh xạ các view
        ImageView ivSettingBackground = dialogView.findViewById(R.id.ivSettingBackground);
        LinearLayout charactersContainer = dialogView.findViewById(R.id.charactersContainer);
        LinearLayout itemsContainer = dialogView.findViewById(R.id.itemsContainer);
        TextView tvEmptyPreview = dialogView.findViewById(R.id.tvEmptyPreview);
        TextView tvPageContent = dialogView.findViewById(R.id.tvPageContent);
        TextView tvElementsPreview = dialogView.findViewById(R.id.tvElementsPreview);
        TextView tvPreviewTitle = dialogView.findViewById(R.id.tvPreviewTitle);
        Button btnClosePreview = dialogView.findViewById(R.id.btnClosePreview);
        
        // Thiết lập tiêu đề
        tvPreviewTitle.setText("Xem trước trang " + (position + 1));
        
        // Thiết lập nội dung trang
        tvPageContent.setText(page.getContent());
        
        // Xóa các view cũ
        charactersContainer.removeAllViews();
        itemsContainer.removeAllViews();
        
        // Kiểm tra có bối cảnh không
        if (page.getSetting() != null && page.getSetting().getImageUrl() != null) {
            tvEmptyPreview.setVisibility(View.GONE);
            
            // Hiển thị ảnh bối cảnh
            Glide.with(this)
                .load(page.getSetting().getImageUrl())
                .placeholder(R.drawable.img_placeholder)
                .error(R.drawable.img_placeholder)
                .into(ivSettingBackground);
            
            // Thêm nhân vật vào container
            if (page.getCharacters() != null && !page.getCharacters().isEmpty()) {
                // Giới hạn số lượng nhân vật hiển thị (tối đa 3)
                int numCharsToShow = Math.min(page.getCharacters().size(), 3);
                
                for (int i = 0; i < numCharsToShow; i++) {
                    StoryElement character = page.getCharacters().get(i);
                    if (character.getImageUrl() != null && !character.getImageUrl().isEmpty()) {
                        // Tạo ImageView cho mỗi nhân vật
                        ImageView characterImage = new ImageView(this);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                180); // Chiều cao cố định cho nhân vật
                        params.setMargins(16, 0, 16, 0); // Margin giữa các nhân vật
                        characterImage.setLayoutParams(params);
                        characterImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        
                        // Load ảnh nhân vật
                        Glide.with(this)
                            .load(character.getImageUrl())
                            .into(characterImage);
                        
                        // Thêm vào container
                        charactersContainer.addView(characterImage);
                    }
                }
            }
            
            // Thêm vật phẩm vào container
            if (page.getItems() != null && !page.getItems().isEmpty()) {
                // Giới hạn số lượng vật phẩm hiển thị (tối đa 2)
                int numItemsToShow = Math.min(page.getItems().size(), 2);
                
                for (int i = 0; i < numItemsToShow; i++) {
                    StoryElement item = page.getItems().get(i);
                    if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                        // Tạo ImageView cho mỗi vật phẩm
                        ImageView itemImage = new ImageView(this);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                140, 140); // Kích thước cố định cho vật phẩm
                        params.setMargins(8, 0, 8, 0); // Margin giữa các vật phẩm
                        itemImage.setLayoutParams(params);
                        itemImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        
                        // Load ảnh vật phẩm
                        Glide.with(this)
                            .load(item.getImageUrl())
                            .into(itemImage);
                        
                        // Thêm vào container
                        itemsContainer.addView(itemImage);
                    }
                }
            }
        } else {
            tvEmptyPreview.setVisibility(View.VISIBLE);
            tvEmptyPreview.setText("Trang chưa có bối cảnh");
        }
        
        // Hiển thị hoặc ẩn TextView các phần tử trang
        StringBuilder elementsText = new StringBuilder();
        
        // Thêm thông tin về bối cảnh
        if (page.getSetting() != null) {
            elementsText.append("Bối cảnh: ").append(page.getSetting().getName());
        }
        
        // Thêm thông tin về nhân vật
        if (page.getCharacters() != null && !page.getCharacters().isEmpty()) {
            if (elementsText.length() > 0) {
                elementsText.append("\n");
            }
            elementsText.append("Nhân vật: ");
            for (int i = 0; i < page.getCharacters().size(); i++) {
                elementsText.append(page.getCharacters().get(i).getName());
                if (i < page.getCharacters().size() - 1) {
                    elementsText.append(", ");
                }
            }
        }
        
        // Thêm thông tin về vật phẩm
        if (page.getItems() != null && !page.getItems().isEmpty()) {
            if (elementsText.length() > 0) {
                elementsText.append("\n");
            }
            elementsText.append("Vật phẩm: ");
            for (int i = 0; i < page.getItems().size(); i++) {
                elementsText.append(page.getItems().get(i).getName());
                if (i < page.getItems().size() - 1) {
                    elementsText.append(", ");
                }
            }
        }
        
        // Hiển thị hoặc ẩn TextView các phần tử trang
        if (elementsText.length() > 0) {
            tvElementsPreview.setVisibility(View.VISIBLE);
            tvElementsPreview.setText(elementsText.toString());
        } else {
            tvElementsPreview.setVisibility(View.GONE);
        }
        
        // Thiết lập sự kiện cho nút đóng
        btnClosePreview.setOnClickListener(v -> {
            builder.create().dismiss();
        });
        
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
} 