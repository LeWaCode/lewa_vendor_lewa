package com.lewa.os.ui;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.support.lewa.view.PagerAdapter;
import android.support.lewa.view.LewaPagerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import java.util.ArrayList;

import com.lewa.os.ui.ActivityResultBridge;
import com.lewa.os.ui.PendingContentLoader;
import com.lewa.os.ui.ViewPagerIndicator;

public class ViewPagerIndicatorActivity extends Activity implements ActivityResultBridge {
    private MPagerAdapter mPagerAdapter;
    private LewaPagerView  mViewPager;
    private ViewPagerIndicator mIndicator;

    private ActivityResultReceiver mResultReceiver;

    private ArrayList<PendingContentLoader> mContentLoaders;
    private LoadPendingContentHandler mLoadContentHandler;

    private ArrayList<StartParameter> flingViewClass;
    LocalActivityManager mLocalActivityManager;
    private int ScreenDisertyDpi = 0;
    private int pagernums;
    private int currentScreen = 0;

    private int mLayoutResId;
    private int mIndicatorResId;
    private int mPagerResId;

	private int ifLewaMusic = 0;

    private boolean mGestureDisabled = false;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(mLayoutResId);
        mLocalActivityManager = new LocalActivityManager(this,true);
        mLocalActivityManager.dispatchCreate(savedInstanceState);  
        mLocalActivityManager.dispatchResume(); 

        DisplayMetrics dm = new DisplayMetrics();        
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        ScreenDisertyDpi = dm.densityDpi;
        
        mViewPager = (LewaPagerView)findViewById(mPagerResId);        
        mPagerAdapter = new MPagerAdapter(this, mViewPager);        
        //mViewPager.setCurrentItem(0);        
        mIndicator = (ViewPagerIndicator)findViewById(mIndicatorResId);	
		if(ifLewaMusic == 1) {
			mIndicator.setIfMusicPlayer();
		}
        mViewPager.setOnPageChangeListener(mIndicator);
        		
        for (int i = 0; i < pagernums; i++) {
            StartParameter startParam = flingViewClass.get(i);
            Intent musiIntent = new Intent(this, startParam.mCls);
            if (null != startParam.mIntent) {
                musiIntent.setAction(startParam.mIntent.getAction());
                musiIntent.putExtras(startParam.mIntent);
                musiIntent.setData(startParam.mIntent.getData());
            }
            musiIntent.setFlags(i);
            
            View musicmainView = activityToView( musiIntent);
            //flingTitle.setId(2);
            musicmainView.setId(1);           
            mPagerAdapter.addPage(startParam.mCls, null, startParam.mIndicatorResId, musicmainView);
        }
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(currentScreen);
        if (Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation) {
            mIndicator.init(currentScreen, mPagerAdapter.getCount(), mPagerAdapter, ScreenDisertyDpi, 1);
        } else {
            mIndicator.init(currentScreen, mPagerAdapter.getCount(), mPagerAdapter, ScreenDisertyDpi, 0);
        }

        mIndicator.setFocusedTextColor(new int[]{255, 0, 0});
        mIndicator.setOnClickListener(new OnIndicatorClickListener());

