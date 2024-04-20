package com.example.flextrain.ui.nutrition;

public class NutritionEntry {

    private String id;
    private String ingredientName;
    private String calories;
    private String protein;
    private String fat;
    private String carbohydrates;
    private String date;

    public NutritionEntry() {
        // Default constructor required for Firebase
    }

    public NutritionEntry(String id, String ingredientName, String calories, String protein, String fat, String carbohydrates, String date) {
        this.id = id;
        this.ingredientName = ingredientName;
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carbohydrates = carbohydrates;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public String getCalories() {
        return calories;
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }

    public String getProtein() {
        return protein;
    }

    public void setProtein(String protein) {
        this.protein = protein;
    }

    public String getFat() {
        return fat;
    }

    public void setFat(String fat) {
        this.fat = fat;
    }

    public String getCarbohydrates() {
        return carbohydrates;
    }

    public void setCarbohydrates(String carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Ingredient: " + ingredientName + "\n" +
                "Calories: " + calories + "\n" +
                "Protein: " + protein + "\n" +
                "Fat: " + fat + "\n" +
                "Carbohydrates: " + carbohydrates + "\n" +
                "Date: " + date + "\n\n";
    }

}
