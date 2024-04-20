package com.example.flextrain.ui.nutrition;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.flextrain.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NutritionFragment extends Fragment implements NutritionEntryAdapter.ModifyDeleteClickListener, OnChartValueSelectedListener {

    private EditText searchEditText;

    private Button searchButton;
    private Button submitToDbButton;
    private Button selectDateButton;
    private Button viewEntriesButton;
    private ListView resultList;

    private SpoonacularService spoonacularService;
    private ArrayAdapter<Ingredient> adapter;
    private List<Ingredient> ingredientsList;

    private DatabaseReference nutritionRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private PieChart pieChart;


    private static final String TAG = "NutritionFragment";
    private AlertDialog entriesDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nutrition, container, false);

        searchEditText = view.findViewById(R.id.search_edit_text);
        searchButton = view.findViewById(R.id.search_button);
        submitToDbButton = view.findViewById(R.id.submit_to_database_button);
        selectDateButton = view.findViewById(R.id.select_date_button);
        viewEntriesButton = view.findViewById(R.id.view_entries_button);
        resultList = view.findViewById(R.id.result_list);
        pieChart = view.findViewById(R.id.pieChart);


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://flextrain-6df5f-default-rtdb.europe-west1.firebasedatabase.app/");
        nutritionRef = database.getReference("nutrition");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spoonacular.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        spoonacularService = retrofit.create(SpoonacularService.class);

        ingredientsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, ingredientsList);
        resultList.setAdapter(adapter);

        resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Ingredient selectedIngredient = ingredientsList.get(position);
                searchEditText.setText(selectedIngredient.getName());
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchEditText.getText().toString();
                if (!query.isEmpty()) {
                    searchIngredients(query);
                } else {
                    Toast.makeText(getActivity(), "Please enter a search query", Toast.LENGTH_SHORT).show();
                }
            }
        });

        submitToDbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitToFirebase();
            }
        });

        //  OnClickListener for the select date button to show date picker dialog
        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        //  OnClickListener for the view entries button to show previous entries on a specific date
        viewEntriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectDateDialog();
            }
        });
        pieChart.setOnChartValueSelectedListener(this);

        return view;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        // Get the selected entry from the pie chart
        PieEntry entry = (PieEntry) e;

        // Show a small popup dialog with the total nutrients
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(entry.getLabel());
        builder.setMessage("Total " + entry.getLabel() + ": " + entry.getValue());
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    @Override
    public void onNothingSelected() {
        // This method is called when nothing is selected on the pie chart
    }

    private void searchIngredients(String query) {
        Call<IngredientSearchResponse> call = spoonacularService.searchIngredients("5ef865845a904e9a969ce7d171b81750", query);
        call.enqueue(new Callback<IngredientSearchResponse>() {
            @Override
            public void onResponse(Call<IngredientSearchResponse> call, Response<IngredientSearchResponse> response) {
                if (response.isSuccessful()) {
                    IngredientSearchResponse searchResponse = response.body();
                    if (searchResponse != null) {
                        List<Ingredient> results = searchResponse.getResults();
                        if (results != null && !results.isEmpty()) {
                            ingredientsList.clear();
                            ingredientsList.addAll(results);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getActivity(), "No results found", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), "Failed to search ingredients", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<IngredientSearchResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Failed to search ingredients: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitToFirebase() {
        String ingredientName = searchEditText.getText().toString();
        if (!ingredientName.isEmpty()) {
            EditText caloriesEditText = getView().findViewById(R.id.calories_edit_text);
            EditText proteinEditText = getView().findViewById(R.id.protein_edit_text);
            EditText fatEditText = getView().findViewById(R.id.fat_edit_text);
            EditText carbsEditText = getView().findViewById(R.id.carbs_edit_text);

            String calories = caloriesEditText.getText().toString();
            String protein = proteinEditText.getText().toString();
            String fat = fatEditText.getText().toString();
            String carbohydrates = carbsEditText.getText().toString();

            // Get the selected date from the button text
            String selectedDate = selectDateButton.getText().toString();

            // Check if the selected date is equal to "Select Date"
            if (!selectedDate.equals("Select Date")) {
                String userId = currentUser.getUid();
                NutritionEntry entry = new NutritionEntry(userId, ingredientName, calories, protein, fat, carbohydrates, selectedDate);

                nutritionRef.child(userId).push().setValue(entry)
                        .addOnSuccessListener(aVoid -> {
                            caloriesEditText.setText("");
                            proteinEditText.setText("");
                            fatEditText.setText("");
                            carbsEditText.setText("");
                            Toast.makeText(getActivity(), "Nutrition data submitted successfully", Toast.LENGTH_SHORT).show();

                            // Update total nutrients after adding the new entry
                            updateTotalNutrients();
                            updateTotalNutrientsAndPieChart();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to submit data to Firebase: " + e.getMessage());
                            Toast.makeText(getActivity(), "Failed to submit data to Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(getActivity(), "Please select a date", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "Please select an ingredient", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotalNutrients() {
        String selectedDate = selectDateButton.getText().toString();
        if (!selectedDate.equals("Select Date")) {
            String userId = currentUser.getUid();
            Query query = nutritionRef.child(userId).orderByChild("date").equalTo(selectedDate);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<NutritionEntry> entries = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        NutritionEntry entry = snapshot.getValue(NutritionEntry.class);
                        if (entry != null) {
                            entry.setId(snapshot.getKey());
                            entries.add(entry);
                        }
                    }

                    if (!entries.isEmpty()) {
                        // Calculate total nutrients
                        double totalCalories = 0;
                        double totalProtein = 0;
                        double totalFat = 0;
                        double totalCarbohydrates = 0;

                        for (NutritionEntry entry : entries) {
                            // Check for empty strings before parsing into double
                            if (!entry.getCalories().isEmpty()) {
                                totalCalories += Double.parseDouble(entry.getCalories());
                            }
                            if (!entry.getProtein().isEmpty()) {
                                totalProtein += Double.parseDouble(entry.getProtein());
                            }
                            if (!entry.getFat().isEmpty()) {
                                totalFat += Double.parseDouble(entry.getFat());
                            }
                            if (!entry.getCarbohydrates().isEmpty()) {
                                totalCarbohydrates += Double.parseDouble(entry.getCarbohydrates());
                            }
                        }

                        // Update the displayed total nutrients
                        displayTotalNutrients(totalCalories, totalProtein, totalFat, totalCarbohydrates);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Failed to read entries: " + databaseError.getMessage());
                }
            });
        }
    }

    private void showDatePickerDialog() {
        // Get current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Handle date selection
                        String selectedDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                        selectDateButton.setText(selectedDate);
                    }
                }, year, month, day);

        // Show date picker dialog
        datePickerDialog.show();
    }

    private void showSelectDateDialog() {
        // Create a DatePickerDialog for selecting the date to view entries
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext());
        datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String selectedDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                selectDateButton.setText(selectedDate);
                showEntriesForDate(selectedDate);
            }
        });
        datePickerDialog.show();
    }

    private void showEntriesForDate(String selectedDate) {
        // Retrieve entries from Firebase for the selected date
        String userId = currentUser.getUid();
        Query query = nutritionRef.child(userId).orderByChild("date").equalTo(selectedDate);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<NutritionEntry> entries = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    NutritionEntry entry = snapshot.getValue(NutritionEntry.class);
                    if (entry != null) {
                        entry.setId(snapshot.getKey());
                        entries.add(entry);
                    }
                }

                if (!entries.isEmpty()) {
                    // Show entries in a dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("Food Entries For : " + selectedDate);

                    //  ListView to display the entries
                    ListView listView = new ListView(requireContext());
                    builder.setView(listView);

                    // custom adapter to display the entries
                    NutritionEntryAdapter adapter = new NutritionEntryAdapter(requireContext(), entries);
                    adapter.setModifyDeleteClickListener(NutritionFragment.this); // Set the click listener
                    listView.setAdapter(adapter);

                    // close button to the dialog
                    builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    // Update the existing entries dialog instead of creating a new one
                    entriesDialog = builder.create();
                    entriesDialog.show();

                    // Calculate and display total nutrients
                    calculateTotalNutrients(entries);

                    updateTotalNutrientsAndPieChart();
                } else {
                    Toast.makeText(getActivity(), "No entries found for selected date/please select a date with entries", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read entries: " + databaseError.getMessage());
            }
        });
    }

    private void calculateTotalNutrients(List<NutritionEntry> entries) {
        double totalCalories = 0;
        double totalProtein = 0;
        double totalFat = 0;
        double totalCarbohydrates = 0;

        for (NutritionEntry entry : entries) {
            // Check for empty strings before parsing into double
            if (!entry.getCalories().isEmpty()) {
                totalCalories += Double.parseDouble(entry.getCalories());
            }
            if (!entry.getProtein().isEmpty()) {
                totalProtein += Double.parseDouble(entry.getProtein());
            }
            if (!entry.getFat().isEmpty()) {
                totalFat += Double.parseDouble(entry.getFat());
            }
            if (!entry.getCarbohydrates().isEmpty()) {
                totalCarbohydrates += Double.parseDouble(entry.getCarbohydrates());
            }
        }

        Log.d(TAG, "Total Calories: " + totalCalories);
        Log.d(TAG, "Total Protein: " + totalProtein);
        Log.d(TAG, "Total Fat: " + totalFat);
        Log.d(TAG, "Total Carbohydrates: " + totalCarbohydrates);

        // Call a method to display these totals
        displayTotalNutrients(totalCalories, totalProtein, totalFat, totalCarbohydrates);
    }


    private void displayTotalNutrients(double totalCalories, double totalProtein, double totalFat, double totalCarbohydrates) {
        TextView caloriesTextView = getView().findViewById(R.id.total_calories_text_view);
        TextView proteinTextView = getView().findViewById(R.id.total_protein_text_view);
        TextView fatTextView = getView().findViewById(R.id.total_fat_text_view);
        TextView carbsTextView = getView().findViewById(R.id.total_carbohydrates_text_view);

        // Set the calculated totals to the TextViews
        caloriesTextView.setText("Total Calories: " + totalCalories);
        proteinTextView.setText("Total Protein: " + totalProtein);
        fatTextView.setText("Total Fat: " + totalFat);
        carbsTextView.setText("Total Carbohydrates: " + totalCarbohydrates);
    }

    @Override
    public void onModifyClicked(NutritionEntry entry) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.modify_entry_dialog, null);

        EditText editTextCalories = dialogView.findViewById(R.id.editTextCalories);
        EditText editTextProtein = dialogView.findViewById(R.id.editTextProtein);
        EditText editTextFat = dialogView.findViewById(R.id.editTextFat);
        EditText editTextCarbohydrates = dialogView.findViewById(R.id.editTextCarbohydrates);

        // Set current values to edit texts
        editTextCalories.setText(entry.getCalories());
        editTextProtein.setText(entry.getProtein());
        editTextFat.setText(entry.getFat());
        editTextCarbohydrates.setText(entry.getCarbohydrates());

        builder.setView(dialogView)
                .setTitle("Modify Entry")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get updated values from edit texts
                        String calories = editTextCalories.getText().toString();
                        String protein = editTextProtein.getText().toString();
                        String fat = editTextFat.getText().toString();
                        String carbohydrates = editTextCarbohydrates.getText().toString();

                        // Update entry in Firebase database
                        updateEntryInFirebase(entry, calories, protein, fat, carbohydrates);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateEntryInFirebase(NutritionEntry entry, String calories, String protein, String fat, String carbohydrates) {
        String userId = currentUser.getUid();
        String entryId = entry.getId();

        entry.setCalories(calories.isEmpty() ? "0" : calories);
        entry.setProtein(protein.isEmpty() ? "0" : protein);
        entry.setFat(fat.isEmpty() ? "0" : fat);
        entry.setCarbohydrates(carbohydrates.isEmpty() ? "0" : carbohydrates);

        nutritionRef.child(userId).child(entryId).setValue(entry)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "Entry updated successfully", Toast.LENGTH_SHORT).show();
                        // Dismiss the existing entries dialog
                        if (entriesDialog != null && entriesDialog.isShowing()) {
                            entriesDialog.dismiss();
                        }
                        // Refresh the entries list by showing entries for the selected date again to reflect changes instantly
                        String selectedDate = selectDateButton.getText().toString();
                        if (!selectedDate.equals("Select Date")) {
                            showEntriesForDate(selectedDate);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to update entry: " + e.getMessage());
                        Toast.makeText(getActivity(), "Failed to update entry: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onDeleteClicked(NutritionEntry entry) {
        // Show a confirmation dialog and then delete the entry from the Firebase database
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to delete this entry?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteEntryFromFirebase(entry);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteEntryFromFirebase(NutritionEntry entry) {
        // Delete the entry from the Firebase database
        String userId = currentUser.getUid();
        String entryId = entry.getId();
        nutritionRef.child(userId).child(entryId).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "Entry deleted successfully", Toast.LENGTH_SHORT).show();
                        // Dismiss the existing entries dialog
                        if (entriesDialog != null && entriesDialog.isShowing()) {
                            entriesDialog.dismiss();
                        }
                        // Refresh the entries list by showing entries for the selected date again to reflect changes instantly
                        String selectedDate = selectDateButton.getText().toString();
                        if (!selectedDate.equals("Select Date")) {
                            showEntriesForDate(selectedDate);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to delete entry: " + e.getMessage());
                        Toast.makeText(getActivity(), "Failed to delete entry: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void updateTotalNutrientsAndPieChart() {
        String selectedDate = selectDateButton.getText().toString();
        if (!selectedDate.equals("Select Date")) {
            String userId = currentUser.getUid();
            Query query = nutritionRef.child(userId).orderByChild("date").equalTo(selectedDate);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<NutritionEntry> entries = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        NutritionEntry entry = snapshot.getValue(NutritionEntry.class);
                        if (entry != null) {
                            entry.setId(snapshot.getKey());
                            entries.add(entry);
                        }
                    }

                    if (!entries.isEmpty()) {
                        // Calculate total nutrients
                        double totalCalories = 0;
                        double totalProtein = 0;
                        double totalFat = 0;
                        double totalCarbohydrates = 0;

                        for (NutritionEntry entry : entries) {
                            // Check for empty strings before parsing into double
                            if (!entry.getCalories().isEmpty()) {
                                totalCalories += Double.parseDouble(entry.getCalories());
                            }
                            if (!entry.getProtein().isEmpty()) {
                                totalProtein += Double.parseDouble(entry.getProtein());
                            }
                            if (!entry.getFat().isEmpty()) {
                                totalFat += Double.parseDouble(entry.getFat());
                            }
                            if (!entry.getCarbohydrates().isEmpty()) {
                                totalCarbohydrates += Double.parseDouble(entry.getCarbohydrates());
                            }
                        }

                        // Update the displayed total nutrients
                        displayTotalNutrients(totalCalories, totalProtein, totalFat, totalCarbohydrates);

                        // Update the pie chart
                        updatePieChart(totalCalories, totalProtein, totalFat, totalCarbohydrates);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Failed to read entries: " + databaseError.getMessage());
                }
            });
        }
    }

    private void updatePieChart(double totalCalories, double totalProtein, double totalFat, double totalCarbohydrates) {
        // data entries for the pie chart
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) totalCalories, "Calories"));
        entries.add(new PieEntry((float) totalProtein, "Protein"));
        entries.add(new PieEntry((float) totalFat, "Fat"));
        entries.add(new PieEntry((float) totalCarbohydrates, "Carbohydrates"));

        // Create a dataset to hold the data
        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setColors(Color.rgb(255, 204, 51), Color.rgb(51, 204, 204), Color.rgb(255, 102, 102), Color.rgb(102, 204, 255));

        PieData data = new PieData(dataSet);

        pieChart.setData(data);

        // Customize pie chart attributes
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setCenterText("Nutritional Info");
        pieChart.setCenterTextSize(18f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }


}
