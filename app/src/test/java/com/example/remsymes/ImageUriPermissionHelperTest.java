package com.example.remsymes;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;

import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ImageUriPermissionHelperTest {

    @Test
    public void requestsPersistableReadGrantForPickedImage() {
        ContentResolver resolver = mock(ContentResolver.class);
        Uri uri = mock(Uri.class);

        ImageUriPermissionHelper.persistReadPermission(resolver, uri);

        verify(resolver).takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    @Test
    public void doesNothingWhenResolverOrUriAreNull() {
        ContentResolver resolver = mock(ContentResolver.class);
        ImageUriPermissionHelper.persistReadPermission(null, mock(Uri.class));
        ImageUriPermissionHelper.persistReadPermission(resolver, null);

        verify(resolver, never()).takePersistableUriPermission(any(), anyInt());
    }

    /** Providers that don't support persistable grants (e.g. camera) must not crash the caller. */
    @Test
    public void swallowsSecurityExceptionFromUnsupportedProvider() {
        ContentResolver resolver = mock(ContentResolver.class);
        Uri uri = mock(Uri.class);
        doThrow(new SecurityException("not supported")).when(resolver).takePersistableUriPermission(any(), anyInt());

        ImageUriPermissionHelper.persistReadPermission(resolver, uri);
        // No exception propagated out of this call is the assertion.
    }
}
