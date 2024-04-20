package com.example.flextrain.ui.dashboard;


public class WeightEntry {
    private String date;
    private double weight;
    private double bmi;
    private int caloriesEaten;


    //  default constructor
    public WeightEntry() {
        // Default constructor required for Firebase
    }

    public WeightEntry(String date, double weight, double bmi, int caloriesEaten) {
        this.date = date;
        this.weight = weight;
        this.bmi = bmi;
        this.caloriesEaten = caloriesEaten;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getBmi() {
        return bmi;
    }

    public void setBmi(double bmi) {
        this.bmi = bmi;
    }

    public int getCaloriesEaten() {
        return caloriesEaten;
    }

    public void setCaloriesEaten(int caloriesEaten) {
        this.caloriesEaten = caloriesEaten;
    }
}
