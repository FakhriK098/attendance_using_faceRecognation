package com.example.absensi.ui.resetpassword;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.example.absensi.R;
import com.example.absensi.databinding.ActivityResetPasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSend.setOnClickListener(v -> {
            if (!binding.etEmail.getText().toString().isEmpty()){
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                String email = binding.etEmail.getText().toString();

                firebaseAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                Toast.makeText(ResetPasswordActivity.this, "Email sent",Toast.LENGTH_SHORT).show();
                                ResetPasswordActivity.this.onBackPressed();
                            }else {
                                Toast.makeText(ResetPasswordActivity.this, "Failed sent",Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}