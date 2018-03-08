package com.example.coursify;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CourseFriendsFragment extends Fragment {
    private static final String TAG = CourseFriendsFragment.class.getSimpleName();
    private RecyclerView listCourseFriends;
    private CourseFriendAdapter courseFriendAdapter;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference mUserRef;
    private String courseCode;
    private List<CourseFriend> courseFriends;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_friends, container, false);

        courseCode = getActivity().getIntent().getStringExtra("COURSE_CODE");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mUserRef = mDatabaseRef.child(FirebaseEndpoint.USERS)
                .child(Utils.processEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail()));

        listCourseFriends = view.findViewById(R.id.courseFriendsList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listCourseFriends.setLayoutManager(linearLayoutManager);

        getUserFriends();
        return view;
    }


    // Step 1: Get User Friends' IDs
    private void getUserFriends() {
        final DatabaseReference userFriendsRef = mUserRef.child(FirebaseEndpoint.FACEBOOK_FRIENDS);

        userFriendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> friends = dataSnapshot.getValue() == null ? new ArrayList<String>() : (ArrayList<String>)dataSnapshot.getValue();

                getFaceBookFriendsEmail(friends);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    // Step 2: Get their Emails
    private void getFaceBookFriendsEmail(final List<String> friendFacebookIds) {
        final DatabaseReference facebookUsersRef = mDatabaseRef.child(FirebaseEndpoint.FACEBOOK_USERS);

        facebookUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> facebookUsers = dataSnapshot.getValue() == null ? new HashMap<String, String>() : (HashMap<String, String>)dataSnapshot.getValue();

                List<String> friendEmails = new ArrayList<>();

                for (String friendFacebookID : friendFacebookIds) {
                    String friendEmail = facebookUsers.get(friendFacebookID);
                    if (friendEmail != null) {
                        friendEmails.add(friendEmail);
                    }
                }
                getFriendPreferences(friendEmails);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    // Step 3: Get each friend's preference for the course, if any
    private void getFriendPreferences(final List<String> friendEmails) {
        courseFriends = new ArrayList<>();
        for (int i = 0; i < friendEmails.size(); i++) {
            final int currentCount = i;
            final DatabaseReference friendRef = mDatabaseRef.child(FirebaseEndpoint.USERS).child(friendEmails.get(i));
            friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    GenericTypeIndicator<ArrayList<Course>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<Course>>() {};
                    List<Course> listGoingToTake = dataSnapshot.child(FirebaseEndpoint.GOING_TO_TAKE).getValue(genericTypeIndicator) == null ?
                            new ArrayList<Course>() :
                            (ArrayList<Course>) dataSnapshot.child(FirebaseEndpoint.GOING_TO_TAKE).getValue(genericTypeIndicator);

                    Log.v(TAG, "listGoingToTake length is = " + listGoingToTake.size());

                    List<Course> listInterested = dataSnapshot.child(FirebaseEndpoint.INTERESTED).getValue() == null ?
                            new ArrayList<Course>() :
                            (ArrayList<Course>) dataSnapshot.child(FirebaseEndpoint.INTERESTED).getValue(genericTypeIndicator);

                    Log.v(TAG, "listInterested length is = " + listInterested.size());

                    List<Course> listTaken = dataSnapshot.child(FirebaseEndpoint.TAKEN).getValue() == null ?
                            new ArrayList<Course>() :
                            (ArrayList<Course>) dataSnapshot.child(FirebaseEndpoint.TAKEN).getValue(genericTypeIndicator);

                    Log.v(TAG, "listTaken length is = " + listTaken.size());

                    String currName = (String) dataSnapshot.child(FirebaseEndpoint.NAME).getValue();
                    String email = (String) dataSnapshot.child(FirebaseEndpoint.EMAIL).getValue();
                    String processedEmail = Utils.processEmail(email);

                    for(Course c : listGoingToTake){
                        if(c.courseCode.equals(courseCode)){
                            courseFriends.add(new CourseFriend(currName, "Going To Take", processedEmail));
                        }
                    }

                    for(Course c : listInterested){
                        if(c.courseCode.equals(courseCode)){
                            courseFriends.add(new CourseFriend(currName, "Interested", processedEmail));
                        }
                    }

                    for(Course c : listTaken){
                        if(c.courseCode.equals(courseCode)){
                            courseFriends.add(new CourseFriend(currName, "Taken", processedEmail));
                        }
                    }

                    if (currentCount == friendEmails.size() - 1) {
                        courseFriendAdapter = new CourseFriendAdapter(courseFriends, getActivity());
                        listCourseFriends.setAdapter(courseFriendAdapter);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }
}