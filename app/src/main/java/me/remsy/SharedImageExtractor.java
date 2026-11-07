package me.remsy;

import android.content.Intent;
import android.net.Uri;

/** Detects whether a received share {@link Intent} carries an image, and pulls out its URI. */
public final class SharedImageExtractor {

    private SharedImageExtractor() {
    }

    public static boolean isImageShare(Intent intent) {
        if (intent == null) return false;
        String type = intent.getType();
        return type != null && type.startsWith("image/");
    }

    @SuppressWarnings("deprecation") // Single-arg overload works across all supported API levels.
    public static Uri extractImageUri(Intent intent) {
        if (!isImageShare(intent)) return null;
        return intent.getParcelableExtra(Intent.EXTRA_STREAM);
    }
}
