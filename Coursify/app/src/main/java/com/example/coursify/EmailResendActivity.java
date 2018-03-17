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
import com.google.firebase.auth.FirebaseAuth;

public class EmailResendActivity extends AppCompatActivity {

    Button submit;
    EditText email;
    private static final String TAG = EmailResendActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_resend);
        submit = (Button)findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                email = (EditText)findViewById(R.id.emailInput);
                String emailAddress = email.getText().toString();
                auth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Email sent", Toast.LENGTH_LONG).show();
                                    Log.d(TAG, "Email sent.");
                                    Intent mIntent = new Intent(getApplicationContext(), MainActivity.class);
                                    Log.v(TAG, "Proceeding to MainActivity");
                                    startActivity(mIntent);
                                }else{
                                    Toast.makeText(getApplicationContext(), "Email failed to send", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

    }
}
