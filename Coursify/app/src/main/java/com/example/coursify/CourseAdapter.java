package com.example.coursify;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sveloso on 2017-11-04.
 */
public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
    private List<Course> mCourses;
    private int textColor;
    private static final String TAG = CourseAdapter.class.getSimpleName();

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View mLayout;
        private final Context context;

        private DatabaseReference mDatabase;
        private String processedEmail;

        private final String TAG = CourseAdapter.class.getSimpleName();

        public ViewHolder(View v) {
            super(v);
            this.context = v.getContext();
            mLayout = v;
            mDatabase = FirebaseDatabase.getInstance().getReference();
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            TextView txtCourseCode = mLayout.findViewById(R.id.txtCourseCode);
            String processedCourseCode = Utils.courseCodeFormatter(txtCourseCode.getText().toString());
            processedEmail = Utils.processEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            onSubmit(processedCourseCode);
        }

        public void onSubmit(final String processedCourseId){
            Log.v(TAG, "onSubmit + processedCourseId = " + processedCourseId);
            DatabaseReference courseRef = Utils.getCourseReferenceToDatabase(processedCourseId, mDatabase);
            if(courseRef == null) {
                Log.v(TAG, "no course exists");
                return;
            }

            // get Description of this course and save its ID;
            courseRef.child("Description").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Course course = new Course(processedCourseId, (String) dataSnapshot.getValue());
                    saveAndJumpToCourse(course);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        public void saveAndJumpToCourse(final Course course){
            Log.v(TAG, "saveAndJumpToCourses + courseId + courseDescript = " + course.courseCode + ", " + course.courseTitle);
            Intent intent = new Intent(context, CourseTabActivity.class);
            intent.putExtra("COURSE_CODE", course.courseCode);
            context.startActivity(intent);
        }
    }

    public CourseAdapter(List<Course> dataset, int textColor) {
        mCourses = dataset;
        for(Course c : dataset){
            Log.v(TAG, c.courseCode);
        }
        this.textColor = textColor;
    }

    @Override
    public CourseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.course_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Course currCourse = mCourses.get(position);

        TextView txtCourseTitle = holder.mLayout.findViewById(R.id.txtUserName);
        TextView txtCourseCode = holder.mLayout.findViewById(R.id.txtCourseCode);

        txtCourseTitle.setText(currCourse.courseTitle);
        txtCourseCode.setText(currCourse.courseCode);
        txtCourseTitle.setTextColor(textColor);
        txtCourseCode.setTextColor(textColor);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mCourses.size();
    }
}