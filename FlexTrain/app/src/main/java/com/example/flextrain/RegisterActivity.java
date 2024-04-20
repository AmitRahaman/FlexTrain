package com.example.flextrain;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditTextRegister, passwordEditTextRegister;
    private EditText nameEditText, dobEditText;
    private Button registerButton, btnBackToIntro;
    private Spinner spinnerExpertise;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://flextrain-6df5f-default-rtdb.europe-west1.firebasedatabase.app");

        emailEditTextRegister = findViewById(R.id.editTextEmailReg);
        passwordEditTextRegister = findViewById(R.id.editTextPasswordReg);
        nameEditText = findViewById(R.id.editTextName);
        dobEditText = findViewById(R.id.editTextDOB);
        registerButton = findViewById(R.id.buttonRegister);
        btnBackToIntro = findViewById(R.id.btnBackToIntro);
        spinnerExpertise = findViewById(R.id.spinnerExpertise);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        btnBackToIntro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, IntroActivity.class));
                finish();
            }
        });
    }

    private void registerUser() {
        final String email = emailEditTextRegister.getText().toString().trim();
        String password = passwordEditTextRegister.getText().toString().trim();
        final String name = nameEditText.getText().toString().trim();
        final String dob = dobEditText.getText().toString().trim();
        final String expertiseLevel = spinnerExpertise.getSelectedItem().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            // Store user information in the Firebase Realtime Database
                            HashMap<String, Object> userData = new HashMap<>();
                            userData.put("name", name);
                            userData.put("dob", dob);
                            userData.put("email", email);
                            userData.put("expertiseLevel", expertiseLevel);
                            mDatabase.getReference().child("users").child(userId).setValue(userData)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // Registration successful
                                                Toast.makeText(RegisterActivity.this, "Registration successful.",
                                                        Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                                finish();
                                            } else {
                                                // Registration failed.
                                                Toast.makeText(RegisterActivity.this, "Failed to store user data.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            // Registration failed.
                            Toast.makeText(RegisterActivity.this, "Registration failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
