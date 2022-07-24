package com.example.absen.ui.notifications;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.absen.R;
import com.example.absen.core.Utils;
import com.example.absen.core.model.DataKaryawan;
import com.example.absen.databinding.FragmentNotificationsBinding;
import com.example.absen.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private static final String TAG = "NotificationFragment";

    private FragmentNotificationsBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private Utils utils;
    private List<DataKaryawan> list;

    private TextView mNama, mEmail, mAlamat, mTanggal, mAgama, mKelamin, mJabatan;
    private ImageView mImage;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setHasOptionsMenu(true);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        utils = new Utils();
        list = new ArrayList<>();

        mNama = root.findViewById(R.id.tv_nama_pegawai);
        mEmail = root.findViewById(R.id.tv_email_pegawai);
        mAlamat = root.findViewById(R.id.tv_alamat_pegawai);
        mTanggal = root.findViewById(R.id.tv_tanggal_pegawai);
        mAgama = root.findViewById(R.id.tv_agama_pegawai);
        mKelamin = root.findViewById(R.id.tv_kelamin_pegawai);
        mJabatan = root.findViewById(R.id.tv_jabatan_pegawai);

        mImage = root.findViewById(R.id.img_pegawai);

        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        return root;
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Apakah Anda Yakin?");
        builder.setCancelable(true);

        builder.setPositiveButton("Yes", (dialog, which) -> {
            SharedPreferences preferences = getActivity().getSharedPreferences("myLocalAbsen", Context.MODE_PRIVATE);
            preferences.edit().clear().commit();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            dialog.cancel();
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.cancel();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null){
            String userId = utils.usernameFromEmail(firebaseAuth.getCurrentUser().getEmail());

            firebaseFirestore.collection("users").document(userId).get()
                    .addOnSuccessListener(snapshot -> {
                        DocumentReference documentReference = firebaseFirestore.collection("users").document(userId);

                        showData(documentReference);
                    }).addOnFailureListener(e -> {
                        showMassage();
            });
        }
    }

    private void showMassage() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());

        builder.setMessage("User Tidak Ditemukan");
        builder.setPositiveButton("Ok", (dialog, which) -> {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            firebaseAuth.signOut();
            dialog.cancel();
        });

        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showData(DocumentReference documentReference) {
        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null){
                    DataKaryawan dataKaryawan = documentSnapshot.toObject(DataKaryawan.class);

                    if (dataKaryawan != null){
                        mNama.setText(dataKaryawan.getNama());
                        mEmail.setText(dataKaryawan.getEmail());
                        mAlamat.setText(dataKaryawan.getAsal());
                        mTanggal.setText(dataKaryawan.getTanggal_lahir());
                        mAgama.setText(dataKaryawan.getAgama());
                        mKelamin.setText(dataKaryawan.getJenis_kelamin());
                        mJabatan.setText(dataKaryawan.getJabatan());

                        Glide.with(mImage.getContext())
                                .load(dataKaryawan.getImageUri())
                                .override(120,120)
                                .into(mImage);

                        list.add(dataKaryawan);
                    }
                }
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}