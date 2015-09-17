package com.utility;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.trax.R;

/**
 * Created by Rahul on 5/5/2015.
 */
public class ThemeSetter {

    private Context context;
    private LayoutInflater inflater;
    private View actionbarView;
    private ActionBar.LayoutParams layoutParams;
    private TextView tvHeaderTitle;
    private ImageView ivMenu;

    public ThemeSetter(Context context){
        this.context = context;
    }

    public ImageView setHeaderTheme(ActionBar actionBar, String heading, int ivMenuDrawable){

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        actionbarView = inflater.inflate(R.layout.custom_actionbar_layout, null);
        layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        //tvHeaderTitle = (TextView) actionbarCustView.findViewById(R.id.tvHeaderTitle);
        ivMenu = (ImageView) actionbarView.findViewById(R.id.ivMenu);

        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222E68")));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionbarView, layoutParams);

        //tvHeaderTitle.setText(heading);
        ivMenu.setImageResource(ivMenuDrawable);

        return ivMenu;
    }
}
