package com.app.diseaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiseaseInfoActivity extends AppCompatActivity {

    private String diseaseName;
    private String plantName;
    private String result;

    private TextView valueDisease;
    private TextView valuePlant;
    private TextView valueSymptoms;
    private TextView valueCause;
    private TextView valueCare;
    private TextView valueTreatment;
    private TextView rawResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_info);

        valuePlant = findViewById(R.id.valuePlant);
        valueDisease = findViewById(R.id.valueDisease);
        valueSymptoms = findViewById(R.id.valueSymptoms);
        valueCause = findViewById(R.id.valueCause);
        valueCare = findViewById(R.id.valueCare);
        valueTreatment = findViewById(R.id.valueTreatment);
        rawResponse = findViewById(R.id.rawResponse);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnShare = findViewById(R.id.btnShare);

        diseaseName = getIntent().getStringExtra("diseaseName");
        result = getIntent().getStringExtra("result");
        if (result == null) {
            result = "";
        }

        ParsedResponse parsed = parseGeminiResponse(result);

        plantName = extractPlantName(result, diseaseName);

        if (diseaseName == null || diseaseName.trim().isEmpty()) {
            diseaseName = !parsed.disease.isEmpty() ? parsed.disease : extractDiseaseName(result);
        }

        String finalDisease = firstNonEmpty(parsed.disease, diseaseName, getString(R.string.unknown));
        diseaseName = finalDisease;

        String finalPlant = firstNonEmpty(parsed.plant, plantName, getString(R.string.unknown));
        valuePlant.setText(finalPlant);
        valueDisease.setText(finalDisease);
        valueSymptoms.setText(firstNonEmpty(parsed.symptoms, "Not available"));
        valueCause.setText(firstNonEmpty(parsed.cause, "Not available"));
        valueCare.setText(firstNonEmpty(parsed.care, "Not available"));
        valueTreatment.setText(firstNonEmpty(parsed.treatment, "Not available"));
        rawResponse.setText(result);

        btnSave.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference dbRef = FirebaseDatabase.getInstance()
                    .getReference("DiseaseHistory")
                    .child(userId)
                    .push();

            dbRef.child("disease_name").setValue(diseaseName);
            dbRef.child("plant_name").setValue(finalPlant);
            dbRef.child("gemini_response").setValue(result);
            dbRef.child("timestamp").setValue(
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

            Toast.makeText(this, getString(R.string.saved_to_firebase), Toast.LENGTH_SHORT).show();
        });

        btnShare.setOnClickListener(v -> {
            String shareText = "Plant: " + finalPlant +
                    "\nDisease: " + diseaseName +
                    "\n\nSymptoms:\n" + valueSymptoms.getText().toString() +
                    "\n\nCause:\n" + valueCause.getText().toString() +
                    "\n\nCare:\n" + valueCare.getText().toString() +
                    "\n\nTreatment:\n" + valueTreatment.getText().toString() +
                    "\n\nFull Response:\n" + result;
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Plant Disease Info");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Share using"));
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
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
            }


            return false;
        });
    }

    private String extractDiseaseName(String text) {
        if (text == null) return "";
        try {
            // 1) Look for explicit labels like "Disease:" / "Diagnosis:"
            java.util.regex.Pattern p1 = java.util.regex.Pattern.compile("(?i)(?:disease|diagnosis|condition)\\s*[:\\-]\\s*([^\\n.]{3,})");
            java.util.regex.Matcher m1 = p1.matcher(text);
            if (m1.find()) {
                return cleanName(m1.group(1));
            }

            // 2) Try capturing a phrase ending with known disease keywords
            java.util.regex.Pattern p2 = java.util.regex.Pattern.compile("(?i)([A-Za-z][A-Za-z0-9\'()\\-/ ]{2,}?(?: blight| rot| mildew| rust| spot| wilt| canker| scorch| leaf curl| mosaic))");
            java.util.regex.Matcher m2 = p2.matcher(text);
            if (m2.find()) {
                return cleanName(m2.group(1));
            }

            // 3) Fallback to first non-empty line/sentence
            String firstLine = text.split("\n")[0];
            if (firstLine.length() > 80) {
                int dot = firstLine.indexOf('.');
                if (dot > 0) firstLine = firstLine.substring(0, dot);
            }
            return cleanName(firstLine);
        } catch (Exception e) {
            return "";
        }
    }

    private String cleanName(String s) {
        if (s == null) return "";
        String trimmed = s.trim();
        trimmed = trimmed.replaceAll("^[\u2013\u2014\\-:\\s]+|[\u2013\u2014\\-:,.\\s]+$", "");
        trimmed = trimmed.replaceAll("\\s{2,}", " ");
        return trimmed;
    }

    private ParsedResponse parseGeminiResponse(String text) {
        ParsedResponse parsed = new ParsedResponse();
        if (text == null) return parsed;

        Map<String, StringBuilder> sections = new HashMap<>();
        String currentKey = null;
        Pattern pattern = Pattern.compile("(?i)^(plant|disease|symptoms|cause|care|treatment)\\s*:\\s*(.*)$");

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
        parsed.plant = getSection(sections, "plant");
        parsed.symptoms = getSection(sections, "symptoms");
        parsed.cause = getSection(sections, "cause");
        parsed.care = getSection(sections, "care");
        parsed.treatment = getSection(sections, "treatment");

        return parsed;
    }

    private String getSection(Map<String, StringBuilder> sections, String key) {
        StringBuilder builder = sections.get(key);
        return builder == null ? "" : builder.toString().trim();
    }

    private String extractPlantName(String text, String fallbackDisease) {
        if (text != null) {
            Pattern p = Pattern.compile("(?i)(?:plant|crop|host)\\s*[:\\-]\\s*([^\\n.]{3,})");
            Matcher m = p.matcher(text);
            if (m.find()) {
                return cleanName(m.group(1));
            }
        }
        if (fallbackDisease != null) {
            String[] parts = fallbackDisease.split("\\s+");
            if (parts.length > 0) {
                return cleanName(parts[0]);
            }
        }
        return "";
    }

    private String firstNonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return v.trim();
            }
        }
        return "";
    }

    private static class ParsedResponse {
        String disease = "";
        String plant = "";
        String symptoms = "";
        String cause = "";
        String care = "";
        String treatment = "";
    }
}
