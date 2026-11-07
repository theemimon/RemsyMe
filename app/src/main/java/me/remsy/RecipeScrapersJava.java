package me.remsy;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecipeScrapersJava {

    public static Recipe scrape(String url, String html) {
        if (html == null || html.trim().isEmpty()) {
            return LlmRecipeExtractor.extractRecipeFromText(url, url);
        }

        String rawScrapedText = HtmlTextExtractor.extractRawTextFromHtml(html);

        // 1. JSON-LD Schema.org Recipe (Universal recipe-scrapers strategy)
        Recipe jsonLdRecipe = parseJsonLdRecipe(html, url, rawScrapedText);
        if (jsonLdRecipe != null && (!jsonLdRecipe.getIngredients().isEmpty() || !jsonLdRecipe.getInstructions().isEmpty())) {
            return jsonLdRecipe;
        }

        // 2. Tasty Recipes Plugin (.tasty-recipes)
        Recipe tastyRecipe = parseTastyRecipes(html, url, rawScrapedText);
        if (tastyRecipe != null) {
            return tastyRecipe;
        }

        // 3. WordPress Recipe Maker Plugin (.wprm-recipe)
        Recipe wprmRecipe = parseWprmRecipe(html, url, rawScrapedText);
        if (wprmRecipe != null) {
            return wprmRecipe;
        }

        // 4. Mediavine Create Plugin (.mv-create)
        Recipe mvRecipe = parseMediavineCreateRecipe(html, url, rawScrapedText);
        if (mvRecipe != null) {
            return mvRecipe;
        }

        // 5. Fallback: LLM Semantic Extraction on clean text
        return LlmRecipeExtractor.extractRecipeFromText(rawScrapedText, url);
    }

    private static Recipe parseJsonLdRecipe(String html, String url, String rawScrapedText) {
        Pattern jsonLdPattern = Pattern.compile("<script[^>]*type=[\"']application/ld\\+json[\"'][^>]*>(.*?)</script>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = jsonLdPattern.matcher(html);

        while (matcher.find()) {
            String jsonContent = matcher.group(1);
            if (jsonContent == null) continue;
            jsonContent = jsonContent.trim();

            try {
                if (jsonContent.startsWith("[")) {
                    JSONArray array = new JSONArray(jsonContent);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.optJSONObject(i);
                        if (obj != null && isRecipeJson(obj)) {
                            return extractFromJsonObj(obj, url, rawScrapedText);
                        }
                    }
                } else if (jsonContent.startsWith("{")) {
                    JSONObject obj = new JSONObject(jsonContent);
                    if (obj.has("@graph")) {
                        JSONArray graph = obj.optJSONArray("@graph");
                        if (graph != null) {
                            for (int i = 0; i < graph.length(); i++) {
                                JSONObject item = graph.optJSONObject(i);
                                if (item != null && isRecipeJson(item)) {
                                    return extractFromJsonObj(item, url, rawScrapedText);
                                }
                            }
                        }
                    } else if (isRecipeJson(obj)) {
                        return extractFromJsonObj(obj, url, rawScrapedText);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static boolean isRecipeJson(JSONObject obj) {
        String type = obj.optString("@type");
        return type.equalsIgnoreCase("Recipe") || type.contains("Recipe");
    }

    private static Recipe extractFromJsonObj(JSONObject obj, String url, String rawScrapedText) {
        String title = cleanText(obj.optString("name", ""));
        if (title.isEmpty()) {
            title = LlmRecipeExtractor.extractRecipeFromText(url, url).getName();
        }

        String category = "Uncategorized";
        Object catObj = obj.opt("recipeCategory");
        if (catObj instanceof String) {
            category = (String) catObj;
        } else if (catObj instanceof JSONArray) {
            JSONArray arr = (JSONArray) catObj;
            if (arr.length() > 0) category = arr.optString(0);
        }

        String time = formatIsoDuration(obj.optString("totalTime", obj.optString("prepTime", "")));
        String portions = obj.optString("recipeYield", "");

        StringBuilder ingredients = new StringBuilder();
        JSONArray ingArr = obj.optJSONArray("recipeIngredient");
        if (ingArr != null) {
            for (int i = 0; i < ingArr.length(); i++) {
                if (ingredients.length() > 0) ingredients.append("\n");
                ingredients.append("• ").append(cleanText(ingArr.optString(i)));
            }
        }

        StringBuilder instructions = new StringBuilder();
        Object instObj = obj.opt("recipeInstructions");
        if (instObj instanceof JSONArray) {
            JSONArray instArr = (JSONArray) instObj;
            for (int i = 0; i < instArr.length(); i++) {
                Object item = instArr.opt(i);
                if (item instanceof JSONObject) {
                    JSONObject stepObj = (JSONObject) item;
                    String text = stepObj.optString("text", stepObj.optString("name", ""));
                    if (!text.isEmpty()) {
                        if (instructions.length() > 0) instructions.append("\n");
                        instructions.append(i + 1).append(". ").append(cleanText(text));
                    }
                } else if (item instanceof String) {
                    if (instructions.length() > 0) instructions.append("\n");
                    instructions.append(i + 1).append(". ").append(cleanText((String) item));
                }
            }
        } else if (instObj instanceof String) {
            instructions.append(cleanText((String) instObj));
        }

        return new Recipe(0, title, category, time, portions, 0.0, false, "recipe_sample_image", ingredients.toString(), instructions.toString(), url, rawScrapedText);
    }

    private static Recipe parseTastyRecipes(String html, String url, String rawScrapedText) {
        if (!html.toLowerCase().contains("tasty-recipes")) return null;

        String title = extractTagByClass(html, "tasty-recipes-title");
        String time = extractTagByClass(html, "tasty-recipes-total-time");
        if (time.isEmpty()) time = extractTagByClass(html, "tasty-recipes-prep-time");
        String portions = extractTagByClass(html, "tasty-recipes-yield");

        List<String> rawIngs = extractListByClass(html, "tasty-recipes-ingredients");
        StringBuilder ingredients = new StringBuilder();
        for (String ing : rawIngs) {
            if (ingredients.length() > 0) ingredients.append("\n");
            ingredients.append("• ").append(ing);
        }

        List<String> rawInsts = extractListByClass(html, "tasty-recipes-instructions");
        StringBuilder instructions = new StringBuilder();
        for (int i = 0; i < rawInsts.size(); i++) {
            if (instructions.length() > 0) instructions.append("\n");
            instructions.append(i + 1).append(". ").append(rawInsts.get(i));
        }

        if (ingredients.length() > 0 || instructions.length() > 0) {
            return new Recipe(0, title, "Uncategorized", time, portions, 0.0, false, "recipe_sample_image", ingredients.toString(), instructions.toString(), url, rawScrapedText);
        }
        return null;
    }

    private static Recipe parseWprmRecipe(String html, String url, String rawScrapedText) {
        if (!html.toLowerCase().contains("wprm-recipe")) return null;

        String title = extractTagByClass(html, "wprm-recipe-name");
        String time = extractTagByClass(html, "wprm-recipe-total-time-container");
        String portions = extractTagByClass(html, "wprm-recipe-servings");

        List<String> rawIngs = extractListByClass(html, "wprm-recipe-ingredient");
        StringBuilder ingredients = new StringBuilder();
        for (String ing : rawIngs) {
            if (ingredients.length() > 0) ingredients.append("\n");
            ingredients.append("• ").append(ing);
        }

        List<String> rawInsts = extractListByClass(html, "wprm-recipe-instruction");
        StringBuilder instructions = new StringBuilder();
        for (int i = 0; i < rawInsts.size(); i++) {
            if (instructions.length() > 0) instructions.append("\n");
            instructions.append(i + 1).append(". ").append(rawInsts.get(i));
        }

        if (ingredients.length() > 0 || instructions.length() > 0) {
            return new Recipe(0, title, "Uncategorized", time, portions, 0.0, false, "recipe_sample_image", ingredients.toString(), instructions.toString(), url, rawScrapedText);
        }
        return null;
    }

    private static Recipe parseMediavineCreateRecipe(String html, String url, String rawScrapedText) {
        if (!html.toLowerCase().contains("mv-create")) return null;

        String title = extractTagByClass(html, "mv-create-title");
        String time = extractTagByClass(html, "mv-create-time-total");
        String portions = extractTagByClass(html, "mv-create-yield");

        List<String> rawIngs = extractListByClass(html, "mv-create-ingredients");
        StringBuilder ingredients = new StringBuilder();
        for (String ing : rawIngs) {
            if (ingredients.length() > 0) ingredients.append("\n");
            ingredients.append("• ").append(ing);
        }

        List<String> rawInsts = extractListByClass(html, "mv-create-instructions");
        StringBuilder instructions = new StringBuilder();
        for (int i = 0; i < rawInsts.size(); i++) {
            if (instructions.length() > 0) instructions.append("\n");
            instructions.append(i + 1).append(". ").append(rawInsts.get(i));
        }

        if (ingredients.length() > 0 || instructions.length() > 0) {
            return new Recipe(0, title, "Uncategorized", time, portions, 0.0, false, "recipe_sample_image", ingredients.toString(), instructions.toString(), url, rawScrapedText);
        }
        return null;
    }

    private static String extractTagByClass(String html, String className) {
        Pattern pattern = Pattern.compile("<[^>]*class=[\"'][^\"']*" + className + "[^\"']*[\"'][^>]*>(.*?)</[^>]+>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            String content = matcher.group(1);
            return content != null ? cleanText(content) : "";
        }
        return "";
    }

    private static List<String> extractListByClass(String html, String className) {
        List<String> list = new ArrayList<>();
        Pattern pattern = Pattern.compile("<[^>]*class=[\"'][^\"']*" + className + "[^\"']*[\"'][^>]*>(.*?)</[^>]+>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String text = matcher.group(1);
            if (text != null) {
                String clean = cleanText(text);
                if (!clean.isEmpty() && clean.length() > 2 && !list.contains(clean)) {
                    list.add(clean);
                }
            }
        }
        return list;
    }

    private static String cleanText(String str) {
        if (str == null) return "";
        return str.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
    }

    private static String formatIsoDuration(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        try {
            String clean = iso.replace("PT", "").replace("P", "");
            int mins = 0;
            if (clean.contains("H")) {
                String[] parts = clean.split("H");
                int hours = Integer.parseInt(parts[0]);
                mins += hours * 60;
                clean = parts.length > 1 ? parts[1] : "";
            }
            if (clean.contains("M")) {
                String minutesStr = clean.replace("M", "");
                if (!minutesStr.isEmpty()) {
                    mins += Integer.parseInt(minutesStr);
                }
            }
            if (mins > 0) {
                return mins + " mins";
            }
        } catch (Exception ignored) {
        }
        return iso;
    }
}