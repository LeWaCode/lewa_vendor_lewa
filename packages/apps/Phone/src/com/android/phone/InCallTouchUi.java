/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.phone;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.app.StatusBarManager;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Phone;
import com.android.internal.widget.multiwaveview.MultiWaveView;
import com.android.internal.widget.multiwaveview.MultiWaveView.OnTriggerListener;
import com.android.internal.telephony.CallManager;

/**
 * In-call onscreen touch UI elements, used on some platforms.
 *
 * This widget is a fullscreen overlay, drawn on top of the
 * non-touch-sensitive parts of the in-call UI (i.e. the call card).
 */
public class InCallTouchUi extends FrameLayout
        implements View.OnClickListener, OnTriggerListener {
    private static final int IN_CALL_WIDGET_TRANSITION_TIME = 250; // in ms
    private static final String LOG_TAG = "InCallTouchUi";
    private static final boolean DBG = (PhoneApp.DBG_LEVEL >= 2);

    // Incoming call widget targets
    private static final int ANSWER_CALL_ID = 0;  // drag right
    private static final int SEND_SMS_ID = 1;  // drag up
    private static final int DECLINE_CALL_ID = 2;  // drag left
    /**
     * Reference to the InCallScreen activity that owns us.  This may be
     * null if we haven't been initialized yet *or* after the InCallScreen
     * activity has been destroyed.
     */
    private InCallScreen mInCallScreen;

    private MultiWaveView mIncomingCallWidget;  // UI used for an incoming call
    private View mInCallControls;  // UI elements while on a regular call
    //
//    private ImageButton mMergeButton;
    private Button mEndButton;
    private CompoundButton mDialpadButton;
    private CompoundButton mMuteButton;
    private CompoundButton mAudioButton;
//    private CompoundButton mHoldButton;
    private CompoundButton mMoreButton;
//    private ImageButton mSwapButton;
//    private View mHoldSwapSpacer;
    
    private View tLayoutMore ;
//    private View mBluetoothButton;
    private LinearLayout mAddCallButton;
    private LinearLayout mContactsButton;
    private LinearLayout mHoldButton;
    private LinearLayout mRecordButton;
    
    private LinearLayout mSwapButton;
    private LinearLayout mMergeButton;
    
    private TextView  mRecordTextView;
    private TextView mRecordTip;
    //
    private ViewGroup mExtraButtonRow;
    private ViewGroup mCdmaMergeButton;
    private ViewGroup mManageConferenceButton;
    private ImageButton mManageConferenceButtonImage;

    // "Audio mode" PopupMenu
    //private PopupMenu mAudioModePopup;
    //private boolean mAudioModePopupVisible = false;

    // Time of the most recent "answer" or "reject" action (see updateState())
    private long mLastIncomingCallActionTime;  // in SystemClock.uptimeMillis() time base

    // Parameters for the MultiWaveView "ping" animation; see triggerPing().
    private static final boolean ENABLE_PING_ON_RING_EVENTS = false;
    private static final boolean ENABLE_PING_AUTO_REPEAT = true;
    private static final long PING_AUTO_REPEAT_DELAY_MSEC = 1200;

    private static final int INCOMING_CALL_WIDGET_PING = 101;

    // Overall enabledness of the "touch UI" features
    private boolean mAllowIncomingCallTouchUi;
    private boolean mAllowInCallTouchUi;

    private CallFeaturesSetting mSettings;

//    private StatusBarManager mStatusBarManager; //IStatusBarService StatusBarService
    
    private boolean flag_Recording ;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // If the InCallScreen activity isn't around any more,
            // there's no point doing anything here.
            if (mInCallScreen == null) return;

            switch (msg.what) {
                case INCOMING_CALL_WIDGET_PING:
                    if (DBG) log("INCOMING_CALL_WIDGET_PING...");
                    triggerPing();
                    break;
                default:
                    Log.wtf(LOG_TAG, "mHandler: unexpected message: " + msg);
                    break;
            }
        }
    };

    public boolean isCheckedForKeyboard()
    {
    	if(mDialpadButton != null &&  mDialpadButton.isEnabled() && mDialpadButton.isChecked())
    	{
    		return true;
    	}
    	return false;
    }
    public InCallTouchUi(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DBG) log("InCallTouchUi constructor...");
        if (DBG) log("- this = " + this);
        if (DBG) log("- context " + context + ", attrs " + attrs);

        // Inflate our contents, and add it (to ourself) as a child.
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(
                R.layout.incall_touch_ui,  // resource
                //mSettings.mLeftHand ? R.layout.incall_touch_ui_left : R.layout.incall_touch_ui,  // resource
                this,                      // root
                true);

        // The various touch UI features are enabled on a per-product
        // basis.  (These flags in config.xml may be overridden by
        // product-specific overlay files.)

        mAllowIncomingCallTouchUi = getResources().getBoolean(R.bool.allow_incoming_call_touch_ui);
        if (DBG) log("- incoming call touch UI: "
                     + (mAllowIncomingCallTouchUi ? "ENABLED" : "DISABLED"));
        mAllowInCallTouchUi = getResources().getBoolean(R.bool.allow_in_call_touch_ui);
        if (DBG) log("- regular in-call touch UI: "
                     + (mAllowInCallTouchUi ? "ENABLED" : "DISABLED"));
        mSettings = CallFeaturesSetting.getInstance(context);
    }

    void setInCallScreenInstance(InCallScreen inCallScreen) {
        mInCallScreen = inCallScreen;
        if (mEndButton != null) mEndButton.setOnLongClickListener(mInCallScreen);
    }

    private void initMoreButton()
    {
    	tLayoutMore =(View)mInCallControls.findViewById(R.id.more_tablayout);
//    	mBluetoothButton = (View) tLayoutMore.findViewById(R.id.bluetooth_button);
//    	mBluetoothButton.setOnClickListener(this);
        mAddCallButton = (LinearLayout)mInCallControls.findViewById(R.id.addCall_button);
        mAddCallButton.setOnClickListener(this);
        mContactsButton = (LinearLayout)mInCallControls.findViewById(R.id.contacts_button);
        mContactsButton.setOnClickListener(this);
        mHoldButton = (LinearLayout)mInCallControls.findViewById(R.id.hold_button);
        mHoldButton.setOnClickListener(this);
        
        mSwapButton = (LinearLayout) mInCallControls.findViewById(R.id.swap_button);
        mSwapButton.setOnClickListener(this);
        
        mMergeButton = (LinearLayout) mInCallControls.findViewById(R.id.merge_button);
        mMergeButton.setOnClickListener(this);
        mRecordButton = (LinearLayout)mInCallControls.findViewById(R.id.record_button);
        mRecordButton.setOnClickListener(this);
        //Begin,Added by chenqiang for bug7977,20120712
        mRecordButton.setClickable(false);
        //End
        
        mRecordTextView = (TextView)mInCallControls.findViewById(R.id.record_textview);
        
        mRecordTip = (TextView)mInCallControls.findViewById(R.id.record_tip);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (DBG) log("InCallTouchUi onFinishInflate(this = " + this + ")...");

        
        // Look up the various UI elements.
        
        // "Drag-to-answer" widget for incoming calls.
        mIncomingCallWidget = (MultiWaveView) findViewById(R.id.incomingCallWidget);
        mIncomingCallWidget.setOnTriggerListener(this);

        // Container for the UI elements shown while on a regular call.
        mInCallControls = findViewById(R.id.inCallControls);

        initMoreButton();
        // Regular (single-tap) buttons, where we listen for click events:
        // Main cluster of buttons:
//        mAddButton = (ImageButton) mInCallControls.findViewById(R.id.addButton);
//        mAddButton.setOnClickListener(this);
//        mMergeButton = (ImageButton) mInCallControls.findViewById(R.id.mergeButton);
//        mMergeButton.setOnClickListener(this);
        mEndButton = (Button) mInCallControls.findViewById(R.id.endButton);
        mEndButton.setOnClickListener(this);
        mDialpadButton = (CompoundButton) mInCallControls.findViewById(R.id.dialpadButton);
        mDialpadButton.setOnClickListener(this);
        mMuteButton = (CompoundButton) mInCallControls.findViewById(R.id.muteButton);
        mMuteButton.setOnClickListener(this);
        mAudioButton = (CompoundButton) mInCallControls.findViewById(R.id.audioButton);
        mAudioButton.setOnClickListener(this);
        mMoreButton = (CompoundButton) mInCallControls.findViewById(R.id.moreButton);
        mMoreButton.setOnClickListener(this);
//        mHoldSwapSpacer = mInCallControls.findViewById(R.id.holdSwapSpacer);

/*        tLayoutMore  = (TableLayout)mInCallControls.findViewById(R.id.more_tablayout);
*/        // TODO: Back when these buttons had text labels, we changed
        // the label of mSwapButton for CDMA as follows:
        //
        //      if (PhoneApp.getPhone().getPhoneType() == Phone.PHONE_TYPE_CDMA) {
        //          // In CDMA we use a generalized text - "Manage call", as behavior on selecting
        //          // this option depends entirely on what the current call state is.
        //          mSwapButtonLabel.setText(R.string.onscreenManageCallsText);
        //      } else {
        //          mSwapButtonLabel.setText(R.string.onscreenSwapCallsText);
        //      }
        //
        // If this is still needed, consider having a special icon for this
        // button in CDMA.

        // Buttons shown on the "extra button row", only visible in certain (rare) states.
        mExtraButtonRow = (ViewGroup) mInCallControls.findViewById(R.id.extraButtonRow);
        // The two "buttons" here (mCdmaMergeButton and mManageConferenceButton)
        // are actually layouts containing an icon and a text label side-by-side.
        mCdmaMergeButton =
                (ViewGroup) mInCallControls.findViewById(R.id.cdmaMergeButton);
        mCdmaMergeButton.setOnClickListener(this);
        //
        mManageConferenceButton =
                (ViewGroup) mInCallControls.findViewById(R.id.manageConferenceButton);
        mManageConferenceButton.setOnClickListener(this);
        mManageConferenceButtonImage =
                (ImageButton) mInCallControls.findViewById(R.id.manageConferenceButtonImage);

        // Add a custom OnTouchListener to manually shrink the "hit
        // target" of some buttons.
        // (We do this for a few specific buttons which are vulnerable to
        // "false touches" because either (1) they're near the edge of the
        // screen and might be unintentionally touched while holding the
        // device in your hand, or (2) they're in the upper corners and might
        // be touched by the user's ear before the prox sensor has a chance to
        // kick in.)
        //
        // TODO (new ICS layout): not sure which buttons need this yet.
        // For now, use it only with the "End call" button (which extends all
        // the way to the edges of the screen).  But we can consider doing
        // this for "Dialpad" and/or "Add call" if those turn out to be a
        // problem too.
        //
        View.OnTouchListener smallerHitTargetTouchListener = new SmallerHitTargetTouchListener();
//        mEndButton.setOnTouchListener(smallerHitTargetTouchListener);
    }

    /**
     * Updates the visibility and/or state of our UI elements, based on
     * the current state of the phone.
     */
    void updateState(CallManager cm) {
    	
        if (mInCallScreen == null) {
            log("- updateState: mInCallScreen has been destroyed; bailing out...");
            return;
        }

        Phone.State state = cm.getState();  // IDLE, RINGING, or OFFHOOK
        if (DBG) log("- updateState: CallManager state is " + state);
        
        boolean showIncomingCallControls = false;
        boolean showInCallControls = false;

        final Call ringingCall = cm.getFirstActiveRingingCall();
        Call fg = cm.getActiveFgCall();
        Call bg = cm.getFirstActiveBgCall();
//        Log.e("Main", "Build.DEVICE:"+Build.DEVICE);
//        Log.e("Main", "fg:"+fg.isIdle()+"  State:"+fg.getState() +" isAlive():"+fg.getState().isAlive());
//        Log.e("Main", "bg:"+bg.isIdle()+"  State:"+bg.getState() +" isAlive():"+bg.getState().isAlive() );
//        if(fg.isIdle() && bg.isIdle() && tLayoutMore != null && (tLayoutMore.getVisibility() == View.VISIBLE))
//        {
//        	 tLayoutMore.setVisibility(View.GONE);
//        }
//        if(fg.isIdle()&&!fg.getState().isAlive())
//        {
//        	mInCallScreen.handleOnscreenButtonClick(R.id.hold_button);
//        }
        // If the FG call is dialing/alerting, we should display for that call
        // and ignore the ringing call. This case happens when the telephony
        // layer rejects the ringing call while the FG call is dialing/alerting,
        // but the incoming call *does* briefly exist in the DISCONNECTING or
        // DISCONNECTED state.
        if ((ringingCall.getState() != Call.State.IDLE)
                && !cm.getActiveFgCallState().isDialing()) {
            // A phone call is ringing *or* call waiting.
            if (mAllowIncomingCallTouchUi) {
                // Watch out: even if the phone state is RINGING, it's
                // possible for the ringing call to be in the DISCONNECTING
                // state.  (This typically happens immediately after the user
                // rejects an incoming call, and in that case we *don't* show
                // the incoming call controls.)
                if (ringingCall.getState().isAlive()) {
                    if (DBG) log("- updateState: RINGING!  Showing incoming call controls...");
                    showIncomingCallControls = true;
                }

                // Ugly hack to cover up slow response from the radio:
                // if we attempted to answer or reject an incoming call
                // within the last 500 msec, *don't* show the incoming call
                // UI even if the phone is still in the RINGING state.
                long now = SystemClock.uptimeMillis();
                if (now < mLastIncomingCallActionTime + 500) {
                    log("updateState: Too soon after last action; not drawing!");
                    showIncomingCallControls = false;
                }

                // TODO: UI design issue: if the device is NOT currently
                // locked, we probably don't need to make the user
                // double-tap the "incoming call" buttons.  (The device
                // presumably isn't in a pocket or purse, so we don't need
                // to worry about false touches while it's ringing.)
                // But OTOH having "inconsistent" buttons might just make
                // it *more* confusing.
            }
        } else {
            if (mAllowInCallTouchUi || mSettings.mForceTouch) {
                // Ok, the in-call touch UI is available on this platform,
                // so make it visible (with some exceptions):
                if (mInCallScreen.okToShowInCallTouchUi()) {
                    showInCallControls = true;
                } else {
                    if (DBG) log("- updateState: NOT OK to show touch UI; disabling...");
                }
            }
        }

        if (showInCallControls) {
        	
            // TODO change the phone to CallManager
            updateInCallControls(cm.getActiveFgCall().getPhone());
            mInCallControls.setVisibility(View.VISIBLE);
        } else {
        	mInCallControls.setVisibility(View.GONE);
        }

        if (showIncomingCallControls && showInCallControls) {
            throw new IllegalStateException(
                "'Incoming' and 'in-call' touch controls visible at the same time!");
        }

        if (showIncomingCallControls) {
            showIncomingCallWidget(ringingCall);
        } else {
            hideIncomingCallWidget();
        }
	    //Begin deleted by panqianbo for call screen
        //mInCallControls.setVisibility(showInCallControls ? View.VISIBLE : View.GONE);
	   //End
        // TODO: As an optimization, also consider setting the visibility
        // of the overall InCallTouchUi widget to GONE if *nothing at all*
        // is visible right now.
        if(ringingCall.isIdle() && fg.isIdle() && bg.isIdle() )
        {
        	if(mRecordTip != null)
        		mRecordTip.setVisibility(View.GONE);
        	
        	if( tLayoutMore != null)
        		 tLayoutMore .setVisibility(View.GONE);
        	
//    		mStatusBarManager.disable(-1000);
    		flag_Recording = false ;
//        	 Intent home = new Intent(Intent.ACTION_MAIN);  
//             home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
//             home.addCategory(Intent.CATEGORY_HOME);  
//             mInCallScreen.startActivity(home);  

        }
        else 
        {
        	if((!fg.isIdle() || !bg.isIdle()) && CallFeaturesSetting.mRecording && !flag_Recording && mRecordButton != null && mRecordButton.isClickable()&& mRecordButton.isEnabled() && mMoreButton  != null && mMoreButton.getVisibility() == View.VISIBLE)
        	{
        		flag_Recording = true ;
        		mInCallScreen.handleOnscreenButtonClick(R.id.record_button);
        	}
//            mStatusBarManager.disable(-2000);
        }
    }
   
    
    // View.OnClickListener implementation
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
        	case R.id.moreButton:
