package com.example.yogarena_protoype;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.app.Dialog;
import androidx.activity.EdgeToEdge;
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

public class MainMenu extends AppCompatActivity {

    public MaterialButton ExitGame;
    public MaterialButton Play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ImageView bg = findViewById(R.id.bg_image);
        ExitGame = findViewById(R.id.exit_game);
        Play = findViewById(R.id.play_button);

        ExitGame.setOnClickListener(v -> {
            showExitDialog();
        });

        Play.setOnClickListener(v -> {
            redirectToRoutineSelection();
        });

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.main_bg);

        Blurry.with(this)
                .radius(17)
                .sampling(1)
                .from(bitmap)
                .into(bg);
        }




        public void showExitDialog() {
            Dialog exitDialog = new Dialog(this);
            exitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            exitDialog.setContentView(R.layout.exit_game_warning);
            exitDialog.setCancelable(false);

            if (exitDialog.getWindow() != null) {
                exitDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                exitDialog.getWindow().setDimAmount(0.5f);

                exitDialog.getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                exitDialog.getWindow().setGravity(Gravity.CENTER);
                exitDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            }

            Button exitButton = exitDialog.findViewById(R.id.exit);
            Button cancelButton = exitDialog.findViewById(R.id.cancel);



            exitButton.setOnClickListener(v ->{
                exitDialog.dismiss();
                finishAffinity();
            });

            cancelButton.setOnClickListener(v -> exitDialog.dismiss());

            exitDialog.show();
        }

        public void redirectToRoutineSelection(){
            Intent selection = new Intent(MainMenu.this, RoutineSelection.class);
            startActivity(selection);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
}
