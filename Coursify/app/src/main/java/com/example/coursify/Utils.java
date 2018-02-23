package com.example.coursify;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.view.menu.MenuView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by LucyZhao on 2017/11/11.
 */

public class Utils {

    public static final int RECENTLY_OPENED_LIMIT = 10;
    private static final String TAG = Utils.class.getSimpleName();
    private static final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    protected static String processEmail(String email){
        int i = email.indexOf('@');
        if(i < 0){
            return email;
        }
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
        courseCode = courseCodeFormatter(courseCode);
        String courseDept;
        String courseId;
        try {
            courseDept = courseCode.split(" ")[0];
            courseId = courseCode.split(" ")[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.v(TAG, "wrong format");
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

    public static void updatePopularCount(final int value, final String courseCode, final String courseTitle){
        final DatabaseReference mCourseReference = getCourseReferenceToDatabase(courseCodeFormatter(courseCode), mDatabase);
        mCourseReference.child(FirebaseEndpoint.POPULARCOUNT).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int popularCount = 0;
                if(snapshot.exists()){
                    popularCount = Integer.parseInt(snapshot.getValue().toString());
                }
                mCourseReference.child(FirebaseEndpoint.POPULARCOUNT).setValue(popularCount+value);
                updatePopularCourses(popularCount+value, courseCode, courseTitle);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private static void updatePopularCourses(final int popularCount, final String courseCode, final String courseTitle){
        Log.v(TAG, "I am at updatePopularCourses");
        final DatabaseReference mPopularCourseRef = mDatabase.child(FirebaseEndpoint.POPULAR_COURSES);
        mPopularCourseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList<HashMap<String, String>> popularCourses = snapshot.getValue() == null ?
                        new ArrayList<HashMap<String, String>>() :
                        (ArrayList<HashMap<String, String>>) snapshot.getValue();
                HashMap<String, String> newCourse;
                newCourse = new HashMap<String, String>();
                newCourse.put("courseCode", courseCode);
                newCourse.put("courseTitle", courseTitle);
                newCourse.put("popularCount", Integer.toString(popularCount));
                if(popularCourses.size() == 0){
                    popularCourses.add(newCourse);
                }

                int minIndex = -1;
                int minPopularity = Integer.MAX_VALUE;
                for(int i = 0; i < popularCourses.size(); i++){
                    HashMap<String, String> currCourse = popularCourses.get(i);
                    int currPopularCount = Integer.parseInt(currCourse.get("popularCount"));
                    String currCourseId = currCourse.get("courseCode");
                    // if we find that the newly added course is already in popular,
                    // we simple update the field and return;
                    Log.v(TAG, "currCourseCode + courseId = " + currCourseId + ", " + courseCode);
                    if(currCourseId.equals(courseCode)){
                        newCourse.put("popularCount", Integer.toString(popularCount));
                        popularCourses.set(i, newCourse);
                        mPopularCourseRef.setValue(popularCourses);
                        return;
                    }
                    // finds the minimum index;
                    if(currPopularCount < minPopularity){
                        minPopularity = currPopularCount;
                        minIndex = i;
                    }
                }

                if(popularCount >= minPopularity && popularCourses.size() >= 5){
                    popularCourses.remove(minIndex);
                }
                // if the current popular count is greater than min, then we insert it and remove
                // the other one
                if(popularCount >= minPopularity){
                    popularCourses.add(newCourse);
                }

                mPopularCourseRef.setValue(popularCourses);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
