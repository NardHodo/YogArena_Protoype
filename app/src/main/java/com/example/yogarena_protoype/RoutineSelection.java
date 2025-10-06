package com.example.yogarena_protoype;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.app.Dialog;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import jp.wasabeef.blurry.Blurry;

import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.button.MaterialButton;

public class RoutineSelection extends AppCompatActivity{

    public MaterialButton backButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.routine_selection);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ImageView bg = findViewById(R.id.selection_bg);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.selection_bg);

        Blurry.with(this)
                .radius(17)
                .sampling(1)
                .from(bitmap)
                .into(bg);

        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> backToMainMenu());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Finish the activity
                finish();

                // Apply fade transition when returning
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    public void backToMainMenu(){
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
