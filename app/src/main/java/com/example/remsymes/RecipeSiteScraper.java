package com.example.remsymes;

/**
 * A scraper that knows how to turn a fetched page (URL + raw HTML) into a draft {@link Recipe}.
 * Implementations are registered in {@link RecipeScraperRegistry} and matched by URL host.
 */
public interface RecipeSiteScraper {

    /** Whether this scraper knows how to handle the given URL (typically a host check). */
    boolean supports(String url);

    /** Extracts a best-effort draft {@link Recipe} from the page. Never returns null. */
    Recipe scrape(String url, String html);
}
