package me.remsy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/** Pure URL-host matching helper shared by the per-site scrapers. */
final class UrlHosts {

    private UrlHosts() {
    }

    static String hostOf(String url) {
        if (url == null) return null;
        try {
            String host = new URI(url).getHost();
            return host != null ? host.toLowerCase(Locale.ROOT) : null;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /** True if the URL's host equals one of {@code domains}, or is a subdomain of one of them. */
    static boolean hostMatches(String url, String... domains) {
        String host = hostOf(url);
        if (host == null) return false;
        for (String domain : domains) {
            if (host.equals(domain) || host.endsWith("." + domain)) {
                return true;
            }
        }
        return false;
    }
}
