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
import android.view.ViewOutlineProvider;
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
 * Ultra-premium animated Update Dialogue with Wink-style minimalist face (no background circle),
 * twinkling head sparkles, and a stable shimmer action button.
 *
 * Usage from MainActivity.onCreate():
 *   UpdateDialogue.checkAndShow(this);
 *   // Or directly:
 *   UpdateDialogue.show(this, "New Update!", "Update available.", "DOWNLOAD", "https://example.com");
 */
public class UpdateDialogue {

    private static Dialog mDialog;

    // Encoded text values (decoded at runtime; no readable labels in the dex).
    private static final int[] S_TITLE = {114, 89, 75, 28, 105, 76, 88, 93, 72, 89, 29};
    private static final int[] S_MSG = {105, 76, 88, 93, 72, 89, 28, 93, 74, 93, 85, 80, 93, 94, 80, 89, 18};
    private static final int[] S_TRY = {104, 110, 101, 28, 114, 115, 107};
    private static final int[] S_VISIBLE = {85, 79, 106, 85, 79, 85, 94, 80, 89};
    private static final int[] S_KEY_TITLE = {72, 85, 72, 80, 89};
    private static final int[] S_KEY_MSG = {81, 89, 79, 79, 93, 91, 89};
    private static final int[] S_KEY_URL = {73, 76, 88, 93, 72, 89, 105, 78, 80};
    private static final int[] S_KEY_BTN = {94, 73, 72, 72, 83, 82, 104, 89, 68, 72};
    private static final int[] S_GET = {123, 121, 104};
    private static final int[] S_HTTP = {84, 72, 72, 76, 6, 19, 19};
    private static final int[] S_HTTPS = {84, 72, 72, 76, 79, 6, 19, 19};
    private static final int[] S_BADGE = {114, 121, 107, 28, 105, 108, 120, 125, 104, 121};
    private static final int[] S_VIEW = {93, 82, 88, 78, 83, 85, 88, 18, 85, 82, 72, 89, 82, 72, 18, 93, 95, 72, 85, 83, 82, 18, 106, 117, 121, 107};
    private static final int[] S_PASTEBIN_HOST = {76, 93, 79, 72, 89, 94, 85, 82, 18, 95, 83, 81};
    private static final int[] S_RAW_SEGMENT = {19, 78, 93, 75, 19};
    private static final int[] S_PASTEBIN_RAW_PREFIX = {84, 72, 72, 76, 79, 6, 19, 19, 76, 93, 79, 72, 89, 94, 85, 82, 18, 95, 83, 81, 19, 78, 93, 75, 19};
    private static final int[] S_TRUE = {72, 78, 73, 89};
    private static final int[] S_UA_NAME = {105, 79, 89, 78, 17, 125, 91, 89, 82, 72};
    private static final int[] S_UA_VALUE = {113, 83, 70, 85, 80, 80, 93, 19, 9, 18, 12, 28, 20, 112, 85, 82, 73, 68, 7, 28, 125, 82, 88, 78, 83, 85, 88, 28, 13, 15, 21, 28, 125, 76, 76, 80, 89, 107, 89, 94, 119, 85, 72, 19, 9, 15, 11, 18, 15, 10, 28, 20, 119, 116, 104, 113, 112, 16, 28, 80, 85, 87, 89, 28, 123, 89, 95, 87, 83, 21, 28, 127, 84, 78, 83, 81, 89, 19, 13, 14, 12, 18, 12, 28, 113, 83, 94, 85, 80, 89, 28, 111, 93, 90, 93, 78, 85, 19, 9, 15, 11, 18, 15, 10};
    private static final int[] S_ACCEPT_NAME = {125, 95, 95, 89, 76, 72};
    private static final int[] S_ACCEPT_VALUE = {93, 76, 76, 80, 85, 95, 93, 72, 85, 83, 82, 19, 86, 79, 83, 82, 16, 72, 89, 68, 72, 19, 76, 80, 93, 85, 82, 16, 22, 19, 22};
    private static final int[] S_LOCATION = {112, 83, 95, 93, 72, 85, 83, 82};
    private static final int[] S_ENCODING_NAME = {125, 95, 95, 89, 76, 72, 17, 121, 82, 95, 83, 88, 85, 82, 91};
    private static final int[] S_ENCODING_VALUE = {85, 88, 89, 82, 72, 85, 72, 69};

