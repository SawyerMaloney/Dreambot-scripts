package WhatAreYewDoing;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {
    private final String webhookUrl;

    public DiscordWebhook(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public int sendMessage(String message) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            String jsonPayload = String.format(
                    "{\"username\": \"%s\", \"content\": \"%s\"}",
                    "DreamBot Script",
                    message.replace("\"", "\\\"") // escape quotes
            );

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            connection.disconnect();
            return responseCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
