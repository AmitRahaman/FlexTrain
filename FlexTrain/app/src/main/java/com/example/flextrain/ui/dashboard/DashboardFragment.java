package com.example.flextrain.ui.dashboard;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.flextrain.R;
import com.example.flextrain.databinding.FragmentDashboardBinding;
import com.example.flextrain.ui.dashboard.WeightEntry;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DatabaseReference weightRef;
    private EditText editTextWeight, editTextBMI, editTextCaloriesEaten;
    private Button buttonSubmit, buttonData;
    private TextView textViewCurrentData;
    private TextView textViewPastData;
    private LineChart weightLineChart, bmiLineChart, caloriesLineChart;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        editTextWeight = root.findViewById(R.id.editTextWeight);
        editTextBMI = root.findViewById(R.id.editTextBMI);
        editTextCaloriesEaten = root.findViewById(R.id.editTextCaloriesEaten);
        buttonSubmit = root.findViewById(R.id.buttonSubmit);
        buttonData = root.findViewById(R.id.buttonData);
        weightLineChart = root.findViewById(R.id.weightLineChart);
        bmiLineChart = root.findViewById(R.id.bmiLineChart);
        caloriesLineChart = root.findViewById(R.id.caloriesLineChart);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            weightRef = FirebaseDatabase.getInstance("https://flextrain-6df5f-default-rtdb.europe-west1.firebasedatabase.app/").getReference("weightEntries").child(userId);

            buttonSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveWeightEntry();
                }
            });

            buttonData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDataPopup();
                }
            });

            // Call the method to fetch weight entries
            fetchWeightEntries();
        }

        return root;
    }

    private void saveWeightEntry() {
        String weightStr = editTextWeight.getText().toString().trim();
        String bmiStr = editTextBMI.getText().toString().trim();
        String caloriesStr = editTextCaloriesEaten.getText().toString().trim();

        // Check if any of the fields are empty
        if (weightStr.isEmpty() || bmiStr.isEmpty() || caloriesStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate input for weight
        double weight;
        try {
            weight = Double.parseDouble(weightStr);
        } catch (NumberFormatException e) {
            // Handle invalid input for weight
            Toast.makeText(getContext(), "Please enter a valid weight", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate input for BMI
        double bmi;
        try {
            bmi = Double.parseDouble(bmiStr);
        } catch (NumberFormatException e) {
            // Handle invalid input for BMI
            Toast.makeText(getContext(), "Please enter a valid BMI", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate input for calories eaten
        int caloriesEaten;
        try {
            caloriesEaten = Integer.parseInt(caloriesStr);
        } catch (NumberFormatException e) {
            // Handle invalid input for calories eaten
            Toast.makeText(getContext(), "Please enter valid calories eaten", Toast.LENGTH_SHORT).show();
            return;
        }

        // If all input is valid, proceed to save the weight entry
        String date = "2024-03-14";
        WeightEntry weightEntry = new WeightEntry(date, weight, bmi, caloriesEaten);

        String key = weightRef.push().getKey();
        if (key != null) {
            weightRef.child(key).setValue(weightEntry);
            Toast.makeText(getContext(), "Weight entry saved successfully", Toast.LENGTH_SHORT).show();

            // Clear input fields after submission
            editTextWeight.setText("");
            editTextBMI.setText("");
            editTextCaloriesEaten.setText("");
        } else {
            Toast.makeText(getContext(), "Failed to save weight entry", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchWeightEntries() {
        weightRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<WeightEntry> weightEntries = new ArrayList<>();

                // Get the latest date for comparison
                String latestDate = "0000-00-00";
                for (DataSnapshot entrySnapshot : dataSnapshot.getChildren()) {
                    WeightEntry entry = entrySnapshot.getValue(WeightEntry.class);
                    weightEntries.add(entry);
                }

                // Plot weight entries on LineChart
                plotWeightEntries(weightEntries);

                // Plot BMI entries on LineChart
                plotBMIEntries(weightEntries);

                // Plot calories eaten entries on LineChart
                plotCaloriesEntries(weightEntries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void plotWeightEntries(ArrayList<WeightEntry> weightEntries) {
        ArrayList<Entry> entries = new ArrayList<>();

        // Iterate through weight entries and add them to the Entry list
        for (int i = 0; i < weightEntries.size(); i++) {
            WeightEntry entry = weightEntries.get(i);
            entries.add(new Entry(i, (float) entry.getWeight()));
        }

        // Create a dataset to hold the data
        LineDataSet dataSet = new LineDataSet(entries, "Weight Entries");
        customizeLineDataSet(dataSet, Color.BLUE);

        // Create a LineData object from the LineDataSet
        LineData lineData = new LineData(dataSet);

        // Apply LineData to the LineChart
        customizeLineChart(weightLineChart, lineData);
    }

    private void plotBMIEntries(ArrayList<WeightEntry> weightEntries) {
        ArrayList<Entry> entries = new ArrayList<>();

        // Iterate through weight entries and add BMI entries to the Entry list
        for (int i = 0; i < weightEntries.size(); i++) {
            WeightEntry entry = weightEntries.get(i);
            entries.add(new Entry(i, (float) entry.getBmi()));
        }

        // Create a dataset to hold the BMI data
        LineDataSet dataSet = new LineDataSet(entries, "BMI Entries");
        customizeLineDataSet(dataSet, Color.GREEN);

        // Create a LineData object from the LineDataSet
        LineData lineData = new LineData(dataSet);

        // Apply LineData to the BMI LineChart
        customizeLineChart(bmiLineChart, lineData);
    }

    private void plotCaloriesEntries(ArrayList<WeightEntry> weightEntries) {
        ArrayList<Entry> entries = new ArrayList<>();

        // Iterate through weight entries and add calories eaten entries to the Entry list
        for (int i = 0; i < weightEntries.size(); i++) {
            WeightEntry entry = weightEntries.get(i);
            entries.add(new Entry(i, entry.getCaloriesEaten()));
        }

        // Create a dataset to hold the calories eaten data
        LineDataSet dataSet = new LineDataSet(entries, "Calories Eaten Entries");
        customizeLineDataSet(dataSet, Color.RED);

        // Create a LineData object from the LineDataSet
        LineData lineData = new LineData(dataSet);

        // Apply LineData to the calories eaten LineChart
        customizeLineChart(caloriesLineChart, lineData);
    }

    private void customizeLineDataSet(LineDataSet dataSet, int color) {
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
    }

    private void customizeLineChart(LineChart chart, LineData lineData) {
        // Set description text
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);

        // Customize appearance of X axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);

        // Customize appearance of Y axis
        YAxis yAxisLeft = chart.getAxisLeft();
        yAxisLeft.setTextColor(Color.BLACK);
        YAxis yAxisRight = chart.getAxisRight();
        yAxisRight.setEnabled(false);

        // Set background color
        chart.setBackgroundColor(Color.WHITE);

        // Add grid lines
        chart.setDrawGridBackground(true);
        chart.setGridBackgroundColor(Color.WHITE);
        chart.setDrawGridBackground(true);

        // Apply LineData to the LineChart
        chart.setData(lineData);

        // Refresh chart
        chart.invalidate();
    }

    private void showDataPopup() {
        weightRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Your Entries");

                // Create a layout inflater to inflate custom layout for each entry
                LayoutInflater inflater = requireActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.delete_entry_dialog, null);
                LinearLayout entryLayout = dialogView.findViewById(R.id.entryLayout);

                for (DataSnapshot entrySnapshot : dataSnapshot.getChildren()) {
                    WeightEntry entry = entrySnapshot.getValue(WeightEntry.class);
                    if (entry != null) {
                        // Inflate a layout for each entry
                        View entryView = inflater.inflate(R.layout.entry_item, null);
                        TextView textViewEntry = entryView.findViewById(R.id.textViewEntry);
                        Button deleteButton = entryView.findViewById(R.id.deleteButton);
                        Button updateButton = entryView.findViewById(R.id.updateButton);

                        // Display entry information
                        String entryText = "Date: " + entry.getDate() + "\n" +
                                "Weight: " + entry.getWeight() + "\n" +
                                "BMI: " + entry.getBmi() + "\n" +
                                "Calories Eaten: " + entry.getCaloriesEaten();
                        textViewEntry.setText(entryText);


                        deleteButton.setOnClickListener(v -> {
                            // Display confirmation dialog before deleting
                            AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(getContext());
                            confirmationDialog.setTitle("Delete Entry");
                            confirmationDialog.setMessage("Are you sure you want to delete this entry?");
                            confirmationDialog.setPositiveButton("Yes", (dialog, which) -> {
                                // Delete the entry from Firebase
                                entrySnapshot.getRef().removeValue();
                                Toast.makeText(getContext(), "Entry deleted", Toast.LENGTH_SHORT).show();
                                entryLayout.removeView(entryView);
                                dialog.dismiss();
                            });
                            confirmationDialog.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
                            confirmationDialog.show();
                        });

                        // click listener to update button
                        updateButton.setOnClickListener(v -> {
                            AlertDialog.Builder updateDialogBuilder = new AlertDialog.Builder(getContext());
                            updateDialogBuilder.setTitle("Update Entry");

                            // Inflate the layout for the update dialog
                            View updateDialogView = inflater.inflate(R.layout.update_entry_dialog, null);
                            EditText weightEditText = updateDialogView.findViewById(R.id.editTextWeightUpdate);
                            EditText bmiEditText = updateDialogView.findViewById(R.id.editTextBMIUpdate);
                            EditText caloriesEditText = updateDialogView.findViewById(R.id.editTextCaloriesEatenUpdate);


                            weightEditText.setText(String.valueOf(entry.getWeight()));
                            bmiEditText.setText(String.valueOf(entry.getBmi()));
                            caloriesEditText.setText(String.valueOf(entry.getCaloriesEaten()));

                            updateDialogBuilder.setView(updateDialogView);

                            // Set up the buttons for the update dialog
                            updateDialogBuilder.setPositiveButton("Update", (dialog, which) -> {
                                // Get updated values from EditText fields
                                String updatedWeightStr = weightEditText.getText().toString().trim();
                                String updatedBmiStr = bmiEditText.getText().toString().trim();
                                String updatedCaloriesStr = caloriesEditText.getText().toString().trim();

                                // Validate input for weight
                                double updatedWeight;
                                try {
                                    updatedWeight = Double.parseDouble(updatedWeightStr);
                                } catch (NumberFormatException e) {
                                    Toast.makeText(getContext(), "Please enter a valid weight", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Validate input for BMI
                                double updatedBmi;
                                try {
                                    updatedBmi = Double.parseDouble(updatedBmiStr);
                                } catch (NumberFormatException e) {
                                    Toast.makeText(getContext(), "Please enter a valid BMI", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Validate input for calories eaten
                                int updatedCaloriesEaten;
                                try {
                                    updatedCaloriesEaten = Integer.parseInt(updatedCaloriesStr);
                                } catch (NumberFormatException e) {
                                    Toast.makeText(getContext(), "Please enter valid calories eaten", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Update the entry in Firebase with the new values
                                entrySnapshot.getRef().child("weight").setValue(updatedWeight);
                                entrySnapshot.getRef().child("bmi").setValue(updatedBmi);
                                entrySnapshot.getRef().child("caloriesEaten").setValue(updatedCaloriesEaten);

                                // Update the entry view with the new data
                                String updatedEntryText = "Date: " + entry.getDate() + "\n" +
                                        "Weight: " + updatedWeight + "\n" +
                                        "BMI: " + updatedBmi + "\n" +
                                        "Calories Eaten: " + updatedCaloriesEaten;
                                textViewEntry.setText(updatedEntryText);

                                Toast.makeText(getContext(), "Entry updated successfully", Toast.LENGTH_SHORT).show();
                            });

                            updateDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                            updateDialogBuilder.create().show();
                        });

                        // Add the entry view to the dialog layout
                        entryLayout.addView(entryView);
                        // Apply animation to the entry view
                        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.scale_up);
                        entryView.startAnimation(animation);
                    }
                }

                builder.setView(dialogView);
                builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                builder.create().show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
