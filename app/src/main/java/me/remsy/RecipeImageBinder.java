package me.remsy;

import android.net.Uri;
import android.widget.ImageView;

/**
 * Resolves a recipe's stored {@code imageUri} string (a picked content/file URI, a named
 * sample-drawable placeholder, or null/empty) to the right {@link ImageView} call.
 * Shared by every fragment that renders a recipe thumbnail so the dispatch logic only
 * lives in one, unit-testable place.
 */
public final class RecipeImageBinder {

    private RecipeImageBinder() {
    }

    public static void bind(ImageView imageView, String imageUri) {
        if (imageView == null) return;

        if (imageUri == null || imageUri.isEmpty()) {
            imageView.setImageResource(R.drawable.recipe_sample_image);
            return;
        }

        if (imageUri.startsWith("content://") || imageUri.startsWith("file://")) {
            try {
                imageView.setImageURI(Uri.parse(imageUri));
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.recipe_sample_image);
            }
        } else if ("recipe_sample_dessert".equals(imageUri)) {
            imageView.setImageResource(R.drawable.recipe_sample_dessert);
        } else if ("recipe_sample_main".equals(imageUri)) {
            imageView.setImageResource(R.drawable.recipe_sample_main);
        } else if ("recipe_sample_breakfast".equals(imageUri)) {
            imageView.setImageResource(R.drawable.recipe_sample_breakfast);
        } else {
            imageView.setImageResource(R.drawable.recipe_sample_image);
        }
    }
}
