package com.example.coursify;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Annie Zhou on 11/19/2017.
 */

class Note {
    int color;
    String content;
    String time;
    boolean pinned;

    // creates a new note with color and content, pinned status and current time
    public Note(int color, String content, boolean pinned){
        this.color = color;
        this.content = content;
        this.pinned = pinned;
        this.time = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    }


    public void editColor(int newColor) {
        this.color = newColor;
    }

    public void editContent(String newContent) {
        this.content = newContent;
        this.time = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    }

    public void setPinned(boolean b) {
        this.pinned = b;
    }

    public int getColour() {
        return color;
    }
}
