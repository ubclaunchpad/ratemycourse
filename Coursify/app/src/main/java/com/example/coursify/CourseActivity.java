package com.example.coursify;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
    private DatabaseReference mCourseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        findViewsById();

        courseCode = getIntent().getStringExtra("COURSE_CODE");
        courseDept = courseCode.split(" ")[0];
        courseId = courseCode.split(" ")[1];

        getCourseReferenceToDatabase();
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
        mListComments.setAdapter(mCommentsAdapter);
        btnAddComment = (FloatingActionButton) findViewById(R.id.btnAddComment);
        btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addComment();
            }
        });
    }

    private void getCourseReferenceToDatabase() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference subjectRef = mDatabase.child(FirebaseEndpoint.COURSES).child(courseDept);
        DatabaseReference yearRef = subjectRef.child("Year " + courseId.charAt(0));
        mCourseReference = yearRef.child(courseDept + courseId);
    }

    private void populateUIFromDatabaseInfo() {
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

        mCourseReference.child(FirebaseEndpoint.COMMENTS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Comment> comments = new LinkedList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String comment = snapshot.getValue().toString();
                    comments.add(new Comment("Some student", comment));
                }
                mCommentsAdapter = new CommentAdapter(comments);
                mListComments.setAdapter(mCommentsAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void addComment() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add a comment");
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.add_comment, (ViewGroup) findViewById(R.id.add_comment), false);

        final EditText editTxtCommentBody = (EditText) viewInflated.findViewById(R.id.editTxtCommentBody);

        builder.setView(viewInflated);
        builder.setPositiveButton("Post Comment", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String commentBody = editTxtCommentBody.getText().toString();

                if (commentBody.equals("")) {
                    Toast.makeText(CourseActivity.this, "Please enter a comment before submitting.", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                    addComment();
                } else {
                    addCommentToDatabase(commentBody);
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

    private void addCommentToDatabase (String commentBody) {
        DatabaseReference commentsRef = mCourseReference.child(FirebaseEndpoint.COMMENTS);

        commentsRef.push().setValue(commentBody);
    }
}