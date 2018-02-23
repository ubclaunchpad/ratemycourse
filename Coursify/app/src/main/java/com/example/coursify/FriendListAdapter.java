package com.example.coursify;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LucyZhao on 2017/11/11.
 */
public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {
    private static List<User> userList = new ArrayList<>();
    private static final String TAG = FriendListAdapter.class.getSimpleName();
    Context context;
    public FriendListAdapter(List<User> userList) {
        this.userList = userList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_list_item, parent, false);

        return new ViewHolder(itemLayoutView, context);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.txtFBUserName.setText(userList.get(position).getName());
        holder.txtMajor.setText((userList.get(position).getMajor()));
    }

    public FriendListAdapter(List<User> userList, Context context){
        this.userList = userList;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        Log.v(TAG, "Userlist size is in FriendListAdapter:" + userList.size());
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView txtFBUserName;
        private TextView txtMajor;
        Context context;

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, UserPreferenceTabActivity.class);
            int pos = this.getAdapterPosition();
            String email = userList.get(pos).getEmail();
            intent.putExtra("EMAIL", email);
            context.startActivity(intent);
        }

        public ViewHolder(View itemLayoutView, Context context) {
            super(itemLayoutView);
            this.txtFBUserName = (TextView) itemLayoutView.findViewById(R.id.txtFBUserName);
            this.txtMajor = (TextView) itemLayoutView.findViewById(R.id.txtMajor);
            this.context = context;
            itemLayoutView.setOnClickListener(this);
        }
    }

}
