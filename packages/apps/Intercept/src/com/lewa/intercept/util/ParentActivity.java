package com.lewa.intercept.util;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CursorAdapter;

public abstract class ParentActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        ActivityPool.add(this); 
    }
    public abstract CursorAdapter getAdapter();
}
