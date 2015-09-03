package com.trax;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.adapter.TabFragmentAdapter;
import com.astuetz.PagerSlidingTabStrip;

/**
 * Created by Avishek on 7/8/2015.
 */
public class TraxActivity extends ActionBarActivity{

    public static ViewPager viewPager;
    private PagerSlidingTabStrip tabsStrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trax);

        //startService(new Intent(TraxActivity.this, TraxService.class));

        initialize();

        if(getIntent().getStringExtra("startFrom").equalsIgnoreCase("DashBoardActivity")){
            setViewPager(0);
        }
        else{
            setViewPager(1);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();

    }

    public void initialize() {

        viewPager = (ViewPager)findViewById(R.id.viewpager);
        viewPager.setAdapter(new TabFragmentAdapter(getSupportFragmentManager()));

        // Give the PagerSlidingTabStrip the ViewPager
        tabsStrip = (PagerSlidingTabStrip)findViewById(R.id.tabs);
        tabsStrip.setShouldExpand(true);
        tabsStrip.setAllCaps(true);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);


        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarView = inflater.inflate(R.layout.custom_actionbar_layout, null);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222E68")));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionbarView, layoutParams);

    }

    public static void setViewPager(int i){

        viewPager.setCurrentItem(i);
    }

}
