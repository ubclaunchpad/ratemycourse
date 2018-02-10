package com.example.coursify;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    Button submit;
    EditText oldPass;
    EditText newPass;
    EditText newPassCheck;
    String email;

    FirebaseUser user;
    private static final String TAG = UserSettingsFragment.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null){
            return;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // User is not signed in
            return;
        }
        email = user.getEmail();
        findViewsByIds();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validatePasswordsAndCreateResponse();
            }
        });
    }

    protected void findViewsByIds(){
        submit = (Button)findViewById(R.id.submit);
        oldPass = (EditText)findViewById(R.id.oldPassInput);
        newPass = (EditText)findViewById(R.id.newPassInput);
        newPassCheck = (EditText)findViewById(R.id.newPassInputCheck);
    }

    protected void validatePasswordsAndCreateResponse(){
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), oldPass.getText().toString());

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPass.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Password updated");
                                    } else {
                                        Log.d(TAG, "Error password update failed");
                                    }
                                }
                            });
                        } else {
                            Log.d(TAG, "Error auth failed");
                        }
                    }
                });
    }

    // Get auth credentials from the user for re-authentication. The example below shows
// email and password credentials but there are multiple possible providers,
// such as GoogleAuthProvider or FacebookAuthProvider.


// Prompt the user to re-provide their sign-in credentials

}
