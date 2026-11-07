package com.example.remsymes;

import android.widget.ImageView;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RecipeImageBinderTest {

    @Test
    public void nullImageViewDoesNotThrow() {
        RecipeImageBinder.bind(null, "recipe_sample_main");
    }

    @Test
    public void nullOrEmptyUriFallsBackToDefaultPlaceholder() {
        ImageView imageView = mock(ImageView.class);
        RecipeImageBinder.bind(imageView, null);
        verify(imageView).setImageResource(R.drawable.recipe_sample_image);

        ImageView imageView2 = mock(ImageView.class);
        RecipeImageBinder.bind(imageView2, "");
        verify(imageView2).setImageResource(R.drawable.recipe_sample_image);
    }

    @Test
    public void namedSampleDrawablesResolveToMatchingResource() {
        ImageView dessert = mock(ImageView.class);
        RecipeImageBinder.bind(dessert, "recipe_sample_dessert");
        verify(dessert).setImageResource(R.drawable.recipe_sample_dessert);

        ImageView main = mock(ImageView.class);
        RecipeImageBinder.bind(main, "recipe_sample_main");
        verify(main).setImageResource(R.drawable.recipe_sample_main);

        ImageView breakfast = mock(ImageView.class);
        RecipeImageBinder.bind(breakfast, "recipe_sample_breakfast");
        verify(breakfast).setImageResource(R.drawable.recipe_sample_breakfast);
    }

    @Test
    public void unknownPlaceholderFallsBackToDefaultImage() {
        ImageView imageView = mock(ImageView.class);
        RecipeImageBinder.bind(imageView, "something_unrecognized");
        verify(imageView).setImageResource(R.drawable.recipe_sample_image);
    }

    @Test
    public void contentUriDelegatesToSetImageUriRatherThanResource() {
        ImageView imageView = mock(ImageView.class);
        RecipeImageBinder.bind(imageView, "content://com.example/picked-image");

        verify(imageView, times(1)).setImageURI(org.mockito.ArgumentMatchers.any());
        verify(imageView, never()).setImageResource(org.mockito.ArgumentMatchers.anyInt());
    }
}
