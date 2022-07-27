package com.example.absensi.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.absensi.R;
import com.example.absensi.core.Utils;
import com.example.absensi.core.adapter.KaryawanAdapter;
import com.example.absensi.core.model.DataAbsen;
import com.example.absensi.core.model.DataKaryawan;
import com.example.absensi.core.model.DataReport;
import com.example.absensi.databinding.FragmentHomeBinding;
import com.example.absensi.ui.login.LoginActivity;
import com.example.absensi.ui.pegawai.AddPegawaiActivity;
import com.example.absensi.ui.pegawai.PegawaiActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class HomeFragment extends Fragment implements KaryawanAdapter.ItemClickListener {

    private FragmentHomeBinding binding;
    private static final String TAG = "HomeFragment";
    private FirebaseFirestore firebaseFirestore;
    private Query query;
    private KaryawanAdapter karyawanAdapter;
    private FirebaseAuth firebaseAuth;
    public static File pFile;
    private File absensifile;
    private ArrayList<DataReport> reports;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();

        absensifile = Environment.getExternalStorageDirectory();

        if ( !absensifile.exists()) {
            absensifile.mkdirs();
        }

        binding.svNamaPegawai.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                showKaryawan(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 0){
                    showKaryawan(newText);
                }else {
                    showKaryawan(null);
                }
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

        reports = new ArrayList<>();
        fetchDataKaryawan();

        karyawanAdapter = new KaryawanAdapter(query, this){
            @Override
            protected void onDataChange() {
                if (getItemCount() == 0){
                    binding.rvNamaPegawai.setVisibility(View.GONE);
                    binding.proggresBar.setVisibility(View.GONE);
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
        if (karyawanAdapter != null){
            karyawanAdapter.startListening();
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
        }else if (item.getItemId() == R.id.download_navigation){
            if (hasPermissions(requireContext(), PERMISSIONS)){
                try {
                    createReport(reports);
                } catch (FileNotFoundException e) {
                    Log.e("Error", e.getMessage());
                }
            }else {
                ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS, PERMISSION_ALL);
            }
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

    public static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static int PERMISSION_ALL = 12;
    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    private void fetchDataKaryawan(){
        firebaseFirestore.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            fetchDataAbsen(documentSnapshot.getId(), Objects.requireNonNull(documentSnapshot.get("nama")).toString());
                        }
                    }
                });
    }

    private void fetchDataAbsen(String id, String nama){
        DataReport dataReport = new DataReport();
        dataReport.setNama(nama);
        firebaseFirestore.collection("users")
                .document(id)
                .collection("absen")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            DataAbsen dataAbsen = Objects.requireNonNull(documentSnapshot.toObject(DataAbsen.class));
                            dataReport.setKegiatan(dataAbsen.getKegiatan());
                            dataReport.setPukul(dataAbsen.getPukul());
                            dataReport.setTanggal(dataAbsen.getTanggal());
                            reports.add(dataReport);
                        }
                    }
                });
    }

    private void createReport(ArrayList<DataReport> reports) throws FileNotFoundException {

        String mFileTime = new SimpleDateFormat("yyyyMMddHHmmss",
                Locale.getDefault()).format(System.currentTimeMillis());
        String mFileName = "LaporanAbsensi"+mFileTime+".pdf";
        pFile = new File(absensifile, mFileName);

        FileOutputStream output = new FileOutputStream(pFile);
        Document document = new Document(PageSize.A4);
        PdfPTable table = new PdfPTable(new float[]{6, 25, 20, 20,20});

        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setFixedHeight(50);
        table.setTotalWidth(PageSize.A4.getWidth());
        table.setWidthPercentage(100);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

        Chunk noText = new Chunk("No");
        PdfPCell noCell = new PdfPCell(new Phrase(noText));
        noCell.setFixedHeight(50);
        noCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        noCell.setVerticalAlignment(Element.ALIGN_CENTER);

        Chunk namaText = new Chunk("Nama");
        PdfPCell namaCell = new PdfPCell(new Phrase(namaText));
        namaCell.setFixedHeight(50);
        namaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        namaCell.setVerticalAlignment(Element.ALIGN_CENTER);

        Chunk kegiatanText = new Chunk("Kegiatan");
        PdfPCell kegiatanCell = new PdfPCell(new Phrase(kegiatanText));
        kegiatanCell.setFixedHeight(50);
        kegiatanCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        kegiatanCell.setVerticalAlignment(Element.ALIGN_CENTER);

        Chunk pukulText = new Chunk("Pukul");
        PdfPCell pukulCell = new PdfPCell(new Phrase(pukulText));
        pukulCell.setFixedHeight(50);
        pukulCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pukulCell.setVerticalAlignment(Element.ALIGN_CENTER);

        Chunk tanggalText = new Chunk("Tanggal");
        PdfPCell tanggalCell = new PdfPCell(new Phrase(tanggalText));
        tanggalCell.setFixedHeight(50);
        tanggalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tanggalCell.setVerticalAlignment(Element.ALIGN_CENTER);

        table.addCell(noCell);
        table.addCell(namaCell);
        table.addCell(kegiatanCell);
        table.addCell(pukulCell);
        table.addCell(tanggalCell);

        for (int i = 0; i < reports.size(); i++){
            String no = String.valueOf(i +1);
            String nama = reports.get(i).getNama();
            String kegiatan = reports.get(i).getKegiatan();
            String pukul = reports.get(i).getPukul();
            String tanggal = reports.get(i).getTanggal();

            table.addCell(no);
            table.addCell(nama);
            table.addCell(kegiatan);
            table.addCell(pukul);
            table.addCell(tanggal);
        }

        try {
            PdfWriter.getInstance(document, output);
            document.open();
            String waktu = new SimpleDateFormat("dd MMMM yyyy",
                    Locale.getDefault()).format(System.currentTimeMillis());
            Font g = new Font(Font.FontFamily.HELVETICA, 25.0f, Font.NORMAL);
            Font f = new Font(Font.FontFamily.HELVETICA, 16.0f, Font.NORMAL);
            document.add(new Paragraph("Laporan Absensi Karyawan\n\n", g));
            document.add(table);
            document.add(new Paragraph("\n\n"+waktu, f));
            document.close();

            Toast.makeText(requireActivity(), mFileName+"\n saved to \n"+output, Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Log.e("Errror",e.getMessage());
        }
    }
}