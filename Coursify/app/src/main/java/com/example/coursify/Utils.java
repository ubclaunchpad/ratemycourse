package com.example.coursify;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by LucyZhao on 2017/11/11.
 */

public class Utils {
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

    /**
     * Takes in a course code in the format "CPSC110" and
     * convert to "CPSC 110"
     * @param courseCode
     * @return
     */
    static String processCourseCode(String courseCode) {
        int firstNumIndex = 0;
        for(int i = 0; i < courseCode.length(); i++) {
            if(Character.isDigit(courseCode.charAt(i))) {
                firstNumIndex = i;
                break;
            }
        }
        Log.v(TAG, "index of the first number is: " + firstNumIndex);
        String courseDept = courseCode.substring(0, firstNumIndex);
        String courseId = courseCode.substring(firstNumIndex);
        Log.v(TAG, "processed course code is " + courseDept + " " + courseId);
        return courseDept + " " + courseId;
    }
}
