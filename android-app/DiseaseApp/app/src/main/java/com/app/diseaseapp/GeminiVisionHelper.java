package com.app.diseaseapp;


import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class GeminiVisionHelper {

    public interface Callback {
        void onSuccess(String result);
        void onFailure(String error);
    }

    public static void identifyDiseaseFromImage(Bitmap image, String languageCode, Callback callback) {
        new Thread(() -> {
            try {
              
                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=Your_API_Key");
              
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);

                JSONObject inlineData = new JSONObject();
                inlineData.put("mimeType", "image/jpeg");
                inlineData.put("data", base64Image);

                JSONArray parts = new JSONArray();
                String targetLanguage = languageCode != null && languageCode.startsWith("hi") ? "Hindi" : "English";
                String visionPrompt = "Identify the plant or fruit disease in this image. " +
                        "Reply in plain text (no markdown) under 120 words in " + targetLanguage + " using this template:\n" +
                        "Plant: <plant or crop name>\n" +
                        "Disease: <name>\n" +
                        "Symptoms: <2 short points>\n" +
                        "Cause: <short cause>\n" +
                        "Care:\n" +
                        "- Temperature: <range>\n" +
                        "- Water: <brief watering>\n" +
                        "- Light: <brief light>\n" +
                        "Treatment: 1) <organic> 2) <chemical>.";

                parts.put(new JSONObject().put("text", visionPrompt));
                parts.put(new JSONObject().put("inlineData", inlineData));

                JSONObject contentsItem = new JSONObject();
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
                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder result = new StringBuilder();
                while (scanner.hasNextLine()) {
                    result.append(scanner.nextLine());
                }
                scanner.close();

                JSONObject responseJson = new JSONObject(result.toString());
                JSONArray candidates = responseJson.getJSONArray("candidates");
                JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
                JSONArray partsArray = content.getJSONArray("parts");
                String diseaseInfo = partsArray.getJSONObject(0).getString("text");

                callback.onSuccess(diseaseInfo);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFailure(e.getMessage());
            }
        }).start();
    }

    public static void identifyDiseaseFromImage(Bitmap image, Callback callback) {
        identifyDiseaseFromImage(image, "en", callback);
    }

    public static void generateContentFromDiseaseName(String diseaseName, String languageCode, Callback callback) {
        new Thread(() -> {
            try {
                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=AIzaSyApwkIV1jUDxvo-yrbWIpT34Kiud7Qu2iU");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String targetLanguage = languageCode != null && languageCode.startsWith("hi") ? "Hindi" : "English";
                String prompt = "Give a concise plain-text guide for '" + diseaseName + "' under 100 words in " + targetLanguage + "." +
                        "\nDisease: <name>" +
                        "\nSymptoms: <2 short points>" +
                        "\nPrevention: <2 bullets>" +
                        "\nTreatment: 1) <organic or cultural> 2) <chemical>." +
                        "\nEnvironment: Temperature=<range>; Water=<brief>; Light=<brief>.";

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
                JSONArray candidates = responseJson.getJSONArray("candidates");
                if (candidates.length() == 0) {
                    callback.onFailure("No candidates in response");
                    return;
                }
                JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
                JSONArray partsArray = content.getJSONArray("parts");
                if (partsArray.length() == 0 || partsArray.getJSONObject(0).isNull("text")) {
                    callback.onFailure("Empty text in response");
                    return;
                }
                String info = partsArray.getJSONObject(0).getString("text");
                if (info == null || info.trim().isEmpty()) {
                    callback.onFailure("Blank text in response");
                    return;
                }
                callback.onSuccess(info);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFailure(e.getMessage());
            }
        }).start();
    }

    public static void generateContentFromDiseaseName(String diseaseName, Callback callback) {
        generateContentFromDiseaseName(diseaseName, "en", callback);
    }

    public static void generateContentFromQuery(String query, String languageCode, Callback callback) {
        new Thread(() -> {
            try {
                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=AIzaSyApwkIV1jUDxvo-yrbWIpT34Kiud7Qu2iU");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String targetLanguage = languageCode != null && languageCode.startsWith("hi") ? "Hindi" : "English";
                String prompt = "You are an agronomy assistant. Answer the user's question about fruits/plants or their diseases. Always reply in plain text under 120 words in " + targetLanguage + " using this exact template:" +
                        "\nDisease: <name or topic>" +
                        "\nSymptoms: <2 short points or N/A>" +
                        "\nCause: <brief cause or N/A>" +
                        "\nCare: <brief preventive tips>" +
                        "\nTreatment: 1) <organic or cultural> 2) <chemical if relevant>." +
                        "\nQuestion: " + query;

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
                JSONArray candidates = responseJson.getJSONArray("candidates");
                if (candidates.length() == 0) {
                    callback.onFailure("No candidates in response");
                    return;
                }
                JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
                JSONArray partsArray = content.getJSONArray("parts");
                if (partsArray.length() == 0 || partsArray.getJSONObject(0).isNull("text")) {
                    callback.onFailure("Empty text in response");
                    return;
                }
                String info = partsArray.getJSONObject(0).getString("text");
                if (info == null || info.trim().isEmpty()) {
                    callback.onFailure("Blank text in response");
                    return;
                }
                callback.onSuccess(info);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFailure(e.getMessage());
            }
        }).start();
    }

    public static void generateContentFromQuery(String query, Callback callback) {
        generateContentFromQuery(query, "en", callback);
    }

    public static String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }
}
