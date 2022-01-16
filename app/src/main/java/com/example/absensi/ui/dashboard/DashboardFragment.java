package com.example.absensi.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.absensi.R;
import com.example.absensi.core.Utils;
import com.example.absensi.core.model.DataKaryawan;
import com.example.absensi.databinding.FragmentDashboardBinding;
import com.example.absensi.ui.SpalshActivity;
import com.example.absensi.ui.absen.AbsenActivity;
import com.example.absensi.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.opencv.android.OpenCVLoader;

public class DashboardFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "DashboardFragment";

    private FragmentDashboardBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String readFace = null;
    private String name = null;

    private Utils utils;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        utils = new Utils();
        OpenCVLoader.initDebug();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        binding.btnMulaiAbsen.setOnClickListener(this);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null){
            String email = utils.usernameFromEmail(firebaseAuth.getCurrentUser().getEmail());

            firebaseFirestore.collection("users").document(email).get()
                    .addOnSuccessListener(snapshot -> {
                        getFaceRead(email);
                    }).addOnFailureListener(e -> {
                        showMessage();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_mulai_absen){
            Intent intent = new Intent(getActivity(), AbsenActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("readFace", readFace);
            intent.putExtra("nama",name);
            startActivity(intent);
        }
    }

    private void getFaceRead(String username) {
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(username);

        documentReference.get().addOnSuccessListener(snapshot -> {
            DataKaryawan dataKaryawan = snapshot.toObject(DataKaryawan.class);
            if (dataKaryawan != null) {
                readFace = dataKaryawan.getReadFace();
                name = dataKaryawan.getNama();
            }
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