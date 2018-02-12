package com.example.coursify;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchFragment extends Fragment {
    //Class information
    private static final String TAG = SearchFragment.class.getSimpleName();

    //View information
    EditText courseInput;
    Button submit;
    RecyclerView recView;
    RecyclerView.Adapter searchCourseAdapter;

    //User information
    String email, processedEmail;

    //Database information
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Authenticates user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        boolean emailVerified = user.isEmailVerified();
        if (!emailVerified) {
            return;
        }
        email = user.getEmail();
        processedEmail = Utils.processEmail(email);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        submit = view.findViewById(R.id.submit);
        courseInput = view.findViewById(R.id.searchField);
        recView = view.findViewById(R.id.listSearchCourses);


        LinearLayoutManager LLM = new LinearLayoutManager(getActivity());
        recView.setLayoutManager(LLM);

        displayCourses();

        // Submit functionality
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String processedCourseId = Utils.courseCodeFormatter(courseInput.getText().toString());
                onSubmit(processedCourseId);
            }
        });

        return view;
    }

    public void onSubmit(final String processedCourseId){
        Log.v(TAG, "onSubmit + processedCourseId = " + processedCourseId);
        DatabaseReference courseRef = Utils.getCourseReferenceToDatabase(processedCourseId, database);
        if(courseRef == null) {
            Log.v(TAG, "no course exists");
            return;
        }

        // get Description of this course and save its ID;
        courseRef.child("Description").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Course course = new Course(processedCourseId, (String) dataSnapshot.getValue());
                saveAndJumpToCourse(course);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void saveAndJumpToCourse(final Course course){
        Log.v(TAG, "saveAndJumpToCourses + courseId + courseDescript = " + course.courseCode + ", " + course.courseTitle);
        final DatabaseReference recentlySearchedRef = database.child(FirebaseEndpoint.USERS).child(processedEmail).child(FirebaseEndpoint.RECENTLY_SEARCHED);
        recentlySearchedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<HashMap<String, String>> recentlySearchedCourses =
                        dataSnapshot.getValue() == null ? new ArrayList<HashMap<String, String>>() : (ArrayList<HashMap<String, String>>)dataSnapshot.getValue();
                for(int i = 0; i < recentlySearchedCourses.size(); i++){
                    HashMap<String, String> currCourse = recentlySearchedCourses.get(i);
                    String currCourseId = currCourse.get("courseCode");
                    if(currCourseId.equals(course.courseCode)){
                        recentlySearchedCourses.remove(i);
                    }
                }
                HashMap<String, String> courseMap = new HashMap<String, String>();
                courseMap.put("courseCode", course.courseCode);
                courseMap.put("courseTitle", course.courseTitle);
                recentlySearchedCourses.add(courseMap);
                recentlySearchedRef.setValue(recentlySearchedCourses);

                Intent intent = new Intent(getActivity(), CourseTabActivity.class);
                intent.putExtra("COURSE_CODE", course.courseCode);
                startActivity(intent);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void displayCourses(){
        final DatabaseReference recentlySearchedRef = database.child(FirebaseEndpoint.USERS).child(processedEmail).child(FirebaseEndpoint.RECENTLY_SEARCHED);
        recentlySearchedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<HashMap<String, String>> recentlySearchedCourses =
                        dataSnapshot.getValue() == null ? new ArrayList<HashMap<String, String>>() : (ArrayList<HashMap<String, String>>)dataSnapshot.getValue();
                ArrayList<String> recentlySearchedIds = new ArrayList<String>();
                ArrayList<String> recentlySearchedDescripts = new ArrayList<String>();

                for(int i = 0; i < recentlySearchedCourses.size(); i++){
                    HashMap<String, String> currCourse = recentlySearchedCourses.get(i);
                    String currCourseId = currCourse.get("courseCode");
                    String currCourseDescript = currCourse.get("courseTitle");
                    recentlySearchedIds.add(currCourseId);
                    recentlySearchedDescripts.add(currCourseDescript);
                }

                searchCourseAdapter = new SearchCourseAdapter(recentlySearchedIds, recentlySearchedDescripts, getActivity());
                recView.setAdapter(searchCourseAdapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
/*
NOTE: changed implementation; now the workflow is:
1. when user searches for a course, we look for the reference (the function in Utils)
-- if reference = null, then course does not exist and we do it in Utils
-- if reference != null then we:
2. after getting the course reference, we can go to the course and get its description
3. after getting the description, we save both the courseCode AND description into recentlySearched

4. when viewing, we retrieve recentlySearched, which already contains courseCode AND description

TAG ALONG: when user searches for a course, first thing we do is process the courseID (new function in Utils)
Therefore everything saved in Firebase contains a "space"
 */
