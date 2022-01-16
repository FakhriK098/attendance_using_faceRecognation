package com.example.absensi.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
    private FirebaseAuth firebaseAuth;
    private Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpalshBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        new Handler().postDelayed(() -> {
            firebaseAuth = FirebaseAuth.getInstance();
            if (firebaseAuth.getCurrentUser() != null){
                String email = firebaseAuth.getCurrentUser().getEmail();
                FirebaseFirestore.getInstance().collection("users").document(utils.usernameFromEmail(email))
                        .get().addOnSuccessListener(snapshot -> {
                            if (snapshot.exists()){
                                Intent intent = new Intent(SpalshActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.d("Splash","Gagal");
                            startActivity(new Intent(SpalshActivity.this, LoginActivity.class));
                        });
            }else {
                startActivity(new Intent(SpalshActivity.this, LoginActivity.class));
            }
            finish();
        },3000);
    }


}