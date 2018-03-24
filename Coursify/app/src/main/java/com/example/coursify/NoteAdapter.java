package com.example.coursify;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Created by Ravina on 2018-02-17.
 */

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
    private List<Note> mNotes;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    // taken from CommentAdapter
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public RelativeLayout mLayout;

        public ViewHolder(RelativeLayout v) {
            super(v);
            mLayout = v;
        }

        public void bind(final Note note, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(note);
                }
            });

        }
    }

    public NoteAdapter(List<Note> dataset, OnItemClickListener listener) {
        mNotes = dataset;
        Collections.sort(mNotes);
        this.listener = listener;
    }

    @Override
    public NoteAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // create a new view
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext()).
                inflate(R.layout.note_list_item, parent, false);


        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    // replaces contents of a view (invoked by layout manager)
    @Override
    public void onBindViewHolder(NoteAdapter.ViewHolder holder, int position) {
        Note currNote = mNotes.get(position);
        holder.bind(currNote, listener);
        TextView txtNoteBody = holder.mLayout.findViewById(R.id.txtNoteBody);

        if(currNote.pinned) {
            ImageView pin = holder.mLayout.findViewById(R.id.pinIV);
            pin.setVisibility(View.VISIBLE);
        }

        txtNoteBody.setText(currNote.content);

        txtNoteBody.setBackgroundColor(currNote.getColour());
        holder.mLayout.invalidate();
    }


    @Override
    public int getItemCount() {
        return mNotes.size();
    }
}
