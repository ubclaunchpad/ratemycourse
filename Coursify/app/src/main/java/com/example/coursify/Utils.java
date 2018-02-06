package com.example.coursify;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by LucyZhao on 2017/11/11.
 */

public class Utils {

    public static final int RECENTLY_OPENED_LIMIT = 10;
    private static final String TAG = Utils.class.getSimpleName();

    protected static String processEmail(String email){
        int i = email.indexOf('@');
        email = email.substring(0, i) + ";at;" + email.substring(i+1);
        ArrayList<Integer> indeces = new ArrayList<Integer>();
        for(int k = 0; k < email.length(); k++){
            if(email.charAt(k) == '.'){
                email = email.substring(0, k) + ";dot;" + email.substring(k+1);
            }
        }
        return email;
    }

    protected static int convertDpToPx (Context context, double dp) {
        return (int) ((dp * context.getResources().getDisplayMetrics().density) + 0.5);
    }

    /**
     * Given a course code, return reference to it in Firebase
     * @param courseCode in the format of "CPSC 110"
     * @return
     */
    public static DatabaseReference getCourseReferenceToDatabase(String courseCode, DatabaseReference mDatabase) {
        String courseDept = courseCode.split(" ")[0];
        String courseId = courseCode.split(" ")[1];

        DatabaseReference subjectRef = mDatabase.child(FirebaseEndpoint.COURSES).child(courseDept);
        DatabaseReference yearRef = subjectRef.child("Year " + courseId.charAt(0));
        return yearRef.child(courseDept + courseId);
    }

    public static ArrayList<String> processCourses(ArrayList<String> courses){
        for(int j = courses.size()-1; j >= 0; j--){
            if(courses.get(j) == null){
                courses.remove(j);
            }
        }
        return courses;
    }
}