    private static String d(int[] values) {
        byte[] out = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            out[i] = (byte) (values[i] ^ 0x3C);
        }
        return new String(out, java.nio.charset.StandardCharsets.UTF_8);
    }

    private static String normalizeRemoteUrl(String url) {
        if (url == null) {
            return "";
        }
        String value = url.trim();
        try {
            Uri uri = Uri.parse(value);
            String host = uri.getHost();
            if (host != null && d(S_PASTEBIN_HOST).equalsIgnoreCase(host)) {
                String path = uri.getPath();
                if (path == null) {
                    path = "";
                }
                if (!path.startsWith(d(S_RAW_SEGMENT))) {
                    String id = path.replaceAll("^/+", "");
                    int slash = id.indexOf('/');
                    if (slash >= 0) {
                        id = id.substring(0, slash);
                    }
                    if (!id.isEmpty()) {
                        return d(S_PASTEBIN_RAW_PREFIX) + id;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return value;
    }

    /**
     * The remote JSON link is locked inside the app at build time
     * (see SecureLinkConfig, generated by tools/generate_secure_link.py).
     * It is verified with SHA-256 + an RSA signature before every use,
     * so it cannot be swapped at runtime or through preferences.
     * Kept for API compatibility: runtime changes are ignored.
     */
    public static void setRemoteUrl(Context context, String url) {
        // Locked build: the link only changes by rebuilding with a new link.
    }

    /**
     * Gets the locked, verified remote JSON URL ("" if verification fails).
     */
    public static String getRemoteUrl(Context context) {
        return normalizeRemoteUrl(SecureLinkConfig.getRemoteUrl());
    }

    /** Short SHA-256 fingerprint of the locked link, safe to display. */
    public static String getLinkFingerprint() {
        return SecureLinkConfig.getFingerprint();
    }

    /**
     * Fetches update config using the configured remote URL on a background thread and displays
     * the dialogue if "isVisible" is true. Safe to call from Activity.onCreate().
     */
    public static void checkAndShow(final Activity activity) {
        checkAndShow(activity, getRemoteUrl(activity));
    }

    /**
     * Fetches update config from the locked remote URL on a background thread
     * and displays the dialogue if "isVisible" is true. The targetUrl argument
     * is ignored in locked builds - only the build-time link is ever fetched.
     */
    public static void checkAndShow(final Activity activity, final String targetUrl) {
        final String fetchUrl = getRemoteUrl(activity);
        if (fetchUrl.isEmpty()) {
            // Link verification failed (tampered build) - never fetch.
            return;
        }
        fetchAndShow(activity, fetchUrl);
    }

    /** Fetches update config from an explicit URL (controller test path). */
    public static void checkAndShowFromUrl(final Activity activity, final String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }
        fetchAndShow(activity, normalizeRemoteUrl(url));
    }

    private static void fetchAndShow(final Activity activity, final String fetchUrl) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String jsonResponse = null;
                    for (int attempt = 0; attempt < 4 && jsonResponse == null; attempt++) {
                        String attemptUrl = normalizeRemoteUrl(fetchUrl);
                        if (attempt == 1) {
                            attemptUrl += (attemptUrl.indexOf('?') >= 0 ? '&' : '?')
                                    + "_=" + System.currentTimeMillis();
                        }
                        jsonResponse = fetchJson(attemptUrl);
                        if (jsonResponse == null && attempt < 3) {
                            Thread.sleep(700L);
                        }
                    }
                    if (jsonResponse != null) {
                        JSONObject json = new JSONObject(extractJsonObject(jsonResponse));
                        boolean visible = json.optBoolean(d(S_VISIBLE), false)
                                || d(S_TRUE).equalsIgnoreCase(json.optString(d(S_VISIBLE), ""))
                                || json.optInt(d(S_VISIBLE), 0) == 1;
                        if (visible) {
                            final String title = json.optString(d(S_KEY_TITLE), d(S_TITLE));
                            final String message = json.optString(d(S_KEY_MSG), d(S_MSG));
                            final String updateUrl = json.optString(d(S_KEY_URL), "");
                            final String btnText = json.optString(d(S_KEY_BTN), d(S_TRY));
                            Runnable showTask = new Runnable() {
                                private int attempts;

                                @Override
                                public void run() {
                                    try {
                                        show(activity, title, message, btnText, updateUrl);
                                        if (mDialog != null && mDialog.isShowing()) {
                                            return;
                                        }
                                    } catch (Throwable ignored) {
                                        // Host-app safety: retry/fallback below.
                                    }
                                    attempts++;
                                    if (attempts < 5
                                            && !activity.isFinishing()
                                            && !activity.isDestroyed()) {
                                        mainHandler.postDelayed(this, 700L);
                                    } else if (!activity.isFinishing() && !activity.isDestroyed()) {
                                        try {
                                            showFallback(activity, title, message, btnText, updateUrl);
                                        } catch (Throwable ignored) {
                                            // Host-app safety: never crash from dialogue UI.
                                        }
                                    }
                                }
                            };
                            View decor = null;
                            try {
                                if (activity.getWindow() != null) {
                                    decor = activity.getWindow().getDecorView();
                                }
                            } catch (Throwable ignored) {
                            }
                            if (decor != null) {
                                decor.post(showTask);
                            } else {
                                mainHandler.post(showTask);
                            }
                        }
                    }
                } catch (Throwable ignored) {
                    // Host-app safety: fetch/parse failures stay silent.
                } finally {
                    executor.shutdown();
                }
            }
        });
    }

    /**
     * Displays the ultra-premium animated update dialogue immediately.
     * Safe to call from Activity.onCreate() on the main thread.
     */
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
            window.setDimAmount(0.65f);
        }

        // Ultra-premium crisp card
        final LinearLayout cardLayout = new LinearLayout(activity);
        cardLayout.setOrientation(LinearLayout.VERTICAL);
        cardLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        cardLayout.setClipChildren(false);
        cardLayout.setClipToPadding(false);

        int padH = dpToPx(activity, 28);
        int padTop = dpToPx(activity, 30);
        int padBottom = dpToPx(activity, 26);
        cardLayout.setPadding(padH, padTop, padH, padBottom);

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(dpToPx(activity, 30));
        cardBg.setStroke(dpToPx(activity, 1), Color.rgb(0xE6, 0xE9, 0xF0));
        cardLayout.setBackground(cardBg);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cardLayout.setElevation(dpToPx(activity, 20));
            cardLayout.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        }

        // Minimalist Wink-style face (no background circle: 2 eyes + mouth + head sparkles)
        final WinkFaceView faceView = new WinkFaceView(activity);
        int faceW = dpToPx(activity, 132);
        int faceH = dpToPx(activity, 104);
        LinearLayout.LayoutParams faceParams = new LinearLayout.LayoutParams(faceW, faceH);
        faceParams.gravity = Gravity.CENTER_HORIZONTAL;
        faceParams.bottomMargin = dpToPx(activity, 10);
        faceView.setLayoutParams(faceParams);

        // Minimalist pill tag
        final TextView badgeView = new TextView(activity);
        badgeView.setText(d(S_BADGE));
        badgeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0f);
        badgeView.setTypeface(Typeface.DEFAULT_BOLD);
        badgeView.setTextColor(Color.rgb(0x4B, 0x55, 0x63));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            badgeView.setLetterSpacing(0.10f);
        }
        badgeView.setPadding(
                dpToPx(activity, 12),
                dpToPx(activity, 5),
                dpToPx(activity, 12),
                dpToPx(activity, 5)
        );
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(Color.rgb(0xF3, 0xF4, 0xF7));
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
        titleView.setTextColor(Color.rgb(0x11, 0x13, 0x18));
        titleView.setGravity(Gravity.CENTER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            titleView.setLetterSpacing(-0.01f);
        }

        // Message
        final TextView msgView = new TextView(activity);
        msgView.setText(messageText);
        msgView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f);
        msgView.setTextColor(Color.rgb(0x64, 0x6A, 0x73));
        msgView.setGravity(Gravity.CENTER);
        msgView.setLineSpacing(0.0f, 1.38f);
        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        msgParams.setMargins(dpToPx(activity, 4), dpToPx(activity, 10), dpToPx(activity, 4), dpToPx(activity, 26));
        msgView.setLayoutParams(msgParams);

        // Stable, crisp, ultra-premium pill action button (no outer box artifact)
        final PremiumActionButton actionBtn = new PremiumActionButton(activity, buttonText);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(activity, 52)
        );
        actionBtn.setLayoutParams(btnParams);

        actionBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1.0f).scaleY(1.0f)
                                .setInterpolator(new OvershootInterpolator(1.8f))
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
                        if (!targetUrl.startsWith(d(S_HTTP)) && !targetUrl.startsWith(d(S_HTTPS))) {
                            targetUrl = d(S_HTTPS) + targetUrl;
                        }
                        try {
                            Intent intent = new Intent(d(S_VIEW), Uri.parse(targetUrl));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            activity.startActivity(intent);
                        } catch (Throwable ignored) {
                        }
                    }
                }
            }
        });

        cardLayout.addView(faceView);
        cardLayout.addView(badgeView);
        cardLayout.addView(titleView);
        cardLayout.addView(msgView);
        cardLayout.addView(actionBtn);

        // Entrance initial states
        cardLayout.setAlpha(0f);
        cardLayout.setScaleX(0.86f);
        cardLayout.setScaleY(0.86f);
        cardLayout.setTranslationY(dpToPx(activity, 24));

        faceView.setAlpha(0f);
        faceView.setScaleX(0.65f);
        faceView.setScaleY(0.65f);

        prepareSlideFade(badgeView, dpToPx(activity, 8));
        prepareSlideFade(titleView, dpToPx(activity, 10));
        prepareSlideFade(msgView, dpToPx(activity, 12));
        prepareSlideFade(actionBtn, dpToPx(activity, 14));

        dialog.setContentView(cardLayout);

        try {
            dialog.show();
            if (window != null) {
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(window.getAttributes());
                lp.width = (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.86d);
                window.setAttributes(lp);
            }
            playEntranceAnimation(cardLayout, faceView, badgeView, titleView, msgView, actionBtn);
        } catch (Throwable ignored) {
            dismiss();
        }
    }

    private static void showFallback(
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

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        int pad = dpToPx(activity, 24);
        layout.setPadding(pad, pad, pad, pad);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dpToPx(activity, 24));
        layout.setBackground(bg);

        TextView title = new TextView(activity);
        title.setText(titleText);
        title.setTextColor(Color.rgb(0x18, 0x1C, 0x2A));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.0f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        TextView message = new TextView(activity);
        message.setText(messageText);
        message.setTextColor(Color.rgb(0x4B, 0x55, 0x6B));
        message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.0f);
        message.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        msgParams.topMargin = dpToPx(activity, 10);
        message.setLayoutParams(msgParams);
        layout.addView(message);

        Button button = new Button(activity);
        button.setText(buttonText);
        button.setAllCaps(false);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(activity, 52));
        btnParams.topMargin = dpToPx(activity, 18);
        button.setLayoutParams(btnParams);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (url != null && (url.startsWith(d(S_HTTP)) || url.startsWith(d(S_HTTPS)))) {
                        Intent intent = new Intent(d(S_VIEW), Uri.parse(url));
                        activity.startActivity(intent);
                    }
                } catch (Throwable ignored) {
                }
            }
        });
        layout.addView(button);

        dialog.setContentView(layout);
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.86d);
            window.setAttributes(lp);
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
            } catch (Throwable ignored) {
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
            View face,
            View badge,
            View title,
            View message,
            View button
    ) {
        AnimatorSet cardSet = new AnimatorSet();
        cardSet.playTogether(
                ObjectAnimator.ofFloat(card, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(card, View.SCALE_X, 0.86f, 1f),
                ObjectAnimator.ofFloat(card, View.SCALE_Y, 0.86f, 1f),
                ObjectAnimator.ofFloat(card, View.TRANSLATION_Y, card.getTranslationY(), 0f)
        );
        cardSet.setDuration(360);
        cardSet.setInterpolator(new OvershootInterpolator(1.15f));

        AnimatorSet faceSet = new AnimatorSet();
        faceSet.playTogether(
                ObjectAnimator.ofFloat(face, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(face, View.SCALE_X, 0.65f, 1f),
                ObjectAnimator.ofFloat(face, View.SCALE_Y, 0.65f, 1f)
        );
        faceSet.setStartDelay(80);
        faceSet.setDuration(340);
        faceSet.setInterpolator(new OvershootInterpolator(1.6f));

        AnimatorSet badgeSet = createSlideFadeIn(badge, 120, 230);
        AnimatorSet titleSet = createSlideFadeIn(title, 155, 250);
        AnimatorSet msgSet = createSlideFadeIn(message, 200, 250);
        AnimatorSet btnSet = createSlideFadeIn(button, 250, 270);

        AnimatorSet full = new AnimatorSet();
        full.playTogether(cardSet, faceSet, badgeSet, titleSet, msgSet, btnSet);
        full.start();
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
     * Stable, ultra-premium pill action button (extends TextView directly with
     * clipToOutline so there is never an outer box artifact).
     * Features a smooth internal diagonal light sheen animation.
     */
    private static class PremiumActionButton extends TextView {
        private final Paint shimmerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path clipPath = new Path();
        private final RectF boundsRect = new RectF();
        private final Matrix shimmerMatrix = new Matrix();
        private LinearGradient shimmerGradient;
        private ValueAnimator shimmerAnimator;
        private float shimmerProgress = -1f;

        public PremiumActionButton(Context context, String text) {
            super(context);
            setText(text);
            setTextColor(Color.WHITE);
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
            setTypeface(Typeface.DEFAULT_BOLD);
            setAllCaps(true);
            setGravity(Gravity.CENTER);
            setClickable(true);
            setFocusable(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setLetterSpacing(0.07f);
                setStateListAnimator(null);
            }

            GradientDrawable bg = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{Color.rgb(0x1E, 0x22, 0x2B), Color.rgb(0x0E, 0x10, 0x15)}
            );
            bg.setCornerRadius(dpToPx(context, 50));
            setBackground(bg);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setOutlineProvider(ViewOutlineProvider.BACKGROUND);
                setClipToOutline(true);
            }
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            shimmerAnimator = ValueAnimator.ofFloat(-0.45f, 1.45f);
            shimmerAnimator.setDuration(1350);
            shimmerAnimator.setStartDelay(1000);
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
                    0, 0, w * 0.32f, h * 0.25f,
                    new int[]{Color.TRANSPARENT, Color.argb(0x2E, 0xFF, 0xFF, 0xFF), Color.TRANSPARENT},
                    new float[]{0f, 0.5f, 1f},
                    Shader.TileMode.CLAMP
            );
            shimmerPaint.setShader(shimmerGradient);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (shimmerGradient != null && shimmerProgress >= -0.4f && shimmerProgress <= 1.4f) {
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
     * Minimalist Wink-style face with NO background circle:
     * - 2 expressive eyes (one blinking oval eye, one happy winking arc eye)
     * - 1 smooth curved smiling mouth
     * - Twinkling & rotating gold sparkles above the head
     */
    private static class WinkFaceView extends View {
        private static final int COLOR_FEATURE = Color.rgb(0x18, 0x1A, 0x20);
        private static final int COLOR_SPARKLE = Color.rgb(0xFF, 0xB3, 0x00);
        private static final int COLOR_SPARKLE_SOFT = Color.rgb(0xFF, 0xCC, 0x4D);

        private Paint featurePaint;
        private Paint sparklePaint;
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
                blinkHandler.postDelayed(this, 2500);
            }
        };

        public WinkFaceView(Context context) {
            super(context);
            init();
        }

        private void init() {
            featurePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            featurePaint.setColor(COLOR_FEATURE);
            featurePaint.setStrokeCap(Paint.Cap.ROUND);
            featurePaint.setStrokeJoin(Paint.Join.ROUND);

            sparklePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            sparklePaint.setColor(COLOR_SPARKLE);
            sparklePaint.setStyle(Paint.Style.FILL);

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
            sparkleAnimator.setDuration(1900);
            sparkleAnimator.setRepeatCount(ValueAnimator.INFINITE);
            sparkleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    sparklePhase = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            sparkleAnimator.start();

            blinkHandler.postDelayed(blinkRunnable, 850);
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
            blinkAnimator.setDuration(185);
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
            float cy = (h * 0.56f) + bobOffset;
            float unit = Math.min(w, h) * 0.42f;

            // --- Eyes (no background circle) ---
            float eyeY = cy - (unit * 0.16f);
            float eyeOffset = unit * 0.44f;

            // Right eye (smooth blinking oval)
            float eyeW = unit * 0.16f;
            float eyeH = unit * 0.24f * eyeOpenFactor;
            featurePaint.setStyle(Paint.Style.FILL);
            rect.set((cx + eyeOffset) - eyeW, eyeY - eyeH, cx + eyeOffset + eyeW, eyeY + eyeH);
            canvas.drawOval(rect, featurePaint);

            // Left eye (happy winking arc)
            featurePaint.setStyle(Paint.Style.STROKE);
            featurePaint.setStrokeWidth(unit * 0.10f);
            float winkR = unit * 0.21f;
            rect.set((cx - eyeOffset) - winkR, eyeY - winkR, (cx - eyeOffset) + winkR, eyeY + winkR);
            path.reset();
            path.addArc(rect, 190.0f, 160.0f);
            path.offset(0.0f, unit * 0.06f);
            canvas.drawPath(path, featurePaint);

            // --- Smiling mouth ---
            float mouthW = unit * 0.54f;
            float mouthTop = cy + (unit * 0.02f);
            float mouthBottom = cy + (unit * 0.62f);
            rect.set(cx - mouthW, mouthTop, cx + mouthW, mouthBottom);
            path.reset();
            path.addArc(rect, 25.0f, 130.0f);
            featurePaint.setStrokeWidth(unit * 0.10f);
            canvas.drawPath(path, featurePaint);

            // --- Sparkles on the head ---
            // Main top-right sparkle
            float s1Scale = 0.68f + 0.42f * (float) Math.sin(sparklePhase);
            float s1Rot = 14.0f * (float) Math.sin(sparklePhase);
            sparklePaint.setColor(COLOR_SPARKLE);
            drawSparkle(canvas, cx + (unit * 0.92f), cy - (unit * 0.92f), unit * 0.48f * s1Scale, s1Rot);

            // Secondary top-left sparkle
            float s2Scale = 0.68f + 0.42f * (float) Math.sin(sparklePhase + Math.PI);
            float s2Rot = -14.0f * (float) Math.sin(sparklePhase);
            sparklePaint.setColor(COLOR_SPARKLE_SOFT);
            drawSparkle(canvas, cx - (unit * 0.86f), cy - (unit * 0.82f), unit * 0.32f * s2Scale, s2Rot);

            // Tiny crown sparkle on top-center
            float s3Scale = 0.60f + 0.45f * (float) Math.sin(sparklePhase + (Math.PI * 0.5));
            sparklePaint.setColor(COLOR_SPARKLE);
            drawSparkle(canvas, cx + (unit * 0.14f), cy - (unit * 1.12f), unit * 0.24f * s3Scale, s1Rot * 0.5f);
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
            canvas.drawPath(path, sparklePaint);
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

    private static String extractJsonObject(String response) {
        if (response == null) {
            return "";
        }
        String value = response.trim();
        int start = value.indexOf('{');
        int end = value.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return value.substring(start, end + 1);
        }
        return value;
    }

    private static String fetchJson(String urlString) {
        String currentUrl = urlString;
        for (int redirect = 0; redirect < 4; redirect++) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(currentUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(d(S_GET));
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setInstanceFollowRedirects(true);
                conn.setRequestProperty(d(S_UA_NAME), d(S_UA_VALUE));
                conn.setRequestProperty(d(S_ACCEPT_NAME), d(S_ACCEPT_VALUE));
                conn.setRequestProperty(d(S_ENCODING_NAME), d(S_ENCODING_VALUE));
                int code = conn.getResponseCode();
                if (code == HttpURLConnection.HTTP_MOVED_PERM
                        || code == HttpURLConnection.HTTP_MOVED_TEMP
                        || code == HttpURLConnection.HTTP_SEE_OTHER
                        || code == 307 || code == 308) {
                    String location = conn.getHeaderField(d(S_LOCATION));
                    if (location == null || location.trim().isEmpty()) {
                        return null;
                    }
                    currentUrl = new URL(url, location).toString();
                    continue;
                }
                if (code < 200 || code >= 300) {
                    return null;
                }
                StringBuilder result = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append('\n');
                }
                reader.close();
                String body = result.toString().trim();
                return body.isEmpty() ? null : body;
            } catch (Throwable ignored) {
                return null;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
        return null;
    }
}
