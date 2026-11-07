package me.remsy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class UrlHostsTest {

    @Test
    public void hostOfReturnsLowercasedHost() {
        assertEquals("www.example.com", UrlHosts.hostOf("https://WWW.Example.com/path"));
    }

    @Test
    public void hostOfReturnsNullForMalformedUrl() {
        assertNull(UrlHosts.hostOf("not a url"));
        assertNull(UrlHosts.hostOf(null));
    }

    @Test
    public void hostMatchesExactDomain() {
        assertTrue(UrlHosts.hostMatches("https://instagram.com/reel/abc", "instagram.com"));
    }

    @Test
    public void hostMatchesSubdomain() {
        assertTrue(UrlHosts.hostMatches("https://www.youtube.com/watch?v=abc", "youtube.com"));
        assertTrue(UrlHosts.hostMatches("https://m.tiktok.com/v/123", "tiktok.com"));
    }

    @Test
    public void hostMatchesAnyOfMultipleDomains() {
        assertTrue(UrlHosts.hostMatches("https://youtu.be/abc", "youtube.com", "youtu.be"));
    }

    @Test
    public void hostDoesNotMatchUnrelatedDomain() {
        assertFalse(UrlHosts.hostMatches("https://example.com/recipe", "instagram.com"));
        // Must not falsely match "notinstagram.com" just because it ends with the substring.
        assertFalse(UrlHosts.hostMatches("https://notinstagram.com/recipe", "instagram.com"));
    }
}
