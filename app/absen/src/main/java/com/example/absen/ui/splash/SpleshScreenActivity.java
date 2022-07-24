package com.example.absen.ui.splash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.example.absen.MainActivity;
import com.example.absen.core.Utils;
import com.example.absen.databinding.ActivitySpleshScreenBinding;
import com.example.absen.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SpleshScreenActivity extends AppCompatActivity {

    private ActivitySpleshScreenBinding binding;
    private FirebaseAuth firebaseAuth;
    private Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpleshScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new Handler().postDelayed(() -> {
            SharedPreferences preferences = this.getSharedPreferences("myLocalAbsen", Context.MODE_PRIVATE);
            Boolean hasLogin = preferences.getBoolean("hasLogin", false);
            if (hasLogin){
                startActivity(new Intent(SpleshScreenActivity.this, MainActivity.class));
            }else {
                startActivity(new Intent(SpleshScreenActivity.this, LoginActivity.class));
            }
            finish();
        },3000);
    }
}