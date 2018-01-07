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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by sveloso on 2017-11-11.
 */
public class CourseActivity extends Activity {

    public static final int IMGVIEW_MAX_WIDTH = 250;

    private LinearLayout layoutFabComment;
    private LinearLayout layoutFabRating;
    private boolean fabExpanded = false;

    private TextView txtCourseTitle;
    private TextView txtCourseCode;
    private TextView txtEasinessRating;
    private TextView txtUsefulnessRating;
    private FloatingActionButton btnAddCommentOrRating;

    private String courseCode;
    private String courseDept;
    private String courseId;

    private String currUserName;

    private RecyclerView mListComments;
    private RecyclerView.Adapter mCommentsAdapter;
    private RecyclerView.LayoutManager mCommentsManager;

    private ImageView imgEasinessRating;
    private ImageView imgUsefulnessRating;

    private DatabaseReference mDatabase;
    private DatabaseReference mCourseReference;

    private List<String> recentlyOpenedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        findViewsById();

        courseCode = getIntent().getStringExtra("COURSE_CODE");
        courseDept = courseCode.split(" ")[0];
        courseId = courseCode.split(" ")[1];

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mCourseReference = Utils.getCourseReferenceToDatabase(courseCode, mDatabase);

        getCurrentUserName();
        populateUIFromDatabaseInfo();
        txtCourseCode.setText(courseCode);

        closeSubMenusFab();

