package com.example.remsymes;

import android.content.Intent;
import android.net.Uri;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SharedImageExtractorTest {

    @Test
    public void isImageShareTrueForImageMimeTypes() {
        Intent intent = mock(Intent.class);
        when(intent.getType()).thenReturn("image/png");
        assertTrue(SharedImageExtractor.isImageShare(intent));
    }

    @Test
    public void isImageShareFalseForNonImageOrMissingType() {
        Intent textIntent = mock(Intent.class);
        when(textIntent.getType()).thenReturn("text/plain");
        assertFalse(SharedImageExtractor.isImageShare(textIntent));

        Intent noTypeIntent = mock(Intent.class);
        assertFalse(SharedImageExtractor.isImageShare(noTypeIntent));

        assertFalse(SharedImageExtractor.isImageShare(null));
    }

    @Test
    public void extractImageUriReturnsStreamExtraForImageShares() {
        Intent intent = mock(Intent.class);
        Uri uri = mock(Uri.class);
        when(intent.getType()).thenReturn("image/jpeg");
        when(intent.getParcelableExtra(Intent.EXTRA_STREAM)).thenReturn(uri);

        assertSame(uri, SharedImageExtractor.extractImageUri(intent));
    }

    @Test
    public void extractImageUriReturnsNullForNonImageShares() {
        Intent intent = mock(Intent.class);
        when(intent.getType()).thenReturn("text/plain");

        assertNull(SharedImageExtractor.extractImageUri(intent));
    }
}
