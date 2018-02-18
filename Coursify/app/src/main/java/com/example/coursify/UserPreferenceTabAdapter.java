package com.example.coursify;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by sveloso on 2018-01-20.
 */
public class UserPreferenceTabAdapter extends FragmentPagerAdapter {

    int mNumOfTabs;
    String email;

    public UserPreferenceTabAdapter(FragmentManager fm, int NumOfTabs, String email) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.email = email;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                UserInterestListFragment tab1 = new UserInterestListFragment();
                Bundle args1 = new Bundle();
                args1.putString("EMAIL", email);
                tab1.setArguments(args1);
                return tab1;
            case 1:
                UserWillTakeListFragment tab2 = new UserWillTakeListFragment();
                Bundle args2 = new Bundle();
                args2.putString("EMAIL", email);
                tab2.setArguments(args2);
                return tab2;
            case 2:
                UserTakenListFragment tab3 = new UserTakenListFragment();
                Bundle args3 = new Bundle();
                args3.putString("EMAIL", email);
                tab3.setArguments(args3);
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