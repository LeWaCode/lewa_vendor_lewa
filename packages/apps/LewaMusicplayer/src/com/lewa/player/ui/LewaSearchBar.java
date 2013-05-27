package com.lewa.player.ui;

import com.lewa.player.R;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LewaSearchBar extends LinearLayout implements OnClickListener,
						OnLongClickListener, TextWatcher{
	private EditText mEditText;
	private ImageView mCancelView;
	private TextView mTipView;
	Context mContext;

	public LewaSearchBar(Context context) {	    
		super(context);		
		// TODO Auto-generated constructor stub
		mContext = context;
	}
	
	public LewaSearchBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		mTipView = (TextView)findViewById(R.id.searchbar_tip);
		if(mContext instanceof AddPlaylistSongsActivity) {
		    mTipView.setVisibility(View.GONE);
		}
		mEditText = (EditText)findViewById(R.id.search_src_text);
		mCancelView = (ImageView)findViewById(R.id.img_search_category_cancel);
		mCancelView.setOnClickListener(this);
		mCancelView.setOnLongClickListener(this);
		mCancelView.setVisibility(INVISIBLE);
		mEditText.addTextChangedListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.img_search_category_cancel) {
			doClearText();
		}
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.img_search_category_cancel) {
			doClearText();
		}
		return false;
	}
	
	private void doClearText() {
		String text = mEditText.getText().toString();
        if (!TextUtils.isEmpty(text)) {
        	mEditText.setText("");
        }
	}

    public void setSearchResult(String str) {
    	mTipView.setText(str);
    }

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		String text = mEditText.getText().toString();
        if (TextUtils.isEmpty(text)) {
        	mCancelView.setVisibility(INVISIBLE);
        } else {
        	mCancelView.setVisibility(VISIBLE);
        }
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		
	}
	
    
}
