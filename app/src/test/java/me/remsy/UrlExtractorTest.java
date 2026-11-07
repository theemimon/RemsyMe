package me.remsy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UrlExtractorTest {

    @Test
    public void returnsNullForNullInput() {
        assertNull(UrlExtractor.extractUrl(null));
    }

    @Test
    public void returnsNullWhenNoUrlPresent() {
        assertNull(UrlExtractor.extractUrl("just some shared text with no link"));
    }

    @Test
    public void extractsHttpUrl() {
        assertEquals("http://example.com/recipe",
                UrlExtractor.extractUrl("Check this out: http://example.com/recipe it's great"));
    }

    @Test
    public void extractsHttpsUrlWithQueryString() {
        assertEquals("https://example.com/recipe?query=1",
                UrlExtractor.extractUrl("https://example.com/recipe?query=1 some trailing text"));
    }

    @Test
    public void returnsFirstUrlWhenMultiplePresent() {
        assertEquals("https://first.example.com",
                UrlExtractor.extractUrl("https://first.example.com and https://second.example.com"));
    }
}
