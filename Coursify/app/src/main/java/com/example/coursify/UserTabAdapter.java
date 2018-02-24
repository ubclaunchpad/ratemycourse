package com.example.coursify;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by sveloso on 2018-02-10.
 */

public class UserTabAdapter extends FragmentPagerAdapter {

    int mNumOfTabs;

    public UserTabAdapter (FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                NoteFragment tab2 = new NoteFragment();
                return tab2;
            case 1:
                UserBookmarkFragment tab3 = new UserBookmarkFragment();
                return tab3;
            case 2:
                UserFriendsFragment tab1 = new UserFriendsFragment();
                return tab1;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}