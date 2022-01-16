package com.example.absensi.ui.absen;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.example.absensi.MainActivity;
import com.example.absensi.core.Utils;
import com.example.absensi.core.model.DataAbsen;
import com.example.absensi.databinding.ActivityResultAbsenBinding;
import com.example.absensi.ui.SpalshActivity;
import com.example.absensi.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ResultAbsenActivity extends AppCompatActivity {
    private static final String TAG = "ResultAbsenActivity";

    private ActivityResultAbsenBinding binding;
    private Date date;
    private SimpleDateFormat tanggal;
    private SimpleDateFormat pukul;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private FirebaseFirestore firebaseFirestore;

    private String username = null;

    private Utils utils;

    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultAbsenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        date = new Date();
        tanggal = new SimpleDateFormat("dd-MM-yyyy");
        pukul = new SimpleDateFormat("HH:mm:ss");

        String nama = getIntent().getStringExtra("nama");
        binding.tvNamaKaryawan.setText("Hallo, "+ nama);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);
        binding.btnAbsen.setOnClickListener(v -> absen());

        utils = new Utils();
    }

    private void absen() {
        progressDialog.setMessage("Proses Absen, Please Wait..");
        progressDialog.show();
        String tanggal = binding.keteranganAbsen.tvTanggalAbsen.getText().toString();
        String pukul = binding.keteranganAbsen.tvPukulAbsen.getText().toString();
        String kegiatan = binding.keteranganAbsen.spKegiatan.getSelectedItem().toString();

        DataAbsen dataAbsen = new DataAbsen(tanggal,pukul, kegiatan);
        Map<String,Object> map = dataAbsen.toMap();

        firebaseFirestore.collection("users")
                .document(username)
                .collection("absen")
                .add(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        startActivity(new Intent(ResultAbsenActivity.this, MainActivity.class));
                        progressDialog.dismiss();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null){
            username = utils.usernameFromEmail(firebaseAuth.getCurrentUser().getEmail());
            binding.keteranganAbsen.tvTanggalAbsen.setText(tanggal.format(date));
            binding.keteranganAbsen.tvPukulAbsen.setText(pukul.format(date));
        }
    }
}