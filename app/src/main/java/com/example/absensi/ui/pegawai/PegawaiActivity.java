package com.example.absensi.ui.pegawai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.absensi.MainActivity;
import com.example.absensi.R;
import com.example.absensi.core.Utils;
import com.example.absensi.core.adapter.AbsenAdapter;
import com.example.absensi.core.model.DataKaryawan;
import com.example.absensi.databinding.ActivityPegawaiBinding;
import com.example.absensi.ui.DialogEdit;
import com.example.absensi.ui.SpalshActivity;
import com.example.absensi.ui.login.LoginActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PegawaiActivity extends AppCompatActivity implements AbsenAdapter.ItemClickListener {
    private static final String TAG = "PegawaiActivity";

    private ActivityPegawaiBinding binding;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private Query query;
    private String userId;
    private List<DataKaryawan> list;

    private AbsenAdapter absenAdapter;
    private ProgressDialog progressDialog;
    private Utils utils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPegawaiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitle("Karyawan");

        firebaseAuth = FirebaseAuth.getInstance();

        list = new ArrayList<>();
        userId = getIntent().getStringExtra("userId");

        FirebaseFirestore.setLoggingEnabled(true);
        firebaseFirestore = FirebaseFirestore.getInstance();
        showKaryawan();
        progressDialog = new ProgressDialog(PegawaiActivity.this);

        utils = new Utils();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.up_menu_pegawai, menu);

        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.btn_ubah:
                DialogEdit dialogEdit = new DialogEdit();
                Bundle bundle = new Bundle();
                bundle.putString("userId",userId);
                bundle.putString("nama",list.get(0).getNama());
                bundle.putString("email",list.get(0).getEmail());
                bundle.putString("asal",list.get(0).getAsal());
                bundle.putString("ttl",list.get(0).getTanggal_lahir());
                bundle.putString("agama",list.get(0).getAgama());
                bundle.putString("kelamin",list.get(0).getJenis_kelamin());
                bundle.putString("jabatan",list.get(0).getJabatan());
                bundle.putString("imageUri",list.get(0).getImageUri());
                bundle.putString("readFace",list.get(0).getReadFace());
                bundle.putString("hakAkses",list.get(0).getHakAkses());
                bundle.putString("nik", list.get(0).getNik());
                dialogEdit.setArguments(bundle);
                dialogEdit.show(getSupportFragmentManager(),"DialogEdit");

                break;
            case R.id.btn_hapus:
                hapusPegawai();
                break;
        }
        return true;
    }

    private void hapusPegawai() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PegawaiActivity.this);
        builder.setMessage("Apakah Anda Yakin Ingin Menghapus?");
        builder.setCancelable(true);

        builder.setPositiveButton("Yes", (dialog, which) -> {
            progressDialog.setMessage("Delete, Please Wait...");
            progressDialog.show();
            deletePegawai();
            dialog.cancel();
        });

        builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePegawai() {
        String folder = utils.usernameFromEmail(list.get(0).getEmail());

        firebaseFirestore.collection("users").document(userId)
                .delete().addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    startActivity(new Intent(PegawaiActivity.this, MainActivity.class));
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(PegawaiActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        showKaryawan();
        if (absenAdapter != null){
            absenAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (absenAdapter != null){
            absenAdapter.stopListening();
        }
    }

    private void showKaryawan() {
        //Show Biodata Karyawan
        DocumentReference documentReferenceBiodata = firebaseFirestore.collection("users").document(userId);
        documentReferenceBiodata.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null){
                    showDocument(Objects.requireNonNull(documentSnapshot.toObject(DataKaryawan.class)));
                }
            }
        });

        //Show History Absen
        query = firebaseFirestore.collection("users").document(userId).collection("absen");
        absenAdapter = new AbsenAdapter(query,this){
            @Override
            protected void onDataChange() {
                super.onDataChange();
                if (getItemCount() == 0){
                    binding.rvHistory.setVisibility(View.GONE);
                }else {
                    binding.rvHistory.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                Snackbar.make(binding.getRoot(),
                        "Error: ",Snackbar.LENGTH_LONG).show();
            }
        };
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(PegawaiActivity.this));
        binding.rvHistory.setAdapter(absenAdapter);
    }

    private void showDocument(DataKaryawan karyawan) {
        binding.vBiodata.tvNamaPegawai.setText(karyawan.getNama());
        binding.vBiodata.tvEmailPegawai.setText(karyawan.getEmail());
        binding.vBiodata.tvAlamatPegawai.setText(karyawan.getAsal());
        binding.vBiodata.tvTanggalPegawai.setText(karyawan.getTanggal_lahir());
        binding.vBiodata.tvAgamaPegawai.setText(karyawan.getAgama());
        binding.vBiodata.tvKelaminPegawai.setText(karyawan.getJenis_kelamin());
        binding.vBiodata.tvJabatanPegawai.setText(karyawan.getJabatan());
        binding.vBiodata.tvNikPegawai.setText(karyawan.getNik());


        Glide.with(binding.imgPegawai.getContext())
                .load(karyawan.getImageUri())
                .into(binding.imgPegawai);
        list.add(karyawan);

    }

    @Override
    public void onItemClick(DocumentSnapshot snapshot) {

    }
}