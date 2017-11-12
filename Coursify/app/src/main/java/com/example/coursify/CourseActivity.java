package com.example.coursify;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by sveloso on 2017-11-11.
 */
public class CourseActivity extends Activity {

    private TextView txtCourseTitle;
    private TextView txtCourseCode;
    private TextView txtEasinessRating;
    private TextView txtUsefulnessRating;
    private FloatingActionButton btnAddComment;

    private String courseCode;
    private String courseDept;
    private String courseId;

    private RecyclerView mListComments;
    private RecyclerView.Adapter mCommentsAdapter;
    private RecyclerView.LayoutManager mCommentsManager;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        findViewsById();

        courseCode = getIntent().getStringExtra("COURSE_CODE");
        courseDept = courseCode.split(" ")[0];
        courseId = courseCode.split(" ")[1];

        Toast.makeText(this, "Course dept: " + courseDept +  "  course id: " + courseId, Toast.LENGTH_SHORT).show();

        populateUIFromDatabaseInfo();
        txtCourseCode.setText(courseCode);
    }

    private void findViewsById() {
        txtCourseTitle = (TextView) findViewById(R.id.txtCourseTitle);
        txtCourseCode = (TextView) findViewById(R.id.txtCourseCode);
        txtEasinessRating = (TextView) findViewById(R.id.txtEasinessRating);
        txtUsefulnessRating = (TextView) findViewById(R.id.txtUsefulnessRating);
        mListComments = (RecyclerView) findViewById(R.id.listCourseComments);
        mListComments.setHasFixedSize(true);
        mCommentsManager = new LinearLayoutManager(this);
        mListComments.setLayoutManager(mCommentsManager);
        mCommentsAdapter = new CommentAdapter(initializeComments());
        mListComments.setAdapter(mCommentsAdapter);
        btnAddComment = (FloatingActionButton) findViewById(R.id.btnAddComment);
    }

    private void populateUIFromDatabaseInfo() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference subjectRef = mDatabase.child("Courses").child(courseDept);
        DatabaseReference yearRef = subjectRef.child("Year " + courseId.charAt(0));
        DatabaseReference courseRef = yearRef.child(courseDept + courseId);

        courseRef.child("Description").addValueEventListener(new ValueEventListener() {
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

    private List<Comment> initializeComments() {
        List<Comment> comments = new LinkedList<>();
        Comment c1 = new Comment("Annie Zhou", "This course is pretty good, it's super relevant to everything.");
        Comment c2 = new Comment("Sam Veloso", "It's a good course. ");
        Comment c3 = new Comment("Lucy Zhao", "It's not a good course.");
        comments.add(c1);
        comments.add(c2);
        comments.add(c3);
        return comments;
    }
}