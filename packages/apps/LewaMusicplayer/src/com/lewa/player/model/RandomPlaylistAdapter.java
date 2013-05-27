package com.lewa.player.model;

import java.util.Random;

import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.online.LocalAsync;
import com.lewa.player.ui.AddPlaylistActivity;
import com.lewa.player.ui.MusicFolderActivity;
import com.lewa.player.ui.outer.PlaylistBrowserActivity;
import com.lewa.player.ui.outer.PlaylistTrackBrowserActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class RandomPlaylistAdapter extends CursorAdapter implements MusicUtils.Defs{
    
    private Context mContext;
    private LayoutInflater mInflater;
    private int mItemHeight;
    private int mPaddingBottom;
    private ContentResolver mResolver;
    private long[] mFavouriteList;
//    private View mFavouriteView;
    private Cursor mCursor;
    private LocalAsync labg;
    private boolean bSetFavouriteBg = false;
    
    public class ItemViewHolder {
        int      playlistId;
        TextView title;
        TextView num;
        View     gridViewItem;
        Bitmap   bitmap;
    }
    
    public static class PlaylistInfo {
        public int playlistId;
        public long[] songsId;
        public String playlistName;
    }
    
    public RandomPlaylistAdapter(Context context) {
        super(context, null);
        
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        Resources resources = context.getResources();
        mItemHeight = 
            resources.getDimensionPixelOffset(R.dimen.grid_item_height);
        mPaddingBottom = 
            resources.getDimensionPixelOffset(R.dimen.grid_padding_bottom);
        
        mResolver = mContext.getContentResolver();
        if(IsCanUseSdCard()) {
            updateFavouriteList();
        }else {
        	mFavouriteList = null;
        }

    }
    
    public void updateFavouriteList() {
        mFavouriteList = MusicUtils.getFavouriteTracks(mContext);        
    }
    
    public boolean IsCanUseSdCard() { 
        try { 
            return Environment.getExternalStorageState().equals( 
                    Environment.MEDIA_MOUNTED); 
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
        return false; 
    } 
    
    public int getHeight(){
        return mItemHeight;
    }
    
    public int getPaddingBottom() {
        return mPaddingBottom;
    }
    
    public void setPlaylistCursor(Cursor cursor) {        
        mCursor = cursor;
    }
    
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        if (mCursor != null)
            return 3 + mCursor.getCount();
        return 3;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }
    public int getRealPosition(int position) {
        if (mCursor != null) 
            if (position != 0  && position != 1)
                return position - 2;
            
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View v = null;
        PlaylistContextMenuListener listContextMenuListener = new PlaylistContextMenuListener();
        PlaylistClickListener listCliskListener = new PlaylistClickListener();
        if(convertView == null || convertView.getTag() == null) {
            if (position == getCount() - 1) {
                v = mInflater.inflate(R.layout.playlist_grid_new, parent, false);
            } else {
                if (position != 0 && position != 1) {
                    if (!mCursor.moveToPosition(getRealPosition(position))) {
                        throw new IllegalStateException("couldn't move cursor to position " + position);
                    }
                }
//                if (position != 0 || mFavouriteView == null) {
//                    v = newView(mContext, mCursor, parent);
//                }
//                if (position == 0 && mFavouriteView == null) {
//                    bSetFavouriteBg = false;
//                    mFavouriteView = v;
//                }
                v = newView(mContext, mCursor, parent);
            }
        } else {
            v = convertView;
        }
        if (position == 0) {
//            if (mFavouriteView != null) {
//                v = mFavouriteView;
//            }
            ItemViewHolder holder = (ItemViewHolder)v.getTag();            
            holder.title.setText(R.string.favourite_list_title);
            int count;
            if(mFavouriteList == null || MusicUtils.mHasSongs == false) 
                count = 0;
            else count = mFavouriteList.length;
            String string = mContext.getResources().getQuantityString(R.plurals.Nsongs, count, count);
            holder.num.setText(string);
//            if (bSetFavouriteBg == false) {
//                bSetFavouriteBg = true;
                
                Bitmap bm = MusicUtils.getDefaultBg(mContext, R.drawable.playlist_default);
                v.setBackgroundDrawable(new BitmapDrawable(bm));
//            }
            v.setOnClickListener(listCliskListener);
            v.setOnCreateContextMenuListener(listContextMenuListener);
        } else if (position == getCount() - 1) {
            bindAddView(v, parent);
        } else if (position == 1) {
            ItemViewHolder holder = (ItemViewHolder)v.getTag();            
            holder.title.setText(R.string.title_folder);
            int count = MusicUtils.getFolderPath(mContext).length;//MusicUtils.getPathList(mContext).size();
            if(count == 0) {
                count = MusicUtils.getPathList(mContext).size();
            }
            String string = mContext.getResources().getQuantityString(R.plurals.Nfolders, count, count);
            holder.num.setText(string);

//            Bitmap bm = MusicUtils.getDefaultBg(mContext, R.drawable.playlist_default);
//            v.setBackgroundDrawable(new BitmapDrawable(bm));
            v.setBackgroundResource(R.drawable.playlist_default_1);
            holder.playlistId = -1;
            v.setOnClickListener(listCliskListener);
        } else {
            bindView(v, mContext, mCursor);
            v.setOnClickListener(listCliskListener);
            v.setOnCreateContextMenuListener(listContextMenuListener);
        }
        return v;
    }

    @Override
    public void bindView(final View view, Context context, Cursor cursor) {
        // TODO Auto-generated method stub
            final ItemViewHolder holder = (ItemViewHolder)view.getTag();
            
            //set playlist title
            int titleIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME);             
            holder.title.setText(cursor.getString(titleIdx));
            
            //set songs' num of the playlist
            int playlistId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists._ID);
            holder.playlistId = Integer.parseInt(mCursor.getString(playlistId));
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", holder.playlistId);
            StringBuilder where = new StringBuilder();
            where.append(MediaStore.Audio.Media.TITLE + " != ''"); 
            where.append(MusicUtils.getWhereBuilder(mContext, "_id", 1));
            Cursor cursorSongs = mResolver.query(uri, null, where.toString(), null, 
                    MediaStore.Audio.Playlists.Members.PLAY_ORDER);
            if (cursorSongs != null) {
                String string = mContext.getResources().getQuantityString(R.plurals.Nsongs, cursorSongs.getCount(), cursorSongs.getCount());
                holder.num.setText(string);
                cursorSongs.close();
                cursorSongs = null;
            }
            
            Bitmap bitmap = MusicUtils.getPlaylistCover(holder.playlistId);
            if(bitmap != null) {
                view.setBackgroundDrawable(new BitmapDrawable(bitmap)); 
            } else {
                view.setBackgroundResource(R.drawable.playlist_default_1);
            }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // TODO Auto-generated method stub
        View v = mInflater.inflate(R.layout.playlist_grid, parent, false);
        ItemViewHolder holder = new ItemViewHolder();
        holder.title= (TextView)v.findViewById(R.id.playlisttitle);
        holder.num = (TextView)v.findViewById(R.id.playlistcount);
        holder.gridViewItem = v;        
        v.setTag(holder);
        return v;
    }
    
    public void bindAddView(View view, ViewGroup parent) {        
        View layout = view.findViewById(R.id.layout_playlist_add);
        
        layout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setClass(mContext, AddPlaylistActivity.class);
                mContext.startActivity(intent);

            }
            
        });
    }
    
    private class PlaylistClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            ItemViewHolder holder = (ItemViewHolder)v.getTag();
            if(holder != null) {
                Intent intent = new Intent();
                 
                if (holder.playlistId == 0) {
                    intent.putExtra("ifRandom", 0);
                    intent.setClass(mContext, PlaylistTrackBrowserActivity.class);
                } else if(holder.playlistId == -1) {
                    intent.putExtra("isOuter", 1);
                    intent.setClass(mContext, MusicFolderActivity.class);
                }  else {
                    intent.putExtra("playlistid", holder.playlistId);
                    intent.putExtra("title", holder.title.getText());
                    intent.putExtra("ifRandom", 2);
                    intent.putExtra("playlistcurrent", getPlaylistCurrent(v));
                    intent.setClass(mContext, PlaylistTrackBrowserActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
            }
        }   
    };
    
    public class PlaylistContextMenuListener implements View.OnCreateContextMenuListener {

        @Override
        public void onCreateContextMenu(ContextMenu menu, View view,
                ContextMenuInfo menuInfo) {
            // TODO Auto-generated method stub
            int id = ((ItemViewHolder)view.getTag()).playlistId;
            menu.add(0, PLAY_SELECTION, 0, R.string.play_selection);
//            menu.add(0, MusicUtils.Defs.SHARE_LIST, 0, R.string.share_list);
            
            String name = (String) ((ItemViewHolder)view.getTag()).title.getText();
            PlaylistInfo playlist = new PlaylistInfo();
            playlist.playlistId = id;
            playlist.playlistName = name;
            if(id != 0) {
                playlist.songsId = getPlaylistCurrent(view);                
                menu.add(0, EDIT_PLAYLIST, 0, R.string.edit_playlist);
                menu.add(0, DELETE_ITEM, 0, R.string.delete_item);
            } else {
                playlist.songsId = mFavouriteList;
            }
            
            SubMenu sub = menu.addSubMenu(0, ADD_TO_PLAYLIST, 0, R.string.add_to_playlist);
            MusicUtils.makePlaylistMenu(mContext, sub);
            
            ((PlaylistBrowserActivity)mContext).setPlaylistSelected(playlist);
            menu.setHeaderTitle(name);            
        }
    }
    private long[] getPlaylistCurrent(View view) {
        int id = ((ItemViewHolder)view.getTag()).playlistId;
        long[] playlist;
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media.TITLE + " != ''"); 
        where.append(MusicUtils.getWhereBuilder(mContext, "_id", 1));
        Cursor cursorSongs = mResolver.query(uri, null, where.toString(), null,
                MediaStore.Audio.Playlists.Members.PLAY_ORDER);
        playlist = MusicUtils.getSongListForCursor(cursorSongs);
        return playlist;
    }

