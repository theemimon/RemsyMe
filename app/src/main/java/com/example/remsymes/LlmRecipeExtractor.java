package com.example.remsymes;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LlmRecipeExtractor {

    public static Recipe extractRecipeFromText(String rawText, String sourceUrl) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return new Recipe(0, "Imported Recipe", "Uncategorized", "", "", 0.0, false, "recipe_sample_image", "", "", sourceUrl, "");
        }

        String text = rawText.trim();
        String[] lines = text.split("\r?\n");

        String name = "";
        String prepTime = "";
        String portions = "";
        StringBuilder ingredients = new StringBuilder();
        StringBuilder instructions = new StringBuilder();

        // 1. Title Extraction: Search first 15 lines for the best candidate title
        for (int i = 0; i < Math.min(lines.length, 15); i++) {
            String l = lines[i].trim();
            if (l.isEmpty() || l.toLowerCase().startsWith("http") || isCommonSiteBanner(l)) continue;
            if (l.length() >= 3 && l.length() <= 70 && name.isEmpty()) {
                name = cleanTitle(l);
                break;
            }
        }

        if (name.isEmpty()) {
            name = extractTitleFromUrl(sourceUrl);
        }

        // 2. Extract Cooking Time & Servings
        Pattern timePattern = Pattern.compile("(?i)(prep|cook|total)?\\s*time\\s*:?\\s*(\\d+\\s*(minutes?|mins?|hours?|hrs?|h|m))");
        Pattern yieldPattern = Pattern.compile("(?i)(servings?|yield|serves|portions?)\\s*:?\\s*(\\d+\\s*(servings?|portions?|people)?)");

        for (String line : lines) {
            String l = line.trim();
            if (prepTime.isEmpty()) {
                Matcher tm = timePattern.matcher(l);
                if (tm.find()) {
                    prepTime = tm.group(2);
                }
            }
            if (portions.isEmpty()) {
                Matcher ym = yieldPattern.matcher(l);
                if (ym.find()) {
                    portions = ym.group(2) + " Servings";
                }
            }
        }

        // 3. Extract Ingredients & Instructions Sections via Semantic Markers
        List<String> ingredientLines = new ArrayList<>();
        List<String> instructionLines = new ArrayList<>();

        boolean insideIngredients = false;
        boolean insideInstructions = false;

        for (String line : lines) {
            String l = line.trim();
            if (l.isEmpty()) continue;
            String lower = l.toLowerCase();

            if (isIngredientHeader(lower)) {
                insideIngredients = true;
                insideInstructions = false;
                continue;
            } else if (isInstructionHeader(lower)) {
                insideInstructions = true;
                insideIngredients = false;
                continue;
            }

            if (insideIngredients) {
                if (isHeader(lower) && !isIngredientHeader(lower)) {
                    insideIngredients = false;
                } else if (!isCommonSiteBanner(l)) {
                    ingredientLines.add(cleanIngredient(l));
                }
            } else if (insideInstructions) {
                if (isHeader(lower) && !isInstructionHeader(lower)) {
                    insideInstructions = false;
                } else if (!isCommonSiteBanner(l)) {
                    instructionLines.add(l);
                }
            } else {
                // Heuristic Classifier when no explicit section marker was hit
                if (isIngredientLine(l)) {
                    ingredientLines.add(cleanIngredient(l));
                } else if (isInstructionLine(l)) {
                    instructionLines.add(l);
                }
            }
        }

        // Build Ingredients text
        for (String ing : ingredientLines) {
            if (ingredients.length() > 0) ingredients.append("\n");
            ingredients.append("• ").append(ing);
        }

        // Build Instructions text
        for (int i = 0; i < instructionLines.size(); i++) {
            if (instructions.length() > 0) instructions.append("\n");
            String step = instructionLines.get(i);
            if (!step.matches("^\\d+[.:)].*")) {
                instructions.append(i + 1).append(". ").append(step);
            } else {
                instructions.append(step);
            }
        }

        // Fallback: If no structured instructions, put full raw text in instructions
        if (instructions.length() == 0) {
            instructions.append(text);
        }

        return new Recipe(0, name, "Uncategorized", prepTime, portions, 0.0, false, "recipe_sample_image", ingredients.toString(), instructions.toString(), sourceUrl, text);
    }

    private static boolean isIngredientHeader(String lower) {
        return lower.contains("ingredient") || lower.contains("what you'll need") || lower.contains("what you need");
    }

    private static boolean isInstructionHeader(String lower) {
        return lower.contains("instruction") || lower.contains("direction") || lower.contains("method")
                || lower.contains("preparation") || lower.contains("how to make") || lower.contains("step");
    }

    private static boolean isHeader(String lower) {
        return isIngredientHeader(lower) || isInstructionHeader(lower) || lower.contains("nutrition") || lower.contains("reviews") || lower.contains("comments");
    }

    private static final Pattern WEIGHT_UNIT_PATTERN = Pattern.compile("\\d\\s*(g|oz)\\b", Pattern.CASE_INSENSITIVE);

    private static boolean isIngredientLine(String l) {
        String lower = l.toLowerCase();
        return l.matches("^[0-9½⅓¼¾•\\-*].*") || lower.contains("cup") || lower.contains("tbsp")
                || lower.contains("tsp") || lower.contains("grams") || WEIGHT_UNIT_PATTERN.matcher(l).find()
                || lower.contains("cloves") || lower.contains("pinch") || lower.contains("tablespoon") || lower.contains("teaspoon");
    }

    private static boolean isInstructionLine(String l) {
        return l.matches("^(Step|step)?\\s*\\d+[.:)].*") || (l.length() > 45 && (l.endsWith(".") || l.endsWith("!")));
    }

    private static boolean isCommonSiteBanner(String l) {
        String lower = l.toLowerCase();
        return lower.contains("cookie") || lower.contains("privacy policy") || lower.contains("terms of service")
                || lower.contains("subscribe") || lower.contains("all rights reserved") || lower.contains("sign in") || lower.contains("log in");
    }

    private static String cleanTitle(String l) {
        return l.replaceAll("(?i)\\s*(\\||-|•).*$", "").trim();
    }

    private static String cleanIngredient(String l) {
        return l.replaceAll("^[0-9•\\-*\\s]+", "").trim();
    }

    private static String extractTitleFromUrl(String url) {
        if (url == null || url.isEmpty()) return "Imported Recipe";
        try {
            String path = url.substring(url.lastIndexOf('/') + 1);
            if (path.contains("?")) path = path.substring(0, path.indexOf('?'));
            if (path.contains(".")) path = path.substring(0, path.indexOf('.'));
            path = path.replace("-", " ").replace("_", " ").trim();
            if (!path.isEmpty()) {
                String[] words = path.split("\\s+");
                StringBuilder sb = new StringBuilder();
                for (String w : words) {
                    if (!w.isEmpty()) {
                        sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase()).append(" ");
                    }
                }
                return sb.toString().trim();
            }
        } catch (Exception ignored) {}
        return "Imported Recipe";
    }
}