package me.remsy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class InstagramRecipeScraperTest {

    private final InstagramRecipeScraper scraper = new InstagramRecipeScraper();

    @Test
    public void supportsInstagramUrlsOnly() {
        assertTrue(scraper.supports("https://www.instagram.com/reel/Cxyz123/"));
        assertFalse(scraper.supports("https://www.youtube.com/watch?v=abc"));
    }

    @Test
    public void stripEngagementPrefixExtractsQuotedCaption() {
        String ogDescription = "50K likes, 230 comments - foodie on November 7, 2026: \"Creamy Pasta Recipe\"";
        assertEquals("Creamy Pasta Recipe", InstagramRecipeScraper.stripEngagementPrefix(ogDescription));
    }

    @Test
    public void stripEngagementPrefixLeavesPlainDescriptionUnchangedWhenNoMarker() {
        assertEquals("Just a plain caption", InstagramRecipeScraper.stripEngagementPrefix("Just a plain caption"));
    }

    @Test
    public void stripEngagementPrefixReturnsNullForNull() {
        assertNull(InstagramRecipeScraper.stripEngagementPrefix(null));
    }

    @Test
    public void scrapesCaptionIntoRecipeFromOgDescription() {
        String caption = "Creamy Pasta Recipe\nIngredients\n2 cups flour\n1 egg\nInstructions\nCombine everything well\nCook for 10 minutes";
        String html = "<html><head><meta property=\"og:description\" content=\""
                + "50K likes, 230 comments - foodie on November 7, 2026: &quot;" + caption + "&quot;\">"
                + "</head><body></body></html>";

        Recipe recipe = scraper.scrape("https://www.instagram.com/reel/Cxyz123/", html);

        assertEquals("Creamy Pasta Recipe", recipe.getName());
        assertEquals("• 2 cups flour\n• 1 egg", recipe.getIngredients());
        assertEquals("1. Combine everything well\n2. Cook for 10 minutes", recipe.getInstructions());
        assertEquals("https://www.instagram.com/reel/Cxyz123/", recipe.getSourceLink());
    }

    @Test
    public void fallsBackToGenericScraperWhenNoOgDescriptionPresent() {
        String html = "<html><body><p>No meta description on this page at all.</p></body></html>";

        Recipe recipe = scraper.scrape("https://www.instagram.com/reel/Cxyz123/", html);

        assertEquals("Uncategorized", recipe.getCategory());
    }
}