        getRecentlyOpened(courseCode);
    }

    /**
     * Retrieve recently opened list from Firebase and update it locally
     * @param courseCode
     */
    private void getRecentlyOpened(final String courseCode) {
        DatabaseReference recentlyOpenedRef =
                mDatabase.child(FirebaseEndpoint.USERS)
                        .child(Utils.processEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
                        .child(FirebaseEndpoint.RECENTLY_OPENED_COURSES);
        recentlyOpenedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> recentlyOpened;
                if(dataSnapshot.exists()) {
                        /* Gets a list of course code strings */
                    GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {
                    };

                    recentlyOpened = dataSnapshot.getValue(genericTypeIndicator);
                }
                else {
                    recentlyOpened = new ArrayList<>();
                }
                if(!recentlyOpened.contains(courseCode)) {
                    recentlyOpened.add(courseCode);
                }
                else {
                    recentlyOpened.remove(courseCode);
                    recentlyOpened.add(courseCode);
                }
                while(recentlyOpened.size() > Utils.RECENTLY_OPENED_LIMIT) {
                    recentlyOpened.remove(0);
                }
                recentlyOpenedList = recentlyOpened;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * save updated recently opened list to Firebase
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.child(FirebaseEndpoint.USERS)
                .child(Utils.processEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
                .child(FirebaseEndpoint.RECENTLY_OPENED_COURSES)
                .setValue(recentlyOpenedList);
    }

    private void findViewsById() {
        txtCourseTitle = findViewById(R.id.txtCourseTitle);
        txtCourseCode = findViewById(R.id.txtCourseCode);
        txtEasinessRating = findViewById(R.id.txtEasinessRating);
        txtUsefulnessRating = findViewById(R.id.txtUsefulnessRating);
        mListComments = findViewById(R.id.listCourseComments);
        layoutFabComment = findViewById(R.id.layoutFabComment);
        layoutFabRating = findViewById(R.id.layoutFabRating);
        imgEasinessRating = findViewById(R.id.imgEasinessRating);
        imgUsefulnessRating = findViewById(R.id.imgUsefulnessRating);
        mListComments.setHasFixedSize(true);
        mCommentsManager = new LinearLayoutManager(this);
        mListComments.setLayoutManager(mCommentsManager);
        mListComments.setAdapter(mCommentsAdapter);

        btnAddCommentOrRating = findViewById(R.id.fabCommentOrRating);
        btnAddCommentOrRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabExpanded){
                    closeSubMenusFab();
                } else {
                    openSubMenusFab();
                }
            }
        });
        FloatingActionButton btnAddComment = findViewById(R.id.fabComment);
        btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addComment();
                closeSubMenusFab();
            }
        });
        FloatingActionButton btnAddRating = findViewById(R.id.fabRating);
        btnAddRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRating();
                closeSubMenusFab();
            }
        });
    }

    private void getCurrentUserName() {
        // Get current user email
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String userEmail = user.getEmail();

        // Get current user name from database using current user email
        mDatabase.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (userEmail.equals(snapshot.child("email").getValue().toString())) {
                        currUserName = snapshot.child("name").getValue().toString();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

//    private void getCourseReferenceToDatabase() {
//        mDatabase = FirebaseDatabase.getInstance().getReference();
//        DatabaseReference subjectRef = mDatabase.child(FirebaseEndpoint.COURSES).child(courseDept);
//        DatabaseReference yearRef = subjectRef.child("Year " + courseId.charAt(0));
//        mCourseReference = yearRef.child(courseDept + courseId);
//    }

    // Load Firebase course information
    private void populateUIFromDatabaseInfo() {
        // Get course title from Firebase
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

        // Load comments from Firebase
        mCourseReference.child(FirebaseEndpoint.COMMENTS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Comment> comments = new LinkedList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String author = snapshot.child("author").getValue().toString();
                    String comment = snapshot.child("commentBody").getValue().toString();
                    boolean anonymity = (boolean) snapshot.child("anonymity").getValue();
                    comments.add(new Comment(author, comment, anonymity));
                }
                mCommentsAdapter = new CommentAdapter(comments);
                mListComments.setAdapter(mCommentsAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        // Load easiness and usefulness ratings from Firebase
        mCourseReference.child(FirebaseEndpoint.RATINGS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double totalEasiness = 0;
                double totalUsefulness = 0;
                int count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Long longEasiness = (Long) snapshot.child("easiness").getValue();
                    Long longUsefulness = (Long) snapshot.child("usefulness").getValue();

                    totalEasiness += longEasiness.doubleValue();
                    totalUsefulness += longUsefulness.doubleValue();
                    count++;
                }

                if (count != 0) {
                    double averageEasiness = totalEasiness / count;
                    double averageUsefulness = totalUsefulness / count;

                    // Find percent easiness out of 10 to set ImageView width as pixels
                    double percentEasiness = averageEasiness / 10;
                    double percentUsefulness = averageUsefulness / 10;
                    imgEasinessRating.getLayoutParams().width = Utils.convertDpToPx (CourseActivity.this, IMGVIEW_MAX_WIDTH * percentEasiness);
                    imgUsefulnessRating.getLayoutParams().width = Utils.convertDpToPx (CourseActivity.this, IMGVIEW_MAX_WIDTH * percentUsefulness);

                    // Find percent easiness out of 100 to show course ratings
                    double easinessRating = (averageEasiness) * 10;
                    double usefulnessRating = (averageUsefulness) * 10;
                    txtEasinessRating.setText(Math.round(easinessRating) + "%");
                    txtUsefulnessRating.setText(Math.round(usefulnessRating) + "%");
                }
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

        final EditText editTxtCommentBody = viewInflated.findViewById(R.id.editTxtCommentBody);
        final CheckBox chkBoxAnon = viewInflated.findViewById(R.id.chkBoxAnon);

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
                    addCommentToDatabase(commentBody, chkBoxAnon.isChecked());
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

    private void addCommentToDatabase (String commentBody, boolean anonymous) {
        Comment comment = new Comment(currUserName, commentBody, anonymous);
        DatabaseReference commentsRef = mCourseReference.child(FirebaseEndpoint.COMMENTS);

        commentsRef.push().setValue(comment);
    }

    private void addRating() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add a rating");
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.add_rating, (ViewGroup) findViewById(R.id.add_rating), false);

        final NumberPicker npEasiness = (NumberPicker) viewInflated.findViewById(R.id.numPickerEasiness);
        npEasiness.setMinValue(0);
        npEasiness.setMaxValue(10);
        final NumberPicker npUsefulness = (NumberPicker) viewInflated.findViewById(R.id.numPickerUsefulness);
        npUsefulness.setMinValue(0);
        npUsefulness.setMaxValue(10);

        builder.setView(viewInflated);
        builder.setPositiveButton("Add Rating", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int easinessRating = npEasiness.getValue();
                int usefulnessRating = npUsefulness.getValue();
                addRatingToDatabase(easinessRating, usefulnessRating);
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

    private void addRatingToDatabase (int easinessRating, int usefulnessRating) {
        Rating rating = new Rating("Some person", easinessRating, usefulnessRating);    // no author name for now
        DatabaseReference commentsRef = mCourseReference.child(FirebaseEndpoint.RATINGS);

        commentsRef.push().setValue(rating);
    }

    // Closes FAB submenus
    private void closeSubMenusFab(){
        layoutFabComment.setVisibility(View.INVISIBLE);
        layoutFabRating.setVisibility(View.INVISIBLE);
        btnAddCommentOrRating.setImageResource(R.mipmap.ic_add);
        fabExpanded = false;
    }

    // Opens FAB submenus
    private void openSubMenusFab(){
        layoutFabComment.setVisibility(View.VISIBLE);
        layoutFabRating.setVisibility(View.VISIBLE);
        btnAddCommentOrRating.setImageResource(R.mipmap.ic_close);
        fabExpanded = true;
    }
}