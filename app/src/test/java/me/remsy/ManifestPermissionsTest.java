package me.remsy;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

/**
 * Guards against regressing the "WebRecipeScraper/WebViewScraper make network calls without the
 * INTERNET permission" bug found during the audit: the manifest must declare it.
 */
public class ManifestPermissionsTest {

    @Test
    public void manifestDeclaresInternetPermission() throws IOException {
        String manifest = readManifest();
        assertTrue("AndroidManifest.xml must declare android.permission.INTERNET for the web scraper to work",
                manifest.contains("android.permission.INTERNET"));
    }

    private static String readManifest() throws IOException {
        Path candidate = Paths.get("src/main/AndroidManifest.xml");
        if (!Files.exists(candidate)) {
            // Fall back to a path relative to the repository root, in case the test's working
            // directory isn't the app module (depends on how the build tool invokes the JVM).
            candidate = Paths.get("app/src/main/AndroidManifest.xml");
        }
        return new String(Files.readAllBytes(candidate));
    }
}
