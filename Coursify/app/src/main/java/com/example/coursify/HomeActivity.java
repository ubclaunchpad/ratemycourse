package com.example.coursify;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.dgreenhalgh.android.simpleitemdecoration.linear.DividerItemDecoration;
import com.dgreenhalgh.android.simpleitemdecoration.linear.EndOffsetItemDecoration;
import com.dgreenhalgh.android.simpleitemdecoration.linear.StartOffsetItemDecoration;
import com.example.coursify.Course;
import com.example.coursify.CourseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sveloso on 2017-11-04.
 */
public class HomeActivity extends Activity {

    private List<Course> listRecentlyOpened;

    private RecyclerView mListRecentlyOpened;
    private RecyclerView.Adapter mRecentlyOpenedAdapter;
    private RecyclerView.LayoutManager mRecentlyOpenedManager;

    private RecyclerView mListRecommended;
    private RecyclerView.Adapter mRecommendedAdapter;
    private RecyclerView.LayoutManager mRecommendedManager;

    private RecyclerView mListPopular;
    private RecyclerView.Adapter mPopularAdapter;
    private RecyclerView.LayoutManager mPopularManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeCourses();

        mListRecentlyOpened = (RecyclerView) findViewById(R.id.listRecentlyOpened);
        mListRecommended = (RecyclerView) findViewById(R.id.listRecommended);
        mListPopular = (RecyclerView) findViewById(R.id.listPopular);
        mListRecommended.setHasFixedSize(true);
        mListRecentlyOpened.setHasFixedSize(true);
        mListPopular.setHasFixedSize(true);

        mRecentlyOpenedManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecommendedManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mPopularManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        mListRecentlyOpened.setLayoutManager(mRecentlyOpenedManager);
        mListRecommended.setLayoutManager(mRecommendedManager);
        mListPopular.setLayoutManager(mPopularManager);

        mRecentlyOpenedAdapter = new CourseAdapter(listRecentlyOpened);
        mRecommendedAdapter = new CourseAdapter(listRecentlyOpened);
        mPopularAdapter = new CourseAdapter(listRecentlyOpened);
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
    }

    private void initializeCourses() {
        Course c1 = new Course("MATH 100", "Differential Calculus with Applications to Physical Sciences and Engineering");
        Course c2 = new Course("CPSC 110", "Computation, Programs, and Programming");
        Course c3 = new Course("CPSC 304", "Introduction to Relational Databases");
        listRecentlyOpened = new ArrayList<>();
        listRecentlyOpened.add(c1);
        listRecentlyOpened.add(c2);
        listRecentlyOpened.add(c3);
    }

    public void showProfilePage(View view) {
        startActivity(new Intent(this, ProfileActivity.class));
    }
}