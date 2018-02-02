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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sveloso on 2017-11-04.
 */
public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
    private List<Course> mCourses;
    private int textColor;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View mLayout;
        private final Context context;

        private DatabaseReference mDatabase;

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
            String courseCode = txtCourseCode.getText().toString();
            getRecentlyOpened(courseCode);
            Intent intent = new Intent(context, CourseTabActivity.class);
            intent.putExtra("COURSE_CODE", courseCode);
            context.startActivity(intent);
        }


        /**
         * Retrieve recently opened list from Firebase and update it locally
         * @param courseCode
         */
        private void getRecentlyOpened(final String courseCode) {
            Log.v(TAG, "in getting recently opened");
            DatabaseReference recentlyOpenedRef =
                    mDatabase.child(FirebaseEndpoint.USERS)
                            .child(Utils.processEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
                            .child(FirebaseEndpoint.RECENTLY_OPENED_COURSES);
            recentlyOpenedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<String> recentlyOpened;
                    if (dataSnapshot.exists()) {
                        /* Gets a list of course code strings */
                        GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {
                        };

                        recentlyOpened = dataSnapshot.getValue(genericTypeIndicator);
                    } else {
                        recentlyOpened = new ArrayList<>();
                    }

                    if (!recentlyOpened.contains(courseCode)) {
                        recentlyOpened.add(courseCode);
                    } else {
                        recentlyOpened.remove(courseCode);
                        recentlyOpened.add(courseCode);
                    }

                    while(recentlyOpened.size() > Utils.RECENTLY_OPENED_LIMIT) {
                        recentlyOpened.remove(0);
                    }

                    mDatabase.child(FirebaseEndpoint.USERS)
                            .child(Utils.processEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
                            .child(FirebaseEndpoint.RECENTLY_OPENED_COURSES)
                            .setValue(recentlyOpened);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public CourseAdapter(List<Course> dataset, int textColor) {
        mCourses = dataset;
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

        TextView txtCourseTitle = holder.mLayout.findViewById(R.id.txtCourseTitle);
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