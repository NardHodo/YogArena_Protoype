package com.example.yogarena_protoype;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import jp.wasabeef.blurry.Blurry;

import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.button.MaterialButton;

public class MainMenu extends AppCompatActivity {

    public MaterialButton ExitGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ImageView bg = findViewById(R.id.bg_image);
        ExitGame = findViewById(R.id.exit_game);
        ExitGame.setOnClickListener(v -> {
            finishAffinity();
        });

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.main_bg);

        Blurry.with(this)
                .radius(17)
                .sampling(1)
                .from(bitmap)
                .into(bg);
        }


}
