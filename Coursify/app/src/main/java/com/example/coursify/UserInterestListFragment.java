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
import java.util.HashMap;
import java.util.List;

import static com.example.coursify.Utils.processEmail;


public class UserInterestListFragment extends Fragment {
    private static final String TAG = UserInterestListFragment.class.getSimpleName();
    private String email;

    RecyclerView recyclerView;
    RecyclerView.Adapter courseListAdapter;
    DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null){
            Log.v(TAG, "Please sign in");
            startActivity(new Intent(getActivity(), LoginActivity.class));
        }else if(!user.isEmailVerified()){
            Log.v(TAG, "Please verify your email");
            startActivity(new Intent(getActivity(), LoginActivity.class));
        }
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            email = bundle.getString("EMAIL", user.getEmail());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_pref, container, false);
        recyclerView = view.findViewById(R.id.coursesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        databaseReference = FirebaseDatabase.getInstance().getReference();
        displayCourses();

        return view;
    }

    public void displayCourses(){
        final DatabaseReference interestListRef = databaseReference.child(FirebaseEndpoint.USERS).child(Utils.processEmail(email)).child(FirebaseEndpoint.INTERESTED);
        interestListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Course> interestedList;
                ArrayList<String> interestedCourseList = new ArrayList<>();
                ArrayList<String> interestedCourseDescriptList = new ArrayList<>();
                if (dataSnapshot.exists()) { /* Gets a list of course code strings in user previous preference */
                    GenericTypeIndicator<ArrayList<Course>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<Course>>() {};

                    interestedList = dataSnapshot.getValue(genericTypeIndicator);
                    Log.v(TAG, "this size of interestedList is" + interestedList.size());
                    for(int i = 0; i < interestedList.size(); i++){
                        Course currCourse = interestedList.get(i);
                        Log.v(TAG, "the course code is: "+ currCourse.courseCode + " and the courseTitle is " + currCourse.courseTitle);
                        interestedCourseList.add(currCourse.courseCode);
                        interestedCourseDescriptList.add(currCourse.courseTitle);
                    }
                }
                Log.v(TAG, "assssddddfffff this size of interestedCourseList is" + interestedCourseList.size());
                courseListAdapter = new CourseListAdapter(interestedCourseList, interestedCourseDescriptList, getActivity());
                recyclerView.setAdapter(courseListAdapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}