package me.remsy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ingredient {
    private String quantity;
    private String name;

    public Ingredient() {
        this("", "");
    }

    public Ingredient(String quantity, String name) {
        this.quantity = quantity != null ? quantity.trim() : "";
        this.name = name != null ? name.trim() : "";
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity != null ? quantity.trim() : "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : "";
    }

    /**
     * Serializes a single ingredient for storage/string representation.
     * If quantity is present, format is "quantity | name".
     * If quantity is empty, format is "name".
     */
    public String toStorageString() {
        if (!quantity.isEmpty() && !name.isEmpty()) {
            return quantity + " | " + name;
        } else if (!name.isEmpty()) {
            return name;
        } else {
            return quantity;
        }
    }

    /**
     * Serializes a list of ingredients into a multi-line string.
     */
    public static String toStorageString(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Ingredient ing : ingredients) {
            if (ing == null) continue;
            String str = ing.toStorageString();
            if (!str.trim().isEmpty()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(str);
            }
        }
        return sb.toString();
    }

    private static final Pattern EXPLICIT_DELIMITER_PATTERN = Pattern.compile("^[•\\-*\\s]*(.*?)\\s*\\|\\s*(.*)$");

    // Regex matching numbers/fractions and common measurement units at the start of an ingredient string
    private static final Pattern QUANTITY_SPLIT_PATTERN = Pattern.compile(
        "^[•\\-*\\s]*" +
        "([0-9½⅓¼¾⅕⅙⅐⅛⅑⅒\\s/.,\\-]+" +
        "(?:g|grams?|kg|oz|ounces?|lbs?|pounds?|ml|l|liters?|cups?|tbsp|tablespoons?|tsp|teaspoons?|cloves?|pinches?|dashes?|slices?|cans?|pkgs?|packages?|heads?|bunches?|stalks?|sprigs?|pieces?|handfuls?|drops?|bottles?|bars?|sticks?|large|medium|small|ripe)?)" +
        "\\s+(.*)$",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * Parses a single raw ingredient line into an Ingredient object.
     */
    public static Ingredient parse(String rawLine) {
        if (rawLine == null || rawLine.trim().isEmpty()) {
            return new Ingredient("", "");
        }

        String line = rawLine.trim();

        // 1. Check if line contains explicit "|" delimiter (e.g., "200g | Fettuccine or Spaghetti")
        if (line.contains("|")) {
            Matcher m = EXPLICIT_DELIMITER_PATTERN.matcher(line);
            if (m.find()) {
                String q = m.group(1) != null ? m.group(1).trim() : "";
                String n = m.group(2) != null ? m.group(2).trim() : "";
                return new Ingredient(q, n);
            }
        }

        // 2. Clean leading bullets
        String cleaned = line.replaceAll("^[•\\-*\\s]+", "").trim();

        // 3. Try quantity pattern split
        Matcher m = QUANTITY_SPLIT_PATTERN.matcher(cleaned);
        if (m.find()) {
            String q = m.group(1) != null ? m.group(1).trim() : "";
            String n = m.group(2) != null ? m.group(2).trim() : "";
            if (!q.isEmpty() && !n.isEmpty()) {
                return new Ingredient(q, n);
            }
        }

        // 4. Fallback: entire line as ingredient name
        return new Ingredient("", cleaned);
    }

    /**
     * Parses a multiline ingredients string into a list of Ingredient objects.
     */
    public static List<Ingredient> parseList(String rawIngredients) {
        List<Ingredient> result = new ArrayList<>();
        if (rawIngredients == null || rawIngredients.trim().isEmpty()) {
            return result;
        }

        String[] lines = rawIngredients.split("\r?\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                Ingredient ing = parse(trimmed);
                if (!ing.getQuantity().isEmpty() || !ing.getName().isEmpty()) {
                    result.add(ing);
                }
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return Objects.equals(quantity, that.quantity) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, name);
    }
}
