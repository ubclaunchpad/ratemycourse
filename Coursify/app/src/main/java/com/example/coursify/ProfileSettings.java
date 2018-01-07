package com.example.coursify;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import static com.example.coursify.Utils.processEmail;

public class ProfileSettings extends AppCompatActivity {
    private static final String TAG = ProfileSettings.class.getSimpleName();

    private LoginButton mLoginButton;
    private CallbackManager mCallBackManager;
    private AccessToken mAccessToken;
    private String mUserId;
    EditText mEmail, mName, mMajor, mGradDate, mInterest;
    String email, name, major, gradDate, interest;
    Button mSubmit, mChangePass;
    boolean found = false;

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
        email = bundle.getString("Email");
        findViewsByIds();

        mLoginButton.setReadPermissions("user_friends", "email");
        mCallBackManager = CallbackManager.Factory.create();

        mLoginButton.registerCallback(mCallBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.v(TAG, "Successfully connected to FaceBook.");
                mAccessToken = loginResult.getAccessToken();
                mUserId = loginResult.getAccessToken().getUserId();
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
    protected void findViewsByIds(){
        mLoginButton = (LoginButton) findViewById(R.id.login_button);
        mEmail = (EditText)findViewById(R.id.emailInput);
        mName = (EditText)findViewById(R.id.nameInput);
        mMajor = (EditText)findViewById(R.id.majorInput);
        mGradDate = (EditText)findViewById(R.id.gradInput);
        mInterest = (EditText) findViewById(R.id.interestInput);
        mSubmit = (Button)findViewById(R.id.submit);
        mChangePass = (Button)findViewById(R.id.changePass);
    }

    protected void setFacebookUserId(final String email, final String mUserId){
        Log.v(TAG, "and my facebook user id in set Facebook UserId is:" + mUserId);
        DatabaseReference firebasereference, fbUserReference, userFbIdReference;
        firebasereference = FirebaseDatabase.getInstance().getReference();
        String processedEmail = processEmail(email);
        fbUserReference = firebasereference.child(FirebaseEndpoint.FACEBOOK_USERS).child(mUserId);
        fbUserReference.setValue(processedEmail);
        userFbIdReference = firebasereference.child(FirebaseEndpoint.USERS).child(processedEmail).child(FirebaseEndpoint.FACEBOOK_ID);
        userFbIdReference.setValue(mUserId);
    }

    protected void collectInformation(final String email, final String mUserId){
        mEmail.setText(email.trim());
        mEmail.setFocusable(false);

        // shows user information in form if user has registered before
        DatabaseReference firebasereference, userReference;
        firebasereference = FirebaseDatabase.getInstance().getReference();
        userReference = firebasereference.child(FirebaseEndpoint.USERS);
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.getKey().toString().equals(processEmail(email))) {
                        HashMap<String, String> currUser = (HashMap)ds.getValue();
                        String showName = currUser.get("name");
                        String showGradDate = currUser.get("gradDate");
                        String showMajor = currUser.get("major");
                        String showInterest = currUser.get("interest");

                        mName.setText(showName.trim());
                        mGradDate.setText(showGradDate.trim());
                        mMajor.setText(showMajor.trim());
                        mInterest.setText(showInterest.trim());

                        found = true;
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
            name = mName.getText().toString();
            major = mMajor.getText().toString();
            gradDate = mGradDate.getText().toString();
            interest = mInterest.getText().toString();
            Log.v(TAG, "my interest is:" + interest);
            updateProfile(email, name, major, gradDate, interest, found);
            }
        });

        mChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(getApplicationContext(), ChangePassword.class);
                Log.v(TAG, "Proceeding to ChangePassword");
                startActivity(mIntent);
            }
        });
    }

    protected void updateProfile(String email, String name, String major, String gradDate,
                                 String interest, boolean found){
        Log.v(TAG, "found is " + found);
        DatabaseReference firebasereference, userReference;
        firebasereference = FirebaseDatabase.getInstance().getReference();
        String processedEmail = processEmail(email);
        userReference = firebasereference.child(FirebaseEndpoint.USERS).child(processedEmail);
        if(name.length() == 0 || major.length() == 0 || gradDate.length() == 0){
            Log.v(TAG, "You need to fill out all of name, major and gradDate fields");
        }else{
            userReference.child("email").setValue(email);
            userReference.child("name").setValue(name);
            userReference.child("gradDate").setValue(gradDate);
            userReference.child("major").setValue(major);
            userReference.child("interest").setValue(interest);
            return;
        }
    }
}
