package com.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.fragment.CompletedFragment;
import com.fragment.IntransitFragment;
import com.fragment.PendingFragment;

/**
 * Created by Rahul on 5/28/2015.
 */
public class TabFragmentAdapter extends FragmentPagerAdapter {

    final int PAGE_COUNT =  3;
    private String tabTitles[] = new String[] { "Pending", "InTransit", "Completed" };

    public TabFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position){

            case 0:
                return new PendingFragment();

            case 1:
                return new IntransitFragment();

            case 2:
                return new CompletedFragment();

        }

        return null;

    }

    @Override
    public int getCount() {

        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        return tabTitles[position];
    }
}
