package com.example.absen.core.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.absen.core.model.DataAbsen;
import com.example.absen.databinding.ItemHistoryBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import org.jetbrains.annotations.NotNull;

public class HistoryAdapter extends FirestoreAdapter<HistoryAdapter.ViewHolder> {

    public interface ItemClickListener{
        void onItemClick(DocumentSnapshot snapshot);
    }

    private ItemClickListener itemClickListener;

    public HistoryAdapter(Query query, ItemClickListener itemClickListener){
        super(query);
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemHistoryBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull HistoryAdapter.ViewHolder holder, int position) {
        holder.bind(getSnapshot(position),itemClickListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        private ItemHistoryBinding binding;

        public ViewHolder(ItemHistoryBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }

        public ViewHolder(View view){super(view);}

        public void bind(final DocumentSnapshot snapshot,
                         final ItemClickListener itemClickListener){
            DataAbsen dataAbsen = snapshot.toObject(DataAbsen.class);
            binding.tvTanggalAbsen.setText(dataAbsen.getTanggal());
            binding.tvPukulAbsen.setText(dataAbsen.getPukul());
            binding.tvKegiatanAbsen.setText(dataAbsen.getKegiatan());

            itemView.setOnClickListener(v -> {
                if (itemClickListener != null){
                    itemClickListener.onItemClick(snapshot);
                }
            });
        }
    }
}
