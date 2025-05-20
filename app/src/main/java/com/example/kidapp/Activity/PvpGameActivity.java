package com.example.kidapp.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.gridlayout.widget.GridLayout;
import androidx.lifecycle.ViewModelProvider;

import com.example.kidapp.DB.FirebaseInitializer;
import com.example.kidapp.DB.PvpRoomManager;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.PvpViewModel;
import com.example.kidapp.ViewModel.UserViewModel;
import com.example.kidapp.models.PvpRoom;
import com.example.kidapp.models.FlipCard;
import com.example.kidapp.models.FlipCardLevel;
import com.example.kidapp.ViewModel.FlipCardLevelViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class PvpGameActivity extends AppCompatActivity {
    private static final String TAG = "PvpGameActivity";
    private static final int GAME_DURATION_SECONDS = 60;
    
    // Thêm biến theo dõi điểm số trực tiếp
    private int hostScore = 0;
    private int guestScore = 0;
    
    // UI Components
    private TextView tvPlayer1Name, tvPlayer2Name;
    private TextView tvPlayer1Score, tvPlayer2Score;
    private TextView tvTimeRemaining, tvGameName, tvGameLevel;
    private FrameLayout gameContentFrame, gameOverlayFrame;
    private MaterialButton btnGameAction, btnExit;
    private ProgressBar gameProgressBar;
    private ImageView ivGameResultIcon;
    private TextView tvGameResultTitle, tvGameResultMessage;
    private MaterialButton btnGameResultAction;
    private LayoutInflater inflater;
    
    // Game specific UI components
    private GridLayout gridCards;
    private List<CardView> cards = new ArrayList<>();
    private CardView firstCard, secondCard;
    private int firstCardPosition = -1, secondCardPosition = -1;
    
    // Game state
    private PvpViewModel pvpViewModel;
    private UserViewModel userViewModel;
    private PvpRoom currentRoom;
    private String currentUserId;
    private String gameType;
    private boolean isHost = false;
    private boolean gameStarted = false;
    private boolean playerTurn = false;
    private int myScore = 0;
    private int opponentScore = 0;
    private CountDownTimer gameTimer;
    private int remainingTime = GAME_DURATION_SECONDS;
    private boolean allowCardFlip = false;
    
    // Card image resources for FlipCard game
    private final int[] cardImages = {
            R.drawable.ic_arrow_back, // Placeholder - replace with actual drawable resources
            R.drawable.ic_add,        // Placeholder - replace with actual drawable resources
            R.drawable.panda_icon,    // Placeholder - replace with actual drawable resources
            R.drawable.ic_arrow_back, // Placeholder - replace with actual drawable resources
            R.drawable.ic_add,        // Placeholder - replace with actual drawable resources
            R.drawable.panda_icon,    // Placeholder - replace with actual drawable resources
    };
    
    // ViewModel cho FlipCard
    private FlipCardLevelViewModel flipCardLevelViewModel;
    
    // Dữ liệu thẻ từ Firestore
    private FlipCardLevel currentFlipCardLevel;
    private List<FlipCard> flipCardList;
    
    // Mảng chứa các cặp giá trị thẻ (thay thế cardValues)
    private Integer[] pairValues;
    
    // Thêm biến đếm ngược
    private TextView tvCountdown;
    private FrameLayout countdownOverlay;
    private CountDownTimer gameStartCountdownTimer;
    private boolean isCountingDown = false;
    private boolean gameActuallyStarted = false;
    
    // ValueEventListener để theo dõi thay đổi điểm số theo thời gian thực
    private com.google.firebase.database.ValueEventListener scoreListener;
    
    // Biến để theo dõi trạng thái tải dữ liệu thẻ
    private boolean isLoadingCardData = false;
    private androidx.lifecycle.Observer<List<FlipCardLevel>> flipCardLevelObserver = null;
    
    private Timer gameStateCheckTimer;
    
    // Thêm biến điểm max
    private int maxScore = -1;
    
    // Thêm các biến cho game làm toán
    private TextView tvMathQuestion;
    private Button btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4;
    private int currentAnswer;
    private int questionCount = 0;
    private static final int MAX_QUESTIONS = 10;
    private Random random = new Random();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pvp_game);
        
        Log.d(TAG, "PvpGameActivity onCreate started");
        
        // Get room ID from intent
        String roomId = getIntent().getStringExtra("ROOM_ID");
        if (roomId == null || roomId.isEmpty()) {
            Log.e(TAG, "Room ID not provided");
            Toast.makeText(this, "Không tìm thấy thông tin phòng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        Log.d(TAG, "Room ID from intent: " + roomId);
        
        // Kiểm tra xem có cần bắt đầu game ngay lập tức không
        boolean startImmediately = getIntent().getBooleanExtra("GAME_START_IMMEDIATELY", false);
        Log.d(TAG, "Start game immediately: " + startImmediately);
        
        // QUAN TRỌNG: Đảm bảo phòng không bị xóa
        PvpRoomManager.getInstance().setPreventRoomDeletion(roomId, true);
        PvpRoomManager.getInstance().registerRoomUsage(roomId);
        Log.d(TAG, "Registered room usage and prevent deletion for room: " + roomId);
        
        // Check user login
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not logged in");
            Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        currentUserId = currentUser.getUid();
        Log.d(TAG, "Current user ID: " + currentUserId);
        
        // Initialize LayoutInflater
        inflater = LayoutInflater.from(this);
        
        // Initialize views
        initViews();
        
        // Set up ViewModel
        pvpViewModel = new ViewModelProvider(this).get(PvpViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        pvpViewModel.setUserViewModel(userViewModel);
        
        // Khởi tạo FlipCardLevelViewModel để tải dữ liệu thẻ
        flipCardLevelViewModel = new ViewModelProvider(this).get(FlipCardLevelViewModel.class);
        
        Log.d(TAG, "ViewModels initialized");
        
        // Tải dữ liệu phòng từ Firebase
        loadRoomDataDirectly(roomId);
        
        // Observe room data
        pvpViewModel.getCurrentRoom().observe(this, room -> {
            if (room == null) {
                Log.e(TAG, "Room data is null from observer");
                // Thay vì kết thúc activity ngay lập tức, hiển thị thông báo và đợi người dùng
                Toast.makeText(this, "Phòng không còn tồn tại hoặc bị đóng bởi chủ phòng", Toast.LENGTH_LONG).show();
                // Đợi một chút để người dùng đọc thông báo
                new Handler().postDelayed(() -> {
                    finish();
                }, 2000);
                return;
            }
            
            Log.d(TAG, "Room data updated: " + room.getRoomId() + ", Status: " + room.getStatus());
            Log.d(TAG, "Host: " + room.getHostId() + " (" + room.getHostName() + ")");
            Log.d(TAG, "Guest: " + room.getGuestId() + " (" + (room.getGuestName() != null ? room.getGuestName() : "null") + ")");
            Log.d(TAG, "ExtraData: " + (room.getExtraData() != null ? room.getExtraData().toString() : "null"));
            
            currentRoom = room;
            gameType = room.getGameType();
            
            // Determine if current user is host
            isHost = room.getHostId().equals(currentUserId);
            Log.d(TAG, "Current user is host: " + isHost);
            
            // Update UI with room information
            updateUI();
            
            // Check game state
            checkGameState();
            
            // Nếu cần bắt đầu ngay lập tức và đây là lần đầu nhận dữ liệu phòng
            if (startImmediately && !gameStarted) {
                Log.d(TAG, "Starting game immediately as requested");
                gameStarted = true;
                btnGameAction.setVisibility(View.GONE);
                
                // Đảm bảo một chút thời gian để UI cập nhật
                new Handler().postDelayed(() -> {
                    Log.d(TAG, "Delayed start of countdown");
                    startCountdownBeforeGame();
                }, 500);
            }
        });
        
        // Set up buttons
        btnGameAction.setOnClickListener(v -> {
            if (!gameStarted) {
                // Start game
                Log.d(TAG, "Start game button clicked");
                startGame();
            } else {
                // Action depends on game type
                // For now, just a placeholder
                Log.d(TAG, "Game action button clicked when game already started");
                Toast.makeText(this, "Game đã bắt đầu!", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnExit.setOnClickListener(v -> {
            Log.d(TAG, "Exit button clicked");
            showExitConfirmDialog();
        });
        
        Log.d(TAG, "PvpGameActivity onCreate completed");
    }
    
    private void initViews() {
        // Main UI components
        tvPlayer1Name = findViewById(R.id.tvPlayer1Name);
        tvPlayer2Name = findViewById(R.id.tvPlayer2Name);
        tvPlayer1Score = findViewById(R.id.tvPlayer1Score);
        tvPlayer2Score = findViewById(R.id.tvPlayer2Score);
        tvTimeRemaining = findViewById(R.id.tvTimeRemaining);
        tvGameName = findViewById(R.id.tvGameName);
        tvGameLevel = findViewById(R.id.tvGameLevel);
        gameContentFrame = findViewById(R.id.gameContentFrame);
        gameOverlayFrame = findViewById(R.id.gameOverlayFrame);
        btnGameAction = findViewById(R.id.btnGameAction);
        btnExit = findViewById(R.id.btnExit);
        gameProgressBar = findViewById(R.id.gameProgressBar);
        
        // Result overlay components
        ivGameResultIcon = findViewById(R.id.ivGameResultIcon);
        tvGameResultTitle = findViewById(R.id.tvGameResultTitle);
        tvGameResultMessage = findViewById(R.id.tvGameResultMessage);
        btnGameResultAction = findViewById(R.id.btnGameResultAction);
        
        // Thêm overlay đếm ngược
        createCountdownOverlay();
        
        // Set initial values
        tvTimeRemaining.setText(String.valueOf(GAME_DURATION_SECONDS));
        gameProgressBar.setVisibility(View.VISIBLE);
        
        Log.d(TAG, "Views initialized");
    }
    
    private void createCountdownOverlay() {
        // Tạo overlay đếm ngược
        countdownOverlay = new FrameLayout(this);
        countdownOverlay.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        countdownOverlay.setBackgroundColor(getResources().getColor(android.R.color.black, null));
        countdownOverlay.getBackground().setAlpha(150); // Semi-transparent
        
        // Tạo TextView đếm ngược
        tvCountdown = new TextView(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = android.view.Gravity.CENTER;
        tvCountdown.setLayoutParams(layoutParams);
        tvCountdown.setTextSize(72);
        tvCountdown.setTextColor(getResources().getColor(android.R.color.white, null));
        tvCountdown.setText("3");
        
        // Thêm vào overlay
        countdownOverlay.addView(tvCountdown);
        
        // Ẩn overlay ban đầu
        countdownOverlay.setVisibility(View.GONE);
        
        // Thêm vào layout chính
        ViewGroup rootView = findViewById(android.R.id.content);
        rootView.addView(countdownOverlay);
        
        Log.d(TAG, "Countdown overlay created");
    }
    
    private void startCountdownBeforeGame() {
        if (isCountingDown) {
            Log.d(TAG, "Already counting down, ignoring duplicate start request");
            return;
        }
        
        Log.d(TAG, "Starting countdown before game");
        isCountingDown = true;
        
        // Hiển thị overlay đếm ngược
        countdownOverlay.setVisibility(View.VISIBLE);
        
        // Cập nhật trạng thái đếm ngược trên Firebase (chỉ host)
        // Chỉ host cập nhật dữ liệu lên Firebase, guest sẽ nhận và hiển thị khi nhận được thông báo
        if (isHost) {
            Map<String, Object> countdownUpdate = new HashMap<>();
            countdownUpdate.put("countdownStarted", true);
            countdownUpdate.put("countdownStartTime", System.currentTimeMillis());
            countdownUpdate.put("gameStarted", true);
            updateRoomDataToFirebase(currentRoom.getRoomId(), countdownUpdate);
            
            Log.d(TAG, "Updated countdown state to Firebase (host)");
        }
        
        // Đảm bảo rằng gameStarted = true cho cả host và guest
        gameStarted = true;
        btnGameAction.setVisibility(View.GONE);
        
        // Log thêm để debug
        Log.d(TAG, "Countdown overlay showing, countdownStarted = " + isCountingDown);
        Log.d(TAG, "User role: " + (isHost ? "HOST" : "GUEST") + " is starting countdown");
        
        // Bắt đầu đếm ngược
        gameStartCountdownTimer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000) + 1;
                tvCountdown.setText(String.valueOf(secondsLeft));
                Log.d(TAG, "Countdown: " + secondsLeft + " role: " + (isHost ? "HOST" : "GUEST"));
            }
            
            @Override
            public void onFinish() {
                // Hiển thị "Start!"
                tvCountdown.setText("Bắt đầu!");
                Log.d(TAG, "Countdown finished, showing 'Start!'");
                
                // Ẩn overlay sau 0.5 giây
                new Handler().postDelayed(() -> {
                    countdownOverlay.setVisibility(View.GONE);
                    isCountingDown = false;
                    
                    // Thực sự bắt đầu game
                    startGameActually();
                    
                    Log.d(TAG, "Countdown overlay hidden, game actually started");
                }, 500);
            }
        }.start();
    }
    
    private void startGameActually() {
        if (gameActuallyStarted) {
            Log.d(TAG, "Game already actually started, ignoring duplicate start");
            return;
        }
        
        Log.d(TAG, "Actually starting game now");
        gameActuallyStarted = true;
        
        // Khởi tạo điểm về 0
        hostScore = 0;
        guestScore = 0;
        
        // Cập nhật trạng thái game thực sự đã bắt đầu
        // Chỉ host cập nhật lên Firebase
        if (isHost) {
            Map<String, Object> gameStartedUpdate = new HashMap<>();
            gameStartedUpdate.put("gameActuallyStarted", true);
            gameStartedUpdate.put("startTime", System.currentTimeMillis());
            gameStartedUpdate.put("remainingTime", GAME_DURATION_SECONDS);
            gameStartedUpdate.put("hostScore", 0);
            gameStartedUpdate.put("guestScore", 0);
            
            Log.d(TAG, "Host updating actual game start state to Firebase");
            updateRoomDataToFirebase(currentRoom.getRoomId(), gameStartedUpdate);
        }
        
        // Ghi log thông tin vai trò khi bắt đầu
        Log.d(TAG, "User role: " + (isHost ? "HOST" : "GUEST") + " starting game actually");
        
        // Bật tính năng tương tác game cho cả host và guest
        allowCardFlip = true;
        
        // Đồng bộ điểm số ban đầu từ Firebase
        syncScoresFromFirebase();
        
        // Thiết lập lắng nghe thay đổi điểm số từ Firebase (hàm dùng ValueEventListener)
        setupScoreListener();
        
        // Bắt đầu bộ đếm thời gian game
        startGameTimer(GAME_DURATION_SECONDS);
        
        // Bắt đầu timer kiểm tra trạng thái game
        startGameStateCheckTimer();
        
        Log.d(TAG, "Game actually started - cards enabled, timer started");
        Log.d(TAG, "User " + (isHost ? "HOST" : "GUEST") + " can now interact with cards, allowCardFlip = " + allowCardFlip);
    }
    
    private void startGameStateCheckTimer() {
        if (gameStateCheckTimer != null) {
            gameStateCheckTimer.cancel();
        }
        
        gameStateCheckTimer = new Timer();
        gameStateCheckTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (currentRoom != null && gameActuallyStarted) {
                        checkGameState();
                    }
                });
            }
        }, 0, 1000); // Kiểm tra mỗi giây
    }
    
    private void setupScoreListener() {
        if (currentRoom == null) return;
        
        // Nếu đã có listener, hủy listener cũ
        if (scoreListener != null) {
            FirebaseInitializer.getPvpRoomsRef().child(currentRoom.getRoomId()).removeEventListener(scoreListener);
        }
        
        // Tạo listener mới
        scoreListener = new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                // Đọc điểm từ Firebase
                if (dataSnapshot.child("hostScore").exists()) {
                    Long value = dataSnapshot.child("hostScore").getValue(Long.class);
                    if (value != null) {
                        hostScore = value.intValue();
                    }
                }
                
                if (dataSnapshot.child("guestScore").exists()) {
                    Long value = dataSnapshot.child("guestScore").getValue(Long.class);
                    if (value != null) {
                        guestScore = value.intValue();
                    }
                }
                
                Log.d(TAG, "Lắng nghe thay đổi điểm từ Firebase - Host: " + hostScore + ", Guest: " + guestScore);
                
                // Cập nhật UI
                updateScoreUI();
            }
            
            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Log.e(TAG, "Lỗi lắng nghe điểm từ Firebase", databaseError.toException());
            }
        };
        
        // Đăng ký listener
        FirebaseInitializer.getPvpRoomsRef().child(currentRoom.getRoomId()).addValueEventListener(scoreListener);
        Log.d(TAG, "Đã thiết lập lắng nghe thay đổi điểm số");
    }
    
    private void updateUI() {
        if (currentRoom == null) return;
        
        // Update player names - cố định vị trí tên người chơi
        tvPlayer1Name.setText(currentRoom.getHostName()); // Bên trái luôn là tên host
        tvPlayer2Name.setText(currentRoom.getGuestName() != null ? 
                              currentRoom.getGuestName() : "Đang chờ..."); // Bên phải luôn là tên guest
        
        // Update game info
        tvGameName.setText(getGameNameFromType(currentRoom.getGameType()));
        tvGameLevel.setText("Độ khó: " + getGameLevelName(currentRoom.getGameId()));
        
        // Update scores
        updateScoreUI();
        
        // Set appropriate game content based on game type
        setGameContent(currentRoom.getGameType());
        
        // Hide progress after UI is updated
        gameProgressBar.setVisibility(View.GONE);
    }
    
    private String getGameNameFromType(String gameType) {
        if (gameType == null) return "Trò chơi";
        
        switch (gameType) {
            case "FLIP_CARD": return "Lật Thẻ";
            case "PUZZLE": return "Ghép Hình";
            case "GUESS_WORD": return "Đoán Từ";
            case "MATH": return "Toán Học";
            default: return "Trò chơi";
        }
    }
    
    private String getGameLevelName(String gameId) {
        if (gameId == null) return "Dễ";
        
        switch (gameId.toLowerCase()) {
            case "dễ": case "de": case "easy": return "Dễ";
            case "trung bình": case "trungbinh": case "medium": return "Trung bình";
            case "khó": case "kho": case "hard": return "Khó";
            default: return "Dễ";
        }
    }
    
    private void setGameContent(String gameType) {
        // Clear previous content
        gameContentFrame.removeAllViews();
        
        // Inflate and add appropriate game content based on game type
        View gameContent = null;
        
        switch (gameType) {
            case "FLIP_CARD":
                gameContent = inflater.inflate(R.layout.game_content_flip_card, gameContentFrame, false);
                if (!gameActuallyStarted) {
                    // Tải dữ liệu thẻ từ Firestore trước khi bắt đầu game
                    loadFlipCardData();
                } else {
                    // Nếu game đã bắt đầu, sử dụng dữ liệu đã có
                    initFlipCardGame(gameContent);
                }
                break;
            case "PUZZLE":
                // TODO: Add puzzle game content
                break;
            case "GUESS_WORD":
                // TODO: Add guess word game content
                break;
            case "MATH":
                gameContent = inflater.inflate(R.layout.game_content_math, gameContentFrame, false);
                initMathGame(gameContent);
                break;
            default:
                // Default to flip card
                gameContent = inflater.inflate(R.layout.game_content_flip_card, gameContentFrame, false);
                initFlipCardGame(gameContent);
                break;
        }
        
        if (gameContent != null) {
            gameContentFrame.addView(gameContent);
        }
    }
    
    // Phương thức mới để tải dữ liệu thẻ từ Firestore
    private void loadFlipCardData() {
        if (isLoadingCardData) {
            Log.d(TAG, "Already loading card data, ignoring duplicate request");
            return;
        }
        
        Log.d(TAG, "Loading flip card data from Firestore");
        isLoadingCardData = true;
        
        // Tạo observer chỉ khi cần
        if (flipCardLevelObserver == null) {
            flipCardLevelObserver = levels -> {
                isLoadingCardData = false;
                
                if (levels != null && !levels.isEmpty()) {
                    // Chọn một level ngẫu nhiên
                    Random random = new Random();
                    FlipCardLevel randomLevel = levels.get(random.nextInt(levels.size()));
                    
                    currentFlipCardLevel = randomLevel;
                    flipCardList = randomLevel.getCards();
                    
                    Log.d(TAG, "Loaded flip card level: " + randomLevel.getTopic() +
                          " with " + (flipCardList != null ? flipCardList.size() : 0) + " cards");
                    
                    // Tìm gameContent đã tạo
                    View gameContent = gameContentFrame.getChildAt(0);
                    if (gameContent != null) {
                        initFlipCardGame(gameContent);
                    } else {
                        Log.e(TAG, "Game content view not found");
                    }
                } else {
                    Log.e(TAG, "Failed to load flip card data or no levels available");
                    // Fallback to default images if data loading fails
                    flipCardList = null;
                    View gameContent = gameContentFrame.getChildAt(0);
                    if (gameContent != null) {
                        initFlipCardGame(gameContent);
                    }
                }
                
                // Hủy đăng ký observer sau khi xử lý xong
                flipCardLevelViewModel.getAllLevels().removeObserver(flipCardLevelObserver);
            };
        }
        
        // Đăng ký observer
        flipCardLevelViewModel.getAllLevels().observe(this, flipCardLevelObserver);
    }
    
    private void initFlipCardGame(View gameContent) {
        try {
            Log.d(TAG, "Initializing flip card game");
            
            gridCards = gameContent.findViewById(R.id.gridCards);
            if (gridCards == null) {
                Log.e(TAG, "gridCards view not found");
                Toast.makeText(this, "Lỗi khởi tạo game: không tìm thấy gridCards", Toast.LENGTH_SHORT).show();
                return;
            }
            
            gridCards.removeAllViews();
            cards.clear();
            
            // Tạo danh sách các cặp thẻ theo logic của GameLatTheActivity
            List<CardPair> cardPairs = new ArrayList<>();
            
            // Check if we have Firestore data available
            if (flipCardList != null && !flipCardList.isEmpty()) {
                Log.d(TAG, "Using Firestore card data with " + flipCardList.size() + " cards");
                
                // Tạo các cặp thẻ theo logic GameLatTheActivity
                for (int i = 0; i < flipCardList.size(); i++) {
                    FlipCard flipCard = flipCardList.get(i);
                    // Tạo hai thẻ giống nhau cho mỗi cặp
                    CardPair pair = new CardPair(flipCard.getCardImageUrl(), i);
                    cardPairs.add(pair);
                    cardPairs.add(pair); // Thêm 2 lần để tạo cặp
                    
                    Log.d(TAG, "Created card pair for card " + i + " with URL: " + flipCard.getCardImageUrl());
                }
            } else {
                // Fallback to default images if no Firestore data
                Log.d(TAG, "Using default card images");
                
                // Check if cardImages array is large enough
                if (cardImages.length == 0) {
                    Log.e(TAG, "cardImages array is empty");
                    Toast.makeText(this, "Lỗi: Không có hình ảnh cho thẻ", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Tạo các cặp thẻ với hình ảnh mặc định
                for (int i = 0; i < cardImages.length; i++) {
                    // Tạo cặp thẻ với hình ảnh mặc định
                    CardPair pair = new CardPair(null, i);
                    cardPairs.add(pair);
                    cardPairs.add(pair); // Thêm 2 lần để tạo cặp
                }
            }
            
            // Tính điểm max dựa vào số lượng cặp thẻ
            maxScore = cardPairs.size() / 2;
            Log.d(TAG, "Max score set to: " + maxScore);
            
            // Tính kích thước lưới dựa vào số lượng thẻ, làm tròn lên
            int totalCards = cardPairs.size();
            int gridSize = (int) Math.ceil(Math.sqrt(totalCards));
            
            Log.d(TAG, "Total cards: " + totalCards + ", calculated grid size: " + gridSize + "x" + gridSize);
            
            // Set grid dimensions
            gridCards.setColumnCount(gridSize);
            gridCards.setRowCount(gridSize);
            
            Log.d(TAG, "Created " + cardPairs.size() + " cards in pairs");
            
            // Shuffle card pairs
            Collections.shuffle(cardPairs);
            
            Log.d(TAG, "Card pairs shuffled");
            
            // Create and add cards to grid
            for (int i = 0; i < cardPairs.size(); i++) {
                try {
                    // Inflate card layout
                    View cardView = inflater.inflate(R.layout.item_flip_card, gridCards, false);
                    if (cardView == null) {
                        Log.e(TAG, "Failed to inflate card view at position " + i);
                        continue;
                    }
                    
                    // Set layout params
                    cardView.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                    
                    // Get card and image views
                    final CardView card = cardView.findViewById(R.id.cardView);
                    if (card == null) {
                        Log.e(TAG, "CardView not found in inflated layout at position " + i);
                        continue;
                    }
                    
                    ImageView cardImage = card.findViewById(R.id.ivCardImage);
                    if (cardImage == null) {
                        Log.e(TAG, "Card image view not found in inflated layout at position " + i);
                        continue;
                    }
                    
                    // Store the card pair value in card's tag for matching later
                    CardPair currentPair = cardPairs.get(i);
                    card.setTag(currentPair.getValue());
                    
                    // Set image resource based on available data
                    if (currentPair.getImageUrl() != null && !currentPair.getImageUrl().isEmpty()) {
                        // Use Firestore data - load image with Picasso
                        Log.d(TAG, "Loading image from URL: " + currentPair.getImageUrl() + " for card " + i);
                        
                        // Enhanced Picasso configuration for more reliable loading
                        com.squareup.picasso.Picasso.get()
                                .load(currentPair.getImageUrl())
                                .placeholder(R.drawable.panda_icon)
                                .error(R.drawable.panda_icon)
                                .fit()
                                .centerCrop()  // Better for filling the entire space
                                .noFade()
                                .into(cardImage, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                    }
                                    
                                    @Override
                                    public void onError(Exception e) {
                                        cardImage.setImageResource(R.drawable.panda_icon);
                                    }
                                });
                    } else {
                        // Use default images
                        int imageResource = cardImages[currentPair.getValue() % cardImages.length];
                        cardImage.setImageResource(imageResource);
                    }
                    
                    // ADDED: Explicitly set initial visibility states
                    LinearLayout frontLayout = card.findViewById(R.id.cardFrontLayout);
                    LinearLayout backLayout = card.findViewById(R.id.cardBackLayout);
                    if (frontLayout != null && backLayout != null) {
                        // Initially show card back, hide card front
                        frontLayout.setVisibility(View.INVISIBLE);
                        backLayout.setVisibility(View.VISIBLE);
                        Log.d(TAG, "Set initial visibility for card " + i + ": front=INVISIBLE, back=VISIBLE");
                    }
                    
                    // Set click listener
                    final int position = i;
                    card.setOnClickListener(v -> {
                        if (allowCardFlip && !isCardFlipped(position)) {
                            Log.d(TAG, "Card clicked at position " + position);
                            flipCard(card, position);
                        } else {
                            Log.d(TAG, "Card click ignored: allowCardFlip=" + allowCardFlip + 
                                  ", isCardFlipped=" + isCardFlipped(position));
                        }
                    });
                    
                    // Set fixed dimensions for the card using GridLayout.LayoutParams
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = 0;
                    params.height = 0;
                    params.columnSpec = GridLayout.spec(i % gridSize, 1, 1f);
                    params.rowSpec = GridLayout.spec(i / gridSize, 1, 1f);
                    params.setMargins(8, 8, 8, 8);
                    card.setLayoutParams(params);
                    
                    // Add to grid and cards list
                    gridCards.addView(card);
                    cards.add(card);
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error creating card at position " + i, e);
                }
            }
            
            Log.d(TAG, "Created " + cards.size() + " cards in grid");
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing flip card game", e);
            Toast.makeText(this, "Lỗi khởi tạo game: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    // Class mới biểu diễn một cặp thẻ bài
    private static class CardPair {
        private final String imageUrl;
        private final int value;
        
        public CardPair(String imageUrl, int value) {
            this.imageUrl = imageUrl;
            this.value = value;
        }
        
        public String getImageUrl() {
            return imageUrl;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    private boolean isCardFlipped(int position) {
        return position == firstCardPosition || position == secondCardPosition;
    }
    
    private void flipCard(CardView card, int position) {
        // Get views
        LinearLayout frontLayout = card.findViewById(R.id.cardFrontLayout);
        LinearLayout backLayout = card.findViewById(R.id.cardBackLayout);
        
        // Ensure we can find both layouts
        if (frontLayout == null || backLayout == null) {
            Log.e(TAG, "Cannot find front or back layout for card at position " + position);
            return;
        }
        
        // Log initial state for debugging
        Log.d(TAG, "Flipping card " + position + ", initial visibility - front: " + 
              (frontLayout.getVisibility() == View.VISIBLE ? "VISIBLE" : "INVISIBLE") + 
              ", back: " + (backLayout.getVisibility() == View.VISIBLE ? "VISIBLE" : "INVISIBLE"));
        
        // Determine which card is being flipped (first or second)
        if (firstCard == null) {
            firstCard = card;
            firstCardPosition = position;
        } else if (secondCard == null && firstCard != card) {
            secondCard = card;
            secondCardPosition = position;
            
            // Temporarily disable card flipping while checking match
            allowCardFlip = false;
            
            // Schedule to check if cards match after animation completes
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> checkCardsMatch());
                }
            }, 1000);
        } else {
            // Already have two cards flipped, do nothing
            return;
        }
        
        // Flip animation
        card.animate()
                .setDuration(200)
                .setInterpolator(new AccelerateInterpolator())
                .rotationY(90)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // Swap visibility
                        backLayout.setVisibility(View.INVISIBLE);
                        frontLayout.setVisibility(View.VISIBLE);
                        
                        Log.d(TAG, "Card " + position + " mid-flip - front: VISIBLE, back: INVISIBLE");
                        
                        // Animate back
                        card.setRotationY(-90);
                        card.animate()
                                .setDuration(200)
                                .setInterpolator(new DecelerateInterpolator())
                                .rotationY(0)
                                .setListener(null);
                    }
                });
    }
    
    private void flipCardBack(CardView card) {
        // Get views
        LinearLayout frontLayout = card.findViewById(R.id.cardFrontLayout);
        LinearLayout backLayout = card.findViewById(R.id.cardBackLayout);
        
        // Ensure we can find both layouts
        if (frontLayout == null || backLayout == null) {
            Log.e(TAG, "Cannot find front or back layout for card while flipping back");
            return;
        }
        
        // Log initial state for debugging
        Log.d(TAG, "Flipping card back, initial visibility - front: " + 
              (frontLayout.getVisibility() == View.VISIBLE ? "VISIBLE" : "INVISIBLE") + 
              ", back: " + (backLayout.getVisibility() == View.VISIBLE ? "VISIBLE" : "INVISIBLE"));
        
        // Flip animation
        card.animate()
                .setDuration(200)
                .setInterpolator(new AccelerateInterpolator())
                .rotationY(90)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // Swap visibility
                        frontLayout.setVisibility(View.INVISIBLE);
                        backLayout.setVisibility(View.VISIBLE);
                        
                        Log.d(TAG, "Card flipped back - front: INVISIBLE, back: VISIBLE");
                        
                        // Animate back
                        card.setRotationY(-90);
                        card.animate()
                                .setDuration(200)
                                .setInterpolator(new DecelerateInterpolator())
                                .rotationY(0)
                                .setListener(null);
                    }
                });
    }
    
    private void checkCardsMatch() {
        if (firstCard != null && secondCard != null && firstCardPosition != -1 && secondCardPosition != -1) {
            // Check if the two flipped cards match using tags
            if (firstCard.getTag() != null && secondCard.getTag() != null && 
                firstCard.getTag().equals(secondCard.getTag())) {
                // Match found
                firstCard.setEnabled(false);
                secondCard.setEnabled(false);
                
                // Tăng điểm và cập nhật lên Firebase
                incrementScore();
                
                // Clear card selections
                firstCard = null;
                secondCard = null;
                firstCardPosition = -1;
                secondCardPosition = -1;
                
                // Check if game is over
                checkGameEnd();
            } else {
                // No match, flip cards back
                CardView tempFirstCard = firstCard;
                CardView tempSecondCard = secondCard;
                
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(() -> {
                            flipCardBack(tempFirstCard);
                            flipCardBack(tempSecondCard);
                            
                            // Clear card selections
                            firstCard = null;
                            secondCard = null;
                            firstCardPosition = -1;
                            secondCardPosition = -1;
                        });
                    }
                }, 500);
            }
            
            // Re-enable card flipping
            allowCardFlip = true;
        }
    }
    
    private void updateScore() {
        // Update score in Firebase
        if (currentRoom != null && currentUserId != null) {
            Map<String, Object> scoreUpdate = new HashMap<>();
            
            // Cập nhật điểm trong map scores với userId làm key
            scoreUpdate.put("scores/" + currentUserId, myScore);
            
            // Đồng thời vẫn cập nhật hostScore/guestScore để tương thích với code cũ
            if (isHost) {
                scoreUpdate.put("hostScore", myScore);
                Log.d(TAG, "Updating host score to Firebase - userId: " + currentUserId + ", score: " + myScore);
            } else {
                scoreUpdate.put("guestScore", myScore);
                Log.d(TAG, "Updating guest score to Firebase - userId: " + currentUserId + ", score: " + myScore);
            }
            
            // Cập nhật điểm trực tiếp qua Firebase
            updateRoomDataToFirebase(currentRoom.getRoomId(), scoreUpdate);
            
            // Kiểm tra nếu điểm đạt max thì kết thúc game
            if (myScore == maxScore) {
                Log.d(TAG, "Score reached max (" + maxScore + "), ending game");
                allowCardFlip = false;
                endGame();
            }
        }
    }
    
    // Phương thức đơn giản để cập nhật UI điểm số
    private void updateScoreUI() {
        // Điểm bên trái luôn là host, điểm bên phải luôn là guest
        tvPlayer1Score.setText(String.valueOf(hostScore));
        tvPlayer2Score.setText(String.valueOf(guestScore));
        if (hostScore == maxScore || guestScore == maxScore) {
            allowCardFlip = false;
            endGame();
        }
        // Log để kiểm tra
        Log.d(TAG, "Cập nhật UI điểm số - Host: " + hostScore + ", Guest: " + guestScore);
    }
    
    private void checkGameState() {
        if (currentRoom == null) {
            Log.e(TAG, "checkGameState: currentRoom is null");
            return;
        }
        
        Log.d(TAG, "Checking game state");
        
        // Kiểm tra trạng thái kết thúc game từ Firebase
        Boolean gameEnded = currentRoom.getBooleanExtraValue("gameEnded", false);
        if (gameEnded && gameActuallyStarted) {
            Log.d(TAG, "Game đã kết thúc từ người chơi khác");
            allowCardFlip = false; // Vô hiệu hóa tương tác với thẻ
            
            // Lấy điểm số cuối cùng từ Firebase
            Integer finalHostScore = getIntValue(currentRoom, "finalHostScore", hostScore);
            Integer finalGuestScore = getIntValue(currentRoom, "finalGuestScore", guestScore);
            
            // Cập nhật điểm số cuối cùng
            hostScore = finalHostScore;
            guestScore = finalGuestScore;
            
            // Hiển thị kết quả game
            endGame();
            return;
        }
        
        // Check if both players are ready
        boolean bothPlayersReady = currentRoom.getHostId() != null && currentRoom.getGuestId() != null;
        Log.d(TAG, "Both players ready: " + bothPlayersReady);
        
        // Update game state
        if (bothPlayersReady && !gameStarted) {
            // Enable start button for host only
            if (isHost) {
                btnGameAction.setEnabled(true);
                btnGameAction.setText("Bắt Đầu");
                Log.d(TAG, "Enabling start button for host");
            } else {
                btnGameAction.setEnabled(false);
                btnGameAction.setText("Chờ chủ phòng");
                Log.d(TAG, "Disabling start button for guest");
            }
        }
        
        // Kiểm tra trạng thái game đã bắt đầu chưa
        Boolean gameStartedValue = currentRoom.getBooleanExtraValue("gameStarted", false);
        Log.d(TAG, "Game started value from server: " + gameStartedValue);
        
        if (gameStartedValue != null && gameStartedValue && !gameStarted) {
            // Game đã được bắt đầu bởi host, cập nhật trạng thái máy khách
            Log.d(TAG, "Game started by host, updating local state");
            gameStarted = true;
            btnGameAction.setVisibility(View.GONE);
            
            // Gọi startGame() ngay cả cho guest để kích hoạt đếm ngược
            if (!isHost && !isCountingDown && !gameActuallyStarted) {
                Log.d(TAG, "Guest detected game started, calling startGame()");
                // Đảm bảo không chạy đè lên nhau
                new Handler().postDelayed(() -> {
                    if (!isCountingDown && !gameActuallyStarted) {
                        startGame();
                    }
                }, 500);
            }
        }
        
        // Kiểm tra trạng thái đếm ngược
        Boolean countdownStarted = currentRoom.getBooleanExtraValue("countdownStarted", false);
        if (countdownStarted && !isCountingDown && !gameActuallyStarted) {
            Log.d(TAG, "Countdown started on server, starting local countdown");
            startCountdownBeforeGame();
        }
        
        // Kiểm tra game thực sự đã bắt đầu chưa
        Boolean gameActuallyStartedValue = currentRoom.getBooleanExtraValue("gameActuallyStarted", false);
        Log.d(TAG, "Game actually started value from server: " + gameActuallyStartedValue);
        
        // Cập nhật điểm số từ Firebase bất kể trạng thái của game
        updateScoresFromFirebase();
        
        // Check if game is already in progress
        if (gameStartedValue != null && gameStartedValue) {
            // Nếu game thực sự đã bắt đầu, bật tính năng tương tác
            if (gameActuallyStartedValue != null && gameActuallyStartedValue) {
                if (!gameActuallyStarted) {
                    Log.d(TAG, "Game actually started on server, enabling game interaction");
                    gameActuallyStarted = true;
                    allowCardFlip = true;
                    
                    // Đảm bảo timer đã bắt đầu
                    if (!isCountingDown && (gameTimer == null)) {
                        int remainingTimeValue = getIntValue(currentRoom, "remainingTime", GAME_DURATION_SECONDS);
                        Log.d(TAG, "Starting game timer with remaining time: " + remainingTimeValue);
                        startGameTimer(remainingTimeValue);
                    }
                    
                    // Log thêm để debug
                    Log.d(TAG, "User " + (isHost ? "HOST" : "GUEST") + " can now interact with cards");
                    Log.d(TAG, "allowCardFlip = " + allowCardFlip + ", gameActuallyStarted = " + gameActuallyStarted);
                }
                
                // Start or resume timer nếu không đang đếm ngược và chưa có timer
                if (!isCountingDown && (gameTimer == null)) {
                    int remainingTimeValue = getIntValue(currentRoom, "remainingTime", GAME_DURATION_SECONDS);
                    Log.d(TAG, "Starting game timer with remaining time: " + remainingTimeValue);
                    startGameTimer(remainingTimeValue);
                }
            }
        }
    }
    
    // Thêm phương thức mới để cập nhật điểm từ Firebase
    private void updateScoresFromFirebase() {
        if (currentRoom == null) return;
        
        try {
            // Lấy điểm từ map scores
            Map<String, Object> scoresMap = (Map<String, Object>) currentRoom.getExtraData().get("scores");
            
            // Nếu map scores tồn tại
            if (scoresMap != null) {
                // Lấy điểm của mình từ scores map
                Object myScoreObj = scoresMap.get(currentUserId);
                if (myScoreObj != null) {
                    if (myScoreObj instanceof Long) {
                        myScore = ((Long) myScoreObj).intValue();
                    } else if (myScoreObj instanceof Integer) {
                        myScore = (Integer) myScoreObj;
                    } else if (myScoreObj instanceof Double) {
                        myScore = ((Double) myScoreObj).intValue();
                    }
                }
                
                // Lấy điểm của đối thủ
                String opponentId = isHost ? currentRoom.getGuestId() : currentRoom.getHostId();
                if (opponentId != null) {
                    Object opponentScoreObj = scoresMap.get(opponentId);
                    if (opponentScoreObj != null) {
                        if (opponentScoreObj instanceof Long) {
                            opponentScore = ((Long) opponentScoreObj).intValue();
                        } else if (opponentScoreObj instanceof Integer) {
                            opponentScore = (Integer) opponentScoreObj;
                        } else if (opponentScoreObj instanceof Double) {
                            opponentScore = ((Double) opponentScoreObj).intValue();
                        }
                    }
                }
                
                Log.d(TAG, "Scores from Firebase scores map - My userId: " + currentUserId + 
                      ", My score: " + myScore + ", Opponent ID: " + opponentId + 
                      ", Opponent score: " + opponentScore);
                
                // Kiểm tra nếu điểm đạt max thì kết thúc game
                if (myScore == maxScore || opponentScore == maxScore) {
                    Log.d(TAG, "Score reached max (" + maxScore + "), ending game");
                    allowCardFlip = false;
                    endGame();
                }
            } else {
                // Nếu không có map scores, quay lại dùng hostScore/guestScore
                int hostScore = getIntValue(currentRoom, "hostScore", 0);
                int guestScore = getIntValue(currentRoom, "guestScore", 0);
                
                Log.d(TAG, "Scores from Firebase fallback - Host: " + hostScore + ", Guest: " + guestScore);
                
                // Cập nhật cả điểm của mình và đối thủ từ Firebase để đảm bảo đồng bộ
                if (isHost) {
                    // Nếu là host, cập nhật điểm từ Firebase
                    myScore = hostScore;
                    opponentScore = guestScore;
                    Log.d(TAG, "HOST updated scores - My score (host): " + myScore + ", Opponent (guest): " + opponentScore);
                } else {
                    // Nếu là guest, cập nhật điểm từ Firebase
                    myScore = guestScore;
                    opponentScore = hostScore;
                    Log.d(TAG, "GUEST updated scores - My score (guest): " + myScore + ", Opponent (host): " + opponentScore);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating scores from Firebase", e);
        }
        
        // Cập nhật UI điểm số
        updateScoreUI();
    }
    
    private int getIntValue(PvpRoom room, String key, int defaultValue) {
        try {
            Map<String, Object> extraData = room.getExtraData();
            if (extraData != null && extraData.containsKey(key)) {
                Object value = extraData.get(key);
                if (value instanceof Integer) {
                    return (Integer) value;
                } else if (value instanceof Long) {
                    return ((Long) value).intValue();
                } else if (value instanceof String) {
                    return Integer.parseInt((String) value);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting int value for key: " + key, e);
        }
        return defaultValue;
    }
    
    private void startGame() {
        if (currentRoom == null) {
            Log.e(TAG, "startGame: currentRoom is null");
            return;
        }
        
        Log.d(TAG, isHost ? "Host starting game" : "Guest starting game");
        
        // QUAN TRỌNG: Đánh dấu để tránh bị xóa phòng khi ViewModel bị hủy
        pvpViewModel.setPreventRoomDeletion(true);
        
        // Update game started state in Firebase (chỉ host mới cập nhật)
        if (isHost) {
            Map<String, Object> gameStateUpdate = new HashMap<>();
            gameStateUpdate.put("gameStarted", true);
            gameStateUpdate.put("gameStartTime", System.currentTimeMillis());
            
            // Đánh dấu game đã bắt đầu nhưng chưa sẵn sàng chơi
            updateRoomDataToFirebase(currentRoom.getRoomId(), gameStateUpdate);
        }
        
        // Update local state - áp dụng cho cả host và guest
        gameStarted = true;
        btnGameAction.setVisibility(View.GONE);
        
        // Đợi một chút để đảm bảo cập nhật đã được ghi vào Firebase
        new Handler().postDelayed(() -> {
            // Bắt đầu đếm ngược - áp dụng cho cả host và guest
            startCountdownBeforeGame();
            
            Log.d(TAG, "Game started - waiting for countdown");
        }, 300);
    }
    
    private void startGameTimer(int seconds) {
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        
        remainingTime = seconds > 0 ? seconds : GAME_DURATION_SECONDS;
        tvTimeRemaining.setText(String.valueOf(remainingTime));
        
        gameTimer = new CountDownTimer(remainingTime * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = (int) (millisUntilFinished / 1000);
                tvTimeRemaining.setText(String.valueOf(remainingTime));
                
                // Update remaining time in Firebase (only host updates every 5 seconds to reduce writes)
                if (isHost && (remainingTime % 5 == 0 || remainingTime <= 5)) {
                    Map<String, Object> timeUpdate = new HashMap<>();
                    timeUpdate.put("remainingTime", remainingTime);
                    updateRoomDataToFirebase(currentRoom.getRoomId(), timeUpdate);
                }
            }
            
            @Override
            public void onFinish() {
                remainingTime = 0;
                tvTimeRemaining.setText("0");
                
                // Game over
                allowCardFlip = false;
                endGame();
            }
        }.start();
    }
    
    private void checkGameEnd() {
        // Kiểm tra xem tất cả thẻ đã được lật chưa
        boolean allMatched = true;
        for (CardView card : cards) {
            if (card.isEnabled()) {
                allMatched = false;
                break;
            }
        }
        
        if (allMatched) {
            Log.d(TAG, "Tất cả thẻ đã được lật - Kết thúc game");
            // Vô hiệu hóa tương tác với thẻ
            allowCardFlip = false;
            // Kết thúc game và hiển thị điểm
            endGame();
        }
    }
    
    private void endGame() {
        // Cancel timer
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        
        // Disable game interaction
        allowCardFlip = false;
        
        // Đảm bảo đồng bộ điểm số cuối cùng từ Firebase
        syncScoresFromFirebase();
        
        // Đợi một chút để đảm bảo tất cả các cập nhật điểm đã hoàn tất
        new Handler().postDelayed(() -> {
            // Determine winner based on final scores
            String winnerMessage;
            if (isHost) {
                // Nếu là host
                if (hostScore > guestScore) {
                    winnerMessage = "Bạn đã thắng! 🏆";
                    showGameResult(true, winnerMessage);
                } else if (hostScore < guestScore) {
                    winnerMessage = "Bạn đã thua! 😢";
                    showGameResult(false, winnerMessage);
                } else {
                    winnerMessage = "Hòa! 🤝";
                    showGameResult(null, winnerMessage);
                }
            } else {
                // Nếu là guest
                if (guestScore > hostScore) {
                    winnerMessage = "Bạn đã thắng! 🏆";
                    showGameResult(true, winnerMessage);
                } else if (guestScore < hostScore) {
                    winnerMessage = "Bạn đã thua! 😢";
                    showGameResult(false, winnerMessage);
                } else {
                    winnerMessage = "Hòa! 🤝";
                    showGameResult(null, winnerMessage);
                }
            }
            
            // Log kết quả cuối cùng để debug
            Log.d(TAG, "Game ended - Final scores: Host: " + hostScore + ", Guest: " + guestScore);
            
            // Update game state in Firebase (host only)
            if (isHost || !isHost) {
                Map<String, Object> gameEndUpdate = new HashMap<>();
                gameEndUpdate.put("gameEnded", true);
                gameEndUpdate.put("remainingTime", 0);
                gameEndUpdate.put("finalHostScore", hostScore);
                gameEndUpdate.put("finalGuestScore", guestScore);
                updateRoomDataToFirebase(currentRoom.getRoomId(), gameEndUpdate);
                
                Log.d(TAG, "Host updated final scores to Firebase - Host: " + hostScore + ", Guest: " + guestScore);
            }
        }, 500); // Delay 500ms để đảm bảo cập nhật đã hoàn tất
    }
    
    private void showGameResult(Boolean isWin, String message) {
        // Lấy điểm số của người chơi hiện tại để hiển thị
        int myFinalScore = isHost ? hostScore : guestScore;
        int opponentFinalScore = isHost ? guestScore : hostScore;
        
        // Log điểm số cuối cùng
        Log.d(TAG, "FINAL SCORES - Host: " + hostScore + ", Guest: " + guestScore);
        Log.d(TAG, "My Score: " + myFinalScore + ", Opponent Score: " + opponentFinalScore);
        Log.d(TAG, "Game result: " + (isWin == null ? "Hòa" : (isWin ? "Thắng" : "Thua")));
        
        // Set result information
        if (isWin != null) {
            if (isWin) {
                ivGameResultIcon.setImageResource(R.drawable.panda_icon);
                ivGameResultIcon.setColorFilter(getResources().getColor(android.R.color.holo_green_light));
                tvGameResultTitle.setText("Chiến thắng!");
                btnGameResultAction.setText("Quay lại Lobby");
            } else {
                ivGameResultIcon.setImageResource(R.drawable.panda_icon);
                ivGameResultIcon.setColorFilter(getResources().getColor(android.R.color.holo_red_light));
                tvGameResultTitle.setText("Thua cuộc!");
                btnGameResultAction.setText("Quay lại Lobby");
            }
        } else {
            ivGameResultIcon.setImageResource(R.drawable.panda_icon);
            ivGameResultIcon.setColorFilter(getResources().getColor(android.R.color.holo_blue_light));
            tvGameResultTitle.setText("Hòa!");
            btnGameResultAction.setText("Quay lại Lobby");
        }
        
        // Hiển thị chi tiết điểm số trong thông báo kết quả
        tvGameResultMessage.setText(message + 
                "\nĐiểm của bạn: " + myFinalScore + 
                "\nĐiểm đối thủ: " + opponentFinalScore );
        
        // Set button action
        btnGameResultAction.setOnClickListener(v -> {
            gameOverlayFrame.setVisibility(View.GONE);
            
            // Nếu là host, đóng phòng và xóa phòng
            if (isHost && currentRoom != null) {
                // Cập nhật trạng thái phòng thành CLOSED
                Map<String, Object> closeUpdate = new HashMap<>();
                closeUpdate.put("status", "CLOSED");
                updateRoomDataToFirebase(currentRoom.getRoomId(), closeUpdate);
                
                // Xóa phòng sau một khoảng thời gian ngắn
                new Handler().postDelayed(() -> {
                    // Xóa phòng khỏi database
                    FirebaseInitializer.getPvpRoomsRef().child(currentRoom.getRoomId()).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Host đã xóa phòng: " + currentRoom.getRoomId());
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Lỗi khi xóa phòng: " + e.getMessage());
                        });
                }, 500);
            }
            
            // Quay về lobby thay vì màn hình chờ
            goToLobby();
        });
        
        // Show overlay
        gameOverlayFrame.setVisibility(View.VISIBLE);
    }
    
    // Phương thức để chuyển về màn hình Lobby
    private void goToLobby() {
        // Tạo intent mới đến PvpLobbyActivity
        Intent intent = new Intent(this, PvpLobbyActivity.class);
        
        // Xóa tất cả các activity trong stack
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        
        // Bắt đầu activity
        startActivity(intent);
        
        // Đóng activity hiện tại
        finish();
        
        Log.d(TAG, "Chuyển hướng về màn hình Lobby");
    }
    
    private void showExitConfirmDialog() {
        Log.d(TAG, "Showing exit confirmation dialog");
        new AlertDialog.Builder(this)
                .setTitle("Thoát trò chơi")
                .setMessage("Bạn có chắc chắn muốn thoát? " + 
                    (isHost ? "Phòng sẽ bị đóng và xóa." : "Bạn sẽ rời khỏi phòng."))
                .setPositiveButton("Thoát", (dialog, which) -> {
                    Log.d(TAG, "User confirmed exit");
                    
                    // If game started, update final score
                    if (gameStarted && currentRoom != null) {
                        Log.d(TAG, "Saving final scores before exit");
                        Map<String, Object> finalScoreUpdate = new HashMap<>();
                        if (isHost) {
                            finalScoreUpdate.put("hostScore", hostScore);
                            finalScoreUpdate.put("hostLeft", true);
                            
                            // Đánh dấu phòng đã đóng
                            finalScoreUpdate.put("status", "CLOSED");
                            
                            // Cập nhật lên Firebase
                            updateRoomDataToFirebase(currentRoom.getRoomId(), finalScoreUpdate);
                            
                            // Xóa phòng sau một khoảng thời gian ngắn nếu là host
                            new Handler().postDelayed(() -> {
                                // Xóa phòng khỏi database
                                FirebaseInitializer.getPvpRoomsRef().child(currentRoom.getRoomId()).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Host đã xóa phòng khi thoát: " + currentRoom.getRoomId());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Lỗi khi xóa phòng: " + e.getMessage());
                                    });
                            }, 500);
                        } else {
                            finalScoreUpdate.put("guestScore", guestScore);
                            finalScoreUpdate.put("guestLeft", true);
                            
                            // Cập nhật lên Firebase
                            updateRoomDataToFirebase(currentRoom.getRoomId(), finalScoreUpdate);
                        }
                    } else if (isHost && currentRoom != null) {
                        // Nếu game chưa bắt đầu nhưng là host, vẫn cần đóng và xóa phòng
                        Map<String, Object> closeUpdate = new HashMap<>();
                        closeUpdate.put("status", "CLOSED");
                        updateRoomDataToFirebase(currentRoom.getRoomId(), closeUpdate);
                        
                        // Xóa phòng sau một khoảng thời gian ngắn
                        new Handler().postDelayed(() -> {
                            FirebaseInitializer.getPvpRoomsRef().child(currentRoom.getRoomId()).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Host đã xóa phòng khi thoát (game chưa bắt đầu): " + currentRoom.getRoomId());
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Lỗi khi xóa phòng: " + e.getMessage());
                                });
                        }, 500);
                    }
                    
                    // Quay về lobby thay vì màn hình chờ
                    goToLobby();
                })
                .setNegativeButton("Ở lại", (dialog, which) -> {
                    Log.d(TAG, "User cancelled exit");
                })
                .show();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
        
        // Tạm dừng game timer nếu đang chạy
        if (gameTimer != null) {
            Log.d(TAG, "Cancelling game timer in onPause");
            gameTimer.cancel();
            gameTimer = null;
        }
        
        // Save game state if needed
        if (gameStarted && currentRoom != null) {
            Log.d(TAG, "Saving game state in onPause");
            Map<String, Object> stateUpdate = new HashMap<>();
            if (isHost) {
                stateUpdate.put("hostScore", hostScore);
                stateUpdate.put("hostActive", false);
            } else {
                stateUpdate.put("guestScore", guestScore);
                stateUpdate.put("guestActive", false);
            }
            
            // If host, also update the remaining time
            if (isHost && remainingTime > 0) {
                stateUpdate.put("remainingTime", remainingTime);
                stateUpdate.put("gamePaused", true);
                stateUpdate.put("pauseTime", System.currentTimeMillis());
            }
            
            updateRoomDataToFirebase(currentRoom.getRoomId(), stateUpdate);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        
        // Đánh dấu người chơi đã quay lại
        if (currentRoom != null) {
            Log.d(TAG, "Marking player as active in onResume");
            Map<String, Object> activeUpdate = new HashMap<>();
            if (isHost) {
                activeUpdate.put("hostActive", true);
            } else {
                activeUpdate.put("guestActive", true);
            }
            
            // Nếu là host và game đang tạm dừng, tiếp tục game
            if (isHost && currentRoom.getBooleanExtraValue("gamePaused", false)) {
                activeUpdate.put("gamePaused", false);
                activeUpdate.put("resumeTime", System.currentTimeMillis());
            }
            
            updateRoomDataToFirebase(currentRoom.getRoomId(), activeUpdate);
        }
        
        // Kiểm tra lại trạng thái game - có thể host đã bắt đầu trong khi player đang ở trạng thái paused
        if (pvpViewModel != null) {
            String roomId = currentRoom != null ? currentRoom.getRoomId() : null;
            if (roomId != null) {
                Log.d(TAG, "Refreshing room data in onResume for room: " + roomId);
                loadRoomDataDirectly(roomId);
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");
        
        // Clean up countdown timer
        if (gameStartCountdownTimer != null) {
            Log.d(TAG, "Cancelling game start countdown timer");
            gameStartCountdownTimer.cancel();
            gameStartCountdownTimer = null;
        }
        
        // Clean up game timer
        if (gameTimer != null) {
            Log.d(TAG, "Cancelling game timer");
            gameTimer.cancel();
            gameTimer = null;
        }
        
        // Clean up game state check timer
        if (gameStateCheckTimer != null) {
            Log.d(TAG, "Cancelling game state check timer");
            gameStateCheckTimer.cancel();
            gameStateCheckTimer = null;
        }
        
        // Hủy lắng nghe điểm số khi Activity bị hủy
        if (currentRoom != null && scoreListener != null) {
            FirebaseInitializer.getPvpRoomsRef().child(currentRoom.getRoomId()).removeEventListener(scoreListener);
            scoreListener = null;
            Log.d(TAG, "Đã hủy lắng nghe điểm số khi onDestroy");
        }
        
        // Đảm bảo hủy observer khi activity bị hủy
        if (flipCardLevelObserver != null) {
            flipCardLevelViewModel.getAllLevels().removeObserver(flipCardLevelObserver);
        }
        
        // Get room ID from intent
        String roomId = getIntent().getStringExtra("ROOM_ID");
        if (roomId != null && !roomId.isEmpty()) {
            // Luôn hủy đăng ký sử dụng phòng
            PvpRoomManager.getInstance().unregisterRoomUsage(roomId);
            Log.d(TAG, "Unregistered room usage for room: " + roomId);
            
            // Nếu là host, đảm bảo phòng được đóng và xóa
            if (isHost && currentRoom != null) {
                // Đánh dấu phòng đã đóng
                Map<String, Object> closeUpdate = new HashMap<>();
                closeUpdate.put("status", "CLOSED");
                FirebaseInitializer.getPvpRoomsRef().child(roomId).updateChildren(closeUpdate);
                
                // Xóa phòng sau một khoảng thời gian ngắn
                new Handler().postDelayed(() -> {
                    FirebaseInitializer.getPvpRoomsRef().child(roomId).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Host đã xóa phòng trong onDestroy: " + roomId);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Lỗi khi xóa phòng trong onDestroy: " + e.getMessage());
                        });
                }, 500);
                
                Log.d(TAG, "Host đã đóng phòng trong onDestroy: " + roomId);
            }
            
            // Đánh dấu cho phép xóa phòng
            PvpRoomManager.getInstance().setPreventRoomDeletion(roomId, false);
        }
        
        // Remove observers to avoid memory leaks
        if (pvpViewModel != null) {
            Log.d(TAG, "Removing observers from ViewModel");
            pvpViewModel.getCurrentRoom().removeObservers(this);
        }
        
        super.onDestroy();
    }
    
    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back button pressed");
        showExitConfirmDialog();
    }
    
    // Phương thức để tải dữ liệu phòng trực tiếp từ Firebase
    private void loadRoomDataDirectly(String roomId) {
        Log.d(TAG, "Loading room data directly from Firebase for room: " + roomId);
        
        // Tham chiếu đến phòng trong Firebase
        DatabaseReference roomRef = 
            FirebaseInitializer.getPvpRoomsRef().child(roomId);
        
        roomRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.e(TAG, "Room does not exist: " + roomId);
                    Toast.makeText(PvpGameActivity.this, 
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
                    
                    // Lưu các dữ liệu khác vào extraData
                    Map<String, Object> extraData = new HashMap<>();
                    
                    if (dataSnapshot.child("gameStarted").exists()) {
                        extraData.put("gameStarted", dataSnapshot.child("gameStarted").getValue(Boolean.class));
                    }
                    
                    if (dataSnapshot.child("remainingTime").exists()) {
                        extraData.put("remainingTime", dataSnapshot.child("remainingTime").getValue(Integer.class));
                    }
                    
                    if (dataSnapshot.child("hostScore").exists()) {
                        extraData.put("hostScore", dataSnapshot.child("hostScore").getValue(Integer.class));
                    }
                    
                    if (dataSnapshot.child("guestScore").exists()) {
                        extraData.put("guestScore", dataSnapshot.child("guestScore").getValue(Integer.class));
                    }
                    
                    // Thiết lập extraData cho room
                    room.setExtraData(extraData);
                    
                    // Cập nhật ViewModel
                    pvpViewModel.setCurrentRoomDirectly(room);
                    
                    // Đăng ký lắng nghe thay đổi phòng
                    pvpViewModel.listenForRoomChangesDirectly(roomId);
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing room data", e);
                    Toast.makeText(PvpGameActivity.this, 
                        "Lỗi khi đọc dữ liệu phòng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Log.e(TAG, "Error loading room data", databaseError.toException());
                Toast.makeText(PvpGameActivity.this, 
                    "Lỗi kết nối đến server: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // Phương thức cập nhật dữ liệu phòng trực tiếp qua Firebase
    private void updateRoomDataToFirebase(String roomId, Map<String, Object> updates) {
        if (roomId == null || updates == null || updates.isEmpty()) {
            Log.e(TAG, "Invalid room data update request");
            return;
        }
        
        // Lưu trữ lại tham chiếu phòng để xử lý lỗi
        final String savedRoomId = roomId;
        final Map<String, Object> savedUpdates = new HashMap<>(updates);
        
        Log.d(TAG, "Updating room data: " + roomId + " with " + updates.toString());
        
        // Kiểm tra xem phòng có tồn tại không trước khi cập nhật
        DatabaseReference roomsRef = FirebaseInitializer.getPvpRoomsRef();
        roomsRef.child(roomId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.e(TAG, "Cannot update room: Room doesn't exist: " + roomId);
                    Toast.makeText(PvpGameActivity.this, 
                        "Phòng không còn tồn tại, có thể đã bị đóng bởi chủ phòng", Toast.LENGTH_LONG).show();
                    return;
                }
                
                // Phòng tồn tại, tiến hành cập nhật
                DatabaseReference roomRef = FirebaseInitializer.getPvpRoomsRef().child(roomId);
                roomRef.updateChildren(savedUpdates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Room data updated successfully: " + savedUpdates.toString());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating room data: " + e.getMessage());
                        
                        // Thử cập nhật lại sau một khoảng thời gian
                        new Handler().postDelayed(() -> {
                            Log.d(TAG, "Retrying room update after failure");
                            DatabaseReference retryRef = FirebaseInitializer.getPvpRoomsRef().child(savedRoomId);
                            retryRef.updateChildren(savedUpdates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Room data update retry successful");
                                })
                                .addOnFailureListener(retryE -> {
                                    Log.e(TAG, "Room data update retry failed: " + retryE.getMessage());
                                    Toast.makeText(PvpGameActivity.this, 
                                        "Không thể cập nhật dữ liệu phòng: " + retryE.getMessage(), 
                                        Toast.LENGTH_SHORT).show();
                                });
                        }, 500);
                    });
            }
            
            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Log.e(TAG, "Error checking room existence", databaseError.toException());
            }
        });
    }
    
    // Phương thức đơn giản để tăng điểm và cập nhật lên Firebase
    private void incrementScore() {
        // Tăng điểm của người chơi hiện tại
        if (isHost || !isHost) {
            hostScore++;
            Log.d(TAG, "Host ghi điểm: " + hostScore);
        } else {
            guestScore++;
            Log.d(TAG, "Guest ghi điểm: " + guestScore);
        }
        
        // Cập nhật điểm lên Firebase
        if (currentRoom != null) {
            Map<String, Object> scoreUpdate = new HashMap<>();
            scoreUpdate.put("hostScore", hostScore);
            scoreUpdate.put("guestScore", guestScore);
            updateRoomDataToFirebase(currentRoom.getRoomId(), scoreUpdate);
            Log.d(TAG, "Đã cập nhật điểm lên Firebase - Host: " + hostScore + ", Guest: " + guestScore);
        }
        if (guestScore == maxScore || hostScore == maxScore)
        {
            endGame();
            Log.d(TAG, "Max điểm: " + maxScore);
        }
        // Cập nhật UI
        updateScoreUI();
    }
    
    // Phương thức đồng bộ điểm số từ Firebase
    private void syncScoresFromFirebase() {
        if (currentRoom == null) return;
        
        // Đọc điểm từ Firebase
        DatabaseReference roomRef = FirebaseInitializer.getPvpRoomsRef().child(currentRoom.getRoomId());
        roomRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                // Đọc điểm từ Firebase
                if (dataSnapshot.child("hostScore").exists()) {
                    Long value = dataSnapshot.child("hostScore").getValue(Long.class);
                    if (value != null) {
                        hostScore = value.intValue();
                    }
                }
                
                if (dataSnapshot.child("guestScore").exists()) {
                    Long value = dataSnapshot.child("guestScore").getValue(Long.class);
                    if (value != null) {
                        guestScore = value.intValue();
                    }
                }
                
                Log.d(TAG, "Đã đồng bộ điểm từ Firebase - Host: " + hostScore + ", Guest: " + guestScore);
                
                // Cập nhật UI
                updateScoreUI();
            }
            
            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Log.e(TAG, "Lỗi đọc điểm từ Firebase", databaseError.toException());
            }
        });
    }

    private void initMathGame(View gameContent) {
        // Khởi tạo các view
        tvMathQuestion = gameContent.findViewById(R.id.tvMathQuestion);
        btnAnswer1 = gameContent.findViewById(R.id.btnAnswer1);
        btnAnswer2 = gameContent.findViewById(R.id.btnAnswer2);
        btnAnswer3 = gameContent.findViewById(R.id.btnAnswer3);
        btnAnswer4 = gameContent.findViewById(R.id.btnAnswer4);

        // Thiết lập click listener cho các nút đáp án
        View.OnClickListener answerClickListener = v -> {
            if (!allowCardFlip) return;
            
            Button clickedButton = (Button) v;
            int selectedAnswer = Integer.parseInt(clickedButton.getText().toString());
            
            if (selectedAnswer == currentAnswer) {
                // Đáp án đúng
                incrementScore();
                questionCount++;
                
                if (questionCount >= MAX_QUESTIONS) {
                    // Đã hoàn thành 10 câu hỏi
                    allowCardFlip = false;
                    endGame();
                } else {
                    // Tạo câu hỏi mới
                    generateNewQuestion();
                }
            }
        };

        btnAnswer1.setOnClickListener(answerClickListener);
        btnAnswer2.setOnClickListener(answerClickListener);
        btnAnswer3.setOnClickListener(answerClickListener);
        btnAnswer4.setOnClickListener(answerClickListener);

        // Tạo câu hỏi đầu tiên
        generateNewQuestion();
    }

    private void generateNewQuestion() {
        // Tạo phép tính ngẫu nhiên
        int num1 = random.nextInt(20) + 1; // Số từ 1-20
        int num2 = random.nextInt(20) + 1;
        int operation = random.nextInt(4); // 0: +, 1: -, 2: *, 3: /
        
        String questionText;
        switch (operation) {
            case 0: // Phép cộng
                currentAnswer = num1 + num2;
                questionText = num1 + " + " + num2 + " = ?";
                break;
            case 1: // Phép trừ
                currentAnswer = num1 - num2;
                questionText = num1 + " - " + num2 + " = ?";
                break;
            case 2: // Phép nhân
                currentAnswer = num1 * num2;
                questionText = num1 + " × " + num2 + " = ?";
                break;
            default: // Phép chia
                // Đảm bảo kết quả là số nguyên
                currentAnswer = num1;
                num2 = 1;
                questionText = num1 + " ÷ " + num2 + " = ?";
                break;
        }
        
        tvMathQuestion.setText(questionText);
        
        // Tạo các đáp án ngẫu nhiên
        List<Integer> answers = new ArrayList<>();
        answers.add(currentAnswer);
        
        // Thêm 3 đáp án sai ngẫu nhiên
        while (answers.size() < 4) {
            int wrongAnswer = currentAnswer + random.nextInt(10) - 5; // Số ngẫu nhiên trong khoảng ±5
            if (wrongAnswer != currentAnswer && !answers.contains(wrongAnswer)) {
                answers.add(wrongAnswer);
            }
        }
        
        // Xáo trộn thứ tự đáp án
        Collections.shuffle(answers);
        
        // Hiển thị đáp án lên các nút
        btnAnswer1.setText(String.valueOf(answers.get(0)));
        btnAnswer2.setText(String.valueOf(answers.get(1)));
        btnAnswer3.setText(String.valueOf(answers.get(2)));
        btnAnswer4.setText(String.valueOf(answers.get(3)));
    }
} 