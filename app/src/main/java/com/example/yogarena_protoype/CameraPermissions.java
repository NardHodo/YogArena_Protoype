package com.example.yogarena_protoype;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class CameraPermissions extends AppCompatActivity {

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as a field in your class.
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your app.
                    Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                    transitionToLoading();
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied.
                    Toast.makeText(this, "Camera Access is Required to Proceed", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_permissions);

        Button allowAccess = findViewById(R.id.set_camera);

        // Set the click listener for the "Allow Access" button
        allowAccess.setOnClickListener(v -> {
            checkAndRequestCameraPermission();
        });

        Button exitGame = findViewById(R.id.exit_game);
        exitGame.setOnClickListener(v -> {
            // Closes the entire application
            finishAffinity();
        });
    }

    private void checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // If permission is already granted, just proceed.
            Toast.makeText(this, "Permission was already granted.", Toast.LENGTH_SHORT).show();
            transitionToLoading();
        } else {
            // Directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void transitionToLoading(){
        Intent intent = new Intent(this, LoadingScreen.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}
