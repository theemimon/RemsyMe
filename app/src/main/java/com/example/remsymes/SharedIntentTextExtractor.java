package com.example.remsymes;

import android.content.ClipData;
import android.content.Intent;

/**
 * Extracts the best-effort shared text payload out of an incoming {@link Intent}.
 * Kept separate from {@link MainActivity} so the extraction priority logic can be
 * unit tested with mocked {@link Intent}/{@link ClipData} objects instead of requiring
 * a real Activity/instrumentation environment.
 */
public final class SharedIntentTextExtractor {

    private SharedIntentTextExtractor() {
    }

    public static String extractSharedText(Intent intent) {
        if (intent == null) return null;

        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (text != null && !text.trim().isEmpty()) {
            return text;
        }

        String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        if (subject != null && !subject.trim().isEmpty()) {
            return subject;
        }

        CharSequence charSeq = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
        if (charSeq != null && !charSeq.toString().trim().isEmpty()) {
            return charSeq.toString();
        }

        ClipData clipData = intent.getClipData();
        if (clipData != null && clipData.getItemCount() > 0) {
            ClipData.Item item = clipData.getItemAt(0);
            if (item != null) {
                if (item.getText() != null && !item.getText().toString().trim().isEmpty()) {
                    return item.getText().toString();
                }
                if (item.getUri() != null) {
                    return item.getUri().toString();
                }
            }
        }

        return null;
    }
}
