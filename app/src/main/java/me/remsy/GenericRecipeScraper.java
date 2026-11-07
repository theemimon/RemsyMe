package me.remsy;

/**
 * Delegates whole-page scraping to the existing JSON-LD / recipe-plugin / heuristic pipeline
 * in {@link RecipeScrapersJava}. Used both as the fallback for unknown hosts and as the
 * fallback within site-specific scrapers when they can't find a description/caption.
 */
public final class GenericRecipeScraper implements RecipeSiteScraper {

    @Override
    public boolean supports(String url) {
        return true;
    }

    @Override
    public Recipe scrape(String url, String html) {
        return RecipeScrapersJava.scrape(url, html);
    }
}
