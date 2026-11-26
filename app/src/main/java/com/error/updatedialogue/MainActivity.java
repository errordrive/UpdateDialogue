package com.error.updatedialogue;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
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
        subtitle.setText("Your Pastebin raw JSON link is locked inside the app at build time (SHA-256 + RSA verified).");
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
        inputLabel.setText("Locked Remote JSON Link:");
        inputLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        inputLabel.setTypeface(Typeface.DEFAULT_BOLD);
        inputLabel.setTextColor(Color.parseColor("#374151"));
        inputLabel.setPadding(0, 0, 0, dp(8));
        card.addView(inputLabel);

        // Locked link status (not editable - the link lives inside the app)
        urlInput = new EditText(this);
        // GENERATED-CALL:STATUS:START
        String statusCode = app.config.RuntimeBridge.statusCode();
        // GENERATED-CALL:STATUS:END
        urlInput.setText("Locked inside app  -  SHA-256: " + statusCode);
        urlInput.setEnabled(false);
        urlInput.setFocusable(false);
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

        // Save & Test Remote Button
        Button saveBtn = new Button(this);
        saveBtn.setText("Test Locked Remote Dialogue");
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
                // GENERATED-CALL:TEST:START
                if (app.config.RuntimeBridge.target().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Link verification failed", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(MainActivity.this, "Fetching locked link...", Toast.LENGTH_SHORT).show();
                app.config.RuntimeBridge.attach(MainActivity.this);
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
                app.config.RuntimeBridge.renderSample(MainActivity.this);
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
        guideTitle.setText("Pastebin JSON Structure:");
        guideTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        guideTitle.setTypeface(Typeface.DEFAULT_BOLD);
        guideTitle.setTextColor(Color.parseColor("#1F2937"));
        guideCard.addView(guideTitle);

        TextView jsonCode = new TextView(this);
        jsonCode.setText("{\n" +
                "  \"isVisible\": true,\n" +
                "  \"title\": \"New Update!\",\n" +
                "  \"message\": \"Version 2.0 is available.\",\n" +
                "  \"updateUrl\": \"https://example.com\",\n" +
                "  \"buttonText\": \"DOWNLOAD\"\n" +
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

        root.addView(guideCard);

        setContentView(scrollView);

        // GENERATED-CALL:STARTUP:START
        app.config.RuntimeBridge.attach(this);
        // GENERATED-CALL:STARTUP:END
    }

    /**
     * Writes "Update Dialogue.zip" (dialogue dex + MainActivity call hook)
     * into local storage: Downloads/Update Dialogue/ on Android 10+,
     * otherwise the app-specific Download/Update Dialogue folder.
     */
    private void generateOutputZip() {
        // GENERATED-CALL:EXPORT:START
        String entryClass = "app.config.RuntimeBridge";
        String entryMethod = "attach";
        // GENERATED-CALL:EXPORT:END

        String smaliClass = "L" + entryClass.replace('.', '/') + ";";
        String hookText = "Generated MainActivity call (names regenerate on every generate)\n\n"
                + "Java (MainActivity.onCreate):\n"
                + "    " + entryClass + "." + entryMethod + "(this);\n\n"
                + "Smali (MainActivity.onCreate, p0 = this):\n"
                + "    invoke-static {p0}, " + smaliClass + "->" + entryMethod
                + "(Landroid/app/Activity;)V\n";

        try {
            OutputStream out;
            String savedLabel;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, "Update Dialogue.zip");
                values.put(MediaStore.Downloads.MIME_TYPE, "application/zip");
                values.put(MediaStore.Downloads.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS + "/Update Dialogue");
                values.put(MediaStore.Downloads.IS_PENDING, 1);
                Uri uri = getContentResolver().insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) {
                    throw new IllegalStateException("Could not create output file");
                }
                out = getContentResolver().openOutputStream(uri);
                if (out == null) {
                    throw new IllegalStateException("Could not open output file");
                }
                writeOutputZip(out, hookText);
                values.clear();
                values.put(MediaStore.Downloads.IS_PENDING, 0);
                getContentResolver().update(uri, values, null, null);
                savedLabel = "Downloads/Update Dialogue/Update Dialogue.zip";
            } else {
                File dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                        "Update Dialogue");
                if (!dir.exists() && !dir.mkdirs()) {
                    throw new IllegalStateException("Could not create output folder");
                }
                File file = new File(dir, "Update Dialogue.zip");
                out = new FileOutputStream(file);
                writeOutputZip(out, hookText);
                savedLabel = file.getAbsolutePath();
            }
            Toast.makeText(this, "Saved: " + savedLabel, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Generate failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void writeOutputZip(OutputStream out, String hookText) throws Exception {
        ZipOutputStream zip = new ZipOutputStream(out);
        try {
            zip.putNextEntry(new ZipEntry("classes.dex"));
            InputStream dex = getAssets().open("dialogue.dex");
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
            zip.write(hookText.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("README.txt"));
            zip.write(("Update Dialogue output\n\n"
                    + "classes.dex - standalone dialogue dex (R8-obfuscated class names;\n"
                    + "the generated entry class keeps its generated name).\n"
                    + "mainactivity-call.txt - the MainActivity call hook for this build.\n"
                    + "Names regenerate on every generate.\n")
                    .getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        } finally {
            zip.close();
        }
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
        app.config.RuntimeBridge.detach();
        // GENERATED-CALL:TEARDOWN:END
        super.onDestroy();
    }
}
