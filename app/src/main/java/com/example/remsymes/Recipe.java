package com.example.remsymes;

public class Recipe {
    private long id;
    private String name;
    private String category;
    private String prepTime;
    private String portions;
    private double rating;
    private boolean isFavorite;
    private String imageUri;
    private String ingredients;
    private String instructions;
    private String sourceLink;
    private String scrapedText;

    public Recipe() {
    }

    public Recipe(long id, String name, String category, String prepTime, String portions,
                  double rating, boolean isFavorite, String imageUri, String ingredients, String instructions) {
        this(id, name, category, prepTime, portions, rating, isFavorite, imageUri, ingredients, instructions, "", "");
    }

    public Recipe(long id, String name, String category, String prepTime, String portions,
                  double rating, boolean isFavorite, String imageUri, String ingredients, String instructions,
                  String sourceLink, String scrapedText) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.prepTime = prepTime;
        this.portions = portions;
        this.rating = rating;
        this.isFavorite = isFavorite;
        this.imageUri = imageUri;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.sourceLink = sourceLink;
        this.scrapedText = scrapedText;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(String prepTime) {
        this.prepTime = prepTime;
    }

    public String getPortions() {
        return portions;
    }

    public void setPortions(String portions) {
        this.portions = portions;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getSourceLink() {
        return sourceLink;
    }

    public void setSourceLink(String sourceLink) {
        this.sourceLink = sourceLink;
    }

    public String getScrapedText() {
        return scrapedText;
    }

    public void setScrapedText(String scrapedText) {
        this.scrapedText = scrapedText;
    }
}