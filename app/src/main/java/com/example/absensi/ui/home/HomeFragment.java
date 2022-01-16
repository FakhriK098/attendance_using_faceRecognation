package com.example.absensi.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.absensi.R;
import com.example.absensi.core.Utils;
import com.example.absensi.core.adapter.KaryawanAdapter;
import com.example.absensi.databinding.FragmentHomeBinding;
import com.example.absensi.ui.login.LoginActivity;
import com.example.absensi.ui.pegawai.AddPegawaiActivity;
import com.example.absensi.ui.pegawai.PegawaiActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import org.jetbrains.annotations.NotNull;

public class HomeFragment extends Fragment implements KaryawanAdapter.ItemClickListener {

    private FragmentHomeBinding binding;
    private static final String TAG = "HomeFragment";
    private FirebaseFirestore firebaseFirestore;
    private Query query;
    private KaryawanAdapter karyawanAdapter;
    private FirebaseAuth firebaseAuth;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();

        binding.svNamaPegawai.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                showKaryawan(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                showKaryawan(newText);
                return false;
            }
        });

        binding.svNamaPegawai.setOnCloseListener(() -> {
            showKaryawan(null);
            return false;
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseFirestore.setLoggingEnabled(true);
        firebaseFirestore = FirebaseFirestore.getInstance();

        query = firebaseFirestore.collection("users");

        karyawanAdapter = new KaryawanAdapter(query, this){
            @Override
            protected void onDataChange() {
                if (getItemCount() == 0){
                    binding.rvNamaPegawai.setVisibility(View.GONE);
                }else {
                    binding.proggresBar.setVisibility(View.GONE);
                    binding.rvNamaPegawai.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                Snackbar.make(binding.getRoot(),
                        "Error: ",Snackbar.LENGTH_LONG).show();
            }
        };
        binding.rvNamaPegawai.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNamaPegawai.setAdapter(karyawanAdapter);

    }



    private void showKaryawan(String nama){
        firebaseFirestore = FirebaseFirestore.getInstance();

        if (nama != null){
            query = firebaseFirestore.collection("users").whereEqualTo("nama",nama);
        }else {
            query = firebaseFirestore.collection("users");
        }

        karyawanAdapter.setQuery(query);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null){
            Utils utils = new Utils();
            String email = utils.usernameFromEmail(firebaseAuth.getCurrentUser().getEmail());

            firebaseFirestore.collection("users").document(email).get()
                    .addOnSuccessListener(snapshot -> {
                        Log.d(TAG, "Load sukses");

                        if (karyawanAdapter != null){
                            karyawanAdapter.startListening();
                        }
                    }).addOnFailureListener(e -> {
                        showMessage();
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (karyawanAdapter != null){
            karyawanAdapter.stopListening();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.up_menu_home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        if (item.getItemId() == R.id.add_navigation){
            Intent intent = new Intent(getActivity(), AddPegawaiActivity.class);
            startActivity(intent);
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemClick(DocumentSnapshot snapshot) {
        Intent intent = new Intent(getActivity(), PegawaiActivity.class);
        intent.putExtra("userId",snapshot.getId());
        startActivity(intent);
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