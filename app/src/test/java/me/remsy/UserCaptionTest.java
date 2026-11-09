package me.remsy;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UserCaptionTest {

    @Test
    public void testParsesInlineCaptionWithSubheadersAndInstructions() {
        String caption = "Who else lives olives? 🫒 Recipe -  Olive and Garlic Cream Cheese Buns  / "
                + "Ingredients: For the dough: "
                + "- 350g all purpose flour (2 cups + 3/4 cup + 1 tablespoon) "
                + "- 35g granulated sugar (3 tablespoons) "
                + "- 7g instant dry yeast  (1 packet) "
                + "- 7g fine sea salt (1 teaspoon + 1/4 teaspoon) "
                + "- 50g unsalted butter, melted and cooled (3 tablespoons + 1 teaspoon) "
                + "- 1 large egg "
                + "- 150g warm milk  (1/2 cup + 2 tablespoons) "
                + "- 100g grated Cheddar Cheese (1 cup) "
                + "- 100g grated Emmentaler Cheese (1 cup) "
                + "For the filling: "
                + "- 200g Chive Philadelphia Cream Cheese "
                + "- Green Olives "
                + "- Kalamata Olives "
                + "- Grated Emmentaler Cheese  "
                + "- An egg, for an egg wash "
                + "- Microgreens, for toppings "
                + "Instructions: "
                + "1. To the bowl of your stand mixer, add the flour, salt, sugar, instant dry yeast, grated Cheddar and grated Emmentaler cheese. Whisk everything together. "
                + "2. Next, add the milk, egg and melted butter. "
                + "3. Using the dough hook attachment, knead on low speed until the dough becomes smooth and forms a ball, about 6-8 minutes. "
                + "4. Once ready, transfer the dough to a clean, lightly oiled bowl. "
                + "5. Cover and let it rise at room temperature for 2 hours. "
                + "6. After 2 hours, punch the dough down and divide it into 8 equal balls, about 105 g each. "
                + "7. Place them on a large baking tray lined with parchment paper, cover and let them rise for another 1 hour. "
                + "8. Preheat the oven to 180°C. "
                + "9. Brush each bun with the egg wash. "
                + "10. Fill each bun with a little cream cheese and top with the olives. "
                + "11. Sprinkle with more grated cheese. "
                + "12. Bake in the preheated oven for 20-25 minutes, or until the buns are golden brown. "
                + "13. Once ready, top with microgreens and serve warm. "
                + "#bread #breakfastrecipes #foodtok #asmr #baking";

        Recipe recipe = LlmRecipeExtractor.extractRecipeFromText(caption, null);

        // 1. Verify Title
        assertEquals("Olive And Garlic Cream Cheese Buns", recipe.getName());

        // 2. Verify Ingredients
        List<Ingredient> ingredients = Ingredient.parseList(recipe.getIngredients());
        assertTrue("Should extract ingredients list", ingredients.size() >= 15);

        // Check sub-headers and ingredients
        assertTrue(recipe.getIngredients().contains("For the dough:"));
        assertTrue(recipe.getIngredients().contains("For the filling:"));
        assertTrue(recipe.getIngredients().contains("350g all purpose flour"));
        assertTrue(recipe.getIngredients().contains("200g Chive Philadelphia Cream Cheese"));

        // 3. Verify Instructions
        List<InstructionStep> steps = InstructionStep.parseList(recipe.getInstructions());
        assertEquals("Should extract all 13 instruction steps", 13, steps.size());
        assertEquals(1, steps.get(0).getStepNumber());
        assertTrue(steps.get(0).getInstructionText().contains("add the flour"));
        assertEquals(13, steps.get(12).getStepNumber());
        assertTrue(steps.get(12).getInstructionText().contains("top with microgreens and serve warm"));
    }
}
