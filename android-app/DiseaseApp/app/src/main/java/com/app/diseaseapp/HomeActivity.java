package com.app.diseaseapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import android.widget.ProgressBar;

public class HomeActivity extends AppCompatActivity {

    LinearLayout plantScanner, fruitScanner;
    private static final int REQUEST_IMAGE_CAPTURE = 1, REQUEST_IMAGE_PICK = 2;
    private Bitmap capturedImage;
    private Classifier classifier;
    ProgressBar progressBar;
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LanguageManager.applyAppLocale(this);
        setContentView(R.layout.activity_home);

        plantScanner = findViewById(R.id.plantScanner);
        fruitScanner = findViewById(R.id.fruitScanner);
        progressBar = findViewById(R.id.progressBar);
        searchBar = findViewById(R.id.searchBar);
        setupLanguageToggle();

        try {
            classifier = new Classifier(this);
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.model_loading_failed, e.getMessage()), Toast.LENGTH_LONG).show();
        }

        //      plantScanner.setOnClickListener(v -> startActivity(new Intent(this, CameraActivity.class)));


        // fruitScanner.setOnClickListener(v -> startActivity(new Intent(this, CameraActivity.class)));

        plantScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        });

        fruitScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_IMAGE_PICK);
            }
        });

        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            boolean isEnter = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN;
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || isEnter) {
                String query = searchBar.getText().toString().trim();
                if (TextUtils.isEmpty(query)) {
                    Toast.makeText(this, getString(R.string.please_enter_question), Toast.LENGTH_SHORT).show();
                    return true;
                }
                askGemini(query);
                return true;
            }
            return false;
        });
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setOnNavigationItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            if (item.getItemId() == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
            }


            return false;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String languageCode = LanguageManager.getSavedLanguage(this);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            capturedImage = (Bitmap) extras.get("data");
            // imageView.setImageBitmap(capturedImage);

            if (capturedImage != null && classifier != null) {
                String prediction = classifier.classify(capturedImage);
                //  resultText.setText("Detected: " + prediction);

                // Optionally move to ResultActivity for cure suggestions
                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                intent.putExtra("disease_name", prediction);
                intent.putExtra("capturedImage", capturedImage);

                startActivity(intent);
            } else {
                Toast.makeText(this, getString(R.string.capture_image_first), Toast.LENGTH_SHORT).show();
            }
        }


        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            capturedImage = (Bitmap) extras.get("data");

            if (capturedImage != null) {
                progressBar.setVisibility(View.VISIBLE); // 👈 Show progress bar

                GeminiVisionHelper.identifyDiseaseFromImage(capturedImage, languageCode, new GeminiVisionHelper.Callback() {
                    @Override
                    public void onSuccess(String result) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE); // 👈 Hide progress bar

                            Intent intent = new Intent(HomeActivity.this, DiseaseInfoActivity.class);
                            intent.putExtra("result", result);
                            startActivity(intent);
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE); // 👈 Hide on failure
                            Toast.makeText(HomeActivity.this, getString(R.string.generic_error_with_reason, error), Toast.LENGTH_LONG).show();
                        });
                    }
                });
            } else {
                Toast.makeText(this, getString(R.string.capture_image_first), Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void askGemini(String query) {
        progressBar.setVisibility(View.VISIBLE);
        String languageCode = LanguageManager.getSavedLanguage(this);
        GeminiQAHelper.askQuestion(query, languageCode, new GeminiQAHelper.Callback() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(HomeActivity.this, QnAResultActivity.class);
                    intent.putExtra("query", query);
                    intent.putExtra("answer", result);
                    startActivity(intent);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(HomeActivity.this, getString(R.string.gemini_error_with_reason, error), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setupLanguageToggle() {
        MaterialButtonToggleGroup languageToggle = findViewById(R.id.languageToggle);

        String savedLanguage = LanguageManager.getSavedLanguage(this);
        languageToggle.check("hi".equals(savedLanguage) ? R.id.btnHindi : R.id.btnEnglish);

        languageToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            String selectedLanguage = checkedId == R.id.btnHindi ? "hi" : "en";
            if (!selectedLanguage.equals(LanguageManager.getSavedLanguage(this))) {
                LanguageManager.setLocale(this, selectedLanguage, true);
                recreate();
            }
        });
    }
}


/*
*
*     if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            capturedImage = (Bitmap) extras.get("data");
            // imageView.setImageBitmap(capturedImage);

            if (capturedImage != null) {
                GeminiVisionHelper.identifyDiseaseFromImage(capturedImage, languageCode, new GeminiVisionHelper.Callback() {
                    @Override
                    public void onSuccess(String result) {
                        runOnUiThread(() -> {
                            // Show in a dialog or new activity
//                            new AlertDialog.Builder(HomeActivity.this)
//                                    .setTitle("Disease Detection Result")
//                                    .setMessage(result)
//                                    .setPositiveButton("OK", null)
//                                    .show();

                            Intent intent = new Intent(HomeActivity.this, DiseaseInfoActivity.class);
                            intent.putExtra("result", result);
                            startActivity(intent);
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(HomeActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
            } else {
                Toast.makeText(this, "Capture image first!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    */
