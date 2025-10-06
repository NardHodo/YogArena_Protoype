package com.example.yogarena_protoype;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


public class LoadingScreen extends AppCompatActivity {


    private ProgressBar loadingTime;
    private int progressStatus = 0;
    private final Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. Start with the blank screen
        setContentView(R.layout.blank_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // 2. Use a Handler to create the "flicker" effect after a short delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // 3. Switch to the actual loading screen layout
            setContentView(R.layout.loading_screen);
            // Apply the fade-in animation for this new layout
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            // 4. Initialize the views from the now-visible loading_screen.xml
            loadingTime = findViewById(R.id.progressLinear);

            // 5. Start the progress bar simulation
            simulateLoading();
        }, 800); // 800ms delay for the blank screen
    }

    private void simulateLoading() {
        new Thread(() -> {
            while (progressStatus < 100) {
                progressStatus += 2;

                handler.post(() -> {
                    if (loadingTime != null) {
                        loadingTime.setProgress(progressStatus, true);
                    }
                });

                try {
                    Thread.sleep(60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // After loading is complete, transition to the main menu
            handler.post(() -> {
                Intent intent = new Intent(LoadingScreen.this, MainMenu.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish(); // Close the loading screen
            });
        }).start();
    }
}
