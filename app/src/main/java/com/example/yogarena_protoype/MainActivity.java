package com.example.yogarena_protoype;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import android.content.Intent;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {

    private boolean hasStartedNextActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        checkCameraPermissions();
        }

    public void transitionToCameraPermissions(){
        new Handler().postDelayed(() -> {
            if (!hasStartedNextActivity) {
                hasStartedNextActivity = true;
                Intent intent = new Intent(this, CameraPermissions.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        }, 3000);
    }

    private void checkCameraPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            transitionToLoading();
        } else{
            transitionToCameraPermissions();
        }
    }

    private void transitionToLoading(){
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(this, LoadingScreen.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, 3000);

    }
}



