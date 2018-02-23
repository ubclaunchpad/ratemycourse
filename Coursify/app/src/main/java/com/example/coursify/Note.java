package com.example.coursify;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Annie Zhou on 11/19/2017.
 */

class Note {
    String color;
    String content;
    String time;
    boolean pinned;

    // creates a new note with color and content, pinned status and current time
    public Note(String color, String content, boolean pinned){
        this.color = color;
        this.content = content;
        this.pinned = pinned;
        this.time = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    }

//    public Note(String color, String content, boolean pinned){
//        this.color = color;
//        this.content = content;
//        this.time = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
//        this.pinned = pinned;
//    }

//    public void edit(String new_color, String new_content, boolean new_pinned){
//        this.color = new_color;
//        this.content = new_content;
//        this.pinned = new_pinned;
//        this.time = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
//    }

    public void editColor(String newColor) {
        this.color = newColor;
    }

    public void editContent(String newContent) {
        this.content = newContent;
        this.time = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    }

    public void setPinned(boolean b) {
        this.pinned = b;
    }
}
