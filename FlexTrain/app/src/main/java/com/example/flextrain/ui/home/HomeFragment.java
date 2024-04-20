package com.example.flextrain.ui.home;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flextrain.R;
import com.example.flextrain.databinding.FragmentHomeBinding;
import com.example.flextrain.ui.home.WorkoutAdapter;
import com.example.flextrain.workoutdatabase.Workout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private WorkoutAdapter workoutAdapter;
    private Spinner categorySpinner;
    private LinearLayout categoryButtonsLayout;
    private List<Workout> workouts = new ArrayList<>();
    private static final int PICK_IMAGE_REQUEST = 1;
    private String imageUrl;
    private EditText searchEditText;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        workoutAdapter = new WorkoutAdapter();
        recyclerView.setAdapter(workoutAdapter);

        databaseReference = FirebaseDatabase.getInstance("https://flextrain-6df5f-default-rtdb.europe-west1.firebasedatabase.app/").getReference("workouts").child("public");

        categorySpinner = binding.categorySpinner;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        Spinner expertiseLevelSpinner = root.findViewById(R.id.expertiseLevelSpinner);
        ArrayAdapter<CharSequence> expertiseAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.expertise_levels_array, android.R.layout.simple_spinner_item);
        expertiseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expertiseLevelSpinner.setAdapter(expertiseAdapter);

        categoryButtonsLayout = root.findViewById(R.id.categoryButtonsLayout);
        addCategoryButtons();

        searchEditText = binding.searchEditText;
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterWorkouts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        final TextView textView = binding.textHome;
        final EditText nameEditText = binding.workoutNameEditText;
        final EditText descriptionEditText = binding.workoutDescriptionEditText;
        final EditText youtubeUrlEditText = binding.youtubeUrlEditText;
        Button addWorkoutButton = binding.addWorkoutButton;

        // Check if the current user is authorized to add workouts
        if (!isUserAuthorized()) {
            // If not authorized, hide the add workout fields
            nameEditText.setVisibility(View.GONE);
            descriptionEditText.setVisibility(View.GONE);
            addWorkoutButton.setVisibility(View.GONE);
            categorySpinner.setVisibility(View.GONE);
            youtubeUrlEditText.setVisibility(View.GONE);
            expertiseLevelSpinner.setVisibility(View.GONE);
        }


        addWorkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString().trim();
                String description = descriptionEditText.getText().toString().trim();
                String category = categorySpinner.getSelectedItem().toString();
                String userId = getCurrentUserId();
                String youtubeUrl = youtubeUrlEditText.getText().toString().trim();
                String expertiseLevel = expertiseLevelSpinner.getSelectedItem().toString();

                if (!userId.equals("w8c3y7URM2RssktwjZc3rETTwKA2")) {
                    Toast.makeText(getContext(), "You are not authorized to add workouts", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (name.isEmpty() || description.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                String workoutId = databaseReference.push().getKey();
                Workout workout = new Workout(name, description, userId, "DefaultCategory", imageUrl, youtubeUrl);
                workout.setCategory(category);
                workout.setExpertiseLevel(expertiseLevel);

                if (workoutId != null) {
                    databaseReference.child(workoutId).setValue(workout);
                }

                nameEditText.setText("");
                descriptionEditText.setText("");
                youtubeUrlEditText.setText("");

                Toast.makeText(getContext(), "Workout added successfully", Toast.LENGTH_SHORT).show();
            }
        });

        // Verify Firebase database connection
        Log.d(TAG, "Firebase database connection established.");

        // Retrieve and display workouts from the database
        fetchWorkouts();

        return root;
    }


    private boolean isUserAuthorized() {
        // Check if the current user is authorized to add workouts
        String userId = getCurrentUserId();
        return userId != null && userId.equals("w8c3y7URM2RssktwjZc3rETTwKA2");
    }

    private void fetchWorkouts() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                workouts.clear(); // Clear the list before adding new data
                for (DataSnapshot workoutSnapshot : dataSnapshot.getChildren()) {
                    Workout workout = workoutSnapshot.getValue(Workout.class);
                    workouts.add(workout);
                }
                workoutAdapter.setWorkouts(workouts);
                Log.d(TAG, "Workouts fetched from database.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Error handling for fetching workouts
                Log.e(TAG, "Error fetching workouts: " + databaseError.getMessage());
            }
        });
    }

    private String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    private void addCategoryButtons() {
        // Get category array from resources
        String[] categories = getResources().getStringArray(R.array.categories_array);
        // Create buttons for each category
        for (String category : categories) {
            Button categoryButton = new Button(getContext());
            categoryButton.setText(category);
            categoryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle category button click event
                    filterWorkoutsByCategory(category);
                }
            });

            // Add button to the layout
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(8, 0, 8, 0);
            categoryButton.setLayoutParams(layoutParams);
            categoryButtonsLayout.addView(categoryButton);
        }
    }
    private void filterWorkouts(String searchText) {
        List<Workout> filteredList = new ArrayList<>();
        for (Workout workout : workouts) {
            if (workout.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                    workout.getCategory().toLowerCase().contains(searchText.toLowerCase()) ||
                    workout.getExpertiseLevel().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(workout);
            }
        }
        workoutAdapter.setWorkouts(filteredList);
    }

    private void filterWorkoutsByCategory(String category) {
        List<Workout> filteredWorkouts = new ArrayList<>();
        for (Workout workout : workouts) {
            if (workout.getCategory().equals(category)) {
                filteredWorkouts.add(workout);
            }
        }
        workoutAdapter.setWorkouts(filteredWorkouts);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
