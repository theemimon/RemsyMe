package me.remsy;

import android.content.ClipData;
import android.content.Intent;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SharedIntentTextExtractorTest {

    @Test
    public void returnsNullForNullIntent() {
        assertNull(SharedIntentTextExtractor.extractSharedText(null));
    }

    @Test
    public void prefersExtraTextString() {
        Intent intent = mock(Intent.class);
        when(intent.getStringExtra(Intent.EXTRA_TEXT)).thenReturn("shared recipe text");

        assertEquals("shared recipe text", SharedIntentTextExtractor.extractSharedText(intent));
    }

    @Test
    public void fallsBackToExtraSubjectWhenTextMissing() {
        Intent intent = mock(Intent.class);
        when(intent.getStringExtra(Intent.EXTRA_TEXT)).thenReturn(null);
        when(intent.getStringExtra(Intent.EXTRA_SUBJECT)).thenReturn("subject line");

        assertEquals("subject line", SharedIntentTextExtractor.extractSharedText(intent));
    }

    @Test
    public void fallsBackToCharSequenceExtraText() {
        Intent intent = mock(Intent.class);
        when(intent.getStringExtra(Intent.EXTRA_TEXT)).thenReturn(null);
        when(intent.getStringExtra(Intent.EXTRA_SUBJECT)).thenReturn(null);
        when(intent.getCharSequenceExtra(Intent.EXTRA_TEXT)).thenReturn("char sequence text");

        assertEquals("char sequence text", SharedIntentTextExtractor.extractSharedText(intent));
    }

    @Test
    public void fallsBackToClipDataText() {
        Intent intent = mock(Intent.class);
        ClipData clipData = mock(ClipData.class);
        ClipData.Item item = mock(ClipData.Item.class);

        when(intent.getStringExtra(Intent.EXTRA_TEXT)).thenReturn(null);
        when(intent.getStringExtra(Intent.EXTRA_SUBJECT)).thenReturn(null);
        when(intent.getCharSequenceExtra(Intent.EXTRA_TEXT)).thenReturn(null);
        when(intent.getClipData()).thenReturn(clipData);
        when(clipData.getItemCount()).thenReturn(1);
        when(clipData.getItemAt(0)).thenReturn(item);
        when(item.getText()).thenReturn("clip data text");

        assertEquals("clip data text", SharedIntentTextExtractor.extractSharedText(intent));
    }

    @Test
    public void fallsBackToClipDataUriWhenNoText() {
        Intent intent = mock(Intent.class);
        ClipData clipData = mock(ClipData.class);
        ClipData.Item item = mock(ClipData.Item.class);
        android.net.Uri uri = mock(android.net.Uri.class);
        when(uri.toString()).thenReturn("content://shared/uri");

        when(intent.getClipData()).thenReturn(clipData);
        when(clipData.getItemCount()).thenReturn(1);
        when(clipData.getItemAt(0)).thenReturn(item);
        when(item.getText()).thenReturn(null);
        when(item.getUri()).thenReturn(uri);

        assertEquals("content://shared/uri", SharedIntentTextExtractor.extractSharedText(intent));
    }

    @Test
    public void returnsNullWhenEverythingIsEmpty() {
        Intent intent = mock(Intent.class);
        assertNull(SharedIntentTextExtractor.extractSharedText(intent));
    }
}
