package com.example.coursify;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sveloso on 2018-01-20.
 */

public class CourseTabActivity extends AppCompatActivity {
    private static final String TAG = CourseTabActivity.class.getSimpleName();

    private TextView txtCourseTitle;
    private TextView txtCourseCode;

    private String courseCode;
    private String courseDept;
    private String courseId;
    private String courseTitle;

    private DatabaseReference mDatabase;
    private DatabaseReference mCourseReference;
    private DatabaseReference mUserRef;

    private String currUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_course_tabs);
        updateHeaderValuesAndVisitsField();

        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Course"));
        tabLayout.addTab(tabLayout.newTab().setText("Settings"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = findViewById(R.id.pager);
        final PagerAdapter adapter = new CourseTabAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        tabLayout.setTabTextColors(getResources().getColor(R.color.colorViolet), getResources().getColor(R.color.colorViolet));

        getUserName();
    }

    private void updateHeaderValuesAndVisitsField() {
        courseCode = getIntent().getStringExtra("COURSE_CODE");
        try {
            courseDept = courseCode.split(" ")[0];
            courseId = courseCode.split(" ")[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            Toast.makeText(this, "Failed to find course with given course code.", Toast.LENGTH_SHORT).show();
            finish();
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mCourseReference = Utils.getCourseReferenceToDatabase(courseCode, mDatabase);

        if(mCourseReference == null){
            Log.v(TAG, "this course does not exist");
            return;
        }

        txtCourseCode = findViewById(R.id.txtCourseCode);
        txtCourseTitle = findViewById(R.id.txtCourseTitle);
        txtCourseCode.setText(courseCode);

        mCourseReference.child(FirebaseEndpoint.DESCRIPTION).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String s = snapshot.getValue().toString();

                if (s == null) {
                    Toast.makeText(CourseTabActivity.this, "Failed to find course with given course code.", Toast.LENGTH_SHORT).show();
                    CourseTabActivity.this.finish();
                } else {
                    txtCourseTitle.setText(s);
                    courseTitle = s;
                    incrementVisits(mCourseReference);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void incrementVisits(final DatabaseReference mCourseReference){
        Log.v(TAG, "I am at incrementVisits");
        mCourseReference.child(FirebaseEndpoint.VISITS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int visits = 0;
                if(snapshot.exists()){
                    visits = Integer.parseInt(snapshot.getValue().toString());
                }
                mCourseReference.child(FirebaseEndpoint.VISITS).setValue(visits+1);
                updatePopularCount(mCourseReference, 1);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void updatePopularCount(final DatabaseReference mCourseReference, final int value){
        mCourseReference.child(FirebaseEndpoint.POPULARCOUNT).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int popularCount = 0;
                if(snapshot.exists()){
                    popularCount = Integer.parseInt(snapshot.getValue().toString());
                }
                mCourseReference.child(FirebaseEndpoint.POPULARCOUNT).setValue(popularCount+value);
                updatePopularCourses(popularCount+value);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void updatePopularCourses(final int popularCount){
        Log.v(TAG, "I am at updatePopularCourses");
        final DatabaseReference mPopularCourseRef = mDatabase.child("PopularCourses");
        mPopularCourseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList<HashMap<String, String>> popularCourses = snapshot.getValue() == null ?
                        new ArrayList<HashMap<String, String>>() :
                        (ArrayList<HashMap<String, String>>) snapshot.getValue();
                HashMap<String, String> newCourse;
                newCourse = new HashMap<String, String>();
                newCourse.put("courseCode", courseCode);
                newCourse.put("courseTitle", courseTitle);
                newCourse.put("popularCount", Integer.toString(popularCount));
                if(popularCourses.size() == 0){
                    popularCourses.add(newCourse);
                }

                int minIndex = -1;
                int minPopularity = Integer.MAX_VALUE;
                for(int i = 0; i < popularCourses.size(); i++){
                    HashMap<String, String> currCourse = popularCourses.get(i);
                    int currPopularCount = Integer.parseInt(currCourse.get("popularCount"));
                    String currCourseId = currCourse.get("courseCode");
                    // if we find that the newly added course is already in popular,
                    // we simple update the field and return;
                    Log.v(TAG, "currCourseCode + courseId = " + currCourseId + ", " + courseCode);
                    if(currCourseId.equals(courseCode)){
                        newCourse.put("popularCount", Integer.toString(popularCount));
                        popularCourses.set(i, newCourse);
                        mPopularCourseRef.setValue(popularCourses);
                        return;
                    }
                    // finds the minimum index;
                    if(currPopularCount < minPopularity){
                        minPopularity = currPopularCount;
                        minIndex = i;
                    }
                }

                if(popularCount >= minPopularity && popularCourses.size() >= 5){
                    popularCourses.remove(minIndex);
                }
                // if the current popular count is greater than min, then we insert it and remove
                // the other one
                if(popularCount >= minPopularity){
                    popularCourses.add(newCourse);
                }

                mPopularCourseRef.setValue(popularCourses);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getUserName() {
        mUserRef = mDatabase.child(FirebaseEndpoint.USERS)
                .child(Utils.processEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail()));

        mUserRef.child(FirebaseEndpoint.NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currUserName =  dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}