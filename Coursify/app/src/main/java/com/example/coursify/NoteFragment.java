package com.example.coursify;

import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {/@/link NoteFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NoteFragment#//newInstance} factory method to
 * create an instance of this fragment.
 */
public class NoteFragment extends Fragment {
    private FloatingActionButton addNoteButton;

    private DatabaseReference mDatabaseRef;
    private DatabaseReference mUserRef;

    private RecyclerView mListNotes;
    private RecyclerView.Adapter mNotesAdapter;
    private RecyclerView.LayoutManager mNotesManager;


    private Dialog currNoteDialog;
    private boolean editMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_note, container, false);
        findViewsById(view);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mUserRef = mDatabaseRef.child(FirebaseEndpoint.USERS)
                .child(Utils.processEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail()));

        populateUIFromDatabaseInfo();
        editMode = false; // starts off as false

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void populateUIFromDatabaseInfo() {
        mUserRef.child(FirebaseEndpoint.NOTES).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Note> notes = new ArrayList<>();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    int color = Integer.valueOf(snapshot.child("color").getValue().toString());
                    String noteBody = snapshot.child("content").getValue().toString();
                    boolean pinned = (boolean) snapshot.child("pinned").getValue();
                    Note note = new Note(color, noteBody, pinned);
                    notes.add(note);
                }

                mNotesAdapter = new NoteAdapter(notes, new NoteAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Note note) {
                        editNote(note);
                    }
                });
                mListNotes.setAdapter(mNotesAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void findViewsById(View container) {
        mListNotes =container.findViewById(R.id.listUserNotes);
        mListNotes.setHasFixedSize(false);
        mNotesManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        mListNotes.setLayoutManager(mNotesManager);
        mListNotes.setAdapter(mNotesAdapter);

        addNoteButton = container.findViewById(R.id.fabNote);
        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddNoteDialog("", new ColorDrawable(getResources().getColor(R.color.colorWhite)), false, "null");
            }
        });
    }

    // prepares info for showAddNoteDialog in editMode
    private void editNote(final Note note) {
        DatabaseReference notesRef = mUserRef.child(FirebaseEndpoint.NOTES);

        notesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if(snapshot.child("content").getValue().toString().equals(note.content)) {
                        String key = snapshot.getKey();
                        editMode = true;
                        showAddNoteDialog(note.content, new ColorDrawable(note.getColour()), note.pinned, key);
                        break;

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    // Shows prompt for adding note. Handles adding note to array list.
    private void showAddNoteDialog (String noteBody, Drawable background, boolean pinned, final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View viewInflated = LayoutInflater.from(getActivity()).inflate(R.layout.add_note, (ViewGroup) getActivity().findViewById(R.id.add_note), false);

        final EditText editTxtNote = viewInflated.findViewById(R.id.noteContent);
        editTxtNote.setText(noteBody);
        viewInflated.setBackground(background);

        final ToggleButton toggleBtnPin = viewInflated.findViewById(R.id.toggleBtnPin);
        toggleBtnPin.setChecked(pinned);

        final ImageButton deleteIB = viewInflated.findViewById(R.id.deleteIB);
        if(editMode) {
            deleteIB.setVisibility(View.VISIBLE);
            deleteIB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDeletePrompt(key);

                    // closes addnote/edit note prompt
                    if(currNoteDialog != null) {
                        currNoteDialog.dismiss();
                    }
                }
            });
        }

        final ImageButton ibWhite = viewInflated.findViewById(R.id.imgBtnWhite);
        ibWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currNoteDialog.cancel();
                showAddNoteDialog(editTxtNote.getText().toString(),
                                    new ColorDrawable(getResources().getColor(R.color.colorWhite)),
                                    toggleBtnPin.isChecked(), key);
            }
        });

        final ImageButton ibLightBlue = viewInflated.findViewById(R.id.imgBtnLightBlue);
        ibLightBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currNoteDialog.cancel();
                showAddNoteDialog(editTxtNote.getText().toString(),
                                    new ColorDrawable(getResources().getColor(R.color.colorLightBlue)),
                                    toggleBtnPin.isChecked(), key);
            }
        });

        final ImageButton ibPurple = viewInflated.findViewById(R.id.imgBtnPurple);
        ibPurple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currNoteDialog.cancel();
                showAddNoteDialog(editTxtNote.getText().toString(),
                                    new ColorDrawable(getResources().getColor(R.color.colorPurple)),
                                    toggleBtnPin.isChecked(), key);
            }
        });

        final ImageButton ibDarkBlue = viewInflated.findViewById(R.id.imgBtnDarkBlue);
        ibDarkBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currNoteDialog.cancel();
                showAddNoteDialog(editTxtNote.getText().toString(),
                                    new ColorDrawable(getResources().getColor(R.color.colorDarkBlue)),
                                    toggleBtnPin.isChecked(), key);
            }
        });

        builder.setView(viewInflated);

        String action;
        if(editMode) {
            action = "Edit";
        } else {
            action = "Post";
        }

        builder.setPositiveButton(action, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String noteBody = editTxtNote.getText().toString();
                boolean pinned = toggleBtnPin.isChecked();
                ColorDrawable bg = (ColorDrawable) viewInflated.getBackground();

                if (noteBody.equals("")) {
                    Toast.makeText(getActivity(), "Please enter a valid note before submitting.", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                    showAddNoteDialog(noteBody, viewInflated.getBackground(), pinned, key);
                } else {
                    if(!editMode) {
                        addNoteToDataBase(noteBody, toggleBtnPin.isChecked(), bg.getColor());
                        refreshFragment();
                    } else {
                        editNoteInDataBase(noteBody, toggleBtnPin.isChecked(), bg.getColor(), key);
                        refreshFragment();
                        editMode = false;
                    }
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        currNoteDialog = builder.create();
        currNoteDialog.show();
    }

    // Add note to database
    private void addNoteToDataBase(String noteBody, boolean pinned, int color) {
        Note note = new Note(color, noteBody, pinned);
        DatabaseReference notesRef = mUserRef.child(FirebaseEndpoint.NOTES);
        notesRef.push().setValue(note);
    }

    private void editNoteInDataBase(String noteBody, boolean pinned, int color, String key) {
        DatabaseReference notesRef = mUserRef.child(FirebaseEndpoint.NOTES);

        notesRef.child(key).child("content").setValue(noteBody);
        notesRef.child(key).child("pinned").setValue(pinned);
        notesRef.child(key).child("color").setValue(color);
    }

    private void openDeletePrompt(final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Note?");
        View viewInflated = LayoutInflater.from(getActivity()).inflate(R.layout.delete_note, (ViewGroup) getActivity().findViewById(R.id.delete_note), false);

        builder.setView(viewInflated);

        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteNoteFromDataBase(key);
                refreshFragment();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void deleteNoteFromDataBase(String key) {
        DatabaseReference notesRef = mUserRef.child(FirebaseEndpoint.NOTES).child(key);
        notesRef.removeValue();
    }

    private void refreshFragment() {
        mListNotes.getAdapter().notifyDataSetChanged(); // not working
    }
}
