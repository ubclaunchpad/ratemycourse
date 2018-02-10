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
import java.util.List;

public class SearchFragment extends Fragment {
    EditText courseInput;
    Button submit;
    String email, processedEmail;
    private static final String TAG = SearchFragment.class.getSimpleName();
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    DatabaseReference ref, refSearch;

    RecyclerView recView;
    RecyclerView.Adapter searchCourseAdapter;

    ArrayList<String> searchedCourses = new ArrayList<>();
    ArrayList<String> searchedCoursesDescript = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // authenticates user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        boolean emailVerified = user.isEmailVerified();
        if(!emailVerified) {
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

        setFields(processedEmail);

        // displaying courses:
        getRecentlySearchedAndSaveCourse("");

        LinearLayoutManager LLM = new LinearLayoutManager(getActivity());
        recView.setLayoutManager(LLM);
        // searchCourseAdapter = new SearchCourseAdapter(searchedCourses, this);
        // recView.setAdapter(searchCourseAdapter);
        // when clicking submit, we want to modify our recentlySearched field and jump to the course
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String courseId = courseInput.getText().toString();
                getRecentlySearchedAndSaveCourse(courseId.toUpperCase());
            }
        });

        return view;
    }

    protected void setFields(String processedEmail) {
        this.ref = database.child(FirebaseEndpoint.USERS).child(processedEmail);
        this.refSearch = ref.child(FirebaseEndpoint.RECENTLY_SEARCHED);
    }

    /*
        If courseId is empty, that means we simply want to display all courses instead of searching for a course
        Therefore we must:
        1. get all the previously searched courses
        2. get all the descriptions of the searched courses
        3. display these courses

        This function gets an arraylist of all the previously searched courses, then sends to its callback
        getSearchedDescript
     */
    protected void getRecentlySearchedAndSaveCourse(final String courseId) {

        refSearch.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> res = null;
                if (dataSnapshot.getValue() != null) {
                    res = Utils.processCourses((ArrayList<String>) dataSnapshot.getValue());
                }
                getSearchedDescript(courseId, res);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    /*
    Callback for getRecentlySearchedAndSaveCourses
    As previously mentioned, we only want to display the courses if courseId is empty
    Therefore if courseId is empty, we
    1. get an arraylist of course descriptions by looping
    2. calls the callback, which only starts displaying when we have completed searching for all
    descriptions simply by checking whether the description length is equal to the course length

    If courseId is not empty, we skip this process by trying to get the course description
     */
    public void getSearchedDescript(final String courseId, ArrayList<String> recentlySearched){
        Log.v(TAG, courseId);

        if (recentlySearched == null){
            recentlySearched = new ArrayList<>();
        }
        if (courseId.length() > 0){
            getSearchedDescriptCallback(courseId, recentlySearched);
            return;
        }
        for (int i = 0; i < recentlySearched.size(); i++){
            String courseCode = recentlySearched.get(i);
            Log.v(TAG, "here's coursecode " + courseCode);
            DatabaseReference courseRef = Utils.getCourseReferenceToDatabase(courseCode, database);
            if (courseRef != null) {
                final ArrayList<String> finalRecentlySearched = recentlySearched;
                courseRef.child(FirebaseEndpoint.DESCRIPTION).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        searchedCoursesDescript.add((String) dataSnapshot.getValue());
                        getSearchedDescriptCallback(courseId, finalRecentlySearched);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "onCancelled", databaseError.toException());
                    }
                });
            }
        }
    }

    /*
    Callback for getSearchedDescript
    This function:
    0. only starts working when we have retrieved all descriptions by checking the lengths of the
    two arraylists
    1. if courseId is empty, we simply display the course and the description and returns
    2. if courseId is not empty, we look at the recentlySearched Arraylist and see if the new
    searched course is inside this array
    3. If the newly searched course is inside this array, we remove it then add it again so it gets
    bumped to the newest one
    4. if not, we simply just add it
    5. finally starts a new courseActivity intent
     */
    public void getSearchedDescriptCallback(String courseId, ArrayList<String> recentlySearched){
        if(searchedCoursesDescript.size() != recentlySearched.size()){
            return;
        }
        if(courseId.length() == 0){
            searchedCourses = recentlySearched;
            searchCourseAdapter = new SearchCourseAdapter(searchedCourses, searchedCoursesDescript, getActivity());
            recView.setAdapter(searchCourseAdapter);
            return;
        }

        for(int i = 0; i < recentlySearched.size(); i++) {
            if (recentlySearched.get(i).equals(courseId)) {
                recentlySearched.remove(i);
            }
        }
        //todo course id validator
        recentlySearched.add(courseId);
        refSearch.setValue(recentlySearched);
        Intent intent = new Intent(getActivity(), CourseTabActivity.class);
        intent.putExtra("COURSE_CODE", courseId.toUpperCase());
        startActivity(intent);
    }
}

/* Additional Notes:
- the reason I have implemented this way is because we need the array "recentlySearched" for everything:
1. if we were to search a course, we need to place this course in the correct position in this list
2. if we were to display the history, we need to:
-- display the courseId itself
-- get the description from the courseId and display the description
All of these can only happen AFTER we get the entire recentlySearched
And getting the complete list of descriptions only happens AFTER description.size() === recentlySearched.size()
We don't really need the descriptions, only recentlySearched, if we were to press the submit button

This maximizes code sharing and logical order
 */
