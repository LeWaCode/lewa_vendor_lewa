package com.lewa.launcher;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.View.OnLongClickListener;
import android.view.View.OnClickListener;
import android.view.animation.TranslateAnimation;

public class SenseWorkspace extends ViewGroup implements OnClickListener,
        OnLongClickListener, DragSource, DropTarget,
        DragController.DragListener {

    private Launcher mLauncher;
    private Context mContext;
    private Bitmap mBitmap;
    private float scale = 0.33f;
    private DragLayer mDragger;

    private int mPageHorizontalMargin = 0;

    private int mItemsValidLeft = 0;
    private int mItemsValidTop = 0;
    private int mItemValidWidth = 0;
    private int mItemValidHeight = 0;
    private int mItemPressX = 0;
    private int mItemPressY = 0;

    private int mNumColumns = 3;
    private int mNumRows = 3;
    private LayoutInflater layoutInflter = null;

    private ImageView mPinView = null;

    private int mPinScreenPos = -1;
    private int mCrrentScreenPos = -1;

	private int mStatus = SENSE_CLOSED;
	private long startTime;
	private long mCurrentTime;
	private final int mAnimationDuration = 150;
	private boolean isAnimating = false;
	private float mScaleFactor;
	private View mPreView;
	
	private static final int SENSE_OPENING = 1;
	private static final int SENSE_CLOSING = 2;
	private static final int SENSE_OPEN = 3;
	private static final int SENSE_CLOSED = 4;
    public SenseWorkspace(Context context) {
        super(context);
        mContext = context;
    }

    public SenseWorkspace(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public SenseWorkspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        heightSpecSize = heightSpecSize - getPaddingTop() - getPaddingBottom();
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                (int) (heightSpecSize * scale), MeasureSpec.EXACTLY);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        widthSpecSize = widthSpecSize - getPaddingLeft() - getPaddingRight();
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(
                (int) (widthSpecSize * scale), MeasureSpec.EXACTLY);
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        int childLeft = getPaddingLeft();
        int childTop = 0;
        final int mTop = getPaddingTop();

        int count = getChildCount();
        if (count > Launcher.MAX_SCREENS) {
            count = Launcher.MAX_SCREENS;
            getChildAt(count).setVisibility(View.GONE);
        }
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            child.setTag(i);
            // if (child.getVisibility() != View.GONE) {
            final int childWidth = child.getMeasuredWidth();
            child.layout(childLeft, mTop + childTop, childLeft + childWidth,
                    mTop + childTop + child.getMeasuredHeight());
            childLeft += childWidth;
            if (i != 0 && (i + 1) % 3 == 0) {
                childLeft = getPaddingLeft();
                childTop += child.getMeasuredHeight();
            }
            // }
        }

        mItemsValidLeft = getPaddingLeft() + mPageHorizontalMargin;        
        mItemsValidTop = getPaddingTop();
        mItemValidWidth = (getMeasuredWidth() - mItemsValidLeft
                - getPaddingRight())
                / mNumColumns;
        mItemValidHeight = (getMeasuredHeight() - mItemsValidTop - getPaddingBottom())
                / mNumRows;
    }

    public void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }

    public void setDragger(DragLayer dragger) {
        // TODO Auto-generated method stub
        mDragger = dragger;
    }

    public void setChildView() {
        layoutInflter = LayoutInflater.from(mContext);
        Workspace workspace = mLauncher.getWorkspace();
        // workspace.lock();
        // workspace.enableChildrenCache();
        //View v = workspace.getChildAt(0);
        ImageView preView = null;
        ImageView pinView = null;
        ImageView deleteView = null;
        for (int i = 0; i < workspace.getChildCount() + 1; i++) {
            final CardLayout screen = (CardLayout) layoutInflter.inflate(
                    R.layout.all_screens, this, false);
            preView = (ImageView) screen.findViewById(R.id.preview);
            pinView = (ImageView) screen.findViewById(R.id.pin);
            deleteView = (ImageView) screen.findViewById(R.id.delete_screen);
            if (i < workspace.getChildCount()) {
            	
            	if (mLauncher.isEditZoneVisibility()) {
                    workspace.getChildAt(i).setBackgroundDrawable(null); 
                }
            	View screenView = workspace.getChildAt(i);
            	screenView.setDrawingCacheEnabled(true);
            	mBitmap = screenView.getDrawingCache();
               /* mBitmap = Bitmap.createBitmap((int) (v.getWidth() * scale),
                        (int) (v.getHeight() * scale), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(mBitmap);
                canvas.scale(scale, scale);
                workspace.getChildAt(i).draw(canvas);*/
                preView.setImageBitmap(mBitmap);

                screen.setOnLongClickListener(this);
                screen.setOnClickListener(this);                
                pinView.setTag(i);
                pinView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Auto-generated method stub
                        setPinScreen(view);
                    }
                }); 

                screen.setTag(i);
                deleteView.setTag(i);
                deleteView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        deleteScreen(view, (Integer)screen.getTag());
                    }
                });
                if (i == mLauncher.getWorkspace().getCurrentScreen()) {
                    View previewBg = screen.findViewById(R.id.preview_bg);
                    previewBg.setBackgroundResource(
                            R.drawable.lw_theme1_editpanel_editpanel_tumbview_selected);
                    mCrrentScreenPos = i;
                }
                if (i == mLauncher.getWorkspace().getDefaultScreen()) {
                    pinView.setBackgroundResource(R.drawable.lw_theme1_editpanel_editpanel_pin_hi);
                    mPinView = pinView;
                    mPinView.setTag(i);
                    mPinScreenPos = i;
                }
            } else {
                pinView.setVisibility(View.GONE);
                deleteView.setVisibility(View.GONE);
                preView.setBackgroundResource(R.drawable.lewa_screen_plus);
                mPreView = preView;
            }
            screen.setOnClickListener(this);
            addView(screen);
        }
        workspace.invalidate();
        requestLayout();
    }    
    

    @Override
    public void onDragStart(View v, DragSource source, Object info,
            int dragAction) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDragEnd() {
        // TODO Auto-generated method stub
        verifyPinScreen();
        (getChildAt(getChildCount() - 1)).setVisibility(View.VISIBLE);
        requestLayout();
    }

    @Override
    public void onDrop(DragSource source, int x, int y, int xOffset,
            int yOffset, Object dragInfo) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDragEnter(DragSource source, int x, int y, int xOffset,
            int yOffset, Object dragInfo) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDragOver(DragSource source, int x, int y, int xOffset,
            int yOffset, Object dragInfo) {
        // TODO Auto-generated method stub
        int dragPos = (Integer) ((View) dragInfo).getTag();
        int pos = (x - mItemsValidLeft) / mItemValidWidth
                + (y - mItemsValidTop) / mItemValidHeight * mNumColumns;//
        if (pos > getChildCount() - 2) {
            pos = getChildCount() - 2;
        }
        if ((pos < 0) || ((dragPos == pos))) {
            return;
        }
        if (((Math.abs(x - mItemPressX) <= mItemValidWidth / 2) && (Math.abs(y
                - mItemPressY) <= mItemValidHeight / 2))) {
            System.out.println("      invalid pos      ");
            return;
        } 
        // store movedView
        int movedSize = Math.abs(pos - dragPos) + 1;
        if (movedSize == 0)
            return;
        ArrayList<View> movedList = new ArrayList<View>();
        int transPos = pos;
        for (int i = movedSize; i > 0; i--) {
            View movedView = getChildAt(transPos);
            movedList.add(movedView);
            if (pos > dragPos)
                transPos--;
            else
                transPos++;
        }
        
        ((View) dragInfo).setTag(pos);
        mLauncher.getWorkspace().swapScreens(dragPos, pos);
        swapScreens(dragPos, pos);
        requestLayout();
        
        //Translation
        View targetView = null;
        View translateView = null;
        for (int i = 0; i < movedSize - 1; i++) {
            translateView = movedList.get(i);
            targetView = movedList.get(i + 1);
            if (translateView == null || targetView == null)
                return;
            final int[] translateLocation = new int[2];
            final int[] targetLocation = new int[2];
            translateView.getLocationOnScreen(translateLocation);
            targetView.getLocationOnScreen(targetLocation);
            playSingleViewAnimation(translateView, targetView,
                    translateLocation[0], targetLocation[0],
                    translateLocation[1], targetLocation[1]);
        }
       
    }

    public void playSingleViewAnimation(View movedView, View targetView,
            int fromX, int toX, int fromY, int toY) {

        int xOffest = toX - fromX;
        int yOffest = toY - fromY;
        TranslateAnimation translateAnimation = new TranslateAnimation(
                -xOffest, 0, -yOffest, 0);
        translateAnimation.setDuration(400);
        movedView.startAnimation(translateAnimation);
    }

    @Override
    public void onDragExit(DragSource source, int x, int y, int xOffset,
            int yOffset, Object dragInfo) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
            int yOffset, Object dragInfo) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void onDropCompleted(View target, boolean success) {
        // TODO Auto-generated method stub
        /*
         * verifyPinScreen(); verifyCurrentScreen(); //if(mCrrentScreenPos )
         * (getChildAt(getChildCount() - 1)).setVisibility(View.VISIBLE);
         */        
    }

    @Override
    public boolean onLongClick(View v) {
        // TODO Auto-generated method stub
        mDragger.startDrag(v, this, v,
                DragController.DRAG_ACTION_MOVE);
       (getChildAt(getChildCount() - 1)).setVisibility(View.GONE);
       return true;
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub

        int pos = (Integer) view.getTag();
        if (pos < getChildCount() - 1) {
            mLauncher.getWorkspace().setCurrentScreen(pos);
            mLauncher.stopDesktopEdit();
            mLauncher.getWorkspace().snapToScreen(pos);  
        } else {
            // onAddScreen(pos);
            if (pos < Launcher.MAX_SCREENS) {
                mLauncher.getWorkspace().addScreen(pos);
                final CardLayout screen = (CardLayout) layoutInflter.inflate(
                        R.layout.all_screens, this, false);
                addView(screen, pos);
                screen.setOnClickListener(this);
                screen.setOnLongClickListener(this);
                View pinView = screen.findViewById(R.id.pin);
                View deleteView = screen.findViewById(R.id.delete_screen);
                screen.setTag(pos);
                pinView.setTag(pos);
                pinView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        setPinScreen(v);
                    }
                });
                deleteView.setTag(pos);
                deleteView.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        deleteScreen(v, (Integer)screen.getTag());
                    }
                });
                requestLayout();
            } else {
                Toast t = Toast.makeText(mContext,
                        R.string.message_cannot_add_desktop_screen, 500);
                t.show();
            }
        }

    }

    private void setPinScreen(View view) {
        if (view.equals(mPinView) && mPinView != null) {
            return;
        }
        mPinScreenPos = (Integer) view.getTag();
        mLauncher.getWorkspace().setDefaultScreen(mPinScreenPos);
        AlmostNexusSettingsHelper.setDefaultScreen(mLauncher, mPinScreenPos);

        view.setBackgroundResource(R.drawable.lw_theme1_editpanel_editpanel_pin_hi);
        mPinView.setBackgroundResource(R.drawable.lw_theme1_editpanel_editpanel_pin);
        mPinView = (ImageView) view;

    }

    public void setDefaultPinScreen() {
        mLauncher.getWorkspace().setDefaultScreen(0);
        AlmostNexusSettingsHelper.setDefaultScreen(mLauncher, 0);

        mPinView = (ImageView) getChildAt(0).findViewById(R.id.pin);
        mPinView.setBackgroundResource(R.drawable.lw_theme1_editpanel_editpanel_pin_hi);
        mPinScreenPos = 0;
    }

    private void verifyPinScreen() {
        // mPinScreenPos = (Integer) mPinView.getTag();
        if (mPinScreenPos != mLauncher.getWorkspace().getDefaultScreen()) {
            mLauncher.getWorkspace().setDefaultScreen(mPinScreenPos);
            AlmostNexusSettingsHelper
                    .setDefaultScreen(mLauncher, mPinScreenPos);
        }
    }

    public void setDefaultCurrentScreen() {
        mLauncher.getWorkspace().setCurrentScreen(0);

        View currentScreen = getChildAt(0).findViewById(R.id.preview_bg);
        currentScreen
                .setBackgroundResource(R.drawable.lw_theme1_editpanel_editpanel_tumbview_selected);
        mCrrentScreenPos = 0;
    }

    public int getPinScreenPos() {
        return mPinScreenPos;
    }

    public int getCurrentScreenPos() {
        return mCrrentScreenPos;
    }

    private void swapScreens(int a, int b) {

        // Collections.swap(mScreens, a, b);
        View v = getChildAt(a);
        removeViewAt(a);
        addView(v, b);
        if (mPinScreenPos == a) {
            mPinScreenPos = b;
        } else if (a > b && mPinScreenPos >= b && mPinScreenPos <= a) {
            mPinScreenPos++;
        } else if (a < b && mPinScreenPos <= b && mPinScreenPos >= a) {
            mPinScreenPos--;
        }
        if (mCrrentScreenPos == a) {
            mCrrentScreenPos = b;
        } else if (a > b && mCrrentScreenPos >= b && mCrrentScreenPos <= a) {
            mCrrentScreenPos++;
        } else if (a < b && mCrrentScreenPos <= b && mCrrentScreenPos >= a) {
            mCrrentScreenPos--;
        }

    }
    
    private void deleteScreen(View view, final int position) {
        if (mLauncher.getWorkspace().getChildCount() == 1) {
            Toast.makeText(mLauncher,
                    mContext.getString(R.string.lewa_editmodetip),
                    Toast.LENGTH_SHORT).show();
            return;
        }        
        
        CellLayout layout = (CellLayout) mLauncher.getWorkspace()
                .getChildAt(position);
        if(layout == null) 
            return;
        if (layout.getChildCount() > 0) {
            AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                    .create();
            alertDialog.setTitle(getResources().getString(
                    R.string.title_dialog_xml));
            alertDialog.setMessage(getResources().getString(
                    R.string.message_delete_desktop_screen));
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    getResources().getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                int which) {

                            if (getPinScreenPos() == position) {
                                // cf.setPinScreenPos(0);
                                setDefaultPinScreen();
                            }
                            if (getCurrentScreenPos() == position) {
                                // cf.setCurrentScreenPos(0);
                                setDefaultCurrentScreen();
                            }

                            mLauncher.getWorkspace().removeScreen(position);

                            removeViewAt(position);
                            (getChildAt(getChildCount() - 1)).setVisibility(View.VISIBLE);

                            // cf.requestLayout();
                        }
                    });
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                    getResources().getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                int which) {
                            (getChildAt(getChildCount() - 1))
                                    .setVisibility(View.VISIBLE);
                            requestLayout();
                        }
                    });
            alertDialog.show();
        } else {
            mLauncher.getWorkspace().removeScreen(position);
            removeViewAt(position);
            if (getPinScreenPos() == position) {
                setDefaultPinScreen();
            }
            if (getCurrentScreenPos() == position) {
                setDefaultCurrentScreen();
            }            
            (getChildAt(getChildCount() - 1)).setVisibility(View.VISIBLE);
        }
    }
    
    
	@Override
	protected void dispatchDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.dispatchDraw(canvas);
		if (isAnimating && mStatus==SENSE_OPENING) {
            if (startTime == 0) {
                startTime = SystemClock.uptimeMillis();
                mCurrentTime = 0;
            } else {
                mCurrentTime = SystemClock.uptimeMillis() - startTime;
            }
            if (mStatus == SENSE_OPENING) {
                mScaleFactor = easeOut(mCurrentTime, 1.5f, 1.0f, mAnimationDuration);
            } else if (mStatus == SENSE_CLOSING) {
                mScaleFactor = easeIn(mCurrentTime, 1.0f, 1.5f, mAnimationDuration);
            }
            if (mCurrentTime >= mAnimationDuration) {
                isAnimating = false;
                if (mStatus == SENSE_OPENING) {
                    mStatus = SENSE_OPEN;
                } else if (mStatus == SENSE_CLOSING) {
                    mStatus = SENSE_CLOSED;
                }
            }
        }
	}

	@Override
	protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
		// TODO Auto-generated method stub
		if(!isAnimating && mStatus==SENSE_OPEN )
			return super.drawChild(canvas, child, drawingTime);
		
		 int saveCount = canvas.save();
		 if(isAnimating){
			postInvalidate();
			int distH=(child.getLeft()+(child.getWidth()/2))-(getWidth()/2);
			int distV=(child.getTop()+(child.getHeight()/2))-(getHeight()/2);
			float x=child.getLeft()+(distH*(mScaleFactor-1))*(mScaleFactor);
			float y=child.getTop()+(distV*(mScaleFactor-1))*(mScaleFactor);
			float width=child.getWidth()*mScaleFactor;

			float scale=((width)/child.getWidth());
			canvas.save();
			canvas.translate(x, y+child.getPaddingTop());
			canvas.scale(scale, scale);
			child.draw(canvas);
			canvas.restore();
			 
		}else{		
			canvas.save();
			canvas.translate(child.getLeft(), child.getTop()+child.getPaddingTop());
			child.draw(canvas);
			canvas.restore();			
		}
		canvas.restoreToCount(saveCount);
		return true;
	}
	/**
	 * Open/close public methods
	 */
	public void open(boolean animate){
		if(mStatus!=SENSE_OPENING){
			if(animate){
				isAnimating=true;
				mStatus=SENSE_OPENING;
			}else{
				isAnimating=false;
				mStatus=SENSE_OPEN;
			}
			startTime=0;
			
			invalidate();
		}
	}
	
	public void close(boolean animate){
		if(mStatus!=SENSE_CLOSED){
			if(animate){
				mStatus=SENSE_CLOSING;
				isAnimating=true;
			}else{
				mStatus=SENSE_CLOSED;
				isAnimating=false;
			}
			startTime=0;
			invalidate();
		}
	}
	
	static float easeOut (float time, float begin, float end, float duration) {
		float change=end- begin;
		float value= change*((time=time/duration-1)*time*time + 1) + begin;
		if(change>0 && value>end) value=end;
		if(change<0 && value<end) value=end;
		return value;
	}
	static float easeIn (float time, float begin, float end, float duration) {
		float change=end- begin;
		float value=change*(time/=duration)*time*time + begin;
		if(change>0 && value>end) value=end;
		if(change<0 && value<end) value=end;
		return value;
	}
	
	@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        final int x = (int) ev.getX();
        final int y = (int) ev.getY();
        
        int pos = (x - mItemsValidLeft) / mItemValidWidth
                + (y - mItemsValidTop) / mItemValidHeight * mNumColumns;

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
                if(pos >= getChildCount() - 1 && pos < Launcher.MAX_SCREENS) {
                    mPreView.setFocusableInTouchMode(true);
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if(pos >= getChildCount() - 1 && pos < Launcher.MAX_SCREENS) {
                    mPreView.setFocusable(false);
                }
                break;
        }
        return false;
    }
}
