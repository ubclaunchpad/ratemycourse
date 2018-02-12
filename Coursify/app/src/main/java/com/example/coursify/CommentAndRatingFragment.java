package com.example.coursify;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
 * Created by sveloso on 2018-01-20.
 */
public class CommentAndRatingFragment extends Fragment {

    public static final int IMGVIEW_MAX_WIDTH = 250;

    private LinearLayout layoutFabComment;
    private LinearLayout layoutFabRating;
    private boolean fabExpanded = false;

    private TextView txtEasinessRating;
    private TextView txtUsefulnessRating;
    private FloatingActionButton btnAddCommentOrRating;

    private String courseCode;
    private String courseDept;
    private String courseId;
    private String courseTitle;

    private String currUserName;

    private RecyclerView mListComments;
    private RecyclerView.Adapter mCommentsAdapter;
    private RecyclerView.LayoutManager mCommentsManager;

    private ImageView imgEasinessRating;
    private ImageView imgUsefulnessRating;

    private DatabaseReference mDatabase;
    private DatabaseReference mCourseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_course, container, false);

        findViewsById(view);

        courseCode = getActivity().getIntent().getStringExtra("COURSE_CODE");
        courseDept = courseCode.split(" ")[0];
        courseId = courseCode.split(" ")[1];

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mCourseReference = Utils.getCourseReferenceToDatabase(courseCode, mDatabase);

        getCourseTitle();
        getCurrentUserName();
        populateUIFromDatabaseInfo();

        closeSubMenusFab();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void findViewsById(View container) {
        txtEasinessRating = container.findViewById(R.id.txtEasinessRating);
        txtUsefulnessRating = container.findViewById(R.id.txtUsefulnessRating);
        mListComments = container.findViewById(R.id.listCourseComments);
        layoutFabComment = container.findViewById(R.id.layoutFabComment);
        layoutFabRating = container.findViewById(R.id.layoutFabRating);
        imgEasinessRating = container.findViewById(R.id.imgEasinessRating);
        imgUsefulnessRating = container.findViewById(R.id.imgUsefulnessRating);
        mListComments.setHasFixedSize(true);
        mCommentsManager = new LinearLayoutManager(getActivity());
        mListComments.setLayoutManager(mCommentsManager);
        mListComments.setAdapter(mCommentsAdapter);

        btnAddCommentOrRating = container.findViewById(R.id.fabCommentOrRating);
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
        FloatingActionButton btnAddComment = container.findViewById(R.id.fabComment);
        btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddCommentWorkflow();
                closeSubMenusFab();
            }
        });
        FloatingActionButton btnAddRating = container.findViewById(R.id.fabRating);
        btnAddRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddRatingWorkflow();
                closeSubMenusFab();
            }
        });
    }

    private void getCourseTitle(){
        mCourseReference.child(FirebaseEndpoint.DESCRIPTION).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                courseTitle = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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

    // Load Firebase course information
    private void populateUIFromDatabaseInfo() {
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

                    Utils.updatePopularCount((int) (longEasiness + longUsefulness), courseCode, courseTitle);
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
                    imgEasinessRating.getLayoutParams().width = Utils.convertDpToPx (getActivity(), IMGVIEW_MAX_WIDTH * percentEasiness);
                    imgUsefulnessRating.getLayoutParams().width = Utils.convertDpToPx (getActivity(), IMGVIEW_MAX_WIDTH * percentUsefulness);

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

    private void startAddCommentWorkflow() {
        // First, check if a comment by this user already exists
        mCourseReference.child(FirebaseEndpoint.COMMENTS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean userAlreadyCommented = false;

                for (DataSnapshot comment : dataSnapshot.getChildren()) {
                    if (comment.child("author").getValue().toString().equals(currUserName)) {
                        userAlreadyCommented = true;
                        break;
                    }
                }

                if (!userAlreadyCommented) {
                    addComment();
                } else {
                    Toast.makeText(CommentAndRatingFragment.this.getActivity(), "You have already commented this course, edit your comment in Settings!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void addComment() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add a comment");
        View viewInflated = LayoutInflater.from(getActivity()).inflate(R.layout.add_comment, (ViewGroup) getActivity().findViewById(R.id.add_comment), false);

        final EditText editTxtCommentBody = viewInflated.findViewById(R.id.editTxtCommentBody);
        final CheckBox chkBoxAnon = viewInflated.findViewById(R.id.chkBoxAnon);

        builder.setView(viewInflated);
        builder.setPositiveButton("Post Comment", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String commentBody = editTxtCommentBody.getText().toString();

                if (commentBody.equals("")) {
                    Toast.makeText(getActivity(), "Please enter a comment before submitting.", Toast.LENGTH_SHORT).show();
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

    private void startAddRatingWorkflow() {
        // First, check if a rating by this user already exists
        mCourseReference.child(FirebaseEndpoint.RATINGS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean userAlreadyRated = false;

                for (DataSnapshot rating : dataSnapshot.getChildren()) {
                    if (rating.child("author").getValue().toString().equals(currUserName)) {
                        userAlreadyRated = true;
                        break;
                    }
                }

                if (!userAlreadyRated) {
                    addRating();
                } else {
                    Toast.makeText(CommentAndRatingFragment.this.getActivity(), "You have already rated this course, edit your rating in Settings!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void addRating() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add a rating");
        View viewInflated = LayoutInflater.from(getActivity()).inflate(R.layout.add_rating, (ViewGroup) getActivity().findViewById(R.id.add_rating), false);

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
        Rating rating = new Rating(currUserName, easinessRating, usefulnessRating);
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