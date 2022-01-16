package com.example.absensi.core.adapter;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;

import java.util.ArrayList;

public abstract class FirestoreAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements EventListener<QuerySnapshot> {

    private static final String TAG = "FirestoreAdapter";

    private Query query;
    private ListenerRegistration listenerRegistration;

    private ArrayList<DocumentSnapshot> documentSnapshots = new ArrayList<>();

    public FirestoreAdapter(Query query){
        this.query = query;
    }

    @Override
    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e){
        if (e != null){
            Log.e(TAG,"error",e);
            return;
        }
        for (DocumentChange change : documentSnapshots.getDocumentChanges()){
            switch (change.getType()){
                case ADDED:
                    onDocumentAdded(change);
                    break;
                case MODIFIED:
                    onDocumentModified(change);
                    break;
                case REMOVED:
                    onDocumentRemove(change);
                    break;
            }
        }

        onDataChange();
    }

    public void startListening(){
        if (query != null && listenerRegistration == null){
            listenerRegistration = query.addSnapshotListener(this);
        }
    }

    public void stopListening(){
        if (listenerRegistration != null){
            listenerRegistration.remove();
            listenerRegistration = null;
        }

        documentSnapshots.clear();
        notifyDataSetChanged();
    }

    public void setQuery(Query query){
        stopListening();

        documentSnapshots.clear();
        notifyDataSetChanged();

        this.query = query;
        startListening();
    }

    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }

    protected DocumentSnapshot getSnapshot(int index){
        return documentSnapshots.get(index);
    }

    protected void onDataChange() {
    }

    private void onDocumentRemove(DocumentChange change) {
        documentSnapshots.remove(change.getOldIndex());
        notifyItemRemoved(change.getOldIndex());
    }

    private void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()){
            documentSnapshots.set(change.getOldIndex(),change.getDocument());
            notifyItemChanged(change.getOldIndex());
        }else {
            documentSnapshots.remove(change.getOldIndex());
            documentSnapshots.add(change.getNewIndex(),change.getDocument());
            notifyItemMoved(change.getOldIndex(),change.getNewIndex());
        }
    }

    private void onDocumentAdded(DocumentChange change) {
        documentSnapshots.add(change.getNewIndex(), change.getDocument());
        notifyItemInserted(change.getNewIndex());
    }

    protected void onError(FirebaseFirestoreException e) {
        Log.w(TAG, "onError", e);
    }
}
