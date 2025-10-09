package com.example.yogarena_protoype;

import android.Manifest;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Canvas; // For drawing keypoints
import android.graphics.Color; // For drawing keypoints
import android.graphics.Paint; // For drawing keypoints
import android.graphics.YuvImage; // For Yuv conversion
import android.graphics.Rect; // For Yuv conversion
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.yogarena_protoype.databinding.CameraBinding;
import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "YogaClassifierApp";
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final float CONFIDENCE_THRESHOLD = 0.5f;

    private CameraBinding binding;
    private ExecutorService cameraExecutor;

    private Interpreter poseDetector;
    private Interpreter imageClassifier;

    private int selectedPoseIndex = -1;
    private static final int MOBILENET_INPUT_SIZE_H = 224;
    private static final int MOBILENET_INPUT_SIZE_W = 224;
    private static final int MOVENET_INPUT_SIZE = 192; // Assuming MoveNet SinglePose Lightning/Thunder input size

    // Variable to track the current camera
    private int currentCameraSelector = CameraSelector.LENS_FACING_BACK;

    private final List<String> poseLabels = Arrays.asList(
            "Akarna_Dhanurasana", "Bharadvaja's_Twist_pose_or_Bharadvajasana_I_",
            "Boat_Pose_or_Paripurna_Navasana_", "Bound_Angle_Pose_or_Baddha_Konasana_",
            "Bow_Pose_or_Dhanurasana_", "Bridge_Pose_or_Setu_Bandha_Sarvangasana_",
            "Camel_Pose_or_Ustrasana_", "Cat_Cow_Pose_or_Marjaryasana_",
            "Chair_Pose_or_Utkatasana_", "Child_Pose_or_Balasana_",
            "Cobra_Pose_or_Bhujangasana_", "Cockerel_Pose",
            "Corpse_Pose_or_Savasana_", "Cow_Face_Pose_or_Gomukhasana_",
            "Crane_(Crow)_Pose_or_Bakasana_", "Dolphin_Plank_Pose_or_Makara_Adho_Mukha_Svanasana_",
            "Dolphin_Pose_or_Ardha_Pincha_Mayurasana_",
            "Downward-Facing_Dog_pose_or_Adho_Mukha_Svanasana_", "Eagle_Pose_or_Garudasana_",
            "Eight-Angle_Pose_or_Astavakrasana_", "Extended_Puppy_Pose_or_Uttana_Shishosana_",
            "Extended_Revolved_Side_Angle_Pose_or_Utthita_Parsvakonasana_",
            "Extended_Revolved_Triangle_Pose_or_Utthita_Trikonasana_",
            "Feathered_Peacock_Pose_or_Pincha_Mayurasana_", "Firefly_Pose_or_Tittibhasana_",
            "Fish_Pose_or_Matsyasana_", "Four-Limbed_Staff_Pose_or_Chaturanga_Dandasana_",
            "Frog_Pose_or_Bhekasana", "Garland_Pose_or_Malasana_", "Gate_Pose_or_Parighasana_",
            "Half_Lord_of_the_Fishes_Pose_or_Ardha_Matsyendrasana_",
            "Half_Moon_Pose_or_Ardha_Chandrasana_", "Handstand_pose_or_Adho_Mukha_Vrksasana_",
            "Happy_Baby_Pose_or_Ananda_Balasana_", "Head-to-Knee_Forward_Bend_pose_or_Janu_Sirsasana_",
            "Heron_Pose_or_Krounchasana_", "Intense_Side_Stretch_Pose_or_Parsvottanasana_",
            "Legs-Up-the-Wall_Pose_or_Viparita_Karani_", "Locust_Pose_or_Salabhasana_",
            "Lord_of_the_Dance_Pose_or_Natarajasana_", "Low_Lunge_pose_or_Anjaneyasana_",
            "Noose_Pose_or_Pasasana_", "Peacock_Pose_or_Mayurasana_",
            "Pigeon_Pose_or_Kapotasana_", "Plank_Pose_or_Kumbhakasana_", "Plow_Pose_or_Halasana_",
            "Pose_Dedicated_to_the_Sage_Koundinya_or_Eka_Pada_Koundinyanasana_I_and_II",
            "Rajakapotasana", "Reclining_Hand-to-Big-Toe_Pose_or_Supta_Padangusthasana_",
            "Revolved_Head-to-Knee_Pose_or_Parivrtta_Janu_Sirsasana_",
            "Scale_Pose_or_Tolasana_", "Scorpion_pose_or_vrischikasana",
            "Seated_Forward_Bend_pose_or_Paschimottanasana_",
            "Shoulder-Pressing_Pose_or_Bhujapidasana_", "Side-Reclining_Leg_Lift_pose_or_Anantasana_",
            "Side_Crane_(Crow)_Pose_or_Parsva_Bakasana_", "Side_Plank_Pose_or_Vasisthasana_",
            "Sitting_pose_1_(normal)", "Split_pose", "Staff_Pose_or_Dandasana_",
            "Standing_Forward_Bend_pose_or_Uttanasana_",
            "Standing_Split_pose_or_Urdhva_Prasarita_Eka_Padasana_",
            "Standing_big_toe_hold_pose_or_Utthita_Padangusthasana",
            "Supported_Headstand_pose_or_Salamba_Sirsasana_",
            "Supported_Shoulderstand_pose_or_Salamba_Sarvangasana_", "Supta_Baddha_Konasana_",
            "Supta_Virasana_Vajrasana", "Tortoise_Pose", "Tree_Pose_or_Vrksasana_",
            "Upward_Bow_(Wheel)_Pose_or_Urdhva_Dhanurasana_",
            "Upward_Facing_Two-Foot_Staff_Pose_or_Dwi_Pada_Viparita_Dandasana_",
            "Upward_Plank_Pose_or_Purvottanasana_", "Virasana_or_Vajrasana",
            "Warrior_III_Pose_or_Virabhadrasana_III_", "Warrior_II_Pose_or_Virabhadrasana_II_",
            "Warrior_I_Pose_or_Virabhadrasana_I_",
            "Wide-Angle_Seated_Forward_Bend_pose_or_Upavistha_Konasana_",
            "Wide-Legged_Forward_Bend_pose_or_Prasarita_Padottanasana_",
            "Wild_Thing_pose_or_Camatkarasana_", "Wind_Relieving_pose_or_Pawanmuktasana",
            "Yogic_sleep_pose", "viparita_virabhadrasana_or_reverse_warrior_pose"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = CameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cameraExecutor = Executors.newSingleThreadExecutor();

        try {
            // Load MoveNet for pose detection
            poseDetector = new Interpreter(loadModelFile(this, "movenet.tflite"));
            // Load MobileNetV2 classifier
            imageClassifier = new Interpreter(loadModelFile(this, "pose_classifier.tflite"));

            Log.d(TAG, "Models loaded successfully.");
        } catch (IOException e) {
            Log.e(TAG, "Failed to load models", e);
            Toast.makeText(this, "Error loading models: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        setupPoseSpinner();

        // Set up the click listener for the switch camera button
        binding.switchCameraButton.setOnClickListener(v -> toggleCamera());

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
    }

    private static MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        try (FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } finally {
            fileDescriptor.close();
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void setupPoseSpinner() {
        String[] spinnerItems = new String[poseLabels.size() + 1];
        spinnerItems[0] = "Select a Pose";
        for (int i = 0; i < poseLabels.size(); i++) spinnerItems[i + 1] = poseLabels.get(i);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.poseSpinner.setAdapter(adapter);

        binding.poseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPoseIndex = (position > 0) ? position - 1 : -1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { selectedPoseIndex = -1; }
        });
    }

    /**
     * Toggles between the front and back cameras and restarts the camera preview.
     */
    private void toggleCamera() {
        if (currentCameraSelector == CameraSelector.LENS_FACING_BACK) {
            currentCameraSelector = CameraSelector.LENS_FACING_FRONT;
        } else {
            currentCameraSelector = CameraSelector.LENS_FACING_BACK;
        }
        startCamera();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error binding camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

        ImageAnalysis imageAnalyzer = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalyzer.setAnalyzer(cameraExecutor, new ImageAnalyzer());

        // Use the current camera selector
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(currentCameraSelector)
                .build();

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer);
        } catch (Exception e) {
            Log.e(TAG, "Binding failed", e);
        }
    }

    private class ImageAnalyzer implements ImageAnalysis.Analyzer {

        // --- FPS COUNTER VARIABLES ---
        private final Queue<Long> frameTimestamps = new LinkedList<>();
        private static final int FPS_HISTORY_SIZE = 10;
        private static final long FPS_UPDATE_INTERVAL_MS = 1000;
        private long lastFpsUpdateTime = 0;
        // -----------------------------

        // --- PERFORMANCE OPTIMIZATION VARIABLES (REVISED) ---
        private byte[] nv21Buffer = null; // Re-use for NV21 conversion
        // ------------------------------------------

        // Define Paint objects for drawing keypoints
        private final Paint keypointPaint = new Paint();
        private final Paint connectionPaint = new Paint();

        public ImageAnalyzer() {
            keypointPaint.setColor(Color.parseColor("#F566E6")); // Pink/Purple
            keypointPaint.setStrokeWidth(4);
            keypointPaint.setStyle(Paint.Style.FILL);

            connectionPaint.setColor(Color.parseColor("#F57542")); // Orange
            connectionPaint.setStrokeWidth(6);
            connectionPaint.setStyle(Paint.Style.STROKE);
        }

        // --- MobileNetV2 preprocessing equivalent: Normalizes to [-1, 1] ---
        private ByteBuffer preprocessImageForMobileNet(Bitmap bitmap) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(4 * MOBILENET_INPUT_SIZE_H * MOBILENET_INPUT_SIZE_W * 3)
                    .order(ByteOrder.nativeOrder());
            buffer.rewind();

            int[] pixels = new int[MOBILENET_INPUT_SIZE_H * MOBILENET_INPUT_SIZE_W];
            bitmap.getPixels(pixels, 0, MOBILENET_INPUT_SIZE_W, 0, 0, MOBILENET_INPUT_SIZE_W, MOBILENET_INPUT_SIZE_H);

            for (int pixel : pixels) {
                float r = (float) Color.red(pixel);
                float g = (float) Color.green(pixel);
                float b = (float) Color.blue(pixel);

                // MobileNetV2 Preprocessing: Normalize to [-1, 1]
                buffer.putFloat((r - 127.5f) / 127.5f);
                buffer.putFloat((g - 127.5f) / 127.5f);
                buffer.putFloat((b - 127.5f) / 127.5f);
            }
            buffer.rewind();
            return buffer;
        }

        /**
         * Converts an ImageProxy (YUV_420_888) to a Bitmap via NV21 intermediate format.
         */
        private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
            if (imageProxy.getFormat() != ImageFormat.YUV_420_888) {
                return null;
            }

            try {
                int width = imageProxy.getWidth();
                int height = imageProxy.getHeight();

                int ySize = width * height;
                int uvSize = ySize / 2;
                int totalSize = ySize + uvSize;

                if (nv21Buffer == null || nv21Buffer.length != totalSize) {
                    nv21Buffer = new byte[totalSize];
                }

                // 1. Convert YUV planes to NV21 format byte array (Manual implementation)
                yuv_420_888ToNv21(imageProxy, nv21Buffer);

                // 2. Convert NV21 byte array to Bitmap
                YuvImage yuvImage = new YuvImage(
                        nv21Buffer, ImageFormat.NV21, width, height, null);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                yuvImage.compressToJpeg(new Rect(0, 0, width, height), 90, out);

                byte[] imageBytes = out.toByteArray();
                return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            } catch (Exception e) {
                Log.e(TAG, "Error in imageProxyToBitmap:", e);
                return null;
            }
        }

        /**
         * Manual implementation of YUV_420_888 to NV21 conversion.
         */
        private void yuv_420_888ToNv21(ImageProxy image, byte[] output) {
            ImageProxy.PlaneProxy yPlane = image.getPlanes()[0];
            ImageProxy.PlaneProxy uPlane = image.getPlanes()[1];
            ImageProxy.PlaneProxy vPlane = image.getPlanes()[2];

            ByteBuffer yBuffer = yPlane.getBuffer();
            ByteBuffer uBuffer = uPlane.getBuffer();
            ByteBuffer vBuffer = vPlane.getBuffer();

            int ySize = yBuffer.remaining();
            int width = image.getWidth();
            int height = image.getHeight();

            // 1. Copy Y plane
            yBuffer.get(output, 0, ySize);

            // 2. Interleave U and V planes into NV21 format (VU VU...)
            int outputOffset = ySize;
            int rowStride = uPlane.getRowStride();
            int pixelStride = uPlane.getPixelStride();

            // Loop through half the height and half the width (since U/V are sub-sampled)
            for (int row = 0; row < height / 2; row++) {
                for (int col = 0; col < width / 2; col++) {
                    int uIndex = row * rowStride + col * pixelStride;
                    int vIndex = row * rowStride + col * pixelStride;

                    // NV21 interleaves V before U
                    if (vBuffer.remaining() > vIndex && outputOffset < output.length) {
                        output[outputOffset++] = vBuffer.get(vIndex);
                    }
                    if (uBuffer.remaining() > uIndex && outputOffset < output.length) {
                        output[outputOffset++] = uBuffer.get(uIndex);
                    }
                }
            }
        }

        @Override
        public void analyze(@NonNull ImageProxy imageProxy) {

            // --- FPS CALCULATION START ---
            long currentTime = System.currentTimeMillis();
            frameTimestamps.add(currentTime);

            while (frameTimestamps.size() > FPS_HISTORY_SIZE) {
                frameTimestamps.remove();
            }

            if (currentTime - lastFpsUpdateTime >= FPS_UPDATE_INTERVAL_MS) {
                if (frameTimestamps.size() >= 2) {
                    long oldestTime = frameTimestamps.peek();
                    double timeElapsed = (double) (currentTime - oldestTime);
                    double fps = (frameTimestamps.size() - 1) * 1000.0 / timeElapsed;

                    String fpsText = String.format("FPS: %.1f", fps);
                    runOnUiThread(() -> binding.fpsTextView.setText(fpsText));
                }
                lastFpsUpdateTime = currentTime;
            }
            // --- FPS CALCULATION END ---

            if (selectedPoseIndex == -1) {
                runOnUiThread(() -> binding.resultTextView.setText("Status: Select a pose to begin."));
                imageProxy.close();
                return;
            }

            try {
                // 1. Convert to Bitmap
                Bitmap originalBitmap = imageProxyToBitmap(imageProxy);
                if (originalBitmap == null) return;

                // 2. FLIP horizontally (Mirroring Fix)
                Bitmap processedBitmap;
                if (currentCameraSelector == CameraSelector.LENS_FACING_FRONT) {
                    // **Only flip for the front camera for the standard 'selfie' mirror effect**
                    processedBitmap = flipBitmap(originalBitmap);
                } else {
                    // **Fix: Use the original non-flipped bitmap for the back camera.**
                    processedBitmap = originalBitmap;
                }

                // 3. Run MoveNet on the processed image for keypoints
                Bitmap movenetInputBitmap = Bitmap.createScaledBitmap(processedBitmap, MOVENET_INPUT_SIZE, MOVENET_INPUT_SIZE, true);
                ByteBuffer movenetInputBuffer = convertBitmapToByteBuffer(movenetInputBitmap);

                float[][][][] movenetOutput = new float[1][1][17][3]; // [1, 1, 17, 3] -> [y, x, confidence]
                poseDetector.run(movenetInputBuffer, movenetOutput);
                float[][] keypoints = movenetOutput[0][0];

                // 4. Check Pose Confidence
                float noseConfidence = keypoints[0][2];
                if (noseConfidence < 0.1f) {
                    runOnUiThread(() -> binding.resultTextView.setText("Target: " + poseLabels.get(selectedPoseIndex) + "\nStatus: NO POSE DETECTED (Low Confidence)"));
                    // **SKELETON HIDE FIX:** Display the unprocessed image
                    runOnUiThread(() -> binding.viewFinderImage.setImageBitmap(processedBitmap));
                    return;
                }

                // 5. Draw Keypoints onto the processedBitmap (for classification use only)
                // We draw the keypoints onto a copy of the processedBitmap to generate the annotatedBitmap.
                Bitmap annotatedBitmap = drawKeypointsOnBitmap(processedBitmap, keypoints);

                // **SKELETON HIDE FIX:** We display the UN-ANNOTATED processedBitmap to the user,
                // hiding the skeleton but still showing the live video feed.
                runOnUiThread(() -> binding.viewFinderImage.setImageBitmap(annotatedBitmap));

                // 6. Preprocess the ANNOTATED image for MobileNetV2 (Classification continues)
                Bitmap resizedAnnotatedBitmap = Bitmap.createScaledBitmap(annotatedBitmap, MOBILENET_INPUT_SIZE_W, MOBILENET_INPUT_SIZE_H, true);
                ByteBuffer mobilenetInput = preprocessImageForMobileNet(resizedAnnotatedBitmap);

                // 7. Run the Image Classifier
                float[][] classifierOutput = new float[1][poseLabels.size()];
                imageClassifier.run(mobilenetInput, classifierOutput);

                // 8. Post-process and update UI
                float confidence = classifierOutput[0][selectedPoseIndex];
                String status = (confidence > CONFIDENCE_THRESHOLD) ? "MATCH" : "NO MATCH";
                String confidenceText = String.format("%.2f%%", confidence * 100);

                runOnUiThread(() -> binding.resultTextView.setText(
                        "Target: " + poseLabels.get(selectedPoseIndex) +
                                "\nStatus: " + status +
                                "\nConfidence: " + confidenceText
                ));

            } catch (Exception e) {
                Log.e(TAG, "Error analyzing frame: " + e.getMessage(), e);
            } finally {
                imageProxy.close();
            }
        }

        // --- Helper Methods ---

        private Bitmap drawKeypointsOnBitmap(Bitmap original, float[][] keypoints) {
            Bitmap mutableBitmap = original.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(mutableBitmap);
            int width = original.getWidth();
            int height = original.getHeight();

            // Draw keypoints (circles)
            for (int i = 0; i < 17; i++) {
                if (keypoints[i][2] > 0.1) { // Check confidence
                    float x = keypoints[i][1] * width;
                    float y = keypoints[i][0] * height;
                    canvas.drawCircle(x, y, 5, keypointPaint);
                }
            }

            // Draw connections
            int[][] connections = {
                    {0, 1}, {0, 2}, {1, 3}, {2, 4}, {5, 6}, {5, 7}, {7, 9}, {6, 8}, {8, 10},
                    {5, 11}, {6, 12}, {11, 12}, {11, 13}, {13, 15}, {12, 14}, {14, 16}
            };

            for (int[] conn : connections) {
                int start = conn[0];
                int end = conn[1];
                if (keypoints[start][2] > 0.1 && keypoints[end][2] > 0.1) {
                    float startX = keypoints[start][1] * width;
                    float startY = keypoints[start][0] * height;
                    float endX = keypoints[end][1] * width;
                    float endY = keypoints[end][0] * height;
                    canvas.drawLine(startX, startY, endX, endY, connectionPaint);
                }
            }
            return mutableBitmap;
        }

        private Bitmap flipBitmap(Bitmap src) {
            android.graphics.Matrix matrix = new android.graphics.Matrix();
            // Flip horizontally
            matrix.preScale(-1.0f, 1.0f);
            return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        }

        // Converts Bitmap to ByteBuffer (0-255) for MoveNet
        private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(1 * bitmap.getHeight() * bitmap.getWidth() * 3)
                    .order(ByteOrder.nativeOrder());
            buffer.rewind();

            int[] pixels = new int[bitmap.getHeight() * bitmap.getWidth()];
            bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

            for (int pixel : pixels) {
                buffer.put((byte) Color.red(pixel));
                buffer.put((byte) Color.green(pixel));
                buffer.put((byte) Color.blue(pixel));
            }
            buffer.rewind();
            return buffer;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use the app.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (imageClassifier != null) imageClassifier.close();
        if (poseDetector != null) poseDetector.close();
        if (cameraExecutor != null) cameraExecutor.shutdown();
    }
}