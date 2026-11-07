package com.example.remsymes;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;

/**
 * Wraps {@link ContentResolver#takePersistableUriPermission} so image URIs picked via
 * {@code ActivityResultContracts.GetContent()} keep working after the app process dies
 * (e.g. after a reboot), instead of silently failing to reload with a transient grant.
 */
public final class ImageUriPermissionHelper {

    private ImageUriPermissionHelper() {
    }

    public static void persistReadPermission(ContentResolver resolver, Uri uri) {
        if (resolver == null || uri == null) return;
        try {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException | IllegalArgumentException e) {
            // Some providers (camera, some file pickers) don't support persistable grants; the
            // image preview will still work for this session even if the grant isn't retained.
        }
    }
}
