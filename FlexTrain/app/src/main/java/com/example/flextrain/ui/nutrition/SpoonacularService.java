package com.example.flextrain.ui.nutrition;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SpoonacularService {

    @GET("/food/ingredients/search")
    Call<IngredientSearchResponse> searchIngredients(
            @Query("apiKey") String apiKey,
            @Query("query") String query
    );

}
