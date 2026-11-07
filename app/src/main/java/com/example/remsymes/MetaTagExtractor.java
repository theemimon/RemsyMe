package com.example.remsymes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Pure helper for pulling `<meta property="..." content="...">`-style tags out of raw HTML. */
final class MetaTagExtractor {

    private MetaTagExtractor() {
    }

    static String extractOgDescription(String html) {
        return extractMetaContent(html, "og:description");
    }

    /**
     * Finds the first {@code <meta>} tag whose {@code property} or {@code name} attribute equals
     * {@code key}, then returns its {@code content} value (HTML-entity-decoded, trimmed).
     * Only matches tags where the property/name attribute appears before content, which covers
     * the vast majority of real-world markup.
     */
    static String extractMetaContent(String html, String key) {
        if (html == null) return null;
        Pattern tagPattern = Pattern.compile(
                "<meta[^>]*(?:property|name)=[\"']" + Pattern.quote(key) + "[\"'][^>]*>",
                Pattern.CASE_INSENSITIVE);
        Matcher tagMatcher = tagPattern.matcher(html);
        if (!tagMatcher.find()) return null;

        Matcher contentMatcher = Pattern.compile("content=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE)
                .matcher(tagMatcher.group());
        if (!contentMatcher.find()) return null;

        return HtmlTextExtractor.decodeHtmlEntities(contentMatcher.group(1)).trim();
    }
}
