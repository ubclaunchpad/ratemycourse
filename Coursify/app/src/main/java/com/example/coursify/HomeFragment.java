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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashSet;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by sveloso on 2017-11-04.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();
    private List<Course> listRecentlyOpened;
    private List<Course> listRecommended;
    private List<Course> listPopular;

    private RecyclerView mListRecentlyOpened;
    private RecyclerView.Adapter mRecentlyOpenedAdapter;
    private RecyclerView.LayoutManager mRecentlyOpenedManager;

    private RecyclerView mListRecommended;
    private RecyclerView.Adapter mRecommendedAdapter;
    private RecyclerView.LayoutManager mRecommendedManager;

    private RecyclerView mListPopular;
    private RecyclerView.Adapter mPopularAdapter;
    private RecyclerView.LayoutManager mPopularManager;

    private RelativeLayout emptyRecentlyOpened;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private View mView;

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
        mView = inflater.inflate(R.layout.fragment_home, container, false);

        mListRecentlyOpened = mView.findViewById(R.id.listRecentlyOpened);
        mListRecommended = mView.findViewById(R.id.listRecommended);
        mListPopular = mView.findViewById(R.id.listPopular);
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

        emptyRecentlyOpened = mView.findViewById(R.id.emptyRecentlyOpened);

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeCourses();
    }

    private void initializeCourses() {
        listRecentlyOpened = new ArrayList<>();
        listRecommended = new ArrayList<>();
        listPopular = new ArrayList<>();
        displayRecentlyOpenedCourses();
        getRecommendedFromDatabase();
        displayPopularCourses();
    }

    private void initializeFirebase() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    public void displayRecentlyOpenedCourses(){
        Log.v(TAG, "I am at displayCourses");
        final DatabaseReference recentlySearchedRef = mDatabase.child(FirebaseEndpoint.USERS).child(processedEmail).child(FirebaseEndpoint.RECENTLY_OPENED_COURSES);
        recentlySearchedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<HashMap<String, String>> recentlyOpenedCourses =
                        dataSnapshot.getValue() == null ? new ArrayList<HashMap<String, String>>() : (ArrayList<HashMap<String, String>>)dataSnapshot.getValue();
                ArrayList<String> recentlyOpenedIds = new ArrayList<String>();
                ArrayList<String> recentlyOpenedDescripts = new ArrayList<String>();
                listRecentlyOpened = new ArrayList<>();
                Collections.reverse(recentlyOpenedCourses);
                for(int i = 0; i < recentlyOpenedCourses.size(); i++) {
                    HashMap<String, String> currCourse = recentlyOpenedCourses.get(i);
                    String currCourseId = currCourse.get("courseCode");
                    String currCourseDescript = currCourse.get("courseTitle");
                    recentlyOpenedIds.add(currCourseId);
                    recentlyOpenedDescripts.add(currCourseDescript);
                    Course course = new Course(currCourseId, currCourseDescript);
                    listRecentlyOpened.add(course);
                    emptyRecentlyOpened.setVisibility(View.INVISIBLE);
                }

                if (listRecentlyOpened.size() == 0) {
                    emptyRecentlyOpened.setVisibility(View.VISIBLE);
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
                    uniYear = uniYear <= 0 ? 1 : uniYear;
                    uniYear = uniYear >= 5 ? 4 : uniYear;

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
                    listRecommended = new ArrayList<>();
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
        HashSet<String> set = new HashSet<>();
        List<Course> displayList = new ArrayList<>();
        for(int i = 0; i < listRecommended.size(); i++){
            Course c = listRecommended.get(i);
            if(set.contains(c.courseCode)){
                continue;
            }
            displayList.add(c);
            set.add(c.courseCode);
        }
        displayList = displayList.subList(0, Math.min(displayList.size(), 5));

        mRecommendedAdapter = new CourseAdapter(displayList, getResources().getColor(R.color.colorRecentlyOpened));
        mListRecommended.setAdapter(mRecommendedAdapter);
    }

    private void displayPopularCourses(){
        DatabaseReference popularCoursesRef = mDatabase.child(FirebaseEndpoint.POPULAR_COURSES);
        popularCoursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<HashMap<String, String>> popularCourses =
                        dataSnapshot.getValue() == null ? new ArrayList<HashMap<String, String>>() : (ArrayList<HashMap<String, String>>)dataSnapshot.getValue();
                listPopular = new ArrayList<>();
                for(int i = 0; i < popularCourses.size(); i++){
                    HashMap<String, String> currPopCourse = popularCourses.get(i);
                    String code = currPopCourse.get("courseCode");
                    String descript = currPopCourse.get("courseTitle");
                    Course currCourse = new Course(code, descript);
                    listPopular.add(currCourse);
                }
                mPopularAdapter = new CourseAdapter(listPopular, getResources().getColor(R.color.colorRecentlyOpened));
                mListPopular.setAdapter(mPopularAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
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