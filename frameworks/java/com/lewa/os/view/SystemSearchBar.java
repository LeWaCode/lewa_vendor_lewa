package com.lewa.os.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class SystemSearchBar extends RelativeLayout implements TextWatcher, View.OnClickListener {
    private EditText mFilterEdit;
    private ImageView mExitBtn;
    private SearchBarOwner mOwner;

    public SystemSearchBar(Context context) {
        super(context);
    }

    public SystemSearchBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOwner(SearchBarOwner owner, int editResId, int exitResId) {
        mOwner = owner;
        mFilterEdit = (EditText )findViewById(editResId);
        mFilterEdit.addTextChangedListener(this);
        mExitBtn = (ImageView )findViewById(exitResId);
        mExitBtn.setOnClickListener(this);
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void afterTextChanged(Editable s) {
        String strKey = s.toString();
        mOwner.filterText(strKey);
    }
    
    public void showKeyboardInput() {
        InputMethodManager inputMgr = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMgr.toggleSoftInput(0, 0);
        if (null != mFilterEdit) {
            mFilterEdit.requestFocus();
        }
    }

    public void hideKeyboardInput() {
        InputMethodManager inputMgr = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMgr.isActive()) {
            inputMgr.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    public String getSearchedText() {
        if (null != mFilterEdit) {
            return mFilterEdit.getText().toString();
        }
        else {
            return null;
        }
    }

    public void setSearchedText(String strToSearch) {
        if (null != mFilterEdit) {
            mFilterEdit.setText(strToSearch);
        }
    }

    public void onClick(View v) {
        if (mExitBtn == v) { //(mExitBtn.getId() == v.getId()) {
            mFilterEdit.setText("");
            mOwner.exitSearch();
        }
    }

    public static abstract interface SearchBarOwner {
        //public abstract void clearText();
        public abstract void filterText(String key);
        public abstract void exitSearch();
    }
}