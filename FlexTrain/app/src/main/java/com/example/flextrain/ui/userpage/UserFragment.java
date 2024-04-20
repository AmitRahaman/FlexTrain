package com.example.flextrain.ui.userpage;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.flextrain.IntroActivity;
import com.example.flextrain.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserFragment extends Fragment {

    private EditText emailEditText;
    private EditText nameEditText;
    private EditText dobEditText;
    private EditText passwordEditText;
    private TextView userNameTextView;
    private Button modifyButton;
    private Button deleteAccountButton;
    private Button logoutButton;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    private ImageButton guideIcon;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://flextrain-6df5f-default-rtdb.europe-west1.firebasedatabase.app");

        emailEditText = root.findViewById(R.id.email_edit_text);
        nameEditText = root.findViewById(R.id.name_edit_text);
        dobEditText = root.findViewById(R.id.dob_edit_text);
        passwordEditText = root.findViewById(R.id.password_edit_text);
        modifyButton = root.findViewById(R.id.modify_button);
        deleteAccountButton = root.findViewById(R.id.delete_account_button);
        logoutButton = root.findViewById(R.id.logout_button);
        userNameTextView = root.findViewById(R.id.user_name_text_view);
        guideIcon = root.findViewById(R.id.guide_icon);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = mDatabase.getReference().child("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String userName = dataSnapshot.child("name").getValue(String.class);
                        if (!TextUtils.isEmpty(userName)) {
                            userNameTextView.setText("Logged in as: " + userName);
                        } else {
                            userNameTextView.setText("Logged in as: " + currentUser.getEmail());
                        }

                        // Set user data in EditText fields
                        emailEditText.setText(currentUser.getEmail());
                        nameEditText.setText(dataSnapshot.child("name").getValue(String.class));
                        dobEditText.setText(dataSnapshot.child("dob").getValue(String.class));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyUserData();
            }
        });

        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        guideIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGuideDialog();  // Opens the guide popup when clicked
            }
        });

        return root;
    }

    private void modifyUserData() {
        // Show dialog to enter current password for verification
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Verify Password");
        //builder.setMessage("To complete modifications, please enter your current password:");

        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_verify_password, null);
        final EditText passwordEditText = dialogView.findViewById(R.id.password_edit_text);
        builder.setView(dialogView);

        // Set positive button
        builder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = passwordEditText.getText().toString().trim();
                // Verify current password before allowing modifications
                verifyPasswordForModification(password);
            }
        });

        // Set negative button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void verifyPasswordForModification(String password) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Authenticate user with provided password
            mAuth.signInWithEmailAndPassword(currentUser.getEmail(), password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Password verification successful, proceed with modifications
                                performModifications();
                            } else {
                                // Password verification failed
                                Toast.makeText(getActivity(), "Incorrect password. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void showGuideDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("How to use FlexTrain");
        builder.setMessage("To use FlexTrain, please follow these steps:\n\n"
                + "1. To view Workouts tap on the first nav bar icon from the left.\n"
                + "2. To use the tracker tap on the second nav bar icon.\n"
                + "3. To use the nutrition feature tap on the third nav bar icon.\n"
                + "4. To use the chat tap on the fourth nav bar icon.\n"
                + "5. To logout, press the button on this page, and to delete the account press on the delete account button\n");

        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();  // Closes the dialog when user clicks "Close"
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void performModifications() {
        // Get modified user data from EditText fields
        String newName = nameEditText.getText().toString().trim();
        String newDOB = dobEditText.getText().toString().trim();
        String newPassword = passwordEditText.getText().toString().trim(); // Hash this password

        // Update user data in Firebase Realtime Database
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = mDatabase.getReference().child("users").child(userId);
            userRef.child("name").setValue(newName);
            userRef.child("dob").setValue(newDOB);

            // Update password if it's not empty
            if (!TextUtils.isEmpty(newPassword)) {
                currentUser.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Failed to update password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            // Clear the password field after successfully changing the password
            passwordEditText.setText("");

            Toast.makeText(getActivity(), "Your Data has been updated successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirm Account Deletion");

        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_confirm_delete, null);
        final EditText passwordEditText = dialogView.findViewById(R.id.password_edit_text);
        builder.setView(dialogView);

        // Set positive button
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = passwordEditText.getText().toString().trim();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getActivity(), "Please enter your password", Toast.LENGTH_SHORT).show();
                } else {
                    // Call method to delete account
                    deleteAccount(password);
                }
            }
        });

        // Set negative button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteAccount(String password) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Authenticate and delete account
            FirebaseAuth.getInstance().signInWithEmailAndPassword(user.getEmail(), password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                user.delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getActivity(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(getActivity(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(getActivity(), "Authentication failed. Please check your password.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), IntroActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
