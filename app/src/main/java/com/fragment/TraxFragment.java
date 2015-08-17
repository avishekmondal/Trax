package com.fragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.adapter.TabFragmentAdapter;
import com.astuetz.PagerSlidingTabStrip;
import com.trax.R;

/**
 * Created by Avishek on 6/26/2015.
 */
public class TraxFragment extends Fragment {

    private View rootView;

    public static ViewPager viewPager;
    private PagerSlidingTabStrip tabsStrip;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.activity_trax, container, false);

        initialize();
        setViewPager(0);

        return rootView;

    }

    public void initialize() {

        viewPager = (ViewPager)rootView.findViewById(R.id.viewpager);
        viewPager.setAdapter(new TabFragmentAdapter(getActivity().getSupportFragmentManager()));

        // Give the PagerSlidingTabStrip the ViewPager
        tabsStrip = (PagerSlidingTabStrip)rootView.findViewById(R.id.tabs);
        tabsStrip.setShouldExpand(true);
        tabsStrip.setAllCaps(true);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);

    }

    public static void setViewPager(int i){

        viewPager.setCurrentItem(i);
    }
}
