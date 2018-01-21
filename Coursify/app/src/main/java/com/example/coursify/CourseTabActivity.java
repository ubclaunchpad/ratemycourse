package com.example.coursify;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by sveloso on 2018-01-20.
 */

public class CourseTabActivity extends AppCompatActivity {

    private TextView txtCourseTitle;
    private TextView txtCourseCode;

    private String courseCode;
    private String courseDept;
    private String courseId;

    private DatabaseReference mDatabase;
    private DatabaseReference mCourseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_course_tabs);
        updateHeaderValues();

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
    }

    private void updateHeaderValues() {
        courseCode = getIntent().getStringExtra("COURSE_CODE");
        courseDept = courseCode.split(" ")[0];
        courseId = courseCode.split(" ")[1];

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mCourseReference = Utils.getCourseReferenceToDatabase(courseCode, mDatabase);

        txtCourseCode = findViewById(R.id.txtCourseCode);
        txtCourseTitle = findViewById(R.id.txtCourseTitle);
        txtCourseCode.setText(courseCode);

        mCourseReference.child(FirebaseEndpoint.DESCRIPTION).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String s = snapshot.getValue().toString();

                txtCourseTitle.setText(s);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}