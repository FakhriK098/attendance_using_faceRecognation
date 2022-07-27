package com.example.absensi.ui.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Toast;

import com.example.absensi.MainActivity;
import com.example.absensi.R;
import com.example.absensi.core.Utils;
import com.example.absensi.core.model.DataKaryawan;
import com.example.absensi.databinding.ActivityLoginBinding;
import com.example.absensi.ui.resetpassword.ResetPasswordActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private FirebaseAuth firebaseAuth;
    private ActivityLoginBinding binding;
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

        binding.tvForgotPass.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
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

    private void signIn() {
        Log.d(TAG,"SignIn");
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
                            Toast.makeText(LoginActivity.this,"Email and Password Wrong!!",Toast.LENGTH_SHORT).show();
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
                    if (snapshot.exists()){
                        DataKaryawan dataKaryawan = snapshot.toObject(DataKaryawan.class);

                        if (dataKaryawan.getHakAkses().equals("true")){
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }else if (dataKaryawan.getSuspend().equals(true)){
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);

                            builder.setMessage("Akun Anda Sudah Tersuspend");
                            builder.setCancelable(true);

                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();

                            binding.etEmail.setText("");
                            binding.etPassword.setText("");
                        }else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);

                            builder.setMessage("Anda Bukan Admin");
                            builder.setCancelable(true);

                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();

                            binding.etEmail.setText("");
                            binding.etPassword.setText("");
                        }
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);

                        builder.setMessage("Email atau Password Salah !!!");
                        builder.setCancelable(true);

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        binding.etEmail.setText("");
                        binding.etPassword.setText("");
                    }

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

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null){
            onSuccess();
        }
    }
}