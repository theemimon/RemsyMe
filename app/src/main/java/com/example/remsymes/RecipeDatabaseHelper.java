package com.example.remsymes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class RecipeDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "remsymes_recipes.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_RECIPES = "recipes";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_PREP_TIME = "prep_time";
    public static final String COLUMN_PORTIONS = "portions";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_IS_FAVORITE = "is_favorite";
    public static final String COLUMN_IMAGE_URI = "image_uri";
    public static final String COLUMN_INGREDIENTS = "ingredients";
    public static final String COLUMN_INSTRUCTIONS = "instructions";
    public static final String COLUMN_SOURCE_LINK = "source_link";
    public static final String COLUMN_SCRAPED_TEXT = "scraped_text";

    private static RecipeDatabaseHelper instance;

    public static synchronized RecipeDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new RecipeDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private RecipeDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_RECIPES_TABLE = "CREATE TABLE " + TABLE_RECIPES + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT NOT NULL, "
                + COLUMN_CATEGORY + " TEXT NOT NULL, "
                + COLUMN_PREP_TIME + " TEXT, "
                + COLUMN_PORTIONS + " TEXT, "
                + COLUMN_RATING + " REAL, "
                + COLUMN_IS_FAVORITE + " INTEGER DEFAULT 0, "
                + COLUMN_IMAGE_URI + " TEXT, "
                + COLUMN_INGREDIENTS + " TEXT, "
                + COLUMN_INSTRUCTIONS + " TEXT, "
                + COLUMN_SOURCE_LINK + " TEXT, "
                + COLUMN_SCRAPED_TEXT + " TEXT"
                + ")";
        db.execSQL(CREATE_RECIPES_TABLE);

        seedDefaultRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_RECIPES + " ADD COLUMN " + COLUMN_SOURCE_LINK + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_RECIPES + " ADD COLUMN " + COLUMN_SCRAPED_TEXT + " TEXT");
        } else {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
            onCreate(db);
        }
    }

    private void seedDefaultRecipes(SQLiteDatabase db) {
        insertSampleRecipe(db, "Creamy Avocado Herb Pasta", "Pasta & Italian", "20 mins", "2 Servings", 5.0, 1,
                "recipe_sample_image",
                "• 200g Fettuccine or Spaghetti\n• 2 ripe Avocados, pitted\n• 2 cloves Garlic, minced\n• ½ cup Fresh Basil leaves\n• 2 tbsp Extra Virgin Olive Oil\n• 1 tbsp Fresh Lemon Juice\n• ⅓ cup Grated Parmesan cheese\n• Salt and freshly ground Black Pepper to taste",
                "1. Cook pasta in a large pot of boiling salted water according to package instructions until al dente.\n2. In a food processor or blender, combine avocados, garlic, basil, olive oil, and lemon juice. Blend until smooth and creamy.\n3. Drain cooked pasta, reserving ¼ cup of pasta water.\n4. Toss hot pasta with avocado cream sauce, adding reserved water if needed. Season with salt, pepper, and parmesan.\n5. Garnish with fresh basil leaves and extra parmesan cheese before serving warm.");

        insertSampleRecipe(db, "Classic Spaghetti Carbonara", "Pasta & Italian", "25 mins", "3 Servings", 4.8, 0,
                "recipe_sample_main",
                "• 350g Spaghetti\n• 150g Pancetta or Guanciale, diced\n• 3 large Eggs\n• 50g Pecorino Romano cheese\n• Freshly cracked Black Pepper",
                "1. Boil spaghetti in salted water.\n2. Fry pancetta in a skillet until crisp.\n3. Whisk eggs and cheese in a bowl.\n4. Combine pasta with pancetta, remove from heat, and mix in egg sauce quickly until velvety.");

        insertSampleRecipe(db, "Berry Vanilla Cheesecake", "Desserts", "45 mins", "8 Servings", 4.9, 1,
                "recipe_sample_dessert",
                "• 200g Digestive biscuits, crushed\n• 75g Unsalted Butter, melted\n• 600g Cream Cheese\n• 100g Icing Sugar\n• 284ml Double Cream\n• 150g Mixed Fresh Berries",
                "1. Mix crushed biscuits with butter and press into tin base.\n2. Beat cream cheese, icing sugar, and double cream until smooth.\n3. Spread over base and chill for 4 hours.\n4. Top with fresh berries before serving.");

        insertSampleRecipe(db, "Chocolate Lava Cake", "Desserts", "15 mins", "2 Servings", 4.7, 0,
                "recipe_sample_dessert",
                "• 100g Dark Chocolate\n• 100g Butter\n• 2 Eggs\n• 75g Caster Sugar\n• 50g Plain Flour",
                "1. Melt dark chocolate and butter together.\n2. Whisk eggs and sugar until pale, then fold in melted chocolate and flour.\n3. Pour into greased ramekins and bake at 200°C for 12 mins.");

        insertSampleRecipe(db, "Grilled Lemon Herb Chicken", "Main Courses", "30 mins", "4 Servings", 4.8, 0,
                "recipe_sample_main",
                "• 4 Chicken Breasts\n• 2 tbsp Olive Oil\n• Juice of 1 Lemon\n• 2 cloves Garlic, minced\n• 1 tsp Dried Oregano",
                "1. Marinate chicken in olive oil, lemon juice, garlic, and oregano for 20 mins.\n2. Preheat grill to medium-high heat.\n3. Grill chicken for 6-8 mins per side until cooked through.");

        insertSampleRecipe(db, "Fluffy Berry Pancakes", "Breakfast & Brunch", "15 mins", "2 Servings", 4.9, 1,
                "recipe_sample_breakfast",
                "• 150g Self-raising Flour\n• 1 tsp Baking Powder\n• 1 Egg\n• 200ml Milk\n• 100g Fresh Blueberries\n• Maple Syrup for serving",
                "1. Whisk flour, baking powder, egg, and milk into a smooth batter.\n2. Heat a non-stick pan over medium heat and pour batter.\n3. Scatter blueberries on top, flip when bubbles form, and cook until golden.");
    }

    private void insertSampleRecipe(SQLiteDatabase db, String name, String category, String time, String portions,
                                     double rating, int isFav, String imageUri, String ingredients, String instructions) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_CATEGORY, category);
        cv.put(COLUMN_PREP_TIME, time);
        cv.put(COLUMN_PORTIONS, portions);
        cv.put(COLUMN_RATING, rating);
        cv.put(COLUMN_IS_FAVORITE, isFav);
        cv.put(COLUMN_IMAGE_URI, imageUri);
        cv.put(COLUMN_INGREDIENTS, ingredients);
        cv.put(COLUMN_INSTRUCTIONS, instructions);
        cv.put(COLUMN_SOURCE_LINK, "");
        cv.put(COLUMN_SCRAPED_TEXT, "");
        db.insert(TABLE_RECIPES, null, cv);
    }

    public long insertRecipe(Recipe recipe) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, recipe.getName());
        cv.put(COLUMN_CATEGORY, recipe.getCategory() != null ? recipe.getCategory() : "Uncategorized");
        cv.put(COLUMN_PREP_TIME, recipe.getPrepTime());
        cv.put(COLUMN_PORTIONS, recipe.getPortions());
        cv.put(COLUMN_RATING, recipe.getRating());
        cv.put(COLUMN_IS_FAVORITE, recipe.isFavorite() ? 1 : 0);
        cv.put(COLUMN_IMAGE_URI, recipe.getImageUri());
        cv.put(COLUMN_INGREDIENTS, recipe.getIngredients());
        cv.put(COLUMN_INSTRUCTIONS, recipe.getInstructions());
        cv.put(COLUMN_SOURCE_LINK, recipe.getSourceLink() != null ? recipe.getSourceLink() : "");
        cv.put(COLUMN_SCRAPED_TEXT, recipe.getScrapedText() != null ? recipe.getScrapedText() : "");

        long id = db.insert(TABLE_RECIPES, null, cv);
        recipe.setId(id);
        return id;
    }

    public Recipe getRecipeById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        Recipe recipe = null;
        if (cursor != null && cursor.moveToFirst()) {
            recipe = cursorToRecipe(cursor);
            cursor.close();
        }
        return recipe;
    }

    public List<Recipe> getRecipesByCategory(String category) {
        if ("Faves".equalsIgnoreCase(category) || "Favorites".equalsIgnoreCase(category)) {
            return getFavoriteRecipes();
        }
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, COLUMN_CATEGORY + "=?",
                new String[]{category}, null, null, COLUMN_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToRecipe(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Recipe> getFavoriteRecipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, COLUMN_IS_FAVORITE + "=1",
                null, null, null, COLUMN_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToRecipe(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        categories.add("Faves");

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT " + COLUMN_CATEGORY + " FROM " + TABLE_RECIPES + " ORDER BY " + COLUMN_CATEGORY + " ASC", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String cat = cursor.getString(0);
                if (cat != null && !cat.trim().isEmpty() && !categories.contains(cat) && !"Faves".equalsIgnoreCase(cat)) {
                    categories.add(cat);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (!categories.contains("Pasta & Italian")) categories.add("Pasta & Italian");
        if (!categories.contains("Desserts")) categories.add("Desserts");
        if (!categories.contains("Main Courses")) categories.add("Main Courses");
        if (!categories.contains("Breakfast & Brunch")) categories.add("Breakfast & Brunch");
        if (!categories.contains("Uncategorized")) categories.add("Uncategorized");

        return categories;
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, null, null, null, null, COLUMN_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToRecipe(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public int updateRecipe(Recipe recipe) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, recipe.getName());
        cv.put(COLUMN_CATEGORY, recipe.getCategory());
        cv.put(COLUMN_PREP_TIME, recipe.getPrepTime());
        cv.put(COLUMN_PORTIONS, recipe.getPortions());
        cv.put(COLUMN_RATING, recipe.getRating());
        cv.put(COLUMN_IS_FAVORITE, recipe.isFavorite() ? 1 : 0);
        cv.put(COLUMN_IMAGE_URI, recipe.getImageUri());
        cv.put(COLUMN_INGREDIENTS, recipe.getIngredients());
        cv.put(COLUMN_INSTRUCTIONS, recipe.getInstructions());
        cv.put(COLUMN_SOURCE_LINK, recipe.getSourceLink());
        cv.put(COLUMN_SCRAPED_TEXT, recipe.getScrapedText());

        return db.update(TABLE_RECIPES, cv, COLUMN_ID + "=?", new String[]{String.valueOf(recipe.getId())});
    }

    public int deleteRecipe(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_RECIPES, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int moveCategoryRecipesToUncategorized(String oldCategory) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_CATEGORY, "Uncategorized");
        return db.update(TABLE_RECIPES, cv, COLUMN_CATEGORY + "=?", new String[]{oldCategory});
    }

    public int getRecipeCountByCategory(String category) {
        if ("Faves".equalsIgnoreCase(category) || "Favorites".equalsIgnoreCase(category)) {
            return getFavoriteRecipesCount();
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_RECIPES + " WHERE " + COLUMN_CATEGORY + "=?", new String[]{category});
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    public int getFavoriteRecipesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_RECIPES + " WHERE " + COLUMN_IS_FAVORITE + "=1", null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    private Recipe cursorToRecipe(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
        String time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PREP_TIME));
        String portions = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PORTIONS));
        double rating = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RATING));
        boolean isFav = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_FAVORITE)) == 1;
        String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI));
        String ingredients = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENTS));
        String instructions = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INSTRUCTIONS));

        String sourceLink = "";
        int linkIdx = cursor.getColumnIndex(COLUMN_SOURCE_LINK);
        if (linkIdx != -1 && !cursor.isNull(linkIdx)) {
            sourceLink = cursor.getString(linkIdx);
        }

        String scrapedText = "";
        int textIdx = cursor.getColumnIndex(COLUMN_SCRAPED_TEXT);
        if (textIdx != -1 && !cursor.isNull(textIdx)) {
            scrapedText = cursor.getString(textIdx);
        }

        return new Recipe(id, name, category, time, portions, rating, isFav, imageUri, ingredients, instructions, sourceLink, scrapedText);
    }
}