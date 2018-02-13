package com.example.coursify;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by sveloso on 2018-01-20.
 */
public class CourseTabAdapter extends FragmentPagerAdapter {

    int mNumOfTabs;

    public CourseTabAdapter (FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                CommentAndRatingFragment tab1 = new CommentAndRatingFragment();
                return tab1;
            case 1:
                CourseSettingsFragment tab2 = new CourseSettingsFragment();
                return tab2;
            case 2:
                CourseFriendsFragment tab3 = new CourseFriendsFragment();
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}