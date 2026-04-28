package com.app.diseaseapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imageView;
    private TextView resultText;
    private Button btnCapture, btnPredict;
    private Bitmap capturedImage;
    private Classifier classifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        resultText = findViewById(R.id.resultText);
        btnCapture = findViewById(R.id.btnCapture);
        btnPredict = findViewById(R.id.btnPredict);

        try {
            classifier = new Classifier(this);
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.model_loading_failed, e.getMessage()), Toast.LENGTH_LONG).show();
        }

        btnCapture.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        });

        btnPredict.setOnClickListener(v -> {
            if (capturedImage != null && classifier != null) {
                String prediction = classifier.classify(capturedImage);
                resultText.setText(getString(R.string.detected_with_name, prediction));

                // Optionally move to ResultActivity for cure suggestions
                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                intent.putExtra("disease_name", prediction);
                startActivity(intent);
            } else {
                Toast.makeText(this, getString(R.string.capture_image_first), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            capturedImage = (Bitmap) extras.get("data");
            imageView.setImageBitmap(capturedImage);
        }
    }
}
