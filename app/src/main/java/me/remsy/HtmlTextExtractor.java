package me.remsy;

/**
 * Pure HTML-to-text helpers used by the recipe scraper. No Android dependencies,
 * so it is directly unit-testable without Robolectric/instrumentation.
 */
public final class HtmlTextExtractor {

    private HtmlTextExtractor() {
    }

    public static String extractRawTextFromHtml(String html) {
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

    static String decodeHtmlEntities(String text) {
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
