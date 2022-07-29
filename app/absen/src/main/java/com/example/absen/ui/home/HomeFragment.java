package com.example.absen.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.absen.core.Utils;
import com.example.absen.core.adapter.HistoryAdapter;
import com.example.absen.core.model.DataKaryawan;
import com.example.absen.databinding.FragmentHomeBinding;
import com.example.absen.ui.login.LoginActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

public class HomeFragment extends Fragment implements HistoryAdapter.ItemClickListener {
    private static final String TAG = "HomeFragment";

    private FragmentHomeBinding binding;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private Query query;
    private Utils utils;
    private String userId;

    private HistoryAdapter historyAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userId = null;
        utils = new Utils();

        return root;
    }

    private void showHistory() {
        query = firebaseFirestore.collection("users").document(userId).collection("absen").orderBy("tanggal");
        historyAdapter = new HistoryAdapter(query,this){
            @Override
            protected void onDataChange() {
                super.onDataChange();
                if (getItemCount() == 0){
                    binding.rvHistory.setVisibility(View.GONE);
                    binding.progress.setVisibility(View.GONE);
                    binding.emptyHistory.setVisibility(View.VISIBLE);
                }else {
                    binding.progress.setVisibility(View.INVISIBLE);
                    binding.rvHistory.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                super.onError(e);
                Snackbar.make(binding.getRoot(),
                        "Error: ",Snackbar.LENGTH_LONG).show();
            }
        };

        binding.rvHistory.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvHistory.setAdapter(historyAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences preferences = requireActivity().getSharedPreferences("myLocalAbsen", Context.MODE_PRIVATE);
        userId = preferences.getString("userId","");

        firebaseFirestore.collection("users").document(userId).get()
                .addOnSuccessListener(snapshot -> {
                    showHistory();
                    getNama();
                    if (historyAdapter != null){
                        historyAdapter.startListening();
                    }
                }).addOnFailureListener(e -> {
                    showMassage();
                });
    }

    private void showMassage() {
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

    private void getNama() {
        DocumentReference documentReference = firebaseFirestore.collection("users").document(userId);

        documentReference.get().addOnSuccessListener(snapshot -> {
            DataKaryawan dataKaryawan = snapshot.toObject(DataKaryawan.class);
            if (dataKaryawan != null){
                String nama = dataKaryawan.getNama();
                binding.tittleSelamatDatang.setText("Selamat Datang "+nama);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (historyAdapter != null){
            historyAdapter.stopListening();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemClick(DocumentSnapshot snapshot) {

    }
}