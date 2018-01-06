package com.example.coursify;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by sveloso on 2017-11-04.
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private List<Comment> mComments;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public RelativeLayout mLayout;

        public ViewHolder(RelativeLayout v) {
            super(v);
            mLayout = v;
        }
    }

    public CommentAdapter(List<Comment> dataset) {
        mComments = dataset;
    }

    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Comment currComment = mComments.get(position);
        TextView txtCommenterName = holder.mLayout.findViewById(R.id.txtCommenterName);
        TextView txtCommentBody = holder.mLayout.findViewById(R.id.txtCommentBody);

        if (currComment.anonymity) {
            txtCommenterName.setText("Anonymous");
        } else {
            txtCommenterName.setText(currComment.author);
        }

        txtCommentBody.setText(currComment.commentBody);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mComments.size();
    }
}