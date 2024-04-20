package com.example.flextrain.ui.nutrition;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class IngredientSearchResponse {

    @SerializedName("results")
    private List<Ingredient> results;

    public List<Ingredient> getResults() {
        return results;
    }
}
