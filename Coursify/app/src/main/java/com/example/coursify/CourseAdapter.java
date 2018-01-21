package com.example.coursify;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
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

import java.util.List;

/**
 * Created by sveloso on 2017-11-04.
 */

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
    private List<Course> mCourses;
    private int textColor;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public View mLayout;
        private final Context context;

        public ViewHolder(View v) {
            super(v);
            this.context = v.getContext();
            mLayout = v;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            TextView txtCourseCode = (TextView) mLayout.findViewById(R.id.txtCourseCode);
            String courseCode = txtCourseCode.getText().toString();
            Intent intent = new Intent(context, CourseTabActivity.class);
            intent.putExtra("COURSE_CODE", courseCode);
            context.startActivity(intent);
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