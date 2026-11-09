package me.remsy;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.mlkit.genai.common.FeatureStatus;
import com.google.mlkit.genai.prompt.GenerateContentRequest;
import com.google.mlkit.genai.prompt.GenerateContentResponse;
import com.google.mlkit.genai.prompt.Generation;
import com.google.mlkit.genai.prompt.TextPart;
import com.google.mlkit.genai.prompt.java.GenerativeModelFutures;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Best-effort on-device pass that asks Gemini Nano (ML Kit GenAI direct model access) to fill in
 * fields {@link LlmRecipeExtractor}'s regex parser left blank or mis-categorized. Only available
 * on a handful of devices with AICore/Gemini Nano installed, so every failure mode (unsupported
 * device, model not downloaded, timeout, bad response) just falls back to the original draft.
 */
public final class RecipeLlmEnhancer {

    private static final String TAG = "RecipeLlmEnhancer";
    private static final long STATUS_TIMEOUT_MS = 3000;
    private static final long GENERATION_TIMEOUT_MS = 8000;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onComplete(Recipe recipe);
    }

    private RecipeLlmEnhancer() {
    }

    /** Always invokes {@code callback} on the main thread with a usable {@link Recipe}. */
    public static void enhance(String rawText, Recipe draft, Callback callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || rawText == null || rawText.trim().isEmpty()) {
            mainHandler.post(() -> callback.onComplete(draft));
            return;
        }

        executor.execute(() -> {
            Recipe result = draft;
            try {
                GenerativeModelFutures model = GenerativeModelFutures.from(Generation.INSTANCE.getClient());
                int status = model.checkStatus().get(STATUS_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (status == FeatureStatus.AVAILABLE) {
                    Recipe enhanced = requestEnhancement(model, rawText, draft);
                    if (enhanced != null) {
                        result = enhanced;
                    }
                }
                // DOWNLOADABLE/DOWNLOADING/UNAVAILABLE: too slow or unsupported for this pass, skip.
            } catch (Exception e) {
                Log.w(TAG, "Gemini Nano enhancement skipped: " + e.getMessage());
            }
            Recipe finalResult = result;
            mainHandler.post(() -> callback.onComplete(finalResult));
        });
    }

    private static Recipe requestEnhancement(GenerativeModelFutures model, String rawText, Recipe draft) throws Exception {
        String prompt = buildPrompt(rawText, draft);
        GenerateContentRequest request = new GenerateContentRequest.Builder(new TextPart(prompt)).build();
        GenerateContentResponse response = model.generateContent(request).get(GENERATION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        if (response.getCandidates().isEmpty()) return null;
        return mergeJsonIntoRecipe(response.getCandidates().get(0).getText(), draft);
    }

    private static String buildPrompt(String rawText, Recipe draft) {
        String truncatedText = rawText.length() > 6000 ? rawText.substring(0, 6000) : rawText;
        return "You are cleaning up a recipe that was extracted from raw scraped/OCR text by a "
                + "simple regex parser. Given the ORIGINAL TEXT and the CURRENT FIELDS, return a "
                + "single JSON object with keys name, category, prepTime, portions, ingredients, "
                + "instructions.\n"
                + "Rules:\n"
                + "- Only set a key if you are filling in a field that is blank/\"Uncategorized\", "
                + "or fixing a field that clearly contains the wrong kind of content (for example an "
                + "ingredient line stuck inside instructions).\n"
                + "- Leave a key out of the JSON (or set it to an empty string) if the current value "
                + "is already fine, or you cannot determine it from the ORIGINAL TEXT.\n"
                + "- Never invent ingredients, steps, or facts that are not grounded in the ORIGINAL TEXT.\n"
                + "- category should be a single common recipe category word, such as Breakfast, "
                + "Lunch, Dinner, Dessert, Snack, Appetizer, or Drink, left out if unclear.\n"
                + "- ingredients should be a newline-separated list, one ingredient per line.\n"
                + "- instructions should be a newline-separated, numbered list of steps.\n"
                + "Respond with ONLY the JSON object, no markdown fences, no commentary.\n\n"
                + "ORIGINAL TEXT:\n" + truncatedText + "\n\n"
                + "CURRENT FIELDS:\n"
                + "name: " + draft.getName() + "\n"
                + "category: " + draft.getCategory() + "\n"
                + "prepTime: " + draft.getPrepTime() + "\n"
                + "portions: " + draft.getPortions() + "\n"
                + "ingredients: " + draft.getIngredients() + "\n"
                + "instructions: " + draft.getInstructions();
    }

    private static Recipe mergeJsonIntoRecipe(String modelText, Recipe draft) {
        if (modelText == null) return null;
        String json = extractJsonObject(modelText);
        if (json == null) return null;

        try {
            JSONObject obj = new JSONObject(json);
            String name = optTrimmed(obj, "name");
            String category = optTrimmed(obj, "category");
            String prepTime = optTrimmed(obj, "prepTime");
            String portions = optTrimmed(obj, "portions");
            String ingredients = optTrimmed(obj, "ingredients");
            String instructions = optTrimmed(obj, "instructions");

            return new Recipe(
                    draft.getId(),
                    !name.isEmpty() ? name : draft.getName(),
                    !category.isEmpty() ? category : draft.getCategory(),
                    !prepTime.isEmpty() ? prepTime : draft.getPrepTime(),
                    !portions.isEmpty() ? portions : draft.getPortions(),
                    draft.getRating(),
                    draft.isFavorite(),
                    draft.getImageUri(),
                    !ingredients.isEmpty() ? ingredients : draft.getIngredients(),
                    !instructions.isEmpty() ? instructions : draft.getInstructions(),
                    draft.getSourceLink(),
                    draft.getScrapedText()
            );
        } catch (Exception e) {
            Log.w(TAG, "Failed to parse Gemini Nano JSON response: " + e.getMessage());
            return null;
        }
    }

    private static String optTrimmed(JSONObject obj, String key) {
        String value = obj.optString(key, "");
        return value == null ? "" : value.trim();
    }

    private static String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end < start) return null;
        return text.substring(start, end + 1);
    }
}
