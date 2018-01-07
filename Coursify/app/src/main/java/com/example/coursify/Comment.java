package com.example.coursify;

/**
 * Created by sveloso on 2017-11-11.
 */
public class Comment {

    String author;
    String commentBody;
    boolean anonymity;

    // I see we are using this in CourseActivity when user adds a comment. But I think in the future
    // in all cases we would need to attach an anonymity to it; so maybe we should remove it once
    // all of migration is done?
    public Comment (String commenterName, String commentBody) {
        this.author = commenterName;
        this.commentBody = commentBody;
    }

    public Comment (String commenterName, String commentBody, boolean anonymity){
        this.author = commenterName;
        this.commentBody = commentBody;
        this.anonymity = anonymity;
    }

}
