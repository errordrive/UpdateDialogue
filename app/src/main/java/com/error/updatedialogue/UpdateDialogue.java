package com.error.updatedialogue;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Animated Update Dialogue that can be called directly from MainActivity.onCreate().
 *
 * Usage:
 *   UpdateDialogue.checkAndShow(this);
 *   // Or show directly without waiting for remote JSON:
 *   UpdateDialogue.show(this, "New Update!", "Version 2.0 is out.", "UPDATE NOW", "https://example.com");
 */
public class UpdateDialogue {
    private static final String JSON_URL = "https://pastebin.com/raw/YJVbZS65";
    private static Dialog mDialog;

    /**
     * Fetches update config from JSON_URL on a background thread and displays
     * the animated dialogue if "isVisible" is true. Safe to call from Activity.onCreate().
     */
    public static void checkAndShow(final Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String jsonResponse = fetchJson(JSON_URL);
                    if (jsonResponse != null) {
                        JSONObject json = new JSONObject(jsonResponse);
                        if (json.optBoolean("isVisible", false)) {
                            final String title = json.optString("title", "New Update!");
                            final String message = json.optString("message", "Update available.");
                            final String updateUrl = json.optString("updateUrl", "");
                            final String btnText = json.optString("buttonText", "TRY NOW");
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    show(activity, title, message, btnText, updateUrl);
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    executor.shutdown();
                }
            }
        });
    }

    /**
     * Displays the animated update dialogue immediately with the provided content.
     * Safe to call from Activity.onCreate() on the main thread.
     */
    @SuppressLint("ClickableViewAccessibility")
    public static void show(
            final Activity activity,
            String titleText,
            String messageText,
            String buttonText,
            final String url
    ) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        dismiss();

        Dialog dialog = new Dialog(activity);
        mDialog = dialog;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.6f);
        }

        // Card container
        final LinearLayout cardLayout = new LinearLayout(activity);
        cardLayout.setOrientation(LinearLayout.VERTICAL);
        cardLayout.setGravity(Gravity.CENTER);
        int padding = dpToPx(activity, 32);
        cardLayout.setPadding(padding, padding, padding, padding);

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(dpToPx(activity, 24));
        cardLayout.setBackground(cardBg);

        // Original winking & sparkling face view (with animations)
        final PerfectEmojiView iconView = new PerfectEmojiView(activity);
        int iconSize = dpToPx(activity, 72);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
        iconParams.gravity = Gravity.CENTER_HORIZONTAL;
        iconParams.bottomMargin = dpToPx(activity, 16);
        iconView.setLayoutParams(iconParams);

        // Title
        final TextView titleView = new TextView(activity);
        titleView.setText(titleText);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22.0f);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setTextColor(Color.parseColor("#202124"));
        titleView.setGravity(Gravity.CENTER);

        // Message
        final TextView msgView = new TextView(activity);
        msgView.setText(messageText);
        msgView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f);
        msgView.setTextColor(Color.parseColor("#5F6368"));
        msgView.setGravity(Gravity.CENTER);
        msgView.setLineSpacing(0.0f, 1.2f);
        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        msgParams.setMargins(0, dpToPx(activity, 8), 0, dpToPx(activity, 32));
        msgView.setLayoutParams(msgParams);

        // Action button
        final Button actionBtn = new Button(activity);
        actionBtn.setText(buttonText);
        actionBtn.setTextColor(Color.WHITE);
        actionBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f);
        actionBtn.setTypeface(Typeface.DEFAULT_BOLD);
        actionBtn.setAllCaps(true);
        actionBtn.setStateListAnimator(null);

        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(Color.parseColor("#202124"));
        btnBg.setCornerRadius(dpToPx(activity, 50));
        actionBtn.setBackground(btnBg);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(activity, 55)
        );
        actionBtn.setLayoutParams(btnParams);

        // Tactile press animation on button
        actionBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(90).start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1.0f).scaleY(1.0f)
                                .setInterpolator(new OvershootInterpolator(2.0f))
                                .setDuration(180)
                                .start();
                        break;
                }
                return false;
            }
        });

        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (url != null) {
                    String targetUrl = url.trim();
                    if (!targetUrl.isEmpty()) {
                        if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
                            targetUrl = "https://" + targetUrl;
                        }
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            activity.startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        cardLayout.addView(iconView);
        cardLayout.addView(titleView);
        cardLayout.addView(msgView);
        cardLayout.addView(actionBtn);

        // Prepare entrance animation initial states
        cardLayout.setAlpha(0f);
        cardLayout.setScaleX(0.84f);
        cardLayout.setScaleY(0.84f);
        cardLayout.setTranslationY(dpToPx(activity, 24));

        iconView.setAlpha(0f);
        iconView.setScaleX(0.5f);
        iconView.setScaleY(0.5f);

        prepareSlideFade(titleView, dpToPx(activity, 12));
        prepareSlideFade(msgView, dpToPx(activity, 14));
        prepareSlideFade(actionBtn, dpToPx(activity, 16));

        dialog.setContentView(cardLayout);

        if (window != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.85d);
            try {
                dialog.show();
                window.setAttributes(lp);
                playEntranceAnimation(cardLayout, iconView, titleView, msgView, actionBtn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Dismisses the currently displayed dialogue if any.
     */
    public static void dismiss() {
        Dialog dialog = mDialog;
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception ignored) {
            }
        }
        mDialog = null;
    }

    private static void prepareSlideFade(View view, float translationY) {
        view.setAlpha(0f);
        view.setTranslationY(translationY);
    }

    private static void playEntranceAnimation(
            View card,
            View icon,
            View title,
            View message,
            View button
    ) {
        // Card pop-in
        ObjectAnimator cardAlpha = ObjectAnimator.ofFloat(card, View.ALPHA, 0f, 1f);
        ObjectAnimator cardScaleX = ObjectAnimator.ofFloat(card, View.SCALE_X, 0.84f, 1f);
        ObjectAnimator cardScaleY = ObjectAnimator.ofFloat(card, View.SCALE_Y, 0.84f, 1f);
        ObjectAnimator cardTransY = ObjectAnimator.ofFloat(card, View.TRANSLATION_Y, card.getTranslationY(), 0f);

        AnimatorSet cardSet = new AnimatorSet();
        cardSet.playTogether(cardAlpha, cardScaleX, cardScaleY, cardTransY);
        cardSet.setDuration(360);
        cardSet.setInterpolator(new OvershootInterpolator(1.25f));

        // Icon pop
        AnimatorSet iconSet = new AnimatorSet();
        iconSet.playTogether(
                ObjectAnimator.ofFloat(icon, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(icon, View.SCALE_X, 0.5f, 1f),
                ObjectAnimator.ofFloat(icon, View.SCALE_Y, 0.5f, 1f)
        );
        iconSet.setStartDelay(90);
        iconSet.setDuration(360);
        iconSet.setInterpolator(new OvershootInterpolator(1.8f));

        // Staggered text & button reveal
        AnimatorSet titleSet = createSlideFadeIn(title, 140, 260);
        AnimatorSet msgSet = createSlideFadeIn(message, 200, 260);
        AnimatorSet btnSet = createSlideFadeIn(button, 260, 280);

        AnimatorSet full = new AnimatorSet();
        full.playTogether(cardSet, iconSet, titleSet, msgSet, btnSet);
        full.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startButtonAttentionPulse(button);
            }
        });
        full.start();
    }

    private static void startButtonAttentionPulse(final View button) {
        ObjectAnimator pulseX = ObjectAnimator.ofFloat(button, View.SCALE_X, 1.0f, 1.035f, 1.0f);
        ObjectAnimator pulseY = ObjectAnimator.ofFloat(button, View.SCALE_Y, 1.0f, 1.035f, 1.0f);
        pulseX.setRepeatCount(ValueAnimator.INFINITE);
        pulseY.setRepeatCount(ValueAnimator.INFINITE);
        pulseX.setRepeatMode(ValueAnimator.RESTART);
        pulseY.setRepeatMode(ValueAnimator.RESTART);

        AnimatorSet pulseSet = new AnimatorSet();
        pulseSet.playTogether(pulseX, pulseY);
        pulseSet.setDuration(1600);
        pulseSet.setStartDelay(800);
        pulseSet.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseSet.start();
    }

    private static AnimatorSet createSlideFadeIn(View view, long delayMs, long durationMs) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view.getTranslationY(), 0f)
        );
        set.setStartDelay(delayMs);
        set.setDuration(durationMs);
        set.setInterpolator(new DecelerateInterpolator());
        return set;
    }

    /**
     * Original PerfectEmojiView face design with added animations:
     * - Periodic natural eye blink (including double-blink)
     * - Gentle floating motion
     * - Twinkling & rotating gold sparkles
     */
    private static class PerfectEmojiView extends View {
        private final int COLOR_FACE = Color.parseColor("#FFCC4D");
        private final int COLOR_FEATURE = Color.parseColor("#664500");
        private final int COLOR_SPARKLE = Color.parseColor("#FFAC33");

        private Paint yellowPaint;
        private Paint brownPaint;
        private Paint goldPaint;
        private Path path;
        private RectF rect;

        // Animated properties
        private float eyeOpenFactor = 1.0f;
        private float floatPhase = 0.0f;
        private float sparklePhase = 0.0f;

        private ValueAnimator floatAnimator;
        private ValueAnimator sparkleAnimator;
        private ValueAnimator blinkAnimator;
        private final Handler blinkHandler = new Handler(Looper.getMainLooper());
        private int blinkCounter = 0;

        private final Runnable blinkRunnable = new Runnable() {
            @Override
            public void run() {
                blinkCounter++;
                boolean doubleBlink = (blinkCounter % 3 == 0);
                triggerBlink(doubleBlink);
                blinkHandler.postDelayed(this, 2600);
            }
        };

        public PerfectEmojiView(Context context) {
            super(context);
            init();
        }

        private void init() {
            yellowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            yellowPaint.setColor(COLOR_FACE);
            yellowPaint.setStyle(Paint.Style.FILL);

            brownPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            brownPaint.setColor(COLOR_FEATURE);
            brownPaint.setStyle(Paint.Style.FILL);
            brownPaint.setStrokeCap(Paint.Cap.ROUND);

            goldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            goldPaint.setColor(COLOR_SPARKLE);
            goldPaint.setStyle(Paint.Style.FILL);

            path = new Path();
            rect = new RectF();
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            startAnimations();
        }

        @Override
        protected void onDetachedFromWindow() {
            stopAnimations();
            super.onDetachedFromWindow();
        }

        private void startAnimations() {
            stopAnimations();

            floatAnimator = ValueAnimator.ofFloat(0f, (float) (Math.PI * 2.0));
            floatAnimator.setDuration(2400);
            floatAnimator.setRepeatCount(ValueAnimator.INFINITE);
            floatAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            floatAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    floatPhase = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            floatAnimator.start();

            sparkleAnimator = ValueAnimator.ofFloat(0f, (float) (Math.PI * 2.0));
            sparkleAnimator.setDuration(1800);
            sparkleAnimator.setRepeatCount(ValueAnimator.INFINITE);
            sparkleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    sparklePhase = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            sparkleAnimator.start();

            blinkHandler.postDelayed(blinkRunnable, 900);
        }

        private void stopAnimations() {
            blinkHandler.removeCallbacks(blinkRunnable);
            if (floatAnimator != null) {
                floatAnimator.cancel();
                floatAnimator = null;
            }
            if (sparkleAnimator != null) {
                sparkleAnimator.cancel();
                sparkleAnimator = null;
            }
            if (blinkAnimator != null) {
                blinkAnimator.cancel();
                blinkAnimator = null;
            }
        }

        private void triggerBlink(final boolean doubleBlink) {
            if (blinkAnimator != null && blinkAnimator.isRunning()) {
                blinkAnimator.cancel();
            }
            blinkAnimator = ValueAnimator.ofFloat(1.0f, 0.06f, 1.0f);
            blinkAnimator.setDuration(190);
            blinkAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            blinkAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    eyeOpenFactor = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            if (doubleBlink) {
                blinkAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        blinkHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                triggerBlink(false);
                            }
                        }, 110);
                    }
                });
            }
            blinkAnimator.start();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            if (w <= 0 || h <= 0) {
                return;
            }

            float cx = w / 2.0f;
            float bobOffset = (float) Math.sin(floatPhase) * dpToPx(2.0f);
            float cy = (h / 2.0f) + bobOffset;
            float faceRadius = Math.min(w, h) / 2.5f;

            // Original yellow face circle
            canvas.drawCircle(cx, cy, faceRadius, yellowPaint);

            // Eyes
            float eyeY = cy - (faceRadius * 0.15f);
            float eyeOffset = faceRadius * 0.35f;
            float eyeW = 0.15f * faceRadius;
            float eyeH = faceRadius * 0.22f * eyeOpenFactor;

            // Right eye (blinks)
            brownPaint.setStyle(Paint.Style.FILL);
            rect.set((cx + eyeOffset) - eyeW, eyeY - eyeH, cx + eyeOffset + eyeW, eyeY + eyeH);
            canvas.drawOval(rect, brownPaint);

            // Left eye (original winking arc)
            brownPaint.setStyle(Paint.Style.STROKE);
            brownPaint.setStrokeWidth(faceRadius * 0.08f);
            float winkRadius = 0.18f * faceRadius;
            rect.set((cx - eyeOffset) - winkRadius, eyeY - winkRadius, (cx - eyeOffset) + winkRadius, eyeY + winkRadius);
            path.reset();
            path.addArc(rect, 180.0f, 180.0f);
            path.offset(0.0f, 0.05f * faceRadius);
            canvas.drawPath(path, brownPaint);

            // Original smiling mouth
            brownPaint.setStyle(Paint.Style.FILL);
            float mouthRadius = 0.5f * faceRadius;
            rect.set(cx - mouthRadius, cy - (0.2f * mouthRadius), cx + mouthRadius, cy + (1.2f * mouthRadius));
            path.reset();
            path.addArc(rect, 30.0f, 120.0f);
            brownPaint.setStyle(Paint.Style.STROKE);
            brownPaint.setStrokeWidth(0.08f * faceRadius);
            canvas.drawPath(path, brownPaint);
            brownPaint.setStyle(Paint.Style.FILL);

            // Original two gold sparkles with twinkle animation
            float s1Scale = 0.65f + 0.45f * (float) Math.sin(sparklePhase);
            float s1Rot = 12.0f * (float) Math.sin(sparklePhase);
            drawSparkle(canvas, (1.1f * faceRadius) + cx, cy - (0.9f * faceRadius), 0.35f * faceRadius * s1Scale, s1Rot);

            float s2Scale = 0.65f + 0.45f * (float) Math.sin(sparklePhase + Math.PI);
            float s2Rot = -12.0f * (float) Math.sin(sparklePhase);
            drawSparkle(canvas, cx - (1.0f * faceRadius), cy - (0.8f * faceRadius), 0.22f * faceRadius * s2Scale, s2Rot);
        }

        private void drawSparkle(Canvas canvas, float cx, float cy, float size, float rotationDeg) {
            if (size <= 0.5f) {
                return;
            }
            float halfSize = size / 2.0f;
            canvas.save();
            canvas.rotate(rotationDeg, cx, cy);
            path.reset();
            path.moveTo(cx, cy - halfSize);
            path.quadTo(cx, cy, cx + halfSize, cy);
            path.quadTo(cx, cy, cx, cy + halfSize);
            path.quadTo(cx, cy, cx - halfSize, cy);
            path.quadTo(cx, cy, cx, cy - halfSize);
            path.close();
            canvas.drawPath(path, goldPaint);
            canvas.restore();
        }

        private float dpToPx(float dp) {
            return TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dp,
                    getResources().getDisplayMetrics()
            );
        }
    }

    private static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

    private static String fetchJson(String urlString) {
        StringBuilder result = new StringBuilder();
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
