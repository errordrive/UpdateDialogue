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
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
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
import android.widget.FrameLayout;
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
 * Polished, animated Update Dialogue that can be called directly from MainActivity.onCreate().
 *
 * Usage:
 *   UpdateDialogue.checkAndShow(this);
 *   // Or show directly:
 *   UpdateDialogue.show(this, "New Update!", "Update available.", "UPDATE NOW", "https://example.com");
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
            window.setDimAmount(0.62f);
        }

        // Main card container
        final LinearLayout cardLayout = new LinearLayout(activity);
        cardLayout.setOrientation(LinearLayout.VERTICAL);
        cardLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        int padH = dpToPx(activity, 28);
        int padTop = dpToPx(activity, 28);
        int padBottom = dpToPx(activity, 26);
        cardLayout.setPadding(padH, padTop, padH, padBottom);

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(dpToPx(activity, 28));
        cardBg.setStroke(dpToPx(activity, 1), Color.parseColor("#ECEEF2"));
        cardLayout.setBackground(cardBg);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cardLayout.setElevation(dpToPx(activity, 18));
        }

        // Spotlight halo + original winking/sparkling face
        FrameLayout iconContainer = new FrameLayout(activity);
        int haloSize = dpToPx(activity, 100);
        LinearLayout.LayoutParams haloParams = new LinearLayout.LayoutParams(haloSize, haloSize);
        haloParams.gravity = Gravity.CENTER_HORIZONTAL;
        haloParams.bottomMargin = dpToPx(activity, 12);
        iconContainer.setLayoutParams(haloParams);

        final HaloGlowView haloView = new HaloGlowView(activity);
        haloView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        final PerfectEmojiView iconView = new PerfectEmojiView(activity);
        int iconSize = dpToPx(activity, 72);
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(iconSize, iconSize);
        iconParams.gravity = Gravity.CENTER;
        iconView.setLayoutParams(iconParams);

        iconContainer.addView(haloView);
        iconContainer.addView(iconView);

        // Subtle status pill badge
        final TextView badgeView = new TextView(activity);
        badgeView.setText("UPDATE AVAILABLE");
        badgeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        badgeView.setTypeface(Typeface.DEFAULT_BOLD);
        badgeView.setTextColor(Color.parseColor("#A66000"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            badgeView.setLetterSpacing(0.08f);
        }
        badgeView.setPadding(
                dpToPx(activity, 12),
                dpToPx(activity, 5),
                dpToPx(activity, 12),
                dpToPx(activity, 5)
        );
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(Color.parseColor("#FFF4D8"));
        badgeBg.setCornerRadius(dpToPx(activity, 50));
        badgeView.setBackground(badgeBg);
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        badgeParams.gravity = Gravity.CENTER_HORIZONTAL;
        badgeParams.bottomMargin = dpToPx(activity, 12);
        badgeView.setLayoutParams(badgeParams);

        // Title
        final TextView titleView = new TextView(activity);
        titleView.setText(titleText);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22.0f);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setTextColor(Color.parseColor("#18191B"));
        titleView.setGravity(Gravity.CENTER);

        // Message
        final TextView msgView = new TextView(activity);
        msgView.setText(messageText);
        msgView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
        msgView.setTextColor(Color.parseColor("#5A5F68"));
        msgView.setGravity(Gravity.CENTER);
        msgView.setLineSpacing(0.0f, 1.32f);
        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        msgParams.setMargins(dpToPx(activity, 4), dpToPx(activity, 10), dpToPx(activity, 4), dpToPx(activity, 28));
        msgView.setLayoutParams(msgParams);

        // Engaging Shimmer Pill Button (Download / Update / Join)
        final ShimmerButton actionBtn = new ShimmerButton(activity, buttonText);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(activity, 56)
        );
        actionBtn.setLayoutParams(btnParams);

        actionBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(85).start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1.0f).scaleY(1.0f)
                                .setInterpolator(new OvershootInterpolator(2.2f))
                                .setDuration(200)
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

        cardLayout.addView(iconContainer);
        cardLayout.addView(badgeView);
        cardLayout.addView(titleView);
        cardLayout.addView(msgView);
        cardLayout.addView(actionBtn);

        // Prepare entrance animation initial states
        cardLayout.setAlpha(0f);
        cardLayout.setScaleX(0.84f);
        cardLayout.setScaleY(0.84f);
        cardLayout.setTranslationY(dpToPx(activity, 26));

        iconContainer.setAlpha(0f);
        iconContainer.setScaleX(0.55f);
        iconContainer.setScaleY(0.55f);

        prepareSlideFade(badgeView, dpToPx(activity, 10));
        prepareSlideFade(titleView, dpToPx(activity, 12));
        prepareSlideFade(msgView, dpToPx(activity, 14));
        prepareSlideFade(actionBtn, dpToPx(activity, 18));

        dialog.setContentView(cardLayout);

        if (window != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.86d);
            try {
                dialog.show();
                window.setAttributes(lp);
                playEntranceAnimation(cardLayout, iconContainer, badgeView, titleView, msgView, actionBtn);
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
            View iconContainer,
            View badge,
            View title,
            View message,
            final View button
    ) {
        AnimatorSet cardSet = new AnimatorSet();
        cardSet.playTogether(
                ObjectAnimator.ofFloat(card, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(card, View.SCALE_X, 0.84f, 1f),
                ObjectAnimator.ofFloat(card, View.SCALE_Y, 0.84f, 1f),
                ObjectAnimator.ofFloat(card, View.TRANSLATION_Y, card.getTranslationY(), 0f)
        );
        cardSet.setDuration(380);
        cardSet.setInterpolator(new OvershootInterpolator(1.2f));

        AnimatorSet iconSet = new AnimatorSet();
        iconSet.playTogether(
                ObjectAnimator.ofFloat(iconContainer, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(iconContainer, View.SCALE_X, 0.55f, 1f),
                ObjectAnimator.ofFloat(iconContainer, View.SCALE_Y, 0.55f, 1f)
        );
        iconSet.setStartDelay(85);
        iconSet.setDuration(360);
        iconSet.setInterpolator(new OvershootInterpolator(1.75f));

        AnimatorSet badgeSet = createSlideFadeIn(badge, 125, 240);
        AnimatorSet titleSet = createSlideFadeIn(title, 165, 260);
        AnimatorSet msgSet = createSlideFadeIn(message, 215, 260);
        AnimatorSet btnSet = createSlideFadeIn(button, 270, 290);

        AnimatorSet full = new AnimatorSet();
        full.playTogether(cardSet, iconSet, badgeSet, titleSet, msgSet, btnSet);
        full.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startButtonAttentionPulse(button);
            }
        });
        full.start();
    }

    private static void startButtonAttentionPulse(final View button) {
        ObjectAnimator pulseX = ObjectAnimator.ofFloat(button, View.SCALE_X, 1.0f, 1.028f, 1.0f);
        ObjectAnimator pulseY = ObjectAnimator.ofFloat(button, View.SCALE_Y, 1.0f, 1.028f, 1.0f);
        pulseX.setRepeatCount(ValueAnimator.INFINITE);
        pulseY.setRepeatCount(ValueAnimator.INFINITE);

        AnimatorSet pulseSet = new AnimatorSet();
        pulseSet.playTogether(pulseX, pulseY);
        pulseSet.setDuration(1800);
        pulseSet.setStartDelay(900);
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
     * Soft breathing warm halo behind the face icon.
     */
    private static class HaloGlowView extends View {
        private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private ValueAnimator pulseAnimator;
        private float pulse = 0f;

        public HaloGlowView(Context context) {
            super(context);
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            pulseAnimator = ValueAnimator.ofFloat(0f, (float) (Math.PI * 2.0));
            pulseAnimator.setDuration(2400);
            pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
            pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            pulseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    pulse = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            pulseAnimator.start();
        }

        @Override
        protected void onDetachedFromWindow() {
            if (pulseAnimator != null) {
                pulseAnimator.cancel();
                pulseAnimator = null;
            }
            super.onDetachedFromWindow();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            if (w <= 0 || h <= 0) {
                return;
            }
            float cx = w / 2f;
            float cy = h / 2f;
            float baseRadius = Math.min(w, h) * 0.48f;
            float radius = baseRadius * (0.94f + 0.06f * (float) Math.sin(pulse));

            RadialGradient gradient = new RadialGradient(
                    cx, cy, radius,
                    new int[]{Color.parseColor("#44FFCC4D"), Color.parseColor("#18FFAC33"), Color.TRANSPARENT},
                    new float[]{0.0f, 0.55f, 1.0f},
                    Shader.TileMode.CLAMP
            );
            glowPaint.setShader(gradient);
            canvas.drawCircle(cx, cy, radius, glowPaint);
        }
    }

    /**
     * Pill-shaped gradient action button with a periodic diagonal light shimmer sweep.
     */
    private static class ShimmerButton extends FrameLayout {
        private final Paint shimmerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path clipPath = new Path();
        private final RectF boundsRect = new RectF();
        private final Matrix shimmerMatrix = new Matrix();
        private LinearGradient shimmerGradient;
        private ValueAnimator shimmerAnimator;
        private float shimmerProgress = -1f;

        public ShimmerButton(Context context, String text) {
            super(context);
            setWillNotDraw(false);
            setClickable(true);
            setFocusable(true);

            GradientDrawable bg = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{Color.parseColor("#2A2F3A"), Color.parseColor("#14171D")}
            );
            bg.setCornerRadius(dpToPx(context, 50));
            bg.setStroke(dpToPx(context, 1), Color.parseColor("#383E4B"));
            setBackground(bg);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setElevation(dpToPx(context, 6));
            }

            TextView label = new TextView(context);
            label.setText(text);
            label.setTextColor(Color.WHITE);
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
            label.setTypeface(Typeface.DEFAULT_BOLD);
            label.setAllCaps(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                label.setLetterSpacing(0.06f);
            }
            label.setGravity(Gravity.CENTER);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            label.setLayoutParams(lp);
            addView(label);
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            shimmerAnimator = ValueAnimator.ofFloat(-0.4f, 1.4f);
            shimmerAnimator.setDuration(1400);
            shimmerAnimator.setStartDelay(1200);
            shimmerAnimator.setRepeatCount(ValueAnimator.INFINITE);
            shimmerAnimator.setRepeatMode(ValueAnimator.RESTART);
            shimmerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            shimmerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    shimmerProgress = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            shimmerAnimator.start();
        }

        @Override
        protected void onDetachedFromWindow() {
            if (shimmerAnimator != null) {
                shimmerAnimator.cancel();
                shimmerAnimator = null;
            }
            super.onDetachedFromWindow();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            boundsRect.set(0, 0, w, h);
            float radius = h / 2f;
            clipPath.reset();
            clipPath.addRoundRect(boundsRect, radius, radius, Path.Direction.CW);
            shimmerGradient = new LinearGradient(
                    0, 0, w * 0.35f, h * 0.25f,
                    new int[]{Color.TRANSPARENT, Color.parseColor("#32FFFFFF"), Color.TRANSPARENT},
                    new float[]{0f, 0.5f, 1f},
                    Shader.TileMode.CLAMP
            );
            shimmerPaint.setShader(shimmerGradient);
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
            if (shimmerGradient != null && shimmerProgress >= -0.35f && shimmerProgress <= 1.35f) {
                canvas.save();
                canvas.clipPath(clipPath);
                float dx = shimmerProgress * getWidth();
                shimmerMatrix.setTranslate(dx, 0);
                shimmerGradient.setLocalMatrix(shimmerMatrix);
                canvas.drawRect(boundsRect, shimmerPaint);
                canvas.restore();
            }
        }
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
