package com.example.remsymes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Reads all text from a {@link Reader} up to a hard character cap, to avoid loading
 * unbounded (or maliciously huge) HTTP responses fully into memory. Pure Java, no
 * Android/network dependency, so the truncation behavior is directly unit testable.
 */
public final class BoundedTextReader {

    public static final int DEFAULT_MAX_CHARS = 2_000_000;

    private BoundedTextReader() {
    }

    public static String readAll(Reader reader, int maxChars) throws IOException {
        BufferedReader br = (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            int remaining = maxChars - sb.length();
            if (remaining <= 0) break;
            if (line.length() >= remaining) {
                sb.append(line, 0, remaining);
                break;
            }
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
