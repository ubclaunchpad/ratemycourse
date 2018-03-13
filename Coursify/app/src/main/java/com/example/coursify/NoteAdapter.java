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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Created by Ravina on 2018-02-17.
 */

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
    private List<Note> mNotes;

    // taken from CommentAdapter
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public RelativeLayout mLayout;

        public ViewHolder(RelativeLayout v) {
            super(v);
            mLayout = v;
        }
    }

    public NoteAdapter(List<Note> dataset, Context context) {
        mNotes = dataset;
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
        TextView txtNoteBody = holder.mLayout.findViewById(R.id.txtNoteBody);

        if(currNote.pinned) {
            // add an imageview or something on top of text view to indicate it is pinned
        }

        txtNoteBody.setText(currNote.content);
//        String hexVal = '#' + Integer.toHexString(currNote.getColour());
//        // need to mask
//        //txtNoteBody.setBackgroundColor(0xff000000 + Integer.parseInt(hexVal,16));
//        int color = Color.parseColor(hexVal);

        txtNoteBody.setBackgroundColor(currNote.getColour());

//        txtNoteBody.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openEditNotePrompt(currNote);
//            }
//        });


    }

//    public void openEditNotePrompt(final Note currNote) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("Edit note");
//        View viewInflated = LayoutInflater.from(context).inflate(R.layout.edit_note, (ViewGroup) parent.findViewById(R.id.edit_note), false);
//
//        final EditText editTxtNoteBody = viewInflated.findViewById(R.id.noteContent);
//        final CheckBox chkBoxPin = viewInflated.findViewById(R.id.pinCheckBox);
//
//        editTxtNoteBody.setText(currNote.content);
//        chkBoxPin.setChecked(currNote.pinned);
//
//
//
//        builder.setView(viewInflated);
//        builder.setPositiveButton("Edit note", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String noteBody = editTxtNoteBody.getText().toString();
//
//                if(noteBody.equals("")) {
//                    Toast.makeText(context, "Please type a note.", Toast.LENGTH_SHORT).show();
//                    dialog.cancel();
//                    openEditNotePrompt(currNote);
//                } else {
//                    editNoteInDatabase(noteBody, chkBoxPin.isChecked(), currNote); // ADD COLOR AFTER (TO add_note AS WELL) imageview with on click?
//                }
//            }
//        });
//
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//
//        builder.show();
//
//    }
//
//    private void editNoteInDatabase(String noteBody, boolean pinned, Note currNote) {
//        currNote.editContent(noteBody);
//        //currNote.editColor();!!!
//        currNote.setPinned(pinned);
//
//        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();
//
//        DatabaseReference mUserRef = mDatabaseRef.child(FirebaseEndpoint.USERS)
//                .child(Utils.processEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail()));
//
//        DatabaseReference notesRef = mUserRef.child(FirebaseEndpoint.NOTES); // will this work ok?
//
//        notesRef.
//        notesRef.push().setValue(note);
//    }


    @Override
    public int getItemCount() {
        return mNotes.size();
    }
}
