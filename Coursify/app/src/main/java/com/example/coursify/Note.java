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

    public Note(String color, String content){
        this.color = color;
        this.content = content;
        this.time = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    }
}
