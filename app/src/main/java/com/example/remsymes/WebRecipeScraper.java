package com.example.remsymes;

import android.os.Handler;
import android.os.Looper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

public class WebRecipeScraper {

    public interface ScrapeCallback {
        void onSuccess(String scrapedText, Recipe draftRecipe);
    }

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void scrapeUrl(String urlString, ScrapeCallback callback) {
        executor.execute(() -> {
            String fullPageText = "";
            Recipe parsedDraft = null;

            try {
                String html = fetchHtml(urlString);
                if (html != null && !html.trim().isEmpty()) {
                    parsedDraft = RecipeScraperRegistry.forUrl(urlString).scrape(urlString, html);
                    if (parsedDraft != null) {
                        fullPageText = parsedDraft.getScrapedText();
                    } else {
                        fullPageText = extractRawTextFromHtml(html);
                    }
                }
            } catch (Exception e) {
                fullPageText = "Shared URL:\n" + urlString;
            }

            if (fullPageText == null || fullPageText.trim().isEmpty()) {
                fullPageText = urlString;
            }

            if (parsedDraft == null) {
                parsedDraft = LlmRecipeExtractor.extractRecipeFromText(fullPageText, urlString);
            }

            parsedDraft.setSourceLink(urlString);
            parsedDraft.setScrapedText(fullPageText);

            final String finalText = fullPageText;
            final Recipe finalDraft = parsedDraft;

            mainHandler.post(() -> callback.onSuccess(finalText, finalDraft));
        });
    }

    private static String fetchHtml(String urlString) throws Exception {
        String currentUrl = urlString;
        int redirectCount = 0;

        while (true) {
            URL url = new URI(currentUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            conn.setRequestProperty("Cache-Control", "no-cache");

            int responseCode = conn.getResponseCode();
            String location = conn.getHeaderField("Location");
            String redirectTarget = RedirectPolicy.nextRedirectUrl(responseCode, location, redirectCount);
            if (redirectTarget != null) {
                conn.disconnect();
                currentUrl = redirectTarget;
                redirectCount++;
                continue;
            }

            String encoding = conn.getContentEncoding();
            InputStream is = conn.getInputStream();
            if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
                is = new GZIPInputStream(is);
            }

            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                return BoundedTextReader.readAll(reader, BoundedTextReader.DEFAULT_MAX_CHARS);
            } finally {
                conn.disconnect();
            }
        }
    }

    static String extractRawTextFromHtml(String html) {
        return HtmlTextExtractor.extractRawTextFromHtml(html);
    }
}
