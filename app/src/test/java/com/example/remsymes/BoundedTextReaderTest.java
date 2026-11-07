package com.example.remsymes;

import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BoundedTextReaderTest {

    @Test
    public void readsAllContentWhenUnderLimit() throws Exception {
        String result = BoundedTextReader.readAll(new StringReader("line one\nline two"), 1000);
        assertEquals("line one\nline two\n", result);
    }

    @Test
    public void truncatesContentExceedingMaxChars() throws Exception {
        StringBuilder huge = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            huge.append("0123456789");
        }
        String result = BoundedTextReader.readAll(new StringReader(huge.toString()), 100);

        assertEquals(100, result.length());
    }

    @Test
    public void zeroMaxCharsReturnsEmptyString() throws Exception {
        String result = BoundedTextReader.readAll(new StringReader("anything"), 0);
        assertEquals("", result);
    }

    @Test
    public void neverExceedsCapAcrossManyLines() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            sb.append("a line of text here\n");
        }
        String result = BoundedTextReader.readAll(new StringReader(sb.toString()), 37);
        assertTrue(result.length() <= 37);
    }
}
