package me.remsy;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TikTok has shipped at least two different embedded-JSON formats for the video description:
 * the legacy {@code <script id="SIGI_STATE">} blob ({@code ItemModule.<videoId>.desc}), and the
 * current {@code <script id="__UNIVERSAL_DATA_FOR_REHYDRATION__">} blob
 * ({@code __DEFAULT_SCOPE__["webapp.video-detail"].itemInfo.itemStruct.desc}). We try both,
 * then fall back to {@code og:description} if neither is found (TikTok may change this again).
 */
public final class TikTokRecipeScraper implements RecipeSiteScraper {

    @Override
    public boolean supports(String url) {
        return UrlHosts.hostMatches(url, "tiktok.com");
    }

    @Override
    public Recipe scrape(String url, String html) {
        String description = extractDescription(html);
        if (description == null || description.trim().isEmpty()) {
            return new GenericRecipeScraper().scrape(url, html);
        }
        return LlmRecipeExtractor.extractRecipeFromText(description, url);
    }

    static String extractDescription(String html) {
        String fromState = extractFromSigiState(html);
        if (fromState != null && !fromState.trim().isEmpty()) {
            return fromState;
        }
        String fromUniversalData = extractFromUniversalData(html);
        if (fromUniversalData != null && !fromUniversalData.trim().isEmpty()) {
            return fromUniversalData;
        }
        return MetaTagExtractor.extractOgDescription(html);
    }

    static String extractFromSigiState(String html) {
        String json = extractScriptJsonById(html, "SIGI_STATE");
        if (json == null) return null;
        try {
            JSONObject root = new JSONObject(json);
            JSONObject itemModule = root.optJSONObject("ItemModule");
            if (itemModule == null) return null;
            Iterator<String> keys = itemModule.keys();
            if (keys.hasNext()) {
                JSONObject item = itemModule.getJSONObject(keys.next());
                return item.optString("desc", null);
            }
        } catch (Exception ignored) {
            // Malformed/unexpected JSON shape; caller falls back to the next strategy.
        }
        return null;
    }

    static String extractFromUniversalData(String html) {
        String json = extractScriptJsonById(html, "__UNIVERSAL_DATA_FOR_REHYDRATION__");
        if (json == null) return null;
        try {
            JSONObject root = new JSONObject(json);
            JSONObject scope = root.optJSONObject("__DEFAULT_SCOPE__");
            if (scope == null) return null;
            JSONObject videoDetail = scope.optJSONObject("webapp.video-detail");
            if (videoDetail == null) return null;
            JSONObject itemInfo = videoDetail.optJSONObject("itemInfo");
            if (itemInfo == null) return null;
            JSONObject itemStruct = itemInfo.optJSONObject("itemStruct");
            if (itemStruct == null) return null;
            return itemStruct.optString("desc", null);
        } catch (Exception ignored) {
            // Malformed/unexpected JSON shape; caller falls back to og:description.
        }
        return null;
    }

    static String extractScriptJsonById(String html, String scriptId) {
        if (html == null) return null;
        Pattern pattern = Pattern.compile(
                "<script[^>]*\\bid=[\"']" + Pattern.quote(scriptId) + "[\"'][^>]*>(.*?)</script>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        return matcher.find() ? matcher.group(1) : null;
    }
}
