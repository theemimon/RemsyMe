package me.remsy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RecipeParserTest {

    @Test
    public void nullTextProducesImportedRecipePlaceholder() {
        Recipe recipe = RecipeParser.parseSharedText(null);
        assertEquals("Imported Recipe", recipe.getName());
        assertEquals("Uncategorized", recipe.getCategory());
        assertEquals("", recipe.getIngredients());
        assertEquals("", recipe.getInstructions());
    }

    @Test
    public void blankTextProducesImportedRecipePlaceholder() {
        Recipe recipe = RecipeParser.parseSharedText("   ");
        assertEquals("Imported Recipe", recipe.getName());
    }

    @Test
    public void parsesTitleIngredientsAndInstructionsFromExplicitSections() {
        String sharedText = "Amazing Pasta Recipe\n"
                + "Ingredients:\n"
                + "2 cups flour\n"
                + "1 egg\n"
                + "Instructions:\n"
                + "Mix everything together\n"
                + "Cook for 10 minutes";

        Recipe recipe = RecipeParser.parseSharedText(sharedText);

        assertEquals("Amazing Pasta Recipe", recipe.getName());
        assertEquals("Uncategorized", recipe.getCategory());
        assertEquals("10 minutes", recipe.getPrepTime());
        assertEquals("• 2 cups flour\n• 1 egg", recipe.getIngredients());
        assertEquals("Mix everything together\nCook for 10 minutes", recipe.getInstructions());
    }

    @Test
    public void derivesTitleFromUrlWhenFirstLineIsALink() {
        String sharedText = "https://example.com/recipes/amazing-chocolate-cake";

        Recipe recipe = RecipeParser.parseSharedText(sharedText);

        assertEquals("Amazing Chocolate Cake", recipe.getName());
    }

    @Test
    public void extractsServingsFromServingsLine() {
        String sharedText = "Weeknight Chili\nServes: 4\nIngredients:\nBeans\nInstructions:\nSimmer slowly in a pot for a long while";

        Recipe recipe = RecipeParser.parseSharedText(sharedText);

        assertEquals("4 Servings", recipe.getPortions());
    }
}
