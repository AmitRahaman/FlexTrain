package com.example.flextrain.ui.nutrition;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class Ingredient {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("image")
    private String image;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }
}
