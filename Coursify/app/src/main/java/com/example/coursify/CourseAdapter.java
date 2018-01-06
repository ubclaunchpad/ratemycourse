package com.example.coursify;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Created by sveloso on 2017-11-04.
 */

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
    private List<Course> mCourses;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public RelativeLayout mLayout;
        private final Context context;

        private DatabaseReference mDatabase;
        private FirebaseAuth mAuth;
        String courseCode;

        private static final String TAG = "CourseAdapter VH";

        public ViewHolder(RelativeLayout v) {
            super(v);
            this.context = v.getContext();
            mLayout = v;
            v.setOnClickListener(this);

            initializeFirebase();
        }

        @Override
        public void onClick(View v) {
            addToRecent();
//            Intent intent = new Intent(context, CourseActivity.class);
//            TextView txtCourseCode = (TextView) mLayout.findViewById(R.id.txtCourseCode);
//            courseCode = txtCourseCode.getText().toString();
//            intent.putExtra("COURSE_CODE", courseCode);
//            context.startActivity(intent);
        }

        private void addToRecent() {
            final DatabaseReference recentlyOpenedRef =
                    mDatabase.child(FirebaseEndpoint.USERS)
                            .child(Utils.processEmail(mAuth.getCurrentUser().getEmail()))
                            .child(FirebaseEndpoint.RECENTLY_OPENED_COURSES);
            recentlyOpenedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        /* Gets a list of course code strings */
                        GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {
                        };

                        List<String> recentlyOpened = dataSnapshot.getValue(genericTypeIndicator);
                        if(!recentlyOpened.contains(courseCode)) {
                            recentlyOpened.add(courseCode);
                            Log.v(TAG, "courseadapter: " + courseCode); // todo fix coursecode is null
                        }


                        recentlyOpenedRef.setValue(recentlyOpened);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        private void initializeFirebase() {
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mAuth = FirebaseAuth.getInstance();
        }
    }

    public CourseAdapter(List<Course> dataset) {
        mCourses = dataset;
    }

    @Override
    public CourseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // create a new view
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.course_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Course currCourse = mCourses.get(position);

        TextView txtCourseTitle = (TextView) holder.mLayout.findViewById(R.id.txtCourseTitle);
        TextView txtCourseCode = (TextView) holder.mLayout.findViewById(R.id.txtCourseCode);

        txtCourseTitle.setText(currCourse.courseTitle);
        txtCourseCode.setText(currCourse.courseCode);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mCourses.size();
    }
}