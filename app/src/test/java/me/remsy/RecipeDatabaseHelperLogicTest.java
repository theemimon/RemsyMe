package me.remsy;

import android.content.ContentValues;
import android.database.Cursor;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RecipeDatabaseHelperLogicTest {

    // ---- buildCategoryList ----

    @Test
    public void nullOrEmptyRawListStillReturnsFavesAndAllDefaultCategories() {
        List<String> result = RecipeDatabaseHelper.buildCategoryList(null);
        assertEquals(Arrays.asList("Faves", "Pasta & Italian", "Desserts", "Main Courses",
                "Breakfast & Brunch", "Uncategorized"), result);

        List<String> resultForEmpty = RecipeDatabaseHelper.buildCategoryList(new ArrayList<>());
        assertEquals(result, resultForEmpty);
    }

    @Test
    public void dedupesBlanksAndCaseInsensitiveFavesWhileKeepingCustomCategories() {
        List<String> raw = Arrays.asList("Pasta & Italian", "", null, "FAVES", "Snacks");

        List<String> result = RecipeDatabaseHelper.buildCategoryList(raw);

        assertEquals(Arrays.asList("Faves", "Pasta & Italian", "Snacks", "Desserts", "Main Courses",
                "Breakfast & Brunch", "Uncategorized"), result);
    }

    // ---- buildUpgradeStatements (regression test for the former destructive migration bug) ----

    @Test
    public void upgradingFromVersionOneAddsNewColumnsOnly() {
        List<String> statements = RecipeDatabaseHelper.buildUpgradeStatements(1, 2);

        assertEquals(2, statements.size());
        for (String statement : statements) {
            assertTrue(statement.startsWith("ALTER TABLE"));
        }
    }

    @Test
    public void upgradeStatementsNeverDropTheRecipesTable() {
        for (int oldVersion = 0; oldVersion <= 5; oldVersion++) {
            for (int newVersion = oldVersion + 1; newVersion <= 6; newVersion++) {
                List<String> statements = RecipeDatabaseHelper.buildUpgradeStatements(oldVersion, newVersion);
                for (String statement : statements) {
                    assertFalse("Migration must never drop data: " + statement,
                            statement.toUpperCase().contains("DROP"));
                }
            }
        }
    }

    @Test
    public void upgradingFromCurrentVersionProducesNoStatements() {
        assertTrue(RecipeDatabaseHelper.buildUpgradeStatements(2, 3).isEmpty());
    }

    // ---- populateContentValues ----

    @Test
    public void populateContentValuesAppliesNullSafeFallbacksForCategorySourceAndScrapedText() {
        ContentValues cv = mock(ContentValues.class);
        Recipe recipe = new Recipe(1, "Soup", null, "10 mins", "2 Servings", 4.5, true,
                "recipe_sample_image", "• Water", "1. Boil", null, null);

        RecipeDatabaseHelper.populateContentValues(cv, recipe);

        verify(cv).put(RecipeDatabaseHelper.COLUMN_CATEGORY, "Uncategorized");
        verify(cv).put(RecipeDatabaseHelper.COLUMN_SOURCE_LINK, "");
        verify(cv).put(RecipeDatabaseHelper.COLUMN_SCRAPED_TEXT, "");
        verify(cv).put(RecipeDatabaseHelper.COLUMN_NAME, "Soup");
        verify(cv).put(RecipeDatabaseHelper.COLUMN_IS_FAVORITE, 1);
    }

    @Test
    public void populateContentValuesPreservesProvidedCategoryAndLinks() {
        ContentValues cv = mock(ContentValues.class);
        Recipe recipe = new Recipe(1, "Soup", "Main Courses", "10 mins", "2 Servings", 4.5, false,
                "recipe_sample_image", "• Water", "1. Boil", "https://example.com", "raw text");

        RecipeDatabaseHelper.populateContentValues(cv, recipe);

        verify(cv).put(RecipeDatabaseHelper.COLUMN_CATEGORY, "Main Courses");
        verify(cv).put(RecipeDatabaseHelper.COLUMN_SOURCE_LINK, "https://example.com");
        verify(cv).put(RecipeDatabaseHelper.COLUMN_SCRAPED_TEXT, "raw text");
        verify(cv).put(RecipeDatabaseHelper.COLUMN_IS_FAVORITE, 0);
    }

    // ---- cursorToRecipe ----

    @Test
    public void cursorToRecipeMapsAllColumnsIncludingOptionalOnes() {
        Cursor cursor = mock(Cursor.class);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_ID, 0);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_NAME, 1);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_CATEGORY, 2);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_PREP_TIME, 3);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_PORTIONS, 4);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_RATING, 5);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_IS_FAVORITE, 6);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_IMAGE_URI, 7);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_INGREDIENTS, 8);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_INSTRUCTIONS, 9);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_SOURCE_LINK, 10);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_SCRAPED_TEXT, 11);

        when(cursor.getLong(0)).thenReturn(42L);
        when(cursor.getString(1)).thenReturn("Soup");
        when(cursor.getString(2)).thenReturn("Main Courses");
        when(cursor.getString(3)).thenReturn("10 mins");
        when(cursor.getString(4)).thenReturn("2 Servings");
        when(cursor.getDouble(5)).thenReturn(4.5);
        when(cursor.getInt(6)).thenReturn(1);
        when(cursor.getString(7)).thenReturn("recipe_sample_image");
        when(cursor.getString(8)).thenReturn("• Water");
        when(cursor.getString(9)).thenReturn("1. Boil");
        when(cursor.isNull(10)).thenReturn(false);
        when(cursor.getString(10)).thenReturn("https://example.com");
        when(cursor.isNull(11)).thenReturn(false);
        when(cursor.getString(11)).thenReturn("raw text");

        Recipe recipe = RecipeDatabaseHelper.cursorToRecipe(cursor);

        assertEquals(42L, recipe.getId());
        assertEquals("Soup", recipe.getName());
        assertEquals("Main Courses", recipe.getCategory());
        assertTrue(recipe.isFavorite());
        assertEquals(4.5, recipe.getRating(), 0.0001);
        assertEquals("https://example.com", recipe.getSourceLink());
        assertEquals("raw text", recipe.getScrapedText());
    }

    @Test
    public void cursorToRecipeDefaultsOptionalColumnsWhenMissingFromOlderSchema() {
        Cursor cursor = mock(Cursor.class);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_ID, 0);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_NAME, 1);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_CATEGORY, 2);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_PREP_TIME, 3);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_PORTIONS, 4);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_RATING, 5);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_IS_FAVORITE, 6);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_IMAGE_URI, 7);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_INGREDIENTS, 8);
        stubColumnIndex(cursor, RecipeDatabaseHelper.COLUMN_INSTRUCTIONS, 9);
        // Simulate a pre-migration row: source_link/scraped_text columns don't exist yet.
        when(cursor.getColumnIndex(RecipeDatabaseHelper.COLUMN_SOURCE_LINK)).thenReturn(-1);
        when(cursor.getColumnIndex(RecipeDatabaseHelper.COLUMN_SCRAPED_TEXT)).thenReturn(-1);

        when(cursor.getLong(0)).thenReturn(1L);
        when(cursor.getString(2)).thenReturn("Uncategorized");

        Recipe recipe = RecipeDatabaseHelper.cursorToRecipe(cursor);

        assertEquals("", recipe.getSourceLink());
        assertEquals("", recipe.getScrapedText());
    }

    private static void stubColumnIndex(Cursor cursor, String columnName, int index) {
        when(cursor.getColumnIndexOrThrow(columnName)).thenReturn(index);
        when(cursor.getColumnIndex(columnName)).thenReturn(index);
    }
}
