package me.remsy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * YouTube watch pages embed the full video description (where recipe creators usually put the
 * actual recipe) as {@code "shortDescription":"..."} inside the server-rendered
 * {@code ytInitialPlayerResponse} JSON blob. We pull just that string value out with a regex
 * (the surrounding object is too large/dynamic to reliably bracket-match) rather than parsing
 * the whole blob, and fall back to the {@code og:description} meta tag (often truncated) if
 * that key isn't found.
 */
public final class YouTubeRecipeScraper implements RecipeSiteScraper {

    private static final Pattern SHORT_DESCRIPTION_PATTERN =
            Pattern.compile("\"shortDescription\":\"((?:\\\\.|[^\"\\\\])*)\"");

    @Override
    public boolean supports(String url) {
        return UrlHosts.hostMatches(url, "youtube.com", "youtu.be");
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
        if (html == null) return null;
        Matcher matcher = SHORT_DESCRIPTION_PATTERN.matcher(html);
        if (matcher.find()) {
            return JsonStringUnescaper.unescape(matcher.group(1));
        }
        return MetaTagExtractor.extractOgDescription(html);
    }
}
