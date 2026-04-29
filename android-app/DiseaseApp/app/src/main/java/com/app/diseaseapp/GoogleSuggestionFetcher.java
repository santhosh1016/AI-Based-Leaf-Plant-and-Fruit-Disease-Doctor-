package com.app.diseaseapp;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

public class GoogleSuggestionFetcher {

 /*   private static final String API_KEY = "YOUR_GOOGLE_API_KEY";
    private static final String CX = "YOUR_SEARCH_ENGINE_ID";
    private static final String API_URL = "https://www.googleapis.com/customsearch/v1?q=%s&key=%s&cx=%s";
    private static String lastQuery = null;
    private static String cachedResult = null;*/



    private static final String API_KEY = "Your_API_Key";
    private static final String CX = "6369a1d9bd93f409b";
    private static final String API_URL = "https://www.googleapis.com/customsearch/v1?q=%s&key=%s&cx=%s";
    private static String lastQuery = null;
    private static String cachedResult = null;

    public static void fetchSuggestions(Context context, String diseaseName, TextView textView, ProgressBar progressBar, Button retryButton, ImageView imageView) {
        String query = diseaseName + " disease in plant and its cure";
        if (query.equals(lastQuery) && cachedResult != null) {
            textView.setText(cachedResult);
            return;
        }

        String url = String.format(API_URL, query.replace(" ", "+"), API_KEY, CX);
        RequestQueue queue = Volley.newRequestQueue(context);

        new Handler(Looper.getMainLooper()).post(() -> {
            progressBar.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.GONE);
        });

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    StringBuilder suggestions = new StringBuilder();
                    String imageUrl = null;

                    try {
                        JSONArray items = response.getJSONArray("items");
                        if (items.length() > 0) {
                            JSONObject item = items.getJSONObject(0);
                            String title = item.getString("title");
                            String snippet = item.getString("snippet");
                            suggestions.append("• ").append(title).append("\n").append(snippet).append("\n\n");

                            JSONObject pagemap = item.optJSONObject("pagemap");
                            if (pagemap != null && pagemap.has("cse_image")) {
                                JSONArray images = pagemap.optJSONArray("cse_image");
                                if (images != null && images.length() > 0) {
                                    imageUrl = images.getJSONObject(0).optString("src");
                                }
                            }
                        } else {
                            suggestions.append("No suggestions found.");
                        }
                    } catch (Exception e) {
                        suggestions.append("Error parsing response: ").append(e.getMessage());
                    }

                    String finalSuggestions = suggestions.toString();
                    String finalImageUrl = imageUrl;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        progressBar.setVisibility(View.GONE);
                        retryButton.setVisibility(View.GONE);
                        textView.setText(finalSuggestions);
                        lastQuery = query;
                        cachedResult = finalSuggestions;
                        if (finalImageUrl != null && !finalImageUrl.isEmpty()) {
                            Glide.with(context).load(finalImageUrl).into(imageView);
                        }
                    });
                },
                error -> new Handler(Looper.getMainLooper()).post(() -> {
                    progressBar.setVisibility(View.GONE);
                    retryButton.setVisibility(View.VISIBLE);
                    textView.setText("Google fetch failed: " + error.getMessage());
                    retryButton.setOnClickListener(v -> {
                        fetchSuggestions(context, diseaseName, textView, progressBar, retryButton, imageView);
                    });
                }));

        queue.add(request);
    }
}
