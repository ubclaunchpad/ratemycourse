package com.example.coursify;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dgreenhalgh.android.simpleitemdecoration.linear.DividerItemDecoration;
import com.dgreenhalgh.android.simpleitemdecoration.linear.EndOffsetItemDecoration;
import com.dgreenhalgh.android.simpleitemdecoration.linear.StartOffsetItemDecoration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseUser;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by sveloso on 2017-11-04.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();
    private List<Course> listRecentlyOpened;
    private List<Course> listRecommended;

    private RecyclerView mListRecentlyOpened;
    private RecyclerView.Adapter mRecentlyOpenedAdapter;
    private RecyclerView.LayoutManager mRecentlyOpenedManager;

    private RecyclerView mListRecommended;
    private RecyclerView.Adapter mRecommendedAdapter;
    private RecyclerView.LayoutManager mRecommendedManager;

    private RecyclerView mListPopular;
    private RecyclerView.Adapter mPopularAdapter;
    private RecyclerView.LayoutManager mPopularManager;

    TextView emptyRecentlyOpened;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    String email, processedEmail;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // User is not signed in
            return;
        }
        email = user.getEmail();
        processedEmail = Utils.processEmail(email);

        initializeFirebase();
        initializeCourses();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mListRecentlyOpened = view.findViewById(R.id.listRecentlyOpened);
        mListRecommended = view.findViewById(R.id.listRecommended);
        mListPopular = view.findViewById(R.id.listPopular);
        mListRecommended.setHasFixedSize(true);
        mListRecentlyOpened.setHasFixedSize(true);
        mListPopular.setHasFixedSize(true);

        mRecentlyOpenedManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mRecommendedManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mPopularManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        mListRecentlyOpened.setLayoutManager(mRecentlyOpenedManager);
        mListRecommended.setLayoutManager(mRecommendedManager);
        mListPopular.setLayoutManager(mPopularManager);

        mRecentlyOpenedAdapter = new CourseAdapter(listRecentlyOpened, getResources().getColor(R.color.colorRecentlyOpened));
        mRecommendedAdapter = new CourseAdapter(listRecommended, getResources().getColor(R.color.colorRecommended));
        mPopularAdapter = new CourseAdapter(listRecommended, getResources().getColor(R.color.colorPopular));
        mListRecentlyOpened.setAdapter(mRecentlyOpenedAdapter);
        mListRecommended.setAdapter(mRecommendedAdapter);
        mListPopular.setAdapter(mPopularAdapter);

        DividerItemDecoration itemDivider = new DividerItemDecoration(ContextCompat.getDrawable(getApplicationContext(), R.drawable.divider));
        mListRecentlyOpened.addItemDecoration(itemDivider);
        mListRecentlyOpened.addItemDecoration(new StartOffsetItemDecoration(30));
        mListRecentlyOpened.addItemDecoration(new EndOffsetItemDecoration(30));

        mListRecommended.addItemDecoration(itemDivider);
        mListRecommended.addItemDecoration(new StartOffsetItemDecoration(30));
        mListRecommended.addItemDecoration(new EndOffsetItemDecoration(30));

        mListPopular.addItemDecoration(itemDivider);
        mListPopular.addItemDecoration(new StartOffsetItemDecoration(30));
        mListPopular.addItemDecoration(new EndOffsetItemDecoration(30));

        emptyRecentlyOpened = view.findViewById(R.id.emptyRecentlyOpened);
        //displayCourses();
        //getRecentlyOpenedFromDatabase();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //getRecentlyOpenedFromDatabase();
        //displayCourses();
    }


    /**
     * todo remove this after implementing recommended and popular
     */
    private void initializeCourses() {
        Course c1 = new Course("CPSC 110", "Differential Calculus with Applications to Physical Sciences and Engineering");
        Course c2 = new Course("ONCO 649", "Doctoral Dissertation");
        Course c3 = new Course("CPSC 210", "CPSC 210 L1K (Laboratory)");
        Course c4 = new Course("PLAN 425", "Urban Planning Issues and Concepts");
        Course c5 = new Course("LARC 415", "The Profession of Planning");
        Course c6 = new Course("LASO 204", "The Profession of Planning");
        Course c7 = new Course("LATN 102", "The Profession of Planning");
        Course c8 = new Course("LIBE 465", "The Profession of Planning");
        Course c9 = new Course("OBST 430", "The Profession of Planning");
        Course c10 = new Course("OBST 649", "The Profession of Planning");

        listRecentlyOpened = new ArrayList<>();
        listRecommended = new ArrayList<>();
        displayCourses();
        getRecommendedFromDatabase();

        /*
        listRecommended.add(c1);
        listRecommended.add(c3);
        listRecommended.add(c2);
        listRecommended.add(c4);
        listRecommended.add(c5);
        listRecommended.add(c6);
        listRecommended.add(c7);
        listRecommended.add(c8);
        listRecommended.add(c9);
        listRecommended.add(c10);
        */
    }

    private void initializeFirebase() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    public void showProfilePage(View view) {
        // startActivity(new Intent(this, UserFriendsFragment.class));
    }

    public void displayCourses(){
        Log.v(TAG, "I am at displayCourses");
        final DatabaseReference recentlySearchedRef = mDatabase.child(FirebaseEndpoint.USERS).child(processedEmail).child(FirebaseEndpoint.RECENTLY_OPENED_COURSES);
        recentlySearchedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<HashMap<String, String>> recentlyOpenedCourses =
                        dataSnapshot.getValue() == null ? new ArrayList<HashMap<String, String>>() : (ArrayList<HashMap<String, String>>)dataSnapshot.getValue();
                ArrayList<String> recentlyOpenedIds = new ArrayList<String>();
                ArrayList<String> recentlyOpenedDescripts = new ArrayList<String>();
                Collections.reverse(recentlyOpenedCourses);
                for(int i = 0; i < recentlyOpenedCourses.size(); i++){
                    HashMap<String, String> currCourse = recentlyOpenedCourses.get(i);
                    String currCourseId = currCourse.get("courseCode");
                    String currCourseDescript = currCourse.get("courseTitle");
                    recentlyOpenedIds.add(currCourseId);
                    recentlyOpenedDescripts.add(currCourseDescript);
                    Course course = new Course(currCourseId, currCourseDescript);
                    listRecentlyOpened.add(course);
                }
                Log.v(TAG, "size of recentlySearchedCourses is "+recentlyOpenedCourses.size());
                mRecentlyOpenedAdapter = new CourseAdapter(listRecentlyOpened, getResources().getColor(R.color.colorRecentlyOpened));
                mListRecentlyOpened.setAdapter(mRecentlyOpenedAdapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getRecommendedFromDatabase() {
        DatabaseReference userRef = mDatabase.child(FirebaseEndpoint.USERS).child(Utils.processEmail(email));
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    HashMap<String, String> currUser = (HashMap)dataSnapshot.getValue();

                    String interest = currUser.get("interest");
                    String major = currUser.get("major");
                    String gradDate = currUser.get("gradDate");

                    int iGradDate = Integer.parseInt(gradDate);
                    Calendar now = Calendar.getInstance();
                    int currYear = now.get(Calendar.YEAR);
                    int uniYear = 4 - (iGradDate - currYear);

                    if(uniYear < 0 || uniYear > 4) {
                        //emptyRecommended.setVisibility(View.VISIBLE);
                        return;
                    }
                    getInterestCourseCodesHelper(interest, major, uniYear);
                }
                else {
                    //emptyRecentlyOpened.setVisibility(View.VISIBLE);
                    Log.v(TAG, "invalid user wtf");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getInterestCourseCodesHelper(String interest, final String major, final int uniYear){
        String uniYearChild = "Year " + uniYear;
        DatabaseReference interestRef = mDatabase.child(FirebaseEndpoint.COURSES).child(interest).child(uniYearChild);
        interestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Log.v(TAG, "getInterestCourseCodesHelper");
                    HashMap<String, HashMap<String, String>> majorCourseMap = (HashMap)dataSnapshot.getValue();
                    for (String key : majorCourseMap.keySet()) {
                        String descript = majorCourseMap.get(key).get("Description");
                        Course course = new Course(Utils.courseCodeFormatter(key), descript);
                        listRecommended.add(course);
                    }
                }
                getMajorCourseCodesHelper(major, uniYear);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getMajorCourseCodesHelper(String major, int uniYear){
        String uniYearChild = "Year " + uniYear;
        DatabaseReference interestRef = mDatabase.child(FirebaseEndpoint.COURSES).child(major).child(uniYearChild);
        interestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Log.v(TAG, "getMajorCourseCodesHelper");
                    HashMap<String, HashMap<String, String>> majorCourseMap = (HashMap)dataSnapshot.getValue();
                    for (String key : majorCourseMap.keySet()) {
                        String descript = majorCourseMap.get(key).get("Description");
                        Course course = new Course(Utils.courseCodeFormatter(key), descript);
                        listRecommended.add(course);
                    }
                }
                getRecommendedCoursesCallback();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getRecommendedCoursesCallback(){
        Collections.shuffle(listRecommended);
        int size = listRecommended.size() <= 5 ? listRecommended.size() : 5;
        ArrayList<Course> displayList = new ArrayList<>();
        for(int i = 0; i < size; i++){
            displayList.add(listRecommended.get(i));
        }
        mRecommendedAdapter = new CourseAdapter(displayList, getResources().getColor(R.color.colorRecentlyOpened));
        mListRecommended.setAdapter(mRecommendedAdapter);

    }

    public void showProfileSettings(View view){
        Intent profSetting = new Intent(getApplicationContext(), UserSettingsFragment.class);
        Bundle bundle = new Bundle();
        bundle.putString("Email", email);
        profSetting.putExtras(bundle);
        Log.v(TAG, "proceeding to profile settings activity");
        startActivity(profSetting);
    }
}