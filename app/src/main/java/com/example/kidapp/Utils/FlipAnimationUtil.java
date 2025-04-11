package com.example.kidapp.Utils;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.example.kidapp.R;

public class FlipAnimationUtil {
    private AnimatorSet frontAnimation;
    private AnimatorSet backAnimation;
    private boolean isFront = true;
    private View frontView;
    private View backView;
    private Runnable autoFlipRunnable;
    private Handler handler;
    private long autoFlipDelay = 5000; // 5 giây

    public FlipAnimationUtil(Context context, View front, View back) {
        frontView = front;
        backView = back;

        // Đặt backView ban đầu ở trạng thái không hiển thị
        backView.setAlpha(0);

        // Khởi tạo animations từ resources
        frontAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_out);
        backAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_in);

        handler = new Handler(Looper.getMainLooper());
        autoFlipRunnable = () -> flipCard();
    }

    public void flipCard() {
        cancelAutoFlip();

        if (isFront) {
            frontAnimation.setTarget(frontView);
            backAnimation.setTarget(backView);
            frontAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    scheduleAutoFlip();
                }
            });
            frontAnimation.start();
            backAnimation.start();
            isFront = false;
        } else {
            // Tương tự cho chiều ngược lại
            frontAnimation.setTarget(backView);
            backAnimation.setTarget(frontView);
            backAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    scheduleAutoFlip();
                }
            });
            backAnimation.start();
            frontAnimation.start();
            isFront = true;
        }
    }

    private void scheduleAutoFlip() {
        handler.postDelayed(autoFlipRunnable, autoFlipDelay);
    }

    public void cancelAutoFlip() {
        handler.removeCallbacks(autoFlipRunnable);
    }
    public boolean isFront() {
        return isFront;
    }
}
