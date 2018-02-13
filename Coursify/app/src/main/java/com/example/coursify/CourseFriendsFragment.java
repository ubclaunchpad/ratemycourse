package com.example.coursify;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CourseFriendsFragment extends Fragment {

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
                    List<String> listGoingToTake = dataSnapshot.child(FirebaseEndpoint.GOING_TO_TAKE).getValue() == null ?
                            new ArrayList<String>() :
                            (ArrayList<String>) dataSnapshot.child(FirebaseEndpoint.GOING_TO_TAKE).getValue();

                    List<String> listInterested = dataSnapshot.child(FirebaseEndpoint.INTERESTED).getValue() == null ?
                            new ArrayList<String>() :
                            (ArrayList<String>) dataSnapshot.child(FirebaseEndpoint.INTERESTED).getValue();

                    List<String> listTaken = dataSnapshot.child(FirebaseEndpoint.TAKEN).getValue() == null ?
                            new ArrayList<String>() :
                            (ArrayList<String>) dataSnapshot.child(FirebaseEndpoint.TAKEN).getValue();

                    String currName = (String) dataSnapshot.child(FirebaseEndpoint.NAME).getValue();

                    if (listGoingToTake.contains(courseCode)) {
                        courseFriends.add(new CourseFriend(currName, "Going To Take"));
                    } else if (listInterested.contains(courseCode)) {
                        courseFriends.add(new CourseFriend(currName, "Interested"));
                    } else if (listTaken.contains(courseCode)){
                        courseFriends.add(new CourseFriend(currName, "Taken"));
                    }

                    if (currentCount == friendEmails.size() - 1) {
                        courseFriendAdapter = new CourseFriendAdapter(courseFriends);
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