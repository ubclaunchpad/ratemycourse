package com.example.coursify;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


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
    //private List<Note> allNotes;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference mUserRef;

    private RecyclerView mListNotes;
    private RecyclerView.Adapter mNotesAdapter;
    private RecyclerView.LayoutManager mNotesManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_note, container, false);
        //allNotes = new ArrayList<>(); // !!!
        findViewsById(view);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mUserRef = mDatabaseRef.child(FirebaseEndpoint.USERS)
                .child(Utils.processEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail()));

        populateUIFromDatabaseInfo();

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
                    String color = snapshot.child("color").getValue().toString();
                    String noteBody = snapshot.child("content").getValue().toString();
                    boolean pinned = (boolean) snapshot.child("pinned").getValue();
                    Note note = new Note(color, noteBody, pinned);
                    notes.add(note);
                }

                mNotesAdapter = new NoteAdapter(notes);
                mListNotes.setAdapter(mNotesAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void findViewsById(View container) {
        mListNotes =container.findViewById(R.id.listUserNotes);
        mListNotes.setHasFixedSize(true); // is it though? if size is not fixed, can note size vary depending on content?!!!
        mNotesManager = new LinearLayoutManager(getActivity()); // should it be relative layout since note positions vary?
        mListNotes.setLayoutManager(mNotesManager);
        mListNotes.setAdapter(mNotesAdapter);


        addNoteButton = container.findViewById(R.id.fabNote);
        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddNotePrompt();
            }
        });
    }

    // shows prompt for adding note. handles adding note to array list
    private void openAddNotePrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add a note");
        View viewInflated = LayoutInflater.from(getActivity()).inflate(R.layout.add_note, (ViewGroup) getActivity().findViewById(R.id.add_note), false);

        final EditText editTxtNoteBody = viewInflated.findViewById(R.id.noteContent);
        final CheckBox chkBoxPin = viewInflated.findViewById(R.id.pinCheckBox);

        builder.setView(viewInflated);
        builder.setPositiveButton("Add note", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String noteBody = editTxtNoteBody.getText().toString();

                if(noteBody.equals("")) {
                    Toast.makeText(getActivity(), "Please type a note.", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                    openAddNotePrompt();
                } else {
                    addNoteToDataBase(noteBody, chkBoxPin.isChecked()); // ADD COLOR AFTER (TO add_note AS WELL) imageview with on click?
                }
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

    // adds note to database
    private void addNoteToDataBase(String noteBody, boolean pinned) {
        // !!!
        Note note = new Note("COLOR", noteBody, pinned);
        DatabaseReference notesRef = mUserRef.child(FirebaseEndpoint.NOTES); // will this work ok?
        notesRef.push().setValue(note);
    }



    // adds note to allNotes
//    private void addNote(Note note) {
//        allNotes.add(note);
//    }
//
//    private void deleteNote(Note note) {
//        allNotes.remove(note);
//    }
//
//    // shows all notes from allNotes on notes screen
//    private void buildAllNotes() {
//
//    }


//
//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }
//
//    /**
//     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
//     * to the activity and potentially other fragments contained in that
//     * activity.
//     * <p>
//     * See the Android Training lesson <a href=
//     * "http://developer.android.com/training/basics/fragments/communicating.html"
//     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
}
