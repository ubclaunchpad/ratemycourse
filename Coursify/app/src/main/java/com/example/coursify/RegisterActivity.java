package com.example.coursify;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText emailInput = (EditText)findViewById(R.id.emailInput);
        final EditText passwordInput = (EditText)findViewById(R.id.passwordInput);
        final EditText verifyPasswordInput = (EditText)findViewById(R.id.verifyPasswordInput);
        Button verifyEmailBtn = (Button)findViewById(R.id.registerButton);
        verifyEmailBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();
                String verifyPassword = verifyPasswordInput.getText().toString();
                if(password.equals(verifyPassword)){
                    createUser(email, password);
                }else{
                    Toast.makeText(getApplicationContext(), "Invalid password, please enter again", Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    protected void createUser(String email, String password) {
        final FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            final FirebaseUser user = mAuth.getCurrentUser();
                            user.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(), "Email sent to " + user.getEmail(), Toast.LENGTH_LONG).show();
                                                Intent mIntent = new Intent(getApplicationContext(), LoginActivity.class);
                                                startActivity(mIntent);
                                            }else {
                                                Toast.makeText(getApplicationContext(), "Email failed to send", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                        }else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "User already exists",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}
