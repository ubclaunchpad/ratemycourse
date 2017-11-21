package com.example.coursify;

/**
 * Created by sveloso on 2017-11-11.
 */

public class Comment {

    String author;
    String commentBody;
    boolean anonymity;



    public Comment (String commenterName, String commentBody) {
        this.author = commenterName;
        this.commentBody = commentBody;
    }

}
