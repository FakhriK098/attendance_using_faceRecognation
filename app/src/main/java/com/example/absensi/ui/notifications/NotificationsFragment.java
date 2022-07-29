package com.example.absensi.ui.notifications;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.absensi.MainActivity;
import com.example.absensi.R;
import com.example.absensi.core.Utils;
import com.example.absensi.core.model.DataKaryawan;
import com.example.absensi.databinding.FragmentNotificationsBinding;
import com.example.absensi.ui.DialogEdit;
import com.example.absensi.ui.login.LoginActivity;
import com.example.absensi.ui.pegawai.AddPegawaiActivity;
import com.example.absensi.ui.pegawai.PegawaiActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NotificationsFragment extends Fragment {
    private static final String TAG = "NotificationFragment";

    private FragmentNotificationsBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private ProgressDialog progressDialog;


    private Utils utils;
    private List<DataKaryawan> list;
    private String userId;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setHasOptionsMenu(true);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        binding.btnLogout.setOnClickListener(v -> {
            SharedPreferences preferences = getActivity().getSharedPreferences("myLocalAbsensi", Context.MODE_PRIVATE);
            preferences.edit().clear().commit();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
        });

        utils = new Utils();
        list = new ArrayList<>();
        progressDialog = new ProgressDialog(getActivity());

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences preferences = getActivity().getSharedPreferences("myLocalAbsensi", Context.MODE_PRIVATE);
        userId = preferences.getString("userId","");

        firebaseFirestore.collection("users").document(userId).get()
                .addOnSuccessListener(snapshot -> {
                    DocumentReference documentReference = firebaseFirestore.collection("users").document(userId);

                    showData(documentReference);
                }).addOnFailureListener(e -> {
                    showMessage();
                });
    }

    private void showData(DocumentReference documentReference) {
        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null){
                    karyawanBio(Objects.requireNonNull(documentSnapshot.toObject(DataKaryawan.class)));
                }
            }
        });
    }

    private void karyawanBio(DataKaryawan karyawan) {
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
                .override(120,120)
                .into(binding.imgPegawai);

        list.add(karyawan);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.up_menu_pegawai, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
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
                dialogEdit.setArguments(bundle);
                dialogEdit.show(getParentFragmentManager(),"DialogEdit");
                break;
            case R.id.btn_hapus:
                hapusPegawai();
                break;
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void hapusPegawai() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
            Log.d(TAG,"delete Firestore sukses");
            StorageReference reference = FirebaseStorage.getInstance().getReference(folder);

            reference.delete().addOnSuccessListener(unused1 -> {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                Log.d(TAG,"delete Image sukses");
                progressDialog.dismiss();
            });
        });
    }

    private void showMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("User Tidak Ditemukan");
        builder.setPositiveButton("Ok", (dialog, which) -> {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            firebaseAuth.signOut();
            dialog.cancel();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}