package me.remsy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JsonStringUnescaperTest {

    @Test
    public void unescapesNewlinesAndQuotes() {
        assertEquals("Line one\nLine \"two\"", JsonStringUnescaper.unescape("Line one\\nLine \\\"two\\\""));
    }

    @Test
    public void unescapesBackslashAndSlash() {
        assertEquals("a\\b/c", JsonStringUnescaper.unescape("a\\\\b\\/c"));
    }

    @Test
    public void unescapesUnicodeEscape() {
        assertEquals("caf\u00e9", JsonStringUnescaper.unescape("caf\\u00e9"));
    }

    @Test
    public void leavesPlainTextUnchanged() {
        assertEquals("2 cups flour, 1 egg", JsonStringUnescaper.unescape("2 cups flour, 1 egg"));
    }

    @Test
    public void returnsNullForNullInput() {
        assertNull(JsonStringUnescaper.unescape(null));
    }
}