//        	case R.id.bluetooth_button:
        	case R.id.addCall_button:
        	case R.id.contacts_button:
        	case R.id.hold_button:
        	case R.id.record_button:
//            case R.id.mergeButton:
            case R.id.endButton:
            case R.id.dialpadButton:
            case R.id.muteButton:
            case R.id.swap_button:
            case R.id.merge_button:
//            case R.id.swapButton:
            case R.id.cdmaMergeButton:
            case R.id.manageConferenceButton:
            case R.id.audioButton:	
                // Clicks on the regular onscreen buttons get forwarded
                // straight to the InCallScreen.
                mInCallScreen.handleOnscreenButtonClick(id);
                
                break;
           /* case R.id.moreButton:
            	onShowHideMore() ;
            	break;*/
            default:
                Log.w(LOG_TAG, "onClick: unexpected click: View " + view + ", id " + id);
                break;
        }
    }
    /**
     * Updates the enabledness and "checked" state of the buttons on the
     * "inCallControls" panel, based on the current telephony state.
     */
    void updateInCallControls(Phone phone) {
        int phoneType = phone.getPhoneType();
        InCallControlState inCallControlState = mInCallScreen.getUpdatedInCallControlState();
        boolean showExtraButtonRow = false;

        if (inCallControlState.canAddCall) {
        	mAddCallButton.setVisibility(View.VISIBLE);
        	mAddCallButton.setEnabled(true);
            mMergeButton.setVisibility(View.GONE);
        } else if (inCallControlState.canMerge) {
            if (phoneType == Phone.PHONE_TYPE_CDMA) {
                mMergeButton.setVisibility(View.GONE);
            } else if ((phoneType == Phone.PHONE_TYPE_GSM)
                    || (phoneType == Phone.PHONE_TYPE_SIP)) {
                mMergeButton.setVisibility(View.VISIBLE);
                mMergeButton.setEnabled(true);
                mAddCallButton.setVisibility(View.GONE);
            } else {
                throw new IllegalStateException("Unexpected phone type: " + phoneType);
            }
        } else {
        	
        	mAddCallButton.setVisibility(View.VISIBLE);
        	mAddCallButton.setEnabled(false);
            mMergeButton.setVisibility(View.GONE);
        }
        if (inCallControlState.canAddCall && inCallControlState.canMerge) {
            if ((phoneType == Phone.PHONE_TYPE_GSM)
                    || (phoneType == Phone.PHONE_TYPE_SIP)) {
                Log.w(LOG_TAG, "updateInCallControls: Add *and* Merge enabled," +
                        " but can't show both!");
            } else if (phoneType == Phone.PHONE_TYPE_CDMA) {
                if (DBG) log("updateInCallControls: CDMA: Add and Merge both enabled");
            } else {
                throw new IllegalStateException("Unexpected phone type: " + phoneType);
            }
        }

        mEndButton.setEnabled(inCallControlState.canEndCall);
        mDialpadButton.setEnabled(inCallControlState.dialpadEnabled);
        mDialpadButton.setChecked(inCallControlState.dialpadVisible);
        mMoreButton.setChecked(inCallControlState.moreVisible);
        mMuteButton.setEnabled(inCallControlState.canMute);
        mMuteButton.setChecked(inCallControlState.muteIndicatorOn);
        
//        mBluetoothButton.setEnabled(inCallControlState.bluetoothEnabled);
//   	 	mBluetoothButton.setSelected(inCallControlState.bluetoothIndicatorOn);

        updateAudioButton(inCallControlState);
      if (inCallControlState.canHold) {
//            mHoldButton.setVisibility(View.VISIBLE);
            mHoldButton.setEnabled(true);
//            mHoldButton.setChecked(inCallControlState.onHold);
            
            mHoldButton.setVisibility(View.VISIBLE);
            
            mSwapButton.setVisibility(View.GONE);
            mSwapButton.setEnabled(false);
        } 
       else if (inCallControlState.canSwap) {
    	   mHoldButton.setVisibility(View.GONE);
    	   mHoldButton.setEnabled(false);
    	   
            mSwapButton.setVisibility(View.VISIBLE);
            mSwapButton.setEnabled(true);
//            mHoldButton.setVisibility(View.GONE);
            
        } else {
            // Neither "Hold" nor "Swap" is available.  This can happen for two
            // reasons:
            //   (1) this is a transient state on a device that *can*
            //       normally hold or swap, or
            //   (2) this device just doesn't have the concept of hold/swap.
            //
            // In case (1), show the "Hold" button in a disabled state.  In case
            // (2), remove the button entirely.  (This means that the button row
            // will only have 4 buttons on some devices.)

            if (inCallControlState.supportsHold) {
//                mHoldButton.setVisibility(View.VISIBLE);
                mHoldButton.setEnabled(false);
                mHoldButton.setVisibility(View.VISIBLE);
                
                mSwapButton.setVisibility(View.GONE);
                mSwapButton.setEnabled(true);
            } else {
//                mHoldButton.setVisibility(View.GONE);
                mHoldButton.setEnabled(false);
                mHoldButton.setVisibility(View.VISIBLE);
                
                mSwapButton.setVisibility(View.GONE);
//                mHoldSwapSpacer.setVisibility(View.GONE);
            }
        }
        if (inCallControlState.canSwap && inCallControlState.canHold) {
            // Uh oh, the InCallControlState thinks that Swap *and* Hold
            // should both be available.  This *should* never happen with
            // either GSM or CDMA, but if it's possible on any future
            // devices we may need to re-layout Hold and Swap so they can
            // both be visible at the same time...
            Log.w(LOG_TAG, "updateInCallControls: Hold *and* Swap enabled, but can't show both!");
        }

        // CDMA-specific "Merge" button.
        // This button and its label are totally hidden (rather than just disabled)
        // when the operation isn't available.
        boolean showCdmaMerge =
                (phoneType == Phone.PHONE_TYPE_CDMA) && inCallControlState.canMerge;
        if (showCdmaMerge) {
            mCdmaMergeButton.setVisibility(View.VISIBLE);
            showExtraButtonRow = true;
        } else {
            mCdmaMergeButton.setVisibility(View.GONE);
        }
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            if (inCallControlState.canSwap && inCallControlState.canMerge) {
                // Uh oh, the InCallControlState thinks that Swap *and* Merge
                // should both be available.  This *should* never happen with
                // CDMA, but if it's possible on any future
                // devices we may need to re-layout Merge and Swap so they can
                // both be visible at the same time...
                Log.w(LOG_TAG, "updateInCallControls: Merge *and* Swap" +
                        "enabled, but can't show both!");
            }
        }

        // "Manage conference" (used only on GSM devices)
        // This button and its label are shown or hidden together.
        if (inCallControlState.manageConferenceVisible) {
            mManageConferenceButton.setVisibility(View.VISIBLE);
            showExtraButtonRow = true;
            mManageConferenceButtonImage.setEnabled(inCallControlState.manageConferenceEnabled);
        } else {
            mManageConferenceButton.setVisibility(View.GONE);
        }

        // Finally, update the "extra button row": It's displayed above the
        // "End" button, but only if necessary.  Also, it's never displayed
        // while the dialpad is visible (since it would overlap.)
        if (showExtraButtonRow && !inCallControlState.dialpadVisible) {
            mExtraButtonRow.setVisibility(View.VISIBLE);
        } else {
            mExtraButtonRow.setVisibility(View.GONE);
        }
    }

    //
    // InCallScreen API
    //

    /**
     * @return true if the onscreen touch UI is enabled (for regular
     * "ongoing call" states) on the current device.
     */
    /* package */ boolean isTouchUiEnabled() {
        return mAllowInCallTouchUi || mSettings.mForceTouch;
    }

    /**
     * @return true if the onscreen touch UI is enabled for
     * the "incoming call" state on the current device.
     */
    /* package */ boolean isIncomingCallTouchUiEnabled() {
        return mAllowIncomingCallTouchUi;
    }

    //
    // RotarySelector.OnDialTriggerListener implementation
    //

    /**
     * Handles "Answer" and "Reject" actions for an incoming call.
     * We get this callback from the RotarySelector
     * when the user triggers an action.
     *
     * To answer or reject the incoming call, we call
     * InCallScreen.handleOnscreenButtonClick() and pass one of the
     * special "virtual button" IDs:
     *   - R.id.answerButton to answer the call
     * or
     *   - R.id.rejectButton to reject the call.
     */
    public void onDialTrigger(View v, int whichHandle) {
        log("onDialTrigger(whichHandle = " + whichHandle + ")...");

        /*switch (whichHandle) {
            case RotarySelector.OnDialTriggerListener.LEFT_HANDLE:
                if (DBG) log("LEFT_HANDLE: answer!");
                acceptCallTriggered();
                break;
            case RotarySelector.OnDialTriggerListener.RIGHT_HANDLE:
                if (DBG) log("RIGHT_HANDLE: reject!");
                rejectCallTriggered();
                break;
            default:
                Log.e(LOG_TAG, "onDialTrigger: unexpected whichHandle value: " + whichHandle);
                break;
        }*/

        // Regardless of what action the user did, be sure to clear out
        // the hint text we were displaying while the user was dragging.
        //mInCallScreen.updateRotarySelectorHint(0, 0);
    }

    //
    // RingSelector.OnRingTriggerListener implementation
    //

    /**
     * Handles "Answer" and "Reject" actions for an incoming call.
     * We get this callback from the RotarySelector
     * when the user triggers an action.
     *
     * To answer or reject the incoming call, we call
     * InCallScreen.handleOnscreenButtonClick() and pass one of the
     * special "virtual button" IDs:
     *   - R.id.answerButton to answer the call
     * or
     *   - R.id.rejectButton to reject the call.
     */
    public void onRingTrigger(View v, int whichRing, int whichApp) {
        log("onRingTrigger(whichRing = " + whichRing + ")...");

        /*switch (whichRing) {
            case RingSelector.OnRingTriggerListener.LEFT_RING:
                if (DBG) log("LEFT_RING: answer!");
                acceptCallTriggered();
                break;
            case RingSelector.OnRingTriggerListener.RIGHT_RING:
                if (DBG) log("RIGHT_RING: reject!");
                rejectCallTriggered();
                break;
            default:
                Log.e(LOG_TAG, "onRingTrigger: unexpected whichRing value: " + whichRing);
                break;
        }*/

        // Regardless of what action the user did, be sure to clear out
        // the hint text we were displaying while the user was dragging.
        //mInCallScreen.updateRotarySelectorHint(0, 0);
    }
    //
    // SlidingTab.OnTriggerListener implementation
    //

    public void onGrabbed(View v, int handle) {

    }

    public void onReleased(View v, int handle) {

    }    

    public void onTrigger(View v, int whichHandle) {
    	if (DBG) log("onDialTrigger(whichHandle = " + whichHandle + ")...");

        // On any action by the user, hide the widget:
        hideIncomingCallWidget();

        // ...and also prevent it from reappearing right away.
        // (This covers up a slow response from the radio for some
        // actions; see updateState().)
        mLastIncomingCallActionTime = SystemClock.uptimeMillis();

        // The InCallScreen actually implements all of these actions.
        // Each possible action from the incoming call widget corresponds
        // to an R.id value; we pass those to the InCallScreen's "button
        // click" handler (even though the UI elements aren't actually
        // buttons; see InCallScreen.handleOnscreenButtonClick().)

        if (mInCallScreen == null) {
            Log.wtf(LOG_TAG, "onTrigger(" + whichHandle
                    + ") from incoming-call widget, but null mInCallScreen!");
            return;
        }
        switch (whichHandle) {
            case ANSWER_CALL_ID:
                if (DBG) log("ANSWER_CALL_ID: answer!");
                mInCallScreen.handleOnscreenButtonClick(R.id.incomingCallAnswer);
                break;

            case SEND_SMS_ID:
                if (DBG) log("SEND_SMS_ID!");
                mInCallScreen.handleOnscreenButtonClick(R.id.incomingCallRespondViaSms);
                break;

            case DECLINE_CALL_ID:
                if (DBG) log("DECLINE_CALL_ID: reject!");
                mInCallScreen.handleOnscreenButtonClick(R.id.incomingCallReject);
                break;

            default:
                Log.wtf(LOG_TAG, "onDialTrigger: unexpected whichHandle value: " + whichHandle);
                break;
        }

        // Regardless of what action the user did, be sure to clear out
        // the hint text we were displaying while the user was dragging.
        mInCallScreen.updateIncomingCallWidgetHint(0, 0);
    }
    public void onSlidingTrigger(View v, int whichHandle) {
    
    /* Log.e("slidingview", " incall touch trigger");
            switch (whichHandle) {
            case SlidingTab.OnTriggerListener.LEFT_HANDLE:
                if (DBG) log("LEFT_HANDLE: answer!");
                acceptCallTriggered();
                
                break;

            case SlidingTab.OnTriggerListener.RIGHT_HANDLE:
                if (DBG) log("RIGHT_HANDLE: reject!");

                rejectCallTriggered();
                break;
      
            default:
                Log.e(LOG_TAG, "onDialTrigger: unexpected whichHandle value: " + whichHandle);
                break;
        }

        // Regardless of what action the user did, be sure to clear out
        // the hint text we were displaying while the user was dragging.
        mInCallScreen.updateRotarySelectorHint(0, 0);*/
    
    }

    private void acceptCallTriggered() {
        //hideIncomingCallWidget();
        // ...and also prevent it from reappearing right away.
        // (This covers up a slow response from the radio; see updateState().)
        mLastIncomingCallActionTime = SystemClock.uptimeMillis();

        // Do the appropriate action.
        if (mInCallScreen != null) {
            // Send this to the InCallScreen as a virtual "button click" event:
            mInCallScreen.handleOnscreenButtonClick(R.id.answerButton);
        } else {
            Log.e(LOG_TAG, "answer trigger: mInCallScreen is null");
        }
    }

    private void rejectCallTriggered() {
        //hideIncomingCallWidget();
        // ...and also prevent it from reappearing right away.
        // (This covers up a slow response from the radio; see updateState().)
        mLastIncomingCallActionTime = SystemClock.uptimeMillis();

        // Do the appropriate action.
        if (mInCallScreen != null) {
            // Send this to the InCallScreen as a virtual "button click" event:
            mInCallScreen.handleOnscreenButtonClick(R.id.rejectButton);
        } else {
            Log.e(LOG_TAG, "reject trigger: mInCallScreen is null");
        }
    }

    /**
     * Apply an animation to hide the incoming call widget.
     */
    private void hideIncomingCallWidget() {
    	if (DBG) log("hideIncomingCallWidget()...");
        if (mIncomingCallWidget.getVisibility() != View.VISIBLE
                || mIncomingCallWidget.getAnimation() != null) {
            // Widget is already hidden or in the process of being hidden
            return;
        }
        // Hide the incoming call screen with a transition
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(IN_CALL_WIDGET_TRANSITION_TIME);
        anim.setAnimationListener(new AnimationListener() {

            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationRepeat(Animation animation) {

            }

            public void onAnimationEnd(Animation animation) {
                // hide the incoming call UI.
                mIncomingCallWidget.clearAnimation();
                mIncomingCallWidget.setVisibility(View.GONE);
            }
        });
        mIncomingCallWidget.startAnimation(anim);
    }

    /**
     * Shows the incoming call widget and cancels any animation that may be fading it out.
     */
    private void showIncomingCallWidget(Call ringingCall) {
        if (DBG) log("showIncomingCallWidget()...");

        Animation anim = mIncomingCallWidget.getAnimation();
        if (anim != null) {
            anim.reset();
            mIncomingCallWidget.clearAnimation();
        }

        // Update the MultiWaveView widget's targets based on the state of
        // the ringing call.  (Specifically, we need to disable the
        // "respond via SMS" option for certain types of calls, like SIP
        // addresses or numbers with blocked caller-id.)

        /*boolean allowRespondViaSms = RespondViaSmsManager.allowRespondViaSmsForCall(ringingCall);
        if (allowRespondViaSms) {
            // The MultiWaveView widget is allowed to have all 3 choices:
            // Answer, Decline, and Respond via SMS.
            mIncomingCallWidget.setTargetResources(R.array.incoming_call_widget_3way_targets);
            mIncomingCallWidget.setTargetDescriptionsResourceId(
                    R.array.incoming_call_widget_3way_target_descriptions);
            mIncomingCallWidget.setDirectionDescriptionsResourceId(
                    R.array.incoming_call_widget_3way_direction_descriptions);
        } else {
            // You only get two choices: Answer or Decline.
            mIncomingCallWidget.setTargetResources(R.array.incoming_call_widget_2way_targets);
            mIncomingCallWidget.setTargetDescriptionsResourceId(
                    R.array.incoming_call_widget_2way_target_descriptions);
            mIncomingCallWidget.setDirectionDescriptionsResourceId(
                    R.array.incoming_call_widget_2way_direction_descriptions);
        }*/

        // Watch out: be sure to call reset() and setVisibility() *after*
        // updating the target resources, since otherwise the MultiWaveView
        // widget will make the targets visible initially (even before you
        // touch the widget.)
        mIncomingCallWidget.reset(false);
        mIncomingCallWidget.setVisibility(View.VISIBLE);

        // Finally, manually trigger a "ping" animation.
        //
        // Normally, the ping animation is triggered by RING events from
        // the telephony layer (see onIncomingRing().)  But that *doesn't*
        // happen for the very first RING event of an incoming call, since
        // the incoming-call UI hasn't been set up yet at that point!
        //
        // So trigger an explicit ping() here, to force the animation to
        // run when the widget first appears.
        //
        mHandler.removeMessages(INCOMING_CALL_WIDGET_PING);
        mHandler.sendEmptyMessageDelayed(
               INCOMING_CALL_WIDGET_PING,
                // Visual polish: add a small delay here, to make the
                // MultiWaveView widget visible for a brief moment
                // *before* starting the ping animation.
                // This value doesn't need to be very precise.
                250 /* msec */);
    }

    /**
     * Handles state changes of the Selector widget.  While the user
     * is dragging one of the handles, we display an onscreen hint; see
     * CallCard.getRotateWidgetHint().
     */
    public void onGrabbedStateChange(View v, int grabbedState) {
        if (mInCallScreen != null) {
            // Look up the hint based on which handle is currently grabbed.
            // (Note we don't simply pass grabbedState thru to the InCallScreen,
            // since *this* class is the only place that knows that the left
            // handle means "Answer" and the right handle means "Decline".)
            int hintTextResId, hintColorResId;
            switch (grabbedState) {
                case 0://MultiWaveView.OnTriggerListener.NO_HANDLE:
                case 1://MultiWaveView.OnTriggerListener.CENTER_HANDLE:
                    hintTextResId = 0;
                    hintColorResId = 0;
                    break;
                // TODO: MultiWaveView only has one handle. MultiWaveView could send an event
                // indicating that a snap (but not release) happened. Could be used to show text
                // when user hovers over an item.
                //        case SlidingTab.OnTriggerListener.LEFT_HANDLE:
                //            hintTextResId = R.string.slide_to_answer;
                //            hintColorResId = R.color.incall_textConnected;  // green
                //            break;
                //        case SlidingTab.OnTriggerListener.RIGHT_HANDLE:
                //            hintTextResId = R.string.slide_to_decline;
                //            hintColorResId = R.color.incall_textEnded;  // red
                //            break;
                default:
                    Log.e(LOG_TAG, "onGrabbedStateChange: unexpected grabbedState: "
                          + grabbedState);
                    hintTextResId = 0;
                    hintColorResId = 0;
                    break;
            }

            // Tell the InCallScreen to update the CallCard and force the
            // screen to redraw.
            //mInCallScreen.updateIncomingCallWidgetHint(hintTextResId, hintColorResId);
        }
    }


    /**
     * OnTouchListener used to shrink the "hit target" of some onscreen
     * buttons.
     */
    class SmallerHitTargetTouchListener implements View.OnTouchListener {
        /**
         * Width of the allowable "hit target" as a percentage of
         * the total width of this button.
         */
        private static final int HIT_TARGET_PERCENT_X = 50;

        /**
         * Height of the allowable "hit target" as a percentage of
         * the total height of this button.
         *
         * This is larger than HIT_TARGET_PERCENT_X because some of
         * the onscreen buttons are wide but not very tall and we don't
         * want to make the vertical hit target *too* small.
         */
        private static final int HIT_TARGET_PERCENT_Y = 80;

        // Size (percentage-wise) of the "edge" area that's *not* touch-sensitive.
        private static final int X_EDGE = (100 - HIT_TARGET_PERCENT_X) / 2;
        private static final int Y_EDGE = (100 - HIT_TARGET_PERCENT_Y) / 2;
        // Min/max values (percentage-wise) of the touch-sensitive hit target.
        private static final int X_HIT_MIN = X_EDGE;
        private static final int X_HIT_MAX = 100 - X_EDGE;
        private static final int Y_HIT_MIN = Y_EDGE;
        private static final int Y_HIT_MAX = 100 - Y_EDGE;

        // True if the most recent DOWN event was a "hit".
        boolean mDownEventHit;

        /**
         * Called when a touch event is dispatched to a view. This allows listeners to
         * get a chance to respond before the target view.
         *
         * @return True if the listener has consumed the event, false otherwise.
         *         (In other words, we return true when the touch is *outside*
         *         the "smaller hit target", which will prevent the actual
         *         button from handling these events.)
         */
        public boolean onTouch(View v, MotionEvent event) {
        	
            // if (DBG) log("SmallerHitTargetTouchListener: " + v + ", event " + event);

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // Note that event.getX() and event.getY() are already
                // translated into the View's coordinates.  (In other words,
                // "0,0" is a touch on the upper-left-most corner of the view.)
                int touchX = (int) event.getX();
                int touchY = (int) event.getY();

                int viewWidth = v.getWidth();
                int viewHeight = v.getHeight();

                // Touch location as a percentage of the total button width or height.
                int touchXPercent = (int) ((float) (touchX * 100) / (float) viewWidth);
                int touchYPercent = (int) ((float) (touchY * 100) / (float) viewHeight);
                // if (DBG) log("- percentage:  x = " + touchXPercent + ",  y = " + touchYPercent);

                // TODO: user research: add event logging here of the actual
                // hit location (and button ID), and enable it for dogfooders
                // for a few days.  That'll give us a good idea of how close
                // to the center of the button(s) most touch events are, to
                // help us fine-tune the HIT_TARGET_PERCENT_* constants.

                if (touchXPercent < X_HIT_MIN || touchXPercent > X_HIT_MAX
                        || touchYPercent < Y_HIT_MIN || touchYPercent > Y_HIT_MAX) {
                    // Missed!
                    // if (DBG) log("  -> MISSED!");
                    mDownEventHit = false;
                    return true;  // Consume this event; don't let the button see it
                } else {
                    // Hit!
                    // if (DBG) log("  -> HIT!");
                    mDownEventHit = true;
                    return false;  // Let this event through to the actual button
                }
            } else {
                // This is a MOVE, UP or CANCEL event.
                //
                // We only do the "smaller hit target" check on DOWN events.
                // For the subsequent MOVE/UP/CANCEL events, we let them
                // through to the actual button IFF the previous DOWN event
                // got through to the actual button (i.e. it was a "hit".)
                return !mDownEventHit;
            }
        }
    }

    
    public void setRecordButtonClickable(boolean clickable){
        if (clickable) {
            mRecordButton.setOnClickListener(this);
        }
        mRecordButton.setClickable(clickable);
    }

    public void setRecordImageButtonState(boolean isRecording){
        if (mRecordButton == null) {
            return ;
        }
//    	mRecordButton.setImageResource(isRecording ? R.drawable.btn_record_stop: R.drawable.btn_record_normal);
        if(isRecording)
        {
        	mRecordButton.setBackgroundResource(R.drawable.bottom_button_bg_hline);
        	mRecordTextView.setText(R.string.menu_recording);
        }
        else
        {
        	 mRecordButton.setBackgroundColor(0000);
        	 mRecordTextView.setText(R.string.menu_record);
        }
    }
    public void updateRecrodTextView(String text) {
        /*if (mRecordTextView == null) {
            return ;
        }
        
        if (!TextUtils.isEmpty(text)) {
            mRecordTextView.setText(text);
            mRecordTextView.setTextColor(Color.RED);
        } else {
            mRecordTextView.setText(R.string.menu_record);
            mRecordTextView.setTextColor(mRecordTextDefaultcolor);
        }*/
    }
    
    public void setRecordTipState(boolean state) {
    	Log.d(LOG_TAG, "record tip show: " + state);
    	mRecordTip.setVisibility(state?View.VISIBLE:View.GONE);
    	
    }
    //End

    //Begin added by chenqiang for bug7939,20120713
    public void setHoldImageButtonState(boolean isOnHoldState) {
        if (mHoldButton == null) {
            return ;
        }
        if (isOnHoldState) {
            mHoldButton.setBackgroundResource(R.drawable.bottom_button_bg_hline);
        } else {
            mHoldButton.setBackgroundColor(0000);
        }
    }
    //End

    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
    
    private void updateAudioButton(InCallControlState inCallControlState) {
        if (DBG) log("updateAudioButton()...");

        // The various layers of artwork for this button come from
        // btn_compound_audio.xml.  Keep track of which layers we want to be
        // visible:
        //
        // - This selector shows the blue bar below the button icon when
        //   this button is a toggle *and* it's currently "checked".
        boolean showToggleStateIndication = false;
        //
        // - This is visible if the popup menu is enabled:
        boolean showMoreIndicator = false;
        //
        // - Foreground icons for the button.  Exactly one of these is enabled:
        boolean showSpeakerIcon = false;
        boolean showHandsetIcon = false;
        boolean showBluetoothIcon = false;

        if (inCallControlState.bluetoothEnabled) {
            if (DBG) log("- updateAudioButton: 'popup menu action button' mode...");

            mAudioButton.setEnabled(true);

            // The audio button is NOT a toggle in this state.  (And its
            // setChecked() state is irrelevant since we completely hide the
            // btn_compound_background layer anyway.)

            // Update desired layers:
            showMoreIndicator = true;
            if (inCallControlState.bluetoothIndicatorOn) {
                showBluetoothIcon = true;
            } else if (inCallControlState.speakerOn) {
                showSpeakerIcon = true;
            } else {
                showHandsetIcon = true;
                // TODO: if a wired headset is plugged in, that takes precedence
                // over the handset earpiece.  If so, maybe we should show some
                // sort of "wired headset" icon here instead of the "handset
                // earpiece" icon.  (Still need an asset for that, though.)
            }
        } else if (inCallControlState.speakerEnabled) {
            if (DBG) log("- updateAudioButton: 'speaker toggle' mode...");
            mAudioButton.setEnabled(true);

            // The audio button *is* a toggle in this state, and indicates the
            // current state of the speakerphone.
            mAudioButton.setChecked(inCallControlState.speakerOn);

            // Update desired layers:
            showToggleStateIndication = true;
            showSpeakerIcon = true;
        } else {
            if (DBG) log("- updateAudioButton: disabled...");

            // The audio button is a toggle in this state, but that's mostly
            // irrelevant since it's always disabled and unchecked.
            mAudioButton.setEnabled(false);
            mAudioButton.setChecked(false);
            
            // Update desired layers:
            showToggleStateIndication = true;
            showSpeakerIcon = true;
        }

        // Finally, update the drawable layers (see btn_compound_audio.xml).

        // Constants used below with Drawable.setAlpha():
        final int HIDDEN = 0;
        final int VISIBLE = 255;

        LayerDrawable layers = (LayerDrawable) mAudioButton.getBackground();
        if (DBG) log("- 'layers' drawable: " + layers);

       /* layers.findDrawableByLayerId(R.id.compoundBackgroundItem)
                .setAlpha(showToggleStateIndication ? VISIBLE : HIDDEN);

        layers.findDrawableByLayerId(R.id.moreIndicatorItem)
                .setAlpha(showMoreIndicator ? VISIBLE : HIDDEN);

        layers.findDrawableByLayerId(R.id.bluetoothItem)
                .setAlpha(showBluetoothIcon ? VISIBLE : HIDDEN);

        layers.findDrawableByLayerId(R.id.handsetItem)
                .setAlpha(showHandsetIcon ? VISIBLE : HIDDEN);

        layers.findDrawableByLayerId(R.id.speakerphoneItem)
                .setAlpha(showSpeakerIcon ? VISIBLE : HIDDEN);*/
    }
    
    public void triggerPing() {
        if (DBG) log("triggerPing: mIncomingCallWidget = " + mIncomingCallWidget);

        if (!mInCallScreen.isForegroundActivity()) {
            // InCallScreen has been dismissed; no need to run a ping *or*
            // schedule another one.
            log("- triggerPing: InCallScreen no longer in foreground; ignoring...");
            return;
        }

        if (mIncomingCallWidget == null) {
            // This shouldn't happen; the MultiWaveView widget should
            // always be present in our layout file.
            Log.w(LOG_TAG, "- triggerPing: null mIncomingCallWidget!");
            return;
        }

        if (DBG) log("- triggerPing: mIncomingCallWidget visibility = "
                     + mIncomingCallWidget.getVisibility());

        if (mIncomingCallWidget.getVisibility() != View.VISIBLE) {
            if (DBG) log("- triggerPing: mIncomingCallWidget no longer visible; ignoring...");
            return;
        }

        // Ok, run a ping (and schedule the next one too, if desired...)

        mIncomingCallWidget.ping();

        if (ENABLE_PING_AUTO_REPEAT) {
            // Schedule the next ping.  (ENABLE_PING_AUTO_REPEAT mode
            // allows the ping animation to repeat much faster than in
            // the ENABLE_PING_ON_RING_EVENTS case, since telephony RING
            // events come fairly slowly (about 3 seconds apart.))

            // No need to check here if the call is still ringing, by
            // the way, since we hide mIncomingCallWidget as soon as the
            // ringing stops, or if the user answers.  (And at that
            // point, any future triggerPing() call will be a no-op.)

            // TODO: Rather than having a separate timer here, maybe try
            // having these pings synchronized with the vibrator (see
            // VibratorThread in Ringer.java; we'd just need to get
            // events routed from there to here, probably via the
            // PhoneApp instance.)  (But watch out: make sure pings
            // still work even if the Vibrate setting is turned off!)

            mHandler.sendEmptyMessageDelayed(INCOMING_CALL_WIDGET_PING,
                                             PING_AUTO_REPEAT_DELAY_MSEC);
        }
    }
}
