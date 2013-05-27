package com.lewa.player.ui.outer;

import com.lewa.player.R;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class MusicAboutActivity extends Activity{
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.about);
        setContentView(imageView);
    }
}
