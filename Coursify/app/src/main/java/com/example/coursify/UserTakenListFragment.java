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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class UserTakenListFragment extends Fragment {
    private static final String TAG = UserTakenListFragment.class.getSimpleName();
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }

    public void displayCourses(){
        final DatabaseReference takenCourseRef = databaseReference.child(FirebaseEndpoint.USERS).child(Utils.processEmail(email)).child(FirebaseEndpoint.TAKEN);
        takenCourseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Course> interestedList;
                ArrayList<String> takenCourseList = new ArrayList<>();
                ArrayList<String> takenCourseDescriptList = new ArrayList<>();
                if (dataSnapshot.exists()) { /* Gets a list of course code strings in user previous preference */
                    GenericTypeIndicator<ArrayList<Course>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<Course>>() {};

                    interestedList = dataSnapshot.getValue(genericTypeIndicator);
                    Log.v(TAG, "this size of willTake is" + interestedList.size());
                    for(int i = 0; i < interestedList.size(); i++){
                        Course currCourse = interestedList.get(i);
                        Log.v(TAG, "the course code is: "+ currCourse.courseCode + " and the courseTitle is " + currCourse.courseTitle);
                        takenCourseList.add(currCourse.courseCode);
                        takenCourseDescriptList.add(currCourse.courseTitle);
                    }
                }
                Log.v(TAG, "assssddddfffff this size of willTakeCourseList is" + takenCourseList.size());
                courseListAdapter = new CourseListAdapter(takenCourseList, takenCourseDescriptList, getActivity());
                recyclerView.setAdapter(courseListAdapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}