package com.example.remsymes;

import android.content.Context;
import android.net.Uri;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

/**
 * Workaround for sources (notably Instagram) that block anonymous HTTP/WebView scraping: the
 * user screenshots the recipe from within the real, already-logged-in app and shares the image
 * here instead of a link. Runs on-device OCR (ML Kit, bundled model — no network call) on the
 * image, then feeds the recognized text through the same heuristic parser used for every other
 * source.
 */
public final class ScreenshotOcrScraper {

    public interface Callback {
        void onSuccess(Recipe draftRecipe);
        void onError(String message);
    }

    public static void scrapeImage(Context context, Uri imageUri, Callback callback) {
        try {
            InputImage image = InputImage.fromFilePath(context, imageUri);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String rawText = visionText.getText();
                        Recipe draft = LlmRecipeExtractor.extractRecipeFromText(rawText, null);
                        draft.setImageUri(imageUri.toString());
                        callback.onSuccess(draft);
                    })
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        } catch (IOException e) {
            callback.onError(e.getMessage());
        }
    }
}
