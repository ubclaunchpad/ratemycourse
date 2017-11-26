package com.example.coursify;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.dgreenhalgh.android.simpleitemdecoration.*;
import com.dgreenhalgh.android.simpleitemdecoration.linear.DividerItemDecoration;
import com.dgreenhalgh.android.simpleitemdecoration.linear.EndOffsetItemDecoration;
import com.dgreenhalgh.android.simpleitemdecoration.linear.StartOffsetItemDecoration;
import com.example.coursify.Course;
import com.example.coursify.CourseAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Created by sveloso on 2017-11-04.
 */
public class HomeActivity extends Activity {

    private static final String TAG = HomeActivity.class.getSimpleName();
    private List<Course> listRecentlyOpened;
    private List<Course> listRecommended;

    private RecyclerView mListRecentlyOpened;
    private RecyclerView.Adapter mRecentlyOpenedAdapter;
    private RecyclerView.LayoutManager mRecentlyOpenedManager;

    private RecyclerView mListRecommended;
    private RecyclerView.Adapter mRecommendedAdapter;
    private RecyclerView.LayoutManager mRecommendedManager;

    private RecyclerView mListPopular;
    private RecyclerView.Adapter mPopularAdapter;
    private RecyclerView.LayoutManager mPopularManager;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeFirebase();
        initializeCourses();

        mListRecentlyOpened = (RecyclerView) findViewById(R.id.listRecentlyOpened);
        mListRecommended = (RecyclerView) findViewById(R.id.listRecommended);
        mListPopular = (RecyclerView) findViewById(R.id.listPopular);
        mListRecommended.setHasFixedSize(true);
        mListRecentlyOpened.setHasFixedSize(true);
        mListPopular.setHasFixedSize(true);

        mRecentlyOpenedManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecommendedManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mPopularManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        mListRecentlyOpened.setLayoutManager(mRecentlyOpenedManager);
        mListRecommended.setLayoutManager(mRecommendedManager);
        mListPopular.setLayoutManager(mPopularManager);

        mRecentlyOpenedAdapter = new CourseAdapter(listRecentlyOpened);
        mRecommendedAdapter = new CourseAdapter(listRecommended);
        mPopularAdapter = new CourseAdapter(listRecommended);
        mListRecentlyOpened.setAdapter(mRecentlyOpenedAdapter);
        mListRecommended.setAdapter(mRecommendedAdapter);
        mListPopular.setAdapter(mPopularAdapter);

        DividerItemDecoration itemDivider = new DividerItemDecoration(ContextCompat.getDrawable(getApplicationContext(), R.drawable.divider));
        mListRecentlyOpened.addItemDecoration(itemDivider);
        mListRecentlyOpened.addItemDecoration(new StartOffsetItemDecoration(30));
        mListRecentlyOpened.addItemDecoration(new EndOffsetItemDecoration(30));

        mListRecommended.addItemDecoration(itemDivider);
        mListRecommended.addItemDecoration(new StartOffsetItemDecoration(30));
        mListRecommended.addItemDecoration(new EndOffsetItemDecoration(30));


        mListPopular.addItemDecoration(itemDivider);
        mListPopular.addItemDecoration(new StartOffsetItemDecoration(30));
        mListPopular.addItemDecoration(new EndOffsetItemDecoration(30));
    }

    @Override
    protected void onStart() {
        super.onStart();
        getRecentlyOpenedFromDatabase(); //TODO move it somewhere else, this is causing duplicate entries
    }

    private void initializeCourses() {
        Course c1 = new Course("MATH 100", "Differential Calculus with Applications to Physical Sciences and Engineering");
        Course c2 = new Course("CPSC 110", "Computation, Programs, and Programming");
        Course c3 = new Course("CPSC 210", "CPSC 210 L1K (Laboratory)");
        listRecentlyOpened = new ArrayList<>();
        listRecommended = new ArrayList<>();
        listRecommended.add(c1);
//        listRecentlyOpened.add(c2);
//        listRecentlyOpened.add(c3);


    }

    private void initializeFirebase() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    public void showProfilePage(View view) {
        startActivity(new Intent(this, ProfileActivity.class));
    }


    private void getRecentlyOpenedFromDatabase() {
        DatabaseReference recentlyOpenedRef =
                mDatabase.child(FirebaseEndpoint.USERS)
                        .child(Utils.processEmail(mAuth.getCurrentUser().getEmail()))
                        .child(FirebaseEndpoint.RECENTLY_OPENED_COURSES);
        recentlyOpenedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    /* Gets a list of course code strings */
                    GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {};
                    List<String> recentlyOpened = dataSnapshot.getValue(genericTypeIndicator);
                    Collections.reverse(recentlyOpened);
                    /* Find the corresponding course description */
                    for(int i  = 0; i < recentlyOpened.size(); i++) {
                        String courseCode = recentlyOpened.get(i);
                        // Assumes the course code exists
                        final String processedCourseCode = Utils.processCourseCode(courseCode);
                        DatabaseReference courseRef = getCourseReferenceToDatabase(processedCourseCode);
                        courseRef.child(FirebaseEndpoint.DESCRIPTION)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String description = dataSnapshot.getValue(String.class);
                                Course course = new Course(processedCourseCode, description);
                                listRecentlyOpened.add(course);
                                mRecentlyOpenedAdapter.notifyItemInserted(listRecentlyOpened.size() - 1);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    /**
     * Given a course code, return reference to it in Firebase
     * @param courseCode in the format of "CPSC 110"
     * @return
     */
    private DatabaseReference getCourseReferenceToDatabase(String courseCode) {
        String courseDept = courseCode.split(" ")[0];
        String courseId = courseCode.split(" ")[1];

        DatabaseReference subjectRef = mDatabase.child(FirebaseEndpoint.COURSES).child(courseDept);
        DatabaseReference yearRef = subjectRef.child("Year " + courseId.charAt(0));
        Log.v(TAG, "getting course reference, course code is: " + courseDept + courseId);
        return yearRef.child(courseDept + courseId);
    }
}