package com.example.kidapp.Activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.kidapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WordGameFragment extends Fragment {
    private LinearLayout llAllLetters, llDropZone;
    private TextView textViewResult;
    private ImageView imageView;
    private Button btnReset;
    private LottieAnimationView lottieSuccess;
    private OkHttpClient client = new OkHttpClient();
    private String unsplashAccessKey = "oJ2DtNWMv7bbk5-ALu1k_mFwvDjnUpKsGB_iINh1Wv4";
    private Map<String, Integer> letterImageMap;
    private int[] backgroundColors = {
            R.color.cool_mint  ,
            R.color.pastel_pink,
            R.color.pastel_cyan,
            R.color.beige_light,
            R.color.off_white
    };
    private Random random = new Random();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_game, container, false);

        llAllLetters = view.findViewById(R.id.llAllLetters);
        llDropZone = view.findViewById(R.id.llDropZone);
        textViewResult = view.findViewById(R.id.textViewResult);
        imageView = view.findViewById(R.id.imageView);
        btnReset = view.findViewById(R.id.btnReset);

        // Initialize the Lottie animation view for success celebration
        if (view.findViewById(R.id.lottieSuccess) != null) {
            lottieSuccess = view.findViewById(R.id.lottieSuccess);
        }

        setupLetterResources();
        setupAllLetters();

        btnReset.setOnClickListener(v -> {
            animateButton(btnReset);
            resetGame();
        });

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
        for (char c = 'A'; c <= 'Z'; c++) {
            ImageView iv = createLetterImageView(String.valueOf(c));
            llAllLetters.addView(iv);

            // Animation for letters appearing
            animateLetterAppear(iv);
        }

        llDropZone.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.DragEvent.ACTION_DROP:
                    String letter = (String) event.getLocalState();
                    addLetterToDropZone(letter);
                    break;
            }
            return true;
        });
    }

    private void animateLetterAppear(View view) {
        view.setScaleX(0);
        view.setScaleY(0);
        view.setAlpha(0);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1.2f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(scaleX, scaleY, alpha);
        animSet.setInterpolator(new BounceInterpolator());
        animSet.setDuration(500);
        animSet.setStartDelay(random.nextInt(300));
        animSet.start();
    }

    private void animateButton(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f, 1f);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(scaleX, scaleY);
        animSet.setDuration(300);
        animSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animSet.start();
    }

    private void resetGame() {
        llDropZone.removeAllViews();
        textViewResult.setText("");
        imageView.setImageDrawable(null);
        if (lottieSuccess != null) {
            lottieSuccess.cancelAnimation();
            lottieSuccess.setVisibility(View.INVISIBLE);
        }
    }

    private void addLetterToDropZone(String letter) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(65),
                dpToPx(65)
        );
        params.setMargins(10, 10, 10, 10);

        ImageView newIv = new ImageView(getContext());
        newIv.setImageResource(letterImageMap.get(letter));
        newIv.setTag(letter);
        newIv.setLayoutParams(params);
        newIv.setBackgroundResource(R.drawable.letter_drop_bg);

        // Animation for entering the drop zone
        newIv.setScaleX(1.5f);
        newIv.setScaleY(1.5f);
        newIv.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new BounceInterpolator())
                .start();

        newIv.setOnClickListener(v -> {
            animateLetterRemove(newIv);
        });

        llDropZone.addView(newIv);
        checkWordAuto();
    }

    private void animateLetterRemove(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(scaleX, scaleY, alpha);
        animSet.setDuration(300);
        animSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animSet.start();

        animSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                llDropZone.removeView(view);
                checkWordAuto();
            }
        });
    }

    private ImageView createLetterImageView(String letter) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(65),
                dpToPx(65)
        );
        params.setMargins(8, 8, 8, 8);

        ImageView iv = new ImageView(getContext());
        iv.setImageResource(letterImageMap.get(letter));
        iv.setTag(letter);
        iv.setLayoutParams(params);

        // Set random colorful background for each letter
        iv.setBackgroundResource(R.drawable.letter_bg);
        iv.getBackground().setTint(ContextCompat.getColor(requireContext(),
                backgroundColors[random.nextInt(backgroundColors.length)]));

        iv.setOnLongClickListener(v -> {
            android.view.View.DragShadowBuilder shadowBuilder = new android.view.View.DragShadowBuilder(iv);
            iv.startDragAndDrop(null, shadowBuilder, letter, 0);
            return true;
        });

        iv.setOnClickListener(v -> {
            animateButton(iv);
            addLetterToDropZone(letter);
        });

        return iv;
    }

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
                requireActivity().runOnUiThread(() ->
                        showError("Network Error! Check your connection"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> {
                        celebrateSuccess(word);
                        getUnsplashImage(word);
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        showNotAWord();
                        imageView.setImageDrawable(null);
                    });
                }
            }
        });
    }

    private void celebrateSuccess(String word) {
        textViewResult.setText("Amazing! You found a real word!");
        textViewResult.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));

        // Play celebration animation if available
        if (lottieSuccess != null) {
            lottieSuccess.setVisibility(View.VISIBLE);
            lottieSuccess.playAnimation();
        }

        // Animate the text result
        textViewResult.setScaleX(0);
        textViewResult.setScaleY(0);
        textViewResult.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setInterpolator(new BounceInterpolator())
                .start();
    }

    private void showNotAWord() {
        textViewResult.setText("Try again! Not a real word.");
        textViewResult.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));

        // Shake animation for the text
        ObjectAnimator animator = ObjectAnimator.ofFloat(textViewResult, "translationX",
                0, 15, -15, 15, -15, 10, -10, 5, -5, 0);
        animator.setDuration(500);
        animator.start();
    }

    private void showError(String message) {
        textViewResult.setText(message);
        textViewResult.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
    }

    private void getUnsplashImage(String keyword) {
        String url = "https://api.unsplash.com/search/photos?query=" + keyword + "&client_id=" + unsplashAccessKey + "&per_page=1";
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Couldn't load image!", Toast.LENGTH_SHORT).show());
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

                            requireActivity().runOnUiThread(() -> {
                                // Use Glide with animation for image loading
                                RequestOptions options = new RequestOptions()
                                        .centerCrop()
                                        .placeholder(R.drawable.no_image);

                                Glide.with(requireContext())
                                        .load(imageUrl)
                                        .apply(options)
                                        .transition(DrawableTransitionOptions.withCrossFade())
                                        .into(imageView);

                                // Animate the image view
                                imageView.setAlpha(0f);
                                imageView.animate()
                                        .alpha(1f)
                                        .setDuration(500)
                                        .start();
                            });
                        } else {
                            requireActivity().runOnUiThread(() ->
                                    imageView.setImageResource(R.drawable.no_image));
                        }
                    } catch (JSONException e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Error processing image data!", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    // Utility method to convert dp to pixels
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}