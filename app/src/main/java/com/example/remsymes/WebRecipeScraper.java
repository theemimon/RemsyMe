package com.example.remsymes;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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
                    parsedDraft = RecipeScrapersJava.scrape(urlString, html);
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
        URL url = new URL(urlString);
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
        if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == 307 || responseCode == 308) {
            String newUrl = conn.getHeaderField("Location");
            if (newUrl != null && !newUrl.isEmpty()) {
                conn.disconnect();
                return fetchHtml(newUrl);
            }
        }

        String encoding = conn.getContentEncoding();
        InputStream is = conn.getInputStream();
        if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
            is = new GZIPInputStream(is);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        conn.disconnect();
        return sb.toString();
    }

    static String extractRawTextFromHtml(String html) {
        if (html == null) return "";

        // Remove script, style, header, footer, nav tags completely
        String clean = html.replaceAll("(?s)<script.*?>.*?</script>", " ")
                .replaceAll("(?s)<style.*?>.*?</style>", " ")
                .replaceAll("(?s)<noscript.*?>.*?</noscript>", " ")
                .replaceAll("(?s)<header.*?>.*?</header>", " ")
                .replaceAll("(?s)<footer.*?>.*?</footer>", " ")
                .replaceAll("(?s)<nav.*?>.*?</nav>", " ");

        // Replace block tags with newlines
        clean = clean.replaceAll("(?i)<(p|div|h1|h2|h3|h4|h5|h6|li|tr|br|hr)[^>]*>", "\n")
                .replaceAll("<[^>]+>", " ");

        // Decode HTML entities
        clean = decodeHtmlEntities(clean);

        // Normalize multiple empty lines
        String[] lines = clean.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.replaceAll("\\s+", " ").trim();
            if (!trimmed.isEmpty() && trimmed.length() > 1) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(trimmed);
            }
        }

        return sb.toString();
    }

    private static String decodeHtmlEntities(String text) {
        if (text == null) return "";
        return text.replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&rsquo;", "'")
                .replace("&lsquo;", "'")
                .replace("&ldquo;", "\"")
                .replace("&rdquo;", "\"")
                .replace("&ndash;", "-")
                .replace("&mdash;", "—")
                .replace("&#189;", "1/2")
                .replace("&#188;", "1/4")
                .replace("&#190;", "3/4")
                .replace("&frac12;", "1/2")
                .replace("&frac14;", "1/4")
                .replace("&frac34;", "3/4");
    }
}