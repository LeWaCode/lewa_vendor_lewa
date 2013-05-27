package com.lewa.os.view;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class FootBar extends LinearLayout {
    public FootBar(Context context, int[] btnIds, int[] btnResIds, View.OnClickListener clickListener) {
        super(context);
        for (int i = 0; i < btnIds.length; ++i) {
            ImageView imageV = new ImageView(context);
            imageV.setImageResource(btnResIds[i]);
            imageV.setId(btnIds[i]);
            imageV.setFocusable(true);
            imageV.setOnClickListener(clickListener);

            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            layoutParams.weight = 1.0F;
            addView(imageV, layoutParams);
        }
    }

    public FootBar(Context context, int[] btnIds, int[] btnResIds, int[] btnBgResIds, View.OnClickListener clickListener) {
        super(context);
        for (int i = 0; i < btnIds.length; ++i) {
            ImageView imageV = new ImageView(context);
            imageV.setImageResource(btnResIds[i]);
            imageV.setBackgroundResource(btnBgResIds[i]);
            imageV.setId(btnIds[i]);
            imageV.setFocusable(true);
            imageV.setOnClickListener(clickListener);

            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            layoutParams.weight = 1.0F;
            addView(imageV, layoutParams);
        }
    }

    public void addToView(View view) {
        if (view instanceof ViewGroup) {
            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            ((ViewGroup )view).addView(this, layoutParams);
            //((ViewGroup )view).addView(this);
        }
    }

    public void addButtons(int[] btnIds, int[] btnResIds, View.OnClickListener clickListener) {
        Context context = getContext();
        for (int i = 0; i < btnIds.length; ++i) {
            ImageView imageV = new ImageView(context);
            imageV.setImageResource(btnResIds[i]);
            imageV.setId(btnIds[i]);
            imageV.setFocusable(true);
            imageV.setOnClickListener(clickListener);

            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            layoutParams.weight = 1.0F;
            addView(imageV, layoutParams);
        }
    }

    public void removeButton(int viewId) {
    }

    public void removeAllButtons() {
        super.removeAllViews();
    }

    public void showButton(int viewId) {
        View btn = findViewById(viewId);
        if (null != btn) {
            btn.setVisibility(View.VISIBLE);
        }
    }

    public void hideButton(int viewId) {
        View btn = findViewById(viewId);
        if (null != btn) {
            btn.setVisibility(View.GONE);
        }
    }

    public void show() {
        setVisibility(View.VISIBLE);
    }

    public void hide() {
        setVisibility(View.GONE);
    }
}