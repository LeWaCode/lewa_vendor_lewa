package com.lewa.PIM.contacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.lewa.PIM.R;
import com.lewa.PIM.contacts.LayoutQuickContactBadge.QCBadgeOnClickListener;
import com.lewa.PIM.contacts.util.Constants;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.PIM.util.CommonMethod.SelectorOnClickListener;
import com.lewa.os.util.LocationUtil;

import android.R.integer;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.RawContactsEntity;
import android.provider.Telephony.Threads;
import android.speech.tts.ITtsCallback;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.TwoLineListItem;

public class LayoutQuickContactBadge extends QuickContactBadge {

    public LayoutQuickContactBadge(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

    }

    public static class UnknownQCBOnClickListener
            implements OnClickListener, SelectorOnClickListener {
        private Context mContext;
        private String  mNumber;

        public UnknownQCBOnClickListener(
                Context context,
                String number,
                QuickContactBadge quickContactBadge) {
            mContext = context;
            mNumber  = number;
            quickContactBadge.setOnClickListener(this);
        }

        public void onClick(View v) {
            if (!TextUtils.isEmpty(mNumber)) {
                CommonMethod.openSelectorForNewOrEditContact(mContext, mNumber, this);
            }
        }

        @Override
        public void onSelectorItemClick(int item) {
            if (SelectorOnClickListener.NEW_CONTACT_ITEM == item) {
                CommonMethod.newContact(mContext, mNumber);
            }
            else if (SelectorOnClickListener.ADD_TO_CONTACTS_ITEM == item) {
                CommonMethod.createContact(mContext, mNumber);
            }
        }
    }

    public static class CodeQCBadgeOnClickListener extends
            QCBadgeOnClickListener {
        public CodeQCBadgeOnClickListener(Context context,
                Activity contactItemListAdapter, View parent,
                ViewGroup quickLayoutVariable, boolean isLast) {
            super(context, contactItemListAdapter, parent, quickLayoutVariable,
                    isLast);
            // TODO Auto-generated constructor stub
        }

        public CodeQCBadgeOnClickListener(
                Context context,
                Activity contactItemListAdapter,
                View parent,
                ViewGroup quickLayoutVariable,
                long contactId,
                boolean isLast,
                QuickContactBadge quickContact) {
            super(context, contactItemListAdapter, parent, quickLayoutVariable, contactId, isLast, quickContact);
        }

        public void performHideSettingAndLayout(View quickContactLayout,
                boolean toExpand, Integer visibileOption) {
            //((ContactListItemView) quickContactLayout.getParent())
            //        .setQuickExpand(toExpand);
            if (visibileOption != null) {
                quickContactLayout.setVisibility(visibileOption);
            }
            quickContactLayout.getParent().requestLayout();
        }
    }

    public static class XMLQCBadgeOnClickListener extends
            QCBadgeOnClickListener {
        public XMLQCBadgeOnClickListener(Context context,
                Activity contactItemListAdapter, View parent,
                ViewGroup quickLayoutVariable, boolean isLast) {
            super(context, contactItemListAdapter, parent, quickLayoutVariable,
                    isLast);
            // TODO Auto-generated constructor stub
        }

        public XMLQCBadgeOnClickListener(
                Context context,
                Activity contactItemListAdapter,
                View parent,
                ViewGroup quickLayoutVariable,
                long contactId,
                boolean isLast,
                QuickContactBadge quickContact) {
            super(context, contactItemListAdapter, parent, quickLayoutVariable, contactId, isLast, quickContact);
        }

        public void performHideSettingAndLayout(View quickContactLayout,
                boolean toExpand, Integer visibileOption) {

            if (visibileOption != null) {
                if (visibileOption == View.VISIBLE) {
                    QCBadgeOnClickListener.startAnimation(context,
                    // start chenliang
                            (Activity) context, quickContactLayout, true);
                    // end
                }
                quickContactLayout.setVisibility(visibileOption);
            }
        }
    }

