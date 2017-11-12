package com.example.coursify;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.FacebookSdk;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import static android.R.attr.name;

public class ProfileSettings extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();

    private LoginButton mLoginButton;
    private CallbackManager mCallBackManager;
    private AccessToken mAccessToken;
    private String mUserId;

    public static class User {
        public String major;
        public String name;
        public String gradDate;
        public String facebookID;
        public User(String name, String major, String gradDate, String facebookID){
            this.major = major;
            this.name = name;
            this.gradDate = gradDate;
            this.facebookID = facebookID;
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallBackManager.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);
        Bundle bundle = getIntent().getExtras();
        final String email = bundle.getString("Email");

        mLoginButton = (LoginButton) findViewById(R.id.login_button);
        mLoginButton.setReadPermissions("user_friends", "email");
        mCallBackManager = CallbackManager.Factory.create();

        mLoginButton.registerCallback(mCallBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.v(TAG, "Successfully connected to FaceBook.");
                mAccessToken = loginResult.getAccessToken();
                mUserId = loginResult.getAccessToken().getUserId();
                Log.v(TAG, "HERE IS MY USERID" + mUserId);
                setFacebookUserId(email, mUserId);
                collectInformation(email, mUserId);
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

        collectInformation(email, mUserId);
    }

    protected void setFacebookUserId(final String email, final String mUserId){
        Log.v(TAG, "and my facebook user id in set Facebook UserId is:" + mUserId);
        DatabaseReference firebasereference, userReference;
        firebasereference = FirebaseDatabase.getInstance().getReference();
        String processedEmail = processString(email);
        userReference = firebasereference.child("FacebookUsers").child(mUserId);
        userReference.setValue(processedEmail);
    }

    protected void collectInformation(final String email, final String mUserId){
        final EditText mEmail = (EditText)findViewById(R.id.emailInput);
        final EditText mName = (EditText)findViewById(R.id.nameInput);
        final EditText mMajor = (EditText)findViewById(R.id.majorInput);
        final EditText mGradDate = (EditText)findViewById(R.id.gradInput);
        Button mSubmit = (Button)findViewById(R.id.submit);
        mEmail.setText(email.trim());
        mEmail.setFocusable(false);

        DatabaseReference firebasereference, userReference;
        firebasereference = FirebaseDatabase.getInstance().getReference();
        userReference = firebasereference.child("Users");
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.getKey().toString().equals(processString(email))) {
                        Log.v(TAG, "found user");
                        HashMap<String, String> currUser = (HashMap)ds.getValue();
                        Log.v(TAG, "i WAS HERE");
                        String showName = currUser.get("name");
                        String showGradDate = currUser.get("gradDate");
                        String showMajor = currUser.get("major");
                        mName.setText(showName);
                        mMajor.setText(showMajor);
                        mGradDate.setText(showGradDate);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString();
                final String name = mName.getText().toString();
                final String major = mMajor.getText().toString();
                final String gradDate = mGradDate.getText().toString();
                updateProfile(email, name, major, gradDate, mUserId);
            }
        });
    }

    protected void updateProfile(String email, String name, String major, String gradDate, String facebookID){
        DatabaseReference firebasereference, userReference;
        firebasereference = FirebaseDatabase.getInstance().getReference();
        String processedEmail = processString(email);
        userReference = firebasereference.child("Users").child(processedEmail);
        if(name.length() == 0 || major.length() == 0 || gradDate.length() == 0){
            Log.v(TAG, "You need to fill out all of name, major and gradDate fields");
        }else{
            userReference.setValue(new User(name, major, gradDate, facebookID));
        }
    }

    protected String processString(String email){
        int i = email.indexOf('@');
        email = email.substring(0, i) + ";at;" + email.substring(i+1);
        for(int k = 0; k < email.length(); k++){
            if(email.charAt(k) == '.'){
                email = email.substring(0, k) + ";dot;" + email.substring(k+1);
            }
        }
        return email;
    }
}
