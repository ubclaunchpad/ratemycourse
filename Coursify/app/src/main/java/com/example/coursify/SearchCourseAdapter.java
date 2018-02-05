package com.example.coursify;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Annie Zhou on 2/3/2018.
 */

public class SearchCourseAdapter extends RecyclerView.Adapter {
    ArrayList<String> searchedCourses;
    View view;
    Context context;
    ViewHolder viewHolder;
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(context).inflate(R.layout.search_course_list_item, parent, false);
        viewHolder = new ViewHolder(view, context);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int totalItem = getItemCount();
        int processedPosition = totalItem - 1 - position;
        viewHolder.courseId.setText(searchedCourses.get(processedPosition));
        viewHolder.courseDescript.setText(searchedCourses.get(position));
    }

    @Override
    public int getItemCount() {
        return searchedCourses.size();
    }

    public SearchCourseAdapter(ArrayList<String> searchedCourses, Context context){
        this.searchedCourses = searchedCourses;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView courseId;
        Context context;
        TextView courseDescript;

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, CourseTabActivity.class);
            intent.putExtra("COURSE_CODE", courseId.getText().toString());
            context.startActivity(intent);
        }

        public ViewHolder(View view, Context context) {
            super(view);
            this.courseId = view.findViewById(R.id.searchedCourseId);
            this.courseDescript = view.findViewById(R.id.searchedCourseDescript);
            this.context = context;
            view.setOnClickListener(this);
        }
    }
}