        if (null != mContentLoaders) {
            mLoadContentHandler = new LoadPendingContentHandler();
            Message message = mLoadContentHandler.obtainMessage();
            mLoadContentHandler.sendMessageDelayed(message, 100);
        }
    }
    
    
    public void setIfPIM(Boolean pim) {
    	if(pim == true){
    		mViewPager.setTouchSlop(70);
    	}
    }

	public void setIfMusic(Boolean music) {
		if(music == true) {
			ifLewaMusic = 1;
		}
	}
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onRestoreInstanceState(savedInstanceState);
	Log.i("viewpager", "viewpager onRestoreInstanceState=");
	if (Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation) {
	    mIndicator.init(mViewPager.getCurrentItem(), mPagerAdapter.getCount(), mPagerAdapter, ScreenDisertyDpi, 1);
	} else {
	    mIndicator.init(mViewPager.getCurrentItem(), mPagerAdapter.getCount(), mPagerAdapter, ScreenDisertyDpi, 0);
	}
    }



	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		Log.i("viewpager", "viewpager onSaveInstanceState=");
	}


	@Override
    protected void onPause() {
    	super.onPause();

    	mLocalActivityManager.dispatchPause(isFinishing());    	
    }
    
	/*@Override
	protected void onStop(){
		super.onStop();
		mLocalActivityManager.dispatchStop();
	}*/
	
    @Override
    protected void onResume() {
    	super.onResume();
        if(mLocalActivityManager != null){
    	    mLocalActivityManager.dispatchResume();
       }
    }
    
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	mLocalActivityManager.dispatchDestroy(isFinishing());
    	mPagerAdapter.mPages.clear();
    	mPagerAdapter.info = null;
	}

    
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        if (Configuration.ORIENTATION_LANDSCAPE == newConfig.orientation) {
            mIndicator.init(mViewPager.getCurrentItem(), mPagerAdapter.getCount(), mPagerAdapter, ScreenDisertyDpi, 1);
        } else if(Configuration.ORIENTATION_PORTRAIT == newConfig.orientation) {
            mIndicator.init(mViewPager.getCurrentItem(), mPagerAdapter.getCount(), mPagerAdapter, ScreenDisertyDpi, 0);
        }
    }

    @Override
    public boolean dispatchTouchEvent (MotionEvent ev) {
        //Log.e("LewaPager", "dispatchTouchEvent: ev=" + ev.getAction());
        if(mGestureDisabled && (MotionEvent.ACTION_MOVE == ev.getAction())) {
			mViewPager.setScrollDisable();
        } else if (mGestureDisabled && (ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP)) {
			mGestureDisabled = false;
		}

        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (null != mResultReceiver) {
            for (int i = 0; i < flingViewClass.size(); ++i) {
                final Activity childActivity = getItemActivity(i);
                if ((null != childActivity) && (childActivity instanceof ActivityResultReceiver)) {
                    ((ActivityResultReceiver )childActivity).handleActivityResult(
                            mResultReceiver,
                            requestCode,
                            resultCode,
                            intent);
                }
            }
            mResultReceiver = null;
        }
    }

    @Override
    public void startActivityForResult(ActivityResultReceiver resultReceiver, Intent intent, int requestCode) {
        mResultReceiver = resultReceiver;
        startActivityForResult(intent, requestCode);
    }
    
    @Override
    public void handleActivityEvent(Activity activity, int evtCode, Object extra) {
        if (ActivityResultBridge.EVT_ACTIVITY_ON_CREATE_CONTEXT_MENU == evtCode) {
            mGestureDisabled = true;
        }
		else if(ActivityResultBridge.EVT_ACTIVITY_ON_BACK_PRESSED == evtCode){ 
			//add by shenqi 
			// Instead of stopping, simply push this to the back of the stack.
            // This is only done when running at the top of the task stack;
			Log.e("shenqi", "activity = " + activity.getComponentName().getClassName() +" onBackPressed is taskroot = " + isTaskRoot()); 
			if (isTaskRoot()) {
			 	moveTaskToBack(true);
		    } else {			
			 	super.onBackPressed();
		    }
		}
    }
    
    public void setDisplayScreen(int id) {
        currentScreen = id;
        if (null != mViewPager) {
            mViewPager.setCurrentItem(id);
            if (Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation) {
                mIndicator.init(currentScreen, mPagerAdapter.getCount(), mPagerAdapter, ScreenDisertyDpi, 1);
            } else {
                mIndicator.init(currentScreen, mPagerAdapter.getCount(), mPagerAdapter, ScreenDisertyDpi, 0);
            }
        }
    }

	public void setDisplayScreenNoSmoothScroll(int id) {
        currentScreen = id;
        if (null != mViewPager) {
            mViewPager.setCurrentItem(id,false);
            if (Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation) {
                mIndicator.init(currentScreen, mPagerAdapter.getCount(), mPagerAdapter, ScreenDisertyDpi, 1);
            } else {
                mIndicator.init(currentScreen, mPagerAdapter.getCount(), mPagerAdapter, ScreenDisertyDpi, 0);
            }
        }
    }
	
	public void setOnTriggerPagerChange(ViewPagerIndicator.OnPagerSlidingListener listener) {
    	mIndicator.setOnTriggerListener(listener);
    }
    

	public int getCurrentPage(int id) {
		return mViewPager.getCurrentItem();
	}

    
    public Activity getItemActivity(int position) {
        return mLocalActivityManager.getActivity(String.valueOf(position));
    }
    
    public void setupFlingParm(ArrayList<StartParameter> as, int layoutResId, int indicatorResId, int pagerResId) {
        flingViewClass = as;
        pagernums = as.size();

        mLayoutResId = layoutResId;
        mIndicatorResId = indicatorResId;
        mPagerResId  = pagerResId;
    }

    private View activityToView(Intent intent){
        //LocalActivityManager mLocalActivityManager = new LocalActivityManager(parent,true);
        final Window w = mLocalActivityManager.startActivity(
        String.valueOf(intent.getFlags()), intent);
        final View wd = w != null ? w.getDecorView() : null;
        if (wd != null) {
            wd .setVisibility(View.VISIBLE);
            wd .setFocusableInTouchMode(true);
            ((ViewGroup) wd).setDescendantFocusability(262144);

            int position = intent.getFlags();
            Activity childActivity = getItemActivity(position);
            if (null != childActivity) {
                if (childActivity instanceof ActivityResultReceiver) {
                    ((ActivityResultReceiver )childActivity).registerActivityResultBridge(this);
                }

                if (childActivity instanceof PendingContentLoader) {
                    if (null == mContentLoaders) {
                        mContentLoaders = new ArrayList<PendingContentLoader>();
                    }
                    
                    if (position == currentScreen) {
                        mContentLoaders.add(0, (PendingContentLoader )childActivity);
                    } else {
                        mContentLoaders.add((PendingContentLoader )childActivity);
                    }
                }
            }
        }
        return wd ;
    }
    
    
    class OnIndicatorClickListener implements ViewPagerIndicator.OnClickListener{
		@Override
		public void onCurrentClicked(View v) {

		}
		
		@Override
		public void onNextClicked(View v) {
			mViewPager.setCurrentItem(Math.min(mPagerAdapter.getCount() - 1, mIndicator.getCurrentPosition() + 1));
		}

		@Override
		public void onPreviousClicked(View v) {
			mViewPager.setCurrentItem(Math.max(0, mIndicator.getCurrentPosition() - 1));
		}
    	
    }
    static final class PageInfo {
        private final Class<?> clss;
        private final String args;
        private final View mView;
        
        PageInfo(Class<?> _clss, String _args, View mmView) {
            clss = _clss;
            args = _args;
            mView = mmView;
        }
    }
    class MPagerAdapter extends PagerAdapter implements ViewPagerIndicator.PageInfoProvider{
    	
    	
        private final Context mContext;
        private final LewaPagerView mPager;
        private PageInfo info;
        //private final PagerHeader mHeader;
        private final ArrayList<PageInfo> mPages = new ArrayList<PageInfo>();
    	
		public MPagerAdapter(Context context, LewaPagerView mViewPager) {
			mPager = mViewPager;
			mContext = context;
		}

        public void addPage(Class<?> clss, String title, View mView) {
            addPage(clss, null, title,mView);
        }

        public void addPage(Class<?> clss, Bundle args, int res,View mView) {
            addPage(clss, null, mContext.getResources().getString(res), mView);
        }

        public void addPage(Class<?> clss, Bundle args, String title, View mView) {
        	info = new PageInfo(clss, title, mView);
            mPages.add(info);
            notifyDataSetChanged();
        }

		@Override
		public int getCount() {
			return mPages.size();
		}
		
		@Override
		public String getTitle(int pos){
			return mPages.get(pos).args;
		}

		@Override
		public void destroyItem(View arg0, int position, Object arg2) {
			// TODO Auto-generated method stub
			((LewaPagerView) arg0).removeView(mPages.get(position).mView);
		}

		@Override
		public void finishUpdate(View arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Object instantiateItem(View arg0, int position) {
			// TODO Auto-generated method stub
			View convertView = null;
			if(mPages.get(position).mView != null) {
				convertView = mPages.get(position).mView;
				((LewaPagerView) arg0).addView(mPages.get(position).mView,0);
			}
			return convertView;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0==(arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Parcelable saveState() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
			// TODO Auto-generated method stub
			
		}
    }


    private class LoadPendingContentHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (null != mContentLoaders) {
                int loaderCount = mContentLoaders.size();
                //Log.i(TAG, "handleMessage: loaderCount=" + loaderCount);
                if (loaderCount > 0) {
                    PendingContentLoader contentLoader = mContentLoaders.get(0);
                    contentLoader.loadContent();

                    mContentLoaders.remove(0);
                    if (1 == loaderCount) {
                        mContentLoaders = null;
                    } else {
                        Message message = obtainMessage();
                        sendMessageDelayed(message, 500);
                    }
                }
            }
        }
    }
    

    public final class StartParameter {
        private Class<?> mCls;
        private Intent   mIntent;
        private int      mIndicatorResId;

        public StartParameter(Class<?> cls, Intent intent, int indicatorResId) {
            mCls = cls;
            mIntent = intent;
            mIndicatorResId = indicatorResId;
        }
    }
}