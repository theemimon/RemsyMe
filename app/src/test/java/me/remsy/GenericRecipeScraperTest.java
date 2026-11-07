package me.remsy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GenericRecipeScraperTest {

    private final GenericRecipeScraper scraper = new GenericRecipeScraper();

    @Test
    public void supportsAnyUrl() {
        assertTrue(scraper.supports("https://anything.example.com"));
    }

    @Test
    public void delegatesToRecipeScrapersJava() {
        String html = "<html><body><p>Just a random paragraph with no recognizable recipe markup here at all.</p></body></html>";

        Recipe recipe = scraper.scrape("https://example.com/unknown", html);

        assertEquals("Uncategorized", recipe.getCategory());
    }
}
