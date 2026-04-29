package com.app.diseaseapp;


import android.content.Context;
import android.graphics.Bitmap;
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

public class GeminiFetcher {

    private static final String API_KEY = "Your_API_Key";

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=Your_API_Key";

    public static void fetchFromGemini(Context context, String diseaseName, TextView outputTextView, ProgressBar progressBar, Button retryButton, ImageView imageView, Bitmap fallbackImage) {
        String targetLanguage = LanguageManager.getSavedLanguage(context).startsWith("hi") ? "Hindi" : "English";
        String prompt = "Give a short summary about '" + diseaseName + "' including symptoms and how to cure it step by step. Also suggest common organic treatment methods. Respond in " + targetLanguage + ".";

        JSONObject requestBody = new JSONObject();
        try {
            JSONArray parts = new JSONArray();
            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);
            parts.put(textPart);

            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            content.put("role", "user");
            content.put("parts", parts);
            contents.put(content);

            requestBody.put("contents", contents);
        } catch (Exception e) {
            outputTextView.setText("Error creating request body: " + e.getMessage());
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            progressBar.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.GONE);
        });

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, API_URL, requestBody,
                response -> {
                    StringBuilder reply = new StringBuilder();
                    try {
                        JSONArray candidates = response.getJSONArray("candidates");
                        if (candidates.length() > 0) {
                            JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
                            JSONArray parts = content.getJSONArray("parts");
                            if (parts.length() > 0) {
                                reply.append(parts.getJSONObject(0).getString("text"));
                            }
                        }
                    } catch (Exception e) {
                        reply.append("Error parsing Gemini response: ").append(e.getMessage());
                    }

                    String replyText = reply.toString();
                    if ((replyText == null || replyText.trim().isEmpty() || "null".equalsIgnoreCase(replyText.trim())) && fallbackImage != null) {
                        GeminiVisionHelper.identifyDiseaseFromImage(fallbackImage, LanguageManager.getSavedLanguage(context), new GeminiVisionHelper.Callback() {
                            @Override
                            public void onSuccess(String result) {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    progressBar.setVisibility(View.GONE);
                                    outputTextView.setText(result);
                                    retryButton.setVisibility(View.GONE);
                                });
                            }

                            @Override
                            public void onFailure(String error) {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    progressBar.setVisibility(View.GONE);
                                    outputTextView.setText("Gemini text fetch empty; vision fallback failed: " + error);
                                    retryButton.setVisibility(View.VISIBLE);
                                });
                            }
                        });
                    } else {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            progressBar.setVisibility(View.GONE);
                            outputTextView.setText(replyText == null || replyText.trim().isEmpty() ? ("No information found for " + diseaseName) : replyText);
                            retryButton.setVisibility(View.GONE);
                        });
                    }
                },
                error -> {
                    String message = error.getMessage();
                    String body = "";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        body = new String(error.networkResponse.data);
                    }
                    String fullMessage = "Gemini fetch failed: " + message + "\n" + body;

                    new Handler(Looper.getMainLooper()).post(() -> {
                        progressBar.setVisibility(View.GONE);
                        retryButton.setVisibility(View.VISIBLE);
                        outputTextView.setText(fullMessage);
                        retryButton.setOnClickListener(v -> fetchFromGemini(context, diseaseName, outputTextView, progressBar, retryButton, imageView, fallbackImage));
                    });
                }
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }
}
