package com.example.remsymes;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TikTokRecipeScraperTest {

    private final TikTokRecipeScraper scraper = new TikTokRecipeScraper();

    @Test
    public void supportsTikTokUrlsOnly() {
        assertTrue(scraper.supports("https://www.tiktok.com/@user/video/12345"));
        assertFalse(scraper.supports("https://www.instagram.com/reel/abc"));
    }

    @Test
    public void extractsDescriptionFromSigiStateJson() {
        String html = "<html><body><script id=\"SIGI_STATE\" type=\"application/json\">"
                + "{\"ItemModule\":{\"123456\":{\"id\":\"123456\",\"desc\":\"Creamy Pasta Recipe\\nIngredients\\n2 cups flour\"}}}"
                + "</script></body></html>";

        assertEquals("Creamy Pasta Recipe\nIngredients\n2 cups flour", TikTokRecipeScraper.extractFromSigiState(html));
    }

    @Test
    public void fallsBackToOgDescriptionWhenSigiStateMissing() {
        String html = "<html><head><meta property=\"og:description\" content=\"Fallback caption\">"
                + "</head><body></body></html>";

        assertEquals("Fallback caption", TikTokRecipeScraper.extractDescription(html));
    }

    @Test
    public void extractFromSigiStateReturnsNullWhenScriptMissing() {
        assertNull(TikTokRecipeScraper.extractFromSigiState("<html><body>no script here</body></html>"));
    }

    @Test
    public void extractFromSigiStateReturnsNullOnMalformedJson() {
        String html = "<script id=\"SIGI_STATE\" type=\"application/json\">not valid json</script>";
        assertNull(TikTokRecipeScraper.extractFromSigiState(html));
    }

    @Test
    public void extractsDescriptionFromUniversalDataJsonWhenSigiStateMissing() {
        String html = "<html><body><script id=\"__UNIVERSAL_DATA_FOR_REHYDRATION__\">"
                + "{\"__DEFAULT_SCOPE__\":{\"webapp.video-detail\":{\"itemInfo\":{\"itemStruct\":"
                + "{\"id\":\"123456\",\"desc\":\"Creamy Pasta Recipe\\nIngredients\\n2 cups flour\"}}}}}"
                + "</script></body></html>";

        assertEquals("Creamy Pasta Recipe\nIngredients\n2 cups flour", TikTokRecipeScraper.extractDescription(html));
    }

    @Test
    public void extractFromUniversalDataReturnsNullWhenShapeUnexpected() {
        String html = "<script id=\"__UNIVERSAL_DATA_FOR_REHYDRATION__\">{\"somethingElse\":true}</script>";
        assertNull(TikTokRecipeScraper.extractFromUniversalData(html));
    }

    @Test
    public void scrapesDescriptionIntoRecipe() {
        String desc = "Creamy Pasta Recipe\\nIngredients\\n2 cups flour\\n1 egg\\nInstructions\\nCombine everything well\\nCook for 10 minutes";
        String html = "<html><body><script id=\"SIGI_STATE\" type=\"application/json\">"
                + "{\"ItemModule\":{\"123456\":{\"id\":\"123456\",\"desc\":\"" + desc + "\"}}}"
                + "</script></body></html>";

        Recipe recipe = scraper.scrape("https://www.tiktok.com/@user/video/12345", html);

        assertEquals("Creamy Pasta Recipe", recipe.getName());
        assertEquals("• cups flour\n• egg", recipe.getIngredients());
        assertEquals("1. Combine everything well\n2. Cook for 10 minutes", recipe.getInstructions());
    }
}
