package com.app.diseaseapp;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatGPTHelper {
    public interface SuggestionCallback {
        void onSuggestionReceived(String suggestion);
    }

    public static void fetchCureSuggestion(String disease, SuggestionCallback callback) {
        OkHttpClient client = new OkHttpClient();
        String prompt = "What is the cure for " + disease + " in crop plants?";
        String json = "{\n" +
                "  \"model\": \"gpt-3.5-turbo\",\n" +
                "  \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}]\n" +
                "}";

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer YOUR_API_KEY_HERE")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                callback.onSuggestionReceived("Error: " + e.getMessage());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    String suggestion = obj.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                    callback.onSuggestionReceived(suggestion);
                } catch (Exception e) {
                    callback.onSuggestionReceived("Parsing error: " + e.getMessage());
                }
            }
        });
    }
}
