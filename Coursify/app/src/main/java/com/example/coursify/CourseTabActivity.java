package com.example.coursify;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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

public class CourseTabActivity extends AppCompatActivity {
    private static final String TAG = CourseTabActivity.class.getSimpleName();

    private TextView txtCourseTitle;
    private TextView txtCourseCode;

    private ImageButton btnBack;

    private String courseCode;
    private String courseDept;
    private String courseId;
    private String courseTitle;

    private DatabaseReference mDatabase;
    private DatabaseReference mCourseReference;
    private DatabaseReference mUserRef, mBookmarkRef;

    private String currUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_course_tabs);
        updateHeaderValuesAndVisitsField();

        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Course"));
        tabLayout.addTab(tabLayout.newTab().setText("Settings"));
        tabLayout.addTab(tabLayout.newTab().setText("Friends"));
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

        final ToggleButton toggle = findViewById(R.id.bookmark);
        mUserRef = mDatabase.child(FirebaseEndpoint.USERS)
                .child(Utils.processEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail()));
        mBookmarkRef = mUserRef.child(FirebaseEndpoint.BOOKMARKS);

        mBookmarkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            List<Course> bookmarks = new ArrayList<Course>();
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Course>> genericTypeIndicator = new GenericTypeIndicator<List<Course>>() {};
                bookmarks = dataSnapshot.getValue(genericTypeIndicator) == null ? bookmarks : dataSnapshot.getValue(genericTypeIndicator);
                if(bookmarks == null){
                    return;
                }
                for(Course c : bookmarks){
                    if(c.courseCode.equals(courseCode)){
                        toggle.setChecked(true);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    addCourseBookmark();

                } else {
                    removeCourseBookmark();
                }
            }
        });
        getUserName();
    }

    private void addCourseBookmark(){
        mUserRef = mDatabase.child(FirebaseEndpoint.USERS)
                .child(Utils.processEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail()));
        mBookmarkRef = mUserRef.child(FirebaseEndpoint.BOOKMARKS);
        mBookmarkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            List<Course> bookmarks = new ArrayList<Course>();
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Course>> genericTypeIndicator = new GenericTypeIndicator<List<Course>>() {};
                bookmarks = dataSnapshot.getValue(genericTypeIndicator) == null ? bookmarks : dataSnapshot.getValue(genericTypeIndicator);
                if(bookmarks == null){
                    bookmarks.add(new Course(courseCode, courseTitle));
                    mBookmarkRef.setValue(bookmarks);
                    return;
                }
                for(Course c : bookmarks){
                    if(c.courseCode.equals(courseCode)){
                        return;
                    }
                }
                bookmarks.add(new Course(courseCode, courseTitle));
                mBookmarkRef.setValue(bookmarks);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void removeCourseBookmark(){
        mUserRef = mDatabase.child(FirebaseEndpoint.USERS)
                .child(Utils.processEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail()));
        mBookmarkRef = mUserRef.child(FirebaseEndpoint.BOOKMARKS);
        mBookmarkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            List<Course> bookmarks = new ArrayList<Course>();
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Course>> genericTypeIndicator = new GenericTypeIndicator<List<Course>>() {};
                bookmarks = dataSnapshot.getValue(genericTypeIndicator) == null ? bookmarks : dataSnapshot.getValue(genericTypeIndicator);
                if(bookmarks == null){
                    bookmarks.add(new Course(courseCode, courseTitle));
                    mBookmarkRef.setValue(bookmarks);
                    return;
                }
                for(int i = 0; i < bookmarks.size(); i++){
                    if(bookmarks.get(i).courseCode.equals(courseCode)){
                        bookmarks.remove(i);
                        break;
                    }
                }
                mBookmarkRef.setValue(bookmarks);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
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
        txtCourseTitle = findViewById(R.id.txtUserName);
        txtCourseCode.setText(courseCode);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CourseTabActivity.this.finish();
            }
        });

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
                    updateRecentlyOpened();
                    Utils.updatePopularCount(1, courseCode, courseTitle);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void updateRecentlyOpened(){
        Log.v(TAG, "I am at updateRecentlyOpened");
        mUserRef = mDatabase.child(FirebaseEndpoint.USERS)
                .child(Utils.processEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail()));
        final DatabaseReference recentlyOpenedRef = mUserRef.child(FirebaseEndpoint.RECENTLY_OPENED_COURSES);
        recentlyOpenedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists()){
                    GenericTypeIndicator<ArrayList<Course>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<Course>>() {};
                    List<Course> recentlyOpenedCourses = snapshot.getValue(genericTypeIndicator) == null ?
                            new ArrayList<Course>() : snapshot.getValue(genericTypeIndicator);
                    for(int i = 0; i < recentlyOpenedCourses.size(); i++){
                        Course c = recentlyOpenedCourses.get(i);
                        if(c.courseCode.equals(courseCode)){
                            recentlyOpenedCourses.remove(i);
                            break;
                        }
                    }
                    recentlyOpenedCourses.add(new Course(courseCode, courseTitle));
                    recentlyOpenedRef.setValue(recentlyOpenedCourses);
                }
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