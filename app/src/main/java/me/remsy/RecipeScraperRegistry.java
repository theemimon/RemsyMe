package me.remsy;

import java.util.Arrays;
import java.util.List;

/** Picks the right {@link RecipeSiteScraper} for a URL, falling back to the generic scraper. */
public final class RecipeScraperRegistry {

    private static final List<RecipeSiteScraper> SITE_SCRAPERS = Arrays.asList(
            new InstagramRecipeScraper(),
            new YouTubeRecipeScraper(),
            new TikTokRecipeScraper()
    );

    private static final RecipeSiteScraper GENERIC_SCRAPER = new GenericRecipeScraper();

    private RecipeScraperRegistry() {
    }

    public static RecipeSiteScraper forUrl(String url) {
        for (RecipeSiteScraper scraper : SITE_SCRAPERS) {
            if (scraper.supports(url)) {
                return scraper;
            }
        }
        return GENERIC_SCRAPER;
    }
}
