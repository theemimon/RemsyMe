package me.remsy;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecipeParser {

    public static Recipe parseSharedText(String sharedText) {
        if (sharedText == null || sharedText.trim().isEmpty()) {
            return new Recipe(0, "Imported Recipe", "Uncategorized", "", "", 0.0, false, "recipe_sample_image", "", "");
        }

        String rawText = sharedText.trim();
        String title = "";
        String time = "";
        String portions = "";
        StringBuilder ingredients = new StringBuilder();
        StringBuilder instructions = new StringBuilder();
        List<String> unassignedLines = new ArrayList<>();

        String[] lines = rawText.split("\r?\n");

        boolean inIngredients = false;
        boolean inInstructions = false;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            String lower = line.toLowerCase();

            // Skip plain URL lines for title
            if (lower.startsWith("http://") || lower.startsWith("https://")) {
                if (title.isEmpty()) {
                    title = extractTitleFromUrl(line);
                }
                continue;
            }

            // Extract title from first clean non-header line
            if (title.isEmpty() && !isHeader(lower) && line.length() < 80) {
                title = cleanTitle(line);
                continue;
            }

            // Detect headers
            if (isIngredientHeader(lower)) {
                inIngredients = true;
                inInstructions = false;
                continue;
            } else if (isInstructionHeader(lower)) {
                inInstructions = true;
                inIngredients = false;
                continue;
            }

            // Extract time
            if (time.isEmpty() && (lower.contains("prep") || lower.contains("cook") || lower.contains("time") || lower.contains("min"))) {
                String extractedTime = extractPattern(line, "(\\d+\\s*(minutes?|mins?|hours?|hrs?|h|m))");
                if (!extractedTime.isEmpty()) {
                    time = extractedTime;
                } else if (lower.contains("time:")) {
                    time = line;
                }
            }

            // Extract portions / servings
            if (portions.isEmpty() && (lower.contains("serv") || lower.contains("yield") || lower.contains("portion"))) {
                String extractedServings = extractPattern(line, "(\\d+\\s*(servings?|portions?|people|serves)?)");
                if (!extractedServings.isEmpty()) {
                    portions = extractedServings + " Servings";
                } else {
                    portions = line;
                }
            }

            // Categorize text
            if (inIngredients) {
                if (ingredients.length() > 0) ingredients.append("\n");
                ingredients.append("• ").append(cleanBullet(line));
            } else if (inInstructions) {
                if (instructions.length() > 0) instructions.append("\n");
                instructions.append(line);
            } else {
                if (isIngredientLine(line)) {
                    if (ingredients.length() > 0) ingredients.append("\n");
                    ingredients.append("• ").append(cleanBullet(line));
                } else if (isInstructionLine(line)) {
                    if (instructions.length() > 0) instructions.append("\n");
                    instructions.append(line);
                } else {
                    unassignedLines.add(line);
                }
            }
        }

        // If no explicit instructions header was found, append unassigned lines to instructions
        if (!unassignedLines.isEmpty()) {
            for (String unassigned : unassignedLines) {
                if (instructions.length() > 0) instructions.append("\n");
                instructions.append(unassigned);
            }
        }

        if (title.isEmpty()) {
            title = "Imported Recipe";
        }

        return new Recipe(0, title, "Uncategorized", time, portions, 0.0, false, "recipe_sample_image", ingredients.toString(), instructions.toString());
    }

    private static boolean isHeader(String lower) {
        return isIngredientHeader(lower) || isInstructionHeader(lower);
    }

    private static boolean isIngredientHeader(String lower) {
        return lower.contains("ingredient") || lower.contains("what you need") || lower.contains("shopping list");
    }

    private static boolean isInstructionHeader(String lower) {
        return lower.contains("instruction") || lower.contains("direction") || lower.contains("method")
                || lower.contains("step") || lower.contains("preparation") || lower.contains("how to make");
    }

    private static final Pattern WEIGHT_UNIT_PATTERN = Pattern.compile("\\d\\s*(g|oz)\\b", Pattern.CASE_INSENSITIVE);

    private static boolean isIngredientLine(String line) {
        String lower = line.toLowerCase();
        return line.matches("^[0-9½⅓¼¾•\\-*].*") || lower.contains("cup") || lower.contains("tbsp")
                || lower.contains("tsp") || lower.contains("grams") || WEIGHT_UNIT_PATTERN.matcher(line).find()
                || lower.contains("pinch") || lower.contains("cloves") || lower.contains("diced");
    }

    private static boolean isInstructionLine(String line) {
        return line.matches("^(Step|step)?\\s*\\d+[.:)].*") || line.length() > 40;
    }

    private static String extractTitleFromUrl(String url) {
        try {
            String path = url.substring(url.lastIndexOf('/') + 1);
            if (path.contains("?")) {
                path = path.substring(0, path.indexOf('?'));
            }
            if (path.contains(".")) {
                path = path.substring(0, path.indexOf('.'));
            }
            path = path.replace("-", " ").replace("_", " ").trim();
            if (!path.isEmpty()) {
                return capitalizeWords(path);
            }
        } catch (Exception ignored) {
        }
        return "Imported Recipe";
    }

    private static String cleanTitle(String line) {
        String cleaned = line.replace("Check out this recipe:", "")
                .replace("Check out", "")
                .replace("Sharing:", "")
                .trim();
        return capitalizeWords(cleaned);
    }

    private static String cleanBullet(String line) {
        return line.replaceAll("^[0-9•\\-*\\s]+", "").trim();
    }

    private static String extractPattern(String text, String regex) {
        try {
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(0);
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private static String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                        .append(w.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return sb.toString().trim();
    }
}