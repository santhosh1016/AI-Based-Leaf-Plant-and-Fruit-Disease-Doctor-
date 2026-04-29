package com.app.diseaseapp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class GeminiQAHelper {

    public interface Callback {
        void onSuccess(String result);
        void onFailure(String error);
    }

    public static void askQuestion(String query, String languageCode, Callback callback) {
        new Thread(() -> {
            try {
                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=Your_API_Key");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String targetLanguage = languageCode != null && languageCode.startsWith("hi") ? "Hindi" : "English";
                String prompt = "You are an agronomy assistant. Answer the user's fruit/plant/disease question in 4-5 concise lines, plain text, no bullets. Each line should be short and practical. Respond in " + targetLanguage + ". Question: " + query;

                JSONArray parts = new JSONArray();
                parts.put(new JSONObject().put("text", prompt));

                JSONObject contentsItem = new JSONObject();
                contentsItem.put("role", "user");
                contentsItem.put("parts", parts);
                JSONArray contentsArray = new JSONArray();
                contentsArray.put(contentsItem);

                JSONObject requestBody = new JSONObject();
                requestBody.put("contents", contentsArray);

                OutputStream os = conn.getOutputStream();
                os.write(requestBody.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                java.io.InputStream is = responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
                if (is == null) {
                    callback.onFailure("Empty response stream (code " + responseCode + ")");
                    return;
                }
                Scanner scanner = new Scanner(is);
                StringBuilder result = new StringBuilder();
                while (scanner.hasNextLine()) {
                    result.append(scanner.nextLine());
                }
                scanner.close();

                if (responseCode >= 400) {
                    callback.onFailure("HTTP " + responseCode + ": " + result.toString());
                    return;
                }

                JSONObject responseJson = new JSONObject(result.toString());
                JSONArray candidates = responseJson.optJSONArray("candidates");
                if (candidates == null || candidates.length() == 0) {
                    callback.onFailure("No candidates in response");
                    return;
                }
                JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
                if (content == null) {
                    callback.onFailure("Missing content in response");
                    return;
                }
                JSONArray partsArray = content.optJSONArray("parts");
                if (partsArray == null || partsArray.length() == 0 || partsArray.getJSONObject(0).isNull("text")) {
                    callback.onFailure("Empty text in response");
                    return;
                }
                String info = partsArray.getJSONObject(0).getString("text");
                if (info == null || info.trim().isEmpty()) {
                    callback.onFailure("Blank text in response");
                    return;
                }
                callback.onSuccess(info.trim());

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFailure(e.getMessage());
            }
        }).start();
    }

    public static void askQuestion(String query, Callback callback) {
        askQuestion(query, "en", callback);
    }
}
