package me.remsy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RecipeScrapersJavaTest {

    @Test
    public void nullOrBlankHtmlFallsBackToLlmExtractorOnTheUrlItself() {
        String url = "https://example.com/recipes/amazing-choco-cake";

        Recipe recipe = RecipeScrapersJava.scrape(url, null);

        assertEquals("Amazing Choco Cake", recipe.getName());
        assertEquals("Uncategorized", recipe.getCategory());
        assertEquals(url, recipe.getSourceLink());
    }

    @Test
    public void prefersJsonLdRecipeSchemaAndFormatsIsoDuration() {
        String html = "<html><head>"
                + "<script type=\"application/ld+json\">"
                + "{\"@context\":\"https://schema.org/\",\"@type\":\"Recipe\",\"name\":\"Test Cake\","
                + "\"recipeCategory\":\"Desserts\",\"totalTime\":\"PT1H30M\",\"recipeYield\":\"8 servings\","
                + "\"recipeIngredient\":[\"2 cups flour\",\"1 cup sugar\"],"
                + "\"recipeInstructions\":[{\"@type\":\"HowToStep\",\"text\":\"Mix dry ingredients.\"},"
                + "{\"@type\":\"HowToStep\",\"text\":\"Bake at 350F.\"}]}"
                + "</script></head><body><p>Test Cake</p></body></html>";

        Recipe recipe = RecipeScrapersJava.scrape("https://example.com/cake", html);

        assertEquals("Test Cake", recipe.getName());
        assertEquals("Desserts", recipe.getCategory());
        assertEquals("90 mins", recipe.getPrepTime());
        assertEquals("8 servings", recipe.getPortions());
        assertEquals("• 2 cups flour\n• 1 cup sugar", recipe.getIngredients());
        assertEquals("1. Mix dry ingredients.\n2. Bake at 350F.", recipe.getInstructions());
        assertEquals("https://example.com/cake", recipe.getSourceLink());
    }

    @Test
    public void parsesTastyRecipesPluginMarkupWhenNoJsonLdPresent() {
        String html = "<html><body>"
                + "<div class=\"tasty-recipes-title\">Tasty Pancakes</div>"
                + "<div class=\"tasty-recipes-total-time\">20 minutes</div>"
                + "<div class=\"tasty-recipes-yield\">4 servings</div>"
                + "<div class=\"tasty-recipes-ingredients\"><span>2 cups flour</span></div>"
                + "<div class=\"tasty-recipes-ingredients\"><span>1 egg</span></div>"
                + "<div class=\"tasty-recipes-instructions\"><span>Mix everything</span></div>"
                + "<div class=\"tasty-recipes-instructions\"><span>Cook well</span></div>"
                + "</body></html>";

        Recipe recipe = RecipeScrapersJava.scrape("https://example.com/pancakes", html);

        assertEquals("Tasty Pancakes", recipe.getName());
        assertEquals("20 minutes", recipe.getPrepTime());
        assertEquals("4 servings", recipe.getPortions());
        assertEquals("• 2 cups flour\n• 1 egg", recipe.getIngredients());
        assertEquals("1. Mix everything\n2. Cook well", recipe.getInstructions());
    }

    @Test
    public void fallsBackToLlmExtractorWhenNoKnownRecipeMarkupIsPresent() {
        String html = "<html><body><p>Just a random paragraph with no recognizable recipe markup here at all.</p></body></html>";

        Recipe recipe = RecipeScrapersJava.scrape("https://example.com/unknown", html);

        assertEquals("Uncategorized", recipe.getCategory());
        assertTrue(recipe.getInstructions().contains("random paragraph"));
    }
}
