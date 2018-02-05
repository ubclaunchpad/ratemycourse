package com.example.coursify;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    EditText courseInput;
    Button submit;
    String email, processedEmail;
    private static final String TAG = SearchActivity.class.getSimpleName();
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    DatabaseReference ref;
    DatabaseReference refSearch;

    RecyclerView recView;
    RecyclerView.Adapter searchCourseAdapter;

    ArrayList<String> searchedCourses = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        submit = (Button)findViewById(R.id.submit);
        recView = (RecyclerView)findViewById(R.id.listSearchCourses);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        boolean emailVerified = user.isEmailVerified();
        if(!emailVerified) {
            return;
        }
        email = user.getEmail();
        processedEmail = Utils.processEmail(email);

        setFields(processedEmail);
        //displaying courses:
        getRecentlySearchedAndSaveCourse("");

        LinearLayoutManager LLM = new LinearLayoutManager(this);
        recView.setLayoutManager(LLM);
        //searchCourseAdapter = new SearchCourseAdapter(searchedCourses, this);
        //recView.setAdapter(searchCourseAdapter);
        // when clicking submit, we want to modify our recentlySearched field and jump to the course
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                courseInput = (EditText)findViewById(R.id.searchField);
                String courseId = courseInput.getText().toString();
                getRecentlySearchedAndSaveCourse(courseId);
            }
        });
    }

    protected void setFields(String processedEmail){
        this.ref = database.child(FirebaseEndpoint.USERS).child(processedEmail);
        this.refSearch = ref.child(FirebaseEndpoint.RECENTLY_SEARCHED);
    }

    protected void getRecentlySearchedAndSaveCourse(final String courseId){

        refSearch.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getRecentlySearchedCallback(courseId, (ArrayList<String>)dataSnapshot.getValue());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    public void getRecentlySearchedCallback(String courseId, ArrayList<String> recentlySearched){
        Log.v(TAG, courseId);

        if(recentlySearched == null){
            recentlySearched = new ArrayList<String>();
        }
        if(courseId.length() == 0){
            searchedCourses = recentlySearched;
            searchCourseAdapter = new SearchCourseAdapter(searchedCourses, this);
            recView.setAdapter(searchCourseAdapter);
            return;
        }

        for(int i = 0; i < recentlySearched.size(); i++) {
            if (recentlySearched.get(i).equals(courseId)) {
                recentlySearched.remove(i);
                recentlySearched.add(courseId);
                refSearch.setValue(recentlySearched);
                Intent intent = new Intent(getApplicationContext(), CourseTabActivity.class);
                intent.putExtra("COURSE_CODE", courseId);
                startActivity(intent);
                return;
            }
        }
        recentlySearched.add(courseId);
        refSearch.setValue(recentlySearched);
        Intent intent = new Intent(getApplicationContext(), CourseTabActivity.class);
        intent.putExtra("COURSE_CODE", courseId);
        startActivity(intent);
    }
}
