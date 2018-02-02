package com.example.coursify;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dgreenhalgh.android.simpleitemdecoration.*;
import com.dgreenhalgh.android.simpleitemdecoration.linear.DividerItemDecoration;
import com.dgreenhalgh.android.simpleitemdecoration.linear.EndOffsetItemDecoration;
import com.dgreenhalgh.android.simpleitemdecoration.linear.StartOffsetItemDecoration;
import com.example.coursify.Course;
import com.example.coursify.CourseAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseUser;


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

    TextView emptyRecentlyOpened;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // User is not signed in
            return;
        }
        email = user.getEmail();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeFirebase();
        initializeCourses();

        mListRecentlyOpened = findViewById(R.id.listRecentlyOpened);
        mListRecommended = findViewById(R.id.listRecommended);
        mListPopular = findViewById(R.id.listPopular);
        mListRecommended.setHasFixedSize(true);
        mListRecentlyOpened.setHasFixedSize(true);
        mListPopular.setHasFixedSize(true);

        mRecentlyOpenedManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecommendedManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mPopularManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        mListRecentlyOpened.setLayoutManager(mRecentlyOpenedManager);
        mListRecommended.setLayoutManager(mRecommendedManager);
        mListPopular.setLayoutManager(mPopularManager);

        mRecentlyOpenedAdapter = new CourseAdapter(listRecentlyOpened, getResources().getColor(R.color.colorRecentlyOpened));
        mRecommendedAdapter = new CourseAdapter(listRecommended, getResources().getColor(R.color.colorRecommended));
        mPopularAdapter = new CourseAdapter(listRecommended, getResources().getColor(R.color.colorPopular));
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

        emptyRecentlyOpened = findViewById(R.id.emptyRecentlyOpened);

        getRecentlyOpenedFromDatabase();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getRecentlyOpenedFromDatabase();
    }


    /**
     * todo remove this after implementing recommended and popular
     */
    private void initializeCourses() {
        Course c1 = new Course("CPSC 110", "Differential Calculus with Applications to Physical Sciences and Engineering");
        Course c2 = new Course("ONCO 649", "Doctoral Dissertation");
        Course c3 = new Course("CPSC 210", "CPSC 210 L1K (Laboratory)");
        Course c4 = new Course("PLAN 425", "Urban Planning Issues and Concepts");
        Course c5 = new Course("LARC 415", "The Profession of Planning");
        Course c6 = new Course("LASO 204", "The Profession of Planning");
        Course c7 = new Course("LATN 102", "The Profession of Planning");
        Course c8 = new Course("LIBE 465", "The Profession of Planning");
        Course c9 = new Course("OBST 430", "The Profession of Planning");
        Course c10 = new Course("OBST 649", "The Profession of Planning");

        listRecentlyOpened = new ArrayList<>();

        listRecommended = new ArrayList<>();
        listRecommended.add(c1);
        listRecommended.add(c3);
        listRecommended.add(c2);
        listRecommended.add(c4);
        listRecommended.add(c5);
        listRecommended.add(c6);
        listRecommended.add(c7);
        listRecommended.add(c8);
        listRecommended.add(c9);
        listRecommended.add(c10);
    }

    private void initializeFirebase() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    public void showProfilePage(View view) {
        startActivity(new Intent(this, ProfileActivity.class));
    }

    /**
     * Helper function for retrieving all items in list in correct order
     * @param recentlyOpened a list that contains at least one item
     * @param coursePos
     */
    private void getRecentlyOpenedHelper(final List<String> recentlyOpened, final int coursePos) {
        if (coursePos == recentlyOpened.size()) {
            return;
        }

        final String courseCode = recentlyOpened.get(coursePos);
        // Assumes the course code exists
        DatabaseReference courseRef = Utils.getCourseReferenceToDatabase(courseCode, mDatabase);
        courseRef.child(FirebaseEndpoint.DESCRIPTION)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String description = dataSnapshot.getValue(String.class);
                        Course course = new Course(courseCode, description);
                        listRecentlyOpened.add(course);
                        mRecentlyOpenedAdapter.notifyDataSetChanged();
                        getRecentlyOpenedHelper(recentlyOpened, coursePos + 1);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    /**
     * Retrieve recently opened list from Firebase and display it on UI
     */
    private void getRecentlyOpenedFromDatabase() {
        Log.v(TAG, "getting recently opened from database");
        listRecentlyOpened.clear();
        mRecentlyOpenedAdapter.notifyDataSetChanged();

        DatabaseReference recentlyOpenedRef =
                mDatabase.child(FirebaseEndpoint.USERS)
                        .child(Utils.processEmail(mAuth.getCurrentUser().getEmail()))
                        .child(FirebaseEndpoint.RECENTLY_OPENED_COURSES);
        recentlyOpenedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    emptyRecentlyOpened.setVisibility(View.GONE);
                    /* Gets a list of course code strings */
                    GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {};
                    List<String> recentlyOpened = dataSnapshot.getValue(genericTypeIndicator);
                    for(String course : recentlyOpened) {
                        Log.v(TAG, course);
                    }
                    Collections.reverse(recentlyOpened);
                    getRecentlyOpenedHelper(recentlyOpened, 0);
                }
                else {
                    emptyRecentlyOpened.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void showProfileSettings(View view){
        Intent profSetting = new Intent(getApplicationContext(), ProfileSettings.class);
        Bundle bundle = new Bundle();
        bundle.putString("Email", email);
        profSetting.putExtras(bundle);
        Log.v(TAG, "proceeding to profile settings activity");
        startActivity(profSetting);

    }
}