package me.remsy;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class RecipeScraperRegistryTest {

    @Test
    public void routesInstagramUrlsToInstagramScraper() {
        assertTrue(RecipeScraperRegistry.forUrl("https://www.instagram.com/reel/abc") instanceof InstagramRecipeScraper);
    }

    @Test
    public void routesYouTubeUrlsToYouTubeScraper() {
        assertTrue(RecipeScraperRegistry.forUrl("https://www.youtube.com/watch?v=abc") instanceof YouTubeRecipeScraper);
        assertTrue(RecipeScraperRegistry.forUrl("https://youtu.be/abc") instanceof YouTubeRecipeScraper);
    }

    @Test
    public void routesTikTokUrlsToTikTokScraper() {
        assertTrue(RecipeScraperRegistry.forUrl("https://www.tiktok.com/@user/video/1") instanceof TikTokRecipeScraper);
    }

    @Test
    public void routesUnknownHostsToGenericScraper() {
        assertTrue(RecipeScraperRegistry.forUrl("https://example.com/recipes/cake") instanceof GenericRecipeScraper);
    }
}
