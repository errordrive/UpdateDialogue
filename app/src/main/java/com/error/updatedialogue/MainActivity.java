package com.error.updatedialogue;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);

        Button previewBtn = new Button(this);
        previewBtn.setText("Preview Animated Dialogue");
        previewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateDialogue.show(
                        MainActivity.this,
                        "New Update!",
                        "A cooler animated update dialogue is ready.",
                        "TRY NOW",
                        "https://github.com"
                );
            }
        });
        root.addView(previewBtn);
        setContentView(root);

        // Call from MainActivity.onCreate()
        UpdateDialogue.checkAndShow(this);
    }

    @Override
    protected void onDestroy() {
        UpdateDialogue.dismiss();
        super.onDestroy();
    }
}
