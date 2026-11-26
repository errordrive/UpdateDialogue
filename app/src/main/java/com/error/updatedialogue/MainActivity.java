package com.error.updatedialogue;

import android.app.Activity;
import android.graphics.Color;
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
        subtitle.setText("Paste your Pastebin raw JSON URL below to remotely control the dialogue box.");
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

        // EditText for URL
        urlInput = new EditText(this);
        urlInput.setText(UpdateDialogue.getRemoteUrl(this));
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
                String input = urlInput.getText().toString().trim();
                if (input.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter a valid URL", Toast.LENGTH_SHORT).show();
                    return;
                }
                UpdateDialogue.setRemoteUrl(MainActivity.this, input);
                Toast.makeText(MainActivity.this, "Remote URL saved! Fetching...", Toast.LENGTH_SHORT).show();
                UpdateDialogue.checkAndShow(MainActivity.this, input);
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
                UpdateDialogue.show(
                        MainActivity.this,
                        "New Update!",
                        "Version 2.0 is ready with enhanced features and animations.",
                        "DOWNLOAD",
                        "https://github.com/errordrive/UpdateDialogue"
                );
            }
        });
        card.addView(previewBtn);

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

        // Initial check on create with active URL
        UpdateDialogue.checkAndShow(this);
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
        UpdateDialogue.dismiss();
        super.onDestroy();
    }
}
