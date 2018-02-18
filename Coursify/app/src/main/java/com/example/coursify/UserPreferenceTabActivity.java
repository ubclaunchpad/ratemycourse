package com.example.coursify;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by sveloso on 2018-01-20.
 */

public class UserPreferenceTabActivity extends AppCompatActivity {
    private static final String TAG = UserPreferenceTabActivity.class.getSimpleName();

    private TextView txtUserName;
    private TextView txtMajor;

    private DatabaseReference mDatabase;
    private DatabaseReference mUserRef;

    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        email = getIntent().getStringExtra("EMAIL");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_pref_tab);
        txtUserName = findViewById(R.id.txtUserName);
        txtMajor = findViewById(R.id.txtMajor);

        //Set up Database Info & retrieve user info:
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            return;
        }
        email = email == null ? user.getEmail() : email;
        email = Utils.processEmail(email);

        Log.v(TAG, "the email in userPrefTab is + " + email);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUserRef = mDatabase.child(FirebaseEndpoint.USERS).child(email);

        getNameAndMajor();

        //TabLayout
        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Interested"));
        tabLayout.addTab(tabLayout.newTab().setText("Will Take"));
        tabLayout.addTab(tabLayout.newTab().setText("Taken"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        //Pager
        final ViewPager viewPager = findViewById(R.id.pager);
        final PagerAdapter adapter = new UserPreferenceTabAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount(), email);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        tabLayout.setTabTextColors(getResources().getColor(R.color.colorViolet), getResources().getColor(R.color.colorViolet));
    }

    private void getNameAndMajor() {
        mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child(FirebaseEndpoint.NAME).getValue();
                String major = (String) dataSnapshot.child(FirebaseEndpoint.MAJOR).getValue();

                txtUserName.setText(name);
                txtMajor.setText(major);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}