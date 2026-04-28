package com.app.diseaseapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageView imagePreview;
    private FloatingActionButton btnCapture;
    private com.google.android.material.button.MaterialButton btnIdentify;

    private ImageCapture imageCapture;
    private Uri lastPhotoUri;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) startCamera();
                else {
                    Toast.makeText(this, getString(R.string.camera_permission_required_short), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);
        imagePreview = findViewById(R.id.imagePreview);
        btnCapture = findViewById(R.id.btnCapture);
        btnIdentify = findViewById(R.id.btnIdentify);
        com.google.android.material.appbar.MaterialToolbar topBar = findViewById(R.id.topBar);
        topBar.setNavigationOnClickListener(v -> finish());

        btnCapture.setOnClickListener(v -> takePhoto());
        btnIdentify.setOnClickListener(v -> {
            if (lastPhotoUri != null) {
                Bitmap bmp = loadBitmapFromUri(lastPhotoUri);
                if (bmp != null) {
                    Bitmap scaled = scaleBitmap(bmp, 800);
                    Intent intent = new Intent(CameraActivity.this, ResultActivity.class);
                    intent.putExtra("capturedImage", scaled);
                    startActivity(intent);
                }
            } else {
                Toast.makeText(this, getString(R.string.please_capture_image_first), Toast.LENGTH_SHORT).show();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, getString(R.string.failed_to_start_camera), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis());
        java.io.File outputDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);

        if (outputDir == null) {
            Toast.makeText(this, getString(R.string.storage_not_available), Toast.LENGTH_SHORT).show();
            return;
        }

        java.io.File photoFile = new java.io.File(outputDir, name + ".jpg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                lastPhotoUri = Uri.fromFile(photoFile);
                Bitmap bmp = android.graphics.BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                if (bmp != null) imagePreview.setImageBitmap(bmp);
                Toast.makeText(CameraActivity.this, getString(R.string.photo_captured), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(CameraActivity.this, getString(R.string.capture_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap loadBitmapFromUri(Uri uri) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                return ImageDecoder.decodeBitmap(source);
            } else {
                return MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }
        } catch (IOException e) {
            return null;
        }
    }

    private Bitmap scaleBitmap(Bitmap src, int maxDim) {
        int w = src.getWidth();
        int h = src.getHeight();
        int max = Math.max(w, h);
        if (max <= maxDim) return src;
        float scale = (float) maxDim / (float) max;
        int newW = Math.round(w * scale);
        int newH = Math.round(h * scale);
        return Bitmap.createScaledBitmap(src, newW, newH, true);
    }
}
