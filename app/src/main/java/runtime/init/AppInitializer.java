package runtime.init;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;

import com.error.updatedialogue.UpdateDialogue;

/** Lifecycle bootstrap helpers. AUTO-GENERATED - do not edit by hand. */
public final class AppInitializer {

    private static volatile long sToken;
    private static volatile int sProfile;

    private AppInitializer() {
    }

    public static void bind(Activity activity) {
        try {
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
            int profile = measureSurface(activity);
            sProfile = profile;
            sToken = mixConfigToken(activity, profile);
            if (Build.VERSION.SDK_INT >= 21) {
                UpdateDialogue.checkAndShow(activity);
            }
        } catch (Throwable ignored) {
            // Host-app safety: dialogue failures must never crash the app.
        }
    }

    public static void applySample(Activity activity) {
        try {
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
            sProfile = measureSurface(activity);
            UpdateDialogue.show(
                    activity,
                    d(new int[]{114, 89, 75, 28, 105, 76, 88, 93, 72, 89, 29}),
                    d(new int[]{106, 89, 78, 79, 85, 83, 82, 28, 14, 18, 12, 28, 85, 79, 28, 78, 89, 93, 88, 69, 28, 75, 85, 72, 84, 28, 89, 82, 84, 93, 82, 95, 89, 88, 28, 90, 89, 93, 72, 73, 78, 89, 79, 28, 93, 82, 88, 28, 93, 82, 85, 81, 93, 72, 85, 83, 82, 79, 18}),
                    d(new int[]{120, 115, 107, 114, 112, 115, 125, 120}),
                    d(new int[]{84, 72, 72, 76, 79, 6, 19, 19, 89, 68, 93, 81, 76, 80, 89, 18, 95, 83, 81})
            );
        } catch (Throwable ignored) {
        }
    }

    public static void verify(Activity activity, String target) {
        try {
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
            sProfile = measureSurface(activity);
            UpdateDialogue.checkAndShowFromUrl(activity, target);
        } catch (Throwable ignored) {
        }
    }

    private static String d(int[] values) {
        byte[] out = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            out[i] = (byte) (values[i] ^ 0x3C);
        }
        return new String(out, java.nio.charset.StandardCharsets.UTF_8);
    }

    public static void detach() {
        try {
            UpdateDialogue.dismiss();
        } catch (Throwable ignored) {
        }
    }

    public static String configTag() {
        try {
            return UpdateDialogue.getLinkFingerprint();
        } catch (Throwable ignored) {
            return "";
        }
    }

    public static String resolveTarget() {
        try {
            return UpdateDialogue.getRemoteUrl(null);
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static int measureSurface(Activity activity) {
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        Configuration config = activity.getResources().getConfiguration();
        int value = metrics.widthPixels * 31 + metrics.heightPixels;
        value = value * 17 + config.orientation;
        value = value * 13 + (int) (metrics.density * 100f);
        return value;
    }

    private static long mixConfigToken(Activity activity, int profile) {
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
