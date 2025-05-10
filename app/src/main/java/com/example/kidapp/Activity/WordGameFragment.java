package com.example.kidapp.Activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.kidapp.R;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.HashMap;
import java.util.Map;
import android.view.DragEvent;

public class WordGameFragment extends Fragment {
    private LinearLayout llAllLetters, llDropZone;
    private TextView textViewResult;
    private ImageView imageView;
    private OkHttpClient client = new OkHttpClient();
    private String unsplashAccessKey = "oJ2DtNWMv7bbk5-ALu1k_mFwvDjnUpKsGB_iINh1Wv4";
    private Map<String, Integer> letterImageMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_game, container, false);
        llAllLetters = view.findViewById(R.id.llAllLetters);
        llDropZone = view.findViewById(R.id.llDropZone);
        textViewResult = view.findViewById(R.id.textViewResult);
        imageView = view.findViewById(R.id.imageView);

        setupLetterResources();
        setupAllLetters();

        // Xóa nút kiểm tra nếu có
        Button btnCheck = view.findViewById(R.id.btnCheck);
        if (btnCheck != null) btnCheck.setVisibility(View.GONE);

        return view;
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

    private void setupAllLetters() {
        llAllLetters.removeAllViews();
        llDropZone.removeAllViews();
        for (char c = 'A'; c <= 'Z'; c++) {
            ImageView iv = createLetterImageView(String.valueOf(c));
            llAllLetters.addView(iv);
        }
        llDropZone.setOnDragListener(dropZoneDragListener);
    }

    private void addLetterToDropZone(String letter) {
        ImageView newIv = new ImageView(getContext());
        newIv.setImageResource(letterImageMap.get(letter));
        newIv.setTag(letter);
        newIv.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        newIv.setOnClickListener(v2 -> {
            llDropZone.removeView(newIv);
            checkWordAuto();
        });
        llDropZone.addView(newIv);
        checkWordAuto();
    }

    private ImageView createLetterImageView(String letter) {
        ImageView iv = new ImageView(getContext());
        iv.setImageResource(letterImageMap.get(letter));
        iv.setTag(letter);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
        params.setMargins(8, 8, 8, 8);
        iv.setLayoutParams(params);
        iv.setOnLongClickListener(v -> {
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(iv);
            v.startDragAndDrop(null, shadowBuilder, letter, 0);
            return true;
        });
        iv.setOnClickListener(v -> addLetterToDropZone(letter));
        return iv;
    }

    private final View.OnDragListener dropZoneDragListener = (v, event) -> {
        switch (event.getAction()) {
            case DragEvent.ACTION_DROP:
                String letter = (String) event.getLocalState();
                addLetterToDropZone(letter);
                break;
        }
        return true;
    };

    private void checkWordAuto() {
        int count = llDropZone.getChildCount();
        if (count < 3) {
            textViewResult.setText("");
            imageView.setImageDrawable(null);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            ImageView iv = (ImageView) llDropZone.getChildAt(i);
            sb.append(iv.getTag());
        }
        String userWord = sb.toString().toLowerCase();
        checkWordExists(userWord);
    }

    private void checkWordExists(String word) {
        String url = "https://api.dictionaryapi.dev/api/v2/entries/en/" + word;
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() -> textViewResult.setText("Lỗi mạng!"));
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> textViewResult.setText("Wow!"));
                    getUnsplashImage(word);
                } else {
                    requireActivity().runOnUiThread(() -> {
                        textViewResult.setText("Không phải từ có thật!");
                        imageView.setImageDrawable(null);
                    });
                }
            }
        });
    }

    private void getUnsplashImage(String keyword) {
        String url = "https://api.unsplash.com/search/photos?query=" + keyword + "&client_id=" + unsplashAccessKey + "&per_page=1";
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Lỗi tải ảnh!", Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        JSONArray results = jsonObject.getJSONArray("results");
                        if (results.length() > 0) {
                            String imageUrl = results.getJSONObject(0)
                                    .getJSONObject("urls")
                                    .getString("regular");
                            requireActivity().runOnUiThread(() -> Glide.with(getContext()).load(imageUrl).into(imageView));
                        } else {
                            requireActivity().runOnUiThread(() -> imageView.setImageResource(R.drawable.no_image));
                        }
                    } catch (JSONException e) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Lỗi xử lý dữ liệu ảnh!", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }
} 