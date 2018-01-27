package com.example.coursify;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

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

    private Spinner spinnerCoursePreference;
    private TextView txtCommentBody;
    private TextView txtUsefulness;
    private TextView txtEasiness;
    private String preference;
    private DatabaseReference mDatabase;
    private DatabaseReference mUserRef;
    private DatabaseReference mCourseRef;
    private String currUserName;

    private String courseCode;

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
        txtUsefulness = view.findViewById(R.id.txtUsefulnessRating);
        txtEasiness = view.findViewById(R.id.txtEasinessRating);

        getUserNameThenUserComment();

        return view;
    }

    private void getUserNameThenUserComment() {
        mUserRef.child(FirebaseEndpoint.NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currUserName =  dataSnapshot.getValue().toString();
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