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
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.example.coursify.Utils.processEmail;


public class UserFriendsFragment extends Fragment {
    private static final String TAG = UserFriendsFragment.class.getSimpleName();
    CallbackManager mCallBackManager;

    private AccessToken mAccessToken;
    private String mUserId;
    private String email;

    List<User> userList = new ArrayList<>();
    FriendListAdapter friendListAdapter;
    DatabaseReference ref;
    private FirebaseAuth mAuth;
    private AccessToken accessToken;
    private AccessTokenTracker accessTokenTracker;

//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        mCallBackManager.onActivityResult(requestCode, resultCode, data);
//    }

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

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged (
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                Log.v(TAG, "access token changed");
                accessToken = currentAccessToken;
            }
        };
        accessToken = AccessToken.getCurrentAccessToken();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_friends, container, false);

        LoginButton mLoginButton;
        mLoginButton = (LoginButton) view.findViewById(R.id.btnFBLogin);
        mLoginButton.setReadPermissions("user_friends", "email");
        mCallBackManager = CallbackManager.Factory.create();

        mLoginButton.registerCallback(mCallBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.v(TAG, "Successfully connected to FaceBook.");
                mAccessToken = loginResult.getAccessToken();
                mUserId = loginResult.getAccessToken().getUserId();
                setFacebookUserId(email, mUserId);
            }

            @Override
            public void onCancel() {
                Log.v(TAG, "Cancelled login to FaceBook.");
            }

            @Override
            public void onError(FacebookException error) {
                Log.v(TAG, "Error logging in to FaceBook");
                Log.v(TAG, error.getMessage());
            }
        });

        RecyclerView recyclerView;
        recyclerView = view.findViewById(R.id.friendsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ref = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        this.friendListAdapter = new FriendListAdapter(userList);
        recyclerView.setAdapter(friendListAdapter);

        ref.child(FirebaseEndpoint.USERS)
                .child(Utils.processEmail(mAuth.getCurrentUser().getEmail()))
                .child(FirebaseEndpoint.FACEBOOK_ID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String fbID = dataSnapshot.getValue(String.class);
                        getFBFriends(fbID);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, databaseError.toString());
                    }
                });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    protected void setFacebookUserId(final String email, final String mUserId){
        Log.v(TAG, "and my facebook user id in set Facebook UserId is:" + mUserId);
        DatabaseReference firebasereference, fbUserReference, userFbIdReference;
        firebasereference = FirebaseDatabase.getInstance().getReference();
        String processedEmail = processEmail(email);
        fbUserReference = firebasereference.child(FirebaseEndpoint.FACEBOOK_USERS).child(mUserId);
        fbUserReference.setValue(processedEmail);
        userFbIdReference = firebasereference.child(FirebaseEndpoint.USERS).child(processedEmail)
                .child(FirebaseEndpoint.FACEBOOK_ID);
        userFbIdReference.setValue(mUserId);
    }

    private void getFriendInfo(String fbID) {
        ref.child(FirebaseEndpoint.FACEBOOK_USERS)
                .child(fbID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String email = dataSnapshot.getValue(String.class);
                        Log.v(TAG, "email is " + email);
                        if(email != null) {
                            ref.child(FirebaseEndpoint.USERS)
                                    .child(email)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String friendName = "";
                                            String friendMajor = "";
                                            for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                if(snapshot.getKey().equals(FirebaseEndpoint.MAJOR)) {
                                                    friendMajor = snapshot.getValue(String.class);
                                                }
                                                else if (snapshot.getKey().equals(FirebaseEndpoint.NAME)) {
                                                    friendName = snapshot.getValue(String.class);
                                                }
                                            }
                                            userList.add(new User(friendName, friendMajor));
                                            Log.v(TAG, "the size is: " + userList.size());
                                            friendListAdapter.notifyItemInserted(userList.size() - 1);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    /**
     * Fetch a list of Facebook friends using Facebook SDK
     * Update the user/facebookFriends field in Firebase with
     * the new list
     * @param mFBUserId
     */
    private void getFBFriends(String mFBUserId) {
        if(accessToken == null) {
            Toast.makeText(getActivity(), "please login to facebook", Toast.LENGTH_LONG).show();
        }
        else {
            new GraphRequest(
                    accessToken,
                    "/" + mFBUserId + "/friends",
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            JSONObject object = response.getJSONObject();
                            try {
                                JSONArray arrayOfUsersInFriendList = object.getJSONArray("data");
                                Log.v(TAG, "User friend list length: " + arrayOfUsersInFriendList.length());
                                List<String> fbFriends = new ArrayList<>();

                                for(int i = 0; i < arrayOfUsersInFriendList.length(); i++) {
                                    JSONObject user = arrayOfUsersInFriendList.getJSONObject(i);
                                    String userId = user.getString("id");
                                    Log.v(TAG, "user id: " + userId);
                                    Log.v(TAG, "user name is " + user.getString("name"));
                                    getFriendInfo(userId);
                                    fbFriends.add(userId);
                                }
                                ref.child(FirebaseEndpoint.USERS)
                                        .child(Utils.processEmail(mAuth.getCurrentUser().getEmail()))
                                        .child(FirebaseEndpoint.FACEBOOK_FRIENDS)
                                        .setValue(fbFriends);

                            } catch (JSONException e) {
                                Log.v(TAG, e.toString());
                                e.printStackTrace();
                            }
                        }
                    }
            ).executeAsync();
        }
    }
}