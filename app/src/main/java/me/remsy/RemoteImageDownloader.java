package me.remsy;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

/** Downloads a scraped recipe's remote cover image once and caches it in app-private storage. */
final class RemoteImageDownloader {

    private static final long MAX_IMAGE_BYTES = 8 * 1024 * 1024;
    private static final int CONNECT_TIMEOUT_MS = 10000;
    private static final int READ_TIMEOUT_MS = 10000;

    private RemoteImageDownloader() {
    }

    /**
     * Downloads {@code imageUrl} into the app's private files dir and returns a {@code file://}
     * URI pointing at it, or {@code null} if the URL is invalid, the download fails, or the
     * response exceeds {@link #MAX_IMAGE_BYTES}.
     */
    static String downloadToFileUri(Context context, String imageUrl) {
        if (context == null || imageUrl == null || imageUrl.trim().isEmpty()) return null;

        HttpURLConnection conn = null;
        try {
            URL url = new URI(imageUrl).toURL();
            String scheme = url.getProtocol();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) return null;

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) return null;

            File imagesDir = new File(context.getFilesDir(), "recipe_images");
            if (!imagesDir.exists() && !imagesDir.mkdirs()) return null;

            File outFile = new File(imagesDir, UUID.randomUUID() + ".jpg");
            long written = 0;
            try (InputStream in = conn.getInputStream(); FileOutputStream out = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    written += read;
                    if (written > MAX_IMAGE_BYTES) {
                        outFile.delete();
                        return null;
                    }
                    out.write(buffer, 0, read);
                }
            }
            return Uri.fromFile(outFile).toString();
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
