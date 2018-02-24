package com.example.coursify;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Ravina on 2018-02-17.
 */

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
    private List<Note> mNotes;

    // taken from CommentAdapter
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public RelativeLayout mLayout;

        public ViewHolder(RelativeLayout v) {
            super(v);
            mLayout = v;
        }
    }

    public NoteAdapter(List<Note> dataset) {
        mNotes = dataset;
    }

    @Override
    public NoteAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext()).
                inflate(R.layout.note_list_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    // replaces contents of a view (invoked by layout manager)
    @Override
    public void onBindViewHolder(NoteAdapter.ViewHolder holder, int position) {
        Note currNote = mNotes.get(position);
        TextView txtNoteBody = holder.mLayout.findViewById(R.id.txtNoteBody);

        if(currNote.pinned) {
            // add an imageview or something on top of text view to indicate it is pinned
        }

        txtNoteBody.setText(currNote.content);

    }

    @Override
    public int getItemCount() {
        return mNotes.size();
    }
}
