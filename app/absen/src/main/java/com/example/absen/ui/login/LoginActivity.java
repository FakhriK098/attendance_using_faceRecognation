package com.example.absen.ui.login;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;

import com.example.absen.MainActivity;
import com.example.absen.R;
import com.example.absen.core.Utils;
import com.example.absen.databinding.ActivityLoginBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private FirebaseFirestore firebaseFirestore;

    private Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        utils = new Utils();
        progressDialog = new ProgressDialog(this);

        binding.btnLogin.setOnClickListener(v -> signIn());
        binding.btnShowPass.setOnClickListener(v -> {
            if (binding.etPassword.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                binding.btnShowPass.setImageResource(R.drawable.icons_hide_green24);
                binding.etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }else {
                binding.btnShowPass.setImageResource(R.drawable.icons_eye_green24);
                binding.etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
    }

    private void signIn() {
        if (validateForm()){
            progressDialog.setMessage("Login, Please Wait");
            progressDialog.show();
            String email = binding.etEmail.getText().toString();
            String password = binding.etPassword.getText().toString();

            firebaseAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(LoginActivity.this, task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()){
                            onSuccess();
                        }else {
                            Snackbar.make(binding.getRoot(),"Email dan Password Salah", Snackbar.LENGTH_LONG).show();
                            binding.etEmail.setText("");
                            binding.etPassword.setText("");
                        }
                    });
        }
    }

    private void onSuccess() {
        String email = firebaseAuth.getCurrentUser().getEmail();
        String userId = utils.usernameFromEmail(email);
        firebaseFirestore.collection("users").document(userId).get()
                .addOnSuccessListener(snapshot -> {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                }).addOnFailureListener(e -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setMessage("Email atau Password Salah !!!");
                    builder.setCancelable(true);

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    binding.etEmail.setText("");
                    binding.etPassword.setText("");
        });
    }

    private boolean validateForm(){
        boolean result = true;
        if (TextUtils.isEmpty(binding.etEmail.getText().toString())){
            binding.etEmail.setError("Required");
            result = false;
        }else {
            binding.etEmail.setError(null);
        }

        if (TextUtils.isEmpty(binding.etPassword.getText().toString())){
            binding.etPassword.setError("Required");
            result = false;
        }else {
            binding.etPassword.setError(null);
        }

        return result;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null){
            onSuccess();
        }
    }
}