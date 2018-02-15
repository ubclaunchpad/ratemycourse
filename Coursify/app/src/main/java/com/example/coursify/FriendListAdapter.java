package com.example.coursify;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by LucyZhao on 2017/11/11.
 */
public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {
    private final List<User> userList;
    private static final String TAG = FriendListAdapter.class.getSimpleName();

    public FriendListAdapter(List<User> userList) {
        this.userList = userList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_list_item, parent, false);

        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.txtFBUserName.setText(userList.get(position).getName());
        holder.txtMajor.setText((userList.get(position).getMajor()));
    }

    @Override
    public int getItemCount() {
        Log.v(TAG, "Userlist size is in FriendListAdapter:" + userList.size());
        return userList.size();
    }

    // inner class to hold a reference to each item of RecyclerView
    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView txtFBUserName;
        private TextView txtMajor;


        private ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            this.txtFBUserName = (TextView) itemLayoutView.findViewById(R.id.txtFBUserName);
            this.txtMajor = (TextView) itemLayoutView.findViewById(R.id.txtMajor);
        }

        @Override
        public void onClick(View view) {
            //todo
        }

    }
}
