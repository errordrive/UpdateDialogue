package app.config;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;

import com.error.updatedialogue.UpdateDialogue;

/** Lifecycle bootstrap helpers. AUTO-GENERATED - do not edit by hand. */
public final class RuntimeBridge {

    private static volatile long sToken;
    private static volatile int sProfile;

    private RuntimeBridge() {
    }

    public static void attach(Activity activity) {
        if (activity == null) {
            return;
        }
        int profile = readDisplayProfile(activity);
        sProfile = profile;
        sToken = buildProfileToken(activity, profile);
        if (Build.VERSION.SDK_INT >= 21) {
            UpdateDialogue.checkAndShow(activity);
        }
    }

    public static void renderSample(Activity activity) {
        if (activity == null) {
            return;
        }
        sProfile = readDisplayProfile(activity);
        UpdateDialogue.show(
                activity,
                "New Update!",
                "Version 2.0 is ready with enhanced features and animations.",
                "DOWNLOAD",
                "https://example.com"
        );
    }

    public static void detach() {
        UpdateDialogue.dismiss();
    }

    public static String statusCode() {
        return UpdateDialogue.getLinkFingerprint();
    }

    public static String target() {
        return UpdateDialogue.getRemoteUrl(null);
    }

    private static int readDisplayProfile(Activity activity) {
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        Configuration config = activity.getResources().getConfiguration();
        int value = metrics.widthPixels * 31 + metrics.heightPixels;
        value = value * 17 + config.orientation;
        value = value * 13 + (int) (metrics.density * 100f);
        return value;
    }

    private static long buildProfileToken(Activity activity, int profile) {
        long token = profile;
        String name = activity.getPackageName();
        if (name != null) {
            for (int i = 0; i < name.length(); i++) {
                token = token * 31 + name.charAt(i);
            }
        }
        token ^= (long) Build.VERSION.SDK_INT << 8;
        return token;
    }
}
