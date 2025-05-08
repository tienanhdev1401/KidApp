package com.example.kidapp.Activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.kidapp.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class LearnLetterFragment extends Fragment {
    private LinearLayout letterContainer;
    private Map<String, Integer> letterImageMap;
    private TextToSpeech textToSpeech;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learn_letter, container, false);
        CardView btnBack = view.findViewById(R.id.backButton);
        btnBack.setOnClickListener(v -> requireActivity().finish());
        letterContainer = view.findViewById(R.id.letterContainer);
        setupLetterResources();
        setupClickListeners(view);
        textToSpeech = new TextToSpeech(getContext(), status -> {});
        return view;
    }

    @Override
    public void onDestroyView() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroyView();
    }

    private void setupLetterResources() {
        letterImageMap = new HashMap<>();
        letterImageMap.put("A", R.drawable.a);
        letterImageMap.put("B", R.drawable.b);
        letterImageMap.put("C", R.drawable.c);
        letterImageMap.put("D", R.drawable.d);
        letterImageMap.put("E", R.drawable.e);
        letterImageMap.put("F", R.drawable.f);
        letterImageMap.put("G", R.drawable.g);
        letterImageMap.put("H", R.drawable.h);
        letterImageMap.put("I", R.drawable.i);
        letterImageMap.put("J", R.drawable.j);
        letterImageMap.put("K", R.drawable.k);
        letterImageMap.put("L", R.drawable.l);
        letterImageMap.put("M", R.drawable.m);
        letterImageMap.put("N", R.drawable.n);
        letterImageMap.put("O", R.drawable.o);
        letterImageMap.put("P", R.drawable.p);
        letterImageMap.put("Q", R.drawable.q);
        letterImageMap.put("R", R.drawable.r);
        letterImageMap.put("S", R.drawable.s);
        letterImageMap.put("T", R.drawable.t);
        letterImageMap.put("U", R.drawable.u);
        letterImageMap.put("V", R.drawable.v);
        letterImageMap.put("W", R.drawable.w);
        letterImageMap.put("X", R.drawable.x);
        letterImageMap.put("Y", R.drawable.y);
        letterImageMap.put("Z", R.drawable.z);
    }

    private void setupClickListeners(View rootView) {
        for (String letter : letterImageMap.keySet()) {
            int resId = getResources().getIdentifier("image" + letter, "id", requireContext().getPackageName());
            ImageView imageView = rootView.findViewById(resId);
            if (imageView != null) {
                imageView.setOnClickListener(v -> handleLetterClick(letter, imageView));
            }
        }
    }

    private void handleLetterClick(String letter, ImageView source) {
        if (source != null) {
            createNewLetterView(source, letter);
        }
    }

    private void createNewLetterView(ImageView source, String letter) {
        // Tạo layout chứa hình ảnh và text
        LinearLayout itemLayout = new LinearLayout(getContext());
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 32); // Bottom margin 32dp
        itemLayout.setLayoutParams(layoutParams);
        itemLayout.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

        // Tạo ImageView mới
        ImageView newImageView = new ImageView(getContext());
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(250, 250);
        imageParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        newImageView.setLayoutParams(imageParams);
        newImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        newImageView.setImageDrawable(source.getDrawable());

        // Tạo TextView mới để hiển thị từ tiếng Anh
        TextView englishWordsView = new TextView(getContext());
        LinearLayout.LayoutParams englishParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        englishParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        englishParams.setMargins(0, 8, 0, 0); // Top margin 8dp
        englishWordsView.setLayoutParams(englishParams);
        englishWordsView.setText("Đang lấy từ tiếng Anh...");
        englishWordsView.setTextSize(16);
        englishWordsView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark));

        // Thêm sự kiện click để chỉ phát âm từ tiếng Anh
        englishWordsView.setOnClickListener(v -> {
            String word = englishWordsView.getText().toString();
            if (word.isEmpty() || word.equals("Đang lấy từ tiếng Anh...") || word.equals("Không lấy được từ tiếng Anh") || word.equals("Không có từ tiếng Anh phù hợp")) {
                return;
            }
            if (textToSpeech != null) {
                textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        // Thêm views vào layout
        itemLayout.addView(newImageView);
        itemLayout.addView(englishWordsView);

        // Thêm vào container
        letterContainer.addView(itemLayout);

        // Gọi API lấy từ tiếng Anh
        fetchEnglishWords(letter, englishWordsView);

        // Chạy animation
        playJumpAnimation(source, newImageView, englishWordsView);
    }

    private void playJumpAnimation(ImageView source, ImageView target, TextView infoText) {
        // Tạo ImageView tạm thời cho animation
        final ImageView tempImage = new ImageView(getContext());
        tempImage.setImageDrawable(source.getDrawable());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                source.getWidth(),
                source.getHeight());
        tempImage.setLayoutParams(params);
        ((ViewGroup) source.getParent()).addView(tempImage);

        // Ẩn ảnh gốc tạm thời
        source.setVisibility(View.INVISIBLE);

        // Lấy vị trí trên màn hình
        int[] sourceLoc = new int[2];
        int[] targetLoc = new int[2];
        source.getLocationOnScreen(sourceLoc);
        target.getLocationOnScreen(targetLoc);

        // Thiết lập animation
        ObjectAnimator moveX = ObjectAnimator.ofFloat(tempImage, "translationX",
                0, targetLoc[0] - sourceLoc[0]);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(tempImage, "translationY",
                0, targetLoc[1] - sourceLoc[1]);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(tempImage, "scaleX", 1f, 1.5f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(tempImage, "scaleY", 1f, 1.5f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(moveX, moveY, scaleX, scaleY);
        animatorSet.setDuration(800);

        animatorSet.start();

        // Sau khi animation hoàn thành
        tempImage.postDelayed(() -> {
            // Hiển thị text với hiệu ứng fade in
            infoText.animate().alpha(1f).setDuration(300).start();

            // Dọn dẹp temp image
            ((ViewGroup) tempImage.getParent()).removeView(tempImage);
            source.setVisibility(View.VISIBLE);
        }, 800);
    }

    private void fetchEnglishWords(String letter, TextView resultView) {
        String url = "https://api.datamuse.com/words?sp=" + letter.toLowerCase() + "*&max=10";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    if (response.length() > 0) {
                        // Chọn ngẫu nhiên 1 từ trong danh sách trả về
                        int randomIndex = (int) (Math.random() * response.length());
                        try {
                            JSONObject obj = response.getJSONObject(randomIndex);
                            String word = obj.getString("word");
                            resultView.setText(word);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            resultView.setText("Không lấy được từ tiếng Anh");
                        }
                    } else {
                        resultView.setText("Không có từ tiếng Anh phù hợp");
                    }
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                resultView.setText("Không lấy được từ tiếng Anh");
            }
        });

        queue.add(jsonArrayRequest);
    }
} 