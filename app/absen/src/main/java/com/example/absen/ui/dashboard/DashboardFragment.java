package com.example.absen.ui.dashboard;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.absen.core.Utils;
import com.example.absen.core.model.DataKaryawan;
import com.example.absen.databinding.FragmentDashboardBinding;
import com.example.absen.ui.absen.AbsenActivity;
import com.example.absen.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.opencv.android.OpenCVLoader;

public class DashboardFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "DashboardFragment";

    private FragmentDashboardBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    private String readFace = null;
    private String nama = null;
    private Utils utils;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        OpenCVLoader.initDebug();
        utils = new Utils();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        binding.btnMulaiAbsen.setOnClickListener(this);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences preferences = requireActivity().getSharedPreferences("myLocalAbsen",  Context.MODE_PRIVATE);
        String userId = preferences.getString("userId","");
        getFaceRead(userId);
    }

    private void showMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("User Tidak Ditemukan");
        builder.setPositiveButton("Ok", (dialog, which) -> {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            FirebaseAuth.getInstance().signOut();
            dialog.cancel();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getFaceRead(String username) {
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(username);

        documentReference.get().addOnSuccessListener(snapshot -> {
            DataKaryawan dataKaryawan = snapshot.toObject(DataKaryawan.class);
            if (dataKaryawan != null){
                readFace = dataKaryawan.getReadFace();
                nama = dataKaryawan.getNama();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), AbsenActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("readFace",readFace);
        intent.putExtra("nama",nama);
        startActivity(intent);

    }
}