package com.example.remsymes;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RecipeTest {

    @Test
    public void shortConstructorDefaultsSourceLinkAndScrapedTextToEmptyStrings() {
        Recipe recipe = new Recipe(1, "Soup", "Main Courses", "10 mins", "2 Servings",
                4.5, true, "recipe_sample_image", "• Water", "1. Boil");

        assertEquals("", recipe.getSourceLink());
        assertEquals("", recipe.getScrapedText());
    }

    @Test
    public void gettersReflectFullConstructorArguments() {
        Recipe recipe = new Recipe(7, "Soup", "Main Courses", "10 mins", "2 Servings",
                4.5, true, "recipe_sample_image", "• Water", "1. Boil",
                "https://example.com", "raw text");

        assertEquals(7, recipe.getId());
        assertEquals("Soup", recipe.getName());
        assertEquals("Main Courses", recipe.getCategory());
        assertEquals(4.5, recipe.getRating(), 0.0001);
        assertEquals(true, recipe.isFavorite());
        assertEquals("https://example.com", recipe.getSourceLink());
        assertEquals("raw text", recipe.getScrapedText());
    }

    @Test
    public void settersMutateState() {
        Recipe recipe = new Recipe();
        recipe.setId(5);
        recipe.setFavorite(true);
        recipe.setRating(3.0);

        assertEquals(5, recipe.getId());
        assertFalse(!recipe.isFavorite());
        assertEquals(3.0, recipe.getRating(), 0.0001);
    }
}
