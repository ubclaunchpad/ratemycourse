package com.example.coursify;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by sveloso on 2018-02-10.
 */
public class UserTabFragment extends Fragment {
    private static final String TAG = UserTabFragment.class.getSimpleName();

    private DatabaseReference mDatabaseRef;
    private DatabaseReference mUserRef;

    private TextView txtUserName;
    private TextView txtMajor;

    private TextView txtViewProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_tabs, container, false);

        txtUserName = view.findViewById(R.id.txtUserName);
        txtMajor = view.findViewById(R.id.txtMajor);
        txtViewProfile = view.findViewById(R.id.profileBtn);
        txtViewProfile.setPaintFlags(txtViewProfile.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mUserRef = mDatabaseRef.child(FirebaseEndpoint.USERS)
                .child(Utils.processEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail()));

        final TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Notes"));
        tabLayout.addTab(tabLayout.newTab().setText("Bookmarks"));
        tabLayout.addTab(tabLayout.newTab().setText("Friends"));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);


        final ViewPager viewPager = view.findViewById(R.id.pager);
        final PagerAdapter adapter = new UserTabAdapter
                (getChildFragmentManager(), tabLayout.getTabCount());
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

        getNameAndMajor();

        txtViewProfile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent mIntent = new Intent(getActivity(), UserPreferenceTabActivity.class);
                mIntent.putExtra("EMAIL", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                Log.v(TAG, "Proceeding to UserPreferenceActivity");
                startActivity(mIntent);
            }
        });

        return view;
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