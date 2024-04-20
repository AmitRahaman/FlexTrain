package com.example.flextrain.workoutdatabase;

public class Workout {
    private String description;
    private String name;
    private String userId; // ID of the user who created the workout
    private String category;
    private String imageUrl;

    private String youtubeUrl;

    private String expertiseLevel; // New field for expertise level

    // Constructor
    public Workout() {
        // Default constructor required for Firebase
    }

    public Workout(String name, String description, String userId,String category, String imageUrl, String youtubeUrl) {
        this.name = name;
        this.description = description;
        this.userId = userId;
        this.category = category;
        this.imageUrl = imageUrl;
        this.youtubeUrl = youtubeUrl;


    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCategory() { return category;}

    public void setCategory(String category) { this.category = category;}

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getExpertiseLevel() {
        return expertiseLevel;
    }

    public void setExpertiseLevel(String expertiseLevel) {
        this.expertiseLevel = expertiseLevel;
    }
}
