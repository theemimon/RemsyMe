package com.example.remsymes;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MetaTagExtractorTest {

    @Test
    public void extractsContentWhenPropertyComesFirst() {
        String html = "<head><meta property=\"og:description\" content=\"A tasty recipe\"></head>";
        assertEquals("A tasty recipe", MetaTagExtractor.extractOgDescription(html));
    }

    @Test
    public void extractsContentUsingNameAttribute() {
        String html = "<meta name=\"og:description\" content=\"Another recipe\">";
        assertEquals("Another recipe", MetaTagExtractor.extractMetaContent(html, "og:description"));
    }

    @Test
    public void decodesHtmlEntitiesInContent() {
        String html = "<meta property=\"og:description\" content=\"Salt &amp; pepper\">";
        assertEquals("Salt & pepper", MetaTagExtractor.extractOgDescription(html));
    }

    @Test
    public void returnsNullWhenTagMissing() {
        String html = "<head><title>No og tags here</title></head>";
        assertNull(MetaTagExtractor.extractOgDescription(html));
    }

    @Test
    public void returnsNullForNullHtml() {
        assertNull(MetaTagExtractor.extractOgDescription(null));
    }
}
