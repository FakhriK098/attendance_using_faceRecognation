package com.example.absen.ui.absen;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.absen.MainActivity;
import com.example.absen.R;
import com.example.absen.core.Utils;
import com.example.absen.core.model.DataAbsen;
import com.example.absen.databinding.ActivityResultAbsenBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ResultAbsenActivity extends AppCompatActivity {

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
        binding.tvNamaKaryawan.setText("Hallo, "+nama);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);
        binding.btnAbsen.setOnClickListener(v -> absen());

        utils = new Utils();
    }

    private void absen() {
        progressDialog.setMessage("Proses Absen, Please Wait..");
        progressDialog.show();
        TextView t = (TextView) findViewById(R.id.tv_tanggal_absen);
        TextView p = (TextView) findViewById(R.id.tv_pukul_absen);
        Spinner k = (Spinner) findViewById(R.id.sp_kegiatan);
        String tanggal = t.getText().toString();
        String pukul = p.getText().toString();
        String kegiatan = k.getSelectedItem().toString();

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
            String email = firebaseAuth.getCurrentUser().getEmail();
            if (email != null){
                username = utils.usernameFromEmail(email);
            }
            TextView t = (TextView) findViewById(R.id.tv_tanggal_absen);
            TextView p = (TextView) findViewById(R.id.tv_pukul_absen);
            t.setText(tanggal.format(date));
            p.setText(pukul.format(date));
        }
    }
}