package com.example.coursify;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sveloso on 2018-01-20.
 */
public class CourseSettingsFragment extends Fragment {
    private static final String TAG = CourseSettingsFragment.class.getSimpleName();
    private Spinner spinnerCoursePreference;
    private TextView txtCommentBody;
    private TextView txtUsefulness;
    private TextView txtEasiness;
    private String preference;
    private DatabaseReference mDatabase;
    private DatabaseReference mUserRef;
    private DatabaseReference mCourseRef;
    private String currUserName;

    private ImageButton btnEditComment;
    private ImageButton btnEditRating;

    private String courseCode;
    private String courseTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_settings, container, false);

        courseCode = getActivity().getIntent().getStringExtra("COURSE_CODE");

        spinnerCoursePreference = view.findViewById(R.id.course_preference);
        final String[] preferences = {"No preference", "Taken", "Going to take", "Interested"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item,  preferences);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUserRef = mDatabase.child(FirebaseEndpoint.USERS)
                .child(Utils.processEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail()));
        mCourseRef = Utils.getCourseReferenceToDatabase(courseCode, mDatabase);

        getCoursePreference();
        spinnerCoursePreference.setAdapter(adapter);

        getCourseTitle();

        spinnerCoursePreference.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String newPreference;
                switch (i) {
                    case 0:
                        removeCourseFromOldPreference();
                        preference = "noPreference";
                        break;
                    case 1:
                        newPreference = "taken";
                        updateCoursePreference(newPreference);
                        break;
                    case 2:
                        newPreference = "goingToTake";
                        updateCoursePreference(newPreference);
                        break;
                    case 3:
                        newPreference = "interested";
                        updateCoursePreference(newPreference);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        txtCommentBody = view.findViewById(R.id.txtCommentBody);
        txtCommentBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editComment();
            }
        });
        txtUsefulness = view.findViewById(R.id.txtUsefulnessRating);
        txtEasiness = view.findViewById(R.id.txtEasinessRating);

        btnEditComment = view.findViewById(R.id.btnEditComment);
        btnEditComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editComment();
            }
        });
        btnEditRating = view.findViewById(R.id.btnEditRating);
        btnEditRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editRating();
            }
        });

        getUserNameThenUserComment();

        return view;
    }

    private void getCourseTitle(){
        if(mCourseRef == null) {
            Log.v(TAG, "mCourseRef is null");
        }
        mCourseRef.child(FirebaseEndpoint.DESCRIPTION).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                courseTitle = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    // Finds the user's comment in database, then prompt user to edit it
    private void editComment () {
        mCourseRef.child(FirebaseEndpoint.COMMENTS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("author").getValue().toString().equals(currUserName)) {
                        String key = snapshot.getKey();
                        promptUserForUpdatedComment(key);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    // Prompt user to edit comment, then write it to database
    private void promptUserForUpdatedComment (final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit your comment");
        View viewInflated = LayoutInflater.from(getActivity()).inflate(R.layout.edit_comment, (ViewGroup) getActivity().findViewById(R.id.edit_comment), false);

        final EditText editTxtCommentBody = viewInflated.findViewById(R.id.editTxtCommentBody);
        editTxtCommentBody.setText(txtCommentBody.getText().toString());
        final CheckBox chkBoxAnon = viewInflated.findViewById(R.id.chkBoxAnon);

        builder.setView(viewInflated);
        builder.setPositiveButton("Edit Comment", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String commentBody = editTxtCommentBody.getText().toString();
                boolean anonymous = chkBoxAnon.isChecked();

                if (commentBody.equals("")) {
                    Toast.makeText(getActivity(), "Please enter a comment before submitting.", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                } else {
                    editCommentInDatabase(key, commentBody, anonymous);
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

    // Write updated comment to database
    private void editCommentInDatabase (String key, String commentBody, boolean anonymous) {
        mCourseRef.child(FirebaseEndpoint.COMMENTS).child(key).child("commentBody").setValue(commentBody);
        mCourseRef.child(FirebaseEndpoint.COMMENTS).child(key).child("anonymity").setValue(anonymous);
    }


    // Finds the user's rating in database, then prompt user to edit it
    private void editRating () {
        mCourseRef.child(FirebaseEndpoint.RATINGS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("author").getValue().toString().equals(currUserName)) {
                        String key = snapshot.getKey();
                        promptUserForUpdatedRating(key);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    // Prompt user to edit rating, then write it to database
    private void promptUserForUpdatedRating (final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit your rating");
        View viewInflated = LayoutInflater.from(getActivity()).inflate(R.layout.add_rating, (ViewGroup) getActivity().findViewById(R.id.add_rating), false);

        final NumberPicker npEasiness = viewInflated.findViewById(R.id.numPickerEasiness);
        npEasiness.setMinValue(0);
        npEasiness.setMaxValue(10);
        final NumberPicker npUsefulness = viewInflated.findViewById(R.id.numPickerUsefulness);
        npUsefulness.setMinValue(0);
        npUsefulness.setMaxValue(10);

        builder.setView(viewInflated);
        builder.setPositiveButton("Edit Rating", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int easinessRating = npEasiness.getValue();
                int usefulnessRating = npUsefulness.getValue();
                editRatingInDatabase(key, easinessRating, usefulnessRating);
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

    // Write updated comment to database
    private void editRatingInDatabase (String key, int easinessRating, int usefulnessRating) {
        mCourseRef.child(FirebaseEndpoint.RATINGS).child(key).child("easiness").setValue(easinessRating);
        mCourseRef.child(FirebaseEndpoint.RATINGS).child(key).child("usefulness").setValue(usefulnessRating);
    }

    private void getUserNameThenUserComment() {
        mUserRef.child(FirebaseEndpoint.NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currUserName = dataSnapshot.getValue().toString();
                getUserCommentAndRating (currUserName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void getUserCommentAndRating (final String userName) {
        mCourseRef.child(FirebaseEndpoint.COMMENTS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("author").getValue().toString().equals(userName)) {
                        txtCommentBody.setText(snapshot.child("commentBody").getValue().toString());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mCourseRef.child(FirebaseEndpoint.RATINGS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("author").getValue().toString().equals(currUserName)) {
                        txtEasiness.setText(snapshot.child("easiness").getValue().toString());
                        txtUsefulness.setText(snapshot.child("usefulness").getValue().toString());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getCoursePreference () {
        preference = "noPreference";

        DatabaseReference goingToTakeRef = mUserRef
                        .child(FirebaseEndpoint.GOING_TO_TAKE);

        goingToTakeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> goingToTakeList;
                if (dataSnapshot.exists()) { /* Gets a list of course code strings in user previous preference */
                    GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {};

                    goingToTakeList = dataSnapshot.getValue(genericTypeIndicator);

                    for (String goingToTakeCourse : goingToTakeList) {
                        if (goingToTakeCourse.equals(courseCode)) {
                            preference = "goingToTake";
                            updateSpinnerSelection();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        DatabaseReference takenRef = mUserRef
                        .child(FirebaseEndpoint.TAKEN);

        takenRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> takenList;
                if (dataSnapshot.exists()) { /* Gets a list of course code strings in user previous preference */
                    GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {};

                    takenList = dataSnapshot.getValue(genericTypeIndicator);

                    for (String takenCourse : takenList) {
                        if (takenCourse.equals(courseCode)) {
                            preference = "taken";
                            updateSpinnerSelection();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        DatabaseReference interestedRef = mUserRef
                        .child(FirebaseEndpoint.INTERESTED);

        interestedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> interestedList;
                if (dataSnapshot.exists()) { /* Gets a list of course code strings in user previous preference */
                    GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {};

                    interestedList = dataSnapshot.getValue(genericTypeIndicator);

                    for (String interestedCourse : interestedList) {
                        if (interestedCourse.equals(courseCode)) {
                            preference = "interested";
                            updateSpinnerSelection();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void updateSpinnerSelection() {
        if (preference.equals("taken")) {
            spinnerCoursePreference.setSelection(1);
        } else if (preference.equals("goingToTake")) {
            spinnerCoursePreference.setSelection(2);
        } else if (preference.equals("interested")){
            spinnerCoursePreference.setSelection(3);
        } else {
            spinnerCoursePreference.setSelection(0);
        }
    }

    private void updateCoursePreference (final String newPreference) {
        if (!preference.equals(newPreference)) { // if new preference is different from the old preference
            if (!preference.equals("noPreference")) { // if there is a valid previous preference
                removeCourseFromOldAndAddCourseToNewPreference (newPreference); // remove this course from that preference list
            } else {
                addCourseToNewPreferenceSetting(newPreference); // if previous preference was simply "no preference", no need to remove and add straight away to new
                Utils.updatePopularCount(4, courseCode, courseTitle);
            }
        }
    }

    private void removeCourseFromOldPreference () {
        final DatabaseReference previousPreferenceRef = mUserRef.child(preference);

        previousPreferenceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> previousPreferenceList;
                if (dataSnapshot.exists()) { /* Gets a list of course code strings in user previous preference */
                    GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {};

                    previousPreferenceList = dataSnapshot.getValue(genericTypeIndicator);

                    previousPreferenceList.remove(courseCode); // Remove this course from user previous preference
                    previousPreferenceRef.setValue(previousPreferenceList);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void removeCourseFromOldAndAddCourseToNewPreference (final String newPreference) {
        final DatabaseReference previousPreferenceRef = mUserRef.child(preference);

        previousPreferenceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> previousPreferenceList;
                if (dataSnapshot.exists()) { /* Gets a list of course code strings in user previous preference */
                    GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {};

                    previousPreferenceList = dataSnapshot.getValue(genericTypeIndicator);

                    previousPreferenceList.remove(courseCode); // Remove this course from user previous preference
                    previousPreferenceRef.setValue(previousPreferenceList);

                    if (!newPreference.equals("noPreference")) { // No need to add to preference list if no preference
                        addCourseToNewPreferenceSetting(newPreference);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void addCourseToNewPreferenceSetting (String newPreference) {
        final DatabaseReference newPreferenceRef = mUserRef.child(newPreference);

        newPreferenceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> newPreferenceList;
                if (dataSnapshot.exists()) { /* Gets a list of course code strings in user previous preference */
                    GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {};

                    newPreferenceList = dataSnapshot.getValue(genericTypeIndicator);

                } else {
                    newPreferenceList = new ArrayList<>();
                }

                newPreferenceList.add(courseCode); // Add this course to the new preference
                newPreferenceRef.setValue(newPreferenceList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}