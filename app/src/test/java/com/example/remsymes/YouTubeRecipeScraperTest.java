package com.example.remsymes;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class YouTubeRecipeScraperTest {

    private final YouTubeRecipeScraper scraper = new YouTubeRecipeScraper();

    @Test
    public void supportsYouTubeAndShortUrlsOnly() {
        assertTrue(scraper.supports("https://www.youtube.com/watch?v=abc123"));
        assertTrue(scraper.supports("https://youtu.be/abc123"));
        assertFalse(scraper.supports("https://www.tiktok.com/@user/video/1"));
    }

    @Test
    public void extractsAndUnescapesShortDescriptionFromEmbeddedJson() {
        String html = "<html><body><script>var ytInitialPlayerResponse = {\"videoDetails\":{"
                + "\"shortDescription\":\"Creamy Pasta Recipe\\nIngredients\\n2 cups flour\\n1 egg\""
                + "}};</script></body></html>";

        String description = YouTubeRecipeScraper.extractDescription(html);

        assertEquals("Creamy Pasta Recipe\nIngredients\n2 cups flour\n1 egg", description);
    }

    @Test
    public void fallsBackToOgDescriptionWhenNoShortDescriptionField() {
        String html = "<html><head><meta property=\"og:description\" content=\"Fallback description\">"
                + "</head><body></body></html>";

        assertEquals("Fallback description", YouTubeRecipeScraper.extractDescription(html));
    }

    @Test
    public void returnsNullWhenNeitherSourcePresent() {
        assertNull(YouTubeRecipeScraper.extractDescription("<html><body>nothing here</body></html>"));
    }

    @Test
    public void scrapesDescriptionIntoRecipe() {
        String html = "<html><body><script>var ytInitialPlayerResponse = {\"videoDetails\":{"
                + "\"shortDescription\":\"Creamy Pasta Recipe\\nIngredients\\n2 cups flour\\n1 egg\\n"
                + "Instructions\\nCombine everything well\\nCook for 10 minutes\""
                + "}};</script></body></html>";

        Recipe recipe = scraper.scrape("https://www.youtube.com/watch?v=abc123", html);

        assertEquals("Creamy Pasta Recipe", recipe.getName());
        assertEquals("• cups flour\n• egg", recipe.getIngredients());
        assertEquals("1. Combine everything well\n2. Cook for 10 minutes", recipe.getInstructions());
    }
}
