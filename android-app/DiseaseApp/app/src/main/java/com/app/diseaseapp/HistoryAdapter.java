package com.app.diseaseapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// No Firebase operations here; handled in detail screen

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final Context context;
    private final ArrayList<Map<String, String>> data;
    private final ArrayList<String> keys;

    public HistoryAdapter(Context context, ArrayList<Map<String, String>> data, ArrayList<String> keys) {
        this.context = context;
        this.data = data;
        this.keys = keys;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history_card, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Map<String, String> item = data.get(position);
        String disease = item.get("disease_name");
        String plant = item.get("plant_name");
        String timestamp = item.get("timestamp");
        String desc = item.get("gemini_response");

        holder.txtTitle.setText(disease != null ? disease : "");
        holder.txtPlant.setText(plant != null && !plant.isEmpty() ? plant : holder.itemView.getContext().getString(R.string.unknown));
        holder.txtTime.setText(formatTimestamp(timestamp));
        holder.txtDesc.setText(desc != null ? desc : "");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, HistoryDetailActivity.class);
            intent.putExtra("disease_name", item.get("disease_name"));
            intent.putExtra("plant_name", plant);
            intent.putExtra("gemini_response", item.get("gemini_response"));
            intent.putExtra("timestamp", item.get("timestamp"));
            intent.putExtra("history_key", keys.get(position));
            context.startActivity(intent);
        });

        // Delete moved to detail screen
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtPlant, txtTime, txtDesc;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtDisease);
            txtPlant = itemView.findViewById(R.id.txtPlant);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtDesc = itemView.findViewById(R.id.txtDesc);
        }
    }

    private String formatTimestamp(String ts) {
        if (ts == null) return "";
        try {
            long ms = Long.parseLong(ts);
            Date date = new Date(ms);
            SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            return ts; // fallback to raw
        }
    }
}
