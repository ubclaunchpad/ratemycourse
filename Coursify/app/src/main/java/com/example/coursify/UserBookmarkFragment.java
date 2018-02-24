package com.example.coursify;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.example.coursify.Utils.processEmail;


public class UserBookmarkFragment extends Fragment {
    private static final String TAG = UserBookmarkFragment.class.getSimpleName();

    CourseListAdapter courseListAdapter;
    RecyclerView recyclerView;
    DatabaseReference databaseReference, mUserReference, mBookmarkReference;
    String email;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getActivity());
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            Log.v(TAG, "Please sign in");
            startActivity(new Intent(getActivity(), LoginActivity.class));
        }else if(!user.isEmailVerified()){
            Log.v(TAG, "Please verify your email");
            startActivity(new Intent(getActivity(), LoginActivity.class));
        }
        email = user.getEmail();
        Log.v(TAG, "here is my email:" + email);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mUserReference = databaseReference.child(FirebaseEndpoint.USERS).child(Utils.processEmail(email));
        mBookmarkReference = mUserReference.child(FirebaseEndpoint.BOOKMARKS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_pref, container, false);
        recyclerView = view.findViewById(R.id.coursesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        displayCourses();

        return view;
    }

    public void displayCourses() {
        mBookmarkReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Course> bookmarkList = new ArrayList<>();
                ArrayList<String> bookmarkCourseList = new ArrayList<>();
                ArrayList<String> bookmarkCourseDescriptList = new ArrayList<>();
                if (dataSnapshot.exists()) { /* Gets a list of course code strings in user previous preference */
                    GenericTypeIndicator<ArrayList<Course>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<Course>>() {
                    };

                    bookmarkList = dataSnapshot.getValue(genericTypeIndicator);
                    Log.v(TAG, "this size of interestedList is" + bookmarkList.size());
                    for (int i = 0; i < bookmarkList.size(); i++) {
                        Course currCourse = bookmarkList.get(i);
                        Log.v(TAG, "the course code is: " + currCourse.courseCode + " and the courseTitle is " + currCourse.courseTitle);
                        bookmarkCourseList.add(currCourse.courseCode);
                        bookmarkCourseDescriptList.add(currCourse.courseTitle);
                    }
                }
                Log.v(TAG, "assssddddfffff this size of interestedCourseList is" + bookmarkList.size());
                courseListAdapter = new CourseListAdapter(bookmarkCourseList, bookmarkCourseDescriptList, getActivity());
                recyclerView.setAdapter(courseListAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}