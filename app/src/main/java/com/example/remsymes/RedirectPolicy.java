package com.example.remsymes;

import java.net.HttpURLConnection;

/**
 * Pure redirect-following policy used by {@link WebRecipeScraper}. Extracted so the
 * "stop after N redirects" rule can be unit tested without any real network I/O.
 */
public final class RedirectPolicy {

    public static final int MAX_REDIRECTS = 5;

    private RedirectPolicy() {
    }

    public static boolean isRedirectStatusCode(int responseCode) {
        return responseCode == HttpURLConnection.HTTP_MOVED_PERM
                || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                || responseCode == 307 || responseCode == 308;
    }

    /**
     * Returns the URL to follow next, or {@code null} if the response isn't a redirect
     * (or has no usable Location header).
     *
     * @throws TooManyRedirectsException if {@code redirectCount} already reached {@link #MAX_REDIRECTS}.
     */
    public static String nextRedirectUrl(int responseCode, String locationHeader, int redirectCount) {
        if (!isRedirectStatusCode(responseCode)) return null;
        if (locationHeader == null || locationHeader.isEmpty()) return null;
        if (redirectCount >= MAX_REDIRECTS) {
            throw new TooManyRedirectsException(redirectCount);
        }
        return locationHeader;
    }

    public static final class TooManyRedirectsException extends RuntimeException {
        public TooManyRedirectsException(int redirectCount) {
            super("Exceeded maximum of " + MAX_REDIRECTS + " redirects (encountered " + redirectCount + ")");
        }
    }
}
