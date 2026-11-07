package com.example.remsymes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Pure text utility for pulling the first http(s) URL out of shared text. No Android deps, directly unit-testable. */
public final class UrlExtractor {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);

    private UrlExtractor() {
    }

    public static String extractUrl(String text) {
        if (text == null) return null;
        Matcher matcher = URL_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }
}
