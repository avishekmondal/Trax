package com.trax;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.fragment.DashBoardFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.utility.CircularImageView;
import com.utility.DBAdapter;
import com.utility.Pref;

public class HomeActivity extends SlidingFragmentActivity{

	private SlidingMenu slidingMenu;
	private ImageView toggleMenu;

    private CircularImageView imgViewProfile;
	private TextView tvName, tvMobile, tvEmailId, tvAddress;
    private LinearLayout llLogout;

    private ImageLoader imageLoader;
    private Pref _pref;
    private DBAdapter db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_home);

        startService(new Intent(HomeActivity.this, TraxUpdateShipmentService.class));
        startService(new Intent(HomeActivity.this, TraxRejectReasonService.class));

		setBehindContentView(R.layout.left_menu);
		slidingMenu = getSlidingMenu();
		slidingMenu.setFadeEnabled(true);
		slidingMenu.setFadeDegree(0.35f);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		slidingMenu.setMode(SlidingMenu.LEFT);
		slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);

		initialize();
        setValue();
		onclick();

		loadDashBoardFragment();

	}

    private static long back_pressed;

    public void onBackPressed() {
        //super.onBackPressed();
        if (back_pressed + 3000 > System.currentTimeMillis()) {
            android.os.Process.killProcess(android.os.Process.myPid());
            finish();

        } else {
            Toast.makeText(getBaseContext(), "Press once again to exit!", Toast.LENGTH_LONG)
                    .show();
            back_pressed = System.currentTimeMillis();

        }
    }

	private void initialize() {
		// TODO Auto-generated method stub

        _pref = new Pref(HomeActivity.this);
        db = new DBAdapter(HomeActivity.this);
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));

		toggleMenu = (ImageView) findViewById(R.id.ivMenu);

        imgViewProfile = (CircularImageView) slidingMenu.findViewById(R.id.imgViewProfile);
        tvName = (TextView) slidingMenu.findViewById(R.id.tvName);
        tvMobile = (TextView) slidingMenu.findViewById(R.id.tvMobile);
        tvEmailId = (TextView) slidingMenu.findViewById(R.id.tvEmailId);
        tvAddress = (TextView) slidingMenu.findViewById(R.id.tvAddress);
		llLogout = (LinearLayout) slidingMenu.findViewById(R.id.llLogout);

	}

	private void setValue(){

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true).cacheOnDisk(true)
                .showImageOnLoading(R.drawable.profile_pic)
                .showImageForEmptyUri(R.drawable.profile_pic)
                .showImageOnFail(R.drawable.profile_pic)
                .imageScaleType(ImageScaleType.EXACTLY).build();

        imageLoader.displayImage(_pref.getProfileImage(),
                imgViewProfile, options);

        tvName.setText(_pref.getName());
        tvMobile.setText(_pref.getMobileNo());
        tvEmailId.setText(_pref.getEmailId());
        tvAddress.setText(_pref.getAddress());

    }

    private void onclick() {
		// TODO Auto-generated method stub

		toggleMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				slidingMenu.toggle();
			}
		});

        llLogout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                _pref.saveLoginFlag("0");

                db.open();
                db.deleteAllRecord();
                db.close();

                stopService(new Intent(HomeActivity.this, TraxService.class));
                stopService(new Intent(HomeActivity.this, TraxUpdateShipmentService.class));
                stopService(new Intent(HomeActivity.this, TraxRejectReasonService.class));

                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();

            }
        });

	}

	public void loadDashBoardFragment() {

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction trans = fm.beginTransaction();
		Fragment fragment = new DashBoardFragment();
		trans.replace(R.id.FramelayoutContainer, fragment);
		trans.addToBackStack(null);
		trans.commit();
	}

}