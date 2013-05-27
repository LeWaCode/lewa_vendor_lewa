package com.lewa.base.adapter;

import com.lewa.base.adapter.MapAdapter.ActionListener;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.lewa.base.adapter.MapAdapter;
import com.lewa.base.Logs;
import java.util.HashMap;
import java.util.Map;

public class ListenerBox implements OnClickListener, OnTouchListener,
        OnLongClickListener, OnCheckedChangeListener {

    public Map<Integer, ActionListener> handlers = new HashMap<Integer, ActionListener>();
    private MotionEvent onTouch_MotionEvent;
    private boolean onCheckedChange_BooleanArg;
    public MapAdapter basicAdapter;

    public ListenerBox(MapAdapter ba, ActionListener lwListViewHandler) {
        // TODO Auto-generated constructor stub
        this.basicAdapter = ba;
        handlers.put(lwListViewHandler.getListenerType(), lwListViewHandler);

    }
    public void addActionListener(ActionListener lwListViewHandler) {
        // TODO Auto-generated constructor stub
            handlers.put(lwListViewHandler.getListenerType(), lwListViewHandler);
    }
    public MapAdapter getBaseAdapter() {
        return basicAdapter;
    }

    public MotionEvent getOnTouch_MotionEvent() {
        return onTouch_MotionEvent;
    }

    public boolean isOnCheckedChange_BooleanArg() {
        return onCheckedChange_BooleanArg;
    }

    @Override
    public boolean onLongClick(View view) {
        // TODO Auto-generated method stub
        if (handlers.containsKey(ActionListener.OnLongClick)) {
            handlers.get(ActionListener.OnLongClick).handle(view, this);
        }
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // TODO Auto-generated method stub
        Logs.i("", "onTouch --------------- ");
        if (handlers.containsKey(ActionListener.OnTouch)) {
            onTouch_MotionEvent = motionEvent;
            handlers.get(ActionListener.OnTouch).handle(view, this);
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        if (handlers.containsKey(ActionListener.OnClick)) {
            handlers.get(ActionListener.OnClick).handle(view, this);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean bArg) {
        // TODO Auto-generated method stub
        if (handlers.containsKey(ActionListener.OnCheckChanged)) {
            onCheckedChange_BooleanArg = bArg;
            handlers.get(ActionListener.OnCheckChanged).handle(compoundButton, this);
        }
    }
}
