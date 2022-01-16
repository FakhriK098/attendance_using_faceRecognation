package com.example.absensi.core.adapter;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.absensi.core.model.DataKaryawan;
import com.example.absensi.databinding.ItemPegawaiBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class KaryawanAdapter extends FirestoreAdapter<KaryawanAdapter.ViewHolder> {

    public interface ItemClickListener{
        void onItemClick(DocumentSnapshot snapshot);
    }

    private ItemClickListener itemClickListener;

    public KaryawanAdapter(Query query, ItemClickListener itemClickListener){
        super(query);
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemPegawaiBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull KaryawanAdapter.ViewHolder holder, int position) {
        holder.bind(getSnapshot(position),itemClickListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ItemPegawaiBinding binding;

        public ViewHolder(ItemPegawaiBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }

        public ViewHolder(View view){
            super(view);
        }

        public void bind(final DocumentSnapshot snapshot,
                        final ItemClickListener itemClickListener){
            DataKaryawan dataKaryawan = snapshot.toObject(DataKaryawan.class);

            Glide.with(binding.imgPegawai.getContext())
                    .load(dataKaryawan.getImageUri())
                    .override(44,44)
                    .into(binding.imgPegawai);
            binding.tvNamaPegawai.setText(dataKaryawan.getNama());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null){
                        itemClickListener.onItemClick(snapshot);
                    }
                }
            });
        }
    }
}
