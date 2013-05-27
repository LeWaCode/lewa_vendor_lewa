package com.lewa.PIM.mms.ui;

import com.lewa.PIM.R;
import com.lewa.PIM.mms.data.WorkingMessage;
import com.lewa.PIM.mms.model.SlideModel;

import android.R.integer;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ImsAttachmentEditor extends LinearLayout{
	
    static final int MSG_REPLACE_IMAGE    = 1;
    static final int MSG_REPLACE_VIDEO    = 2;
    static final int MSG_REPLACE_AUDIO    = 3;
    static final int MSG_PLAY_VIDEO       = 4;
    static final int MSG_PLAY_AUDIO       = 5;
    static final int MSG_VIEW_IMAGE       = 6;
    static final int MSG_REMOVE_ATTACHMENT = 7;
    
    static final int IMS_ATTACHMENT_ALL_TYPE 	= 0;
    static final int IMS_ATTACHMENT_IMG_TYPE 	= 1;
    static final int IMS_ATTACHMENT_AUDIO_TYPE 	= 2;
    static final int IMS_ATTACHMENT_VIDEO_TYPE 	= 3;
        
	private final Context mImsAttachmentContext;
	private SlideViewInterface mView;
	private Handler mHandler;
	private String mAttachmentPath = null;
	
	public ImsAttachmentEditor(Context context, AttributeSet attrs) {
		super(context, attrs);
		mImsAttachmentContext = context;
	}

	public ImsAttachmentEditor(Context context) {
		super(context);
		mImsAttachmentContext = context;
	}
	
    public void hideView() {
        if (mView != null) {
            ((View)mView).setVisibility(View.GONE);
        }
    }
    
    private SlideViewInterface createView(int type) {
    	boolean inPortrait = inPortraitMode();
    	
        if (type == IMS_ATTACHMENT_IMG_TYPE) {
            return createMediaView(
                    inPortrait ? R.id.ims_image_attachment_view_portrait_stub :
                        R.id.ims_image_attachment_view_landscape_stub,
                    inPortrait ? R.id.ims_image_attachment_view_portrait :
                        R.id.ims_image_attachment_view_landscape,
                    R.id.ims_view_image_button,
                    R.id.ims_replace_image_button, 
                    R.id.ims_remove_image_button, 
                    MSG_VIEW_IMAGE, MSG_REPLACE_IMAGE, MSG_REMOVE_ATTACHMENT);
        }else if (type == IMS_ATTACHMENT_AUDIO_TYPE) {
            return createMediaView(
                    inPortrait ? R.id.ims_audio_attachment_view_portrait_stub :
                        R.id.ims_audio_attachment_view_landscape_stub,
                    inPortrait ? R.id.ims_audio_attachment_view_portrait :
                        R.id.ims_audio_attachment_view_landscape,
                    R.id.ims_play_audio_button, 
                    R.id.ims_replace_audio_button, 
                    R.id.ims_remove_audio_button,
                    MSG_PLAY_AUDIO, MSG_REPLACE_AUDIO, MSG_REMOVE_ATTACHMENT);
			
		}else if (type == IMS_ATTACHMENT_VIDEO_TYPE) {
            return createMediaView(
                    inPortrait ? R.id.ims_video_attachment_view_portrait_stub :
                        R.id.ims_video_attachment_view_landscape_stub,
                    inPortrait ? R.id.ims_video_attachment_view_portrait :
                        R.id.ims_video_attachment_view_landscape,
                    R.id.ims_view_video_button, 
                    R.id.ims_replace_video_button, 
                    R.id.ims_remove_video_button,
                    MSG_PLAY_VIDEO, MSG_REPLACE_VIDEO, MSG_REMOVE_ATTACHMENT);
			
		}else {
            throw new IllegalArgumentException();
        }
    }
    
    private SlideViewInterface createMediaView(
            int stub_view_id, int real_view_id,
            int view_button_id, int replace_button_id, int remove_button_id,
            int view_message, int replace_message, int remove_message) {
    	
        LinearLayout view = (LinearLayout)getStubView(stub_view_id, real_view_id);
        view.setVisibility(View.VISIBLE);

        Button replaceButton = (Button) view.findViewById(replace_button_id);
        Button removeButton = (Button) view.findViewById(remove_button_id);
        Button viewButton = (Button) view.findViewById(view_button_id);

        replaceButton.setOnClickListener(new MessageOnClick(replace_message));
        removeButton.setOnClickListener(new MessageOnClick(remove_message));
        viewButton.setOnClickListener(new MessageOnClick(view_message));			
        return (SlideViewInterface) view;
    }
    
    private View getStubView(int stubId, int viewId) {
        View view = findViewById(viewId);
        if (view == null) {
            ViewStub stub = (ViewStub) findViewById(stubId);
            view = stub.inflate();
        }
        return view;
    }
    
    public void setHandler(Handler handler) {
        mHandler = handler;
    }
    
    private class MessageOnClick implements OnClickListener {
        private int mWhat;

        public MessageOnClick(int what) {
            mWhat = what;
        }

        public void onClick(View v) {
            Message msg = Message.obtain(mHandler, mWhat);
            msg.sendToTarget();
        }
    }
    
    public void update(int type) {
        hideView();
        mView = null;
        mView = createView(type);        
    }
    
    public void reset(){
    	if (mView == null) {
			return ;
		}
    	mView.reset();
    }
    
    public void setImage(String path, Bitmap bitmap) {
    	if (mView == null) {
			return ;
		}
    	mView.setImage(path, bitmap);
        mView.setVisibility(true);
    }
    
    public void setAudio(String name){
    	if (mView == null) {
			return ;
		}
    	mView.setAudio(null, name, null);
    }    
    
    public void playAudio(String path, Recorder recorder){
    	if (mView != null) {
        	((ImsAudioAttachmentView)mView).startAudio(path, recorder);			
		}
    }
    
    public void stopAudio(){
    	if (mView != null) {
			mView.stopAudio();
		}
    }
    
    private boolean inPortraitMode() {
        final Configuration configuration = mImsAttachmentContext.getResources().getConfiguration();
        return configuration.orientation == Configuration.ORIENTATION_PORTRAIT;
    }
    
    public void setImsAttachmentPath(String path){
    	mAttachmentPath = path;
    }
    
    public String getImsAttachmentPath(){
    	return mAttachmentPath;
    }
    
    public boolean hasAttachment(){
    	boolean ret = false;
    	if (mView != null) {
        	if (((View)mView).getVisibility() == View.VISIBLE && mAttachmentPath != null) {
    			ret = true;
    		}			
		}
    	return ret;
    }
}
