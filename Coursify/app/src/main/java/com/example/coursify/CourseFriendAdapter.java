package com.example.coursify;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by sveloso on 2018-02-12.
 */

public class CourseFriendAdapter extends RecyclerView.Adapter<CourseFriendAdapter.ViewHolder> {
    private static List<CourseFriend> courseFriendList;
    private static final String TAG = FriendListAdapter.class.getSimpleName();
    Context context;

    public CourseFriendAdapter(List<CourseFriend> courseFriendList, Context context) {
        this.courseFriendList = courseFriendList;
        this.context = context;
    }

    @Override
    public CourseFriendAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.course_friend_list_item, parent, false);

        return new CourseFriendAdapter.ViewHolder(itemLayoutView, context);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.txtName.setText(courseFriendList.get(position).getName());
        holder.txtPreference.setText((courseFriendList.get(position).getPreference()));
    }

    @Override
    public int getItemCount() {
        return courseFriendList.size();
    }

    // inner class to hold a reference to each item of RecyclerView
    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView txtName;
        private TextView txtPreference;
        Context context;

        private ViewHolder(View itemLayoutView, Context context) {
            super(itemLayoutView);
            this.txtName = itemLayoutView.findViewById(R.id.txtFriendName);
            this.txtPreference = itemLayoutView.findViewById(R.id.txtFriendPreference);
            this.context = context;
        }

        @Override
        public void onClick(View view) {
            Log.v(TAG, "I was here clicking");
            Intent intent = new Intent(context, UserPreferenceTabActivity.class);
            int pos = this.getAdapterPosition();
            String email = courseFriendList.get(pos).getEmail();
            intent.putExtra("EMAIL", email);
            context.startActivity(intent);
        }
    }
}