    public static abstract class QCBadgeOnClickListener implements
            OnClickListener {
        public static Map<Activity, Boolean> isLastFlag = new HashMap<Activity, Boolean>();
        public static Map<Activity, ViewGroup> enviroment = new HashMap<Activity, ViewGroup>();
        public final Map<Integer, Object> typeInfoPair = new HashMap<Integer, Object>();
        private boolean isSetted;

        // start : enum type
        public static final int TYPE_CENTER_AREA = 5;
        public static final int TYPE_CALL = 0;
        public static final int TYPE_CONTACT = 1;
        public static final int TYPE_SMS = 2;
        public static final int TYPE_EMAIL = 3;
        public static final int TYPE_GEOGRAPHIC_LOCATION = 4;
        // end

        public static int CENTER_AREA_ID;
        public static int CALL_ID;
        public static int CONTACT_ID;
        public static int SMS_ID;
        public static int EMAIL_ID;
        public static int GEOGRAPHIC_LOCATION_ID;
        public static boolean upMoved;
        public static int upMoveHeight;

        static final private int TOKEN_CONTACT_LOOKUP_AND_TRIGGER   = 0;
        static final private int TOKEN_LOCATION_AND_CARDTYPE_LOOKUP = 1;

        private long mContactId;
        private String primaryNumber;
        private String primaryEmail;
        private String location;

        private QueryHandler mQueryHandler;
        
        TwoLineListItem tl;
        View parent;
        ViewGroup quickLayoutVariable;
        Context context;
        boolean handledFirst;
        private Activity envClazz;
        public boolean isLast;

        private QuickContactBadge quickContact;
        
        public static void cleanEnviromentMap(){
            enviroment.clear();
        }

        public void setTopInfo(String personName) {
            this.primaryNumber = personName;
        }

        public void setPersonInfoUnderName(String personInfoUnderName) {
            this.location = personInfoUnderName;
        }

        public static void setResIds(int center_area, int call, int contact,
                int sms, int email, int geographicLocation) {
            CALL_ID = call;
            CONTACT_ID = contact;
            SMS_ID = sms;
            EMAIL_ID = email;
            GEOGRAPHIC_LOCATION_ID = geographicLocation;
            CENTER_AREA_ID = center_area;
        }

        public QCBadgeOnClickListener(Context context,
                Activity contactItemListAdapter, View parent,
                ViewGroup quickLayoutVariable, boolean isLast) {
            this.parent = parent;
            this.quickLayoutVariable = quickLayoutVariable;
            this.context = context;
            if (!enviroment.containsKey(contactItemListAdapter)) {
                enviroment
                        .put(contactItemListAdapter, this.quickLayoutVariable);
            }
            this.envClazz = contactItemListAdapter;
            this.isLast = isLast;
        }

        public QCBadgeOnClickListener(
                Context paramContext,
                Activity contactItemListAdapter,
                View paramParent,
                ViewGroup quickLayoutVariableParam,
                long contactId,
                boolean paramIsLast,
                QuickContactBadge paramQuickContact) {
            context             = paramContext;
            envClazz            = contactItemListAdapter;
            parent              = paramParent;
            quickLayoutVariable = quickLayoutVariableParam;
            mContactId          = contactId;
            isLast              = paramIsLast;
            quickContact        = paramQuickContact;
            paramQuickContact.setOnClickListener(this);
            if (!enviroment.containsKey(contactItemListAdapter)) {
                enviroment.put(contactItemListAdapter, quickLayoutVariable);
            }
        }

        @Override
        public void onClick(View v) {
            if (mContactId > 0) {
                if (null == mQueryHandler) {
                    mQueryHandler = new QueryHandler(context.getContentResolver());
                }
                mQueryHandler.startQuery(
                        TOKEN_CONTACT_LOOKUP_AND_TRIGGER,
                        null,
                        ContactsContract.RawContactsEntity.CONTENT_URI,
                        new String[] {
                                RawContactsEntity.CONTACT_ID,
                                Data.MIMETYPE,
                                Data.DATA1, //NAME, NUMBER, EMAIL, ...
                                Data.IS_PRIMARY},
                        RawContactsEntity.CONTACT_ID + "=" + mContactId,
                        null,
                        null);
            }
        }

        private void onClickQueryResult() {
            if (CENTER_AREA_ID <= 0) {
                setResIds(R.id.twoline_view, R.id.call_btn, -1, R.id.sms_btn, R.id.email_btn, -1);
            }
            
            View quickContactLayout = parent;
            View container = quickContactLayout.findViewById(R.id.quick_contact_icon_container);

            String topInfo = null;
            if (primaryNumber != null) {
                topInfo = primaryNumber;
            } else if (primaryEmail != null) {
                topInfo = primaryEmail;
            }
            tl = (TwoLineListItem) quickContactLayout.findViewById(CENTER_AREA_ID);
            tl.getText1().setText(topInfo);
            if (location != null) {
                tl.getText2().setText(location);
            } else {
                tl.getText2().setText("");
            }
            
            isSetted = true;
            List<View> vs = new ArrayList<View>();                        
            setItemViewAction(TYPE_CALL, CALL_ID, quickContactLayout);
            setItemViewAction(TYPE_CONTACT, CONTACT_ID, quickContactLayout);
            setItemViewAction(TYPE_SMS, SMS_ID, quickContactLayout);
            setItemViewAction(TYPE_EMAIL, EMAIL_ID, quickContactLayout);
            setItemViewAction(TYPE_GEOGRAPHIC_LOCATION, GEOGRAPHIC_LOCATION_ID,
                    quickContactLayout);
            isLastFlag.put(envClazz, this.isLast);

            if (quickContactLayout.getVisibility() == View.VISIBLE) {
                performHideSettingAndLayout(quickContactLayout, false, View.GONE);
                enviroment.put(envClazz, null);
            } else {
                container.setVisibility(View.VISIBLE);
                closeLatestBar(enviroment, envClazz);

                quickContactLayout.setAnimation(AnimationUtils.loadAnimation(
                        context, R.anim.quickcontact));                
                performHideSettingAndLayout(quickContactLayout, true, View.VISIBLE);
                enviroment.put(envClazz, (LinearLayout) quickContactLayout);
                ((FloatContact) context).setQCBadgeOnClickListener(this);
            }

            if (isChildrenInvisible((ViewGroup) container)) {
                tl.getText1().setText(R.string.no_data);
                // begin jxli
                tl.getText2().setText("");
                // end
                container.setVisibility(View.GONE);
            }

        }

        public boolean isLatestBarClosed(Map<Activity, ViewGroup> enviroment,
                Activity envClazz) {
            return enviroment.get(envClazz) != null ? false : true;
        }

        private boolean isChildrenInvisible(ViewGroup quickContactLayout) {
            // TODO Auto-generated method stub
            for (int i = 0; i < quickContactLayout.getChildCount(); i++) {
                if (quickContactLayout.getChildAt(i).getVisibility() != View.VISIBLE) {
                    continue;
                }
                return false;
            }
            return true;
        }

        // start chenliang
        public static void startAnimation(Context mContext, Activity activity,
                final View mLinearLayout) {
            startAnimation(mContext, activity, mLinearLayout, false);
        }

        public static void startAnimation(Context mContext, Activity activity,
                final View mLinearLayout, boolean isXMLState) {

            View targetParent = (View) mLinearLayout.getParent();
            int h = mLinearLayout.getLayoutParams().height;
            h += 2;
            h = isXMLState ? 0 : h;
            // end
            Animation a = new TranslateAnimation(
                    targetParent.getMeasuredWidth()
                            - targetParent.getPaddingLeft()
                            - targetParent.getPaddingRight(), 0.0f, h, h);
            a.setDuration(700);
            a.setStartOffset(0);
            a.setRepeatCount(0);
            a.setInterpolator(AnimationUtils.loadInterpolator(mContext,
                    android.R.anim.overshoot_interpolator/*
                                                          * android.R.anim.
                                                          * bounce_interpolator
                                                          */));
            mLinearLayout.startAnimation(a);
            if (isXMLState) {
                adjust4XML(mLinearLayout, isXMLState);
            } else {
                adjust4Code(mLinearLayout, isXMLState);
            }

        }

        private static void adjust4XML(View mLinearLayout, boolean isXMLState) {
            // TODO Auto-generated method stub
            View currView = (View) mLinearLayout.getParent();
            ListView lView = ((ListView) mLinearLayout.getParent().getParent());
            int screenChildren = ((ListView) mLinearLayout.getParent()
                    .getParent()).getChildCount();
            int itemsCount = lView.getCount(); 
            int childHeight = ((View) (mLinearLayout.getParent()))
                    .getMeasuredHeight();
            int containable = lView.getMeasuredHeight() / childHeight;
            View lastChild = lView.getChildAt(lView.getChildCount() - 1);
            View lastSecondChild = lView.getChildAt(lView.getChildCount() - 2);
            Rect rect = new Rect(lastChild.getLeft(), lastChild.getTop(),
                    lastChild.getRight(), lastChild.getBottom());

                if (currView.getBottom() + childHeight <= lView.getBottom()) {

                } else if (currView.getTop() < lView.getBottom()) {
                    QCBadgeOnClickListener.upMoveHeight = (childHeight
                            - (lView.getBottom() - currView.getBottom()));
                    upMoved = true;
                    ((View) mLinearLayout.getParent().getParent()).scrollBy(0,
                            QCBadgeOnClickListener.upMoveHeight);
                    try {
                        Thread.currentThread().sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else{
                    if (screenChildren == itemsCount
                    && screenChildren < containable + 1) {
                ;
            }
                }

        }

        private static void adjust4Code(final View mLinearLayout,
                boolean isXMLState) {
            View currView = (View) mLinearLayout.getParent();
            ListView lView = ((ListView) mLinearLayout.getParent().getParent());
            int screenChildren = ((ListView) mLinearLayout.getParent()
                    .getParent()).getChildCount();
            int itemsCount = lView.getCount();
            int childHeight = ((View) (mLinearLayout.getParent()))
                    .getMeasuredHeight() / 2;
            int containable = lView.getMeasuredHeight() / childHeight;
            View lastChild = lView.getChildAt(lView.getChildCount() - 1);
            View lastSecondChild = lView.getChildAt(lView.getChildCount() - 2);
            Rect rect = new Rect(lastChild.getLeft(), lastChild.getTop(),
                    lastChild.getRight(), lastChild.getBottom());


            if (!isXMLState
                    && currView.getBottom()  <= lView.getBottom()) {

            } else if (currView.getTop() < lView.getBottom()) {
                QCBadgeOnClickListener.upMoveHeight = 2 * childHeight
                        - (lView.getBottom() - currView.getTop());
                upMoved = true;
                ((View) mLinearLayout.getParent().getParent()).scrollBy(0,
                        QCBadgeOnClickListener.upMoveHeight);
                try {
                    Thread.currentThread().sleep(200);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                {
                    if (screenChildren == itemsCount
                            && screenChildren < containable + 1) {
                        ;
                    }
                }
            }
        }


        public static void isLatestBarVisible() {

        }

        public void closeLatestBar(Map<Activity, ViewGroup> enviroment,
                Activity envClazz) {
            if (enviroment.get(envClazz) != null) {
                performHideSettingAndLayout(enviroment.get(envClazz), false,
                        View.GONE);
            }
        }

        private void setItemViewAction(final int type, final int resid,
                final View quickContactLayout) {
            final Context mContext = context;

            if (resid == -1) {
                ;
            } else if (typeInfoPair.get(type) == null) {
                View container = quickContactLayout
                        .findViewById(R.id.quick_contact_icon_container);
                container.findViewById(resid).setVisibility(View.GONE);
            } else {
                final Object info = typeInfoPair.get(type);
                View container = quickContactLayout
                        .findViewById(R.id.quick_contact_icon_container);

                ImageView iv = ((ImageView) (container.findViewById(resid)));
                iv.setVisibility(View.VISIBLE);
                if (iv != null) {
                    iv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openView(mContext, type, resid, info);
                            // begin jxli
                            if (quickContactLayout.getVisibility() == View.VISIBLE) {
                                performHideSettingAndLayout(quickContactLayout,
                                        false, View.GONE);
                                enviroment.put(envClazz, null);

                            }
                            // end
                            if (QCBadgeOnClickListener.upMoved) {
                                QCBadgeOnClickListener.upMoved = false;
                                // ((ListView)(parent.getParent().getParent())).scrollBy(0,
                                // QCBadgeOnClickListener.upMoveHeight);
                            }
                        }

//                    iv.setOnTouchListener(new OnTouchListener() {

//                        @Override
//                        public boolean onTouch(View arg0, MotionEvent arg1) {
//                            openView(mContext, type, resid, info);
//                            // begin jxli
//                            if (quickContactLayout.getVisibility() == View.VISIBLE) {
//                                performHideSettingAndLayout(quickContactLayout,
//                                        false, View.GONE);
//                                enviroment.put(envClazz, null);
//
//                            }
//                            // end
//                            if (QCBadgeOnClickListener.upMoved) {
//                                QCBadgeOnClickListener.upMoved = false;
//                                // ((ListView)(parent.getParent().getParent())).scrollBy(0,
//                                // QCBadgeOnClickListener.upMoveHeight);
//                            }
//                            return false;
//                        }

                        private void openView(Context context, int type,
                                int res, Object info) {
                            // TODO Auto-generated method stub
                            if (info == null)
                                return;

                            Intent intent = null;
                            Uri uri = null;
                            switch (type) {
                            case TYPE_CALL: {
                                // uri = Uri.fromParts(Constants.SCHEME_TEL,
                                // info.toString(), null);
                                // intent = new Intent(
                                // Intent.ACTION_CALL_PRIVILEGED, uri);
                                CommonMethod.call(context, info.toString());
                                return;
                            }
                            case TYPE_CONTACT: {

                            }
                                break;
                            case TYPE_EMAIL: {
                                uri = Uri.fromParts(Constants.SCHEME_MAILTO,
                                        info.toString(), null);
                                intent = new Intent(Intent.ACTION_SENDTO, uri);
                            }
                                break;
                            case TYPE_GEOGRAPHIC_LOCATION:
                                break;
                            case TYPE_SMS: {
                                uri = Uri.fromParts(Constants.SCHEME_SMSTO,
                                        info.toString(), null);
                                intent = new Intent(Intent.ACTION_SENDTO, uri);
                            }
                                break;
                            default:
                                break;
                            }
                            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            // Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            try {
                                context.startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                if (Intent.ACTION_SENDTO.equals(intent
                                        .getAction())) {
                                    ComponentName comp = new ComponentName(
                                            "com.android.email",
                                            "com.android.email.activity.Welcome");
                                    intent.setComponent(comp);
                                    intent.setAction("android.intent.action.MAIN");
                                    context.startActivity(intent);
                                }
                            }
                        }

                    });
                }

            }
        }

