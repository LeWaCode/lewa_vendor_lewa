package com.lewa.player.model;

import java.util.ArrayList;

import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.ui.MusicFolderActivity;
import com.lewa.player.ui.outer.AlbumTrackBrowserActivity;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;


public class MusicFolderAdapter extends BaseAdapter implements MusicUtils.Defs{

    private TextView mFolderTitle;
    private TextView mFolderPath;
    private CheckBox mCheckBox;
    private ArrayList<String> mPathList;
    private ArrayList<String> mPathListInDB;
    private MusicFolderActivity mActivity;
    private int mIsOuter;
    private AsyncQueryHandler mQueryHandler;
    
    public MusicFolderAdapter(MusicFolderActivity activity, ArrayList<String> list, ArrayList<String> listInDB, int isOut) {

        // TODO Auto-generated constructor stub
        mPathList = list;
        mPathListInDB = listInDB;
        mActivity = activity;
        mIsOuter = isOut;
 //       mQueryHandler = new QueryHandler(mActivity.getContentResolver());
    }
    
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        
        if(mIsOuter == 0) {
            if(mPathList != null) {
                return mPathList.size();
            } 
        } else if(mIsOuter == 1){
            if(mPathListInDB != null) {
                return mPathListInDB.size();
            }
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
    
    public AsyncQueryHandler getQueryHandler() {
        return mQueryHandler;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View view;

        view = LayoutInflater.from(mActivity).inflate(
                R.layout.folder_list_item, null);
        mFolderTitle = (TextView) view.findViewById(R.id.folder_title);
        mFolderPath = (TextView) view.findViewById(R.id.folder_path);
        mCheckBox = (CheckBox) view.findViewById(R.id.folder_checked);

        String path;
        if (mIsOuter == 1) {
            path = mPathListInDB.get(position);
        } else {
            path = mPathList.get(position);
        }
        String titleStr = path.substring(path.lastIndexOf("/") + 1,
                path.length());
        String title = titleStr.replaceFirst(titleStr.substring(0, 1), titleStr
                .substring(0, 1).toUpperCase());
        String pathStr = null;
        if (path.startsWith("/mnt/sdcard")) {
            pathStr = path.substring(4, path.length());
        }

        mFolderPath.setText(pathStr);

        if (mIsOuter == 1) {
            ImageView folderIcon = (ImageView) view
                    .findViewById(R.id.folder_icon);
            folderIcon.setImageResource(R.drawable.folder_outer);
            int count = MusicUtils.getSongListForFolder(mActivity, path).length;
            mFolderTitle.setText(title + " (" + count + ") ");
            mCheckBox.setVisibility(View.GONE);
            LinearLayout folderInfo = (LinearLayout) view
                    .findViewById(R.id.folder_info);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            mFolderTitle.setLayoutParams(lp);
            mFolderPath.setLayoutParams(lp);

            mFolderTitle.setTextAppearance(mActivity,
                    com.android.internal.R.style.TextAppearance_Medium);
            mFolderTitle.setTextColor(0xFFFFFFFF);
            mFolderPath.setTextAppearance(mActivity,
                    com.android.internal.R.style.TextAppearance_Small);
            mFolderPath.setTextColor(0xFFD3D3D3);
            view.setTag(path);
        } else {

            mFolderTitle.setText(title);
            mFolderTitle.setTextAppearance(mActivity,
                    com.android.internal.R.style.TextAppearance_Small);
            mFolderPath.setTextSize(12);
            mCheckBox.setVisibility(View.VISIBLE);
            mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                        boolean isChecked) {
                    // TODO Auto-generated method stub
                    mActivity.setItemState(position, isChecked);
                }
            });

            if (mPathListInDB != null && mPathListInDB.size() > 0) {
                if (mPathListInDB.contains(path)) {
                    mCheckBox.setChecked(true);
                } else {
                    mCheckBox.setChecked(false);
                }
            }
            view.setTag(mCheckBox);
        }
        return view;
    }
}
