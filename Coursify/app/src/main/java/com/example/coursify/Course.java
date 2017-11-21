package com.example.coursify;

import java.util.List;

/**
 * Created by sveloso on 2017-11-04.
 */

public class Course {

    String courseTitle;
    String courseCode;
    List<String> takenUsers;
    List<String> goingUsers;
    List<String> interestedUsers;
    List<Comment> comments;
    List<Rating> ratings;
    double easiness;
    double usefulness;

    public Course (String code, String title) {
        courseTitle = title;
        courseCode = code;
        easiness = -1;
        usefulness = -1;
    }


}