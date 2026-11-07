package com.example.remsymes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

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
            Bitmap bitmap = loadSoftwareBitmap(context, imageUri);
            InputImage image = InputImage.fromBitmap(bitmap, 0);
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

    /**
     * Decodes the image forcing a software (ARGB_8888) bitmap config. Letting the OS pick
     * defaults can yield a hardware-backed bitmap, which ML Kit's text recognizer cannot read
     * pixel data from and crashes on with an obscure internal NullPointerException.
     */
    private static Bitmap loadSoftwareBitmap(Context context, Uri uri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), uri);
            return ImageDecoder.decodeBitmap(source, (decoder, info, src) ->
                    decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE));
        }

        @SuppressWarnings("deprecation")
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        if (bitmap != null && bitmap.getConfig() == Bitmap.Config.HARDWARE) {
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        }
        return bitmap;
    }
}
