package com.app.diseaseapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView title = findViewById(R.id.detailDisease);
        TextView plantView = findViewById(R.id.detailPlant);
        TextView time = findViewById(R.id.detailTime);
        TextView response = findViewById(R.id.detailResponse);
        Button btnDelete = findViewById(R.id.btnDeleteDetail);

        String disease = getIntent().getStringExtra("disease_name");
        String plantName = getIntent().getStringExtra("plant_name");
        String timestamp = getIntent().getStringExtra("timestamp");
        String geminiResponse = getIntent().getStringExtra("gemini_response");
        String key = getIntent().getStringExtra("history_key");

        title.setText(disease != null ? disease : "");
        plantView.setText(plantName != null && !plantName.isEmpty() ? plantName : getString(R.string.unknown));
        time.setText(formatTimestamp(timestamp));
        response.setText(geminiResponse != null ? geminiResponse : "");

        // Set toolbar title dynamically to disease name
        String toolbarTitle = (disease != null && !disease.isEmpty()) ? disease : getString(R.string.app_name);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(toolbarTitle);
        } else {
            toolbar.setTitle(toolbarTitle);
        }

        btnDelete.setOnClickListener(v -> {
            try {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("DiseaseHistory")
                        .child(userId).child(key);
                dbRef.removeValue();
                Toast.makeText(this, getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                finish();
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.delete_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatTimestamp(String ts) {
        if (ts == null) return "";
        try {
            long ms = Long.parseLong(ts);
            Date date = new Date(ms);
            SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            return ts;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
