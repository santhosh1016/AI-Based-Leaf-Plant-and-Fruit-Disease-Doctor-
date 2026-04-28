package com.app.diseaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerHistory;
    private ArrayList<Map<String, String>> dataList;
    private ArrayList<String> keyList;
    private HistoryAdapter adapter;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerHistory = findViewById(R.id.recyclerHistory);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));

        dataList = new ArrayList<>();
        keyList = new ArrayList<>();
        adapter = new HistoryAdapter(this, dataList, keyList);
        recyclerHistory.setAdapter(adapter);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance()
                .getReference("DiseaseHistory")
                .child(userId);

        loadHistory();

        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setOnNavigationItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_logout)
            {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            if (item.getItemId() == R.id.nav_home)
            {
                startActivity(new Intent(this, HomeActivity.class));
            }


            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        if (dbRef == null) return;
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                keyList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Map<String, Object> item = (Map<String, Object>) child.getValue();
                    if (item != null) {
                        Map<String, String> mapped = new HashMap<>();
                        mapped.put("disease_name", (String) item.get("disease_name"));
                        mapped.put("plant_name", (String) item.get("plant_name"));
                        mapped.put("gemini_response", (String) item.get("gemini_response"));
                        mapped.put("timestamp", (String) item.get("timestamp"));
                        dataList.add(mapped);
                        keyList.add(child.getKey());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HistoryActivity.this, getString(R.string.history_load_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
