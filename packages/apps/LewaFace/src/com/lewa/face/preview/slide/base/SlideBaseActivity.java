package com.lewa.face.preview.slide.base;

import java.util.ArrayList;

import com.lewa.face.R;
import com.lewa.face.app.ThemeApplication;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.util.ThemeUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.lewa.view.LewaPagerView;
import android.support.lewa.view.PagerAdapter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.lewa.face.util.Logs;

public abstract class SlideBaseActivity extends Activity implements
		LewaPagerView.OnPageChangeListener, OnClickListener ,OnSharedPreferenceChangeListener{
	
	private static final String TAG = SlideBaseActivity.class.getSimpleName();

	private LewaPagerView viewPager = null;
	protected ArrayList<String> source = null;
	protected Context mContext = null;

	/**
	 * last indication position
	 */
	private int lastPosition = 0;

	private Animation mTopBarAnimHide = null;
	private Animation mTopBarAnimShow = null;
	private Animation mBottomBarAnimHide = null;
	private Animation mBottomBarAnimShow = null;

	private RelativeLayout mThemeTopBar = null;
	private LinearLayout mThemeBottomBar = null;
	private LinearLayout mThemeIndicator = null;

	private TextView mThemeName = null;
	private TextView mThemeAuthor = null;
	private TextView mThemeSize = null;

	//private boolean currentIsFullScreen = false;

	/**
	 * monitor the touch screen time
	 */
	private Handler mMonitorHandler = new Handler();

	protected ThemeBase themeBase = null;

	private boolean moreThanOnePage = false;
	
	protected AlertDialog alertDialog = null;
	
	private PagerAdapter slideAdapter = null;
	
	protected SharedPreferences sharedPreferences = null;
    private static int generalTime = 2500;
    protected ProgressDialog progressDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		ThemeApplication.activities.add(this);

        if(!ThemeUtil.isSDCardEnable()){
            ThemeUtil.createAlertDialog(this);
            return;
        }
		
		ThemeUtil.debugMode();

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		mContext = this;

		setContentView();
		
		Intent intent = getIntent();
		
		themeBase = initThemeBase(intent);
		
		initViews();
		
		initOtherViews();

		initAnimations();
		
		source = getList();
		int totalPages = source.size();

		if (totalPages > 1) {
			mThemeIndicator = (LinearLayout) findViewById(R.id.theme_indicator);
			for (int i = 0; i < totalPages; i++) {
				ImageView imageView = new ImageView(mContext);
				if (i == 0) {
					imageView.setImageResource(R.drawable.theme_preview_current_page);
				} else {
					imageView.setImageResource(R.drawable.theme_preview_free_page);
				}
				imageView.setPadding(8, 0, 8, 0);
				mThemeIndicator.addView(imageView);
			}
			moreThanOnePage = true;
		}

		if (ThemeUtil.isEN) {
			mThemeName.setText(themeBase.getEnName());
			mThemeAuthor.setText(themeBase.getEnAuthor());
		} else {
			mThemeName.setText(themeBase.getCnName());
			mThemeAuthor.setText(themeBase.getCnAuthor());
		}
		mThemeSize.setText(themeBase.getSize());

		slideAdapter = initAdapter();
		viewPager.setAdapter(slideAdapter);
		viewPager.setOnPageChangeListener(this);

        hideAfterDelayedTime(generalTime);
		
		sharedPreferences = mContext.getSharedPreferences("DOWNLOADED", Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void hideAfterDelayedTime(int delay) {
        mMonitorHandler.postDelayed(monitorTouchScreen, delay);
	}

	private void initViews() {
		mThemeTopBar = (RelativeLayout) findViewById(R.id.theme_top_bar);
		mThemeBottomBar = (LinearLayout) findViewById(R.id.theme_bottom_bar);

		mThemeName = (TextView) findViewById(R.id.theme_name);
		mThemeAuthor = (TextView) findViewById(R.id.theme_author);
		mThemeSize = (TextView) findViewById(R.id.theme_size);

		viewPager = (LewaPagerView) findViewById(R.id.pager);

	}

	protected abstract void setContentView();
	
	protected abstract ThemeBase initThemeBase(Intent intent);
	
	protected abstract void initOtherViews();
	
	protected abstract PagerAdapter initAdapter();
	
	protected abstract ArrayList<String> getList();

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub
		if (moreThanOnePage) {
			changeCurrentPage(position);
		}
	}

	private void changeCurrentPage(int currentPage) {
		ImageView currentImageView = (ImageView) mThemeIndicator
				.getChildAt(currentPage);
		ImageView lastImageView = (ImageView) mThemeIndicator
				.getChildAt(lastPosition);
		if (currentImageView != null && lastImageView != null) {
			currentImageView
					.setImageResource(R.drawable.theme_preview_current_page);
			lastImageView.setImageResource(R.drawable.theme_preview_free_page);
		}
		lastPosition = currentPage;

	}
    boolean control;
    boolean moved;
    float beginX;
    float beginY;
    float endX;
    float endY;
    boolean inslide;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
                inslide = true;
                if (!moved) {
                    moved = true;
                    beginX = ev.getX();
                    beginY = ev.getY();
                }
			break;
		case MotionEvent.ACTION_UP:
                endX = ev.getX();
                endY = ev.getY();
                if (!inslide) {
                    endX = beginX = 0;
                    endY = beginY = 0;
                } else {
                    inslide = false;
                }
                moved = false;
                double distance = getDistance(beginX, beginY, endX, endY);
                Logs.i("================ distance " + distance);
                if (distance == 0 || distance < 35) {
                    if (!setFloatingBarGone()) {
                    setFloatingBarVisible();
                        hideAfterDelayedTime(generalTime);
                    } else {
                        control = true;
                    }
                }
			break;
		}

		return super.dispatchTouchEvent(ev);
	}

	private Runnable monitorTouchScreen = new Runnable() {

		@Override
		public void run() {
            if (control) {
                control = false;
                return;
			}
            setFloatingBarGone();
		}

	};

	private void initAnimations() {
		mTopBarAnimShow = AnimationUtils.loadAnimation(mContext,
				R.anim.theme_top_bar_enter);
		mTopBarAnimHide = AnimationUtils.loadAnimation(mContext,
				R.anim.theme_top_bar_exit);
		mBottomBarAnimShow = AnimationUtils.loadAnimation(mContext,
				R.anim.theme_bottom_bar_enter);
		mBottomBarAnimHide = AnimationUtils.loadAnimation(mContext,
				R.anim.theme_bottom_bar_exit);
	}

    private boolean setFloatingBarGone() {

		if (mThemeTopBar.getVisibility() == View.VISIBLE) {
			mThemeTopBar.setVisibility(View.GONE);
			mThemeTopBar.startAnimation(mTopBarAnimHide);

			if (moreThanOnePage) {
				mThemeIndicator.setVisibility(View.GONE);
				mThemeIndicator.startAnimation(mBottomBarAnimHide);
			}

			mThemeBottomBar.setVisibility(View.GONE);
			mThemeBottomBar.startAnimation(mBottomBarAnimHide);
            return true;
		}
        return false;
	}

	private void setFloatingBarVisible() {
		if (mThemeTopBar.getVisibility() == View.GONE) {
			mThemeTopBar.setVisibility(View.VISIBLE);
			mThemeTopBar.startAnimation(mTopBarAnimShow);

			if (moreThanOnePage) {
				mThemeIndicator.setVisibility(View.VISIBLE);
				mThemeIndicator.startAnimation(mBottomBarAnimShow);
			}

			mThemeBottomBar.setVisibility(View.VISIBLE);
			mThemeBottomBar.startAnimation(mBottomBarAnimShow);
		}
	}

	@Override
	protected void onDestroy() {
		mMonitorHandler.removeCallbacks(monitorTouchScreen);
		if (source != null) {
			source.clear();
			source = null;
		}
		
		super.onDestroy();
	}
	
	@Override
    public void onLowMemory() {
        Log.e(TAG, "onLowMemory and runGC()");
        ThemeUtil.runGC();
        super.onLowMemory();
    }

    private double getDistance(float beginX, float beginY, float endX, float endY) {
        float x = (endX - beginX);
        x *= x;
        float y = (endY - beginY);
        y *= y;
        return Math.sqrt(x + y);
    }
}
