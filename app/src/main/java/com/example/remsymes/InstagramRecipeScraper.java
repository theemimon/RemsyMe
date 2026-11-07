package com.example.remsymes;

/**
 * Instagram posts/reels don't expose their caption in easily-queryable JSON for logged-out
 * requests, but the server-rendered page still carries it in the {@code og:description} meta
 * tag (used for link previews), formatted like:
 * {@code "50K likes, 230 comments - username on Month Day, 2026: \"Actual caption here\""}.
 * We strip that engagement-stats prefix and feed the remaining caption text through the
 * generic heuristic recipe parser.
 */
public final class InstagramRecipeScraper implements RecipeSiteScraper {

    @Override
    public boolean supports(String url) {
        return UrlHosts.hostMatches(url, "instagram.com");
    }

    @Override
    public Recipe scrape(String url, String html) {
        String caption = extractCaption(html);
        if (caption == null || caption.trim().isEmpty()) {
            return new GenericRecipeScraper().scrape(url, html);
        }
        return LlmRecipeExtractor.extractRecipeFromText(caption, url);
    }

    static String extractCaption(String html) {
        return stripEngagementPrefix(MetaTagExtractor.extractOgDescription(html));
    }

    /** Strips a leading "N likes, N comments - user on date: " prefix, if present. */
    static String stripEngagementPrefix(String ogDescription) {
        if (ogDescription == null) return null;
        int marker = ogDescription.indexOf(": \"");
        if (marker != -1 && ogDescription.endsWith("\"")) {
            return ogDescription.substring(marker + 3, ogDescription.length() - 1);
        }
        return ogDescription;
    }
}
