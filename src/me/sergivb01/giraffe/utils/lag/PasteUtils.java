package me.sergivb01.giraffe.utils.lag;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class PasteUtils {
    private static final String GIST_API = "https://api.github.com/gists";
    private static final String SHORTEN_API = "https://git.io";

    /**
     * Uploads content to GIST, and returns a shortened URL.
     *
     * @param desc the description of the gist
     * @param files the files to include in the gist (file name --> content)
     * @return the url, or null
     */
    public static String paste(String desc, List<Map.Entry<String, String>> files) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(GIST_API).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                StringWriter sw = new StringWriter();

                JsonWriter jw = new JsonWriter(sw)
                        .beginObject()
                        .name("description").value(desc)
                        .name("public").value(false)
                        .name("files")
                        .beginObject();

                for (Map.Entry<String, String> file : files) {
                    jw.name(file.getKey()).beginObject().name("content").value(file.getValue()).endObject();
                }

                jw.endObject().endObject();
                os.write(sw.toString().getBytes(StandardCharsets.UTF_8));
            }

            if (connection.getResponseCode() >= 400) {
                throw new RuntimeException("Connection returned response code: " + connection.getResponseCode() + " - " + connection.getResponseMessage());
            }

            String pasteUrl;
            try (InputStream inputStream = connection.getInputStream()) {
                try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                    JsonObject response = new Gson().fromJson(reader, JsonObject.class);
                    pasteUrl = response.get("html_url").getAsString();
                }
            }

            connection.disconnect();

            try {
                connection = (HttpURLConnection) new URL(SHORTEN_API).openConnection();
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(("url=" + pasteUrl).getBytes(StandardCharsets.UTF_8));
                }

                if (connection.getResponseCode() >= 400) {
                    new RuntimeException("Connection returned response code: " + connection.getResponseCode() + " - " + connection.getResponseMessage()).printStackTrace();
                } else {
                    String shortUrl = connection.getHeaderField("Location");
                    if (shortUrl != null) {
                        pasteUrl = shortUrl;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return pasteUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }


}