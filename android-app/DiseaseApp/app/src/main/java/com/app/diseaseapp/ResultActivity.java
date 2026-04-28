package com.app.diseaseapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultActivity extends AppCompatActivity {

    private TextView txtDiseaseHeader;
    private TextView tvcheck;
    private TextView valueDiseaseName;
    private TextView valueSymptoms;
    private TextView valueCause;
    private TextView valueCare;
    private TextView valueTreatment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        ProgressBar progressBar = findViewById(R.id.suggestionProgress);
        Button retryButton = findViewById(R.id.retryButton);
        ImageView diseaseImage = findViewById(R.id.diseaseImage);
        tvcheck = findViewById(R.id.tvcheckdisease);
        valueDiseaseName = findViewById(R.id.valueDiseaseName);
        valueSymptoms = findViewById(R.id.valueSymptoms);
        valueCause = findViewById(R.id.valueCause);
        valueCare = findViewById(R.id.valueCare);
        valueTreatment = findViewById(R.id.valueTreatment);

        String diseaseRaw = getIntent().getStringExtra("disease_name");
        final String disease = (diseaseRaw == null || diseaseRaw.trim().isEmpty()) ? "Unknown" : diseaseRaw;
        Bitmap capturedImage = getIntent().getParcelableExtra("capturedImage");
        diseaseImage.setImageBitmap(capturedImage);

        txtDiseaseHeader = findViewById(R.id.txtDisease);

        progressBar.setVisibility(ProgressBar.VISIBLE);
        retryButton.setVisibility(Button.GONE);

        GeminiVisionHelper.Callback textCallback = new GeminiVisionHelper.Callback() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    retryButton.setVisibility(Button.GONE);

                    ParsedResponse parsed = parseGeminiResponse(result);
                    valueDiseaseName.setText(parsed.disease.isEmpty() ? disease : parsed.disease);
                    valueSymptoms.setText(parsed.symptoms.isEmpty() ? "Not available" : parsed.symptoms);
                    valueCause.setText(parsed.cause.isEmpty() ? "Not available" : parsed.cause);
                    valueCare.setText(parsed.care.isEmpty() ? "Not available" : parsed.care);
                    valueTreatment.setText(parsed.treatment.isEmpty() ? "Not available" : parsed.treatment);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    valueDiseaseName.setText(disease);
                    valueSymptoms.setText("Gemini fetch failed: " + error);
                    valueCause.setText("-");
                    valueCare.setText("-");
                    valueTreatment.setText("-");
                    retryButton.setVisibility(Button.VISIBLE);
                });
            }
        };

        retryButton.setOnClickListener(v -> {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            retryButton.setVisibility(Button.GONE);
            GeminiVisionHelper.generateContentFromDiseaseName(disease, textCallback);
        });

        GeminiVisionHelper.generateContentFromDiseaseName(disease, textCallback);

        txtDiseaseHeader.setText("Detected Disease: " + disease);

        tvcheck.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        });

        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        if (nav != null) {
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

                if (item.getItemId() == R.id.nav_home) {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                }

                return false;
            });
        }
    }

    private ParsedResponse parseGeminiResponse(String text) {
        ParsedResponse parsed = new ParsedResponse();
        if (text == null) return parsed;

        Map<String, StringBuilder> sections = new HashMap<>();
        String currentKey = null;
        Pattern pattern = Pattern.compile("(?i)^(disease|symptoms|cause|care|treatment)\\s*:\\s*(.*)$");

        for (String rawLine : text.split("\\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                currentKey = matcher.group(1).toLowerCase();
                sections.putIfAbsent(currentKey, new StringBuilder());
                String remainder = matcher.group(2).trim();
                if (!remainder.isEmpty()) {
                    sections.get(currentKey).append(remainder);
                }
                continue;
            }

            if (currentKey != null) {
                sections.putIfAbsent(currentKey, new StringBuilder());
                StringBuilder builder = sections.get(currentKey);
                if (builder.length() > 0) builder.append("\n");
                builder.append(line);
            }
        }

        parsed.disease = getSection(sections, "disease");
        parsed.symptoms = getSection(sections, "symptoms");
        parsed.cause = getSection(sections, "cause");
        parsed.care = getSection(sections, "care");
        parsed.treatment = getSection(sections, "treatment");

        if (parsed.disease.isEmpty() && !sections.isEmpty()) {
            parsed.disease = text.split("\\n")[0].trim();
        }

        return parsed;
    }

    private String getSection(Map<String, StringBuilder> sections, String key) {
        StringBuilder builder = sections.get(key);
        return builder == null ? "" : builder.toString().trim();
    }

    private static class ParsedResponse {
        String disease = "";
        String symptoms = "";
        String cause = "";
        String care = "";
        String treatment = "";
    }
}
