package com.example.absensi.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.absensi.MainActivity;
import com.example.absensi.core.Utils;
import com.example.absensi.databinding.ActivitySpalshBinding;
import com.example.absensi.ui.login.LoginActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

public class SpalshActivity extends AppCompatActivity {

    private ActivitySpalshBinding binding;
    private Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpalshBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        new Handler().postDelayed(() -> {
            SharedPreferences preferences = this.getSharedPreferences("myLocalAbsensi",MODE_PRIVATE);
            Boolean hasLogin = preferences.getBoolean("hasLogin", false);
            if (hasLogin){
                Intent intent = new Intent(SpalshActivity.this, MainActivity.class);
                startActivity(intent);
            }else {
                startActivity(new Intent(SpalshActivity.this, LoginActivity.class));
            }
            finish();
        },3000);
    }


}