/*    public class LoadCoverAsync extends AsyncTask<ItemViewHolder, Integer, ItemViewHolder>{

        @Override
        protected ItemViewHolder doInBackground(ItemViewHolder... arg0) {
            // TODO Auto-generated method stub
            ItemViewHolder viewHolder = new ItemViewHolder();
            viewHolder.playlistId = arg0[0].playlistId;
            viewHolder.gridViewItem = arg0[0].gridViewItem;
            viewHolder.bitmap = MusicUtils.getPlaylistCover(arg0[0].playlistId);
            return viewHolder;
        }
        
        @Override
        protected void onPostExecute(ItemViewHolder holder) {
            // TODO Auto-generated method stub
            //super.onPostExecute(result);
            if(holder != null && holder.gridViewItem != null) {
                if (holder.bitmap != null) {
                    BitmapDrawable bd = new BitmapDrawable(holder.bitmap);
                    holder.gridViewItem.setBackgroundDrawable(bd);
                } else {
//                    Random random = new Random();
//                    int resourceId = random.nextInt(5);
                    int resourceId = R.drawable.playlist_default_1; //holder.playlistId % 5;
                    switch (resourceId) {
//                        case 0: resourceId = R.drawable.playlist_default_0; break;                        
//                        case 1: resourceId = R.drawable.playlist_default_1; break;
//                        case 2: resourceId = R.drawable.playlist_default_2; break;
//                        case 3: resourceId = R.drawable.playlist_default_3; break;
//                        case 4: resourceId = R.drawable.playlist_default_4; break;
                        default : resourceId = R.drawable.playlist_default_1; break;
                    }
                    holder.gridViewItem.setBackgroundResource(resourceId);
                }
            }            
        }       
    } */
}
