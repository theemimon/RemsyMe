package me.remsy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LlmRecipeExtractorTest {

    @Test
    public void nullTextProducesImportedRecipePlaceholder() {
        Recipe recipe = LlmRecipeExtractor.extractRecipeFromText(null, "https://example.com/recipes/choco-cake");
        assertEquals("Imported Recipe", recipe.getName());
        assertEquals("https://example.com/recipes/choco-cake", recipe.getSourceLink());
    }

    @Test
    public void parsesTitleIngredientsAndNumberedInstructions() {
        String text = "Amazing Pasta Recipe\n"
                + "\n"
                + "Ingredients\n"
                + "2 cups flour\n"
                + "1 egg\n"
                + "\n"
                + "Instructions\n"
                + "Combine everything well\n"
                + "Cook for 10 minutes until done";

        Recipe recipe = LlmRecipeExtractor.extractRecipeFromText(text, "https://example.com/pasta");

        assertEquals("Amazing Pasta Recipe", recipe.getName());
        assertEquals("Uncategorized", recipe.getCategory());
        assertEquals("", recipe.getPrepTime());
        assertEquals("• 2 cups flour\n• 1 egg", recipe.getIngredients());
        assertEquals("1. Combine everything well\n2. Cook for 10 minutes until done", recipe.getInstructions());
    }

    @Test
    public void extractsExplicitPrepTimeLabel() {
        String text = "Weeknight Tacos\nPrep Time: 15 mins\nIngredients\nTortillas\nInstructions\nAssemble and serve warm to everyone";

        Recipe recipe = LlmRecipeExtractor.extractRecipeFromText(text, "https://example.com/tacos");

        assertEquals("15 mins", recipe.getPrepTime());
    }

    @Test
    public void extractsExplicitServingsLabel() {
        String text = "Weeknight Tacos\nServings: 4\nIngredients\nTortillas\nInstructions\nAssemble and serve warm to everyone";

        Recipe recipe = LlmRecipeExtractor.extractRecipeFromText(text, "https://example.com/tacos");

        assertEquals("4 Servings", recipe.getPortions());
    }

    /**
     * Known limitation (flagged in the audit, not fixed here due to regression risk): header
     * detection is a plain "contains(ingredient-ish word)" check, so an instruction sentence that
     * happens to mention "ingredients" is misread as the start of a new ingredients section,
     * which swallows that sentence and misroutes whatever follows it. This test documents the
     * current behavior so a future fix can be verified against it deliberately.
     */
    @Test
    public void knownLimitation_instructionSentenceMentioningIngredientsIsMisreadAsNewSectionHeader() {
        String text = "Weeknight Tacos\n"
                + "Ingredients\n"
                + "Tortillas\n"
                + "Instructions\n"
                + "Combine all ingredients in a bowl\n"
                + "Serve warm to everyone";

        Recipe recipe = LlmRecipeExtractor.extractRecipeFromText(text, "https://example.com/tacos");

        // The instruction line itself is swallowed as a (false) section header, and the line
        // after it is misfiled as an "ingredient" instead of staying an instruction.
        assertEquals("• Tortillas\n• Serve warm to everyone", recipe.getIngredients());
    }

    @Test
    public void fallsBackToRawTextWhenNoInstructionsFound() {
        String text = "Just A Title";

        Recipe recipe = LlmRecipeExtractor.extractRecipeFromText(text, "https://example.com/x");

        assertEquals("Just A Title", recipe.getInstructions());
    }
}
