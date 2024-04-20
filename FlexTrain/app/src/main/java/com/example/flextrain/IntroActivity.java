package com.example.flextrain;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class IntroActivity extends AppCompatActivity {

    private Button loginButton, registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        ImageView welcomeImage = findViewById(R.id.welcomeImage);
        TextView appName = findViewById(R.id.appName);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Apply the fade-in animation to the ImageView
        welcomeImage.startAnimation(fadeIn);
        appName.startAnimation(fadeIn);

        loginButton = findViewById(R.id.btnLogin);
        registerButton = findViewById(R.id.btnRegister);

        //  onClickListener for login button
        loginButton.setOnClickListener(v -> {
            startActivity(new Intent(IntroActivity.this, LoginActivity.class));
        });

        //  onClickListener for register button
        registerButton.setOnClickListener(v -> {
            startActivity(new Intent(IntroActivity.this, RegisterActivity.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if the user is already signed in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // if User is signed in, redirect to MainActivity
            startActivity(new Intent(IntroActivity.this, MainActivity.class));
            finish(); // Finish IntroActivity to prevent going back to it using the back button
        }
    }
}
