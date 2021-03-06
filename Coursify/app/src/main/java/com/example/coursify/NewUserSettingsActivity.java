package com.example.coursify;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import static com.example.coursify.Utils.processEmail;

public class NewUserSettingsActivity extends AppCompatActivity {

    private static final String TAG = UserSettingsFragment.class.getSimpleName();

    private LoginButton mLoginButton;
    private CallbackManager mCallBackManager;
    private AccessToken mAccessToken;
    private String mUserId;
    EditText mEmail, mName, mMajor, mGradDate, mInterest;
    String email, name, major, gradDate, interest;
    Button mSubmit, mChangePass;
    boolean found = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_settings);
        FacebookSdk.sdkInitialize(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // User is not signed in
            return;
        }
        email = user.getEmail();

        mLoginButton = findViewById(R.id.login_button);
        mEmail = findViewById(R.id.emailInput);
        mEmail.setText(email);
        mEmail.setEnabled(false);
        mName = findViewById(R.id.nameInput);
        mMajor = findViewById(R.id.majorInput);
        mGradDate = findViewById(R.id.gradInput);
        mInterest = findViewById(R.id.interestInput);
        mSubmit = findViewById(R.id.submit);
        mChangePass = findViewById(R.id.changePass);

        mLoginButton.setReadPermissions("user_friends", "email");
        mCallBackManager = CallbackManager.Factory.create();

        mLoginButton.registerCallback(mCallBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.v(TAG, "Successfully connected to FaceBook.");
                Toast.makeText(getApplicationContext(), "Successfully connected to Facebook. ", Toast.LENGTH_SHORT).show();
                mAccessToken = loginResult.getAccessToken();
                mUserId = loginResult.getAccessToken().getUserId();
                setFacebookUserId(email, mUserId);
             //   collectInformation(email, mUserId);
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

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = mName.getText().toString();
                major = mMajor.getText().toString();
                gradDate = mGradDate.getText().toString();
                interest = mInterest.getText().toString();
                Log.v(TAG, "my interest is:" + interest);
                updateProfile(email, name, major, gradDate, interest, found);
                Intent mIntent = new Intent(NewUserSettingsActivity.this, NavigationActivity.class);
                startActivity(mIntent);
            }
        });

        mChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(NewUserSettingsActivity.this, ChangePasswordActivity.class);
                Log.v(TAG, "Proceeding to ChangePasswordActivity");
                startActivity(mIntent);
            }
        });

      //  collectInformation(email, mUserId);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallBackManager.onActivityResult(requestCode, resultCode, data);
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