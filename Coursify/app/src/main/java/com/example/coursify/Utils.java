package com.example.coursify;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;

import java.lang.reflect.Array;
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
        String courseDept;
        String courseId;
        try {
            courseDept = courseCode.split(" ")[0];
            courseId = courseCode.split(" ")[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }

        DatabaseReference subjectRef = mDatabase.child(FirebaseEndpoint.COURSES).child(courseDept);
        if(subjectRef == null){
            Log.v(TAG, "subject does not exist!");
            return null;
        }
        DatabaseReference yearRef = subjectRef.child("Year " + courseId.charAt(0));
        if(yearRef == null){
            Log.v(TAG, "year does not exist!");
            return null;
        }
        if(yearRef.child(courseDept + courseId) == null){
            Log.v(TAG, "course does not exist!");
            return null;
        }
        return yearRef.child(courseDept + courseId);
    }

    public static String courseCodeFormatter(String courseCode){
        String course = "", code = "";
        if(courseCode.contains(" ")){
            String arr[] = courseCode.split(" ");
            if(arr.length != 2){
                Log.v(TAG, "please enter valid course format");
                return "";
            }
            course = arr[0];
            code = arr[1];
        }else{
            for(int i = 0; i < courseCode.length(); i++){
                String nums = "0123456789";
                char c = courseCode.charAt(i);
                if(nums.indexOf(c) >= 0){
                    course = courseCode.substring(0, i);
                    code = courseCode.substring(i);
                    break;
                }
            }
        }
        Log.v(TAG, "formatted is " + course.toUpperCase() + " " + code);
        return course.toUpperCase() + " " + code;
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
