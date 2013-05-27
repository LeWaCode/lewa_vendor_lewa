/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.lewa.PIM.mms.ui;

import com.lewa.PIM.mms.MmsConfig;
import com.lewa.PIM.R;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

/**
 * An adapter to store icons and strings for attachment type list.
 */
public class AttachmentTypeSelectorAdapter extends IconListAdapter {
    public final static int MODE_WITH_SLIDESHOW    = 0;
    public final static int MODE_WITHOUT_SLIDESHOW = 1;

    public final static int ADD_IMAGE               = 0;
    public final static int TAKE_PICTURE            = 1;
    public final static int ADD_VIDEO               = 2;
    public final static int RECORD_VIDEO            = 3;
    public final static int ADD_SOUND               = 4;
    public final static int RECORD_SOUND            = 5;
    public final static int ADD_SLIDESHOW           = 6;
    public final static int ADD_CONTACT_INFO        = 7;
    
    public AttachmentTypeSelectorAdapter(Context context, int mode) {
       super(context, getData(mode, context));
    }
    
    public int buttonToCommand(int whichButton) {
        AttachmentListItem item = (AttachmentListItem)getItem(whichButton);
        return item.getCommand();
    }

    protected static List<IconListItem> getData(int mode, Context context) {
        List<IconListItem> data = new ArrayList<IconListItem>(7);
        
        PackageManager mg = context.getPackageManager();
        //image gallery
        try {
            Drawable dr = mg.getApplicationIcon("com.android.gallery");                
            addItem(data, context.getString(R.string.attach_image), 0, ADD_IMAGE, dr);            
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            
            addItem(data, context.getString(R.string.attach_image),
                    R.drawable.ic_launcher_gallery, ADD_IMAGE);            
        }
        //attach take photo
        try {
            Drawable dr = mg.getApplicationIcon("com.android.camera");                
            addItem(data, context.getString(R.string.attach_take_photo), 0, TAKE_PICTURE, dr);            
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            
            addItem(data, context.getString(R.string.attach_take_photo),
                    R.drawable.ic_launcher_camera, TAKE_PICTURE);            
        }        
        //attach video
//        try {
           // Drawable dr = mg.getApplicationIcon("com.lewa.player");                
          //  addItem(data, context.getString(R.string.attach_video), 0, ADD_VIDEO, dr);        	             
//        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            
            addItem(data, context.getString(R.string.attach_video),
                    R.drawable.ic_launcher_video_player, ADD_VIDEO);            
//        }        
        //record void
        try {
            Drawable dr = mg.getApplicationIcon("com.android.camera");                
            addItem(data, context.getString(R.string.attach_record_video), 0, RECORD_VIDEO, dr);            
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            
            addItem(data, context.getString(R.string.attach_record_video),
                    R.drawable.ic_launcher_camera_record, RECORD_VIDEO);            
        } 
        //sound attach
        if (MmsConfig.getAllowAttachAudio()) {
            try {
                Drawable dr = mg.getApplicationIcon("com.lewa.player");                
                addItem(data, context.getString(R.string.attach_sound), 0, ADD_SOUND, dr);            
            } catch (NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                
                addItem(data, context.getString(R.string.attach_sound),
                        R.drawable.ic_launcher_musicplayer_2, ADD_SOUND);            
            } 
        }
        //record sound
        try {
            Drawable dr = mg.getApplicationIcon("com.android.soundrecorder");                
            addItem(data, context.getString(R.string.attach_record_sound), 0, RECORD_SOUND, dr);            
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            
            addItem(data, context.getString(R.string.attach_record_sound),
                    R.drawable.ic_launcher_record_audio, RECORD_SOUND);            
        }         
        //attach slideshow
        if (mode == MODE_WITH_SLIDESHOW) {
            addItem(data, context.getString(R.string.attach_slideshow),
                    R.drawable.ic_launcher_slideshow_add_sms, ADD_SLIDESHOW);
        }
        //contact info
        try {
            Drawable dr = mg.getApplicationIcon(context.getPackageName());                
            addItem(data, context.getString(R.string.attach_contact_info), 0, ADD_CONTACT_INFO, dr);            
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            
            addItem(data, context.getString(R.string.attach_contact_info),
                    R.drawable.ic_menu_contact, ADD_CONTACT_INFO);            
        }
        return data;
    }

    protected static void addItem(List<IconListItem> data, String title,
            int resource, int command) {
        AttachmentListItem temp = new AttachmentListItem(title, resource, command);
        data.add(temp);
    }
    
    protected static void addItem(List<IconListItem> data, String title, int resource, 
            int command, Drawable drawable) {
        AttachmentListItem temp = new AttachmentListItem(title, resource, command, drawable);
        data.add(temp);
    }
    
    public static class AttachmentListItem extends IconListAdapter.IconListItem {
        private int mCommand;

        public AttachmentListItem(String title, int resource, int command) {
            super(title, resource);

            mCommand = command;
        }

        public AttachmentListItem(String title, int resource, int command, Drawable drawable) {
            super(title, resource, drawable);

            mCommand = command;
        }

        public int getCommand() {
            return mCommand;
        }
    }
}