        public abstract void performHideSettingAndLayout(
                View quickContactLayout, boolean toExpand,
                Integer visibileOption);

        public void setSrcImage(QuickContactBadge quickContact) {
            // TODO Auto-generated method stub
            this.quickContact = quickContact;
        }

        public QuickContactBadge getQuickContactImage() {
            return quickContact;
        }


        private class QueryHandler extends AsyncQueryHandler {
            public QueryHandler(ContentResolver cr) {
                super(cr);
            }

            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                boolean trigger = false;
                try {
                    if (TOKEN_CONTACT_LOOKUP_AND_TRIGGER == token) {
                        if (null != cursor) {
                            while (cursor.moveToNext()) {
                                long contactId = cursor.getLong(0);
                                String mimeType = cursor.getString(1);
                                String mimeValue = cursor.getString(2);
                                int primary = cursor.getInt(3);
                                Log.i("QCBadgeOnClickListener",
                                        "onQueryComplete:"
                                        + "contactId=" + contactId
                                        + " mimeType=" + mimeType
                                        + " mimeValue=" + mimeValue
                                        + " primary=" + primary);
                                if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                                    if ((null == primaryNumber) || (0 != primary)) {
                                        primaryNumber = mimeValue;
                                    }
                                }
                                else if (Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                                    if ((null == primaryEmail) || (0 != primary)) {
                                        primaryEmail = mimeValue;
                                    }
                                }

                                trigger = true;
                            }

                            if (!TextUtils.isEmpty(primaryNumber)) {
                                String location = LocationUtil.getPhoneLocation(context, primaryNumber);
                                if (!TextUtils.isEmpty(location)) {
                                    String cardType = LocationUtil.getPhoneCardType(context, primaryNumber);
                                    if (!TextUtils.isEmpty(cardType)) {
                                        location += "("+cardType+")";
                                    }
                                    setPersonInfoUnderName(location);
                                }
                            }
                        }
                    }
                    else if (TOKEN_LOCATION_AND_CARDTYPE_LOOKUP == token) {
                    }
                } finally {
                    if (null != cursor) {
                        cursor.close();
                    }
                }

                if (trigger) {
                    typeInfoPair.put(TYPE_CONTACT, new Object[] {mContactId, null});

                    if (!TextUtils.isEmpty(primaryNumber)) {
                        typeInfoPair.put(TYPE_CALL, primaryNumber);
                        typeInfoPair.put(TYPE_SMS, primaryNumber);
                    }
                    
                    if (!TextUtils.isEmpty(primaryEmail)) {
                        typeInfoPair.put(TYPE_EMAIL, primaryEmail);
                    }
                    
                    onClickQueryResult();
                }
            }
        }
    }

    public static interface FloatContact {
        public void addQuickLayout(View view);

        public QuickContactBadge getQuickContactImage();

        public void setQuickContactImage(QuickContactBadge qcb);

        public void setQuickExpand(boolean iExpanded);

        public QCBadgeOnClickListener getQCBadgeOnClickListener();

        public void setQCBadgeOnClickListener(
                QCBadgeOnClickListener qCBadgeOnClickListener);
    }

    public static interface FloatContact4Adapter {
        public ViewGroup getQuickLayoutVar();

    }
}
