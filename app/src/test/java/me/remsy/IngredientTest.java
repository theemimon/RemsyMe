package me.remsy;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class IngredientTest {

    @Test
    public void testParseWithExplicitDelimiter() {
        Ingredient ing = Ingredient.parse("200g | Fettuccine or Spaghetti");
        assertEquals("200g", ing.getQuantity());
        assertEquals("Fettuccine or Spaghetti", ing.getName());
    }

    @Test
    public void testParseWithBulletAndQuantity() {
        Ingredient ing = Ingredient.parse("• 200g Fettuccine or Spaghetti");
        assertEquals("200g", ing.getQuantity());
        assertEquals("Fettuccine or Spaghetti", ing.getName());
    }

    @Test
    public void testParseWithFractionsAndUnits() {
        Ingredient ing1 = Ingredient.parse("½ cup Fresh Basil leaves");
        assertEquals("½ cup", ing1.getQuantity());
        assertEquals("Fresh Basil leaves", ing1.getName());

        Ingredient ing2 = Ingredient.parse("• 2 cloves Garlic, minced");
        assertEquals("2 cloves", ing2.getQuantity());
        assertEquals("Garlic, minced", ing2.getName());

        Ingredient ing3 = Ingredient.parse("1/2 tsp salt");
        assertEquals("1/2 tsp", ing3.getQuantity());
        assertEquals("salt", ing3.getName());
    }

    @Test
    public void testParseWithoutQuantity() {
        Ingredient ing = Ingredient.parse("Salt and freshly ground Black Pepper to taste");
        assertEquals("", ing.getQuantity());
        assertEquals("Salt and freshly ground Black Pepper to taste", ing.getName());
    }

    @Test
    public void testStorageStringSerialization() {
        Ingredient ing1 = new Ingredient("200g", "Fettuccine or Spaghetti");
        assertEquals("200g | Fettuccine or Spaghetti", ing1.toStorageString());

        Ingredient ing2 = new Ingredient("", "Salt to taste");
        assertEquals("Salt to taste", ing2.toStorageString());

        List<Ingredient> list = Arrays.asList(ing1, ing2);
        String serialized = Ingredient.toStorageString(list);
        assertEquals("200g | Fettuccine or Spaghetti\nSalt to taste", serialized);
    }

    @Test
    public void testParseListAndRoundTrip() {
        String raw = "200g | Fettuccine\n½ cup | Basil\nSalt to taste";
        List<Ingredient> ingredients = Ingredient.parseList(raw);
        assertEquals(3, ingredients.size());
        assertEquals("200g", ingredients.get(0).getQuantity());
        assertEquals("Fettuccine", ingredients.get(0).getName());
        assertEquals("½ cup", ingredients.get(1).getQuantity());
        assertEquals("Basil", ingredients.get(1).getName());
        assertEquals("", ingredients.get(2).getQuantity());
        assertEquals("Salt to taste", ingredients.get(2).getName());

        String roundTrip = Ingredient.toStorageString(ingredients);
        assertEquals("200g | Fettuccine\n½ cup | Basil\nSalt to taste", roundTrip);
    }
}
